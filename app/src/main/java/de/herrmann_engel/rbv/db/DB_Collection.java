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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DB_Collection that = (DB_Collection) o;
        return uid == that.uid && colors == that.colors && Objects.equals(name, that.name) && Objects.equals(desc, that.desc) && Objects.equals(emoji, that.emoji);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, desc, colors, emoji);
    }
}
