package de.herrmann_engel.rbv.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DB_Card_DAO {

    @Query("SELECT EXISTS (SELECT 1 FROM db_card LIMIT 1)")
    boolean hasCards();

    @Query("SELECT COUNT(*) FROM db_card")
    int countCards();

    @Query("SELECT COUNT(*) FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:cid)")
    int countCardsInCollection(int cid);

    @Query("SELECT COUNT(*) FROM db_card WHERE pack=:pid")
    int countCardsInPack(int pid);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card")
    List<DB_Card_With_Meta> getAllWithMeta();

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:cid)")
    List<DB_Card_With_Meta> getAllByCollectionWithMeta(int cid);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid")
    List<DB_Card_With_Meta> getAllByPackWithMeta(int pid);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known>=:progress")
    List<DB_Card_With_Meta> getAllGreaterEqualWithMeta(int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known<=:progress")
    List<DB_Card_With_Meta> getAllLessEqualWithMeta(int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE last_repetition>=:date")
    List<DB_Card_With_Meta> getAllNewerEqualRepetitionWithMeta(long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE last_repetition<:date")
    List<DB_Card_With_Meta> getAllOlderRepetitionWithMeta(long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known>=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllGreaterEqualNewerEqualRepetitionWithMeta(int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known<=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllLessEqualNewerEqualRepetitionWithMeta(int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known>=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllGreaterEqualOlderRepetitionWithMeta(int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known<=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllLessEqualOlderRepetitionWithMeta(int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds)")
    List<DB_Card_With_Meta> getAllByPacksWithMeta(List<Integer> packIds);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND known>=:progress")
    List<DB_Card_With_Meta> getAllByPacksGreaterEqualWithMeta(List<Integer> packIds, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND known<=:progress")
    List<DB_Card_With_Meta> getAllByPacksLessEqualWithMeta(List<Integer> packIds, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds)AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByPacksNewerEqualRepetitionWithMeta(List<Integer> packIds, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByPacksOlderRepetitionWithMeta(List<Integer> packIds, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND known>=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByPacksGreaterEqualNewerEqualRepetitionWithMeta(List<Integer> packIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND known<=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByPacksLessEqualNewerEqualRepetitionWithMeta(List<Integer> packIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND known>=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByPacksGreaterEqualOlderRepetitionWithMeta(List<Integer> packIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND known<=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByPacksLessEqualOlderRepetitionWithMeta(List<Integer> packIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds))")
    List<DB_Card_With_Meta> getAllByTagsWithMeta(List<Integer> tagIds);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known>=:progress")
    List<DB_Card_With_Meta> getAllByTagsGreaterEqualWithMeta(List<Integer> tagIds, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known<=:progress")
    List<DB_Card_With_Meta> getAllByTagsLessEqualWithMeta(List<Integer> tagIds, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByTagsNewerEqualRepetitionWithMeta(List<Integer> tagIds, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByTagsOlderRepetitionWithMeta(List<Integer> tagIds, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known>=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByTagsGreaterEqualNewerEqualRepetitionWithMeta(List<Integer> tagIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known<=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByTagsLessEqualNewerEqualRepetitionWithMeta(List<Integer> tagIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known>=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByTagsGreaterEqualOlderRepetitionWithMeta(List<Integer> tagIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known<=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByTagsLessEqualOlderRepetitionWithMeta(List<Integer> tagIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds))")
    List<DB_Card_With_Meta> getAllByPacksAndTagsWithMeta(List<Integer> packIds, List<Integer> tagIds);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known>=:progress")
    List<DB_Card_With_Meta> getAllByPackAndTagsGreaterEqualWithMeta(List<Integer> packIds, List<Integer> tagIds, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known<=:progress")
    List<DB_Card_With_Meta> getAllByPackAndTagsLessEqualWithMeta(List<Integer> packIds, List<Integer> tagIds, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByPackAndTagsNewerEqualRepetitionWithMeta(List<Integer> packIds, List<Integer> tagIds, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByPackAndTagsOlderRepetitionWithMeta(List<Integer> packIds, List<Integer> tagIds, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known>=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByPackAndTagsGreaterEqualNewerEqualRepetitionWithMeta(List<Integer> packIds, List<Integer> tagIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known<=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByPackAndTagsLessEqualNewerEqualRepetitionWithMeta(List<Integer> packIds, List<Integer> tagIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known>=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByPackAndTagsGreaterEqualOlderRepetitionWithMeta(List<Integer> packIds, List<Integer> tagIds, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (:packIds) AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)) AND known<=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByPackAndTagsLessEqualOlderRepetitionWithMeta(List<Integer> packIds, List<Integer> tagIds, int progress, long date);

    @Query("SELECT db_card.*, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card INNER JOIN db_media_link_card ON db_card.uid = db_media_link_card.cardId WHERE db_media_link_card.fileId=:fileId")
    List<DB_Card_With_Meta> getAllByMedia(int fileId);

    @Query("SELECT * FROM db_card")
    Cursor getAllExport();

    @Query("SELECT * FROM db_card WHERE uid=:cardId LIMIT 1")
    DB_Card getOne(int cardId);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(emoji,' ') IS NOT NULL THEN group_concat(emoji,' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE uid=:cardId LIMIT 1")
    DB_Card_With_Meta getOneWithMeta(int cardId);

    @Query("SELECT uid FROM db_card WHERE pack=:pid AND front=:front AND back=:back AND notes=:notes LIMIT 1")
    int getOneIdByPackAndFrontAndBackAndNotes(int pid, String front, String back, String notes);

    @Query("DELETE FROM db_card WHERE pack=:pid")
    void deleteAllByPack(int pid);

    @Update
    void update(DB_Card card);

    @Insert
    long insert(DB_Card card);

    @Delete
    void delete(DB_Card card);

}
