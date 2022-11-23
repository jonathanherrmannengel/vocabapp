package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterPacksMoveCard;
import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update;
import de.herrmann_engel.rbv.utils.StringTools;
import io.noties.markwon.Markwon;


public class ViewCard extends FileTools {

    private TextView knownText;
    private TextView frontText;
    private TextView backText;
    private TextView notesText;
    private ImageButton knownMinus;
    private DB_Helper_Get dbHelperGet;
    private DB_Helper_Update dbHelperUpdate;
    private DB_Card card;
    private int known;
    private int collectionNo;
    private int packNo;
    private ArrayList<Integer> packNos;
    private int cardNo;
    private boolean reverse;
    private int sort;
    private String searchQuery;
    private int cardPosition;
    private boolean progressGreater;
    private int progressNumber;
    private ArrayList<Integer> savedList;
    private Long savedListSeed;
    private boolean formatCardNotes;
    private ArrayList<DB_Media_Link_Card> imageList;
    private ArrayList<DB_Media_Link_Card> mediaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_card);
        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        packNos = getIntent().getExtras().getIntegerArrayList("packs");
        cardNo = getIntent().getExtras().getInt("card");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        searchQuery = getIntent().getExtras().getString("searchQuery");
        cardPosition = getIntent().getExtras().getInt("cardPosition");
        progressGreater = getIntent().getExtras().getBoolean("progressGreater");
        progressNumber = getIntent().getExtras().getInt("progressNumber");
        savedList = getIntent().getExtras().getIntegerArrayList("savedList");
        savedListSeed = getIntent().getExtras().getLong("savedListSeed");
        dbHelperGet = new DB_Helper_Get(this);
        dbHelperUpdate = new DB_Helper_Update(this);
        boolean formatCards = settings.getBoolean("format_cards", false);
        boolean increaseFontSize = settings.getBoolean("ui_font_size", false);
        frontText = findViewById(R.id.card_front);
        backText = findViewById(R.id.card_back);
        knownText = findViewById(R.id.card_known);
        notesText = findViewById(R.id.card_notes);
        try {
            card = dbHelperGet.getSingleCard(cardNo);
            String cardFront;
            if (formatCards) {
                StringTools formatString = new StringTools();
                SpannableString cardFrontSpannable = formatString.format(card.front);
                cardFront = cardFrontSpannable.toString();
                frontText.setText(cardFrontSpannable);
                backText.setText(formatString.format(card.back));
            } else {
                cardFront = card.front;
                frontText.setText(cardFront);
                backText.setText(card.back);
            }
            formatCardNotes = settings.getBoolean("format_card_notes", false);
            if (card.notes != null && !card.notes.isEmpty()) {
                if (formatCardNotes) {
                    final Markwon markwon = Markwon.create(this);
                    notesText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    markwon.setMarkdown(notesText, card.notes);
                } else {
                    notesText.setText(card.notes);
                }
            } else {
                notesText.setVisibility(View.GONE);
            }
            if (increaseFontSize) {
                frontText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_front_size_big));
                backText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_back_size_big));
                notesText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_notes_size_big));
            }
            TextView date = findViewById(R.id.card_date);
            date.setText(new Date(card.date * 1000).toString());
            known = card.known;
            knownMinus = findViewById(R.id.card_minus);
            updateCardKnown();
            updateColors();
            setTitle(cardFront);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
        setMediaButtons();
    }

    @Override
    protected void notifyFolderSet() {
        setMediaButtons();
    }

    @Override
    protected void notifyMissingAction(int id) {
        Intent intent = new Intent(this.getApplicationContext(), EditCardMedia.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putIntegerArrayListExtra("packs", packNos);
        intent.putExtra("card", id);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", 0);
        intent.putExtra("progressGreater", progressGreater);
        intent.putExtra("progressNumber", progressNumber);
        intent.putIntegerArrayListExtra("savedList", savedList);
        intent.putExtra("savedListSeed", savedListSeed);
        this.startActivity(intent);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_card, menu);
        if (packNo < 0) {
            menu.findItem(R.id.move_card).setVisible(false);
        }
        return true;
    }

    private void updateColors() {
        try {
            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            TypedArray colorsBackgroundLight = getResources().obtainTypedArray(R.array.pack_color_background_light);
            int packColors = dbHelperGet.getSinglePack(card.pack).colors;
            if (packColors < Math.min(Math.min(colors.length(), colorsBackground.length()),
                    colorsBackgroundLight.length()) && packColors >= 0) {
                int color = colors.getColor(packColors, 0);
                int colorBackground = colorsBackground.getColor(packColors, 0);
                int colorBackgroundLight = colorsBackgroundLight.getColor(packColors, 0);
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                Window window = this.getWindow();
                window.setStatusBarColor(color);
                findViewById(R.id.root_view_card).setBackgroundColor(colorBackground);
                findViewById(R.id.card_known_progress).setBackgroundColor(colorBackgroundLight);
            }
            colors.recycle();
            colorsBackground.recycle();
            colorsBackgroundLight.recycle();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCardKnown() {
        knownText.setText(Integer.toString(known));
        knownMinus.setColorFilter(Color.argb(255, 255, 255, 255));
        knownMinus.setColorFilter(ContextCompat.getColor(this, known > 0 ? R.color.dark_red : R.color.dark_grey),
                android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    public void updateCardKnownPlus(View v) {
        known++;
        card.known = known;
        dbHelperUpdate.updateCard(card);
        updateCardKnown();
    }

    public void updateCardKnownMinus(View v) {
        known = Math.max(0, --known);
        card.known = known;
        dbHelperUpdate.updateCard(card);
        updateCardKnown();
    }

    public void editCard(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), EditCard.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putIntegerArrayListExtra("packs", packNos);
        intent.putExtra("card", cardNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", cardPosition);
        intent.putExtra("progressGreater", progressGreater);
        intent.putExtra("progressNumber", progressNumber);
        intent.putIntegerArrayListExtra("savedList", savedList);
        intent.putExtra("savedListSeed", savedListSeed);
        startActivity(intent);
        this.finish();
    }

    public void editCardMedia(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), EditCardMedia.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putIntegerArrayListExtra("packs", packNos);
        intent.putExtra("card", cardNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", cardPosition);
        intent.putExtra("progressGreater", progressGreater);
        intent.putExtra("progressNumber", progressNumber);
        intent.putIntegerArrayListExtra("savedList", savedList);
        intent.putExtra("savedListSeed", savedListSeed);
        startActivity(intent);
        this.finish();
    }

    public void deleteCard(MenuItem menuItem) {
        Dialog confirmDelete = new Dialog(this, R.style.dia_view);
        confirmDelete.setContentView(R.layout.dia_confirm);
        confirmDelete.setTitle(getResources().getString(R.string.delete));
        confirmDelete.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        Button confirmDeleteY = confirmDelete.findViewById(R.id.dia_confirm_yes);
        Button confirmDeleteN = confirmDelete.findViewById(R.id.dia_confirm_no);
        confirmDeleteY.setOnClickListener(v -> {
            DB_Helper_Delete dbHelperDelete = new DB_Helper_Delete(this);
            dbHelperDelete.deleteCard(card);
            if (savedList != null) {
                savedList.remove(Integer.valueOf(card.uid));
            }
            confirmDelete.dismiss();
            Intent intent = new Intent(getApplicationContext(), ListCards.class);
            intent.putExtra("collection", collectionNo);
            intent.putExtra("pack", packNo);
            intent.putIntegerArrayListExtra("packs", packNos);
            intent.putExtra("reverse", reverse);
            intent.putExtra("sort", sort);
            intent.putExtra("searchQuery", searchQuery);
            intent.putExtra("cardPosition", cardPosition);
            intent.putExtra("progressGreater", progressGreater);
            intent.putExtra("progressNumber", progressNumber);
            intent.putIntegerArrayListExtra("savedList", savedList);
            startActivity(intent);
            this.finish();
        });
        confirmDeleteN.setOnClickListener(v -> confirmDelete.dismiss());
        confirmDelete.show();
    }

    public void printCard(MenuItem menuItem) {
        Dialog printDialog = new Dialog(this, R.style.dia_view);
        printDialog.setContentView(R.layout.dia_print);
        printDialog.setTitle(getResources().getString(R.string.print));
        printDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        CheckBox progressCheckBox = printDialog.findViewById(R.id.dia_print_include_progress);
        LinearLayout notesLayout = printDialog.findViewById(R.id.dia_print_include_notes_layout);
        CheckBox notesCheckBox = printDialog.findViewById(R.id.dia_print_include_notes);
        LinearLayout imagesLayout = printDialog.findViewById(R.id.dia_print_include_images_layout);
        CheckBox imagesCheckBox = printDialog.findViewById(R.id.dia_print_include_images);
        LinearLayout mediaLayout = printDialog.findViewById(R.id.dia_print_include_media_layout);
        CheckBox mediaCheckBox = printDialog.findViewById(R.id.dia_print_include_media);
        Button startPrintButton = printDialog.findViewById(R.id.dia_print_start);
        if (card.notes == null || card.notes.isEmpty()) {
            notesLayout.setVisibility(View.GONE);
            notesCheckBox.setChecked(false);
        }
        if (imageList.isEmpty() || imageList.size() > Globals.IMAGE_PREVIEW_MAX) {
            imagesLayout.setVisibility(View.GONE);
            imagesCheckBox.setChecked(false);
        }
        if (mediaList.isEmpty()) {
            mediaLayout.setVisibility(View.GONE);
            mediaCheckBox.setChecked(false);
        }
        startPrintButton.setOnClickListener(v -> {
            Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show();
            printDialog.dismiss();
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            String jobName = "rbv_flashcard_" + cardNo;
            PrintAttributes.Builder builder = new PrintAttributes.Builder();
            builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
            PrintAttributes attributes = builder.build();
            WebView webView = new WebView(this);
            webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter(jobName);
                    printManager.print(jobName, adapter, attributes);
                }
            });
            StringTools stringTools = new StringTools();
            String htmlDocument = "<!doctype html><html><head><meta charset=\"utf-8\"><title>print</title><style>#main-title{margin-bottom: 30px;}.title{text-align:center;color: #000007;}.title:not(.first-title)::before{margin-bottom:20px;margin-top:30px;display:block;content:' ';width:100%;height:1px;outline:2px solid #555;outline-offset:-1px;}.image-div,.media-div{text-align:center}.image{padding:10px;max-width:30%;max-height:10%;object-fit:contain;}</style></head>";
            String title = stringTools.shorten(frontText.getText().toString(), 30);
            if (progressCheckBox.isChecked()) {
                title += " (" + knownText.getText() + ")";
            }
            htmlDocument += "<h1 id=\"main-title\" class=\"title first-title\" dir=\"auto\">" + title + "</h1>";
            htmlDocument += "<article>";
            htmlDocument += "<h2 class=\"title first-title\" dir=\"auto\">" + getString(R.string.card_front) + "</h2><div>";
            htmlDocument += HtmlCompat.toHtml((SpannableString) frontText.getText(), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
            htmlDocument += "</div></article>";
            htmlDocument += "<article>";
            htmlDocument += "<h2 class=\"title\" dir=\"auto\">" + getString(R.string.card_back) + "</h2><div>";
            htmlDocument += HtmlCompat.toHtml((SpannableString) backText.getText(), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
            htmlDocument += "</div></article>";
            if (card.notes != null && !card.notes.isEmpty() && notesCheckBox.isChecked()) {
                htmlDocument += "<article>";
                htmlDocument += "<h2 class=\"title\" dir=\"auto\">" + getString(R.string.card_notes) + "</h2><div dir=\"auto\">";
                if (formatCardNotes) {
                    Parser parser = Parser.builder().build();
                    Node document = parser.parse(card.notes);
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    htmlDocument += renderer.render(document);
                } else {
                    htmlDocument += HtmlCompat.toHtml((SpannableString) notesText.getText(), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
                }
                htmlDocument += "</div></article>";
            }
            if (!imageList.isEmpty() && imagesCheckBox.isChecked()) {
                htmlDocument += "<article>";
                htmlDocument += "<h2 class=\"title\" dir=\"auto\">" + getString(R.string.image_media) + "</h2><div>";
                for (DB_Media_Link_Card i : imageList) {
                    DB_Media currentMedia = dbHelperGet.getSingleMedia(i.file);
                    if (currentMedia != null) {
                        Uri uri = getImageUri(currentMedia.uid);
                        if (uri != null) {
                            htmlDocument += "<div class=\"image-div\"><img class=\"image\" alt=\"" + currentMedia.file + "\" src=\"" + uri + "\"></div>";
                        }
                    }
                }
                htmlDocument += "</div></article>";
            }
            if (!mediaList.isEmpty() && mediaCheckBox.isChecked()) {
                htmlDocument += "<article>";
                htmlDocument += "<h2 class=\"title\" dir=\"auto\">" + getString(R.string.all_media) + "</h2><div>";
                for (DB_Media_Link_Card i : mediaList) {
                    DB_Media currentMedia = dbHelperGet.getSingleMedia(i.file);
                    if (currentMedia != null) {
                        htmlDocument += "<div class=\"media-div\">" + currentMedia.file + "</div>";
                    }
                }
                htmlDocument += "</div></article>";
            }
            htmlDocument += "</html>";
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        });
        printDialog.show();
    }

    public void moveCard(MenuItem menuItem) {
        Dialog moveDialog = new Dialog(this, R.style.dia_view);
        moveDialog.setContentView(R.layout.dia_rec);
        moveDialog.setTitle(getResources().getString(R.string.move_card));
        moveDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        List<DB_Pack> packs;
        if (collectionNo == -1) {
            packs = dbHelperGet.getAllPacks();
        } else {
            packs = dbHelperGet.getAllPacksByCollection(collectionNo);
        }
        RecyclerView recyclerView = moveDialog.findViewById(R.id.dia_rec);
        AdapterPacksMoveCard adapter = new AdapterPacksMoveCard(packs, collectionNo, this, card, moveDialog);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        moveDialog.show();
    }

    public void movedCard() {
        savedList = null;
        savedListSeed = null;
        try {
            card = dbHelperGet.getSingleCard(cardNo);
            packNo = card.pack;
            updateColors();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setMediaButtons() {
        imageList = (ArrayList<DB_Media_Link_Card>) dbHelperGet.getImageMediaLinksByCard(cardNo);
        Button showImages = findViewById(R.id.view_card_images);
        if (imageList.isEmpty()) {
            showImages.setVisibility(View.GONE);
        } else {
            showImages.setVisibility(View.VISIBLE);
        }
        showImages.setOnClickListener(v -> showImageListDialog(imageList));
        mediaList = (ArrayList<DB_Media_Link_Card>) dbHelperGet.getAllMediaLinksByCard(cardNo);
        Button showAllMedia = findViewById(R.id.view_card_media);
        if (mediaList.isEmpty()) {
            showAllMedia.setVisibility(View.GONE);
        } else {
            showAllMedia.setVisibility(View.VISIBLE);
        }
        showAllMedia.setOnClickListener(v -> showMediaListDialog(mediaList));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListCards.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putIntegerArrayListExtra("packs", packNos);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", cardPosition);
        intent.putExtra("progressGreater", progressGreater);
        intent.putExtra("progressNumber", progressNumber);
        intent.putIntegerArrayListExtra("savedList", savedList);
        intent.putExtra("savedListSeed", savedListSeed);
        startActivity(intent);
        this.finish();
    }
}
