package de.herrmann_engel.rbv.db.utils

import android.content.Context
import android.database.Cursor

/**
 * Provides raw database cursors for the CSV export, either for everything or for a single collection.
 */
class DB_Helper_Export(context: Context) {
    private val dbHelper = DB_Helper(context)

    val allCollectionIDs: List<Int>
        get() = dbHelper.collection_dao.allIds

    fun getSingleCollection(collection: Int): Cursor {
        return dbHelper.collection_dao.getOneExport(collection)
    }

    fun getAllPacksByCollection(collection: Int): Cursor {
        return dbHelper.pack_dao.getAllExportByCollection(collection)
    }

    fun getAllCardsByCollection(collection: Int): Cursor {
        return dbHelper.card_dao.getAllExportByCollection(collection)
    }

    val allMedia: Cursor
        get() = dbHelper.media_dao.allExport

    fun getAllMediaByCollection(collection: Int): Cursor {
        return dbHelper.media_dao.getAllExportByCollection(collection)
    }

    val allMediaLinks: Cursor
        get() = dbHelper.media_link_card_dao.allExport

    fun getAllMediaLinksByCollection(collection: Int): Cursor {
        return dbHelper.media_link_card_dao.getAllExportByCollection(collection)
    }


    val allTags: Cursor
        get() = dbHelper.tag_dao.allExport

    fun getAllTagsByCollection(collection: Int): Cursor {
        return dbHelper.tag_dao.getAllExportByCollection(collection)
    }

    val allTagLinks: Cursor
        get() = dbHelper.tag_link_card_dao.allExport

    fun getAllTagLinksByCollection(collection: Int): Cursor {
        return dbHelper.tag_link_card_dao.getAllExportByCollection(collection)
    }
}
