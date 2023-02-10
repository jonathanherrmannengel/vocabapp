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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCardAll;
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCardImages;
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding;
import de.herrmann_engel.rbv.databinding.DiaImageBinding;
import de.herrmann_engel.rbv.databinding.DiaRecBinding;
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
            //Check: No media folder set
            if (folder == null || folder.isEmpty()) {
                if (!dbHelperGet.getAllMedia().isEmpty()) {
                    showSelectDialog(getResources().getString(R.string.select_folder_help_db_entries));
                }
            } else {
                DocumentFile outputDirectory = DocumentFile.fromTreeUri(this, Uri.parse(folder));
                //Check: Media folder set but directory not accessible
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
        Dialog selectFolderInfoDialog = new Dialog(this, R.style.dia_view);
        DiaConfirmBinding bindingSelectFolderInfoDialog = DiaConfirmBinding.inflate(getLayoutInflater());
        selectFolderInfoDialog.setContentView(bindingSelectFolderInfoDialog.getRoot());
        selectFolderInfoDialog.setTitle(getResources().getString(R.string.select_folder_title));
        selectFolderInfoDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        bindingSelectFolderInfoDialog.diaConfirmDesc.setText(text);
        bindingSelectFolderInfoDialog.diaConfirmDesc.setVisibility(View.VISIBLE);
        bindingSelectFolderInfoDialog.diaConfirmYes.setOnClickListener(v -> {
            startSelectMediaFolder();
            selectFolderInfoDialog.dismiss();
        });
        bindingSelectFolderInfoDialog.diaConfirmNo.setOnClickListener(v -> selectFolderInfoDialog.dismiss());
        selectFolderInfoDialog.show();
    }

    public void showMissingDialog(int card) {
        Dialog selectFolderInfoDialog = new Dialog(this, R.style.dia_view);
        DiaConfirmBinding bindingSelectFolderInfoDialog = DiaConfirmBinding.inflate(getLayoutInflater());
        selectFolderInfoDialog.setContentView(bindingSelectFolderInfoDialog.getRoot());
        selectFolderInfoDialog.setTitle(getResources().getString(R.string.media_missing_dialog));
        selectFolderInfoDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        bindingSelectFolderInfoDialog.diaConfirmDesc.setText(getResources().getString(R.string.media_missing_dialog_text));
        bindingSelectFolderInfoDialog.diaConfirmDesc.setVisibility(View.VISIBLE);
        bindingSelectFolderInfoDialog.diaConfirmYes.setOnClickListener(v -> {
            selectFolderInfoDialog.dismiss();
            notifyMissingAction(card);
        });
        bindingSelectFolderInfoDialog.diaConfirmNo.setOnClickListener(v -> selectFolderInfoDialog.dismiss());
        selectFolderInfoDialog.show();
    }

    public void showImageDialog(int id) {
        Dialog imageDialog = new Dialog(this, R.style.dia_view);
        DiaImageBinding bindingImageDialog = DiaImageBinding.inflate(getLayoutInflater());
        imageDialog.setContentView(bindingImageDialog.getRoot());
        imageDialog.setTitle(getResources().getString(R.string.image_media));
        imageDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        Picasso.get().load(getImageUri(id)).fit().centerInside().into(bindingImageDialog.diaImageView);
        imageDialog.show();
    }

    public void showImageListDialog(ArrayList<DB_Media_Link_Card> imageList) {
        if (imageList.size() == 1) {
            showImageDialog(imageList.get(0).file);
        } else if (imageList.size() > Globals.IMAGE_PREVIEW_MAX) {
            showMediaListDialog(imageList, true, getResources().getString(R.string.query_media_image_title));
        } else {
            Dialog imageListDialog = new Dialog(this, R.style.dia_view);
            DiaRecBinding bindingImageListDialog = DiaRecBinding.inflate(getLayoutInflater());
            imageListDialog.setContentView(bindingImageListDialog.getRoot());
            imageListDialog.setTitle(getResources().getString(R.string.query_media_image_title));
            imageListDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            imageListDialog.show();
            AdapterMediaLinkCardImages adapter = new AdapterMediaLinkCardImages(imageList);
            bindingImageListDialog.diaRec.setAdapter(adapter);
            bindingImageListDialog.diaRec.setLayoutManager(new GridLayoutManager(this, 3));
        }
    }

    public void showMediaListDialog(ArrayList<DB_Media_Link_Card> mediaList) {
        showMediaListDialog(mediaList, false, getResources().getString(R.string.query_media_all_title));
    }

    private void showMediaListDialog(ArrayList<DB_Media_Link_Card> mediaList, boolean onlyImages, String title) {
        Dialog mediaListDialog = new Dialog(this, R.style.dia_view);
        DiaRecBinding bindingMediaListDialog = DiaRecBinding.inflate(getLayoutInflater());
        mediaListDialog.setContentView(bindingMediaListDialog.getRoot());
        mediaListDialog.setTitle(title);
        mediaListDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        mediaListDialog.show();
        AdapterMediaLinkCardAll adapter = new AdapterMediaLinkCardAll(mediaList, onlyImages);
        bindingMediaListDialog.diaRec.setAdapter(adapter);
        bindingMediaListDialog.diaRec.setLayoutManager(new LinearLayoutManager(this));
    }

    public Dialog showDeleteDialog(String fileName, String text) {
        Dialog deleteFileDialog = new Dialog(this, R.style.dia_view);
        DiaConfirmBinding bindingDeleteFileDialog = DiaConfirmBinding.inflate(getLayoutInflater());
        deleteFileDialog.setContentView(bindingDeleteFileDialog.getRoot());
        deleteFileDialog.setTitle(getResources().getString(R.string.delete_file_title));
        deleteFileDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        bindingDeleteFileDialog.diaConfirmDesc.setText(text);
        bindingDeleteFileDialog.diaConfirmDesc.setVisibility(View.VISIBLE);
        bindingDeleteFileDialog.diaConfirmYes.setOnClickListener(v -> {
            deleteMediaFile(fileName);
            deleteFileDialog.dismiss();
        });
        bindingDeleteFileDialog.diaConfirmNo.setOnClickListener(v -> deleteFileDialog.dismiss());
        deleteFileDialog.show();
        return deleteFileDialog;
    }

    public Dialog showDeleteDialog(String fileName) {
        return showDeleteDialog(fileName, getResources().getString(R.string.delete_file_info));
    }

    public void handleNoMediaFile() {
        try {
            String folder = getCardMediaFolder();
            if (folder == null || folder.isEmpty()) {
                return;
            }
            DocumentFile outputDirectory = DocumentFile.fromTreeUri(this, Uri.parse(folder));
            if (outputDirectory == null || !outputDirectory.exists() || !outputDirectory.isDirectory()) {
                return;
            }
            SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
            boolean mediaInGallery = settings.getBoolean("media_in_gallery", true);
            DocumentFile noMediaFile = outputDirectory.findFile(".nomedia");
            if (mediaInGallery && noMediaFile != null && noMediaFile.exists() && noMediaFile.isFile()) {
                DocumentsContract.deleteDocument(
                        getContentResolver(),
                        noMediaFile.getUri()
                );
            } else if (!mediaInGallery && (noMediaFile == null || !noMediaFile.exists())) {
                outputDirectory.createFile("application/octet-stream", ".nomedia");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public DocumentFile[] listFiles() {
        DocumentFile folderFile = DocumentFile.fromTreeUri(this, Uri.parse(getCardMediaFolder()));
        if (folderFile == null) {
            return null;
        }
        return folderFile.listFiles();
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
        if (currentMedia == null) {
            return null;
        }
        String fileName = currentMedia.file;
        return getFile(fileName);
    }

    private void openFile(DocumentFile file) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(file.getUri(), file.getType());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivity(intent);
    }

    public void openFile(String name) {
        try {
            DocumentFile file = getFile(name);
            openFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public void openFile(int id) {
        try {
            DocumentFile file = getFile(id);
            openFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(DocumentFile file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType(file.getType());
        intent.putExtra(Intent.EXTRA_STREAM, file.getUri());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.startActivity(Intent.createChooser(intent, file.getName()));
    }

    public void shareFile(String name) {
        try {
            DocumentFile file = getFile(name);
            shareFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public void shareFile(int id) {
        try {
            DocumentFile file = getFile(id);
            shareFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMediaFile(String name) {
        try {
            DocumentFile file = getFile(name);
            if (file != null && file.isFile()) {
                DocumentsContract.deleteDocument(
                        getContentResolver(),
                        file.getUri()
                );
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
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
        DB_Media currentMedia = dbHelperGet.getSingleMedia(input);
        if (currentMedia == null) {
            return false;
        }
        String fileName = currentMedia.file;
        return existsMediaFile(fileName);
    }

    protected abstract void notifyFolderSet();

    protected abstract void notifyMissingAction(int id);
}
