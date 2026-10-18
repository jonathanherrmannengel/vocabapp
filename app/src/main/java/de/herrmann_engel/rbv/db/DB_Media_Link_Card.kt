package de.herrmann_engel.rbv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Link table: assigns a media file to a card (many-to-many).
 */
@Entity(
    foreignKeys = [ForeignKey(
        entity = DB_Media::class,
        parentColumns = ["uid"],
        childColumns = ["fileId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = DB_Card::class,
        parentColumns = ["uid"],
        childColumns = ["cardId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(value = ["fileId", "cardId"], unique = true), Index("cardId")]
)
class DB_Media_Link_Card {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "fileId")
    var fileId: Int = 0

    @ColumnInfo(name = "cardId")
    var cardId: Int = 0
}
