package de.herrmann_engel.rbv;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

public class NewCard extends AppCompatActivity {

    TextView frontTextView;
    TextView backTextView;
    TextView notesTextView;
    private int collectionNo;
    private int packNo;
    private boolean reverse;
    private int sort;
    private String searchQuery;
    private int cardPosition;
    private ArrayList<Integer> savedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card);

        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        searchQuery = getIntent().getExtras().getString("searchQuery");
        cardPosition = getIntent().getExtras().getInt("cardPosition");
        savedList = getIntent().getExtras().getIntegerArrayList("savedList");
        TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
        TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        try {
            int packColors = dbHelperGet.getSinglePack(packNo).colors;
            if (packColors < Math.min(colors.length(), colorsBackground.length()) && packColors >= 0) {
                int color = colors.getColor(packColors, 0);
                int colorBackground = colorsBackground.getColor(packColors, 0);
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                Window window = this.getWindow();
                window.setStatusBarColor(color);
                findViewById(R.id.root_new_card).setBackgroundColor(colorBackground);
            }
            colors.recycle();
            colorsBackground.recycle();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }

        frontTextView = findViewById(R.id.new_card_front);
        backTextView = findViewById(R.id.new_card_back);
        notesTextView = findViewById(R.id.new_card_notes);
        notesTextView.setHint(String.format(getString(R.string.optional), getString(R.string.new_card_notes)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    public void insert(MenuItem menuItem) {
        String front = frontTextView.getText().toString();
        String back = backTextView.getText().toString();
        String notes = notesTextView.getText().toString();
        try {
            DB_Helper_Create dbHelperCreate = new DB_Helper_Create(getApplicationContext());
            long id = dbHelperCreate.createCard(front, back, notes, packNo);
            if (savedList != null) {
                savedList.add((int) id);
            }
            startListCards();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error_values, Toast.LENGTH_SHORT).show();
        }
    }

    private void startListCards() {
        Intent intent = new Intent(getApplicationContext(), ListCards.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", cardPosition);
        intent.putIntegerArrayListExtra("savedList", savedList);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        String front = frontTextView.getText().toString();
        String back = backTextView.getText().toString();
        String notes = notesTextView.getText().toString();
        if (front.isEmpty() && back.isEmpty() && notes.isEmpty()) {
            startListCards();
        } else {
            Dialog confirmCancel = new Dialog(this, R.style.dia_view);
            confirmCancel.setContentView(R.layout.dia_confirm);
            confirmCancel.setTitle(getResources().getString(R.string.discard_changes));
            confirmCancel.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);

            Button confirmCancelY = confirmCancel.findViewById(R.id.dia_confirm_yes);
            Button confirmCancelN = confirmCancel.findViewById(R.id.dia_confirm_no);
            confirmCancelY.setOnClickListener(v -> startListCards());
            confirmCancelN.setOnClickListener(v -> confirmCancel.dismiss());
            confirmCancel.show();
        }
    }
}
