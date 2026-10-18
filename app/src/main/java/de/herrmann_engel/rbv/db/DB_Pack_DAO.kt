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
interface DB_Pack_DAO {
    @Query("SELECT COUNT(*) FROM db_pack")
    fun countPacks(): Int

    @Query("SELECT COUNT(*) FROM db_pack WHERE collection=:collectionId")
    fun countPacksInCollection(collectionId: Int): Int

    @Query("SELECT EXISTS (SELECT 1 FROM db_pack WHERE uid=:packId LIMIT 1)")
    fun existsPack(packId: Int): Boolean

    @get:Query("SELECT * FROM db_pack ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val all: List<DB_Pack>

    @get:Query("SELECT *, (SELECT COUNT(*) FROM db_card WHERE pack=db_pack.uid) AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val allWithMeta: MutableList<DB_Pack_With_Meta>

    @get:Query("SELECT *, -1 AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val allWithMetaNoCounter: MutableList<DB_Pack_With_Meta>

    @Query("SELECT * FROM db_pack WHERE collection=:collectionId ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllByCollection(collectionId: Int): List<DB_Pack>

    @Query("SELECT * FROM db_pack WHERE collection=:collectionId AND name=:name AND `desc`=:desc ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllByCollectionAndNameAndDesc(
        collectionId: Int,
        name: String,
        desc: String?
    ): List<DB_Pack>

    @Query("SELECT *, (SELECT COUNT(*) FROM db_card WHERE pack=db_pack.uid) AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack WHERE collection=:collectionId ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllByCollectionWithMeta(collectionId: Int): MutableList<DB_Pack_With_Meta>

    @Query("SELECT *, -1 AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack WHERE collection=:collectionId ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllByCollectionWithMetaNoCounter(collectionId: Int): MutableList<DB_Pack_With_Meta>

    // Raw cursor used by the CSV export
    @Query("SELECT * FROM db_pack WHERE collection=:collectionId")
    fun getAllExportByCollection(collectionId: Int): Cursor

    @Query("SELECT * FROM db_pack WHERE uid=:packId LIMIT 1")
    fun getOne(packId: Int): DB_Pack?

    @Update
    fun update(pack: DB_Pack)

    @Insert
    fun insert(pack: DB_Pack): Long

    @Delete
    fun delete(pack: DB_Pack)
}
