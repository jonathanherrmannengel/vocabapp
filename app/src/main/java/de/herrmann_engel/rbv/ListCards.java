package de.herrmann_engel.rbv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

public class ListCards extends AppCompatActivity {

    private DB_Helper_Get dbHelperGet;

    private int collectionNo;
    private int packNo;
    private boolean reverse;
    private int sort;
    private int cardPosition;

    MenuItem changeFrontBackItem;
    MenuItem sortRandomItem;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_rec);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_cards, menu);
        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort", settings.getInt("default_sort", Globals.SORT_DEFAULT));
        cardPosition = getIntent().getExtras().getInt("cardPosition");
        if(packNo == -1) {
            MenuItem startNewCard = menu.findItem(R.id.start_new_card);
            startNewCard.setVisible(false);
            MenuItem packDetails = menu.findItem(R.id.pack_details);
            packDetails.setVisible(false);
        }
        changeFrontBackItem = menu.findItem(R.id.change_front_back);
        sortRandomItem = menu.findItem(R.id.sort_random);

        recyclerView = this.findViewById(R.id.rec_default);

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
        intent.putExtra("cardPosition", ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstVisibleItemPosition());
        this.startActivity(intent);
        this.finish();
    }
    public void changeFrontBack(MenuItem menuItem) {
        reverse = !reverse;
        setRecView();
    }
    public void sort(MenuItem menuItem) {
        sort++;
        cardPosition = 0;
        setRecView();
    }

    public void packDetails(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), ViewPack.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("cardPosition", ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).findFirstVisibleItemPosition());
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
        AdapterCards adapter = new AdapterCards(cardsList,this, reverse, sort, packNo, collectionNo);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if(sort != Globals.SORT_RANDOM) {
            recyclerView.scrollToPosition(Math.min(cardPosition, Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1));
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListPacks.class);
        intent.putExtra("collection", collectionNo);
        startActivity(intent);
        this.finish();
    }
}
