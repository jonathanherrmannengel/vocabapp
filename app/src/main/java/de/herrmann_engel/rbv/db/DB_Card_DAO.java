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

    @Query("SELECT * FROM db_card")
    List<DB_Card> getAll();

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor FROM db_card")
    List<DB_Card_With_Meta> getAllWithMeta();

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor FROM db_card WHERE pack=:pid")
    List<DB_Card_With_Meta> getAllByPackWithMeta(int pid);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor FROM db_card WHERE known>=:progress")
    List<DB_Card_With_Meta> getAllGreaterEqualWithMeta(int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor FROM db_card WHERE known<=:progress")
    List<DB_Card_With_Meta> getAllLessEqualWithMeta(int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor FROM db_card WHERE pack=:pid AND known>=:progress")
    List<DB_Card_With_Meta> getAllGreaterEqualWithMeta(int pid, int progress);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor FROM db_card WHERE pack=:pid AND known<=:progress")
    List<DB_Card_With_Meta> getAllLessEqualWithMeta(int pid, int progress);

    @Query("SELECT * FROM db_card WHERE uid=:cid")
    List<DB_Card> getOne(int cid);

    @Query("SELECT *, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor FROM db_card WHERE uid=:cid")
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
