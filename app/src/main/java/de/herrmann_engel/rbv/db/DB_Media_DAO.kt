package de.herrmann_engel.rbv.db

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * Data access for media files.
 */
@Dao
interface DB_Media_DAO {
    @Query("SELECT EXISTS (SELECT 1 FROM db_media LIMIT 1)")
    fun hasMedia(): Boolean

    @get:Query("SELECT * FROM db_media ORDER BY file COLLATE NOCASE ASC")
    val all: List<DB_Media>

    @Query("SELECT * FROM db_media WHERE db_media.uid IN (SELECT fileId FROM db_media_link_card WHERE cardId=:cardId) ORDER BY file COLLATE NOCASE ASC")
    fun getAllByCard(cardId: Int): List<DB_Media>

    @Query("SELECT * FROM db_media WHERE file=:file LIMIT 1")
    fun getSingleMedia(file: String): DB_Media?

    @Query("SELECT * FROM db_media WHERE uid=:fileId LIMIT 1")
    fun getSingleMedia(fileId: Int): DB_Media?

    @Query("SELECT EXISTS (SELECT 1 FROM db_media WHERE file=:file LIMIT 1)")
    fun existsMedia(file: String): Boolean

    @get:Query("SELECT * FROM db_media")
    val allExport: Cursor

    @Query("SELECT * FROM db_media WHERE uid IN (SELECT fileId FROM db_media_link_card WHERE cardId IN (SELECT uid FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collectionId)))")
    fun getAllExportByCollection(collectionId: Int): Cursor

    @Query("DELETE FROM db_media WHERE uid=:fileId")
    fun deleteMedia(fileId: Int)

    // Removes the given media entries if they are not linked to any card; returns the number of
    // removed entries
    @Query("DELETE FROM db_media WHERE uid IN (:fileIds) AND NOT EXISTS (SELECT 1 FROM db_media_link_card WHERE db_media_link_card.fileId=db_media.uid)")
    fun deleteUnusedMedia(fileIds: List<Int>): Int

    @Insert
    fun insert(media: DB_Media): Long
}
