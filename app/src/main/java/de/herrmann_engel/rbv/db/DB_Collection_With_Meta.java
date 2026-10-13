package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;

import java.util.Objects;

/**
 * A collection together with the number of packs in it.
 */
public class DB_Collection_With_Meta {
    @Embedded
    public DB_Collection collection;
    // Number of packs in the collection, -1 if not counted
    public int counter;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DB_Collection_With_Meta that = (DB_Collection_With_Meta) o;
        return counter == that.counter && Objects.equals(collection, that.collection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collection, counter);
    }
}
