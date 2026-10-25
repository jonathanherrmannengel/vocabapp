package de.herrmann_engel.rbv.db.utils

import android.content.Context
import de.herrmann_engel.rbv.db.DbCard
import de.herrmann_engel.rbv.db.DbCollection
import de.herrmann_engel.rbv.db.DbPack
import de.herrmann_engel.rbv.db.DbTag

/**
 * Updates existing entries. Methods return false if the new values are invalid (e.g. empty names).
 */
class DbHelperUpdate(context: Context) {
    private val dbHelper = DbHelper(context)

    fun updateCollection(collection: DbCollection): Boolean {
        if (collection.name.isEmpty()) {
            return false
        }
        dbHelper.collectionDao.update(collection)
        return true
    }

    fun updatePack(pack: DbPack): Boolean {
        if (pack.name.isEmpty()) {
            return false
        }
        dbHelper.packDao.update(pack)
        return true
    }

    fun updateCard(card: DbCard): Boolean {
        if (card.front.isEmpty() || card.back.isEmpty()) {
            return false
        }
        dbHelper.cardDao.update(card)
        return true
    }

    // Saves a new progress of the card; each change of the progress counts as a repetition
    fun updateCardProgress(card: DbCard, known: Int) {
        card.known = known
        card.lastRepetition = System.currentTimeMillis() / 1000L
        card.countRepetitions++
        updateCard(card)
    }

    fun updateTag(tag: DbTag): Boolean {
        val dbHelperGet = DbHelperGet(dbHelper.context)
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
