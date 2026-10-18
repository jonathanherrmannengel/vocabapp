package de.herrmann_engel.rbv.db

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Data access for collections. Lists are sorted by name (case-insensitive), newest first for equal names.
 */
@Dao
interface DB_Collection_DAO {
    @Query("SELECT EXISTS (SELECT 1 FROM db_collection LIMIT 1)")
    fun hasCollections(): Boolean

    @Query("SELECT COUNT(*) FROM db_collection")
    fun countCollections(): Int

    @Query("SELECT EXISTS (SELECT 1 FROM db_collection WHERE uid=:collectionId LIMIT 1)")
    fun existsCollection(collectionId: Int): Boolean

    @get:Query("SELECT * FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val all: List<DB_Collection>

    @Query("SELECT * FROM db_collection WHERE name=:name ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllByName(name: String): List<DB_Collection>

    @get:Query("SELECT *, (SELECT COUNT(*) FROM db_pack WHERE collection=db_collection.uid) AS counter FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val allWithMeta: MutableList<DB_Collection_With_Meta>

    @get:Query("SELECT *, -1 AS counter FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val allWithMetaNoCounter: MutableList<DB_Collection_With_Meta>

    @get:Query("SELECT uid FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val allIds: List<Int>

    @Query("SELECT * FROM db_collection WHERE uid=:collectionId LIMIT 1")
    fun getOne(collectionId: Int): DB_Collection?

    // Raw cursor used by the CSV export
    @Query("SELECT * FROM db_collection WHERE uid=:collectionId")
    fun getOneExport(collectionId: Int): Cursor

    @Update
    fun update(collection: DB_Collection)

    @Insert
    fun insert(collection: DB_Collection): Long

    @Delete
    fun delete(collection: DB_Collection)
}
