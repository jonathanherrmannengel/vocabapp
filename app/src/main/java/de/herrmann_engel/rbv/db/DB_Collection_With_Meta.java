package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;

import java.util.Objects;

public class DB_Collection_With_Meta {

    @Embedded
    public DB_Collection collection;
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
