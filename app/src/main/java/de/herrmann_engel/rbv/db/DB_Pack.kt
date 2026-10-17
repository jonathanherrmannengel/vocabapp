package de.herrmann_engel.rbv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Objects

/**
 * A pack belongs to a collection and contains cards.
 */
@Entity(
    foreignKeys = [ForeignKey(
        entity = DB_Collection::class,
        parentColumns = ["uid"],
        childColumns = ["collection"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index("collection")]
)
class DB_Pack {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "name")
    var name: String = ""

    @ColumnInfo(name = "desc")
    var desc: String? = null

    // Creation date (Unix time in seconds)
    @ColumnInfo(name = "date")
    var date: Long = 0

    // Index into the pack color resource arrays (e.g. R.array.pack_color_main)
    @ColumnInfo(name = "colors")
    var colors: Int = 0

    // uid of the collection the pack belongs to
    @ColumnInfo(name = "collection")
    var collection: Int = 0

    @ColumnInfo(name = "emoji")
    var emoji: String? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DB_Pack
        return uid == that.uid && colors == that.colors && collection == that.collection && name == that.name && desc == that.desc && emoji == that.emoji
    }

    override fun hashCode(): Int {
        return Objects.hash(uid, name, desc, colors, collection, emoji)
    }
}
