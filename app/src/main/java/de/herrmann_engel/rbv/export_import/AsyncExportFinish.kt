package de.herrmann_engel.rbv.export_import

import java.io.File

interface AsyncExportFinish {
    fun exportCardsResult(result: File?)
}
