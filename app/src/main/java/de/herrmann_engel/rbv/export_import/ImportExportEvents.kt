package de.herrmann_engel.rbv.export_import

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.channels.BufferOverflow
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Connects running imports and exports with the UI.
 *
 * Imports and exports run in the application scope and report here instead of calling an
 * activity directly. The activity in the foreground shows progress and results (see RBVActivity),
 * so results are neither lost nor delivered to a destroyed activity when the user leaves the
 * screen or rotates the device.
 *
 * Only one import or export runs at a time (see tryStart). Two simultaneous imports could create
 * duplicates, and an export during an import would write a half-finished state. This also means
 * that at most one result can be waiting to be shown.
 */
object ImportExportEvents {
    sealed class Result {
        // The exported file, or null if the export failed
        class ExportFinished(val file: File?) : Result()

        // One of the IMPORT_ERROR_LEVEL_* constants in Globals
        class ImportFinished(val errorLevel: Int) : Result()
    }

    // Progress messages are only shown while an activity is in the foreground; otherwise they are dropped
    private val progressFlow = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val progress: SharedFlow<String> = progressFlow

    // True while an import or export is running
    private val running = AtomicBoolean(false)

    val isRunning: Boolean
        get() = running.get()

    /**
     * Marks an import or export as running. Returns false if one is already running; the new one
     * must then not be started.
     */
    fun tryStart(): Boolean {
        return running.compareAndSet(false, true)
    }

    // Marks the running import or export as finished
    fun finish() {
        running.set(false)
    }

    // The latest result is kept until an activity has handled it
    private val resultFlow = MutableStateFlow<Result?>(null)
    val result: StateFlow<Result?> = resultFlow

    fun reportProgress(progress: String) {
        progressFlow.tryEmit(progress)
    }

    fun reportResult(result: Result) {
        resultFlow.value = result
    }

    /**
     * Marks the result as handled. Returns false if it was already handled (e.g. by another activity).
     */
    fun consumeResult(result: Result): Boolean {
        return resultFlow.compareAndSet(result, null)
    }
}
