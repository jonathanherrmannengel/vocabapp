package de.herrmann_engel.rbv.db;

import android.content.Context;
import android.database.Cursor;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import de.herrmann_engel.rbv.Globals;

/**
 * Builds the Room database including all schema migrations from older app versions.
 */
public class AppDatabaseBuilder {
    // v2: pack colors, renamed card table
    private final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_pack ADD COLUMN colors INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE db_cards RENAME TO db_card");
        }
    };
    // v3: notes on cards
    private final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_card ADD COLUMN notes TEXT");
        }
    };
    // v4: collections; all existing packs are moved into a new default collection (uid 1)
    private final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_pack ADD COLUMN collection INTEGER NOT NULL DEFAULT 1");
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `db_collection` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `desc` TEXT, `date` INTEGER NOT NULL)");
            database.execSQL(
                    "INSERT INTO db_collection (name,`desc`,uid, date) VALUES ('default', 'default', 1, CAST(strftime('%s', 'now') AS INTEGER))");
        }
    };
    // v6: collection colors, pack and collection emojis
    private final Migration MIGRATION_4_6 = new Migration(4, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_collection ADD COLUMN colors INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE db_pack ADD COLUMN emoji TEXT");
            database.execSQL("ALTER TABLE db_collection ADD COLUMN emoji TEXT");
        }
    };
    // v7: media files and their links to cards
    private final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `db_media` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `file` TEXT, `mime` TEXT)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `db_media_link_card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileId` INTEGER NOT NULL, `cardId` INTEGER NOT NULL)");
        }
    };
    // v8: date of the last repetition of a card
    private final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_card ADD COLUMN last_repetition INTEGER DEFAULT 0 NOT NULL");
        }
    };
    // v9: tags and their links to cards
    private final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `db_tag` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tag_name` TEXT)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `db_tag_link_card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tagId` INTEGER NOT NULL, `cardId` INTEGER NOT NULL)");
        }
    };
    // v10: tag emojis and colors
    private final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_tag ADD COLUMN emoji TEXT");
            database.execSQL("ALTER TABLE db_tag ADD COLUMN color TEXT");
        }
    };
    // v11: number of repetitions of a card
    private final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_card ADD COLUMN count_repetitions INTEGER DEFAULT 0 NOT NULL");
        }
    };
    // v12: foreign keys (with cascading deletes) and indices on all references, unique tag names
    private final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // MIGRATION_3_4 used to store the date of the default collection as text
            for (String table : new String[]{"DB_Collection", "DB_Pack", "DB_Card"}) {
                database.execSQL("UPDATE " + table + " SET date = COALESCE(CAST(strftime('%s', date) AS INTEGER), 0) WHERE typeof(date) = 'text'");
            }

            // Data has to satisfy the new constraints before the tables are recreated.
            // Packs and cards without existing parent are still visible in the "all" lists,
            // so they are moved to a recovery collection/pack instead of being deleted.
            boolean orphanedPacks = exists(database, "SELECT 1 FROM DB_Pack WHERE collection NOT IN (SELECT uid FROM DB_Collection)");
            boolean orphanedCards = exists(database, "SELECT 1 FROM DB_Card WHERE pack NOT IN (SELECT uid FROM DB_Pack)");
            if (orphanedPacks || orphanedCards) {
                long now = System.currentTimeMillis() / 1000L;
                database.execSQL("INSERT INTO DB_Collection (name, `desc`, date, colors) VALUES ('_recovered', '_recovered', ?, 0)", new Object[]{now});
                long collectionId = lastInsertId(database);
                database.execSQL("UPDATE DB_Pack SET collection = ? WHERE collection NOT IN (SELECT uid FROM DB_Collection)", new Object[]{collectionId});
                if (orphanedCards) {
                    database.execSQL("INSERT INTO DB_Pack (name, `desc`, date, colors, collection) VALUES ('_recovered', '_recovered', ?, 0, ?)", new Object[]{now, collectionId});
                    long packId = lastInsertId(database);
                    database.execSQL("UPDATE DB_Card SET pack = ? WHERE pack NOT IN (SELECT uid FROM DB_Pack)", new Object[]{packId});
                }
            }
            // Links to missing entries are invisible, so they can simply be removed
            database.execSQL("DELETE FROM DB_Media_Link_Card WHERE fileId NOT IN (SELECT uid FROM DB_Media) OR cardId NOT IN (SELECT uid FROM DB_Card)");
            database.execSQL("DELETE FROM DB_Tag_Link_Card WHERE tagId NOT IN (SELECT uid FROM DB_Tag) OR cardId NOT IN (SELECT uid FROM DB_Card)");
            // Merge tags with the same name into the oldest one
            database.execSQL("UPDATE DB_Tag_Link_Card SET tagId = (SELECT MIN(t2.uid) FROM DB_Tag t1 JOIN DB_Tag t2 ON t2.tag_name = t1.tag_name WHERE t1.uid = DB_Tag_Link_Card.tagId) WHERE tagId IN (SELECT t.uid FROM DB_Tag t WHERE t.uid > (SELECT MIN(t2.uid) FROM DB_Tag t2 WHERE t2.tag_name = t.tag_name))");
            database.execSQL("DELETE FROM DB_Tag WHERE uid > (SELECT MIN(t2.uid) FROM DB_Tag t2 WHERE t2.tag_name = DB_Tag.tag_name)");
            database.execSQL("DELETE FROM DB_Tag_Link_Card WHERE uid NOT IN (SELECT MIN(uid) FROM DB_Tag_Link_Card GROUP BY tagId, cardId)");
            // From now on, tags are deleted as soon as they are no longer used; remove those that
            // are unused already
            database.execSQL("DELETE FROM DB_Tag WHERE uid NOT IN (SELECT tagId FROM DB_Tag_Link_Card)");

            // SQLite cannot add foreign keys to existing tables, so the tables are recreated.
            // Parents are recreated before their children.
            recreateTable(database, "DB_Pack",
                    "CREATE TABLE IF NOT EXISTS `_new_DB_Pack` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `desc` TEXT, `date` INTEGER NOT NULL, `colors` INTEGER NOT NULL, `collection` INTEGER NOT NULL, `emoji` TEXT, FOREIGN KEY(`collection`) REFERENCES `DB_Collection`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
                    "`uid`, `name`, `desc`, `date`, `colors`, `collection`, `emoji`");
            recreateTable(database, "DB_Card",
                    "CREATE TABLE IF NOT EXISTS `_new_DB_Card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `front` TEXT, `back` TEXT, `pack` INTEGER NOT NULL, `known` INTEGER NOT NULL, `date` INTEGER NOT NULL, `notes` TEXT, `last_repetition` INTEGER NOT NULL, `count_repetitions` INTEGER NOT NULL, FOREIGN KEY(`pack`) REFERENCES `DB_Pack`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
                    "`uid`, `front`, `back`, `pack`, `known`, `date`, `notes`, `last_repetition`, `count_repetitions`");
            recreateTable(database, "DB_Media_Link_Card",
                    "CREATE TABLE IF NOT EXISTS `_new_DB_Media_Link_Card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileId` INTEGER NOT NULL, `cardId` INTEGER NOT NULL, FOREIGN KEY(`fileId`) REFERENCES `DB_Media`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`cardId`) REFERENCES `DB_Card`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
                    "`uid`, `fileId`, `cardId`");
            recreateTable(database, "DB_Tag_Link_Card",
                    "CREATE TABLE IF NOT EXISTS `_new_DB_Tag_Link_Card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tagId` INTEGER NOT NULL, `cardId` INTEGER NOT NULL, FOREIGN KEY(`tagId`) REFERENCES `DB_Tag`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`cardId`) REFERENCES `DB_Card`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
                    "`uid`, `tagId`, `cardId`");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_DB_Pack_collection` ON `DB_Pack` (`collection`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_DB_Card_pack` ON `DB_Card` (`pack`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_DB_Media_Link_Card_fileId` ON `DB_Media_Link_Card` (`fileId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_DB_Media_Link_Card_cardId` ON `DB_Media_Link_Card` (`cardId`)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_DB_Tag_tag_name` ON `DB_Tag` (`tag_name`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_DB_Tag_Link_Card_tagId` ON `DB_Tag_Link_Card` (`tagId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_DB_Tag_Link_Card_cardId` ON `DB_Tag_Link_Card` (`cardId`)");
        }

        private boolean exists(SupportSQLiteDatabase database, String query) {
            try (Cursor cursor = database.query(query)) {
                return cursor.moveToFirst();
            }
        }

        private long lastInsertId(SupportSQLiteDatabase database) {
            try (Cursor cursor = database.query("SELECT last_insert_rowid()")) {
                cursor.moveToFirst();
                return cursor.getLong(0);
            }
        }

        // Copies all rows into a new table with the given definition and replaces the old table by it
        private void recreateTable(SupportSQLiteDatabase database, String table, String createNewTable, String columns) {
            database.execSQL(createNewTable);
            database.execSQL("INSERT INTO `_new_" + table + "` (" + columns + ") SELECT " + columns + " FROM `" + table + "`");
            database.execSQL("DROP TABLE `" + table + "`");
            database.execSQL("ALTER TABLE `_new_" + table + "` RENAME TO `" + table + "`");
        }
    };

    public AppDatabase get(Context context) {
        return Room.databaseBuilder(
                        context,
                        AppDatabase.class, Globals.DB_NAME)
                // Queries are executed synchronously throughout the app
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12)
                .build();
    }
}
