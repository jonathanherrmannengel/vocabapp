package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * A tag that can be assigned to cards. Tag names are unique.
 */
@Entity(indices = @Index(value = "tag_name", unique = true))
public class DB_Tag {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "tag_name")
    public String name;

    @ColumnInfo(name = "emoji")
    public String emoji;

    @ColumnInfo(name = "color")
    public String color;
}
