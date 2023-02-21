package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.databinding.ActivityEditCardBinding;
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding;
import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update;

public class EditCard extends AppCompatActivity {
    private ActivityEditCardBinding binding;
    private DB_Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditCardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int cardNo = getIntent().getExtras().getInt("card");
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        try {
            card = dbHelperGet.getSingleCard(cardNo);
            binding.editCardFront.setText(card.front);
            if (card.front.contains(System.getProperty("line.separator"))) {
                binding.editCardFront.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                binding.editCardFront.setSingleLine(false);
            }
            binding.editCardBack.setText(card.back);
            if (card.back.contains(System.getProperty("line.separator"))) {
                binding.editCardBack.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                binding.editCardBack.setSingleLine(false);
            }
            binding.editCardNotes.setText(card.notes);
            binding.editCardNotes.setHint(String.format(getString(R.string.optional), getString(R.string.card_notes)));

            TypedArray colorsStatusBar = getResources().obtainTypedArray(R.array.pack_color_statusbar);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            int packColors = dbHelperGet.getSinglePack(card.pack).colors;
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    public void saveChanges(MenuItem menuItem) {
        card.front = binding.editCardFront.getText().toString();
        card.back = binding.editCardBack.getText().toString();
        card.notes = binding.editCardNotes.getText().toString();
        DB_Helper_Update dbHelperUpdate = new DB_Helper_Update(this);
        if (dbHelperUpdate.updateCard(card)) {
            this.finish();
        } else {
            Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        String front = binding.editCardFront.getText().toString();
        String back = binding.editCardBack.getText().toString();
        String notes = binding.editCardNotes.getText().toString();
        if (card == null || (card.front.equals(front) && card.back.equals(back) && card.notes.equals(notes))) {
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
