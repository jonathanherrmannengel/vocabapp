package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.Intent;
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
import de.herrmann_engel.rbv.databinding.ActivityNewCardBinding;
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;

public class NewCard extends AppCompatActivity {

    private ActivityNewCardBinding binding;
    private int packNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        packNo = getIntent().getExtras().getInt("pack");
        TypedArray colorsStatusBar = getResources().obtainTypedArray(R.array.pack_color_statusbar);
        TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        try {
            int packColors = dbHelperGet.getSinglePack(packNo).colors;
            if (packColors < Math.min(colorsStatusBar.length(), colorsBackground.length()) && packColors >= 0) {
                int colorStatusBar = colorsStatusBar.getColor(packColors, 0);
                int colorBackground = colorsBackground.getColor(packColors, 0);
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(colorStatusBar));
                Window window = this.getWindow();
                window.setStatusBarColor(colorStatusBar);
                binding.getRoot().setBackgroundColor(colorBackground);
            }
            colorsStatusBar.recycle();
            colorsBackground.recycle();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }

        binding.newCardNotes.setHint(String.format(getString(R.string.optional), getString(R.string.card_notes)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    public void insert(MenuItem menuItem) {
        String front = binding.newCardFront.getText().toString();
        String back = binding.newCardBack.getText().toString();
        String notes = binding.newCardNotes.getText().toString();
        try {
            DB_Helper_Create dbHelperCreate = new DB_Helper_Create(this);
            long cardNo = dbHelperCreate.createCard(front, back, notes, packNo);
            Intent intent = new Intent(this, ListCards.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("cardAdded", (int) cardNo);
            this.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        String front = binding.newCardFront.getText().toString();
        String back = binding.newCardBack.getText().toString();
        String notes = binding.newCardNotes.getText().toString();
        if (front.isEmpty() && back.isEmpty() && notes.isEmpty()) {
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
