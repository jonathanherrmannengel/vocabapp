package de.herrmann_engel.rbv;

import androidx.appcompat.app.AppCompatActivity;

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

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.inputfilters.OnlyEmojisInputFilter;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class EditPack extends AppCompatActivity {

    private int collectionNo;
    private int packNo;
    private boolean reverse;
    private int sort;
    private String searchQuery;
    private int cardPosition;
    private ArrayList<Integer> savedList;

    private DB_Pack pack;
    private TextView packName;
    private TextView packDesc;
    EmojiEditText packEmoji;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_collection_or_pack);
        packName = findViewById(R.id.edit_pack_name);
        packDesc = findViewById(R.id.edit_pack_desc);
        packDesc.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc)));
        packEmoji = findViewById(R.id.edit_pack_emoji);
        packEmoji
                .setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_emoji)));
        EmojiPopup emojiPopup = new EmojiPopup(findViewById(R.id.root_edit_pack), packEmoji);
        packEmoji.setFilters(new InputFilter[] { new OnlyEmojisInputFilter() });
        packEmoji.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (!emojiPopup.isShowing()) {
                    emojiPopup.show();
                }
            } else if (emojiPopup.isShowing()) {
                emojiPopup.dismiss();
            }
        });
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        searchQuery = getIntent().getExtras().getString("searchQuery");
        cardPosition = getIntent().getExtras().getInt("cardPosition");
        savedList = getIntent().getExtras().getIntegerArrayList("savedList");
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        try {
            pack = dbHelperGet.getSinglePack(packNo);
            packName.setText(pack.name);
            packDesc.setText(pack.desc);
            packEmoji.setText(pack.emoji);
            packEmoji.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    packEmoji.removeTextChangedListener(this);
                    packEmoji.setText(s.subSequence(start, start + count));
                    packEmoji.addTextChangedListener(this);
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
                if (pack.colors == i) {
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
                            pack.colors = finalI;
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
        DB_Helper_Update dbHelperUpdate = new DB_Helper_Update(this);
        pack.name = packName.getText().toString();
        pack.desc = packDesc.getText().toString();
        if (packEmoji.getText() != null && !packEmoji.getText().toString().isEmpty()) {
            BreakIterator iterator = BreakIterator.getCharacterInstance(Locale.ROOT);
            String emojiText = packEmoji.getText().toString();
            iterator.setText(emojiText);
            pack.emoji = emojiText.substring(iterator.first(), iterator.next());
        } else {
            pack.emoji = null;
        }
        if (dbHelperUpdate.updatePack(pack)) {
            startViewPack();
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

    private void startViewPack() {
        Intent intent = new Intent(getApplicationContext(), ViewPack.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", cardPosition);
        intent.putIntegerArrayListExtra("savedList", savedList);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        String name = packName.getText().toString();
        String desc = packDesc.getText().toString();
        if (pack == null || (pack.name.equals(name) && pack.desc.equals(desc))) {
            startViewPack();
        } else {
            Dialog confirmCancel = new Dialog(this, R.style.dia_view);
            confirmCancel.setContentView(R.layout.dia_confirm);
            confirmCancel.setTitle(getResources().getString(R.string.discard_changes));
            confirmCancel.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            Button confirmCancelY = confirmCancel.findViewById(R.id.dia_confirm_yes);
            Button confirmCancelN = confirmCancel.findViewById(R.id.dia_confirm_no);
            confirmCancelY.setOnClickListener(v -> startViewPack());
            confirmCancelN.setOnClickListener(v -> confirmCancel.dismiss());
            confirmCancel.show();
        }
    }
}
