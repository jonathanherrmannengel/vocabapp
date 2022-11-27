package de.herrmann_engel.rbv.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
        Button manageFilesButton = findViewById(R.id.manage_files_button);
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(Color.argb(75, 200, 200, 250));
        gradientDrawable.setStroke(2, Color.rgb(170, 170, 220));
        gradientDrawable.setCornerRadius(8);
        manageFilesButton.setBackground(gradientDrawable);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        RecyclerView recyclerView = findViewById(R.id.rec_media_manage);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void manageFiles(View view) {
        Intent intent = new Intent(getApplicationContext(), ManageFiles.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}
