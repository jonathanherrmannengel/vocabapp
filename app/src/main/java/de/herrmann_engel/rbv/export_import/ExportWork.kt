package de.herrmann_engel.rbv.export_import

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.export_import.ImportExportWork.KEY_COLLECTION_ID
import de.herrmann_engel.rbv.export_import.ImportExportWork.KEY_FILE
import de.herrmann_engel.rbv.export_import.ImportExportWork.KEY_INCLUDE_MEDIA
import de.herrmann_engel.rbv.export_import.ImportExportWork.KEY_INCLUDE_SETTINGS
import de.herrmann_engel.rbv.export_import.ImportExportWork.KEY_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background work that exports all data to the file at the given URI or a single collection
 * (see [Exporter]), started by [ImportExportWork]. The output is the path of the exported file
 * in the cache directory, missing if the export failed. An interrupted export is simply repeated.
 */
class ExportWork(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uri = inputData.getString(KEY_URI)?.toUri()
        val singleCollection = inputData.keyValueMap.containsKey(KEY_COLLECTION_ID)
        try {
            startForeground()
            val file = withContext(Dispatchers.IO) {
                Exporter(
                    applicationContext,
                    singleCollection,
                    inputData.getInt(KEY_COLLECTION_ID, 0),
                    inputData.getBoolean(KEY_INCLUDE_SETTINGS, false),
                    inputData.getBoolean(KEY_INCLUDE_MEDIA, false),
                    uri
                ) { rows -> setForegroundAsync(notification(rows)) }.execute()
            }
            return Result.success(
                if (file == null) workDataOf() else workDataOf(KEY_FILE to file.absolutePath)
            )
        } finally {
            uri?.let { ImportExportWork.releasePermission(applicationContext, it, URI_PERMISSIONS) }
        }
    }

    private fun notification(rows: Int?): ForegroundInfo = ImportExportWork.foregroundInfo(
        applicationContext,
        R.string.export_running,
        R.drawable.outline_file_upload_24,
        rows
    )

    private suspend fun startForeground() {
        try {
            setForeground(notification(null))
        } catch (e: IllegalStateException) {
            // Running as foreground work is not allowed (e.g. started while the app was in the
            // background): the export runs anyway, just without notification
            e.printStackTrace()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = notification(null)

    companion object {
        // Permissions kept for the destination file while the export runs
        const val URI_PERMISSIONS =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    }
}
