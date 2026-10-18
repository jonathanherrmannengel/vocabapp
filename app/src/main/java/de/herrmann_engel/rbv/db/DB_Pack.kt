package de.herrmann_engel.rbv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
data class DB_Pack(
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "desc")
    var desc: String? = null,

    // Creation date (Unix time in seconds)
    @ColumnInfo(name = "date")
    var date: Long = 0,

    // Index into the pack color resource arrays (e.g. R.array.pack_color_main)
    @ColumnInfo(name = "colors")
    var colors: Int = 0,

    // uid of the collection the pack belongs to
    @ColumnInfo(name = "collection")
    var collectionId: Int = 0,

    @ColumnInfo(name = "emoji")
    var emoji: String? = null
)
