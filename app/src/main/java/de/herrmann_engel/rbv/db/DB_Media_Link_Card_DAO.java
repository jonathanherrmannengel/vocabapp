package de.herrmann_engel.rbv.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DB_Media_Link_Card_DAO {

    @Query("SELECT EXISTS (SELECT 1 FROM db_media_link_card  WHERE fileId=:file LIMIT 1)")
    boolean mediaHasLink(int file);

    @Query("SELECT fileId FROM db_media_link_card WHERE cardId=:card")
    List<Integer> getAllMediaIdsByCard(int card);

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

    @Query("DELETE FROM db_media_link_card WHERE fileId=:file")
    void deleteMediaLinksByMedia(int file);

    @Query("DELETE FROM db_media_link_card WHERE NOT EXISTS (SELECT 1 FROM db_media WHERE db_media.uid=db_media_link_card.fileId) OR NOT EXISTS (SELECT 1 FROM db_card WHERE db_card.uid=db_media_link_card.cardId)")
    void deleteDeadMediaLinks();

    @Insert
    long insert(DB_Media_Link_Card dbMediaLinkCard);
}
