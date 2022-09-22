package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Objects;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update;

public class EditCard extends AppCompatActivity {

    TextView frontTextView;
    TextView backTextView;
    TextView notesTextView;
    DB_Card card;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_card);
        frontTextView = findViewById(R.id.edit_card_front);
        backTextView = findViewById(R.id.edit_card_back);
        notesTextView = findViewById(R.id.edit_card_notes);
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
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        try {
            card = dbHelperGet.getSingleCard(cardNo);
            frontTextView.setText(card.front);
            if (card.front.contains(System.getProperty("line.separator"))) {
                frontTextView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                frontTextView.setSingleLine(false);
            }
            backTextView.setText(card.back);
            if (card.back.contains(System.getProperty("line.separator"))) {
                backTextView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                backTextView.setSingleLine(false);
            }
            notesTextView.setText(card.notes);
            notesTextView.setHint(String.format(getString(R.string.optional), getString(R.string.new_card_notes)));

            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            int packColors = dbHelperGet.getSinglePack(card.pack).colors;
            if (packColors < Math.min(colors.length(), colorsBackground.length()) && packColors >= 0) {
                int color = colors.getColor(packColors, 0);
                int colorBackground = colorsBackground.getColor(packColors, 0);
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                Window window = this.getWindow();
                window.setStatusBarColor(color);
                findViewById(R.id.root_edit_card).setBackgroundColor(colorBackground);
            }
            colors.recycle();
            colorsBackground.recycle();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    public void saveChanges(MenuItem menuItem) {
        card.front = frontTextView.getText().toString();
        card.back = backTextView.getText().toString();
        card.notes = notesTextView.getText().toString();
        DB_Helper_Update dbHelperUpdate = new DB_Helper_Update(this);
        if (dbHelperUpdate.updateCard(card)) {
            startViewCard();
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_values, Toast.LENGTH_SHORT).show();
        }
    }

    private void startViewCard() {
        Intent intent = new Intent(getApplicationContext(), ViewCard.class);
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
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        String front = frontTextView.getText().toString();
        String back = backTextView.getText().toString();
        String notes = notesTextView.getText().toString();
        if (card == null || (card.front.equals(front) && card.back.equals(back) && card.notes.equals(notes))) {
            startViewCard();
        } else {
            Dialog confirmCancel = new Dialog(this, R.style.dia_view);
            confirmCancel.setContentView(R.layout.dia_confirm);
            confirmCancel.setTitle(getResources().getString(R.string.discard_changes));
            confirmCancel.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            Button confirmCancelY = confirmCancel.findViewById(R.id.dia_confirm_yes);
            Button confirmCancelN = confirmCancel.findViewById(R.id.dia_confirm_no);
            confirmCancelY.setOnClickListener(v -> startViewCard());
            confirmCancelN.setOnClickListener(v -> confirmCancel.dismiss());
            confirmCancel.show();
        }
    }
}