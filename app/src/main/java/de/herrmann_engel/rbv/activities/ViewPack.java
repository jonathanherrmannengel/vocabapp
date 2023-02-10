package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import de.herrmann_engel.rbv.adapters.AdapterCollectionsMovePack;
import de.herrmann_engel.rbv.databinding.ActivityViewCollectionOrPackBinding;
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding;
import de.herrmann_engel.rbv.databinding.DiaRecBinding;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class ViewPack extends AppCompatActivity {
    private DB_Helper_Get dbHelperGet;
    private DB_Pack pack;
    private int packNo;
    private int collectionNo;
    private boolean reverse;
    private int sort;
    private String searchQuery;
    private int cardPosition;
    private ArrayList<Integer> savedList;
    private Long savedListSeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityViewCollectionOrPackBinding binding = ActivityViewCollectionOrPackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        searchQuery = getIntent().getExtras().getString("searchQuery");
        cardPosition = getIntent().getExtras().getInt("cardPosition");
        savedList = getIntent().getExtras().getIntegerArrayList("savedList");
        savedListSeed = getIntent().getExtras().getLong("savedListSeed");
        dbHelperGet = new DB_Helper_Get(this);
        boolean increaseFontSize = settings.getBoolean("ui_font_size", false);
        try {
            pack = dbHelperGet.getSinglePack(packNo);
            setTitle(pack.name);
            binding.collectionOrPackName.setText(pack.name);
            if (pack.desc.equals("")) {
                binding.collectionOrPackDesc.setVisibility(View.GONE);
            } else {
                binding.collectionOrPackDesc.setText(pack.desc);
            }
            if (increaseFontSize) {
                binding.collectionOrPackName.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.details_name_size_big));
                binding.collectionOrPackDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimension(R.dimen.details_desc_size_big));
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Instant instant = Instant.ofEpochSecond(pack.date);
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        .withLocale(Locale.ROOT)
                        .withZone(ZoneId.systemDefault());
                binding.collectionOrPackDate.setText(dateTimeFormatter.format(instant));
            } else {
                binding.collectionOrPackDate.setText(new Date(pack.date * 1000).toString());
            }
            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            if (pack.colors < Math.min(colors.length(), colorsBackground.length()) && pack.colors >= 0) {
                int color = colors.getColor(pack.colors, 0);
                int colorBackground = colorsBackground.getColor(pack.colors, 0);
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                Window window = this.getWindow();
                window.setStatusBarColor(color);
                binding.getRoot().setBackgroundColor(colorBackground);
            }
            colors.recycle();
            colorsBackground.recycle();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(this, EditPack.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", cardPosition);
        intent.putIntegerArrayListExtra("savedList", savedList);
        intent.putExtra("savedListSeed", savedListSeed);
        startActivity(intent);
        this.finish();
    }

    public void deletePack(boolean forceDelete) {
        Dialog confirmDeleteDialog = new Dialog(this, R.style.dia_view);
        DiaConfirmBinding bindingConfirmDeleteDialog = DiaConfirmBinding.inflate(getLayoutInflater());
        confirmDeleteDialog.setContentView(bindingConfirmDeleteDialog.getRoot());
        confirmDeleteDialog.setTitle(getResources().getString(R.string.delete));
        confirmDeleteDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        if (dbHelperGet.countCardsInPack(pack.uid) > 0 && !forceDelete) {
            bindingConfirmDeleteDialog.diaConfirmDesc.setText(R.string.delete_pack_with_cards);
            bindingConfirmDeleteDialog.diaConfirmDesc.setVisibility(View.VISIBLE);
        }
        bindingConfirmDeleteDialog.diaConfirmYes.setOnClickListener(v -> {
            if (dbHelperGet.countCardsInPack(pack.uid) == 0 || forceDelete) {
                DB_Helper_Delete dbHelperDelete = new DB_Helper_Delete(this);
                dbHelperDelete.deletePack(pack, forceDelete);
                Intent intent = new Intent(this, ListPacks.class);
                intent.putExtra("collection", collectionNo);
                startActivity(intent);
                this.finish();
            } else {
                deletePack(true);
                confirmDeleteDialog.dismiss();
            }
        });
        bindingConfirmDeleteDialog.diaConfirmNo.setOnClickListener(v -> confirmDeleteDialog.dismiss());
        confirmDeleteDialog.show();
    }

    public void deletePack(MenuItem menuItem) {
        deletePack(false);
    }

    public void movePack(MenuItem menuItem) {
        Dialog moveDialog = new Dialog(this, R.style.dia_view);
        DiaRecBinding bindingMoveDialog = DiaRecBinding.inflate(getLayoutInflater());
        moveDialog.setContentView(bindingMoveDialog.getRoot());
        moveDialog.setTitle(getResources().getString(R.string.move_pack));
        moveDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        List<DB_Collection> collections = dbHelperGet.getAllCollections();
        AdapterCollectionsMovePack adapter = new AdapterCollectionsMovePack(collections, pack, moveDialog);
        bindingMoveDialog.diaRec.setAdapter(adapter);
        bindingMoveDialog.diaRec.setLayoutManager(new LinearLayoutManager(this));

        moveDialog.show();
    }

    public void movedPack() {
        try {
            pack = dbHelperGet.getSinglePack(packNo);
            collectionNo = pack.collection;
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ListCards.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", cardPosition);
        intent.putIntegerArrayListExtra("savedList", savedList);
        intent.putExtra("savedListSeed", savedListSeed);
        startActivity(intent);
        this.finish();
    }
}
