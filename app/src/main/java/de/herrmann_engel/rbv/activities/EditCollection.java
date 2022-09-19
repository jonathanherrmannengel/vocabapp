package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.inputfilters.OnlyEmojisInputFilter;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.Objects;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update;

public class EditCollection extends AppCompatActivity {

    DB_Collection collection;
    TextView collectionName;
    TextView collectionDesc;
    EmojiEditText collectionEmoji;
    private int collectionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_collection_or_pack);

        collectionName = findViewById(R.id.edit_pack_name);
        collectionDesc = findViewById(R.id.edit_pack_desc);
        collectionDesc
                .setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc)));
        collectionEmoji = findViewById(R.id.edit_pack_emoji);
        collectionEmoji
                .setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_emoji)));
        EmojiPopup emojiPopup = new EmojiPopup(findViewById(R.id.root_edit_pack), collectionEmoji);
        collectionEmoji.setFilters(new InputFilter[]{new OnlyEmojisInputFilter()});
        collectionEmoji.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (!emojiPopup.isShowing()) {
                    emojiPopup.show();
                }
            } else if (emojiPopup.isShowing()) {
                emojiPopup.dismiss();
            }
        });
        collectionNo = getIntent().getExtras().getInt("collection");
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        try {
            collection = dbHelperGet.getSingleCollection(collectionNo);
            collectionName.setText(collection.name);
            collectionDesc.setText(collection.desc);
            collectionEmoji.setText(collection.emoji);
            collectionEmoji.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    collectionEmoji.removeTextChangedListener(this);
                    collectionEmoji.setText(s.subSequence(start, start + count));
                    collectionEmoji.addTextChangedListener(this);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            LinearLayout colorPicker = findViewById(R.id.color_picker);
            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            for (int i = 0; i < colors.length() && i < colorsBackground.length(); i++) {
                int color = colors.getColor(i, 0);
                int colorBackground = colorsBackground.getColor(i, 0);
                if (collection.colors == i) {
                    setColors(color, colorBackground);
                }
                ImageButton colorView = new ImageButton(this);
                colorView.setImageDrawable(new ColorDrawable(color));
                int margin = Math.round(
                        10 * ((float) getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(margin * 3, margin * 3);
                lp.setMargins(margin, margin, margin, margin);
                colorView.setLayoutParams(lp);
                colorView.setPadding(0, 0, 0, 0);
                int finalI = i;
                colorView.setOnClickListener(
                        v -> {
                            setColors(color, colorBackground);
                            collection.colors = finalI;
                        });
                colorPicker.addView(colorView);
            }
            colors.recycle();
            colorsBackground.recycle();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    public void saveChanges(MenuItem menuItem) {
        collection.name = collectionName.getText().toString();
        collection.desc = collectionDesc.getText().toString();
        if (collectionEmoji.getText() != null && !collectionEmoji.getText().toString().isEmpty()) {
            BreakIterator iterator = BreakIterator.getCharacterInstance(Locale.ROOT);
            String emojiText = Objects.requireNonNull(collectionEmoji.getText()).toString();
            iterator.setText(emojiText);
            collection.emoji = emojiText.substring(iterator.first(), iterator.next());
        } else {
            collection.emoji = null;
        }
        DB_Helper_Update dbHelperUpdate = new DB_Helper_Update(this);
        if (dbHelperUpdate.updateCollection(collection)) {
            startViewCollection();
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_values, Toast.LENGTH_SHORT).show();
        }
    }

    private void setColors(int main, int background) {
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(main));
        Window window = this.getWindow();
        window.setStatusBarColor(main);
        findViewById(R.id.root_edit_pack).setBackgroundColor(background);
    }

    private void startViewCollection() {
        Intent intent = new Intent(getApplicationContext(), ViewCollection.class);
        intent.putExtra("collection", collectionNo);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        String name = collectionName.getText().toString();
        String desc = collectionDesc.getText().toString();
        if (collection == null || (collection.name.equals(name) && collection.desc.equals(desc))) {
            startViewCollection();
        } else {
            Dialog confirmCancel = new Dialog(this, R.style.dia_view);
            confirmCancel.setContentView(R.layout.dia_confirm);
            confirmCancel.setTitle(getResources().getString(R.string.discard_changes));
            confirmCancel.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            Button confirmCancelY = confirmCancel.findViewById(R.id.dia_confirm_yes);
            Button confirmCancelN = confirmCancel.findViewById(R.id.dia_confirm_no);
            confirmCancelY.setOnClickListener(v -> startViewCollection());
            confirmCancelN.setOnClickListener(v -> confirmCancel.dismiss());
            confirmCancel.show();
        }
    }
}
