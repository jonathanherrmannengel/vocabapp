package de.herrmann_engel.rbv.export_import

import android.util.ArrayMap

/**
 * Maps uids from an imported file to the uids of the newly created database entries,
 * so that references (e.g. card -> pack) can be translated during the import.
 */
class AsyncImportUidConvert {
    private val map: MutableMap<Int, Int> = ArrayMap()
    fun insertPair(oldValue: Int, newValue: Int) {
        map[oldValue] = newValue
    }

    // Returns null if the old uid is unknown
    fun getNewValue(oldValue: Int): Int? {
        return map[oldValue]
    }

    fun getSize(): Int {
        return map.size
    }
}
