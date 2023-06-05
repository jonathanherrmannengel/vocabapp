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

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DB_Card compareCard)) {
            return false;
        }
        return uid == compareCard.uid && Objects.equals(front, compareCard.front) && Objects.equals(back, compareCard.back) && pack == compareCard.pack && known == compareCard.known && Objects.equals(notes, compareCard.notes);
    }

}
