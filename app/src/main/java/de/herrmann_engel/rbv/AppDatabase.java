package de.herrmann_engel.rbv;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DB_Pack.class, DB_Card.class, DB_Collection.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DB_Pack_DAO packDAO();
    public abstract DB_Card_DAO cardDAO();
    public abstract DB_Collection_DAO collectionDAO();
}
