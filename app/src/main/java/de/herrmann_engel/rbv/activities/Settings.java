package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;

public class Settings extends FileTools {

    SharedPreferences settings;
    CheckBox formatCardsButton;
    CheckBox formatCardNotesButton;
    CheckBox uiBgImagesButton;
    CheckBox uiFontSizeButton;
    CheckBox listNoUpdateButton;
    CheckBox mediaInGalleryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        int sort = settings.getInt("default_sort", Globals.SORT_DEFAULT);
        if (sort == Globals.SORT_ALPHABETICAL) {
            RadioButton selectedSort = findViewById(R.id.settings_sort_alphabetical);
            selectedSort.setChecked(true);
        } else if (sort == Globals.SORT_RANDOM) {
            RadioButton selectedSort = findViewById(R.id.settings_sort_random);
            selectedSort.setChecked(true);
        } else {
            RadioButton selectedSort = findViewById(R.id.settings_sort_normal);
            selectedSort.setChecked(true);
        }
        boolean formatCards = settings.getBoolean("format_cards", false);
        boolean formatCardNotes = settings.getBoolean("format_card_notes", false);
        formatCardsButton = findViewById(R.id.settings_format_cards);
        formatCardsButton.setChecked(formatCards);
        formatCardNotesButton = findViewById(R.id.settings_format_card_notes);
        formatCardNotesButton.setChecked(formatCardNotes);
        boolean uiBgImages = settings.getBoolean("ui_bg_images", true);
        boolean uiFontSize = settings.getBoolean("ui_font_size", false);
        uiBgImagesButton = findViewById(R.id.settings_ui_background_images);
        uiFontSizeButton = findViewById(R.id.settings_ui_increase_font_size);
        uiBgImagesButton.setChecked(uiBgImages);
        uiFontSizeButton.setChecked(uiFontSize);
        boolean listNoUpdate = settings.getBoolean("list_no_update", true);
        listNoUpdateButton = findViewById(R.id.settings_list_no_update);
        listNoUpdateButton.setChecked(listNoUpdate);
        boolean mediaInGallery = settings.getBoolean("media_in_gallery", true);
        mediaInGalleryButton = findViewById(R.id.settings_media_in_gallery);
        mediaInGalleryButton.setChecked(mediaInGallery);
    }

    @Override
    protected void notifyFolderSet() {

    }

    @Override
    protected void notifyMissingAction(int id) {

    }

    private void setSort(int sort) {
        SharedPreferences.Editor settingsEdit = settings.edit();
        settingsEdit.putInt("default_sort", sort);
        settingsEdit.apply();
    }

    public void setSortNormal(View view) {
        setSort(Globals.SORT_DEFAULT);
    }

    public void setSortRandom(View view) {
        setSort(Globals.SORT_RANDOM);
    }

    public void setSortAlphabetical(View view) {
        setSort(Globals.SORT_ALPHABETICAL);
    }

    public void setFormatCards(View view) {
        SharedPreferences.Editor settingsEdit = settings.edit();
        settingsEdit.putBoolean("format_cards", formatCardsButton.isChecked());
        settingsEdit.apply();
    }

    public void setFormatCardNotes(View view) {
        SharedPreferences.Editor settingsEdit = settings.edit();
        settingsEdit.putBoolean("format_card_notes", formatCardNotesButton.isChecked());
        settingsEdit.apply();
    }

    public void infoFormatCards(View view) {
        Dialog info = new Dialog(this, R.style.dia_view);
        info.setContentView(R.layout.dia_info);
        info.setTitle(getResources().getString(R.string.info));
        info.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        TextView infoText = info.findViewById(R.id.dia_info_text);
        infoText.setText(R.string.settings_format_cards_info);
        info.show();
    }

    public void setUIBackground(View view) {
        SharedPreferences.Editor settingsEdit = settings.edit();
        settingsEdit.putBoolean("ui_bg_images", uiBgImagesButton.isChecked());
        settingsEdit.apply();
    }

    public void setUIFontSize(View view) {
        SharedPreferences.Editor settingsEdit = settings.edit();
        settingsEdit.putBoolean("ui_font_size", uiFontSizeButton.isChecked());
        settingsEdit.apply();
    }

    public void setListNoUpdate(View view) {
        SharedPreferences.Editor settingsEdit = settings.edit();
        settingsEdit.putBoolean("list_no_update", listNoUpdateButton.isChecked());
        settingsEdit.apply();
    }

    public void setMediaInGallery(View view) {
        SharedPreferences.Editor settingsEdit = settings.edit();
        settingsEdit.putBoolean("media_in_gallery", mediaInGalleryButton.isChecked());
        settingsEdit.apply();
        handleNoMediaFile();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}
