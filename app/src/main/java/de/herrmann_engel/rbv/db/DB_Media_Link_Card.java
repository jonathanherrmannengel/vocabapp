package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DB_Media_Link_Card {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "fileId")
    public int file;

    @ColumnInfo(name = "cardId")
    public int card;

}
