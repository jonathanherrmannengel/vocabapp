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

    @Query("SELECT * FROM db_card")
    List<DB_Card> getAll();

    @Query("SELECT * FROM db_card WHERE pack=:pid")
    List<DB_Card> getAll(int pid);

    @Query("SELECT * FROM db_card WHERE known>=:progress")
    List<DB_Card> getAllGreaterEqual(int progress);

    @Query("SELECT * FROM db_card WHERE known<=:progress")
    List<DB_Card> getAllLessEqual(int progress);

    @Query("SELECT * FROM db_card WHERE pack=:pid AND known>=:progress")
    List<DB_Card> getAllGreaterEqual(int pid, int progress);

    @Query("SELECT * FROM db_card WHERE pack=:pid AND known<=:progress")
    List<DB_Card> getAllLessEqual(int pid, int progress);

    @Query("SELECT * FROM db_card WHERE pack=:pid AND front=:front AND back=:back AND notes=:notes")
    List<DB_Card> getAllByPackAndFrontAndBackAndNotes(int pid, String front, String back, String notes);

    @Query("SELECT * FROM db_card WHERE uid=:cid")
    List<DB_Card> getOne(int cid);

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
