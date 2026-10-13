package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * A pack belongs to a collection and contains cards.
 */
@Entity
public class DB_Pack {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "desc")
    public String desc;
    // Creation date (Unix time in seconds)
    @ColumnInfo(name = "date")
    public long date;
    // Index into the pack color resource arrays (e.g. R.array.pack_color_main)
    @ColumnInfo(name = "colors")
    public int colors;
    // uid of the collection the pack belongs to
    @ColumnInfo(name = "collection")
    public int collection;
    @ColumnInfo(name = "emoji")
    public String emoji;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DB_Pack that = (DB_Pack) o;
        return uid == that.uid && colors == that.colors && collection == that.collection && Objects.equals(name, that.name) && Objects.equals(desc, that.desc) && Objects.equals(emoji, that.emoji);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, desc, colors, collection, emoji);
    }
}
