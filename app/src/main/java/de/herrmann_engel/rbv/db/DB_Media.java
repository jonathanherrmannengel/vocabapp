package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DB_Media {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "file")
    public String file;

    @ColumnInfo(name = "mime")
    public String mime;
}
