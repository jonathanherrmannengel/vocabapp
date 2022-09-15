package de.herrmann_engel.rbv;

import android.content.Context;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DB_Pack.class, DB_Card.class, DB_Collection.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase appDatabase;

    public static synchronized AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            appDatabase = (new AppDatabaseBuilder()).get(context);
        }
        return appDatabase;
    }

    public abstract DB_Pack_DAO packDAO();

    public abstract DB_Card_DAO cardDAO();

    public abstract DB_Collection_DAO collectionDAO();
}
