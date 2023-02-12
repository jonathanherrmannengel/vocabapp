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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.File;
import java.util.List;
import java.util.Objects;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterPacks;
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding;
import de.herrmann_engel.rbv.databinding.DiaExportBinding;
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.export_import.AsyncExport;
import de.herrmann_engel.rbv.export_import.AsyncExportFinish;
import de.herrmann_engel.rbv.export_import.AsyncExportProgress;

public class ListPacks extends AppCompatActivity implements AsyncExportFinish, AsyncExportProgress {

    private ActivityDefaultRecBinding binding;
    private DB_Helper_Get dbHelperGet;
    private AdapterPacks adapter;
    private int collectionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDefaultRecBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        collectionNo = getIntent().getExtras().getInt("collection");
        dbHelperGet = new DB_Helper_Get(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_packs, menu);
        if (collectionNo == -1) {
            MenuItem startNewPack = menu.findItem(R.id.start_new_pack);
            startNewPack.setVisible(false);
            MenuItem collectionDetails = menu.findItem(R.id.collection_details);
            collectionDetails.setVisible(false);
            MenuItem export = menu.findItem(R.id.export_single);
            export.setVisible(false);
        }
        try {
            if (collectionNo > -1) {
                setTitle(dbHelperGet.getSingleCollection(collectionNo).name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        if (settings.getBoolean("ui_bg_images", true)) {
            binding.backgroundImage.setVisibility(View.VISIBLE);
            binding.backgroundImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.bg_packs));
        } else {
            binding.backgroundImage.setVisibility(View.GONE);
        }
        boolean uiFontSizeBig = settings.getBoolean("ui_font_size", false);
        if (adapter == null) {
            adapter = new AdapterPacks(loadContent(), uiFontSizeBig, collectionNo);
            binding.recDefault.setAdapter(adapter);
            binding.recDefault.setLayoutManager(new LinearLayoutManager(this));
        } else {
            adapter.updateContent(loadContent());
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
                    binding.getRoot().setBackgroundColor(colorBackground);
                }
                colors.recycle();
                colorsBackground.recycle();
            } catch (Exception e) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<DB_Pack_With_Meta> loadContent() {
        List<DB_Pack_With_Meta> currentList;
        if (collectionNo == -1) {
            currentList = dbHelperGet.getAllPacksWithMeta();
        } else {
            currentList = dbHelperGet.getAllPacksWithMetaByCollection(collectionNo);
        }
        DB_Pack_With_Meta fixedFirstItemPlaceholder = new DB_Pack_With_Meta();
        if (collectionNo == -1) {
            fixedFirstItemPlaceholder.counter = dbHelperGet.countCards();
        } else {
            fixedFirstItemPlaceholder.counter = dbHelperGet.countCardsInCollection(collectionNo);
        }
        currentList.add(0, fixedFirstItemPlaceholder);
        return currentList;
    }

    public void collectionDetails(MenuItem item) {
        Intent intent = new Intent(this, ViewCollection.class);
        intent.putExtra("collection", collectionNo);
        this.startActivity(intent);
    }

    public void startNewPack(MenuItem menuItem) {
        Intent intent = new Intent(this, NewPack.class);
        intent.putExtra("collection", collectionNo);
        this.startActivity(intent);
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
        DiaExportBinding bindingStartExportDialog = DiaExportBinding.inflate(getLayoutInflater());
        startExportDialog.setContentView(bindingStartExportDialog.getRoot());
        startExportDialog.setTitle(getResources().getString(R.string.options));
        startExportDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        bindingStartExportDialog.diaExportIncludeSettings.setChecked(false);
        bindingStartExportDialog.diaExportIncludeSettings.setVisibility(View.GONE);
        bindingStartExportDialog.diaExportIncludeMedia.setChecked(false);
        bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.setVisibility(bindingStartExportDialog.diaExportIncludeMedia.isChecked() ? View.VISIBLE : View.GONE);
        bindingStartExportDialog.diaExportIncludeMediaWarnAllMedia.setVisibility(bindingStartExportDialog.diaExportIncludeMedia.isChecked() ? View.VISIBLE : View.GONE);
        bindingStartExportDialog.diaExportIncludeMedia.setOnCheckedChangeListener((v, c) -> {
            if (c) {
                bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.setVisibility(View.VISIBLE);
                bindingStartExportDialog.diaExportIncludeMediaWarnAllMedia.setVisibility(View.VISIBLE);
            } else {
                bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.setVisibility(View.GONE);
                bindingStartExportDialog.diaExportIncludeMediaWarnAllMedia.setVisibility(View.GONE);
            }
        });
        bindingStartExportDialog.diaExportStart.setOnClickListener(v -> {
            new AsyncExport(this, this, this, collectionNo, bindingStartExportDialog.diaExportIncludeMedia.isChecked()).execute();
            startExportDialog.dismiss();
            Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show();
        });
        startExportDialog.show();
    }
}
