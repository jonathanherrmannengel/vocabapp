package de.herrmann_engel.rbv.export_import

/**
 * Receives progress messages of a running import.
 */
interface AsyncImportProgress {
    fun importCardsProgress(progress: String)
}
