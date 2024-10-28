package de.herrmann_engel.rbv.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DB_Media_DAO {

    @Query("SELECT EXISTS (SELECT 1 FROM db_media LIMIT 1)")
    boolean hasMedia();

    @Query("SELECT * FROM db_media ORDER BY file COLLATE NOCASE ASC")
    List<DB_Media> getAll();

    @Query("SELECT * FROM db_media WHERE db_media.uid IN (SELECT fileId FROM db_media_link_card WHERE cardId=:card) ORDER BY file COLLATE NOCASE ASC")
    List<DB_Media> getAllByCard(int card);

    @Query("SELECT * FROM db_media WHERE file=:file LIMIT 1")
    DB_Media getSingleMedia(String file);

    @Query("SELECT * FROM db_media WHERE uid=:id LIMIT 1")
    DB_Media getSingleMedia(int id);

    @Query("SELECT EXISTS (SELECT 1 FROM db_media WHERE file=:file LIMIT 1)")
    boolean existsMedia(String file);

    @Query("SELECT * FROM db_media")
    Cursor getAllExport();

    @Query("SELECT * FROM db_media WHERE uid IN (SELECT fileId FROM db_media_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collection)))")
    Cursor getAllExportByCollection(int collection);

    @Query("DELETE FROM db_media WHERE uid=:id")
    void deleteMedia(int id);

    @Insert
    long insert(DB_Media media);
}
