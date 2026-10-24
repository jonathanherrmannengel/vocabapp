package de.herrmann_engel.rbv.export_import

/**
 * Result of an import. value is passed back from the background work (see ImportExportWork).
 */
enum class ImportErrorLevel(val value: Int) {
    OKAY(0),
    WARN(1),
    ERROR(2);

    companion object {
        // Unknown values (e.g. from a newer app version) count as ERROR
        fun fromValue(value: Int): ImportErrorLevel =
            entries.firstOrNull { it.value == value } ?: ERROR
    }
}
