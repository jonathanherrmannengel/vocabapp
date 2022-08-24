package de.herrmann_engel.rbv;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

public class ViewPack extends AppCompatActivity {

    private DB_Helper_Get dbHelperGet;
    private DB_Pack pack;
    private int packNo;
    private int collectionNo;
    private boolean reverse;
    private int sort;
    private String searchQuery;
    private int cardPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_collection_or_pack);

        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        searchQuery = getIntent().getExtras().getString("searchQuery");
        cardPosition = getIntent().getExtras().getInt("cardPosition");
        dbHelperGet = new DB_Helper_Get(this);
        try {
            pack = dbHelperGet.getSinglePack(packNo);
            setTitle(pack.name);
            TextView nameTextView = findViewById(R.id.collection_or_pack_name);
            nameTextView.setText(pack.name);
            TextView descTextView = findViewById(R.id.collection_or_pack_desc);
            if (pack.desc.equals("")) {
                descTextView.setVisibility(View.GONE);
            } else {
                descTextView.setText(pack.desc);
            }
            TextView dateTextView = findViewById(R.id.collection_or_pack_date);
            dateTextView.setText(new java.util.Date(pack.date * 1000).toString());
            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            if (pack.colors < Math.min(colors.length(), colorsBackground.length()) && pack.colors >= 0) {
                int color = colors.getColor(pack.colors, 0);
                int colorBackground = colorsBackground.getColor(pack.colors, 0);
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                Window window = this.getWindow();
                window.setStatusBarColor(color);
                findViewById(R.id.root_view_collection_or_pack).setBackgroundColor(colorBackground);
            }
            colors.recycle();
            colorsBackground.recycle();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_pack, menu);
        if (collectionNo == -1) {
            menu.findItem(R.id.move_pack).setVisible(false);
        }
        return true;
    }

    public void editPack(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), EditPack.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", cardPosition);
        startActivity(intent);
        this.finish();
    }

    public void deletePack(boolean forceDelete) {
        Dialog confirmDelete = new Dialog(this, R.style.dia_view);
        confirmDelete.setContentView(R.layout.dia_confirm);
        confirmDelete.setTitle(getResources().getString(R.string.delete));
        confirmDelete.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        Button confirmDeleteY = confirmDelete.findViewById(R.id.dia_confirm_yes);
        Button confirmDeleteN = confirmDelete.findViewById(R.id.dia_confirm_no);

        if (dbHelperGet.getAllCardsByPack(pack.uid).size() > 0 && !forceDelete) {
            TextView confirmDeleteDesc = confirmDelete.findViewById(R.id.dia_confirm_desc);
            confirmDeleteDesc.setText(R.string.delete_pack_with_cards);
            confirmDeleteDesc.setVisibility(View.VISIBLE);
        }
        confirmDeleteY.setOnClickListener(v -> {
            if (dbHelperGet.getAllCardsByPack(pack.uid).size() == 0 || forceDelete) {
                DB_Helper_Delete dbHelperDelete = new DB_Helper_Delete(getApplicationContext());
                dbHelperDelete.deletePack(pack, forceDelete);
                Intent intent = new Intent(getApplicationContext(), ListPacks.class);
                intent.putExtra("collection", collectionNo);
                startActivity(intent);
                this.finish();
            } else {
                deletePack(true);
                confirmDelete.dismiss();
            }
        });
        confirmDeleteN.setOnClickListener(v -> confirmDelete.dismiss());
        confirmDelete.show();
    }

    public void deletePack(MenuItem menuItem) {
        deletePack(false);
    }

    public void movePack(MenuItem menuItem) {
        Dialog moveDialog = new Dialog(this, R.style.dia_view);
        moveDialog.setContentView(R.layout.dia_rec);
        moveDialog.setTitle(getResources().getString(R.string.move_pack));
        moveDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        List<DB_Collection> collections = dbHelperGet.getAllCollections();
        RecyclerView recyclerView = moveDialog.findViewById(R.id.dia_rec);
        AdapterCollectionsMovePack adapter = new AdapterCollectionsMovePack(collections, pack, this, moveDialog);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        moveDialog.show();
    }

    public void movedPack() {
        try {
            pack = dbHelperGet.getSinglePack(packNo);
            collectionNo = pack.collection;
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
