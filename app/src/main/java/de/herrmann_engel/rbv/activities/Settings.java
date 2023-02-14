package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.CheckBox;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.databinding.ActivitySettingsBinding;
import de.herrmann_engel.rbv.databinding.DiaInfoBinding;

public class Settings extends FileTools {

    SharedPreferences.Editor settingsEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        settingsEdit = settings.edit();

        int sort = settings.getInt("default_sort", Globals.SORT_DEFAULT);
        if (sort == Globals.SORT_ALPHABETICAL) {
            binding.settingsSortAlphabetical.setChecked(true);
        } else if (sort == Globals.SORT_RANDOM) {
            binding.settingsSortRandom.setChecked(true);
        } else {
            binding.settingsSortNormal.setChecked(true);
        }
        binding.settingsSortAlphabetical.setOnClickListener(v -> setSort(Globals.SORT_ALPHABETICAL));
        binding.settingsSortRandom.setOnClickListener(v -> setSort(Globals.SORT_RANDOM));
        binding.settingsSortNormal.setOnClickListener(v -> setSort(Globals.SORT_DEFAULT));

        boolean formatCards = settings.getBoolean("format_cards", false);
        binding.settingsFormatCards.setChecked(formatCards);
        binding.settingsFormatCards.setOnClickListener(v -> {
            settingsEdit.putBoolean("format_cards", ((CheckBox) v).isChecked());
            settingsEdit.apply();
        });
        binding.settingsFormatCardsInfo.setOnClickListener(v -> {
            Dialog infoDialog = new Dialog(this, R.style.dia_view);
            DiaInfoBinding bindingInfoDialog = DiaInfoBinding.inflate(getLayoutInflater());
            infoDialog.setContentView(bindingInfoDialog.getRoot());
            infoDialog.setTitle(getResources().getString(R.string.info));
            infoDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            bindingInfoDialog.diaInfoText.setText(R.string.settings_format_cards_info);
            infoDialog.show();
        });

        boolean formatCardNotes = settings.getBoolean("format_card_notes", false);
        binding.settingsFormatCardNotes.setChecked(formatCardNotes);
        binding.settingsFormatCardNotes.setOnClickListener(v -> {
            settingsEdit.putBoolean("format_card_notes", ((CheckBox) v).isChecked());
            settingsEdit.apply();
        });

        boolean uiBgImages = settings.getBoolean("ui_bg_images", true);
        binding.settingsUiBackgroundImages.setChecked(uiBgImages);
        binding.settingsUiBackgroundImages.setOnClickListener(v -> {
            settingsEdit.putBoolean("ui_bg_images", ((CheckBox) v).isChecked());
            settingsEdit.apply();
        });

        boolean uiFontSize = settings.getBoolean("ui_font_size", false);
        binding.settingsUiIncreaseFontSize.setChecked(uiFontSize);
        binding.settingsUiIncreaseFontSize.setOnClickListener(v -> {
            settingsEdit.putBoolean("ui_font_size", ((CheckBox) v).isChecked());
            settingsEdit.apply();
        });

        boolean mediaInGallery = settings.getBoolean("media_in_gallery", true);
        binding.settingsMediaInGallery.setChecked(mediaInGallery);
        binding.settingsMediaInGallery.setOnClickListener(v -> {
            settingsEdit.putBoolean("media_in_gallery", ((CheckBox) v).isChecked());
            settingsEdit.apply();
            handleNoMediaFile();
        });
    }

    @Override
    protected void notifyFolderSet() {

    }

    @Override
    protected void notifyMissingAction(int id) {

    }

    private void setSort(int sort) {
        settingsEdit.putInt("default_sort", sort);
        settingsEdit.apply();
    }
}
