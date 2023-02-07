package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;
import java.util.Objects;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterPacks;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.export_import.AsyncExport;
import de.herrmann_engel.rbv.export_import.AsyncExportFinish;
import de.herrmann_engel.rbv.export_import.AsyncExportProgress;

public class ListPacks extends AppCompatActivity implements AsyncExportFinish, AsyncExportProgress {

    private DB_Helper_Get dbHelperGet;

    private int collectionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_rec);

        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        if (settings.getBoolean("ui_bg_images", true)) {
            ImageView backgroundImage = findViewById(R.id.background_image);
            backgroundImage.setVisibility(View.VISIBLE);
            backgroundImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.bg_packs));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_packs, menu);
        collectionNo = getIntent().getExtras().getInt("collection");
        if (collectionNo == -1) {
            MenuItem startNewPack = menu.findItem(R.id.start_new_pack);
            startNewPack.setVisible(false);
            MenuItem collectionDetails = menu.findItem(R.id.collection_details);
            collectionDetails.setVisible(false);
            MenuItem export = menu.findItem(R.id.export_single);
            export.setVisible(false);
        }
        dbHelperGet = new DB_Helper_Get(this);
        try {
            if (collectionNo > -1) {
                setTitle(dbHelperGet.getSingleCollection(collectionNo).name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateContent();
        return true;
    }

    private void updateContent() {
        List<DB_Pack> packs;
        if (collectionNo == -1) {
            packs = dbHelperGet.getAllPacks();
        } else {
            packs = dbHelperGet.getAllPacksByCollection(collectionNo);
        }
        if (collectionNo >= 0) {
            try {
                int packColors = dbHelperGet.getSingleCollection(collectionNo).colors;
                TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
                TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
                if (packColors < Math.min(colors.length(), colorsBackground.length()) && packColors >= 0) {
                    int color = colors.getColor(packColors, 0);
                    int colorBackground = colorsBackground.getColor(packColors, 0);
                    Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                    Window window = this.getWindow();
                    window.setStatusBarColor(color);
                    findViewById(R.id.rec_default_root).setBackgroundColor(colorBackground);
                }
                colors.recycle();
                colorsBackground.recycle();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
        RecyclerView recyclerView = this.findViewById(R.id.rec_default);
        AdapterPacks adapter = new AdapterPacks(packs, this, collectionNo);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void collectionDetails(MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), ViewCollection.class);
        intent.putExtra("collection", collectionNo);
        this.startActivity(intent);
        this.finish();
    }

    public void startNewPack(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), NewPack.class);
        intent.putExtra("collection", collectionNo);
        this.startActivity(intent);
        this.finish();
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
        runOnUiThread(() -> {
            Toast.makeText(this, progress, Toast.LENGTH_SHORT).show();
        });
    }

    public void export(MenuItem menuItem) {
        Dialog startExportDialog = new Dialog(this, R.style.dia_view);
        startExportDialog.setContentView(R.layout.dia_export);
        startExportDialog.setTitle(getResources().getString(R.string.options));
        startExportDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        Button startExportButton = startExportDialog.findViewById(R.id.dia_export_start);
        CheckBox includeSettingsCheckBox = startExportDialog.findViewById(R.id.dia_export_include_settings);
        CheckBox includeMediaCheckBox = startExportDialog.findViewById(R.id.dia_export_include_media);
        TextView includeMediaWarnNoFile = startExportDialog.findViewById(R.id.dia_export_include_media_warn_no_files);
        TextView includeMediaWarnAllMedia = startExportDialog.findViewById(R.id.dia_export_include_media_warn_all_media);
        includeSettingsCheckBox.setChecked(false);
        includeSettingsCheckBox.setVisibility(View.GONE);
        includeMediaCheckBox.setChecked(false);
        includeMediaWarnNoFile.setVisibility(includeMediaCheckBox.isChecked() ? View.VISIBLE : View.GONE);
        includeMediaWarnAllMedia.setVisibility(includeMediaCheckBox.isChecked() ? View.VISIBLE : View.GONE);
        includeMediaCheckBox.setOnCheckedChangeListener((v, c) -> {
            if (c) {
                includeMediaWarnNoFile.setVisibility(View.VISIBLE);
                includeMediaWarnAllMedia.setVisibility(View.VISIBLE);
            } else {
                includeMediaWarnNoFile.setVisibility(View.GONE);
                includeMediaWarnAllMedia.setVisibility(View.GONE);
            }
        });
        startExportButton.setOnClickListener(v -> {
            new AsyncExport(getApplicationContext(), this, this, collectionNo, includeMediaCheckBox.isChecked()).execute();
            startExportDialog.dismiss();
            Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show();
        });
        startExportDialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}
