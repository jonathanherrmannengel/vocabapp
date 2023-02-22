package de.herrmann_engel.rbv.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.File;
import java.util.List;
import java.util.Objects;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterCollections;
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding;
import de.herrmann_engel.rbv.databinding.DiaExportBinding;
import de.herrmann_engel.rbv.databinding.DiaImportBinding;
import de.herrmann_engel.rbv.db.DB_Collection_With_Meta;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.export_import.AsyncExport;
import de.herrmann_engel.rbv.export_import.AsyncExportFinish;
import de.herrmann_engel.rbv.export_import.AsyncExportProgress;
import de.herrmann_engel.rbv.export_import.AsyncImport;
import de.herrmann_engel.rbv.export_import.AsyncImportFinish;
import de.herrmann_engel.rbv.export_import.AsyncImportProgress;

public class ListCollections extends FileTools implements AsyncImportFinish, AsyncImportProgress, AsyncExportFinish, AsyncExportProgress {
    private ActivityDefaultRecBinding binding;
    private DB_Helper_Get dbHelperGet;
    private AdapterCollections adapter;
    private int importMode;
    private boolean importIncludeSettings;
    private boolean importIncludeMedia;
    private final ActivityResultLauncher<Intent> launcherImportFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    new AsyncImport(this, this, this, Objects.requireNonNull(result.getData()).getData(), importMode, importIncludeSettings, importIncludeMedia)
                            .execute();
                    Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                }
            });
    private boolean exportIncludeSettings;
    private boolean exportIncludeMedia;
    private final ActivityResultLauncher<Intent> launcherExportFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = Objects.requireNonNull(result.getData()).getData();
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    new AsyncExport(this, this, this, exportIncludeSettings, exportIncludeMedia, uri).execute();
                    Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDefaultRecBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.app_name);
        dbHelperGet = new DB_Helper_Get(this);
        handleNoMediaFile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_collections, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem exportAllMenuItem = menu.findItem(R.id.export_all);
        MenuItem startAdvancedSearchMenuItem = menu.findItem(R.id.start_advanced_search);
        MenuItem startManageMediaMenuItem = menu.findItem(R.id.start_manage_media);
        exportAllMenuItem.setVisible(dbHelperGet.hasCollections());
        startAdvancedSearchMenuItem.setVisible(dbHelperGet.hasCards());
        startManageMediaMenuItem.setVisible(dbHelperGet.hasMedia());
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        if (settings.getBoolean("ui_bg_images", true)) {
            binding.backgroundImage.setVisibility(View.VISIBLE);
            binding.backgroundImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.bg_collections));
        } else {
            binding.backgroundImage.setVisibility(View.GONE);
        }
        updateSettingsAndContent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            File cacheDir = getCacheDir();
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && System.currentTimeMillis() - file.lastModified() > 86400000) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<DB_Collection_With_Meta> loadContent() {
        List<DB_Collection_With_Meta> currentList = dbHelperGet.getAllCollectionsWithMeta();
        DB_Collection_With_Meta fixedFirstItemPlaceholder = new DB_Collection_With_Meta();
        fixedFirstItemPlaceholder.counter = dbHelperGet.countPacks();
        currentList.add(0, fixedFirstItemPlaceholder);
        return currentList;
    }

    private void updateSettingsAndContent() {
        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        boolean uiFontSizeBig = settings.getBoolean("ui_font_size", false);
        if (adapter == null) {
            adapter = new AdapterCollections(loadContent(), uiFontSizeBig);
            binding.recDefault.setAdapter(adapter);
            binding.recDefault.setLayoutManager(new LinearLayoutManager(this));
        } else {
            adapter.updateSettingsAndContent(loadContent(), uiFontSizeBig);
        }
    }

    public void startNewCollection(MenuItem menuItem) {
        this.startActivity(new Intent(this, NewCollection.class));
    }

    public void startAdvancedSearch(MenuItem menuItem) {
        this.startActivity(new Intent(this, AdvancedSearch.class));
    }

    public void startManageMedia(MenuItem menuItem) {
        this.startActivity(new Intent(this, ManageMedia.class));
    }

    public void startSettings(MenuItem menuItem) {
        this.startActivity(new Intent(this, Settings.class));
    }

    public void startAboutApp(MenuItem menuItem) {
        this.startActivity(new Intent(this, AppLicenses.class));
    }

    public void importCards(MenuItem menuItem) {
        Dialog startImportDialog = new Dialog(this, R.style.dia_view);
        DiaImportBinding bindingStartImportDialog = DiaImportBinding.inflate(getLayoutInflater());
        startImportDialog.setContentView(bindingStartImportDialog.getRoot());
        startImportDialog.setTitle(getResources().getString(R.string.options));
        startImportDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        if (dbHelperGet.hasCollections()) {
            bindingStartImportDialog.diaImportRadioIntegrate.setChecked(true);
            bindingStartImportDialog.diaImportRadioDuplicates.setChecked(false);
            bindingStartImportDialog.diaImportIncludeSettings.setChecked(false);
        } else {
            bindingStartImportDialog.diaImportRadioIntegrate.setChecked(false);
            bindingStartImportDialog.diaImportRadioDuplicates.setChecked(true);
            bindingStartImportDialog.diaImportIncludeSettings.setChecked(true);
        }
        bindingStartImportDialog.diaImportRadioSkip.setChecked(false);
        bindingStartImportDialog.diaImportIncludeMedia.setChecked(true);
        bindingStartImportDialog.diaImportIncludeMedia.setOnCheckedChangeListener((v, c) -> {
            if (c) {
                bindingStartImportDialog.diaImportIncludeMediaWarnNoFiles.setVisibility(View.VISIBLE);
            } else {
                bindingStartImportDialog.diaImportIncludeMediaWarnNoFiles.setVisibility(View.GONE);
            }
        });
        bindingStartImportDialog.diaImportStart.setOnClickListener(v -> {
            if (bindingStartImportDialog.diaImportRadio.getCheckedRadioButtonId() == R.id.dia_import_radio_integrate) {
                importMode = Globals.IMPORT_MODE_INTEGRATE;
            } else if (bindingStartImportDialog.diaImportRadio.getCheckedRadioButtonId() == R.id.dia_import_radio_duplicates) {
                importMode = Globals.IMPORT_MODE_DUPLICATES;
            } else {
                importMode = Globals.IMPORT_MODE_SKIP;
            }
            importIncludeSettings = bindingStartImportDialog.diaImportIncludeSettings.isChecked();
            importIncludeMedia = bindingStartImportDialog.diaImportIncludeMedia.isChecked();
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
                    Toast.makeText(this, R.string.import_okay, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.import_warn, Toast.LENGTH_LONG).show();
                }
                if (adapter != null) {
                    updateSettingsAndContent();
                }
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void importCardsProgress(final String progress) {
        runOnUiThread(() -> Toast.makeText(this, progress, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void exportCardsResult(final File file) {
        runOnUiThread(() -> {
            if (file == null) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            } else {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                share.setType("text/csv");
                share.putExtra(
                        Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                this,
                                getPackageName() + ".fileprovider", file
                        )
                );
                startActivity(
                        Intent.createChooser(
                                share,
                                getString(R.string.export_cards)
                        )
                );
            }
        });
    }

    @Override
    public void exportCardsProgress(final String progress) {
        runOnUiThread(() -> Toast.makeText(this, progress, Toast.LENGTH_SHORT).show());
    }

    public void exportAll(MenuItem item) {
        Dialog startExportDialog = new Dialog(this, R.style.dia_view);
        DiaExportBinding bindingStartExportDialog = DiaExportBinding.inflate(getLayoutInflater());
        startExportDialog.setContentView(bindingStartExportDialog.getRoot());
        startExportDialog.setTitle(getResources().getString(R.string.options));
        startExportDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        bindingStartExportDialog.diaExportIncludeSettings.setChecked(false);
        bindingStartExportDialog.diaExportIncludeMedia.setChecked(true);
        bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.setVisibility(bindingStartExportDialog.diaExportIncludeMedia.isChecked() ? View.VISIBLE : View.GONE);
        bindingStartExportDialog.diaExportIncludeMedia.setOnCheckedChangeListener((v, c) -> {
            if (c) {
                bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.setVisibility(View.VISIBLE);
            } else {
                bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.setVisibility(View.GONE);
            }
        });
        bindingStartExportDialog.diaExportStart.setOnClickListener(v -> {
            exportIncludeSettings = bindingStartExportDialog.diaExportIncludeSettings.isChecked();
            exportIncludeMedia = bindingStartExportDialog.diaExportIncludeMedia.isChecked();
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_TITLE, Globals.EXPORT_FILE_NAME + "." + Globals.EXPORT_FILE_EXTENSION);
            launcherExportFile.launch(intent);
            startExportDialog.dismiss();
        });
        startExportDialog.show();
    }

    @Override
    protected void notifyFolderSet() {

    }

    @Override
    protected void notifyMissingAction(int id) {

    }
}
