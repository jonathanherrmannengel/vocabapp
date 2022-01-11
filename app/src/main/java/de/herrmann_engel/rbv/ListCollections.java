package de.herrmann_engel.rbv;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

public class ListCollections extends AppCompatActivity implements AsyncImportFinish {

    private ActivityResultLauncher<Intent> launcherImportFile;
    private MenuItem exportAllMenuItem;
    private boolean ignoreDuplicates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_rec);

        setTitle(R.string.app_name);

        launcherImportFile = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        new AsyncImport(this, this, result.getData().getData(), ignoreDuplicates).execute();
                        Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_collections, menu);
        exportAllMenuItem = menu.findItem(R.id.exportAll);
        updateContent();
        return true;
    }
    private void updateContent(){
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        List<DB_Collection> collections = dbHelperGet.getAllCollections();
        exportAllMenuItem.setVisible(collections.size() > 0);

        RecyclerView recyclerView = this.findViewById(R.id.rec_default);
        AdapterCollections adapter = new AdapterCollections(collections,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    public void startNewCollection(MenuItem menuItem) {
        this.startActivity(new Intent(getApplicationContext(),NewCollection.class));
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
        startImportDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        Button startImportButton = startImportDialog.findViewById(R.id.dia_import_start);
        MaterialCheckBox startImportIgnoreDuplicatesCheckBox = startImportDialog.findViewById(R.id.dia_import_checkbox);
        startImportButton.setOnClickListener(v -> {
            ignoreDuplicates = startImportIgnoreDuplicatesCheckBox.isChecked();
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
    public void importCardsResult(final int result) {runOnUiThread(() -> {
        if(result < Globals.IMPORT_ERROR_LEVEL_ERROR) {
            if(result == Globals.IMPORT_ERROR_LEVEL_OKAY) {
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
        Export export = new Export(this);
        if(!export.exportFile()) {
            Toast.makeText(this,R.string.error, Toast.LENGTH_LONG).show();
        }
    }
}