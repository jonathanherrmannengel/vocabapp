package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
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

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class NewPack extends AppCompatActivity {

    TextView nameTextView;
    TextView descTextView;
    private int collectionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_collection_or_pack);

        collectionNo = getIntent().getExtras().getInt("collection");

        nameTextView = findViewById(R.id.new_collection_or_pack_name);
        TextInputLayout nameTextViewLayout = findViewById(R.id.new_collection_or_pack_name_layout);
        nameTextViewLayout.setHint(String.format(getString(R.string.collection_or_pack_name_format),
                getString(R.string.pack_name), getString(R.string.collection_or_pack_name)));
        descTextView = findViewById(R.id.new_collection_or_pack_desc);

        TextInputLayout descTextViewLayout = findViewById(R.id.new_collection_or_pack_desc_layout);
        descTextViewLayout.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc)));

        TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
        TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        try {
            int packColors = dbHelperGet.getSingleCollection(collectionNo).colors;
            if (packColors < Math.min(colors.length(), colorsBackground.length()) && packColors >= 0) {
                int color = colors.getColor(packColors, 0);
                int colorBackground = colorsBackground.getColor(packColors, 0);
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                Window window = this.getWindow();
                window.setStatusBarColor(color);
                nameTextViewLayout.setBoxStrokeColor(color);
                nameTextViewLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.light_black, getTheme())));
                descTextViewLayout.setBoxStrokeColor(color);
                descTextViewLayout.setHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.light_black, getTheme())));
                findViewById(R.id.root_new_collection_or_pack).setBackgroundColor(colorBackground);
            }
            colors.recycle();
            colorsBackground.recycle();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    public void insert(MenuItem menuItem) {
        String name = nameTextView.getText().toString();
        String desc = descTextView.getText().toString();
        try {
            DB_Helper_Create dbHelperCreate = new DB_Helper_Create(getApplicationContext());
            dbHelperCreate.createPack(name, desc, collectionNo);
            startListPacks();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error_values, Toast.LENGTH_SHORT).show();
        }
    }

    private void startListPacks() {
        Intent intent = new Intent(getApplicationContext(), ListPacks.class);
        intent.putExtra("collection", collectionNo);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        String name = nameTextView.getText().toString();
        String desc = descTextView.getText().toString();
        if (name.isEmpty() && desc.isEmpty()) {
            startListPacks();
        } else {
            Dialog confirmCancel = new Dialog(this, R.style.dia_view);
            confirmCancel.setContentView(R.layout.dia_confirm);
            confirmCancel.setTitle(getResources().getString(R.string.discard_changes));
            confirmCancel.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);

            Button confirmCancelY = confirmCancel.findViewById(R.id.dia_confirm_yes);
            Button confirmCancelN = confirmCancel.findViewById(R.id.dia_confirm_no);
            confirmCancelY.setOnClickListener(v -> startListPacks());
            confirmCancelN.setOnClickListener(v -> confirmCancel.dismiss());
            confirmCancel.show();
        }
    }
}
