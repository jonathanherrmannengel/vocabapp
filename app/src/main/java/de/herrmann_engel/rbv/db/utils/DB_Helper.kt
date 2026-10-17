package de.herrmann_engel.rbv.db.utils

import android.content.Context
import de.herrmann_engel.rbv.db.AppDatabase
import de.herrmann_engel.rbv.db.DB_Card_DAO
import de.herrmann_engel.rbv.db.DB_Collection_DAO
import de.herrmann_engel.rbv.db.DB_Media_DAO
import de.herrmann_engel.rbv.db.DB_Media_Link_Card_DAO
import de.herrmann_engel.rbv.db.DB_Pack_DAO
import de.herrmann_engel.rbv.db.DB_Tag_DAO
import de.herrmann_engel.rbv.db.DB_Tag_Link_Card_DAO

/**
 * Bundles all DAOs of the app database. Base for the public DB_Helper_* classes.
 */
internal class DB_Helper(val context: Context?) {
    val collection_dao: DB_Collection_DAO?
    val pack_dao: DB_Pack_DAO?
    val card_dao: DB_Card_DAO?
    val media_dao: DB_Media_DAO?
    val media_link_card_dao: DB_Media_Link_Card_DAO?
    val tag_dao: DB_Tag_DAO?
    val tag_link_card_dao: DB_Tag_Link_Card_DAO?
    val database: AppDatabase?

    init {
        val db = AppDatabase.getInstance(context)
        database = db
        collection_dao = db.collectionDAO()
        pack_dao = db.packDAO()
        card_dao = db.cardDAO()
        media_dao = db.mediaDAO()
        media_link_card_dao = db.mediaLinkCardDAO()
        tag_dao = db.tagDAO()
        tag_link_card_dao = db.tagLinkCardDAO()
    }
}
