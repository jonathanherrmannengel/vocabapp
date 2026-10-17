package de.herrmann_engel.rbv.db.utils

import android.content.Context
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Collection
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.DB_Tag

/**
 * Updates existing entries. Methods return false if the new values are invalid (e.g. empty names).
 */
class DB_Helper_Update(context: Context) {
    private val dbHelper = DB_Helper(context)

    fun updateCollection(collection: DB_Collection): Boolean {
        if (collection.name.isEmpty()) {
            return false
        }
        dbHelper.collectionDao.update(collection)
        return true
    }

    fun updatePack(pack: DB_Pack): Boolean {
        if (pack.name.isEmpty()) {
            return false
        }
        dbHelper.packDao.update(pack)
        return true
    }

    fun updateCard(card: DB_Card): Boolean {
        if (card.front.isEmpty() || card.back.isEmpty()) {
            return false
        }
        dbHelper.cardDao.update(card)
        return true
    }

    fun updateTag(tag: DB_Tag): Boolean {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        // Do not allow tag name already in use
        val existingTag = dbHelperGet.getSingleTag(tag.name)
        if (existingTag != null && existingTag.uid != tag.uid) {
            return false
        }
        if (tag.name.isBlank()) {
            return false
        }
        dbHelper.tagDao.update(tag)
        return true
    }
}
