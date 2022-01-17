package de.herrmann_engel.rbv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class ViewCard extends AppCompatActivity {

    private DB_Helper_Update dbHelperUpdate;
    private DB_Card card;
    private int known;
    private int collectionNo;
    private int packNo;
    private int cardNo;
    private boolean reverse;
    private int sort;
    TextView knownText;
    ImageButton knownMinus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_card);
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        cardNo = getIntent().getExtras().getInt("card");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        dbHelperUpdate = new DB_Helper_Update(this);
        try {
            card = dbHelperGet.getSingleCard(cardNo);
            setTitle(card.front);
            TextView front = findViewById(R.id.card_front);
            front.setText(card.front);
            TextView back = findViewById(R.id.card_back);
            back.setText(card.back);
            TextView notes = findViewById(R.id.card_notes);
            notes.setText(card.notes);
            TextView date = findViewById(R.id.card_date);
            date.setText(new java.util.Date(card.date*1000).toString());
            knownText = findViewById(R.id.card_known);
            known = card.known;
            knownMinus = findViewById(R.id.card_minus);
            card_known_update();
            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            TypedArray colorsBackgroundLight = getResources().obtainTypedArray(R.array.pack_color_background_light);
            int packColors = dbHelperGet.getSinglePack(card.pack).colors;
            if(packColors < Math.min(Math.min(colors.length(), colorsBackground.length()), colorsBackgroundLight.length()) && packColors >= 0) {
                int color = colors.getColor(packColors,0);
                int colorBackground = colorsBackground.getColor(packColors ,0);
                int colorBackgroundLight = colorsBackgroundLight.getColor(packColors ,0);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_card, menu);
        return true;
    }
    private void card_known_update() {
        knownText.setText(Integer.toString(known));
        knownMinus.setColorFilter(Color.argb(255, 255, 255, 255));
        knownMinus.setColorFilter(ContextCompat.getColor(this, known > 0 ? R.color.dark_red: R.color.dark_grey), android.graphics.PorterDuff.Mode.MULTIPLY);
    }
    public void card_plus (View v) {
        known++;
        card.known = known;
        dbHelperUpdate.updateCard(card);
        card_known_update() ;
    }
    public void card_minus (View v) {
        known = Math.max(0,--known);
        card.known = known;
        dbHelperUpdate.updateCard(card);
        card_known_update();
    }
    public void editCard(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), EditCard.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("card", cardNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        startActivity(intent);
        this.finish();
    }
    public void deleteCard(MenuItem menuItem) {
        Dialog confirmDelete = new Dialog(this, R.style.dia_view);
        confirmDelete.setContentView(R.layout.dia_del);
        confirmDelete.setTitle(getResources().getString(R.string.delete));
        confirmDelete.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        Button confirmDeleteY = confirmDelete.findViewById(R.id.dia_del_yes);
        Button confirmDeleteN = confirmDelete.findViewById(R.id.dia_del_no);
        confirmDeleteY.setOnClickListener(v -> {
            DB_Helper_Delete dbHelperDelete = new DB_Helper_Delete(this);
            dbHelperDelete.deleteCard(card);
            Intent intent = new Intent(getApplicationContext(), ListCards.class);
            intent.putExtra("collection", collectionNo);
            intent.putExtra("pack", packNo);
            intent.putExtra("reverse", reverse);
            intent.putExtra("sort", sort);
            startActivity(intent);
            this.finish();
        });
        confirmDeleteN.setOnClickListener(v -> confirmDelete.dismiss());
        confirmDelete.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ListCards.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        startActivity(intent);
        this.finish();
    }
}