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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DB_Pack db_pack = (DB_Pack) o;
        return uid == db_pack.uid && colors == db_pack.colors && collection == db_pack.collection && Objects.equals(name, db_pack.name) && Objects.equals(desc, db_pack.desc) && Objects.equals(emoji, db_pack.emoji);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, desc, colors, collection, emoji);
    }
}
