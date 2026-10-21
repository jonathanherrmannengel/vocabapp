package de.herrmann_engel.rbv.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.WorkInfo
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.export_import.ImportExportWork
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.showInfoDialog
import de.herrmann_engel.rbv.utils.showMessageDialog
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.io.File

/**
 * Base class of all activities.
 *
 * While an activity is in the foreground, it shows the results of imports and exports (see
 * [ImportExportWork]), no matter which screen started them.
 */
abstract class RBVActivity : AppCompatActivity() {
    // The answer does not matter: imports and exports run with or without notification
    private val notificationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ImportExportWork.observe(this@RBVActivity).filterNotNull().collect { work ->
                    showImportExportWork(work)
                }
            }
        }
    }

    // Shows the result of a finished import or export (once); the progress is shown by the
    // notification of the running work
    private fun showImportExportWork(work: WorkInfo) {
        if (!work.state.isFinished || !ImportExportWork.consumeResult(this, work.id)) {
            return
        }
        // Failed or cancelled work has no output: shown as error
        val output = work.outputData
        if (ImportExportWork.TAG_EXPORT in work.tags) {
            exportFinished(output.getString(ImportExportWork.KEY_FILE)?.let { File(it) })
        } else {
            importFinished(
                output.getInt(ImportExportWork.KEY_ERROR_LEVEL, Globals.IMPORT_ERROR_LEVEL_ERROR)
            )
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

    // Offers to share the exported file (null if the export failed)
    private fun exportFinished(file: File?) {
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

    // errorLevel: one of the IMPORT_ERROR_LEVEL_* constants
    private fun importFinished(errorLevel: Int) {
        when {
            errorLevel >= Globals.IMPORT_ERROR_LEVEL_ERROR ->
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()

            errorLevel == Globals.IMPORT_ERROR_LEVEL_OKAY ->
                Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show()

            else -> Toast.makeText(this, R.string.import_warn, Toast.LENGTH_LONG).show()
        }
        // Also after a failed import: it may have removed entries it created, and settings from
        // the file are applied even if no entry could be imported
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
        // Let the activity's background show through the navigation bar instead of a contrast scrim
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    // Called after every import (also a failed one), e.g. to reload displayed data
    protected open fun onImportFinished() {}

    /**
     * Called when the user wants to start an import or export; showOptions then shows its options
     * dialog. If an import or export is already running (only one at a time), a dialog says so
     * instead.
     *
     * Before, the permission to show notifications is asked for (Android 13+), which is needed for
     * the progress notification. If the user has denied it before, an explanation comes first.
     * Android itself stops showing its question after the second denial.
     */
    protected fun prepareImportExport(showOptions: () -> Unit) {
        if (ImportExportWork.isRunning(this)) {
            showImportExportRunningDialog()
            return
        }
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        ) {
            showOptions()
            return
        }
        // The options dialog is opened below the system's question, so it is shown once that is
        // answered
        if (shouldShowRequestPermissionRationale(permission)) {
            showMessageDialog(
                this,
                getString(R.string.notification_permission_title),
                getString(R.string.notification_permission_text)
            ) {
                notificationPermissionRequest.launch(permission)
                showOptions()
            }
        } else {
            notificationPermissionRequest.launch(permission)
            showOptions()
        }
    }

    // Shows whether an import or export was started (false if one is already running)
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
        showInfoDialog(this, getString(R.string.info), getString(R.string.import_export_running))
    }

}
