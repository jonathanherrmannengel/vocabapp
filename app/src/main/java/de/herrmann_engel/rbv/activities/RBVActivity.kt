package de.herrmann_engel.rbv.activities

import android.content.res.Configuration
import android.os.Build
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.appcompat.app.AppCompatActivity

abstract class RBVActivity : AppCompatActivity() {


    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 35) {
            val currentNightMode = (getResources().configuration.uiMode
                    and Configuration.UI_MODE_NIGHT_MASK)

            window?.insetsController?.setSystemBarsAppearance(
                if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
                    APPEARANCE_LIGHT_STATUS_BARS
                } else {
                    0
                }, APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }

}
