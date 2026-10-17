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
 * Bundles all DAOs of the app database. Used by the public DB_Helper_* classes.
 */
internal class DB_Helper(val context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)
    val collection_dao: DB_Collection_DAO = database.collectionDAO()
    val pack_dao: DB_Pack_DAO = database.packDAO()
    val card_dao: DB_Card_DAO = database.cardDAO()
    val media_dao: DB_Media_DAO = database.mediaDAO()
    val media_link_card_dao: DB_Media_Link_Card_DAO = database.mediaLinkCardDAO()
    val tag_dao: DB_Tag_DAO = database.tagDAO()
    val tag_link_card_dao: DB_Tag_Link_Card_DAO = database.tagLinkCardDAO()
}
