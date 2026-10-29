package de.herrmann_engel.rbv.db

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Data access for tags.
 */
@Dao
interface TagDao {
    @get:Query("SELECT * FROM db_tag ORDER BY tag_name ASC")
    val all: List<DbTag>

    @Query("SELECT * FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=:cardId) ORDER BY tag_name ASC")
    fun getAllByCard(cardId: Int): MutableList<DbTag>

    @get:Query("SELECT * FROM db_tag")
    val allExport: Cursor

    @Query("SELECT * FROM db_tag WHERE uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collectionId)))")
    fun getAllExportByCollection(collectionId: Int): Cursor

    @Query("SELECT * FROM db_tag WHERE tag_name=:tagName LIMIT 1")
    fun getSingleTag(tagName: String): DbTag?

    // Removes the given tags if they are not assigned to any card; returns the number of removed
    // tags. Only the given tags are checked, so tags of a running import that are not linked yet
    // are not affected.
    @Query("DELETE FROM db_tag WHERE uid IN (:tagIds) AND NOT EXISTS (SELECT 1 FROM db_tag_link_card WHERE db_tag_link_card.tagId=db_tag.uid)")
    fun deleteUnusedTags(tagIds: List<Int>): Int

    @Update
    fun update(dbTag: DbTag)

    @Insert
    fun insert(dbTag: DbTag): Long
}
