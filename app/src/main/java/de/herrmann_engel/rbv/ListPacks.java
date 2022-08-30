package de.herrmann_engel.rbv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class ListPacks extends AppCompatActivity {

    private DB_Helper_Get dbHelperGet;

    private int collectionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_rec);


        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        if(settings.getBoolean("ui_bg_images", true)) {
            ImageView backgroundImage = findViewById(R.id.background_image);
            backgroundImage.setVisibility(View.VISIBLE);
            backgroundImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.bg_packs));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_packs, menu);
        collectionNo = getIntent().getExtras().getInt("collection");
        if(collectionNo == -1) {
            MenuItem startNewPack = menu.findItem(R.id.start_new_pack);
            startNewPack.setVisible(false);
            MenuItem collectionDetails = menu.findItem(R.id.collection_details);
            collectionDetails.setVisible(false);
            MenuItem export = menu.findItem(R.id.export_single);
            export.setVisible(false);
        }
        dbHelperGet = new DB_Helper_Get(this);
        try {
            if(collectionNo > -1) {
                setTitle(dbHelperGet.getSingleCollection(collectionNo).name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateContent();
        return true;
    }
    private void updateContent(){
        List<DB_Pack> packs;
        if(collectionNo == -1){
            packs = dbHelperGet.getAllPacks();
        } else{
            packs = dbHelperGet.getAllPacksByCollection(collectionNo);
        }
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
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}