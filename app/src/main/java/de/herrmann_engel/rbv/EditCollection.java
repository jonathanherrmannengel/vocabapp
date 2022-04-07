package de.herrmann_engel.rbv;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditCollection extends AppCompatActivity {

    private int collectionNo;

    DB_Collection collection;
    TextView collectionName;
    TextView collectionDesc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_collection);
        TextView collectionEdit = findViewById(R.id.edit_collection_go);
        collectionName = findViewById(R.id.edit_collection_name);
        collectionDesc = findViewById(R.id.edit_collection_desc);
        collectionDesc.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc)));
        collectionNo = getIntent().getExtras().getInt("collection");
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        DB_Helper_Update dbHelperUpdate = new DB_Helper_Update(this);
        try {
            collection = dbHelperGet.getSingleCollection(collectionNo);
            collectionName.setText(collection.name);
            collectionDesc.setText(collection.desc);
            collectionEdit.setOnClickListener(v -> {
                collection.name = collectionName.getText().toString();
                collection.desc = collectionDesc.getText().toString();
                if(dbHelperUpdate.updateCollection(collection)) {
                    startViewCollection();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error_values, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void startViewCollection(){
        Intent intent = new Intent(getApplicationContext(), ViewCollection.class);
        intent.putExtra("collection", collectionNo);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        String name = collectionName.getText().toString();
        String desc = collectionDesc.getText().toString();
        if(collection == null || (collection.name.equals(name) && collection.desc.equals(desc))) {
            startViewCollection();
        } else {
            Dialog confirmCancel = new Dialog(this, R.style.dia_view);
            confirmCancel.setContentView(R.layout.dia_confirm);
            confirmCancel.setTitle(getResources().getString(R.string.discard_changes));
            confirmCancel.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            Button confirmCancelY = confirmCancel.findViewById(R.id.dia_confirm_yes);
            Button confirmCancelN = confirmCancel.findViewById(R.id.dia_confirm_no);
            confirmCancelY.setOnClickListener(v -> startViewCollection());
            confirmCancelN.setOnClickListener(v -> confirmCancel.dismiss());
            confirmCancel.show();
        }
    }
}