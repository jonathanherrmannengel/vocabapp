package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class DB_Card {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo(name = "front")
    public String front;
    @ColumnInfo(name = "back")
    public String back;
    @ColumnInfo(name = "pack")
    public int pack;
    @ColumnInfo(name = "known")
    public int known;
    @ColumnInfo(name = "date")
    public long date;
    @ColumnInfo(name = "notes")
    public String notes;
    @ColumnInfo(name = "last_repetition")
    public long lastRepetition;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DB_Card that = (DB_Card) o;
        return uid == that.uid && pack == that.pack && known == that.known && Objects.equals(front, that.front) && Objects.equals(back, that.back) && Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, front, back, pack, known, notes);
    }
}
