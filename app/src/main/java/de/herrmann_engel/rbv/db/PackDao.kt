package de.herrmann_engel.rbv.db

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Data access for packs. Lists are sorted by name (case-insensitive), newest first for equal names.
 */
@Dao
interface PackDao {
    @Query("SELECT COUNT(*) FROM db_pack")
    fun countPacks(): Int

    @Query("SELECT EXISTS (SELECT 1 FROM db_pack WHERE uid=:packId LIMIT 1)")
    fun existsPack(packId: Int): Boolean

    @get:Query("SELECT *, (SELECT COUNT(*) FROM db_card WHERE pack=db_pack.uid) AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack ORDER BY name COLLATE LOCALIZED ASC, uid DESC")
    val allWithMeta: MutableList<PackWithMeta>

    @Query("SELECT * FROM db_pack WHERE collection=:collectionId ORDER BY name COLLATE LOCALIZED ASC, uid DESC")
    fun getAllByCollection(collectionId: Int): List<DbPack>

    @Query("SELECT * FROM db_pack WHERE collection=:collectionId AND name=:name AND `desc`=:desc ORDER BY name COLLATE LOCALIZED ASC, uid DESC")
    fun getAllByCollectionAndNameAndDesc(
        collectionId: Int,
        name: String,
        desc: String
    ): List<DbPack>

    @Query("SELECT *, (SELECT COUNT(*) FROM db_card WHERE pack=db_pack.uid) AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack WHERE collection=:collectionId ORDER BY name COLLATE LOCALIZED ASC, uid DESC")
    fun getAllByCollectionWithMeta(collectionId: Int): MutableList<PackWithMeta>

    // Raw cursor used by the CSV export
    @Query("SELECT * FROM db_pack WHERE collection=:collectionId")
    fun getAllExportByCollection(collectionId: Int): Cursor

    // Languages chosen for the front or back sides (see DbHelperGet.getUsedLanguageTags)
    @Query(
        "SELECT front_language FROM db_pack WHERE front_language IS NOT NULL"
                + " UNION SELECT back_language FROM db_pack WHERE back_language IS NOT NULL"
    )
    fun getLanguageTags(): List<String>

    @Query("SELECT * FROM db_pack WHERE uid=:packId LIMIT 1")
    fun getOne(packId: Int): DbPack?

    @Update
    fun update(pack: DbPack)

    @Insert
    fun insert(pack: DbPack): Long

    @Delete
    fun delete(pack: DbPack)
}
