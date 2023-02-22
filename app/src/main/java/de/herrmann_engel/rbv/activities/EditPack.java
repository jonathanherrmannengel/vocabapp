package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTheming;
import com.vanniktech.emoji.inputfilters.OnlyEmojisInputFilter;

import java.util.Objects;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.databinding.ActivityEditCollectionOrPackBinding;
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update;
import de.herrmann_engel.rbv.utils.StringTools;

public class EditPack extends AppCompatActivity {

    private ActivityEditCollectionOrPackBinding binding;
    private DB_Pack pack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCollectionOrPackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.editCollectionOrPackDescLayout.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc)));
        EmojiPopup emojiPopup = new EmojiPopup(binding.getRoot(), binding.editCollectionOrPackEmoji,
                new EmojiTheming(
                        getResources().getColor(R.color.light_grey_default, getTheme()),
                        getResources().getColor(R.color.light_black, getTheme()),
                        getResources().getColor(R.color.highlight, getTheme()),
                        getResources().getColor(R.color.button, getTheme()),
                        getResources().getColor(R.color.light_black, getTheme()),
                        getResources().getColor(R.color.dark_grey, getTheme())
                )
        );
        binding.editCollectionOrPackEmoji.setFilters(new InputFilter[]{new OnlyEmojisInputFilter()});
        binding.editCollectionOrPackEmoji.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (!emojiPopup.isShowing()) {
                    emojiPopup.show();
                }
            } else if (emojiPopup.isShowing()) {
                emojiPopup.dismiss();
            }
        });
        binding.editCollectionOrPackEmoji.setOnClickListener(v -> {
            if (!emojiPopup.isShowing()) {
                emojiPopup.show();
            }
        });
        binding.editCollectionOrPackEmojiLayout.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_emoji)));
        int packNo = getIntent().getExtras().getInt("pack");
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        try {
            pack = dbHelperGet.getSinglePack(packNo);
            binding.editCollectionOrPackName.setText(pack.name);
            binding.editCollectionOrPackDesc.setText(pack.desc);
            binding.editCollectionOrPackEmoji.setText(pack.emoji);
            binding.editCollectionOrPackEmoji.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    binding.editCollectionOrPackEmoji.removeTextChangedListener(this);
                    binding.editCollectionOrPackEmoji.setText(s.subSequence(start, start + count));
                    binding.editCollectionOrPackEmoji.addTextChangedListener(this);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            TypedArray colorNames = getResources().obtainTypedArray(R.array.pack_color_names);
            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsStatusBar = getResources().obtainTypedArray(R.array.pack_color_statusbar);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            for (int i = 0; i < colorNames.length() && i < colors.length() && i < colorsStatusBar.length() && i < colorsBackground.length(); i++) {
                String colorName = colorNames.getString(i);
                int color = colors.getColor(i, 0);
                int colorStatusBar = colorsStatusBar.getColor(i, 0);
                int colorBackground = colorsBackground.getColor(i, 0);
                if (pack.colors == i) {
                    setColors(color, colorStatusBar, colorBackground);
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
                            setColors(color, colorStatusBar, colorBackground);
                            pack.colors = finalI;
                        });
                colorView.setContentDescription(colorName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    colorView.setTooltipText(colorName);
                }
                binding.colorPicker.addView(colorView);
            }
            colorNames.recycle();
            colors.recycle();
            colorsBackground.recycle();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    public void saveChanges(MenuItem menuItem) {
        pack.name = binding.editCollectionOrPackName.getText().toString();
        pack.desc = binding.editCollectionOrPackDesc.getText().toString();
        if (binding.editCollectionOrPackEmoji.getText() != null) {
            pack.emoji = (new StringTools()).firstEmoji(binding.editCollectionOrPackEmoji.getText().toString());
        } else {
            pack.emoji = null;
        }
        DB_Helper_Update dbHelperUpdate = new DB_Helper_Update(this);
        if (dbHelperUpdate.updatePack(pack)) {
            this.finish();
        } else {
            Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show();
        }
    }

    private void setColors(int main, int statusBar, int background) {
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(statusBar));
        Window window = this.getWindow();
        window.setStatusBarColor(statusBar);
        binding.editCollectionOrPackNameLayout.setBoxStrokeColor(main);
        binding.editCollectionOrPackNameLayout.setHintTextColor(ColorStateList.valueOf(main));
        binding.editCollectionOrPackDescLayout.setBoxStrokeColor(main);
        binding.editCollectionOrPackDescLayout.setHintTextColor(ColorStateList.valueOf(main));
        binding.editCollectionOrPackEmojiLayout.setBoxStrokeColor(main);
        binding.editCollectionOrPackEmojiLayout.setHintTextColor(ColorStateList.valueOf(main));
        binding.getRoot().setBackgroundColor(background);
    }

    @Override
    public void onBackPressed() {
        String name = binding.editCollectionOrPackName.getText().toString();
        String desc = binding.editCollectionOrPackDesc.getText().toString();
        if (pack == null || (pack.name.equals(name) && pack.desc.equals(desc))) {
            super.onBackPressed();
        } else {
            Dialog confirmCancelDialog = new Dialog(this, R.style.dia_view);
            DiaConfirmBinding bindingConfirmCancelDialog = DiaConfirmBinding.inflate(getLayoutInflater());
            confirmCancelDialog.setContentView(bindingConfirmCancelDialog.getRoot());
            confirmCancelDialog.setTitle(getResources().getString(R.string.discard_changes));
            confirmCancelDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            bindingConfirmCancelDialog.diaConfirmYes.setOnClickListener(v -> super.onBackPressed());
            bindingConfirmCancelDialog.diaConfirmNo.setOnClickListener(v -> confirmCancelDialog.dismiss());
            confirmCancelDialog.show();
        }
    }
}
