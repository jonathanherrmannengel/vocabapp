package de.herrmann_engel.rbv.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data access for cards.
 * <p>
 * All "WithMeta" queries additionally select the color of the card's pack and a string of the
 * card's tag emojis and names (see SELECT_WITH_META).
 */
@Dao
public interface DB_Card_DAO {

    // Cards with the color of their pack and their tags' emojis and names (used for search and display)
    String SELECT_WITH_META = "SELECT db_card.*, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card";

    @Query("SELECT EXISTS (SELECT 1 FROM db_card LIMIT 1)")
    boolean hasCards();

    @Query("SELECT COUNT(*) FROM db_card")
    int countCards();

    @Query("SELECT COUNT(*) FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:cid)")
    int countCardsInCollection(int cid);

    @Query("SELECT COUNT(*) FROM db_card WHERE pack=:pid")
    int countCardsInPack(int pid);

    @Query(SELECT_WITH_META)
    List<DB_Card_With_Meta> getAllWithMeta();

    @Query(SELECT_WITH_META + " WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:cid)")
    List<DB_Card_With_Meta> getAllByCollectionWithMeta(int cid);

    @Query(SELECT_WITH_META + " WHERE pack=:pid")
    List<DB_Card_With_Meta> getAllByPackWithMeta(int pid);

    /**
     * Cards matching the advanced search filters. Each filter can be switched off.
     *
     * @param allPacks        true: cards of all packs, false: only cards of the packs in packIds
     * @param allTags         true: no tag filter, false: only cards with at least one tag of tagIds
     * @param filterProgress  false: no progress filter
     * @param progressGreater true: progress >= progress, false: progress <= progress
     * @param filterRepetition false: no filter by date of last repetition
     * @param repetitionOlder true: last repetition before date, false: at or after date
     */
    @Query(SELECT_WITH_META + " WHERE (:allPacks OR pack IN (:packIds))"
            + " AND (:allTags OR db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)))"
            + " AND (NOT :filterProgress OR (CASE WHEN :progressGreater THEN known >= :progress ELSE known <= :progress END))"
            + " AND (NOT :filterRepetition OR (CASE WHEN :repetitionOlder THEN last_repetition < :date ELSE last_repetition >= :date END))")
    List<DB_Card_With_Meta> getAllFilteredWithMeta(boolean allPacks, List<Integer> packIds, boolean allTags, List<Integer> tagIds, boolean filterProgress, boolean progressGreater, int progress, boolean filterRepetition, boolean repetitionOlder, long date);

    // Cards linked to the given media file
    @Query(SELECT_WITH_META + " INNER JOIN db_media_link_card ON db_card.uid = db_media_link_card.cardId WHERE db_media_link_card.fileId=:fileId")
    List<DB_Card_With_Meta> getAllByMedia(int fileId);

    // Raw cursor used by the CSV export
    @Query("SELECT * FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collection)")
    Cursor getAllExportByCollection(int collection);

    @Query("SELECT * FROM db_card WHERE uid=:cardId LIMIT 1")
    DB_Card getOne(int cardId);

    @Query(SELECT_WITH_META + " WHERE db_card.uid=:cardId LIMIT 1")
    DB_Card_With_Meta getOneWithMeta(int cardId);

    // Used by the import to find already existing cards
    @Query("SELECT uid FROM db_card WHERE pack=:pid AND front=:front AND back=:back AND notes=:notes LIMIT 1")
    int getOneIdByPackAndFrontAndBackAndNotes(int pid, String front, String back, String notes);

    @Update
    void update(DB_Card card);

    @Insert
    long insert(DB_Card card);

    @Delete
    void delete(DB_Card card);

}
