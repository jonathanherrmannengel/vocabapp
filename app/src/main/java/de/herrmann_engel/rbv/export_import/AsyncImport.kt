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

    /**
     * Starts the operation. Returns false if another import or export is still running; then
     * nothing is started.
     */
    fun execute(): Boolean {
        if (!ImportExportEvents.tryStart()) {
            return false
        }
        (context as RBVApplication).applicationScope.launch {
            val result = try {
                AsyncImportWorker(
                    context,
                    uri,
                    mode,
                    includeSettings,
                    includeMedia
                ).execute()
            } catch (e: Throwable) {
                // Unexpected failure: no result, but a new import or export must be possible
                ImportExportEvents.finish()
                throw e
            }
            // Finish before publishing the result, so a new import or export can be started right away
            ImportExportEvents.finish()
            ImportExportEvents.reportResult(result)
        }
        return true
    }
}
