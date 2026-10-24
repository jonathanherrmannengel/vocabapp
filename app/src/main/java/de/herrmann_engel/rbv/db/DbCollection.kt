package de.herrmann_engel.rbv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A collection is the top level of the hierarchy and groups packs. Shown to users as
 * "filing box".
 */
@Entity(tableName = "DB_Collection")
data class DbCollection(
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0,

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "desc")
    var desc: String = "",

    // Creation date (Unix time in seconds)
    @ColumnInfo(name = "date")
    var date: Long = 0,

    // Index into the pack color resource arrays (e.g. R.array.pack_color_main)
    @ColumnInfo(name = "colors")
    var colors: Int = 0,

    @ColumnInfo(name = "emoji")
    var emoji: String = ""
)
