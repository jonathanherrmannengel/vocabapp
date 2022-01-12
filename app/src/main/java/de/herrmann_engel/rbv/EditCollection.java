package de.herrmann_engel.rbv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditCollection extends AppCompatActivity {

    private int collectionNo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_collection);
        TextView collectionEdit = findViewById(R.id.edit_collection_go);
        TextView collectionName = findViewById(R.id.edit_collection_name);
        TextView collectionDesc = findViewById(R.id.edit_collection_desc);
        collectionNo = getIntent().getExtras().getInt("collection");
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        DB_Helper_Update dbHelperUpdate = new DB_Helper_Update(this);
        try {
            DB_Collection collection = dbHelperGet.getSingleCollection(collectionNo);
            collectionName.setText(collection.name);
            collectionDesc.setText(collection.desc);
            collectionEdit.setOnClickListener(v -> {
                collection.name = collectionName.getText().toString();
                collection.desc = collectionDesc.getText().toString();
                if(dbHelperUpdate.updateCollection(collection)) {
                    Intent intent = new Intent(getApplicationContext(), ViewCollection.class);
                    intent.putExtra("collection", collectionNo);
                    startActivity(intent);
                    this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ViewCollection.class);
        intent.putExtra("collection", collectionNo);
        startActivity(intent);
        this.finish();
    }
}