package de.herrmann_engel.rbv;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DB_Pack_DAO {
        @Query("SELECT * FROM db_pack WHERE collection=:cid ORDER BY name ASC, uid DESC")
        List<DB_Pack> getAll(int cid);
        @Query("SELECT uid FROM db_pack WHERE collection=:cid ORDER BY name ASC, uid DESC")
        List<Integer> getAllIDs(int cid);
        @Query("SELECT * FROM db_pack WHERE uid=:pid")
        List<DB_Pack> getOne(int pid);
        @Query("SELECT * FROM db_pack WHERE collection=:cid")
        Cursor getAllExportByCollection(int cid);

        @Update
        void update(DB_Pack pack);

        @Insert
        long insert(DB_Pack pack);

        @Delete
        void delete(DB_Pack pack);

}
