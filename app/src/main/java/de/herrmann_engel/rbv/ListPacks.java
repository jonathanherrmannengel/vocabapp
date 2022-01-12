package de.herrmann_engel.rbv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

public class ListPacks extends AppCompatActivity {

    private DB_Helper_Get dbHelperGet;

    private int collectionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_rec);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_packs, menu);
        collectionNo = getIntent().getExtras().getInt("collection");
        dbHelperGet = new DB_Helper_Get(this);
        try {
            setTitle(dbHelperGet.getSingleCollection(collectionNo).name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateContent();
        return true;
    }
    private void updateContent(){
        List<DB_Pack> packs = dbHelperGet.getAllPacksByCollection(collectionNo);

        RecyclerView recyclerView = this.findViewById(R.id.rec_default);
        AdapterPacks adapter = new AdapterPacks(packs,this, collectionNo);
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
    public void export(MenuItem menuItem) {
        Export export = new Export(this,collectionNo);
        if(!export.exportFile()) {
            Toast.makeText(this,R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}