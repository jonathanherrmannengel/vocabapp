package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;

public class DB_Collection_With_Meta {

    @Embedded
    public DB_Collection collection;
    public int counter;

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof DB_Collection_With_Meta compareCollection)) {
            return false;
        }
        if (counter != compareCollection.counter) {
            return false;
        }
        if (collection == null || compareCollection.collection == null) {
            return collection == compareCollection.collection;
        }
        return collection.equals(compareCollection.collection);
    }
}
