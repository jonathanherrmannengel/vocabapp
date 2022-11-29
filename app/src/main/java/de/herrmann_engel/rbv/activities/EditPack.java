package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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

import com.google.android.material.textfield.TextInputLayout;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTheming;
import com.vanniktech.emoji.inputfilters.OnlyEmojisInputFilter;

import java.util.ArrayList;
import java.util.Objects;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update;
import de.herrmann_engel.rbv.utils.StringTools;

public class EditPack extends AppCompatActivity {

    private int collectionNo;
    private int packNo;
    private boolean reverse;
    private int sort;
    private String searchQuery;
    private int cardPosition;
    private ArrayList<Integer> savedList;
    private Long savedListSeed;
    private DB_Pack pack;
    private TextView packName;
    private TextView packDesc;
    private EmojiEditText packEmoji;
    private TextInputLayout packNameLayout;
    private TextInputLayout packDescLayout;
    private TextInputLayout packEmojiLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_collection_or_pack);
        packName = findViewById(R.id.edit_collection_or_pack_name);
        packNameLayout = findViewById(R.id.edit_collection_or_pack_name_layout);
        packNameLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.light_black, getTheme())));
        packDesc = findViewById(R.id.edit_collection_or_pack_desc);
        packDescLayout = findViewById(R.id.edit_collection_or_pack_desc_layout);
        packDescLayout.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc)));
        packDescLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.light_black, getTheme())));
        packEmoji = findViewById(R.id.edit_collection_or_pack_emoji);
        EmojiPopup emojiPopup = new EmojiPopup(findViewById(R.id.root_edit_collection_or_pack), packEmoji,
                new EmojiTheming(
                        getResources().getColor(R.color.light_grey, getTheme()),
                        getResources().getColor(R.color.light_black, getTheme()),
                        getResources().getColor(R.color.warn_red, getTheme()),
                        getResources().getColor(R.color.button, getTheme()),
                        getResources().getColor(R.color.light_black, getTheme()),
                        getResources().getColor(R.color.dark_grey, getTheme())
                )
        );
        packEmoji.setFilters(new InputFilter[]{new OnlyEmojisInputFilter()});
        packEmoji.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (!emojiPopup.isShowing()) {
                    emojiPopup.show();
                }
            } else if (emojiPopup.isShowing()) {
                emojiPopup.dismiss();
            }
        });
        packEmoji.setOnClickListener(v -> {
            if (!emojiPopup.isShowing()) {
                emojiPopup.show();
            }
        });
        packEmojiLayout = findViewById(R.id.edit_collection_or_pack_emoji_layout);
        packEmojiLayout.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_emoji)));
        packEmojiLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.light_black, getTheme())));
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        searchQuery = getIntent().getExtras().getString("searchQuery");
        cardPosition = getIntent().getExtras().getInt("cardPosition");
        savedList = getIntent().getExtras().getIntegerArrayList("savedList");
        savedListSeed = getIntent().getExtras().getLong("savedListSeed");
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
            TypedArray colorNames = getResources().obtainTypedArray(R.array.pack_color_names);
            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            for (int i = 0; i < colorNames.length() && i < colors.length() && i < colorsBackground.length(); i++) {
                String colorName = colorNames.getString(i);
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
                colorView.setContentDescription(colorName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    colorView.setTooltipText(colorName);
                }
                colorPicker.addView(colorView);
            }
            colorNames.recycle();
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
        pack.name = packName.getText().toString();
        pack.desc = packDesc.getText().toString();
        if (packEmoji.getText() != null) {
            pack.emoji = (new StringTools()).firstEmoji(packEmoji.getText().toString());
        } else {
            pack.emoji = null;
        }
        DB_Helper_Update dbHelperUpdate = new DB_Helper_Update(this);
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
        packNameLayout.setBoxStrokeColor(main);
        packDescLayout.setBoxStrokeColor(main);
        packEmojiLayout.setBoxStrokeColor(main);
        findViewById(R.id.root_edit_collection_or_pack).setBackgroundColor(background);
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
        intent.putExtra("savedListSeed", savedListSeed);
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
