package de.herrmann_engel.rbv;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NewCollection extends AppCompatActivity {

    TextView nameTextView;
    TextView descTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_collection_or_pack);

        nameTextView = findViewById(R.id.new_collection_or_pack_name);
        nameTextView.setHint(String.format(getString(R.string.collection_or_pack_name_format),
                getString(R.string.collection_name), getString(R.string.collection_or_pack_name)));
        descTextView = findViewById(R.id.new_collection_or_pack_desc);
        descTextView.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc)));
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
            dbHelperCreate.createCollection(name, desc);
            startListCollections();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error_values, Toast.LENGTH_SHORT).show();
        }
    }

    private void startListCollections() {
        Intent intent = new Intent(getApplicationContext(), ListCollections.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        String name = nameTextView.getText().toString();
        String desc = descTextView.getText().toString();
        if (name.isEmpty() && desc.isEmpty()) {
            startListCollections();
        } else {
            Dialog confirmCancel = new Dialog(this, R.style.dia_view);
            confirmCancel.setContentView(R.layout.dia_confirm);
            confirmCancel.setTitle(getResources().getString(R.string.discard_changes));
            confirmCancel.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);

            Button confirmCancelY = confirmCancel.findViewById(R.id.dia_confirm_yes);
            Button confirmCancelN = confirmCancel.findViewById(R.id.dia_confirm_no);
            confirmCancelY.setOnClickListener(v -> startListCollections());
            confirmCancelN.setOnClickListener(v -> confirmCancel.dismiss());
            confirmCancel.show();
        }
    }
}
