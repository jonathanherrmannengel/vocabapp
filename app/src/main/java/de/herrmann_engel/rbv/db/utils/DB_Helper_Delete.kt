package de.herrmann_engel.rbv.db.utils

import android.content.Context
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Collection
import de.herrmann_engel.rbv.db.DB_Pack

/**
 * Deletes entries from the database.
 */
class DB_Helper_Delete(context: Context) {
    private val dbHelper = DB_Helper(context)

    /**
     * Deletes a collection. If it still contains packs, these are deleted as well if force is set,
     * otherwise nothing is deleted and false is returned.
     */
    fun deleteCollection(collection: DB_Collection, force: Boolean): Boolean {
        return dbHelper.database.runInTransaction<Boolean> {
            val dbHelperGet = DB_Helper_Get(dbHelper.context)
            val packs = dbHelperGet.getAllPacksByCollection(collection.uid)
            if (packs.isNotEmpty() && !force) {
                return@runInTransaction false
            }
            packs.forEach { pack -> deletePack(pack, true) }
            dbHelper.collection_dao.delete(collection)
            true
        }
    }

    /**
     * Deletes a pack. If it still contains cards, these are deleted as well if force is set,
     * otherwise nothing is deleted and false is returned. As with deleteCard, media files no
     * longer linked to any card are deleted too, and so are tags no longer assigned to any card.
     */
    fun deletePack(pack: DB_Pack, force: Boolean): Boolean {
        return dbHelper.database.runInTransaction<Boolean> {
            val dbHelperGet = DB_Helper_Get(dbHelper.context)
            val containsCards = dbHelperGet.countCardsInPack(pack.uid) > 0
            if (containsCards && !force) {
                return@runInTransaction false
            }
            val fileIds = dbHelper.media_link_card_dao.getAllMediaIdsByPack(pack.uid)
            val tagIds = dbHelper.tag_link_card_dao.getAllTagIdsByPack(pack.uid)
            // Deleting the pack also deletes its cards and their links (cascade)
            dbHelper.pack_dao.delete(pack)
            deleteUnusedTags(tagIds)
            deleteUnusedMedia(fileIds)
            true
        }
    }

    /**
     * Deletes a card and its media and tag links. Media files and tags no longer linked to any card
     * are deleted too.
     */
    fun deleteCard(card: DB_Card): Boolean {
        return dbHelper.database.runInTransaction<Boolean> {
            val fileIds = dbHelper.media_link_card_dao.getAllMediaIdsByCard(card.uid)
            val tagIds = dbHelper.tag_link_card_dao.getAllTagIdsByCard(card.uid)
            // Deleting the card also deletes its media and tag links (cascade)
            dbHelper.card_dao.delete(card)
            deleteUnusedTags(tagIds)
            deleteUnusedMedia(fileIds)
            true
        }
    }

    // Deletes a media entry; its links are deleted with it (cascade)
    fun deleteMedia(file: Int) {
        dbHelper.media_dao.deleteMedia(file)
    }

    // Removes a media file from a card; returns true if the media entry was deleted because it is
    // no longer used
    fun deleteMediaLink(file: Int, card: Int): Boolean {
        return dbHelper.database.runInTransaction<Boolean> {
            dbHelper.media_link_card_dao.deleteMediaLinkCard(file, card)
            deleteUnusedMedia(listOf(file)) > 0
        }
    }

    // Removes a tag from a card; returns true if the tag was deleted because it is no longer used
    fun deleteTagLink(tag: Int, card: Int): Boolean {
        return dbHelper.database.runInTransaction<Boolean> {
            dbHelper.tag_link_card_dao.deleteTagLinkCard(tag, card)
            deleteUnusedTags(listOf(tag)) > 0
        }
    }

    /**
     * Deletes those of the given media entries that are no longer linked to any card and returns
     * their number. Deleting cards only removes their media links (cascade), so the entries are
     * checked here. The files themselves are kept.
     */
    fun deleteUnusedMedia(files: List<Int>): Int {
        return deleteInChunks(files) { ids -> dbHelper.media_dao.deleteUnusedMedia(ids) }
    }

    /**
     * Deletes those of the given tags that are no longer assigned to any card and returns their
     * number. Deleting cards only removes their tag links (cascade), so the tags are checked here.
     */
    fun deleteUnusedTags(tags: List<Int>): Int {
        return deleteInChunks(tags) { ids -> dbHelper.tag_dao.deleteUnusedTags(ids) }
    }

    // Runs a delete query for a list of ids in chunks and returns the number of deleted rows
    private fun deleteInChunks(ids: List<Int>, delete: (List<Int>) -> Int): Int {
        return ids.chunked(MAX_PARAMETERS).sumOf(delete)
    }

    companion object {
        // Stays below SQLite's limit of 999 bound parameters per statement
        private const val MAX_PARAMETERS = 500
    }
}
