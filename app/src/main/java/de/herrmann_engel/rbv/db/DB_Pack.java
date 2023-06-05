package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class DB_Pack {

    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "desc")
    public String desc;
    @ColumnInfo(name = "date")
    public long date;
    @ColumnInfo(name = "colors")
    public int colors;
    @ColumnInfo(name = "collection")
    public int collection;
    @ColumnInfo(name = "emoji")
    public String emoji;

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DB_Pack comparePack)) {
            return false;
        }
        return uid == comparePack.uid && Objects.equals(name, comparePack.name) && Objects.equals(desc, comparePack.desc) && colors == comparePack.colors && collection == comparePack.collection && Objects.equals(emoji, comparePack.emoji);
    }

}
