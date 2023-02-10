package de.herrmann_engel.rbv.activities;

import android.os.Bundle;

import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Objects;

import de.herrmann_engel.rbv.adapters.AdapterFilesManage;
import de.herrmann_engel.rbv.databinding.ActivityManageFilesBinding;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class ManageFiles extends FileTools {

    private ActivityManageFilesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageFilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
            if (!dbHelperGet.existsMedia(file.getName()) && file.isFile() && !Objects.equals(file.getName(), ".nomedia")) {
                filesWithoutMedia.add(file);
            }
        }
        AdapterFilesManage adapter = new AdapterFilesManage(filesWithoutMedia);
        binding.recFilesManage.setAdapter(adapter);
        binding.recFilesManage.setLayoutManager(new LinearLayoutManager(this));
    }
}
