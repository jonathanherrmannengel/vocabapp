package de.herrmann_engel.rbv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class EditCard extends AppCompatActivity {

    private int collectionNo;
    private int packNo;
    private int cardNo;
    private boolean reverse;
    private int sort;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_card);
        TextView addTextView = findViewById(R.id.edit_card_go);
        TextView frontTextView = findViewById(R.id.edit_card_front);
        TextView backTextView = findViewById(R.id.edit_card_back);
        TextView notesTextView = findViewById(R.id.edit_card_notes);
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        cardNo = getIntent().getExtras().getInt("card");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        DB_Helper_Update dbHelperUpdate = new DB_Helper_Update(this);
        try {
            DB_Card card = dbHelperGet.getSingleCard(cardNo);
            frontTextView.setText(card.front);
            backTextView.setText(card.back);
            notesTextView.setText(card.notes);

            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            int packColors = dbHelperGet.getSinglePack(card.pack).colors;
            if(packColors < Math.min(colors.length(), colorsBackground.length()) && packColors >= 0) {
                int color = colors.getColor(packColors,0);
                int colorBackground = colorsBackground.getColor(packColors ,0);
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                Window window = this.getWindow();
                window.setStatusBarColor(color);
                findViewById(R.id.root_edit_card).setBackgroundColor(colorBackground);
            }
            colors.recycle();
            colorsBackground.recycle();

            addTextView.setOnClickListener(v -> {
                card.front = frontTextView.getText().toString();
                card.back = backTextView.getText().toString();
                card.notes = notesTextView.getText().toString();
                if(dbHelperUpdate.updateCard(card)) {
                    Intent intent = new Intent(getApplicationContext(), ViewCard.class);
                    intent.putExtra("collection", collectionNo);
                    intent.putExtra("pack", packNo);
                    intent.putExtra("card", cardNo);
                    intent.putExtra("reverse", reverse);
                    intent.putExtra("sort", sort);
                    startActivity(intent);
                    this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ViewCard.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("card", cardNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        startActivity(intent);
        this.finish();
    }
}