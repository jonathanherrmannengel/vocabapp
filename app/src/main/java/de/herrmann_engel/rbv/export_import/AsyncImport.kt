@file:JvmName("AsyncImport")

package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AsyncImport internal constructor(
    private val context: Context,
    private val listener: AsyncImportFinish,
    private val uri: Uri,
    private val mode: Int
) {
    fun execute() {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                AsyncImportWorker(
                    context,
                    listener,
                    uri,
                    mode
                ).execute()
            }
        }
    }
}
