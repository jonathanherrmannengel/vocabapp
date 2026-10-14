package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.net.Uri
import de.herrmann_engel.rbv.RBVApplication
import kotlinx.coroutines.launch

/**
 * Starts an export in the background (application scope, IO dispatcher). The actual work is
 * done by [AsyncExportWorker]; progress and result are reported via [ImportExportEvents].
 */
class AsyncExport {
    private val context: Context
    private val singleCollection: Boolean
    private var collectionNo = 0
    private var includeSettings = false
    private var includeMedia: Boolean
    private val exportFileUri: Uri?

    // Exports everything to the given file
    constructor(
        context: Context,
        includeSettings: Boolean,
        includeMedia: Boolean,
        exportFileUri: Uri
    ) {
        singleCollection = false
        this.context = context.applicationContext
        this.includeSettings = includeSettings
        this.includeMedia = includeMedia
        this.exportFileUri = exportFileUri
    }

    // Exports a single collection (e.g. for sharing it)
    constructor(
        context: Context,
        collectionNo: Int,
        includeMedia: Boolean
    ) {
        singleCollection = true
        this.context = context.applicationContext
        this.collectionNo = collectionNo
        this.includeMedia = includeMedia
        exportFileUri = null
    }

    fun execute() {
        (context as RBVApplication).applicationScope.launch {
            AsyncExportWorker(
                context,
                singleCollection,
                collectionNo,
                includeSettings,
                includeMedia,
                exportFileUri
            ).execute()
        }
    }
}
