package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;

import java.util.Objects;

public class DB_Pack_With_Meta {

    @Embedded
    public DB_Pack pack;
    public int counter;
    public String collectionName;

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DB_Pack_With_Meta comparePack)) {
            return false;
        }
        if (counter != comparePack.counter || !Objects.equals(collectionName, comparePack.collectionName)) {
            return false;
        }
        if (pack == null || comparePack.pack == null) {
            return pack == comparePack.pack;
        }
        return pack.equals(comparePack.pack);
    }
}
