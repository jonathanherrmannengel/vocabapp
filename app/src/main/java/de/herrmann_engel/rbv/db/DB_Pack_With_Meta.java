package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;

import java.util.Objects;

/**
 * A pack together with the name of its collection and the number of cards in it.
 */
public class DB_Pack_With_Meta {

    @Embedded
    public DB_Pack pack;
    // Number of cards in the pack, -1 if not counted
    public int counter;
    public String collectionName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DB_Pack_With_Meta that = (DB_Pack_With_Meta) o;
        return counter == that.counter && Objects.equals(pack, that.pack) && Objects.equals(collectionName, that.collectionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pack, counter, collectionName);
    }
}
