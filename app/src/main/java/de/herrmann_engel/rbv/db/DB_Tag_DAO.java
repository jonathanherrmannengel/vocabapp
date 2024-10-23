package de.herrmann_engel.rbv.db;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DB_Tag_DAO {

    @Query("SELECT EXISTS (SELECT 1 FROM db_tag LIMIT 1)")
    boolean hasTags();

    @Query("SELECT EXISTS (SELECT 1 FROM db_tag WHERE tag_name=:tagName LIMIT 1)")
    boolean existsTag(String tagName);

    @Query("SELECT * FROM db_tag ORDER BY tag_name ASC")
    List<DB_Tag> getAll();

    @Query("SELECT * FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=:card) ORDER BY tag_name ASC")
    List<DB_Tag> getAllByCard(int card);

    @Query("SELECT * FROM db_tag")
    Cursor getAllExport();

    @Query("SELECT * FROM db_tag WHERE uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collection)))")
    Cursor getAllExportByCollection(int collection);

    @Query("SELECT * FROM db_tag WHERE tag_name=:tagName LIMIT 1")
    DB_Tag getSingleTag(String tagName);

    @Query("DELETE FROM db_tag WHERE NOT EXISTS (SELECT 1 FROM db_tag_link_card WHERE db_tag_link_card.tagId=db_tag.uid)")
    void deleteDeadTags();

    @Update
    void update(DB_Tag dbTag);

    @Insert
    long insert(DB_Tag dbTag);
}
