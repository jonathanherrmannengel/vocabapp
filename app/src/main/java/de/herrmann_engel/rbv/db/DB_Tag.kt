package de.herrmann_engel.rbv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A tag that can be assigned to cards. Tag names are unique.
 */
@Entity(indices = [Index(value = ["tag_name"], unique = true)])
class DB_Tag {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "tag_name")
    var name: String = ""

    @ColumnInfo(name = "emoji")
    var emoji: String? = null

    @ColumnInfo(name = "color")
    var color: String? = null
}
