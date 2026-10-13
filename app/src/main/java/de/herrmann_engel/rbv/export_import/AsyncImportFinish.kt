package de.herrmann_engel.rbv.export_import

/**
 * Receives the result of an import as one of the IMPORT_ERROR_LEVEL_* constants in Globals.
 */
interface AsyncImportFinish {
    fun importCardsResult(result: Int)
}
