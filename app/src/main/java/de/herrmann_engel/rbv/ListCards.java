package de.herrmann_engel.rbv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ListCards extends AppCompatActivity {

    private DB_Helper_Get dbHelperGet;

    private int collectionNo;
    private int packNo;
    private boolean reverse;
    private int sort;

    MenuItem changeFrontBackItem;
    MenuItem sortRandomItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_rec);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_cards, menu);
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        if(packNo == -1) {
            MenuItem startNewCard = menu.findItem(R.id.startNewCard);
            startNewCard.setVisible(false);
            MenuItem packDetails = menu.findItem(R.id.packDetails);
            packDetails.setVisible(false);
        }
        changeFrontBackItem = menu.findItem(R.id.changeFrontBack);
        sortRandomItem = menu.findItem(R.id.sortRandom);
        dbHelperGet = new DB_Helper_Get(this);
        try {
            if(packNo == -1) {
                setTitle(dbHelperGet.getSingleCollection(collectionNo).name);
            } else {
                setTitle(dbHelperGet.getSinglePack(packNo).name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setRecView();
        return true;
    }
    public void startNewCard(MenuItem menuItem)  {
        Intent intent = new Intent(this.getApplicationContext(), NewCard.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        this.startActivity(intent);
        this.finish();
    }
    public void changeFrontBack(MenuItem menuItem) {
        reverse = !reverse;
        setRecView();
    }
    public void sort(MenuItem menuItem) {
        sort++;
        setRecView();
    }

    public void packDetails(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), ViewPack.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        this.startActivity(intent);
        this.finish();
    }
    public void setRecView() {
        if(sort == Globals.SORT_RANDOM) {
            sortRandomItem.setTitle(R.string.sort_alphabetical);
        } else if (sort == Globals.SORT_ALPHABETICAL) {
            sortRandomItem.setTitle(R.string.sort_normal);
        } else {
            sort = Globals.SORT_DEFAULT;
            sortRandomItem.setTitle(R.string.sort_random);
        }
        List<DB_Card> cardsList;
        if(packNo == -1) {
            cardsList = dbHelperGet.getAllCardsByCollection(collectionNo, sort);
        } else {
            cardsList = dbHelperGet.getAllCardsByPack(packNo, sort);
            try {
                int packColors = dbHelperGet.getSinglePack(packNo).colors;
                TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
                TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
                if (packColors < Math.min(colors.length(), colorsBackground.length()) && packColors >= 0) {
                    int color = colors.getColor(packColors, 0);
                    int colorBackground = colorsBackground.getColor(packColors, 0);
                    Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                    Window window = this.getWindow();
                    window.setStatusBarColor(color);
                    findViewById(R.id.rec_default_root).setBackgroundColor(colorBackground);
                }
                colors.recycle();
                colorsBackground.recycle();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
        changeFrontBackItem.setTitle(reverse ? R.string.change_back_front : R.string.change_front_back);
        if(cardsList.size() == 0) {
            changeFrontBackItem.setVisible(false);
        }
        if(cardsList.size() <= 1) {
            sortRandomItem.setVisible(false);
        }
        RecyclerView recyclerView = this.findViewById(R.id.rec_default);
        AdapterCards adapter = new AdapterCards(cardsList,this, reverse, sort, packNo, collectionNo);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ListPacks.class);
        intent.putExtra("collection", collectionNo);
        startActivity(intent);
        this.finish();
    }
}