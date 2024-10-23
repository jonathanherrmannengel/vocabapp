package de.herrmann_engel.rbv.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DB_Pack_DAO {

    @Query("SELECT COUNT(*) FROM db_pack")
    int countPacks();

    @Query("SELECT COUNT(*) FROM db_pack WHERE collection=:cid")
    int countPacksInCollection(int cid);

    @Query("SELECT * FROM db_pack ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Pack> getAll();

    @Query("SELECT * FROM db_pack WHERE collection=:cid ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Pack> getAll(int cid);

    @Query("SELECT * FROM db_pack WHERE collection=:cid AND name=:name AND `desc`=:desc ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Pack> getAllByCollectionAndNameAndDesc(int cid, String name, String desc);

    @Query("SELECT *, (SELECT COUNT(*) FROM db_card WHERE pack=db_pack.uid) AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Pack_With_Meta> getAllWithMeta();

    @Query("SELECT *, -1 AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Pack_With_Meta> getAllWithMetaNoCounter();

    @Query("SELECT *, (SELECT COUNT(*) FROM db_card WHERE pack=db_pack.uid) AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack WHERE collection=:cid ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Pack_With_Meta> getAllByCollectionWithMeta(int cid);

    @Query("SELECT *, -1 AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack WHERE collection=:cid ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Pack_With_Meta> getAllByCollectionWithMetaNoCounter(int cid);

    @Query("SELECT uid FROM db_pack WHERE collection=:cid ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<Integer> getAllIDs(int cid);

    @Query("SELECT * FROM db_pack WHERE uid=:pid LIMIT 1")
    DB_Pack getOne(int pid);

    @Query("SELECT * FROM db_pack WHERE collection=:cid")
    Cursor getAllExportByCollection(int cid);

    @Update
    void update(DB_Pack pack);

    @Insert
    long insert(DB_Pack pack);

    @Delete
    void delete(DB_Pack pack);

}
