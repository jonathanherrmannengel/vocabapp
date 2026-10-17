package de.herrmann_engel.rbv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A media file (e.g. an image) in the media folder chosen by the user, identified by its file name.
 */
@Entity
class DB_Media {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @JvmField
    @ColumnInfo(name = "file")
    var file: String? = null

    @JvmField
    @ColumnInfo(name = "mime")
    var mime: String? = null
}
