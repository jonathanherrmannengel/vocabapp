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
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterPacksMoveCard;
import de.herrmann_engel.rbv.databinding.ActivityViewCardBinding;
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding;
import de.herrmann_engel.rbv.databinding.DiaPrintBinding;
import de.herrmann_engel.rbv.databinding.DiaRecBinding;
import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Media;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update;
import de.herrmann_engel.rbv.utils.StringTools;
import io.noties.markwon.Markwon;
import io.noties.markwon.linkify.LinkifyPlugin;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;


public class ViewCard extends FileTools {

    private ActivityViewCardBinding binding;
    private DB_Helper_Get dbHelperGet;
    private DB_Helper_Update dbHelperUpdate;
    private DB_Card card;
    private int known;
    private int collectionNo;
    private int packNo;
    private int cardNo;
    private boolean formatCardNotes;
    private ArrayList<DB_Media_Link_Card> imageList;
    private ArrayList<DB_Media_Link_Card> mediaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        cardNo = getIntent().getExtras().getInt("card");
        dbHelperGet = new DB_Helper_Get(this);
        dbHelperUpdate = new DB_Helper_Update(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        boolean formatCards = settings.getBoolean("format_cards", false);
        boolean increaseFontSize = settings.getBoolean("ui_font_size", false);
        try {
            card = dbHelperGet.getSingleCard(cardNo);
            String cardFront;
            if (formatCards) {
                StringTools formatString = new StringTools();
                SpannableString cardFrontSpannable = formatString.format(card.front);
                cardFront = cardFrontSpannable.toString();
                binding.cardFront.setText(cardFrontSpannable);
                binding.cardBack.setText(formatString.format(card.back));
            } else {
                cardFront = card.front;
                binding.cardFront.setText(cardFront);
                binding.cardBack.setText(card.back);
            }
            formatCardNotes = settings.getBoolean("format_card_notes", false);
            if (card.notes != null && !card.notes.isEmpty()) {
                binding.cardNotes.setVisibility(View.VISIBLE);
                if (formatCardNotes) {
                    final Markwon markwon = Markwon.builder(this)
                            .usePlugin(LinkifyPlugin.create(
                                    Linkify.WEB_URLS
                            ))
                            .build();
                    binding.cardNotes.setMovementMethod(BetterLinkMovementMethod.getInstance());
                    binding.cardNotes.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    markwon.setMarkdown(binding.cardNotes, card.notes);
                } else {
                    binding.cardNotes.setAutoLinkMask(Linkify.WEB_URLS);
                    binding.cardNotes.setText(card.notes);
                }
            } else {
                binding.cardNotes.setVisibility(View.GONE);
            }
            if (increaseFontSize) {
                binding.cardFront.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_front_size_big));
                binding.cardBack.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_back_size_big));
                binding.cardNotes.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_notes_size_big));
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Instant instant = Instant.ofEpochSecond(card.date);
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .withLocale(Locale.ROOT)
                        .withZone(ZoneId.systemDefault());
                binding.cardDate.setText(dateTimeFormatter.format(instant));
            } else {
                binding.cardDate.setText(new Date(card.date * 1000).toString());
            }
            known = card.known;
            updateCardKnown();
            updateColors();
            setTitle(cardFront);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
        setMediaButtons();
    }

    @Override
    protected void notifyFolderSet() {
        setMediaButtons();
    }

    @Override
    protected void notifyMissingAction(int id) {
        Intent intent = new Intent(this, EditCardMedia.class);
        intent.putExtra("card", cardNo);
        startActivity(intent);
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
                binding.getRoot().setBackgroundColor(colorBackground);
                binding.cardKnownProgress.setBackgroundColor(colorBackgroundLight);
            }
            colors.recycle();
            colorsBackground.recycle();
            colorsBackgroundLight.recycle();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCardKnown() {
        binding.cardKnown.setText(Integer.toString(known));
        binding.cardMinus.setColorFilter(Color.argb(255, 255, 255, 255));
        binding.cardMinus.setColorFilter(ContextCompat.getColor(this, known > 0 ? R.color.dark_red : R.color.dark_grey),
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
        Intent intent = new Intent(this, EditCard.class);
        intent.putExtra("card", cardNo);
        startActivity(intent);
    }

    public void editCardMedia(MenuItem menuItem) {
        Intent intent = new Intent(this, EditCardMedia.class);
        intent.putExtra("card", cardNo);
        startActivity(intent);
    }

    public void deleteCard(MenuItem menuItem) {
        Dialog confirmDeleteDialog = new Dialog(this, R.style.dia_view);
        DiaConfirmBinding bindingConfirmDeleteDialog = DiaConfirmBinding.inflate(getLayoutInflater());
        confirmDeleteDialog.setContentView(bindingConfirmDeleteDialog.getRoot());
        confirmDeleteDialog.setTitle(getResources().getString(R.string.delete));
        confirmDeleteDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        bindingConfirmDeleteDialog.diaConfirmYes.setOnClickListener(v -> {
            DB_Helper_Delete dbHelperDelete = new DB_Helper_Delete(this);
            dbHelperDelete.deleteCard(card);
            confirmDeleteDialog.dismiss();
            Intent intent = new Intent(this, ListCards.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("cardDeleted", cardNo);
            this.startActivity(intent);
        });
        bindingConfirmDeleteDialog.diaConfirmNo.setOnClickListener(v -> confirmDeleteDialog.dismiss());
        confirmDeleteDialog.show();
    }

    public void printCard(MenuItem menuItem) {
        Dialog printDialog = new Dialog(this, R.style.dia_view);
        DiaPrintBinding bindingPrintDialog = DiaPrintBinding.inflate(getLayoutInflater());
        printDialog.setContentView(bindingPrintDialog.getRoot());
        printDialog.setTitle(getResources().getString(R.string.print));
        printDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        if (card.notes == null || card.notes.isEmpty()) {
            bindingPrintDialog.diaPrintIncludeNotesLayout.setVisibility(View.GONE);
            bindingPrintDialog.diaPrintIncludeNotes.setChecked(false);
        }
        if (imageList.isEmpty() || imageList.size() > Globals.IMAGE_PREVIEW_MAX) {
            bindingPrintDialog.diaPrintIncludeImagesLayout.setVisibility(View.GONE);
            bindingPrintDialog.diaPrintIncludeImages.setChecked(false);
        }
        if (mediaList.isEmpty()) {
            bindingPrintDialog.diaPrintIncludeMediaLayout.setVisibility(View.GONE);
            bindingPrintDialog.diaPrintIncludeMedia.setChecked(false);
        }
        bindingPrintDialog.diaPrintStart.setOnClickListener(v -> {
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
            String title = stringTools.shorten(binding.cardFront.getText().toString(), 30);
            if (bindingPrintDialog.diaPrintIncludeProgress.isChecked()) {
                title += " (" + binding.cardKnown.getText() + ")";
            }
            htmlDocument += "<h1 id=\"main-title\" class=\"title first-title\" dir=\"auto\">" + title + "</h1>";
            htmlDocument += "<article>";
            htmlDocument += "<h2 class=\"title first-title\" dir=\"auto\">" + getString(R.string.card_front) + "</h2><div>";
            htmlDocument += HtmlCompat.toHtml((SpannableString) binding.cardFront.getText(), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
            htmlDocument += "</div></article>";
            htmlDocument += "<article>";
            htmlDocument += "<h2 class=\"title\" dir=\"auto\">" + getString(R.string.card_back) + "</h2><div>";
            htmlDocument += HtmlCompat.toHtml((SpannableString) binding.cardBack.getText(), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
            htmlDocument += "</div></article>";
            if (card.notes != null && !card.notes.isEmpty() && bindingPrintDialog.diaPrintIncludeNotes.isChecked()) {
                htmlDocument += "<article>";
                htmlDocument += "<h2 class=\"title\" dir=\"auto\">" + getString(R.string.card_notes) + "</h2><div dir=\"auto\">";
                if (formatCardNotes) {
                    Parser parser = Parser.builder().build();
                    Node document = parser.parse(card.notes);
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    htmlDocument += renderer.render(document);
                } else {
                    htmlDocument += HtmlCompat.toHtml((SpannableString) binding.cardNotes.getText(), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
                }
                htmlDocument += "</div></article>";
            }
            if (!imageList.isEmpty() && bindingPrintDialog.diaPrintIncludeImages.isChecked()) {
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
            if (!mediaList.isEmpty() && bindingPrintDialog.diaPrintIncludeMedia.isChecked()) {
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
        DiaRecBinding bindingMoveDialog = DiaRecBinding.inflate(getLayoutInflater());
        moveDialog.setContentView(bindingMoveDialog.getRoot());
        moveDialog.setTitle(getResources().getString(R.string.move_card));
        moveDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        List<DB_Pack> packs;
        if (collectionNo == -1) {
            packs = dbHelperGet.getAllPacks();
        } else {
            packs = dbHelperGet.getAllPacksByCollection(collectionNo);
        }
        AdapterPacksMoveCard adapter = new AdapterPacksMoveCard(packs, collectionNo, card, moveDialog);
        bindingMoveDialog.diaRec.setAdapter(adapter);
        bindingMoveDialog.diaRec.setLayoutManager(new LinearLayoutManager(this));
        moveDialog.show();
    }

    public void movedCard() {
        try {
            card = dbHelperGet.getSingleCard(cardNo);
            packNo = card.pack;
            updateColors();
            Intent intent = new Intent(this, ListCards.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("collection", collectionNo);
            intent.putExtra("pack", packNo);
            this.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setMediaButtons() {
        imageList = (ArrayList<DB_Media_Link_Card>) dbHelperGet.getImageMediaLinksByCard(cardNo);
        if (imageList.isEmpty()) {
            binding.viewCardImages.setVisibility(View.GONE);
        } else {
            binding.viewCardImages.setVisibility(View.VISIBLE);
        }
        binding.viewCardImages.setOnClickListener(v -> showImageListDialog(imageList));
        mediaList = (ArrayList<DB_Media_Link_Card>) dbHelperGet.getAllMediaLinksByCard(cardNo);
        if (mediaList.isEmpty()) {
            binding.viewCardMedia.setVisibility(View.GONE);
        } else {
            binding.viewCardMedia.setVisibility(View.VISIBLE);
        }
        binding.viewCardMedia.setOnClickListener(v -> showMediaListDialog(mediaList));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ListCards.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("cardUpdated", cardNo);
        this.startActivity(intent);
    }
}
