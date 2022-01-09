package de.herrmann_engel.rbv;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class EditPack extends AppCompatActivity {

    private int collectionNo;
    private int packNo;
    private boolean reverse;
    private int sort;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pack);
        TextView packEdit = findViewById(R.id.edit_pack_go);
        TextView packName = findViewById(R.id.edit_pack_name);
        TextView packDesc = findViewById(R.id.edit_pack_desc);
        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort");
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(this);
        DB_Helper_Update dbHelperUpdate = new DB_Helper_Update(this);
        try {
            DB_Pack pack = dbHelperGet.getSinglePack(packNo);
            packName.setText(pack.name);
            packDesc.setText(pack.desc);
            packEdit.setOnClickListener(v -> {
                pack.name = packName.getText().toString();
                pack.desc = packDesc.getText().toString();
                if(dbHelperUpdate.updatePack(pack)) {
                    Intent intent = new Intent(getApplicationContext(), ViewPack.class);
                    intent.putExtra("collection", collectionNo);
                    intent.putExtra("pack", packNo);
                    intent.putExtra("reverse", reverse);
                    intent.putExtra("sort", sort);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
                }
            });
            LinearLayout colorPicker = findViewById(R.id.color_picker);
            TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
            TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
            for(int i = 0; i < colors.length() && i < colorsBackground.length(); i++) {
                int color = colors.getColor(i,0);
                int colorBackground = colorsBackground.getColor(i,0);
                if(pack.colors == i) {
                    setColors(color,colorBackground);
                }
                ImageButton colorView = new ImageButton(this);
                colorView.setImageDrawable(new ColorDrawable(color));
                int margin = Math.round(10 * ((float) getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(margin*3,margin*3);
                lp.setMargins(margin,margin,margin,margin);
                colorView.setLayoutParams(lp);
                colorView.setPadding(0,0,0,0);
                int finalI = i;
                colorView.setOnClickListener(
                        v -> {
                            setColors(color,colorBackground);
                            pack.colors = finalI;
                        }
                );
                colorPicker.addView(colorView);
            }
            colors.recycle();
            colorsBackground.recycle();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setColors(int main, int background) {
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(main));
        Window window = this.getWindow();
        window.setStatusBarColor(main);
        findViewById(R.id.root_edit_pack).setBackgroundColor(background);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), ViewPack.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        startActivity(intent);
        this.finish();
    }
}