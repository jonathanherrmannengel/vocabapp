package de.herrmann_engel.rbv.export_import

/**
 * How an import handles collections, packs and cards that already exist: skip them, import them as
 * duplicates or merge them into the existing ones; or import a simple front/back list. value is
 * passed to the background work (see ImportExportWork).
 */
enum class ImportMode(val value: Int) {
    SKIP(0),
    DUPLICATES(1),
    INTEGRATE(2),
    SIMPLE_LIST(3);

    companion object {
        // Unknown values (e.g. from a newer app version) count as INTEGRATE
        fun fromValue(value: Int): ImportMode =
            entries.firstOrNull { it.value == value } ?: INTEGRATE
    }
}
