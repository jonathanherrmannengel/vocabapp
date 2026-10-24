package de.herrmann_engel.rbv.db.utils

import android.content.Context
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Collection
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.DB_Media_Link_Card
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.DB_Tag
import de.herrmann_engel.rbv.db.DB_Tag_Link_Card

/**
 * Inserts new entries into the database and returns their uid. Invalid values (e.g. an empty name)
 * are rejected with an InvalidEntryException. A missing parent or an entry that already exists is
 * rejected by the database (foreign keys and unique indices) with an SQLiteConstraintException.
 */
class DB_Helper_Create(context: Context) {
    // Thrown if an entry cannot be created; the message says why
    class InvalidEntryException(message: String?) : Exception(message)

    private val dbHelper = DB_Helper(context)

    // Creates a collection; by default without color and emoji, dated now
    fun createCollection(
        name: String,
        desc: String,
        colors: Int = 0,
        emoji: String = "",
        date: Long = System.currentTimeMillis() / 1000L
    ): Long {
        if (name.isNotEmpty()) {
            val collection = DB_Collection()
            collection.name = name
            collection.desc = desc
            collection.date = date
            collection.colors = colors
            collection.emoji = emoji
            return dbHelper.collectionDao.insert(collection)
        }
        throw InvalidEntryException("Collection name is empty")
    }

    fun createPack(
        name: String,
        desc: String,
        collectionId: Int,
        colors: Int,
        emoji: String,
        date: Long
    ): Long {
        if (name.isNotEmpty()) {
            val pack = DB_Pack()
            pack.name = name
            pack.desc = desc
            pack.date = date
            pack.colors = colors
            pack.emoji = emoji
            pack.collectionId = collectionId
            return dbHelper.packDao.insert(pack)
        }
        throw InvalidEntryException("Pack name is empty")
    }

    // Creates a pack without emoji, dated now, inheriting the color of its collection
    fun createPack(name: String, desc: String, collectionId: Int): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        val parent = dbHelperGet.getSingleCollection(collectionId)
            ?: throw InvalidEntryException("Collection $collectionId does not exist")
        return createPack(
            name,
            desc,
            collectionId,
            parent.colors,
            "",
            System.currentTimeMillis() / 1000L
        )
    }

    // Creates a card; by default without progress or repetitions, dated now
    fun createCard(
        front: String,
        back: String,
        notes: String,
        packId: Int,
        known: Int = 0,
        date: Long = System.currentTimeMillis() / 1000L,
        lastRepetition: Long = 0,
        countRepetitions: Int = 0
    ): Long {
        if (front.isNotEmpty() && back.isNotEmpty()) {
            val card = DB_Card()
            card.front = front
            card.back = back
            card.packId = packId
            card.known = known
            card.date = date
            card.notes = notes
            card.lastRepetition = lastRepetition
            card.countRepetitions = countRepetitions
            return dbHelper.cardDao.insert(card)
        }
        throw InvalidEntryException("Card front or back is empty")
    }

    fun createMedia(file: String, mime: String): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        if (!dbHelperGet.existsMedia(file)) {
            val media = DB_Media()
            media.file = file
            media.mime = mime
            return dbHelper.mediaDao.insert(media)
        }
        throw InvalidEntryException("Media entry for file $file already exists")
    }

    fun createMediaLink(fileId: Int, cardId: Int): Long {
        val mediaLinkCard = DB_Media_Link_Card()
        mediaLinkCard.fileId = fileId
        mediaLinkCard.cardId = cardId
        return dbHelper.mediaLinkCardDao.insert(mediaLinkCard)
    }

    fun createTag(tagName: String, emoji: String = "", color: String = ""): Long {
        val newTag = DB_Tag()
        newTag.name = tagName
        newTag.emoji = emoji
        newTag.color = color
        return dbHelper.tagDao.insert(newTag)
    }

    fun createTagLink(tagId: Int, cardId: Int): Long {
        val tagLinkCard = DB_Tag_Link_Card()
        tagLinkCard.tagId = tagId
        tagLinkCard.cardId = cardId
        return dbHelper.tagLinkCardDao.insert(tagLinkCard)
    }

    // Links a tag to a card by name, creating the tag first if it does not exist yet
    fun createTagLink(tagName: String, cardId: Int): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        val tagId = dbHelperGet.getSingleTag(tagName)?.uid ?: createTag(tagName).toInt()
        return createTagLink(tagId, cardId)
    }
}
