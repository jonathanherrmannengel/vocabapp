package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Link table: assigns a media file to a card (many-to-many).
 */
@Entity(
        foreignKeys = {
                @ForeignKey(entity = DB_Media.class, parentColumns = "uid", childColumns = "fileId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = DB_Card.class, parentColumns = "uid", childColumns = "cardId", onDelete = ForeignKey.CASCADE)
        },
        // A media file is linked to a card at most once; the index also serves lookups by fileId
        indices = {@Index(value = {"fileId", "cardId"}, unique = true), @Index("cardId")}
)
public class DB_Media_Link_Card {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "fileId")
    public int file;

    @ColumnInfo(name = "cardId")
    public int card;
}
