package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class DB_Collection {

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
    @ColumnInfo(name = "emoji")
    public String emoji;

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DB_Collection compareCollection)) {
            return false;
        }
        return uid == compareCollection.uid && Objects.equals(name, compareCollection.name) && Objects.equals(desc, compareCollection.desc) && colors == compareCollection.colors && Objects.equals(emoji, compareCollection.emoji);
    }

}
