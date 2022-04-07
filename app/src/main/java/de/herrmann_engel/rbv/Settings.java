package de.herrmann_engel.rbv;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    SharedPreferences settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        int sort = settings.getInt("default_sort", Globals.SORT_DEFAULT);
        if(sort == Globals.SORT_ALPHABETICAL) {
            RadioButton selectedSort = findViewById(R.id.settings_sort_alphabetical);
            selectedSort.setChecked(true);
        } else if(sort == Globals.SORT_RANDOM) {
            RadioButton selectedSort = findViewById(R.id.settings_sort_random);
            selectedSort.setChecked(true);
        } else {
            RadioButton selectedSort = findViewById(R.id.settings_sort_normal);
            selectedSort.setChecked(true);
        }
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}