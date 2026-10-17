package de.herrmann_engel.rbv.db

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data access for the links between tags and cards.
 */
@Dao
interface DB_Tag_Link_Card_DAO {
    @Query("SELECT EXISTS (SELECT 1 FROM db_tag_link_card WHERE tagId=:tag AND cardId=:card LIMIT 1)")
    fun existsTagLinkCard(tag: Int, card: Int): Boolean

    @get:Query("SELECT * FROM db_tag_link_card")
    val allExport: Cursor

    @Query("SELECT * FROM db_tag_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collection))")
    fun getAllExportByCollection(collection: Int): Cursor

    @Query("DELETE FROM db_tag_link_card WHERE tagId=:tag AND cardId=:card")
    fun deleteTagLinkCard(tag: Int, card: Int)

    @Query("SELECT tagId FROM db_tag_link_card WHERE cardId=:card")
    fun getAllTagIdsByCard(card: Int): List<Int>

    @Query("SELECT DISTINCT tagId FROM db_tag_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack=:pack)")
    fun getAllTagIdsByPack(pack: Int): List<Int>

    @Insert
    fun insert(dbTagLinkCard: DB_Tag_Link_Card): Long
}
