package de.herrmann_engel.rbv.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Room database of the app. Holds collections, packs, cards, media files, tags and the
 * link tables connecting media files and tags to cards.
 */
@Database(entities = {DB_Pack.class, DB_Card.class, DB_Collection.class, DB_Media.class, DB_Media_Link_Card.class, DB_Tag.class, DB_Tag_Link_Card.class}, version = 12)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase appDatabase;

    /**
     * Returns the app-wide database singleton, creating it on first access.
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            appDatabase = (new AppDatabaseBuilder()).get(context);
        }
        return appDatabase;
    }

    public abstract DB_Pack_DAO packDAO();

    public abstract DB_Card_DAO cardDAO();

    public abstract DB_Collection_DAO collectionDAO();

    public abstract DB_Media_DAO mediaDAO();

    public abstract DB_Media_Link_Card_DAO mediaLinkCardDAO();

    public abstract DB_Tag_DAO tagDAO();

    public abstract DB_Tag_Link_Card_DAO tagLinkCardDAO();
}
