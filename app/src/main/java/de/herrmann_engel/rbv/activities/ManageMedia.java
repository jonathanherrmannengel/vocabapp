package de.herrmann_engel.rbv.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import de.herrmann_engel.rbv.adapters.AdapterMediaManage;
import de.herrmann_engel.rbv.databinding.ActivityManageMediaBinding;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class ManageMedia extends FileTools {

    private ActivityManageMediaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageMediaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(Color.argb(75, 200, 200, 250));
        gradientDrawable.setStroke(2, Color.rgb(170, 170, 220));
        gradientDrawable.setCornerRadius(8);
        binding.manageFilesButton.setBackground(gradientDrawable);
        binding.manageFilesButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageFiles.class);
            startActivity(intent);
        });
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
        AdapterMediaManage adapter = new AdapterMediaManage(mediaList);
        binding.recMediaManage.setAdapter(adapter);
        binding.recMediaManage.setLayoutManager(new LinearLayoutManager(this));
    }
}
