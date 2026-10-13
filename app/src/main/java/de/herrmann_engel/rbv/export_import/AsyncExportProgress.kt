package de.herrmann_engel.rbv.export_import

/**
 * Receives progress messages of a running export.
 */
interface AsyncExportProgress {
    fun exportCardsProgress(progress: String)
}
