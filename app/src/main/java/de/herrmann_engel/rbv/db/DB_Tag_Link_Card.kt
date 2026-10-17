package de.herrmann_engel.rbv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Link table: assigns a tag to a card (many-to-many).
 */
@Entity(
    foreignKeys = [ForeignKey(
        entity = DB_Tag::class,
        parentColumns = "uid",
        childColumns = "tagId",
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = DB_Card::class,
        parentColumns = "uid",
        childColumns = "cardId",
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(value = ["tagId", "cardId"], unique = true), Index("cardId")]
)
class DB_Tag_Link_Card {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @JvmField
    @ColumnInfo(name = "tagId")
    var tag: Int = 0

    @JvmField
    @ColumnInfo(name = "cardId")
    var card: Int = 0
}
