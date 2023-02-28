package de.herrmann_engel.rbv.activities;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatDelegate;

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

        binding.settingsSort.clearCheck();
        int sort = settings.getInt("default_sort", Globals.SORT_DEFAULT);
        if (sort == Globals.SORT_ALPHABETICAL) {
            binding.settingsSortAlphabetical.setChecked(true);
        } else if (sort == Globals.SORT_RANDOM) {
            binding.settingsSortRandom.setChecked(true);
        } else {
            binding.settingsSortNormal.setChecked(true);
        }
        binding.settingsSort.setOnCheckedChangeListener((group, id) -> {
            int sortNew = Globals.SORT_DEFAULT;
            if (id == R.id.settings_sort_alphabetical) {
                sortNew = Globals.SORT_ALPHABETICAL;
            } else if (id == R.id.settings_sort_random) {
                sortNew = Globals.SORT_RANDOM;
            }
            settingsEdit.putInt("default_sort", sortNew);
            settingsEdit.apply();
        });

        boolean formatCards = settings.getBoolean("format_cards", false);
        binding.settingsFormatCards.setChecked(formatCards);
        binding.settingsFormatCards.setOnCheckedChangeListener((v, c) -> {
            settingsEdit.putBoolean("format_cards", c);
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
        binding.settingsFormatCardNotes.setOnCheckedChangeListener((v, c) -> {
            settingsEdit.putBoolean("format_card_notes", c);
            settingsEdit.apply();
        });

        boolean uiBgImages = settings.getBoolean("ui_bg_images", true);
        binding.settingsUiBackgroundImages.setChecked(uiBgImages);
        binding.settingsUiBackgroundImages.setOnCheckedChangeListener((v, c) -> {
            settingsEdit.putBoolean("ui_bg_images", c);
            settingsEdit.apply();
        });

        boolean uiFontSize = settings.getBoolean("ui_font_size", false);
        binding.settingsUiIncreaseFontSize.setChecked(uiFontSize);
        binding.settingsUiIncreaseFontSize.setOnCheckedChangeListener((v, c) -> {
            settingsEdit.putBoolean("ui_font_size", c);
            settingsEdit.apply();
        });

        int uiMode = settings.getInt("ui_mode", Globals.UI_MODE_DAY);
        binding.settingsUiMode.clearCheck();
        if (uiMode == Globals.UI_MODE_NIGHT) {
            binding.settingsUiModeNight.setChecked(true);
        } else if (uiMode == Globals.UI_MODE_DAY) {
            binding.settingsUiModeDay.setChecked(true);
        } else {
            binding.settingsUiModeAuto.setChecked(true);
        }
        binding.settingsUiMode.setOnCheckedChangeListener((group, id) -> {
            int uiModeNew = Globals.UI_MODE_AUTO;
            if (id == R.id.settings_ui_mode_night) {
                uiModeNew = Globals.UI_MODE_NIGHT;
            } else if (id == R.id.settings_ui_mode_day) {
                uiModeNew = Globals.UI_MODE_DAY;
            }
            settingsEdit.putInt("ui_mode", uiModeNew);
            settingsEdit.apply();
            if (uiModeNew == Globals.UI_MODE_NIGHT) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
            } else if (uiModeNew == Globals.UI_MODE_DAY) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
            }
        });

        boolean mediaInGallery = settings.getBoolean("media_in_gallery", true);
        binding.settingsMediaInGallery.setChecked(mediaInGallery);
        binding.settingsMediaInGallery.setOnCheckedChangeListener((v, c) -> {
            settingsEdit.putBoolean("media_in_gallery", c);
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

}
