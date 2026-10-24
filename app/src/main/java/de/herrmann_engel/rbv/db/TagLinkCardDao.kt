package de.herrmann_engel.rbv.db

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data access for the links between tags and cards.
 */
@Dao
interface TagLinkCardDao {
    @Query("SELECT EXISTS (SELECT 1 FROM db_tag_link_card WHERE tagId=:tagId AND cardId=:cardId LIMIT 1)")
    fun existsTagLinkCard(tagId: Int, cardId: Int): Boolean

    @get:Query("SELECT * FROM db_tag_link_card")
    val allExport: Cursor

    @Query("SELECT * FROM db_tag_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collectionId))")
    fun getAllExportByCollection(collectionId: Int): Cursor

    @Query("DELETE FROM db_tag_link_card WHERE tagId=:tagId AND cardId=:cardId")
    fun deleteTagLinkCard(tagId: Int, cardId: Int)

    @Query("SELECT tagId FROM db_tag_link_card WHERE cardId=:cardId")
    fun getAllTagIdsByCard(cardId: Int): List<Int>

    @Query("SELECT DISTINCT tagId FROM db_tag_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack=:packId)")
    fun getAllTagIdsByPack(packId: Int): List<Int>

    @Insert
    fun insert(dbTagLinkCard: DbTagLinkCard): Long
}
