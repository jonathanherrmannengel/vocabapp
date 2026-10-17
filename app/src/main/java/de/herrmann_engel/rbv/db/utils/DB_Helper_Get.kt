package de.herrmann_engel.rbv.db.utils

import android.content.Context
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.db.DB_Collection
import de.herrmann_engel.rbv.db.DB_Collection_With_Meta
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta
import de.herrmann_engel.rbv.db.DB_Tag

/**
 * Read access to the database.
 */
class DB_Helper_Get(context: Context) {
    private val dbHelper = DB_Helper(context)

    // Check if has content
    fun hasCollections(): Boolean {
        return dbHelper.collection_dao.hasCollections()
    }

    fun hasCards(): Boolean {
        return dbHelper.card_dao.hasCards()
    }

    fun hasMedia(): Boolean {
        return dbHelper.media_dao.hasMedia()
    }

    fun hasTags(): Boolean {
        return dbHelper.tag_dao.hasTags()
    }

    // Count
    fun countCollections(): Int {
        return dbHelper.collection_dao.countCollections()
    }

    fun countPacks(): Int {
        return dbHelper.pack_dao.countPacks()
    }

    fun countPacksInCollection(collection: Int): Int {
        return dbHelper.pack_dao.countPacksInCollection(collection)
    }

    fun countCards(): Int {
        return dbHelper.card_dao.countCards()
    }

    fun countCardsInCollection(collection: Int): Int {
        return dbHelper.card_dao.countCardsInCollection(collection)
    }

    fun countCardsInPack(pack: Int): Int {
        return dbHelper.card_dao.countCardsInPack(pack)
    }

    // Exists Entry?
    fun existsCollection(id: Int): Boolean {
        return dbHelper.collection_dao.existsCollection(id)
    }

    fun existsPack(id: Int): Boolean {
        return dbHelper.pack_dao.existsPack(id)
    }

    fun existsMedia(file: String): Boolean {
        return dbHelper.media_dao.existsMedia(file)
    }

    fun existsTag(tagName: String): Boolean {
        return dbHelper.tag_dao.existsTag(tagName)
    }

    fun existsMediaLinkCard(file: Int, card: Int): Boolean {
        return dbHelper.media_link_card_dao.existsMediaLinkCard(file, card)
    }

    fun existsTagLinkCard(tag: Int, card: Int): Boolean {
        return dbHelper.tag_link_card_dao.existsTagLinkCard(tag, card)
    }

    // Get One
    fun getSingleCollection(collection: Int): DB_Collection? {
        return dbHelper.collection_dao.getOne(collection)
    }

    fun getSinglePack(pack: Int): DB_Pack? {
        return dbHelper.pack_dao.getOne(pack)
    }

    fun getSingleCard(card: Int): DB_Card? {
        return dbHelper.card_dao.getOne(card)
    }

    fun getSingleCardWithMeta(cardId: Int): DB_Card_With_Meta? {
        return dbHelper.card_dao.getOneWithMeta(cardId)
    }

    fun getSingleCardIdByPackAndFrontAndBackAndNotes(
        pack: Int,
        front: String,
        back: String,
        notes: String?
    ): Int {
        return dbHelper.card_dao.getOneIdByPackAndFrontAndBackAndNotes(pack, front, back, notes)
    }

    fun getSingleMedia(file: String): DB_Media? {
        return dbHelper.media_dao.getSingleMedia(file)
    }

    fun getSingleMedia(id: Int): DB_Media? {
        return dbHelper.media_dao.getSingleMedia(id)
    }

    fun getSingleTag(tagName: String): DB_Tag? {
        return dbHelper.tag_dao.getSingleTag(tagName)
    }

    // Get All: Collections
    val allCollections: List<DB_Collection>
        get() = dbHelper.collection_dao.all

    fun getAllCollectionsByName(name: String): List<DB_Collection> {
        return dbHelper.collection_dao.getAllByName(name)
    }

    val allCollectionsWithMeta: MutableList<DB_Collection_With_Meta>
        get() = dbHelper.collection_dao.allWithMeta

    val allCollectionsWithMetaNoCounter: MutableList<DB_Collection_With_Meta>
        get() = dbHelper.collection_dao.allWithMetaNoCounter

    // Get All: Packs
    val allPacks: List<DB_Pack>
        get() = dbHelper.pack_dao.all

    fun getAllPacksByCollection(collection: Int): List<DB_Pack> {
        return dbHelper.pack_dao.getAllByCollection(collection)
    }

    fun getAllPacksByCollectionAndNameAndDesc(
        collection: Int,
        name: String,
        desc: String?
    ): List<DB_Pack> {
        return dbHelper.pack_dao.getAllByCollectionAndNameAndDesc(collection, name, desc)
    }

    val allPacksWithMeta: MutableList<DB_Pack_With_Meta>
        get() = dbHelper.pack_dao.allWithMeta

    val allPacksWithMetaNoCounter: MutableList<DB_Pack_With_Meta>
        get() = dbHelper.pack_dao.allWithMetaNoCounter

    fun getAllPacksWithMetaByCollection(collection: Int): MutableList<DB_Pack_With_Meta> {
        return dbHelper.pack_dao.getAllByCollectionWithMeta(collection)
    }

    fun getAllPacksWithMetaNoCounterByCollection(collection: Int): MutableList<DB_Pack_With_Meta> {
        return dbHelper.pack_dao.getAllByCollectionWithMetaNoCounter(collection)
    }

    // Get All: Cards
    val allCardsWithMeta: MutableList<DB_Card_With_Meta>
        get() = dbHelper.card_dao.allWithMeta

    fun getAllCardsByPackWithMeta(pack: Int): MutableList<DB_Card_With_Meta> {
        return dbHelper.card_dao.getAllByPackWithMeta(pack)
    }

    fun getAllCardsByCollectionWithMeta(collection: Int): MutableList<DB_Card_With_Meta> {
        return dbHelper.card_dao.getAllByCollectionWithMeta(collection)
    }

    /**
     * Returns the cards matching the advanced search filters.
     * 
     * @param packs            packs to include, null for all packs
     * @param tags             tags to include (a card needs at least one of them), null for no tag filter
     * @param progressGreater  true: progress >= progressNumber, false: progress <= progressNumber
     * @param progressNumber   progress threshold, negative to disable the progress filter
     * @param repetitionOlder  true: last repetition older than repetitionNumber days, false: newer or equal
     * @param repetitionNumber number of days, negative to disable the repetition filter
     */
    fun getAllCardsWithMetaFiltered(
        packs: List<Int>?,
        tags: List<Int>?,
        progressGreater: Boolean,
        progressNumber: Int,
        repetitionOlder: Boolean,
        repetitionNumber: Int
    ): MutableList<DB_Card_With_Meta> {
        // Point in time repetitionNumber days ago (Unix time in seconds)
        val date = System.currentTimeMillis() / 1000L - repetitionNumber * 86400L
        return dbHelper.card_dao.getAllFilteredWithMeta(
            packs == null, packs ?: emptyList(),
            tags == null, tags ?: emptyList(),
            progressNumber >= 0, progressGreater, progressNumber,
            repetitionNumber >= 0, repetitionOlder, date
        )
    }

    fun getAllCardsByMediaWithMeta(mediaId: Int): MutableList<DB_Card_With_Meta> {
        return dbHelper.card_dao.getAllByMedia(mediaId)
    }

    // Get All: Media
    val allMedia: List<DB_Media>
        get() = dbHelper.media_dao.all

    fun getCardMedia(cardId: Int): List<DB_Media> {
        return dbHelper.media_dao.getAllByCard(cardId)
    }

    // Only the media files of a card that can be displayed as images
    fun getCardImageMedia(cardId: Int): List<DB_Media> {
        return dbHelper.media_dao.getAllByCard(cardId).filter { media -> isPhoto(media.mime) }
    }

    private fun isPhoto(mime: String): Boolean {
        return mime == "image/png" || mime == "image/jpeg" || mime == "image/webp"
    }

    // Get All: Tags
    val allTags: List<DB_Tag>
        get() = dbHelper.tag_dao.all

    fun getCardTags(cardId: Int): MutableList<DB_Tag> {
        return dbHelper.tag_dao.getAllByCard(cardId)
    }
}
