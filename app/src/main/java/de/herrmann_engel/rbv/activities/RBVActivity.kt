package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.DiaInfoBinding
import de.herrmann_engel.rbv.export_import.ImportExportEvents
import de.herrmann_engel.rbv.settings.AppSettings
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
            val currentNightMode = (resources.configuration.uiMode
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
        AppSettings(this).applyUiMode()
    }

    // Draws the screen edge-to-edge (behind the system bars) and pads the given view, so that its
    // content stays clear of the system bars
    protected fun setUpEdgeToEdge(paddedView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(paddedView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        enableEdgeToEdge()
    }

    // Called after a successful import, e.g. to reload displayed data
    protected open fun onImportFinished() {}

    // Shows a dialog and returns true if an import or export is already running (only one at a time)
    protected fun showIfImportExportRunning(): Boolean {
        if (ImportExportEvents.isRunning) {
            showImportExportRunningDialog()
            return true
        }
        return false
    }

    // Shows whether an import or export was started (execute() returns false if one is already running)
    protected fun showImportExportStarted(started: Boolean) {
        if (started) {
            Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show()
        } else {
            showImportExportRunningDialog()
        }
    }

    // Shown as a dialog: the user explicitly tried to start something, so the explanation has to
    // stay visible until it has been read
    private fun showImportExportRunningDialog() {
        val infoDialog = Dialog(this, R.style.dia_view)
        val bindingInfoDialog = DiaInfoBinding.inflate(layoutInflater)
        infoDialog.setContentView(bindingInfoDialog.root)
        infoDialog.setTitle(getString(R.string.info))
        infoDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        bindingInfoDialog.diaInfoText.setText(R.string.import_export_running)
        infoDialog.show()
    }

}
