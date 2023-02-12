package de.herrmann_engel.rbv.db;

import androidx.room.Embedded;

public class DB_Pack_With_Meta {
    @Embedded
    public DB_Pack pack;

    public int counter;

    public String collectionName;
}
