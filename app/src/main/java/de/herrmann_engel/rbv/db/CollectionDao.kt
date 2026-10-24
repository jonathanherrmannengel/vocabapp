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
interface CollectionDao {
    @Query("SELECT EXISTS (SELECT 1 FROM db_collection LIMIT 1)")
    fun hasCollections(): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM db_collection WHERE uid=:collectionId LIMIT 1)")
    fun existsCollection(collectionId: Int): Boolean

    @get:Query("SELECT * FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val all: List<DbCollection>

    @Query("SELECT * FROM db_collection WHERE name=:name ORDER BY name COLLATE NOCASE ASC, uid DESC")
    fun getAllByName(name: String): List<DbCollection>

    @get:Query("SELECT *, (SELECT COUNT(*) FROM db_pack WHERE collection=db_collection.uid) AS counter FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val allWithMeta: MutableList<CollectionWithMeta>

    @get:Query("SELECT uid FROM db_collection ORDER BY name COLLATE NOCASE ASC, uid DESC")
    val allIds: List<Int>

    @Query("SELECT * FROM db_collection WHERE uid=:collectionId LIMIT 1")
    fun getOne(collectionId: Int): DbCollection?

    // Raw cursor used by the CSV export
    @Query("SELECT * FROM db_collection WHERE uid=:collectionId")
    fun getOneExport(collectionId: Int): Cursor

    @Update
    fun update(collection: DbCollection)

    @Insert
    fun insert(collection: DbCollection): Long

    @Delete
    fun delete(collection: DbCollection)
}
