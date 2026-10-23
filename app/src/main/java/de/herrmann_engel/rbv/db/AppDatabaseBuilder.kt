package de.herrmann_engel.rbv.db

import android.content.Context
import androidx.room.Room.databaseBuilder
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.herrmann_engel.rbv.Globals

/**
 * Builds the Room database including all schema migrations from older app versions.
 */
class AppDatabaseBuilder {
    // v2: pack colors, renamed card table
    private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE db_pack ADD COLUMN colors INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE db_cards RENAME TO db_card")
        }
    }

    // v3: notes on cards
    private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE db_card ADD COLUMN notes TEXT")
        }
    }

    // v4: collections; all existing packs are moved into a new default collection (uid 1)
    private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE db_pack ADD COLUMN collection INTEGER NOT NULL DEFAULT 1")
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `db_collection` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `desc` TEXT, `date` INTEGER NOT NULL)"
            )
            database.execSQL(
                "INSERT INTO db_collection (name,`desc`,uid, date) VALUES ('default', 'default', 1, CAST(strftime('%s', 'now') AS INTEGER))"
            )
        }
    }

    // v6: collection colors, pack and collection emojis
    private val MIGRATION_4_6: Migration = object : Migration(4, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE db_collection ADD COLUMN colors INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE db_pack ADD COLUMN emoji TEXT")
            database.execSQL("ALTER TABLE db_collection ADD COLUMN emoji TEXT")
        }
    }

    // v7: media files and their links to cards
    private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `db_media` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `file` TEXT, `mime` TEXT)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `db_media_link_card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileId` INTEGER NOT NULL, `cardId` INTEGER NOT NULL)")
        }
    }

    // v8: date of the last repetition of a card
    private val MIGRATION_7_8: Migration = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE db_card ADD COLUMN last_repetition INTEGER DEFAULT 0 NOT NULL")
        }
    }

    // v9: tags and their links to cards
    private val MIGRATION_8_9: Migration = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `db_tag` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tag_name` TEXT)")
            database.execSQL("CREATE TABLE IF NOT EXISTS `db_tag_link_card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tagId` INTEGER NOT NULL, `cardId` INTEGER NOT NULL)")
        }
    }

    // v10: tag emojis and colors
    private val MIGRATION_9_10: Migration = object : Migration(9, 10) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE db_tag ADD COLUMN emoji TEXT")
            database.execSQL("ALTER TABLE db_tag ADD COLUMN color TEXT")
        }
    }

    // v11: number of repetitions of a card
    private val MIGRATION_10_11: Migration = object : Migration(10, 11) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE db_card ADD COLUMN count_repetitions INTEGER DEFAULT 0 NOT NULL")
        }
    }

    // v12: foreign keys (with cascading deletes) and indices on all references, unique tag names
    private val MIGRATION_11_12: Migration = object : Migration(11, 12) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // MIGRATION_3_4 used to store the date of the default collection as text
            for (table in arrayOf("DB_Collection", "DB_Pack", "DB_Card")) {
                database.execSQL("UPDATE $table SET date = COALESCE(CAST(strftime('%s', date) AS INTEGER), 0) WHERE typeof(date) = 'text'")
            }

            // Data has to satisfy the new constraints before the tables are recreated.
            // Packs and cards without existing parent are still visible in the "all" lists,
            // so they are moved to a recovery collection/pack instead of being deleted.
            val orphanedPacks = exists(
                database,
                "SELECT 1 FROM DB_Pack WHERE collection NOT IN (SELECT uid FROM DB_Collection)"
            )
            val orphanedCards = exists(
                database,
                "SELECT 1 FROM DB_Card WHERE pack NOT IN (SELECT uid FROM DB_Pack)"
            )
            if (orphanedPacks || orphanedCards) {
                val now = System.currentTimeMillis() / 1000L
                database.execSQL(
                    "INSERT INTO DB_Collection (name, `desc`, date, colors) VALUES ('_recovered', '_recovered', ?, 0)",
                    arrayOf(now)
                )
                val collectionId = lastInsertId(database)
                database.execSQL(
                    "UPDATE DB_Pack SET collection = ? WHERE collection NOT IN (SELECT uid FROM DB_Collection)",
                    arrayOf(collectionId)
                )
                if (orphanedCards) {
                    database.execSQL(
                        "INSERT INTO DB_Pack (name, `desc`, date, colors, collection) VALUES ('_recovered', '_recovered', ?, 0, ?)",
                        arrayOf(now, collectionId)
                    )
                    val packId = lastInsertId(database)
                    database.execSQL(
                        "UPDATE DB_Card SET pack = ? WHERE pack NOT IN (SELECT uid FROM DB_Pack)",
                        arrayOf(packId)
                    )
                }
            }
            // Tags without a name and media entries without a file name cannot be used; their
            // links are removed below
            database.execSQL("DELETE FROM DB_Tag WHERE tag_name IS NULL")
            database.execSQL("DELETE FROM DB_Media WHERE file IS NULL")
            // Links to missing entries are invisible, so they can simply be removed
            database.execSQL("DELETE FROM DB_Media_Link_Card WHERE fileId NOT IN (SELECT uid FROM DB_Media) OR cardId NOT IN (SELECT uid FROM DB_Card)")
            database.execSQL("DELETE FROM DB_Tag_Link_Card WHERE tagId NOT IN (SELECT uid FROM DB_Tag) OR cardId NOT IN (SELECT uid FROM DB_Card)")
            // Merge tags with the same name into the oldest one
            database.execSQL("UPDATE DB_Tag_Link_Card SET tagId = (SELECT MIN(t2.uid) FROM DB_Tag t1 JOIN DB_Tag t2 ON t2.tag_name = t1.tag_name WHERE t1.uid = DB_Tag_Link_Card.tagId) WHERE tagId IN (SELECT t.uid FROM DB_Tag t WHERE t.uid > (SELECT MIN(t2.uid) FROM DB_Tag t2 WHERE t2.tag_name = t.tag_name))")
            database.execSQL("DELETE FROM DB_Tag WHERE uid > (SELECT MIN(t2.uid) FROM DB_Tag t2 WHERE t2.tag_name = DB_Tag.tag_name)")
            // Remove duplicate links, which the unique indices of version 12 do not allow
            database.execSQL("DELETE FROM DB_Media_Link_Card WHERE uid NOT IN (SELECT MIN(uid) FROM DB_Media_Link_Card GROUP BY fileId, cardId)")
            database.execSQL("DELETE FROM DB_Tag_Link_Card WHERE uid NOT IN (SELECT MIN(uid) FROM DB_Tag_Link_Card GROUP BY tagId, cardId)")
            // Since version 12, tags are deleted together with their last link; remove tags that
            // have no link already
            database.execSQL("DELETE FROM DB_Tag WHERE uid NOT IN (SELECT tagId FROM DB_Tag_Link_Card)")

            // SQLite cannot add foreign keys or NOT NULL constraints to existing tables, so the
            // tables are recreated. Parents are recreated before their children. Missing texts
            // become empty.
            recreateTable(
                database, "DB_Collection",
                "CREATE TABLE IF NOT EXISTS `_new_DB_Collection` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `desc` TEXT NOT NULL, `date` INTEGER NOT NULL, `colors` INTEGER NOT NULL, `emoji` TEXT NOT NULL)",
                "`uid`, `name`, `desc`, `date`, `colors`, `emoji`",
                "`uid`, COALESCE(`name`, ''), COALESCE(`desc`, ''), `date`, `colors`, COALESCE(`emoji`, '')"
            )
            recreateTable(
                database, "DB_Pack",
                "CREATE TABLE IF NOT EXISTS `_new_DB_Pack` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `desc` TEXT NOT NULL, `date` INTEGER NOT NULL, `colors` INTEGER NOT NULL, `collection` INTEGER NOT NULL, `emoji` TEXT NOT NULL, FOREIGN KEY(`collection`) REFERENCES `DB_Collection`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
                "`uid`, `name`, `desc`, `date`, `colors`, `collection`, `emoji`",
                "`uid`, COALESCE(`name`, ''), COALESCE(`desc`, ''), `date`, `colors`, `collection`, COALESCE(`emoji`, '')"
            )
            recreateTable(
                database, "DB_Card",
                "CREATE TABLE IF NOT EXISTS `_new_DB_Card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `front` TEXT NOT NULL, `back` TEXT NOT NULL, `pack` INTEGER NOT NULL, `known` INTEGER NOT NULL, `date` INTEGER NOT NULL, `notes` TEXT NOT NULL, `last_repetition` INTEGER NOT NULL, `count_repetitions` INTEGER NOT NULL, FOREIGN KEY(`pack`) REFERENCES `DB_Pack`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
                "`uid`, `front`, `back`, `pack`, `known`, `date`, `notes`, `last_repetition`, `count_repetitions`",
                "`uid`, COALESCE(`front`, ''), COALESCE(`back`, ''), `pack`, `known`, `date`, COALESCE(`notes`, ''), `last_repetition`, `count_repetitions`"
            )
            recreateTable(
                database, "DB_Media",
                "CREATE TABLE IF NOT EXISTS `_new_DB_Media` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `file` TEXT NOT NULL, `mime` TEXT NOT NULL)",
                "`uid`, `file`, `mime`",
                "`uid`, `file`, COALESCE(`mime`, '')"
            )
            recreateTable(
                database, "DB_Tag",
                "CREATE TABLE IF NOT EXISTS `_new_DB_Tag` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tag_name` TEXT NOT NULL, `emoji` TEXT NOT NULL, `color` TEXT NOT NULL)",
                "`uid`, `tag_name`, `emoji`, `color`",
                "`uid`, `tag_name`, COALESCE(`emoji`, ''), COALESCE(`color`, '')"
            )
            recreateTable(
                database, "DB_Media_Link_Card",
                "CREATE TABLE IF NOT EXISTS `_new_DB_Media_Link_Card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileId` INTEGER NOT NULL, `cardId` INTEGER NOT NULL, FOREIGN KEY(`fileId`) REFERENCES `DB_Media`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`cardId`) REFERENCES `DB_Card`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
                "`uid`, `fileId`, `cardId`"
            )
            recreateTable(
                database, "DB_Tag_Link_Card",
                "CREATE TABLE IF NOT EXISTS `_new_DB_Tag_Link_Card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tagId` INTEGER NOT NULL, `cardId` INTEGER NOT NULL, FOREIGN KEY(`tagId`) REFERENCES `DB_Tag`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`cardId`) REFERENCES `DB_Card`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
                "`uid`, `tagId`, `cardId`"
            )

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_DB_Pack_collection` ON `DB_Pack` (`collection`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_DB_Card_pack` ON `DB_Card` (`pack`)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_DB_Media_Link_Card_fileId_cardId` ON `DB_Media_Link_Card` (`fileId`, `cardId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_DB_Media_Link_Card_cardId` ON `DB_Media_Link_Card` (`cardId`)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_DB_Tag_tag_name` ON `DB_Tag` (`tag_name`)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_DB_Tag_Link_Card_tagId_cardId` ON `DB_Tag_Link_Card` (`tagId`, `cardId`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_DB_Tag_Link_Card_cardId` ON `DB_Tag_Link_Card` (`cardId`)")
        }

        private fun exists(database: SupportSQLiteDatabase, query: String): Boolean {
            database.query(query).use { cursor ->
                return cursor.moveToFirst()
            }
        }

        private fun lastInsertId(database: SupportSQLiteDatabase): Long {
            database.query("SELECT last_insert_rowid()").use { cursor ->
                cursor.moveToFirst()
                return cursor.getLong(0)
            }
        }

        /**
         * Copies all rows into a new table with the given definition and replaces the old table by
         * it. values are the expressions selected from the old table for the columns (by default
         * the columns themselves).
         */
        private fun recreateTable(
            database: SupportSQLiteDatabase,
            table: String,
            createNewTable: String,
            columns: String,
            values: String = columns
        ) {
            database.execSQL(createNewTable)
            database.execSQL("INSERT INTO `_new_$table` ($columns) SELECT $values FROM `$table`")
            database.execSQL("DROP TABLE `$table`")
            database.execSQL("ALTER TABLE `_new_$table` RENAME TO `$table`")
        }
    }

    fun get(context: Context): AppDatabase {
        return databaseBuilder<AppDatabase>(
            context,
            AppDatabase::class.java, Globals.DB_NAME
        ) // Queries are executed synchronously throughout the app
            .allowMainThreadQueries()
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12
            )
            .build()
    }
}
