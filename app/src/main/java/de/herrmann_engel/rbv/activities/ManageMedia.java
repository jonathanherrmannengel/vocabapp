package de.herrmann_engel.rbv.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterMediaManage;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class ManageMedia extends FileTools {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_media);
        setRecView();
    }

    @Override
    protected void notifyFolderSet() {
        setRecView();
    }

    @Override
    protected void notifyMissingAction(int id) {

    }

    private void setRecView() {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        ArrayList<DB_Media> mediaList = (ArrayList<DB_Media>) dbHelperGet.getAllMedia();
        AdapterMediaManage adapter = new AdapterMediaManage(mediaList, this);
        RecyclerView recyclerView = findViewById(R.id.rec_card_media_manage);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}
