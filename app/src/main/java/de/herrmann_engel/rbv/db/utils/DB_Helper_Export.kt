package de.herrmann_engel.rbv.db.utils

import android.content.Context
import android.database.Cursor

/**
 * Provides raw database cursors for the CSV export, either for everything or for a single collection.
 */
class DB_Helper_Export(context: Context) {
    private val dbHelper = DB_Helper(context)

    val allCollectionIds: List<Int>
        get() = dbHelper.collectionDao.allIds

    fun getSingleCollection(collection: Int): Cursor = dbHelper.collectionDao.getOneExport(collection)

    fun getAllPacksByCollection(collection: Int): Cursor = dbHelper.packDao.getAllExportByCollection(collection)

    fun getAllCardsByCollection(collection: Int): Cursor = dbHelper.cardDao.getAllExportByCollection(collection)

    val allMedia: Cursor
        get() = dbHelper.mediaDao.allExport

    fun getAllMediaByCollection(collection: Int): Cursor = dbHelper.mediaDao.getAllExportByCollection(collection)

    val allMediaLinks: Cursor
        get() = dbHelper.mediaLinkCardDao.allExport

    fun getAllMediaLinksByCollection(collection: Int): Cursor = dbHelper.mediaLinkCardDao.getAllExportByCollection(collection)


    val allTags: Cursor
        get() = dbHelper.tagDao.allExport

    fun getAllTagsByCollection(collection: Int): Cursor = dbHelper.tagDao.getAllExportByCollection(collection)

    val allTagLinks: Cursor
        get() = dbHelper.tagLinkCardDao.allExport

    fun getAllTagLinksByCollection(collection: Int): Cursor = dbHelper.tagLinkCardDao.getAllExportByCollection(collection)
}
