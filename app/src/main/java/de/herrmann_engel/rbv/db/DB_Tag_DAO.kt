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
interface DB_Tag_DAO {
    @Query("SELECT EXISTS (SELECT 1 FROM db_tag LIMIT 1)")
    fun hasTags(): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM db_tag WHERE tag_name=:tagName LIMIT 1)")
    fun existsTag(tagName: String): Boolean

    @get:Query("SELECT * FROM db_tag ORDER BY tag_name ASC")
    val all: List<DB_Tag>

    @Query("SELECT * FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=:card) ORDER BY tag_name ASC")
    fun getAllByCard(card: Int): MutableList<DB_Tag>

    @get:Query("SELECT * FROM db_tag")
    val allExport: Cursor

    @Query("SELECT * FROM db_tag WHERE uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collection)))")
    fun getAllExportByCollection(collection: Int): Cursor

    @Query("SELECT * FROM db_tag WHERE tag_name=:tagName LIMIT 1")
    fun getSingleTag(tagName: String): DB_Tag?

    // Removes the given tags if they are not assigned to any card; returns the number of removed
    // tags. Only the given tags are checked, so tags of a running import that are not linked yet
    // are not affected.
    @Query("DELETE FROM db_tag WHERE uid IN (:tags) AND NOT EXISTS (SELECT 1 FROM db_tag_link_card WHERE db_tag_link_card.tagId=db_tag.uid)")
    fun deleteUnusedTags(tags: List<Int>): Int

    @Update
    fun update(dbTag: DB_Tag)

    @Insert
    fun insert(dbTag: DB_Tag): Long
}
