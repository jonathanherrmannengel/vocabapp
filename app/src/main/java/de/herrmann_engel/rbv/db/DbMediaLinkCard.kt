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
    tableName = "DB_Media_Link_Card",
    foreignKeys = [ForeignKey(
        entity = DbMedia::class,
        parentColumns = ["uid"],
        childColumns = ["fileId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = DbCard::class,
        parentColumns = ["uid"],
        childColumns = ["cardId"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(value = ["fileId", "cardId"], unique = true), Index("cardId")]
)
class DbMediaLinkCard {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "fileId")
    var fileId: Int = 0

    @ColumnInfo(name = "cardId")
    var cardId: Int = 0
}
