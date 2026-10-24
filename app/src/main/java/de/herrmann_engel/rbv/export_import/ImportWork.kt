package de.herrmann_engel.rbv.export_import

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.export_import.ImportExportWork.KEY_ERROR_LEVEL
import de.herrmann_engel.rbv.export_import.ImportExportWork.KEY_INCLUDE_MEDIA
import de.herrmann_engel.rbv.export_import.ImportExportWork.KEY_INCLUDE_SETTINGS
import de.herrmann_engel.rbv.export_import.ImportExportWork.KEY_MODE
import de.herrmann_engel.rbv.export_import.ImportExportWork.KEY_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background work that imports a file (see [Importer]), started by [ImportExportWork].
 * The output is the result as value of [ImportErrorLevel].
 */
class ImportWork(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uri = inputData.getString(KEY_URI)?.toUri() ?: return Result.failure()
        // An interrupted import (e.g. the app process was stopped) is not repeated: the rows
        // imported until then are kept, and a second run would import them again
        if (runAttemptCount > 0) {
            return result(ImportErrorLevel.WARN)
        }
        startForeground()
        val errorLevel = withContext(Dispatchers.IO) {
            Importer(
                applicationContext,
                uri,
                ImportMode.fromValue(inputData.getInt(KEY_MODE, ImportMode.INTEGRATE.value)),
                inputData.getBoolean(KEY_INCLUDE_SETTINGS, false),
                inputData.getBoolean(KEY_INCLUDE_MEDIA, false)
            ) { rows -> setForegroundAsync(notification(rows)) }.execute()
        }
        return result(errorLevel)
    }

    private fun result(errorLevel: ImportErrorLevel) =
        Result.success(workDataOf(KEY_ERROR_LEVEL to errorLevel.value))

    private fun notification(rows: Int?): ForegroundInfo = ImportExportWork.foregroundInfo(
        applicationContext,
        R.string.import_running,
        R.drawable.outline_file_download_24,
        rows
    )

    private suspend fun startForeground() {
        try {
            setForeground(notification(null))
        } catch (e: IllegalStateException) {
            // Running as foreground work is not allowed (e.g. started while the app was in the
            // background): the import runs anyway, just without notification
            e.printStackTrace()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = notification(null)
}
