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

    @Query("SELECT COUNT(*) FROM db_pack WHERE collection=:cid")
    fun countPacksInCollection(cid: Int): Int

    @Query("SELECT EXISTS (SELECT 1 FROM db_pack WHERE uid=:pid LIMIT 1)")
    fun existsPack(pid: Int): Boolean

    @get:Query("SELECT * FROM db_pack ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val all: MutableList<DB_Pack?>?

    @get:Query("SELECT *, (SELECT COUNT(*) FROM db_card WHERE pack=db_pack.uid) AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val allWithMeta: MutableList<DB_Pack_With_Meta?>?

    @get:Query("SELECT *, -1 AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val allWithMetaNoCounter: MutableList<DB_Pack_With_Meta?>?

    @Query("SELECT * FROM db_pack WHERE collection=:cid ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllByCollection(cid: Int): MutableList<DB_Pack?>?

    @Query("SELECT * FROM db_pack WHERE collection=:cid AND name=:name AND `desc`=:desc ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllByCollectionAndNameAndDesc(
        cid: Int,
        name: String?,
        desc: String?
    ): MutableList<DB_Pack?>?

    @Query("SELECT *, (SELECT COUNT(*) FROM db_card WHERE pack=db_pack.uid) AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack WHERE collection=:cid ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllByCollectionWithMeta(cid: Int): MutableList<DB_Pack_With_Meta?>?

    @Query("SELECT *, -1 AS counter, (SELECT name FROM db_collection WHERE uid=db_pack.collection) AS collectionName FROM db_pack WHERE collection=:cid ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllByCollectionWithMetaNoCounter(cid: Int): MutableList<DB_Pack_With_Meta?>?

    // Raw cursor used by the CSV export
    @Query("SELECT * FROM db_pack WHERE collection=:cid")
    fun getAllExportByCollection(cid: Int): Cursor?

    @Query("SELECT uid FROM db_pack WHERE collection=:cid ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllIdsByCollection(cid: Int): MutableList<Int?>?

    @Query("SELECT * FROM db_pack WHERE uid=:pid LIMIT 1")
    fun getOne(pid: Int): DB_Pack?

    @Update
    fun update(pack: DB_Pack?)

    @Insert
    fun insert(pack: DB_Pack?): Long

    @Delete
    fun delete(pack: DB_Pack?)
}
