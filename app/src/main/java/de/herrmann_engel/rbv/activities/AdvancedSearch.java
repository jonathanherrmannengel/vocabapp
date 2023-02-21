package de.herrmann_engel.rbv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import de.herrmann_engel.rbv.adapters.AdapterPacksAdvancedSearch;
import de.herrmann_engel.rbv.databinding.ActivityAdvancedSearchBinding;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class AdvancedSearch extends AppCompatActivity {
    private final ArrayList<Integer> packList = new ArrayList<>();
    private ActivityAdvancedSearchBinding binding;
    private int pack = -3;
    private boolean progressGreater = false;
    private int progressNumber = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdvancedSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        List<DB_Pack> packs = dbHelperGet.getAllPacks();
        AdapterPacksAdvancedSearch adapter = new AdapterPacksAdvancedSearch(packs, packList);
        binding.recAdvancedSearch.setAdapter(adapter);
        binding.recAdvancedSearch.setLayoutManager(new LinearLayoutManager(this));
        if (pack == -3) {
            binding.recAdvancedSearch.setVisibility(View.GONE);
        }

        binding.advancedSearchPacksAll.setChecked(pack == -3);
        binding.advancedSearchPacksAll.setOnClickListener(v -> {
            pack = -3;
            binding.recAdvancedSearch.setVisibility(View.GONE);
        });

        binding.advancedSearchPacksSelect.setChecked(pack == -2);
        binding.advancedSearchPacksSelect.setOnClickListener(v -> {
            pack = -2;
            binding.recAdvancedSearch.setVisibility(View.VISIBLE);
        });

        binding.advancedSearchProgressGreater.setChecked(progressGreater);
        binding.advancedSearchProgressGreater.setOnClickListener(v -> progressGreater = true);

        binding.advancedSearchProgressLess.setChecked(!progressGreater);
        binding.advancedSearchProgressLess.setOnClickListener(v -> progressGreater = false);

        if (progressNumber >= 0) {
            binding.advancedSearchProgressValue.setText(Integer.valueOf(progressNumber).toString());
        }

        binding.advancedSearchGo.setOnClickListener(v -> {
            String progressValueInputTemp = binding.advancedSearchProgressValue.getText().toString();
            if (progressValueInputTemp.isEmpty()) {
                progressNumber = -1;
            } else {
                int progressNumberTemp = Integer.parseInt(progressValueInputTemp);
                if (progressNumberTemp >= 0) {
                    progressNumber = progressNumberTemp;
                }
            }
            Intent intent = new Intent(this, ListCards.class);
            intent.putExtra("collection", -1);
            intent.putExtra("pack", pack);
            if (pack == -2) {
                intent.putIntegerArrayListExtra("packs", packList);
            }
            intent.putExtra("progressGreater", progressGreater);
            intent.putExtra("progressNumber", progressNumber);
            startActivity(intent);
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
}
