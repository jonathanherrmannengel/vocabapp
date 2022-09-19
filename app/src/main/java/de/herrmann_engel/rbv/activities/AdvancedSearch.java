package de.herrmann_engel.rbv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterPacksAdvancedSearch;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class AdvancedSearch extends AppCompatActivity {
    RecyclerView recyclerView;
    int pack = -3;
    ArrayList<Integer> packList = new ArrayList<>();
    boolean progressGreater = false;
    int progressNumber = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search);

        if (getIntent().getExtras() != null) {
            pack = getIntent().getExtras().getInt("pack");
            if (getIntent().getExtras().getIntegerArrayList("packs") != null) {
                packList = getIntent().getExtras().getIntegerArrayList("packs");
            }
            progressGreater = getIntent().getExtras().getBoolean("progressGreater", false);
            progressNumber = getIntent().getExtras().getInt("progressNumber", -1);
        }

        recyclerView = findViewById(R.id.rec_advanced_search);
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        List<DB_Pack> packs = dbHelperGet.getAllPacks();
        AdapterPacksAdvancedSearch adapter = new AdapterPacksAdvancedSearch(packs, this, packList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (pack == -3) {
            recyclerView.setVisibility(View.GONE);
        }

        RadioButton allPacksButton = findViewById(R.id.advanced_search_packs_all);
        allPacksButton.setChecked(pack == -3);
        allPacksButton.setOnClickListener(v -> {
            pack = -3;
            recyclerView.setVisibility(View.GONE);
        });

        RadioButton selectPacksButton = findViewById(R.id.advanced_search_packs_select);
        selectPacksButton.setChecked(pack == -2);
        selectPacksButton.setOnClickListener(v -> {
            pack = -2;
            recyclerView.setVisibility(View.VISIBLE);
        });

        RadioButton progressGreaterButton = findViewById(R.id.advanced_search_progress_greater);
        progressGreaterButton.setChecked(progressGreater);
        progressGreaterButton.setOnClickListener(v -> progressGreater = true);

        RadioButton progressLessButton = findViewById(R.id.advanced_search_progress_less);
        progressLessButton.setChecked(!progressGreater);
        progressLessButton.setOnClickListener(v -> progressGreater = false);

        EditText progressValueInput = findViewById(R.id.advanced_search_progress_value);
        if (progressNumber >= 0) {
            progressValueInput.setText(Integer.valueOf(progressNumber).toString());
        }

        Button go = findViewById(R.id.advanced_search_go);
        go.setOnClickListener(v -> {
            String progressValueInputTemp = progressValueInput.getText().toString();
            if (progressValueInputTemp.isEmpty()) {
                progressNumber = -1;
            } else {
                int progressNumberTemp = Integer.parseInt(progressValueInputTemp);
                if (progressNumberTemp >= 0) {
                    progressNumber = progressNumberTemp;
                }
            }
            Intent intent = new Intent(getApplicationContext(), ListCards.class);
            intent.putExtra("pack", pack);
            intent.putExtra("collection", -1);
            if (pack == -2) {
                intent.putIntegerArrayListExtra("packs", packList);
            }
            intent.putExtra("progressGreater", progressGreater);
            intent.putExtra("progressNumber", progressNumber);
            startActivity(intent);
            this.finish();
        });
    }

    public void addToPackList(int i) {
        if (!packList.contains(i)) {
            packList.add(i);
        }
    }

    public void removeFromPackList(int i) {
        if (packList.contains(i)) {
            packList.remove(Integer.valueOf(i));
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}
