package de.herrmann_engel.rbv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NewCollection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_collection_or_pack);

        TextView addTextView = findViewById(R.id.new_collection_or_pack_go);
        addTextView.setOnClickListener(v -> {
            TextView nameTextView = findViewById(R.id.new_collection_or_pack_name);
            TextView descTextView = findViewById(R.id.new_collection_or_pack_desc);
            String name = nameTextView.getText().toString();
            String desc = descTextView.getText().toString();
            try {
                DB_Helper_Create dbHelperCreate = new DB_Helper_Create(getApplicationContext());
                dbHelperCreate.createCollection(name, desc);
                Intent intent = new Intent(getApplicationContext(), ListCollections.class);
                startActivity(intent);
                this.finish();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }
}
