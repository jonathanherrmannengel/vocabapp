package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;

public class DB_Collection_With_Meta {

    @Embedded
    public DB_Collection collection;

    public int counter;
}
