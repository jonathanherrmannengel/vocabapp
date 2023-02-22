package de.herrmann_engel.rbv.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DB_Media_Link_Card_DAO {

    @Query("SELECT * FROM db_media_link_card")
    List<DB_Media_Link_Card> getAll();

    @Query("SELECT * FROM db_media_link_card WHERE cardId=:card")
    List<DB_Media_Link_Card> getAllByCard(int card);

    @Query("SELECT * FROM db_media_link_card WHERE fileId=:file")
    List<DB_Media_Link_Card> getAllByMedia(int file);

    @Query("SELECT fileId FROM db_media_link_card WHERE cardId=:card")
    List<Integer> getAllMediaIdsByCard(int card);

    @Query("SELECT cardId FROM db_media_link_card WHERE fileId=:file")
    List<Integer> getAllCardIdsByMedia(int file);

    @Query("SELECT EXISTS (SELECT 1 FROM db_media_link_card WHERE fileId=:file AND cardId=:card LIMIT 1)")
    boolean existsMediaLinkCard(int file, int card);

    @Query("SELECT * FROM db_media_link_card")
    Cursor getAllExport();

    @Query("SELECT * FROM db_media_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collection))")
    Cursor getAllExportByCollection(int collection);

    @Query("DELETE FROM db_media_link_card WHERE fileId=:file AND cardId=:card")
    void deleteMediaLinkCard(int file, int card);

    @Query("DELETE FROM db_media_link_card WHERE cardId=:card")
    void deleteMediaLinksByCard(int card);

    @Insert
    long insert(DB_Media_Link_Card dbMediaLinkCard);
}
