@file:JvmName("AsyncImport")

package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*

class AsyncExport {
    private val context: Context
    private val listener: AsyncExportFinish
    private val listenerProgress: AsyncExportProgress
    private val singleCollection: Boolean
    private var collectionNo = 0
    private var includeSettings = false
    private var includeMedia: Boolean
    private val exportFileUri: Uri?

    constructor(
        context: Context,
        listener: AsyncExportFinish,
        listenerProgress: AsyncExportProgress,
        includeSettings: Boolean,
        includeMedia: Boolean,
        exportFileUri: Uri
    ) {
        singleCollection = false
        this.context = context
        this.listener = listener
        this.listenerProgress = listenerProgress
        this.includeSettings = includeSettings
        this.includeMedia = includeMedia
        this.exportFileUri = exportFileUri
    }

    constructor(
        context: Context,
        listener: AsyncExportFinish,
        listenerProgress: AsyncExportProgress,
        collectionNo: Int,
        includeMedia: Boolean
    ) {
        singleCollection = true
        this.context = context
        this.listener = listener
        this.listenerProgress = listenerProgress
        this.collectionNo = collectionNo
        this.includeMedia = includeMedia
        exportFileUri = null
    }

    fun execute() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                AsyncExportWorker(
                    context,
                    listener,
                    listenerProgress,
                    singleCollection,
                    collectionNo,
                    includeSettings,
                    includeMedia,
                    exportFileUri
                ).execute()
            }
        }
    }
}
