package de.herrmann_engel.rbv;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ViewCollection extends AppCompatActivity {

    private DB_Helper_Get dbHelperGet;
    private DB_Collection collection;
    private int collectionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_collection_or_pack);

        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        collectionNo = getIntent().getExtras().getInt("collection");
        dbHelperGet = new DB_Helper_Get(this);
        boolean increaseFontSize = settings.getBoolean("ui_font_size", false);
        try {
            collection = dbHelperGet.getSingleCollection(collectionNo);
            setTitle(collection.name);
            TextView nameTextView = findViewById(R.id.collection_or_pack_name);
            nameTextView.setText(collection.name);
            TextView descTextView = findViewById(R.id.collection_or_pack_desc);
            if (collection.desc.equals("")) {
                descTextView.setVisibility(View.GONE);
            } else {
                descTextView.setText(collection.desc);
            }
            if(increaseFontSize){
                nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.details_name_size_big));
                descTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.details_desc_size_big));
            }
            TextView dateTextView = findViewById(R.id.collection_or_pack_date);
            dateTextView.setText(new java.util.Date(collection.date * 1000).toString());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_collection, menu);
        return true;
    }

    public void editCollection(MenuItem menuItem) {
        Intent intent = new Intent(getApplicationContext(), EditCollection.class);
        intent.putExtra("collection", collectionNo);
        startActivity(intent);
        this.finish();
    }

    public void deleteCollection(boolean forceDelete) {
        Dialog confirmDelete = new Dialog(this, R.style.dia_view);
        confirmDelete.setContentView(R.layout.dia_confirm);
        confirmDelete.setTitle(getResources().getString(R.string.delete));
        confirmDelete.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        Button confirmDeleteY = confirmDelete.findViewById(R.id.dia_confirm_yes);
        Button confirmDeleteN = confirmDelete.findViewById(R.id.dia_confirm_no);
        if (dbHelperGet.getAllPacksByCollection(collection.uid).size() > 0 && !forceDelete) {
            TextView confirmDeleteDesc = confirmDelete.findViewById(R.id.dia_confirm_desc);
            confirmDeleteDesc.setText(R.string.delete_collection_with_packs);
            confirmDeleteDesc.setVisibility(View.VISIBLE);
        }
        confirmDeleteY.setOnClickListener(v -> {
            if (dbHelperGet.getAllPacksByCollection(collection.uid).size() == 0 || forceDelete) {
                DB_Helper_Delete dbHelperDelete = new DB_Helper_Delete(getApplicationContext());
                dbHelperDelete.deleteCollection(collection, forceDelete);
                Intent intent = new Intent(getApplicationContext(), ListCollections.class);
                startActivity(intent);
                this.finish();
            } else {
                deleteCollection(true);
                confirmDelete.dismiss();
            }
        });
        confirmDeleteN.setOnClickListener(v -> confirmDelete.dismiss());
        confirmDelete.show();
    }

    public void deleteCollection(MenuItem menuItem) {
        deleteCollection(false);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ListPacks.class);
        intent.putExtra("collection", collectionNo);
        startActivity(intent);
        this.finish();
    }
}
