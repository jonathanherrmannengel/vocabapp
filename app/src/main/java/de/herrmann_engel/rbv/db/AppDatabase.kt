package de.herrmann_engel.rbv.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database of the app. Holds collections, packs, cards, media files, tags and the
 * link tables connecting media files and tags to cards.
 */
@Database(
    entities = [DB_Pack::class, DB_Card::class, DB_Collection::class, DB_Media::class, DB_Media_Link_Card::class, DB_Tag::class, DB_Tag_Link_Card::class],
    version = 12
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packDAO(): DB_Pack_DAO?

    abstract fun cardDAO(): DB_Card_DAO?

    abstract fun collectionDAO(): DB_Collection_DAO?

    abstract fun mediaDAO(): DB_Media_DAO?

    abstract fun mediaLinkCardDAO(): DB_Media_Link_Card_DAO?

    abstract fun tagDAO(): DB_Tag_DAO?

    abstract fun tagLinkCardDAO(): DB_Tag_Link_Card_DAO?

    companion object {
        private var appDatabase: AppDatabase? = null

        /**
         * Returns the app-wide database singleton, creating it on first access.
         */
        @Synchronized
        fun getInstance(context: Context?): AppDatabase? {
            if (appDatabase == null) {
                appDatabase = (AppDatabaseBuilder()).get(context)
            }
            return appDatabase
        }
    }
}
