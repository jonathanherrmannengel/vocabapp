package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.net.Uri
import de.herrmann_engel.rbv.RBVApplication
import kotlinx.coroutines.launch

/**
 * Starts an import in the background (application scope, IO dispatcher). The actual work is
 * done by [AsyncImportWorker]; progress and result are reported via [ImportExportEvents].
 */
class AsyncImport(
    context: Context,
    private val uri: Uri,
    private val mode: Int,
    private val includeSettings: Boolean,
    private val includeMedia: Boolean
) {
    private val context: Context = context.applicationContext

    fun execute() {
        (context as RBVApplication).applicationScope.launch {
            AsyncImportWorker(
                context,
                uri,
                mode,
                includeSettings,
                includeMedia
            ).execute()
        }
    }
}
