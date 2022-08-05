package de.herrmann_engel.rbv;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NewPack extends AppCompatActivity {

    private int collectionNo;

    TextView nameTextView;
    TextView descTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_collection_or_pack);

        collectionNo = getIntent().getExtras().getInt("collection");

        nameTextView = findViewById(R.id.new_collection_or_pack_name);
        descTextView = findViewById(R.id.new_collection_or_pack_desc);
        descTextView.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc)));

        TextView addTextView = findViewById(R.id.new_collection_or_pack_go);
        addTextView.setOnClickListener(v -> {
            String name = nameTextView.getText().toString();
            String desc = descTextView.getText().toString();
            try {
                DB_Helper_Create dbHelperCreate = new DB_Helper_Create(getApplicationContext());
                dbHelperCreate.createPack(name, desc, collectionNo);
                startListPacks();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.error_values, Toast.LENGTH_SHORT).show();
            }
        });

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
            confirmCancelY.setOnClickListener(v -> {
                startListPacks();
            });
            confirmCancelN.setOnClickListener(v -> confirmCancel.dismiss());
            confirmCancel.show();
        }
    }
}
