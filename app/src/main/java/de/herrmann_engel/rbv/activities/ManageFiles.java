package de.herrmann_engel.rbv.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterFilesManage;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class ManageFiles extends FileTools {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_files);
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
        DocumentFile[] files = listFiles();
        ArrayList<DocumentFile> filesWithoutMedia = new ArrayList<>();
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        for (DocumentFile file : files) {
            if (!dbHelperGet.existsMedia(file.getName()) && file.isFile()) {
                filesWithoutMedia.add(file);
            }
        }
        AdapterFilesManage adapter = new AdapterFilesManage(filesWithoutMedia, this);
        RecyclerView recyclerView = findViewById(R.id.rec_files_manage);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ManageMedia.class);
        startActivity(intent);
        this.finish();
    }
}
