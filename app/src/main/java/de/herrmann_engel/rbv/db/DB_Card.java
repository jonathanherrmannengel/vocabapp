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
        DB_Card db_card = (DB_Card) o;
        return uid == db_card.uid && pack == db_card.pack && known == db_card.known && Objects.equals(front, db_card.front) && Objects.equals(back, db_card.back) && Objects.equals(notes, db_card.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, front, back, pack, known, notes);
    }
}
