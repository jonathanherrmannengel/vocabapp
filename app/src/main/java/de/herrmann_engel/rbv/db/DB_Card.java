package de.herrmann_engel.rbv.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * A single flashcard with a front and a back side, belonging to a pack.
 */
@Entity
public class DB_Card {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo(name = "front")
    public String front;
    @ColumnInfo(name = "back")
    public String back;
    // uid of the pack the card belongs to
    @ColumnInfo(name = "pack")
    public int pack;
    // Learning progress: how often the card has been marked as known (decreases when it is not known)
    @ColumnInfo(name = "known")
    public int known;
    // Creation date (Unix time in seconds)
    @ColumnInfo(name = "date")
    public long date;
    @ColumnInfo(name = "notes")
    public String notes;
    // Date of the last repetition (Unix time in seconds, 0 if never repeated)
    @ColumnInfo(name = "last_repetition")
    public long lastRepetition;
    // Total number of repetitions
    @ColumnInfo(name = "count_repetitions")
    public int countRepetitions;

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
