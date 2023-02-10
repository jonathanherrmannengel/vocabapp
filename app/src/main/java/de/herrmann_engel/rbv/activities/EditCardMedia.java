package de.herrmann_engel.rbv.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCard;
import de.herrmann_engel.rbv.databinding.ActivityEditCardMediaBinding;
import de.herrmann_engel.rbv.databinding.DiaFileExistsBinding;
import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class EditCardMedia extends FileTools {
    private ActivityEditCardMediaBinding binding;
    private int cardNo;

    private DB_Helper_Get dbHelperGet;

    final ActivityResultLauncher<Intent> openFile = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    try {
                        Intent data = result.getData();
                        Uri fileUri = data.getData();
                        DocumentFile input = DocumentFile.fromSingleUri(this, fileUri);
                        DocumentFile outputDirectory = DocumentFile.fromTreeUri(this, Uri.parse(getCardMediaFolder()));
                        if (outputDirectory == null || !outputDirectory.isDirectory()) {
                            showSelectDialog(getResources().getString(R.string.select_folder_help_reselect));
                        } else if (input != null) {
                            String inputFileName = input.getName();
                            String mime = input.getType();
                            if (inputFileName != null && outputDirectory.findFile(inputFileName) == null) {
                                createMediaFile(outputDirectory, mime, inputFileName, fileUri);
                            } else {
                                Dialog fileExistsDialog = new Dialog(this, R.style.dia_view);
                                DiaFileExistsBinding bindingFileExistsDialog = DiaFileExistsBinding.inflate(getLayoutInflater());
                                fileExistsDialog.setContentView(bindingFileExistsDialog.getRoot());
                                fileExistsDialog.setTitle(getResources().getString(R.string.file_exists_title));
                                fileExistsDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                        WindowManager.LayoutParams.MATCH_PARENT);

                                bindingFileExistsDialog.diaFileExistsLink.setOnClickListener(v -> {
                                    addMediaLink(inputFileName, mime);
                                    fileExistsDialog.dismiss();
                                });
                                bindingFileExistsDialog.diaFileExistsNew.setOnClickListener(v -> {
                                    createMediaFile(outputDirectory, mime, inputFileName, fileUri);
                                    fileExistsDialog.dismiss();
                                });
                                fileExistsDialog.show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCardMediaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        cardNo = getIntent().getExtras().getInt("card");
        dbHelperGet = new DB_Helper_Get(this);
        try {
            DB_Card card = dbHelperGet.getSingleCard(cardNo);
            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            int packColors = dbHelperGet.getSinglePack(card.pack).colors;
            if (packColors < Math.min(colors.length(), colorsBackground.length()) && packColors >= 0) {
                int color = colors.getColor(packColors, 0);
                int colorBackground = colorsBackground.getColor(packColors, 0);
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                Window window = this.getWindow();
                window.setStatusBarColor(color);
                binding.getRoot().setBackgroundColor(colorBackground);
            }
            colors.recycle();
            colorsBackground.recycle();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(Color.argb(75, 200, 200, 250));
        gradientDrawable.setStroke(2, Color.rgb(170, 170, 220));
        gradientDrawable.setCornerRadius(8);
        binding.addMediaButton.setBackground(gradientDrawable);
        binding.addMediaButton.setOnClickListener(v -> {
            String folder = getCardMediaFolder();
            if (folder == null || folder.isEmpty()) {
                showSelectDialog(getResources().getString(R.string.select_folder_help));
            }
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            openFile.launch(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRecView();
    }

    private void createMediaFile(DocumentFile outputDirectory, String mime, String inputFileName, Uri fileUri) {
        try {
            DocumentFile output = outputDirectory.createFile(mime, inputFileName);
            String outputFileName = output.getName();
            OutputStream outputStream = getContentResolver().openOutputStream(output.getUri());
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            addMediaLink(outputFileName, mime);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    private void addMediaLink(String outputFileName, String mime) {
        try {
            DB_Helper_Create dbHelperCreate = new DB_Helper_Create(this);
            dbHelperCreate.createMedia(outputFileName, mime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (dbHelperGet.existsMedia(outputFileName)) {
                DB_Media media = dbHelperGet.getSingleMedia(outputFileName);
                int fileId = media.uid;
                DB_Helper_Create dbHelperCreate = new DB_Helper_Create(this);
                dbHelperCreate.createMediaLink(fileId, cardNo);
                setRecView();
                Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    private void setRecView() {
        ArrayList<DB_Media_Link_Card> mediaList = (ArrayList<DB_Media_Link_Card>) dbHelperGet.getAllMediaLinksByCard(cardNo);
        AdapterMediaLinkCard adapter = new AdapterMediaLinkCard(mediaList, cardNo, getCardMediaFolder());
        binding.recCardMedia.setAdapter(adapter);
        binding.recCardMedia.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void notifyFolderSet() {
        setRecView();
    }

    @Override
    protected void notifyMissingAction(int id) {
    }
}
