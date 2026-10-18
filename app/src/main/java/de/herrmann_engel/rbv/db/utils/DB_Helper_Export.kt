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

    fun getSingleCollection(collectionId: Int): Cursor = dbHelper.collectionDao.getOneExport(collectionId)

    fun getAllPacksByCollection(collectionId: Int): Cursor = dbHelper.packDao.getAllExportByCollection(collectionId)

    fun getAllCardsByCollection(collectionId: Int): Cursor = dbHelper.cardDao.getAllExportByCollection(collectionId)

    val allMedia: Cursor
        get() = dbHelper.mediaDao.allExport

    fun getAllMediaByCollection(collectionId: Int): Cursor = dbHelper.mediaDao.getAllExportByCollection(collectionId)

    val allMediaLinks: Cursor
        get() = dbHelper.mediaLinkCardDao.allExport

    fun getAllMediaLinksByCollection(collectionId: Int): Cursor = dbHelper.mediaLinkCardDao.getAllExportByCollection(collectionId)


    val allTags: Cursor
        get() = dbHelper.tagDao.allExport

    fun getAllTagsByCollection(collectionId: Int): Cursor = dbHelper.tagDao.getAllExportByCollection(collectionId)

    val allTagLinks: Cursor
        get() = dbHelper.tagLinkCardDao.allExport

    fun getAllTagLinksByCollection(collectionId: Int): Cursor = dbHelper.tagLinkCardDao.getAllExportByCollection(collectionId)
}
