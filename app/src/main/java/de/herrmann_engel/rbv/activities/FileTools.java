package de.herrmann_engel.rbv.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCardAll;
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCardImages;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public abstract class FileTools extends AppCompatActivity {


    final ActivityResultLauncher<Intent> selectMediaFolder = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        Intent data = result.getData();
                        Uri destination = data.getData();
                        setCardMediaFolder(destination.toString());
                        int takeFlags = data.getFlags();
                        takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(destination, takeFlags);
                        notifyFolderSet();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            String folder = getCardMediaFolder();
            DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
            if (folder == null || folder.isEmpty()) {
                if (!dbHelperGet.getAllMedia().isEmpty()) {
                    showSelectDialog(getResources().getString(R.string.select_folder_help_db_entries));
                }
            } else {
                DocumentFile outputDirectory = DocumentFile.fromTreeUri(this, Uri.parse(getCardMediaFolder()));
                if (outputDirectory == null || !outputDirectory.isDirectory()) {
                    showSelectDialog(getResources().getString(R.string.select_folder_help_reselect));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startSelectMediaFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        selectMediaFolder.launch(intent);
    }

    protected void showSelectDialog(String text) {
        Dialog selectFolderInfo = new Dialog(this, R.style.dia_view);
        selectFolderInfo.setContentView(R.layout.dia_confirm);
        selectFolderInfo.setTitle(getResources().getString(R.string.select_folder_title));
        selectFolderInfo.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        Button yesButton = selectFolderInfo.findViewById(R.id.dia_confirm_yes);
        Button noButton = selectFolderInfo.findViewById(R.id.dia_confirm_no);
        TextView confirmDeleteDesc = selectFolderInfo.findViewById(R.id.dia_confirm_desc);
        confirmDeleteDesc.setText(text);
        confirmDeleteDesc.setVisibility(View.VISIBLE);
        yesButton.setOnClickListener(v -> {
            startSelectMediaFolder();
            selectFolderInfo.dismiss();
        });
        noButton.setOnClickListener(v -> selectFolderInfo.dismiss());
        selectFolderInfo.show();
    }

    public void showMissingDialog(int card) {
        Dialog selectFolderInfo = new Dialog(this, R.style.dia_view);
        selectFolderInfo.setContentView(R.layout.dia_confirm);
        selectFolderInfo.setTitle(getResources().getString(R.string.media_missing_dialog));
        selectFolderInfo.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        Button yesButton = selectFolderInfo.findViewById(R.id.dia_confirm_yes);
        Button noButton = selectFolderInfo.findViewById(R.id.dia_confirm_no);
        TextView confirmDeleteDesc = selectFolderInfo.findViewById(R.id.dia_confirm_desc);
        confirmDeleteDesc.setText(getResources().getString(R.string.media_missing_dialog_text));
        confirmDeleteDesc.setVisibility(View.VISIBLE);
        yesButton.setOnClickListener(v -> {
            selectFolderInfo.dismiss();
            notifyMissingAction(card);
        });
        noButton.setOnClickListener(v -> selectFolderInfo.dismiss());
        selectFolderInfo.show();
    }

    public void showImageDialog(int id) {
        Dialog info = new Dialog(this, R.style.dia_view);
        info.setContentView(R.layout.dia_image);
        info.setTitle(getResources().getString(R.string.image_media));
        info.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        ImageView imageView = info.findViewById(R.id.dia_image_view);
        Picasso.get().load(getImageUri(id)).fit().centerInside().into(imageView);
        info.show();
    }

    public void showImageListDialog(ArrayList<DB_Media_Link_Card> imageList) {
        if (imageList.size() == 1) {
            showImageDialog(imageList.get(0).file);
        } else if (imageList.size() > 9) {
            showMediaListDialog(imageList, true, getResources().getString(R.string.query_media_image_title));
        } else {
            Dialog info = new Dialog(this, R.style.dia_view);
            info.setContentView(R.layout.dia_rec);
            info.setTitle(getResources().getString(R.string.query_media_image_title));
            info.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            info.show();
            AdapterMediaLinkCardImages adapter = new AdapterMediaLinkCardImages(imageList, this);
            RecyclerView recyclerView = info.findViewById(R.id.dia_rec);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }
    }

    public void showMediaListDialog(ArrayList<DB_Media_Link_Card> mediaList) {
        showMediaListDialog(mediaList, false, getResources().getString(R.string.query_media_all_title));
    }

    private void showMediaListDialog(ArrayList<DB_Media_Link_Card> mediaList, boolean onlyImages, String title) {
        Dialog info = new Dialog(this, R.style.dia_view);
        info.setContentView(R.layout.dia_rec);
        info.setTitle(title);
        info.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        info.show();
        AdapterMediaLinkCardAll adapter = new AdapterMediaLinkCardAll(mediaList, onlyImages, this);
        RecyclerView recyclerView = info.findViewById(R.id.dia_rec);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void showDeleteDialog(String fileName) {
        Dialog deleteFileDialog = new Dialog(this, R.style.dia_view);
        deleteFileDialog.setContentView(R.layout.dia_confirm);
        deleteFileDialog.setTitle(getResources().getString(R.string.delete_file_title));
        deleteFileDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        Button yesButton = deleteFileDialog.findViewById(R.id.dia_confirm_yes);
        Button noButton = deleteFileDialog.findViewById(R.id.dia_confirm_no);
        TextView confirmDeleteDesc = deleteFileDialog.findViewById(R.id.dia_confirm_desc);
        confirmDeleteDesc.setText(R.string.delete_file_info);
        confirmDeleteDesc.setVisibility(View.VISIBLE);
        yesButton.setOnClickListener(v -> {
            deleteMediaFile(fileName);
            deleteFileDialog.dismiss();
        });
        noButton.setOnClickListener(v -> deleteFileDialog.dismiss());
        deleteFileDialog.show();
    }

    public Uri getImageUri(int id) {
        DocumentFile folderFile = DocumentFile.fromTreeUri(this, Uri.parse(getCardMediaFolder()));
        if (folderFile == null) {
            return null;
        }
        DocumentFile file = getFile(id);
        if (file == null) {
            return null;
        }
        return file.getUri();
    }

    private DocumentFile getFile(String name) {
        DocumentFile folderFile = DocumentFile.fromTreeUri(this, Uri.parse(getCardMediaFolder()));
        if (folderFile == null) {
            return null;
        }
        return folderFile.findFile(name);
    }

    private DocumentFile getFile(int id) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        DB_Media currentMedia = dbHelperGet.getSingleMedia(id);
        String fileName = currentMedia.file;
        return getFile(fileName);
    }

    public void openFile(int id) {
        try {
            DocumentFile file = getFile(id);
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(file.getUri(), file.getType());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public void shareFile(int id) {
        try {
            DocumentFile file = getFile(id);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType(file.getType());
            intent.putExtra(Intent.EXTRA_STREAM, file.getUri());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.startActivity(Intent.createChooser(intent, file.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMediaFile(String name) {
        try {
            DocumentFile file = getFile(name);
            DocumentsContract.deleteDocument(
                    this.getApplicationContext().getContentResolver(),
                    file.getUri()
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public String getCardMediaFolder() {
        SharedPreferences config = this.getSharedPreferences(Globals.CONFIG_NAME, Context.MODE_PRIVATE);
        return config.getString("card_media_folder", null);
    }

    private void setCardMediaFolder(String uri) {
        SharedPreferences config = this.getSharedPreferences(Globals.CONFIG_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = config.edit();
        editor.putString("card_media_folder", uri);
        editor.apply();
    }

    public boolean existsMediaFile(String name) {
        try {
            DocumentFile folderFile = DocumentFile.fromTreeUri(this, Uri.parse(getCardMediaFolder()));
            return folderFile.findFile(name).exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsMediaFile(int input) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        String fileName = dbHelperGet.getSingleMedia(input).file;
        return existsMediaFile(fileName);
    }

    protected abstract void notifyFolderSet();

    protected abstract void notifyMissingAction(int id);
}
