package de.herrmann_engel.rbv

import android.util.ArrayMap

class AsyncImportUidConvert {
    private val map: MutableMap<Int, Int> = ArrayMap()
    fun insertPair(oldValue: Int, newValue: Int) {
        map[oldValue] = newValue
    }

    fun getNewValue(oldValue: Int): Int {
        return map.getOrDefault(oldValue, 0)
    }

    fun getSize(): Int {
        return map.size
    }
}
