package de.herrmann_engel.rbv.db;

import android.content.Context;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import de.herrmann_engel.rbv.Globals;

public class AppDatabaseBuilder {
    private final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_pack ADD COLUMN colors INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE db_cards RENAME TO db_card");
        }
    };
    private final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_card ADD COLUMN notes TEXT");
        }
    };
    private final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_pack ADD COLUMN collection INTEGER NOT NULL DEFAULT 1");
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `db_collection` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `desc` TEXT, `date` INTEGER NOT NULL)");
            database.execSQL(
                    "INSERT INTO db_collection (name,`desc`,uid, date) VALUES ('default', 'default', 1, DATETIME())");
        }
    };
    private final Migration MIGRATION_4_6 = new Migration(4, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_collection ADD COLUMN colors INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE db_pack ADD COLUMN emoji TEXT");
            database.execSQL("ALTER TABLE db_collection ADD COLUMN emoji TEXT");
        }
    };
    private final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `db_media` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `file` TEXT, `mime` TEXT)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `db_media_link_card` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fileId` INTEGER NOT NULL, `cardId` INTEGER NOT NULL)");
        }
    };
    private final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE db_card ADD COLUMN last_repetition INTEGER DEFAULT 0 NOT NULL");
        }
    };

    public AppDatabase get(Context context) {
        return Room.databaseBuilder(
                        context,
                        AppDatabase.class, Globals.DB_NAME)
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_6, MIGRATION_6_7, MIGRATION_7_8)
                .build();
    }
}
