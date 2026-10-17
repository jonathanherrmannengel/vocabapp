package de.herrmann_engel.rbv.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data access for the links between tags and cards.
 */
@Dao
public interface DB_Tag_Link_Card_DAO {
    @Query("SELECT EXISTS (SELECT 1 FROM db_tag_link_card WHERE tagId=:tag AND cardId=:card LIMIT 1)")
    boolean existsTagLinkCard(int tag, int card);

    @Query("SELECT * FROM db_tag_link_card")
    Cursor getAllExport();

    @Query("SELECT * FROM db_tag_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collection))")
    Cursor getAllExportByCollection(int collection);

    @Query("DELETE FROM db_tag_link_card WHERE tagId=:tag AND cardId=:card")
    void deleteTagLinkCard(int tag, int card);

    @Query("SELECT tagId FROM db_tag_link_card WHERE cardId=:card")
    List<Integer> getAllTagIdsByCard(int card);

    @Query("SELECT DISTINCT tagId FROM db_tag_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack=:pack)")
    List<Integer> getAllTagIdsByPack(int pack);

    @Insert
    long insert(DB_Tag_Link_Card dbTagLinkCard);
}
