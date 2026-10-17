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
 * Inserts new entries into the database. All methods validate their input and throw an
 * InvalidEntryException if the entry cannot be created (e.g. empty name, missing parent,
 * duplicate). They return the uid of the new entry.
 */
class DB_Helper_Create(context: Context) {
    // Thrown if an entry cannot be created; the message says why
    class InvalidEntryException(message: String?) : Exception(message)

    private val dbHelper = DB_Helper(context)

    // Creates a collection; by default without color and emoji, dated now
    fun createCollection(
        name: String,
        desc: String?,
        colors: Int = 0,
        emoji: String? = null,
        date: Long = System.currentTimeMillis() / 1000L
    ): Long {
        if (!name.isEmpty()) {
            val collection = DB_Collection()
            collection.name = name
            collection.desc = desc
            collection.date = date
            collection.colors = colors
            collection.emoji = emoji
            return dbHelper.collection_dao.insert(collection)
        }
        throw InvalidEntryException("Collection name is empty")
    }

    fun createPack(
        name: String,
        desc: String?,
        collection: Int,
        colors: Int,
        emoji: String?,
        date: Long
    ): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        if (dbHelperGet.getSingleCollection(collection) != null && !name.isEmpty()) {
            val pack = DB_Pack()
            pack.name = name
            pack.desc = desc
            pack.date = date
            pack.colors = colors
            pack.emoji = emoji
            pack.collection = collection
            return dbHelper.pack_dao.insert(pack)
        }
        throw InvalidEntryException("Pack name is empty or collection " + collection + " does not exist")
    }

    // Creates a pack without emoji, dated now, inheriting the color of its collection
    fun createPack(name: String, desc: String?, collection: Int): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        val parent = dbHelperGet.getSingleCollection(collection)
            ?: throw InvalidEntryException("Collection " + collection + " does not exist")
        return createPack(
            name,
            desc,
            collection,
            parent.colors,
            null,
            System.currentTimeMillis() / 1000L
        )
    }

    // Creates a card; by default without progress or repetitions, dated now
    fun createCard(
        front: String,
        back: String,
        notes: String?,
        pack: Int,
        known: Int = 0,
        date: Long = System.currentTimeMillis() / 1000L,
        lastRepetition: Long = 0,
        countRepetitions: Int = 0
    ): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        if (dbHelperGet.getSinglePack(pack) != null && !front.isEmpty() && !back.isEmpty()) {
            val card = DB_Card()
            card.front = front
            card.back = back
            card.pack = pack
            card.known = known
            card.date = date
            card.notes = notes
            card.lastRepetition = lastRepetition
            card.countRepetitions = countRepetitions
            return dbHelper.card_dao.insert(card)
        }
        throw InvalidEntryException("Card front or back is empty or pack " + pack + " does not exist")
    }

    fun createMedia(file: String, mime: String): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        if (!dbHelperGet.existsMedia(file)) {
            val media = DB_Media()
            media.file = file
            media.mime = mime
            return dbHelper.media_dao.insert(media)
        }
        throw InvalidEntryException("Media entry for file " + file + " already exists")
    }

    fun createMediaLink(file: Int, card: Int): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        if (!dbHelperGet.existsMediaLinkCard(file, card)) {
            val mediaLinkCard = DB_Media_Link_Card()
            mediaLinkCard.file = file
            mediaLinkCard.card = card
            return dbHelper.media_link_card_dao.insert(mediaLinkCard)
        }
        throw InvalidEntryException("Media file " + file + " is already linked to card " + card)
    }

    fun createTag(tagName: String, emoji: String? = null, color: String? = null): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        if (!dbHelperGet.existsTag(tagName)) {
            val newTag = DB_Tag()
            newTag.name = tagName
            newTag.emoji = emoji
            newTag.color = color
            return dbHelper.tag_dao.insert(newTag)
        }
        throw InvalidEntryException("Tag " + tagName + " already exists")
    }

    fun createTagLink(tag: Int, card: Int): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        if (!dbHelperGet.existsTagLinkCard(tag, card)) {
            val tagLinkCard = DB_Tag_Link_Card()
            tagLinkCard.tag = tag
            tagLinkCard.card = card
            return dbHelper.tag_link_card_dao.insert(tagLinkCard)
        }
        throw InvalidEntryException("Tag " + tag + " is already linked to card " + card)
    }

    // Links a tag to a card by name, creating the tag first if it does not exist yet
    fun createTagLink(tagName: String, card: Int): Long {
        val dbHelperGet = DB_Helper_Get(dbHelper.context)
        val tag = dbHelperGet.getSingleTag(tagName)?.uid ?: createTag(tagName).toInt()
        return createTagLink(tag, card)
    }
}
