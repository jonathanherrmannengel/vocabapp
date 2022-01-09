package de.herrmann_engel.rbv;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.List;

public class ListCollections extends AppCompatActivity implements AsyncImportFinish {

    private ActivityResultLauncher<Intent> launcherImportFile;
    private MenuItem exportAllMenuItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_rec);

        setTitle(R.string.app_name);

        launcherImportFile = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        new AsyncImport(this, this, result.getData().getData()).execute();
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
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setType("text/*");
        launcherImportFile.launch(intent);
    }
    @Override
    public void importCardsResult(final int result) {runOnUiThread(() -> {
        if(result < Globals.ERROR_LEVEL_ERROR) {
            if(result == Globals.ERROR_LEVEL_WARN) {
                Toast.makeText(getApplicationContext(), R.string.import_warn, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.import_okay, Toast.LENGTH_SHORT).show();
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