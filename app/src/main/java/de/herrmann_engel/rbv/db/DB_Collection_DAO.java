package de.herrmann_engel.rbv.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DB_Collection_DAO {
    @Query("SELECT EXISTS (SELECT 1 FROM db_collection LIMIT 1)")
    boolean hasCollections();

    @Query("SELECT COUNT(*) FROM db_collection")
    int countCollections();

    @Query("SELECT * FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Collection> getAll();

    @Query("SELECT * FROM db_collection WHERE name=:name ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Collection> getAllByName(String name);

    @Query("SELECT *, (SELECT COUNT(*) FROM db_pack WHERE collection=db_collection.uid) AS counter FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Collection_With_Meta> getAllWithMeta();

    @Query("SELECT *, -1 AS counter FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<DB_Collection_With_Meta> getAllWithMetaNoCounter();

    @Query("SELECT uid FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    List<Integer> getAllIds();

    @Query("SELECT * FROM db_collection WHERE uid=:cid LIMIT 1")
    DB_Collection getOne(int cid);

    @Query("SELECT * FROM db_collection WHERE uid=:cid")
    Cursor getOneExport(int cid);

    @Update
    void update(DB_Collection collection);

    @Insert
    long insert(DB_Collection collection);

    @Delete
    void delete(DB_Collection collection);

}
