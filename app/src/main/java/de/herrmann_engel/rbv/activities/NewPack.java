package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.databinding.ActivityNewCollectionOrPackBinding;
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class NewPack extends AppCompatActivity {

    private ActivityNewCollectionOrPackBinding binding;
    private int collectionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewCollectionOrPackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        collectionNo = getIntent().getExtras().getInt("collection");

        binding.newCollectionOrPackNameLayout.setHint(String.format(getString(R.string.collection_or_pack_name_format),
                getString(R.string.pack_name), getString(R.string.collection_or_pack_name)));
        binding.newCollectionOrPackDescLayout.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc)));

        TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
        TypedArray colorsStatusBar = getResources().obtainTypedArray(R.array.pack_color_statusbar);
        TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        try {
            int packColors = dbHelperGet.getSingleCollection(collectionNo).colors;
            if (packColors < colors.length() && packColors < colorsStatusBar.length() && packColors < colorsBackground.length() && packColors >= 0) {
                int color = colors.getColor(packColors, 0);
                int colorStatusBar = colorsStatusBar.getColor(packColors, 0);
                int colorBackground = colorsBackground.getColor(packColors, 0);
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(colorStatusBar));
                Window window = this.getWindow();
                window.setStatusBarColor(colorStatusBar);
                binding.newCollectionOrPackNameLayout.setBoxStrokeColor(color);
                binding.newCollectionOrPackNameLayout.setHintTextColor(ColorStateList.valueOf(color));
                binding.newCollectionOrPackDescLayout.setBoxStrokeColor(color);
                binding.newCollectionOrPackDescLayout.setHintTextColor(ColorStateList.valueOf(color));
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
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    public void insert(MenuItem menuItem) {
        String name = binding.newCollectionOrPackName.getText().toString();
        String desc = binding.newCollectionOrPackDesc.getText().toString();
        try {
            DB_Helper_Create dbHelperCreate = new DB_Helper_Create(this);
            dbHelperCreate.createPack(name, desc, collectionNo);
            this.finish();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        String name = binding.newCollectionOrPackName.getText().toString();
        String desc = binding.newCollectionOrPackDesc.getText().toString();
        if (name.isEmpty() && desc.isEmpty()) {
            super.onBackPressed();
        } else {
            Dialog confirmCancelDialog = new Dialog(this, R.style.dia_view);
            DiaConfirmBinding bindingConfirmCancelDialog = DiaConfirmBinding.inflate(getLayoutInflater());
            confirmCancelDialog.setContentView(bindingConfirmCancelDialog.getRoot());
            confirmCancelDialog.setTitle(getResources().getString(R.string.discard_changes));
            confirmCancelDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            bindingConfirmCancelDialog.diaConfirmYes.setOnClickListener(v -> super.onBackPressed());
            bindingConfirmCancelDialog.diaConfirmNo.setOnClickListener(v -> confirmCancelDialog.dismiss());
            confirmCancelDialog.show();
        }
    }
}
