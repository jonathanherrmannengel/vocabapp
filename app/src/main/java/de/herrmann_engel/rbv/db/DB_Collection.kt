package de.herrmann_engel.rbv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Objects

/**
 * A collection is the top level of the hierarchy and groups packs.
 */
@Entity
class DB_Collection {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @JvmField
    @ColumnInfo(name = "name")
    var name: String? = null

    @JvmField
    @ColumnInfo(name = "desc")
    var desc: String? = null

    // Creation date (Unix time in seconds)
    @JvmField
    @ColumnInfo(name = "date")
    var date: Long = 0

    // Index into the pack color resource arrays (e.g. R.array.pack_color_main)
    @JvmField
    @ColumnInfo(name = "colors")
    var colors: Int = 0

    @JvmField
    @ColumnInfo(name = "emoji")
    var emoji: String? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DB_Collection
        return uid == that.uid && colors == that.colors && name == that.name && desc == that.desc && emoji == that.emoji
    }

    override fun hashCode(): Int {
        return Objects.hash(uid, name, desc, colors, emoji)
    }
}
