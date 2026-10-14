package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.export_import.ImportExportEvents
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * Base class of all activities.
 *
 * While an activity is in the foreground, it shows the progress and results of running imports
 * and exports (see [ImportExportEvents]), no matter which screen started them.
 */
abstract class RBVActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    ImportExportEvents.progress.collect { progress ->
                        Toast.makeText(this@RBVActivity, progress, Toast.LENGTH_SHORT).show()
                    }
                }
                ImportExportEvents.result.filterNotNull().collect { result ->
                    if (ImportExportEvents.consumeResult(result)) {
                        when (result) {
                            is ImportExportEvents.Result.ExportFinished -> exportFinished(result)
                            is ImportExportEvents.Result.ImportFinished -> importFinished(result)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Edge-to-edge is enforced on Android 15+: use dark status bar icons in day mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
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

    // Offers to share the exported file
    private fun exportFinished(result: ImportExportEvents.Result.ExportFinished) {
        val file = result.file
        if (file == null) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
            return
        }
        val share = Intent(Intent.ACTION_SEND)
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        share.type = Globals.EXPORT_FILE_TYPE
        share.putExtra(
            Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider", file
            )
        )
        startActivity(
            Intent.createChooser(
                share,
                getString(R.string.export_cards)
            )
        )
    }

    private fun importFinished(result: ImportExportEvents.Result.ImportFinished) {
        if (result.errorLevel >= Globals.IMPORT_ERROR_LEVEL_ERROR) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
            return
        }
        if (result.errorLevel == Globals.IMPORT_ERROR_LEVEL_OKAY) {
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.import_warn, Toast.LENGTH_LONG).show()
        }
        onImportFinished()
        // Apply the theme setting, which may have been changed by the import
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        when (settings.getInt("ui_mode", Globals.UI_MODE_DAY)) {
            Globals.UI_MODE_NIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            Globals.UI_MODE_DAY -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    // Called after a successful import, e.g. to reload displayed data
    protected open fun onImportFinished() {}

}
