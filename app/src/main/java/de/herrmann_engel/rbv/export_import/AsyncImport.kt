@file:JvmName("AsyncImport")

package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*

class AsyncImport internal constructor(
    private val context: Context,
    private val listener: AsyncImportFinish,
    private val listenerProgress: AsyncImportProgress,
    private val uri: Uri,
    private val mode: Int,
    private val includeSettings: Boolean,
    private val includeMedia: Boolean
) {
    fun execute() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                AsyncImportWorker(
                    context,
                    listener,
                    listenerProgress,
                    uri,
                    mode,
                    includeSettings,
                    includeMedia
                ).execute()
            }
        }
    }
}
