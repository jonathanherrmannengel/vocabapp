package de.herrmann_engel.rbv.db

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data access for the links between media files and cards.
 */
@Dao
interface DB_Media_Link_Card_DAO {
    @Query("SELECT fileId FROM db_media_link_card WHERE cardId=:cardId")
    fun getAllMediaIdsByCard(cardId: Int): List<Int>

    @Query("SELECT DISTINCT fileId FROM db_media_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack=:packId)")
    fun getAllMediaIdsByPack(packId: Int): List<Int>

    @Query("SELECT EXISTS (SELECT 1 FROM db_media_link_card WHERE fileId=:fileId AND cardId=:cardId LIMIT 1)")
    fun existsMediaLinkCard(fileId: Int, cardId: Int): Boolean

    @get:Query("SELECT * FROM db_media_link_card")
    val allExport: Cursor

    @Query("SELECT * FROM db_media_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collectionId))")
    fun getAllExportByCollection(collectionId: Int): Cursor

    @Query("DELETE FROM db_media_link_card WHERE fileId=:fileId AND cardId=:cardId")
    fun deleteMediaLinkCard(fileId: Int, cardId: Int)

    @Insert
    fun insert(dbMediaLinkCard: DB_Media_Link_Card): Long
}
