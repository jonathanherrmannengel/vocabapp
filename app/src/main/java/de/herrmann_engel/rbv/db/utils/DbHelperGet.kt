package de.herrmann_engel.rbv.db.utils

import android.content.Context
import de.herrmann_engel.rbv.db.CardWithMeta
import de.herrmann_engel.rbv.db.CollectionWithMeta
import de.herrmann_engel.rbv.db.DbCard
import de.herrmann_engel.rbv.db.DbCollection
import de.herrmann_engel.rbv.db.DbMedia
import de.herrmann_engel.rbv.db.DbPack
import de.herrmann_engel.rbv.db.DbTag
import de.herrmann_engel.rbv.db.PackWithMeta

/**
 * Read access to the database.
 */
class DbHelperGet(context: Context) {
    private val dbHelper = DbHelper(context)

    // Check if has content
    fun hasCollections(): Boolean = dbHelper.collectionDao.hasCollections()

    fun hasCards(): Boolean = dbHelper.cardDao.hasCards()

    fun hasMedia(): Boolean = dbHelper.mediaDao.hasMedia()

    fun hasTags(): Boolean = dbHelper.tagDao.hasTags()

    // Count
    fun countPacks(): Int = dbHelper.packDao.countPacks()

    fun countCards(): Int = dbHelper.cardDao.countCards()

    fun countCardsInCollection(collectionId: Int): Int = dbHelper.cardDao.countCardsInCollection(collectionId)

    fun countCardsInPack(packId: Int): Int = dbHelper.cardDao.countCardsInPack(packId)

    // Exists Entry?
    fun existsCollection(collectionId: Int): Boolean = dbHelper.collectionDao.existsCollection(collectionId)

    fun existsPack(packId: Int): Boolean = dbHelper.packDao.existsPack(packId)

    fun existsMedia(file: String): Boolean = dbHelper.mediaDao.existsMedia(file)

    fun existsMediaLinkCard(fileId: Int, cardId: Int): Boolean = dbHelper.mediaLinkCardDao.existsMediaLinkCard(fileId, cardId)

    fun existsTagLinkCard(tagId: Int, cardId: Int): Boolean = dbHelper.tagLinkCardDao.existsTagLinkCard(tagId, cardId)

    // Get One
    fun getSingleCollection(collectionId: Int): DbCollection? = dbHelper.collectionDao.getOne(collectionId)

    fun getSinglePack(packId: Int): DbPack? = dbHelper.packDao.getOne(packId)

    fun getSingleCard(cardId: Int): DbCard? = dbHelper.cardDao.getOne(cardId)

    fun getSingleCardWithMeta(cardId: Int): CardWithMeta? = dbHelper.cardDao.getOneWithMeta(cardId)

    fun getSingleCardIdByPackAndFrontAndBackAndNotes(
        packId: Int,
        front: String,
        back: String,
        notes: String
    ): Int? = dbHelper.cardDao.getOneIdByPackAndFrontAndBackAndNotes(packId, front, back, notes)

    fun getSingleMedia(file: String): DbMedia? = dbHelper.mediaDao.getSingleMedia(file)

    fun getSingleMedia(fileId: Int): DbMedia? = dbHelper.mediaDao.getSingleMedia(fileId)

    fun getSingleTag(tagName: String): DbTag? = dbHelper.tagDao.getSingleTag(tagName)

    // Get All: Collections
    fun getAllCollections(): List<DbCollection> = dbHelper.collectionDao.all

    fun getAllCollectionsByName(name: String): List<DbCollection> = dbHelper.collectionDao.getAllByName(name)

    fun getAllCollectionsWithMeta(): MutableList<CollectionWithMeta> =
        dbHelper.collectionDao.allWithMeta

    // Get All: Packs
    fun getAllPacksByCollection(collectionId: Int): List<DbPack> = dbHelper.packDao.getAllByCollection(collectionId)

    fun getAllPacksByCollectionAndNameAndDesc(
        collectionId: Int,
        name: String,
        desc: String
    ): List<DbPack> = dbHelper.packDao.getAllByCollectionAndNameAndDesc(collectionId, name, desc)

    fun getAllPacksWithMeta(): MutableList<PackWithMeta> = dbHelper.packDao.allWithMeta

    fun getAllPacksWithMetaByCollection(collectionId: Int): MutableList<PackWithMeta> = dbHelper.packDao.getAllByCollectionWithMeta(collectionId)

    // Get All: Cards
    fun getAllCardsWithMeta(): MutableList<CardWithMeta> = dbHelper.cardDao.allWithMeta

    fun getAllCardsByPackWithMeta(packId: Int): MutableList<CardWithMeta> = dbHelper.cardDao.getAllByPackWithMeta(packId)

    fun getAllCardsByCollectionWithMeta(collectionId: Int): MutableList<CardWithMeta> = dbHelper.cardDao.getAllByCollectionWithMeta(collectionId)

    /**
     * Returns the cards matching the advanced search filters.
     * 
     * @param packIds           packs to include, null for all packs
     * @param tagIds            tags to include (a card needs at least one of them), null for no
     *                          tag filter
     * @param progressGreater   true: progress >= progressThreshold, false: progress <=
     *                          progressThreshold
     * @param progressThreshold null to disable the progress filter
     * @param repetitionOlder   true: last repetition older than repetitionDays days, false: newer
     *                          or equal
     * @param repetitionDays    number of days, null to disable the repetition filter
     */
    fun getAllCardsWithMetaFiltered(
        packIds: List<Int>?,
        tagIds: List<Int>?,
        progressGreater: Boolean,
        progressThreshold: Int?,
        repetitionOlder: Boolean,
        repetitionDays: Int?
    ): MutableList<CardWithMeta> {
        // Point in time repetitionDays days ago (Unix time in seconds)
        val date = System.currentTimeMillis() / 1000L - (repetitionDays ?: 0) * 86400L
        return dbHelper.cardDao.getAllFilteredWithMeta(
            packIds == null, packIds ?: emptyList(),
            tagIds == null, tagIds ?: emptyList(),
            progressThreshold != null, progressGreater, progressThreshold ?: 0,
            repetitionDays != null, repetitionOlder, date
        )
    }

    fun getAllCardsByMediaWithMeta(mediaId: Int): MutableList<CardWithMeta> = dbHelper.cardDao.getAllByMedia(mediaId)

    // Get All: Media
    fun getAllMedia(): List<DbMedia> = dbHelper.mediaDao.all

    fun getCardMedia(cardId: Int): List<DbMedia> = dbHelper.mediaDao.getAllByCard(cardId)

    // Only the media files of a card that can be displayed as images
    fun getCardImageMedia(cardId: Int): List<DbMedia> = dbHelper.mediaDao.getAllByCard(cardId).filter { media -> isPhoto(media.mime) }

    private fun isPhoto(mime: String): Boolean {
        return mime == "image/png" || mime == "image/jpeg" || mime == "image/webp"
    }

    // Get All: Tags
    fun getAllTags(): List<DbTag> = dbHelper.tagDao.all

    fun getCardTags(cardId: Int): MutableList<DbTag> = dbHelper.tagDao.getAllByCard(cardId)
}
