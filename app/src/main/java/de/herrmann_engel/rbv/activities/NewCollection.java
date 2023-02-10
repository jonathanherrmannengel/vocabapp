package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.databinding.ActivityNewCollectionOrPackBinding;
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create;

public class NewCollection extends AppCompatActivity {

    private ActivityNewCollectionOrPackBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewCollectionOrPackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.newCollectionOrPackNameLayout.setHint(String.format(getString(R.string.collection_or_pack_name_format),
                getString(R.string.collection_name), getString(R.string.collection_or_pack_name)));
        binding.newCollectionOrPackDescLayout.setHint(String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc)));
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
            dbHelperCreate.createCollection(name, desc);
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
