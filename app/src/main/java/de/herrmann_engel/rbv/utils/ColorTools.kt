package de.herrmann_engel.rbv.utils

import android.content.res.Resources

/**
 * Returns the color at the given index of a color array resource (e.g. R.array.pack_color_main
 * with the color index of a pack), or null if there is no index or it is out of range.
 */
fun Resources.colorAt(arrayId: Int, index: Int?): Int? {
    val colors = obtainTypedArray(arrayId)
    try {
        return if (index != null && index in 0..<colors.length()) colors.getColor(index, 0) else null
    } finally {
        colors.recycle()
    }
}
