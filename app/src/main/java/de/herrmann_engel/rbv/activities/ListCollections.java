package de.herrmann_engel.rbv.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterCollections;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.export_import.AsyncImport;
import de.herrmann_engel.rbv.export_import.AsyncImportFinish;
import de.herrmann_engel.rbv.export_import.Export;

public class ListCollections extends AppCompatActivity implements AsyncImportFinish {

    List<DB_Collection> collections;
    private ActivityResultLauncher<Intent> launcherImportFile;
    private MenuItem exportAllMenuItem;
    private int importMode;
    private boolean includeSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_rec);

        setTitle(R.string.app_name);

        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        if (settings.getBoolean("ui_bg_images", true)) {
            ImageView backgroundImage = findViewById(R.id.background_image);
            backgroundImage.setVisibility(View.VISIBLE);
            backgroundImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.bg_collections));
        }

        launcherImportFile = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        new AsyncImport(this, this, Objects.requireNonNull(result.getData()).getData(), importMode, includeSettings)
                                .execute();
                        Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_collections, menu);
        exportAllMenuItem = menu.findItem(R.id.export_all);
        updateContent();
        return true;
    }

    private void updateContent() {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        collections = dbHelperGet.getAllCollections();
        exportAllMenuItem.setVisible(collections.size() > 0);

        RecyclerView recyclerView = this.findViewById(R.id.rec_default);
        AdapterCollections adapter = new AdapterCollections(collections, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void startNewCollection(MenuItem menuItem) {
        this.startActivity(new Intent(getApplicationContext(), NewCollection.class));
        this.finish();
    }

    public void startAdvancedSearch(MenuItem menuItem) {
        this.startActivity(new Intent(getApplicationContext(), AdvancedSearch.class));
        this.finish();
    }

    public void startSettings(MenuItem menuItem) {
        this.startActivity(new Intent(getApplicationContext(), Settings.class));
        this.finish();
    }

    public void startAboutApp(MenuItem menuItem) {
        this.startActivity(new Intent(getApplicationContext(), AppLicenses.class));
        this.finish();
    }

    public void importCards(MenuItem menuItem) {
        Dialog startImportDialog = new Dialog(this, R.style.dia_view);
        startImportDialog.setContentView(R.layout.dia_import);
        startImportDialog.setTitle(getResources().getString(R.string.options));
        startImportDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        Button startImportButton = startImportDialog.findViewById(R.id.dia_import_start);
        RadioGroup startImportMode = startImportDialog.findViewById(R.id.dia_import_radio);
        RadioButton startImportModeIntegrate = startImportMode.findViewById(R.id.dia_import_radio_integrate);
        RadioButton startImportModeDuplicates = startImportMode.findViewById(R.id.dia_import_radio_duplicates);
        RadioButton startImportModeSkip = startImportMode.findViewById(R.id.dia_import_radio_skip);
        CheckBox includeSettingsCheckBox = startImportDialog.findViewById(R.id.dia_import_include_settings);
        if (collections.size() > 0) {
            startImportModeIntegrate.setChecked(true);
            startImportModeDuplicates.setChecked(false);
            includeSettingsCheckBox.setChecked(false);
        } else {
            startImportModeIntegrate.setChecked(false);
            startImportModeDuplicates.setChecked(true);
            includeSettingsCheckBox.setChecked(true);
        }
        startImportModeSkip.setChecked(false);
        startImportButton.setOnClickListener(v -> {
            if (startImportMode.getCheckedRadioButtonId() == R.id.dia_import_radio_integrate) {
                importMode = Globals.IMPORT_MODE_INTEGRATE;
            } else if (startImportMode.getCheckedRadioButtonId() == R.id.dia_import_radio_duplicates) {
                importMode = Globals.IMPORT_MODE_DUPLICATES;
            } else {
                importMode = Globals.IMPORT_MODE_SKIP;
            }
            includeSettings = includeSettingsCheckBox.isChecked();
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.setType("text/*");
            launcherImportFile.launch(intent);
            startImportDialog.dismiss();
        });
        startImportDialog.show();
    }

    @Override
    public void importCardsResult(final int result) {
        runOnUiThread(() -> {
            if (result < Globals.IMPORT_ERROR_LEVEL_ERROR) {
                if (result == Globals.IMPORT_ERROR_LEVEL_OKAY) {
                    Toast.makeText(getApplicationContext(), R.string.import_okay, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.import_warn, Toast.LENGTH_LONG).show();
                }
                updateContent();
            } else {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void exportAll(MenuItem item) {
        Dialog startExportDialog = new Dialog(this, R.style.dia_view);
        startExportDialog.setContentView(R.layout.dia_export);
        startExportDialog.setTitle(getResources().getString(R.string.options));
        startExportDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        Button startImportButton = startExportDialog.findViewById(R.id.dia_export_start);
        startImportButton.setOnClickListener(v -> {
            CheckBox includeSettingsCheckBox = startExportDialog.findViewById(R.id.dia_export_include_settings);
            Export export = new Export(this, includeSettingsCheckBox.isChecked());
            if (!export.exportFile()) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            }
            startExportDialog.dismiss();
        });
        startExportDialog.show();
    }
}
