package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A media file (e.g. an image) in the media folder chosen by the user, identified by its file name.
 */
@Entity
public class DB_Media {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "file")
    public String file;

    @ColumnInfo(name = "mime")
    public String mime;
}
