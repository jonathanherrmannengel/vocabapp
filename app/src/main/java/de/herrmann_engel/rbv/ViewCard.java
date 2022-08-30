package de.herrmann_engel.rbv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import io.noties.markwon.Markwon;

public class ViewCard extends AppCompatActivity {

    private DB_Helper_Get dbHelperGet;
    private DB_Helper_Update dbHelperUpdate;
    private DB_Card card;
    private int known;
    private int collectionNo;
    private int packNo;
    private int cardNo;
    private boolean reverse;
    private int sort;
    private String searchQuery;
    private int cardPosition;

    TextView knownText;
    ImageButton knownMinus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_card);
        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        cardNo = getIntent().getExtras().getInt("card");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        searchQuery = getIntent().getExtras().getString("searchQuery");
        cardPosition = getIntent().getExtras().getInt("cardPosition");
        dbHelperGet = new DB_Helper_Get(this);
        dbHelperUpdate = new DB_Helper_Update(this);
        boolean formatCards = settings.getBoolean("format_cards", false);
        boolean increaseFontSize = settings.getBoolean("ui_font_size", false);
        try {
            card = dbHelperGet.getSingleCard(cardNo);
            TextView front = findViewById(R.id.card_front);
            TextView back = findViewById(R.id.card_back);
            String cardFront;
            if(formatCards){
                FormatString formatString = new FormatString();
                SpannableString cardFrontSpannable = formatString.formatString(card.front);
                cardFront = cardFrontSpannable.toString();
                front.setText(cardFrontSpannable);
                back.setText(formatString.formatString(card.back));
            } else {
                cardFront = card.front;
                front.setText(cardFront);
                back.setText(card.back);
            }
            TextView notes = findViewById(R.id.card_notes);
            if(settings.getBoolean("format_card_notes", false)){
                final Markwon markwon = Markwon.create(this);
                notes.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                markwon.setMarkdown(notes, card.notes);
            } else {
                notes.setText(card.notes);
            }
            if(increaseFontSize){
                front.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_front_size_big));
                back.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_back_size_big));
                notes.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_notes_size_big));
            }
            TextView date = findViewById(R.id.card_date);
            date.setText(new java.util.Date(card.date * 1000).toString());
            knownText = findViewById(R.id.card_known);
            known = card.known;
            knownMinus = findViewById(R.id.card_minus);
            updateCardKnown();
            updateColors();
            setTitle(cardFront);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_card, menu);
        if (packNo == -1) {
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
        intent.putExtra("card", cardNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", cardPosition);
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
            Intent intent = new Intent(getApplicationContext(), ListCards.class);
            intent.putExtra("collection", collectionNo);
            intent.putExtra("pack", packNo);
            intent.putExtra("reverse", reverse);
            intent.putExtra("sort", sort);
            intent.putExtra("searchQuery", searchQuery);
            intent.putExtra("cardPosition", cardPosition);
            startActivity(intent);
            this.finish();
        });
        confirmDeleteN.setOnClickListener(v -> confirmDelete.dismiss());
        confirmDelete.show();
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

    public void movedPack() {
        try {
            card = dbHelperGet.getSingleCard(cardNo);
            packNo = card.pack;
            updateColors();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListCards.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", cardPosition);
        startActivity(intent);
        this.finish();
    }
}
