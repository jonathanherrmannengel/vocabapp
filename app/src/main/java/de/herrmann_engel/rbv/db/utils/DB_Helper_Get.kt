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
    fun hasCollections(): Boolean = dbHelper.collectionDao.hasCollections()

    fun hasCards(): Boolean = dbHelper.cardDao.hasCards()

    fun hasMedia(): Boolean = dbHelper.mediaDao.hasMedia()

    fun hasTags(): Boolean = dbHelper.tagDao.hasTags()

    // Count
    fun countCollections(): Int = dbHelper.collectionDao.countCollections()

    fun countPacks(): Int = dbHelper.packDao.countPacks()

    fun countPacksInCollection(collection: Int): Int = dbHelper.packDao.countPacksInCollection(collection)

    fun countCards(): Int = dbHelper.cardDao.countCards()

    fun countCardsInCollection(collection: Int): Int = dbHelper.cardDao.countCardsInCollection(collection)

    fun countCardsInPack(pack: Int): Int = dbHelper.cardDao.countCardsInPack(pack)

    // Exists Entry?
    fun existsCollection(id: Int): Boolean = dbHelper.collectionDao.existsCollection(id)

    fun existsPack(id: Int): Boolean = dbHelper.packDao.existsPack(id)

    fun existsMedia(file: String): Boolean = dbHelper.mediaDao.existsMedia(file)

    fun existsTag(tagName: String): Boolean = dbHelper.tagDao.existsTag(tagName)

    fun existsMediaLinkCard(file: Int, card: Int): Boolean = dbHelper.mediaLinkCardDao.existsMediaLinkCard(file, card)

    fun existsTagLinkCard(tag: Int, card: Int): Boolean = dbHelper.tagLinkCardDao.existsTagLinkCard(tag, card)

    // Get One
    fun getSingleCollection(collection: Int): DB_Collection? = dbHelper.collectionDao.getOne(collection)

    fun getSinglePack(pack: Int): DB_Pack? = dbHelper.packDao.getOne(pack)

    fun getSingleCard(card: Int): DB_Card? = dbHelper.cardDao.getOne(card)

    fun getSingleCardWithMeta(cardId: Int): DB_Card_With_Meta? = dbHelper.cardDao.getOneWithMeta(cardId)

    fun getSingleCardIdByPackAndFrontAndBackAndNotes(
        pack: Int,
        front: String,
        back: String,
        notes: String?
    ): Int? = dbHelper.cardDao.getOneIdByPackAndFrontAndBackAndNotes(pack, front, back, notes)

    fun getSingleMedia(file: String): DB_Media? = dbHelper.mediaDao.getSingleMedia(file)

    fun getSingleMedia(id: Int): DB_Media? = dbHelper.mediaDao.getSingleMedia(id)

    fun getSingleTag(tagName: String): DB_Tag? = dbHelper.tagDao.getSingleTag(tagName)

    // Get All: Collections
    val allCollections: List<DB_Collection>
        get() = dbHelper.collectionDao.all

    fun getAllCollectionsByName(name: String): List<DB_Collection> = dbHelper.collectionDao.getAllByName(name)

    val allCollectionsWithMeta: MutableList<DB_Collection_With_Meta>
        get() = dbHelper.collectionDao.allWithMeta

    val allCollectionsWithMetaNoCounter: MutableList<DB_Collection_With_Meta>
        get() = dbHelper.collectionDao.allWithMetaNoCounter

    // Get All: Packs
    val allPacks: List<DB_Pack>
        get() = dbHelper.packDao.all

    fun getAllPacksByCollection(collection: Int): List<DB_Pack> = dbHelper.packDao.getAllByCollection(collection)

    fun getAllPacksByCollectionAndNameAndDesc(
        collection: Int,
        name: String,
        desc: String?
    ): List<DB_Pack> = dbHelper.packDao.getAllByCollectionAndNameAndDesc(collection, name, desc)

    val allPacksWithMeta: MutableList<DB_Pack_With_Meta>
        get() = dbHelper.packDao.allWithMeta

    val allPacksWithMetaNoCounter: MutableList<DB_Pack_With_Meta>
        get() = dbHelper.packDao.allWithMetaNoCounter

    fun getAllPacksWithMetaByCollection(collection: Int): MutableList<DB_Pack_With_Meta> = dbHelper.packDao.getAllByCollectionWithMeta(collection)

    fun getAllPacksWithMetaNoCounterByCollection(collection: Int): MutableList<DB_Pack_With_Meta> = dbHelper.packDao.getAllByCollectionWithMetaNoCounter(collection)

    // Get All: Cards
    val allCardsWithMeta: MutableList<DB_Card_With_Meta>
        get() = dbHelper.cardDao.allWithMeta

    fun getAllCardsByPackWithMeta(pack: Int): MutableList<DB_Card_With_Meta> = dbHelper.cardDao.getAllByPackWithMeta(pack)

    fun getAllCardsByCollectionWithMeta(collection: Int): MutableList<DB_Card_With_Meta> = dbHelper.cardDao.getAllByCollectionWithMeta(collection)

    /**
     * Returns the cards matching the advanced search filters.
     * 
     * @param packs             packs to include, null for all packs
     * @param tags              tags to include (a card needs at least one of them), null for no
     *                          tag filter
     * @param progressGreater   true: progress >= progressThreshold, false: progress <=
     *                          progressThreshold
     * @param progressThreshold null to disable the progress filter
     * @param repetitionOlder   true: last repetition older than repetitionDays days, false: newer
     *                          or equal
     * @param repetitionDays    number of days, null to disable the repetition filter
     */
    fun getAllCardsWithMetaFiltered(
        packs: List<Int>?,
        tags: List<Int>?,
        progressGreater: Boolean,
        progressThreshold: Int?,
        repetitionOlder: Boolean,
        repetitionDays: Int?
    ): MutableList<DB_Card_With_Meta> {
        // Point in time repetitionDays days ago (Unix time in seconds)
        val date = System.currentTimeMillis() / 1000L - (repetitionDays ?: 0) * 86400L
        return dbHelper.cardDao.getAllFilteredWithMeta(
            packs == null, packs ?: emptyList(),
            tags == null, tags ?: emptyList(),
            progressThreshold != null, progressGreater, progressThreshold ?: 0,
            repetitionDays != null, repetitionOlder, date
        )
    }

    fun getAllCardsByMediaWithMeta(mediaId: Int): MutableList<DB_Card_With_Meta> = dbHelper.cardDao.getAllByMedia(mediaId)

    // Get All: Media
    val allMedia: List<DB_Media>
        get() = dbHelper.mediaDao.all

    fun getCardMedia(cardId: Int): List<DB_Media> = dbHelper.mediaDao.getAllByCard(cardId)

    // Only the media files of a card that can be displayed as images
    fun getCardImageMedia(cardId: Int): List<DB_Media> = dbHelper.mediaDao.getAllByCard(cardId).filter { media -> isPhoto(media.mime) }

    private fun isPhoto(mime: String): Boolean {
        return mime == "image/png" || mime == "image/jpeg" || mime == "image/webp"
    }

    // Get All: Tags
    val allTags: List<DB_Tag>
        get() = dbHelper.tagDao.all

    fun getCardTags(cardId: Int): MutableList<DB_Tag> = dbHelper.tagDao.getAllByCard(cardId)
}
