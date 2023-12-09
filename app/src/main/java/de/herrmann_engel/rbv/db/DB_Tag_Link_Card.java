package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity

public class DB_Tag_Link_Card {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "tagId")
    public int tag;

    @ColumnInfo(name = "cardId")
    public int card;
}
