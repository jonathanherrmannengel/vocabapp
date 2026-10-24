package de.herrmann_engel.rbv.export_import

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.settings.AppConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Runs imports and exports as background work (WorkManager, see [ImportWork] and [ExportWork]),
 * so they go on when the user leaves the app. While they run, a notification shows the progress.
 *
 * Only one import or export runs at a time. Two simultaneous imports could create duplicates, and
 * an export during an import would write a half-finished state.
 *
 * Activities observe the work (see RBVActivity) and show the result once, also if the work
 * finished while the app was in the background.
 */
object ImportExportWork {
    private const val UNIQUE_NAME = "import_export"
    private const val NOTIFICATION_CHANNEL = "import_export"
    private const val NOTIFICATION_ID = 1

    /**
     * Minimum time between two progress updates of the notification. Each update costs a
     * notification and a call to the system, and Android drops updates beyond a few per second.
     */
    const val PROGRESS_INTERVAL_MILLIS = 1000L

    // Tags of the work requests, to tell imports from exports
    const val TAG_IMPORT = "import"
    const val TAG_EXPORT = "export"

    // Input data
    const val KEY_URI = "uri"
    const val KEY_MODE = "mode"
    const val KEY_INCLUDE_SETTINGS = "includeSettings"
    const val KEY_INCLUDE_MEDIA = "includeMedia"
    const val KEY_COLLECTION_ID = "collectionId"

    // Output data: ImportErrorLevel of an import, path of the exported file (missing on errors)
    const val KEY_ERROR_LEVEL = "errorLevel"
    const val KEY_FILE = "file"

    /**
     * Starts an import of the file at uri. Returns false if an import or export is already
     * running; then nothing is started.
     */
    fun startImport(
        context: Context,
        uri: Uri,
        mode: ImportMode,
        includeSettings: Boolean,
        includeMedia: Boolean
    ): Boolean {
        val request = OneTimeWorkRequestBuilder<ImportWork>()
            .addTag(TAG_IMPORT)
            .setInputData(
                workDataOf(
                    KEY_URI to uri.toString(),
                    KEY_MODE to mode.value,
                    KEY_INCLUDE_SETTINGS to includeSettings,
                    KEY_INCLUDE_MEDIA to includeMedia
                )
            )
            .build()
        // The permission for the file given by the file picker lasts as long as the app process.
        // That is enough: an interrupted import is not repeated (see ImportWork).
        return start(context, request)
    }

    /**
     * Starts an export of all data to the file at uri. Returns false if an import or export is
     * already running; then nothing is started.
     */
    fun startExport(
        context: Context,
        uri: Uri,
        includeSettings: Boolean,
        includeMedia: Boolean
    ): Boolean {
        val request = OneTimeWorkRequestBuilder<ExportWork>()
            .addTag(TAG_EXPORT)
            .setInputData(
                workDataOf(
                    KEY_URI to uri.toString(),
                    KEY_INCLUDE_SETTINGS to includeSettings,
                    KEY_INCLUDE_MEDIA to includeMedia
                )
            )
            .build()
        return startWithPermission(context, uri, ExportWork.URI_PERMISSIONS, request)
    }

    /**
     * Starts an export of a single collection (to share it). Returns false if an import or
     * export is already running; then nothing is started.
     */
    fun startCollectionExport(context: Context, collectionId: Int, includeMedia: Boolean): Boolean {
        val request = OneTimeWorkRequestBuilder<ExportWork>()
            .addTag(TAG_EXPORT)
            .setInputData(
                workDataOf(KEY_COLLECTION_ID to collectionId, KEY_INCLUDE_MEDIA to includeMedia)
            )
            .build()
        return start(context, request)
    }

    /**
     * The work keeps the permission for the chosen file until it has finished, also if the app
     * process is stopped in between: an interrupted export is repeated (see ExportWork). Some apps
     * that provide files do not allow this; then the permission given by the file picker is used,
     * which lasts as long as the app process.
     */
    private fun startWithPermission(
        context: Context,
        uri: Uri,
        flags: Int,
        request: OneTimeWorkRequest
    ): Boolean {
        try {
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        if (start(context, request)) {
            return true
        }
        releasePermission(context, uri, flags)
        return false
    }

    private fun start(context: Context, request: OneTimeWorkRequest): Boolean {
        if (isRunning(context)) {
            return false
        }
        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.KEEP, request)
        return true
    }

    fun isRunning(context: Context): Boolean =
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(UNIQUE_NAME).get()
            .any { !it.state.isFinished }

    // The running import or export, else the last finished one; null if there is none
    fun observe(context: Context): Flow<WorkInfo?> =
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(UNIQUE_NAME)
            .map { infos -> infos.firstOrNull { !it.state.isFinished } ?: infos.firstOrNull() }

    /**
     * Marks the result of a finished work as shown. Returns false if it was already shown, also
     * before the app was restarted.
     */
    @Synchronized
    fun consumeResult(context: Context, id: UUID): Boolean {
        val config = AppConfig(context)
        if (config.shownImportExportResult == id.toString()) {
            return false
        }
        config.shownImportExportResult = id.toString()
        return true
    }

    fun releasePermission(context: Context, uri: Uri, flags: Int) {
        try {
            context.contentResolver.releasePersistableUriPermission(uri, flags)
        } catch (e: SecurityException) {
            // Not (or no longer) held
            e.printStackTrace()
        }
    }

    // Notification shown while an import or export runs; rows is the progress so far, if known
    fun foregroundInfo(
        context: Context,
        @StringRes title: Int,
        @DrawableRes icon: Int,
        rows: Int?
    ): ForegroundInfo {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    NOTIFICATION_CHANNEL,
                    context.getString(R.string.import_export_notification_channel),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
        // Tapping the notification opens the app
        val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.let { PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE) }
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
            .setSmallIcon(icon)
            .setContentTitle(context.getString(title))
            .setContentText(
                rows?.let { context.getString(R.string.import_export_lines_progress, it) }
            )
            .setProgress(0, 0, true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openApp)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
}
