package de.herrmann_engel.rbv.db

import androidx.room.Embedded
import androidx.room.Ignore
import java.util.Objects

/**
 * A card together with data needed to display it in lists: the color of its pack and the
 * names (and emojis) of its tags. The ignored fields are computed in memory and not stored.
 */
class DB_Card_With_Meta {
    @JvmField
    @Embedded
    var card: DB_Card? = null
    @JvmField
    var packColor: Int = 0
    @JvmField
    var tagNames: String? = null

    // Front and back with formatting markup (*, _) removed; used for searching and sorting
    @Ignore
    var formattedFront: String? = null

    @Ignore
    var formattedBack: String? = null

    // True if the faster, less accurate unformatting was used (very large lists)
    @Ignore
    var formattingIsInaccurate: Boolean = false

    // Temporary weight for the "mixed" sort order
    @Ignore
    var mixedWeight: Double = 0.0

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DB_Card_With_Meta
        return packColor == that.packColor && formattingIsInaccurate == that.formattingIsInaccurate && card == that.card && tagNames == that.tagNames && formattedFront == that.formattedFront && formattedBack == that.formattedBack
    }

    override fun hashCode(): Int {
        return Objects.hash(
            card,
            packColor,
            tagNames,
            formattedFront,
            formattedBack,
            formattingIsInaccurate
        )
    }
}
