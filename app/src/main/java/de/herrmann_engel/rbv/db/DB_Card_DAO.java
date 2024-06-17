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

    @Query("SELECT COUNT(*) FROM db_card WHERE pack=:pid")
    int countCardsInPack(int pid);

    @Query("SELECT COUNT(*) FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:cid)")
    int countCardsInCollection(int cid);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card")
    List<DB_Card_With_Meta> getAllWithMeta();

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid")
    List<DB_Card_With_Meta> getAllByPackWithMeta(int pid);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:cid)")
    List<DB_Card_With_Meta> getAllByCollectionWithMeta(int cid);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known>=:progress")
    List<DB_Card_With_Meta> getAllGreaterEqualWithMeta(int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known<=:progress")
    List<DB_Card_With_Meta> getAllLessEqualWithMeta(int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE last_repetition>=:date")
    List<DB_Card_With_Meta> getAllNewerEqualRepetitionWithMeta(long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE last_repetition<:date")
    List<DB_Card_With_Meta> getAllOlderRepetitionWithMeta(long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known>=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllGreaterEqualNewerEqualRepetitionWithMeta(int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known<=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllLessEqualNewerEqualRepetitionWithMeta(int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known>=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllGreaterEqualOlderRepetitionWithMeta(int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE known<=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllLessEqualOlderRepetitionWithMeta(int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND known>=:progress")
    List<DB_Card_With_Meta> getAllGreaterEqualWithMeta(int pid, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND known<=:progress")
    List<DB_Card_With_Meta> getAllLessEqualWithMeta(int pid, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllNewerEqualRepetitionWithMeta(int pid, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllOlderRepetitionWithMeta(int pid, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND known>=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllGreaterEqualNewerEqualRepetitionWithMeta(int pid, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND known<=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllLessEqualNewerEqualRepetitionWithMeta(int pid, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND known>=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllGreaterEqualOlderRepetitionWithMeta(int pid, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND known<=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllLessEqualOlderRepetitionWithMeta(int pid, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId)")
    List<DB_Card_With_Meta> getAllByTagWithMeta(int tagId);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND pack=:pid")
    List<DB_Card_With_Meta> getAllByPackAndTagWithMeta(int pid, int tagId);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known>=:progress")
    List<DB_Card_With_Meta> getAllByTagGreaterEqualWithMeta(int tagId, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known<=:progress")
    List<DB_Card_With_Meta> getAllByTagLessEqualWithMeta(int tagId, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByTagNewerEqualRepetitionWithMeta(int tagId, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByTagOlderRepetitionWithMeta(int tagId, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known>=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByTagGreaterEqualNewerEqualRepetitionWithMeta(int tagId, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known<=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByTagLessEqualNewerEqualRepetitionWithMeta(int tagId, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known>=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByTagGreaterEqualOlderRepetitionWithMeta(int tagId, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known<=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByTagLessEqualOlderRepetitionWithMeta(int tagId, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known>=:progress")
    List<DB_Card_With_Meta> getAllByTagGreaterEqualWithMeta(int pid, int tagId, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known<=:progress")
    List<DB_Card_With_Meta> getAllByTagLessEqualWithMeta(int pid, int tagId, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByTagNewerEqualRepetitionWithMeta(int pid, int tagId, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByTagOlderRepetitionWithMeta(int pid, int tagId, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known>=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByTagGreaterEqualNewerEqualRepetitionWithMeta(int pid, int tagId, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known<=:progress AND last_repetition>=:date")
    List<DB_Card_With_Meta> getAllByTagLessEqualNewerEqualRepetitionWithMeta(int pid, int tagId, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known>=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByTagGreaterEqualOlderRepetitionWithMeta(int pid, int tagId, int progress, long date);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE pack=:pid AND db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId=:tagId) AND known<=:progress AND last_repetition<:date")
    List<DB_Card_With_Meta> getAllByTagLessEqualOlderRepetitionWithMeta(int pid, int tagId, int progress, long date);

    @Query("SELECT db_card.*, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card INNER JOIN db_media_link_card ON db_card.uid = db_media_link_card.cardId WHERE db_media_link_card.fileId=:fileId")
    List<DB_Card_With_Meta> getAllByMedia(int fileId);

    @Query("SELECT * FROM db_card WHERE uid=:cid")
    List<DB_Card> getOne(int cid);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (group_concat(emoji,' ') || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card WHERE uid=:cid")
    DB_Card_With_Meta getOneWithMeta(int cid);

    @Query("SELECT uid FROM db_card WHERE pack=:pid AND front=:front AND back=:back AND notes=:notes LIMIT 1")
    int getOneIdByPackAndFrontAndBackAndNotes(int pid, String front, String back, String notes);

    @Query("SELECT * FROM db_card")
    Cursor getAllExport();

    @Query("DELETE FROM db_card WHERE pack=:pid")
    void deleteAllByPack(int pid);

    @Update
    void update(DB_Card card);

    @Insert
    long insert(DB_Card card);

    @Delete
    void delete(DB_Card card);

}
