package de.herrmann_engel.rbv.db.utils

import android.content.Context
import android.database.Cursor

/**
 * Provides raw database cursors for the CSV export, either for everything or for a single collection.
 */
class DbHelperExport(context: Context) {
    private val dbHelper = DbHelper(context)

    fun getAllCollectionIds(): List<Int> = dbHelper.collectionDao.allIds

    fun getSingleCollection(collectionId: Int): Cursor = dbHelper.collectionDao.getOneExport(collectionId)

    fun getAllPacksByCollection(collectionId: Int): Cursor = dbHelper.packDao.getAllExportByCollection(collectionId)

    fun getAllCardsByCollection(collectionId: Int): Cursor = dbHelper.cardDao.getAllExportByCollection(collectionId)

    fun getAllMedia(): Cursor = dbHelper.mediaDao.allExport

    fun getAllMediaByCollection(collectionId: Int): Cursor = dbHelper.mediaDao.getAllExportByCollection(collectionId)

    fun getAllMediaLinks(): Cursor = dbHelper.mediaLinkCardDao.allExport

    fun getAllMediaLinksByCollection(collectionId: Int): Cursor = dbHelper.mediaLinkCardDao.getAllExportByCollection(collectionId)


    fun getAllTags(): Cursor = dbHelper.tagDao.allExport

    fun getAllTagsByCollection(collectionId: Int): Cursor = dbHelper.tagDao.getAllExportByCollection(collectionId)

    fun getAllTagLinks(): Cursor = dbHelper.tagLinkCardDao.allExport

    fun getAllTagLinksByCollection(collectionId: Int): Cursor = dbHelper.tagLinkCardDao.getAllExportByCollection(collectionId)
}
