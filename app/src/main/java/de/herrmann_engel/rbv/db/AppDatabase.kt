package de.herrmann_engel.rbv.db

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database of the app. Holds collections, packs, cards, media files, tags and the
 * link tables connecting media files and tags to cards.
 */
@Database(
    entities = [DbPack::class, DbCard::class, DbCollection::class, DbMedia::class, DbMediaLinkCard::class, DbTag::class, DbTagLinkCard::class],
    version = 12
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packDAO(): PackDao

    abstract fun cardDAO(): CardDao

    abstract fun collectionDAO(): CollectionDao

    abstract fun mediaDAO(): MediaDao

    abstract fun mediaLinkCardDAO(): MediaLinkCardDao

    abstract fun tagDAO(): TagDao

    abstract fun tagLinkCardDAO(): TagLinkCardDao

    companion object {
        private var appDatabase: AppDatabase? = null

        /**
         * Returns the app-wide database singleton, creating it on first access.
         */
        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            return appDatabase ?: AppDatabaseBuilder().get(context).also { appDatabase = it }
        }
    }
}
