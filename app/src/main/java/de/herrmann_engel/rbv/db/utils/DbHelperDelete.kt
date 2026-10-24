package de.herrmann_engel.rbv.db.utils

import android.content.Context
import de.herrmann_engel.rbv.db.DbCard
import de.herrmann_engel.rbv.db.DbCollection
import de.herrmann_engel.rbv.db.DbPack

/**
 * Deletes entries from the database.
 */
class DbHelperDelete(context: Context) {
    private val dbHelper = DbHelper(context)

    /**
     * Deletes a collection. If it still contains packs, these are deleted as well if force is set,
     * otherwise nothing is deleted and false is returned.
     */
    fun deleteCollection(collection: DbCollection, force: Boolean): Boolean =
        dbHelper.database.runInTransaction<Boolean> {
            val dbHelperGet = DbHelperGet(dbHelper.context)
            val packs = dbHelperGet.getAllPacksByCollection(collection.uid)
            if (packs.isNotEmpty() && !force) {
                return@runInTransaction false
            }
            packs.forEach { pack -> deletePack(pack, true) }
            dbHelper.collectionDao.delete(collection)
            true
        }

    /**
     * Deletes a pack. If it still contains cards, these are deleted as well if force is set,
     * otherwise nothing is deleted and false is returned. As with deleteCard, media files no
     * longer linked to any card are deleted too, and so are tags no longer assigned to any card.
     */
    fun deletePack(pack: DbPack, force: Boolean): Boolean =
        dbHelper.database.runInTransaction<Boolean> {
            val dbHelperGet = DbHelperGet(dbHelper.context)
            val containsCards = dbHelperGet.countCardsInPack(pack.uid) > 0
            if (containsCards && !force) {
                return@runInTransaction false
            }
            val fileIds = dbHelper.mediaLinkCardDao.getAllMediaIdsByPack(pack.uid)
            val tagIds = dbHelper.tagLinkCardDao.getAllTagIdsByPack(pack.uid)
            // Deleting the pack also deletes its cards and their links (cascade)
            dbHelper.packDao.delete(pack)
            deleteUnusedTags(tagIds)
            deleteUnusedMedia(fileIds)
            true
        }

    /**
     * Deletes a card and its media and tag links. Media files and tags no longer linked to any card
     * are deleted too.
     */
    fun deleteCard(card: DbCard): Boolean =
        dbHelper.database.runInTransaction<Boolean> {
            val fileIds = dbHelper.mediaLinkCardDao.getAllMediaIdsByCard(card.uid)
            val tagIds = dbHelper.tagLinkCardDao.getAllTagIdsByCard(card.uid)
            // Deleting the card also deletes its media and tag links (cascade)
            dbHelper.cardDao.delete(card)
            deleteUnusedTags(tagIds)
            deleteUnusedMedia(fileIds)
            true
        }

    // Deletes a media entry; its links are deleted with it (cascade)
    fun deleteMedia(fileId: Int) {
        dbHelper.mediaDao.deleteMedia(fileId)
    }

    // Removes a media file from a card; returns true if the media entry was deleted because it is
    // no longer used
    fun deleteMediaLink(fileId: Int, cardId: Int): Boolean =
        dbHelper.database.runInTransaction<Boolean> {
            dbHelper.mediaLinkCardDao.deleteMediaLinkCard(fileId, cardId)
            deleteUnusedMedia(listOf(fileId)) > 0
        }

    // Removes a tag from a card; returns true if the tag was deleted because it is no longer used
    fun deleteTagLink(tagId: Int, cardId: Int): Boolean =
        dbHelper.database.runInTransaction<Boolean> {
            dbHelper.tagLinkCardDao.deleteTagLinkCard(tagId, cardId)
            deleteUnusedTags(listOf(tagId)) > 0
        }

    /**
     * Deletes those of the given media entries that are no longer linked to any card and returns
     * their number. Deleting cards only removes their media links (cascade), so the entries are
     * checked here. The files themselves are kept.
     */
    fun deleteUnusedMedia(fileIds: List<Int>): Int = deleteInChunks(fileIds) { ids -> dbHelper.mediaDao.deleteUnusedMedia(ids) }

    /**
     * Deletes those of the given tags that are no longer assigned to any card and returns their
     * number. Deleting cards only removes their tag links (cascade), so the tags are checked here.
     */
    fun deleteUnusedTags(tagIds: List<Int>): Int = deleteInChunks(tagIds) { ids -> dbHelper.tagDao.deleteUnusedTags(ids) }

    // Runs a delete query for a list of ids in chunks and returns the number of deleted rows
    private fun deleteInChunks(ids: List<Int>, delete: (List<Int>) -> Int): Int {
        return ids.chunked(MAX_PARAMETERS).sumOf(delete)
    }

    companion object {
        // Stays below SQLite's limit of 999 bound parameters per statement
        private const val MAX_PARAMETERS = 500
    }
}
