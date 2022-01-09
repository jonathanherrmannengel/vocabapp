package de.herrmann_engel.rbv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NewPack extends AppCompatActivity {

    private int collectionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_collection_or_pack);

        collectionNo = getIntent().getExtras().getInt("collection");

        TextView addTextView = findViewById(R.id.new_collection_or_pack_go);
        addTextView.setOnClickListener(v -> {
            TextView nameTextView = findViewById(R.id.new_collection_or_pack_name);
            TextView descTextView = findViewById(R.id.new_collection_or_pack_desc);
            String name = nameTextView.getText().toString();
            String desc = descTextView.getText().toString();
            try {
                DB_Helper_Create dbHelperCreate = new DB_Helper_Create(getApplicationContext());
                dbHelperCreate.createPack(name, desc, collectionNo);
                Intent intent = new Intent(getApplicationContext(), ListPacks.class);
                intent.putExtra("collection", collectionNo);
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
        Intent intent = new Intent(getApplicationContext(), ListPacks.class);
        intent.putExtra("collection", collectionNo);
        startActivity(intent);
        this.finish();
    }
}
