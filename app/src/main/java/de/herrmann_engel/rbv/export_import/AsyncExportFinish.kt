package de.herrmann_engel.rbv.export_import

import java.io.File

/**
 * Receives the result of an export: the exported file, or null if the export failed.
 */
interface AsyncExportFinish {
    fun exportCardsResult(result: File?)
}
