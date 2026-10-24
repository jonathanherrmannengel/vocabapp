package de.herrmann_engel.rbv.db

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Data access for cards.
 * 
 * 
 * All "WithMeta" queries additionally select the color of the card's pack and a string of the
 * card's tag emojis and names (see SELECT_WITH_META).
 */
@Dao
interface CardDao {
    @Query("SELECT EXISTS (SELECT 1 FROM db_card LIMIT 1)")
    fun hasCards(): Boolean

    @Query("SELECT COUNT(*) FROM db_card")
    fun countCards(): Int

    @Query("SELECT COUNT(*) FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collectionId)")
    fun countCardsInCollection(collectionId: Int): Int

    @Query("SELECT COUNT(*) FROM db_card WHERE pack=:packId")
    fun countCardsInPack(packId: Int): Int

    @get:Query(SELECT_WITH_META)
    val allWithMeta: MutableList<CardWithMeta>

    @Query(SELECT_WITH_META + " WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collectionId)")
    fun getAllByCollectionWithMeta(collectionId: Int): MutableList<CardWithMeta>

    @Query(SELECT_WITH_META + " WHERE pack=:packId")
    fun getAllByPackWithMeta(packId: Int): MutableList<CardWithMeta>

    /**
     * Cards matching the advanced search filters. Each filter can be switched off.
     * 
     * @param allPacks        true: cards of all packs, false: only cards of the packs in packIds
     * @param allTags         true: no tag filter, false: only cards with at least one tag of tagIds
     * @param filterProgress  false: no progress filter
     * @param progressGreater true: progress >= progress, false: progress <= progress
     * @param filterRepetition false: no filter by date of last repetition
     * @param repetitionOlder true: last repetition before date, false: at or after date
     */
    @Query(
        (SELECT_WITH_META + " WHERE (:allPacks OR pack IN (:packIds))"
                + " AND (:allTags OR db_card.uid IN (SELECT cardId FROM db_tag_link_card WHERE tagId IN (:tagIds)))"
                + " AND (NOT :filterProgress OR (CASE WHEN :progressGreater THEN known >= :progressThreshold ELSE known <= :progressThreshold END))"
                + " AND (NOT :filterRepetition OR (CASE WHEN :repetitionOlder THEN last_repetition < :date ELSE last_repetition >= :date END))")
    )
    fun getAllFilteredWithMeta(
        allPacks: Boolean,
        packIds: List<Int>,
        allTags: Boolean,
        tagIds: List<Int>,
        filterProgress: Boolean,
        progressGreater: Boolean,
        progressThreshold: Int,
        filterRepetition: Boolean,
        repetitionOlder: Boolean,
        date: Long
    ): MutableList<CardWithMeta>

    // Cards linked to the given media file
    @Query(SELECT_WITH_META + " INNER JOIN db_media_link_card ON db_card.uid = db_media_link_card.cardId WHERE db_media_link_card.fileId=:fileId")
    fun getAllByMedia(fileId: Int): MutableList<CardWithMeta>

    // Raw cursor used by the CSV export
    @Query("SELECT * FROM db_card WHERE pack IN (SELECT uid FROM db_pack WHERE collection=:collectionId)")
    fun getAllExportByCollection(collectionId: Int): Cursor

    @Query("SELECT * FROM db_card WHERE uid=:cardId LIMIT 1")
    fun getOne(cardId: Int): DbCard?

    @Query(SELECT_WITH_META + " WHERE db_card.uid=:cardId LIMIT 1")
    fun getOneWithMeta(cardId: Int): CardWithMeta?

    // Used by the import to find already existing cards; null if there is none
    @Query("SELECT uid FROM db_card WHERE pack=:packId AND front=:front AND back=:back AND notes=:notes LIMIT 1")
    fun getOneIdByPackAndFrontAndBackAndNotes(
        packId: Int,
        front: String,
        back: String,
        notes: String
    ): Int?

    @Update
    fun update(card: DbCard)

    @Insert
    fun insert(card: DbCard): Long

    @Delete
    fun delete(card: DbCard)

    companion object {
        // Cards with the color of their pack and their tags' emojis and names (used for search and display)
        const val SELECT_WITH_META: String =
            "SELECT db_card.*, (SELECT colors FROM db_pack WHERE uid=db_card.pack) AS packColor, (SELECT (CASE WHEN group_concat(NULLIF(emoji,''),' ') IS NOT NULL THEN group_concat(NULLIF(emoji,''),' ') ELSE '' END || ' ' || group_concat(tag_name,' ')) FROM db_tag WHERE db_tag.uid IN (SELECT tagId FROM db_tag_link_card WHERE cardId=db_card.uid)) AS tagNames FROM db_card"
    }
}
