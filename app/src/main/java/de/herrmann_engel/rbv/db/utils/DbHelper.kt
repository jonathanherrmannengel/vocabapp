package de.herrmann_engel.rbv.db.utils

import android.content.Context
import de.herrmann_engel.rbv.db.AppDatabase
import de.herrmann_engel.rbv.db.CardDao
import de.herrmann_engel.rbv.db.CollectionDao
import de.herrmann_engel.rbv.db.MediaDao
import de.herrmann_engel.rbv.db.MediaLinkCardDao
import de.herrmann_engel.rbv.db.PackDao
import de.herrmann_engel.rbv.db.TagDao
import de.herrmann_engel.rbv.db.TagLinkCardDao

/**
 * Bundles all DAOs of the app database. Used by the public DbHelper* classes, which are the only
 * place the DAOs are used (see AppDatabase).
 */
internal class DbHelper(val context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)
    val collectionDao: CollectionDao = database.collectionDAO()
    val packDao: PackDao = database.packDAO()
    val cardDao: CardDao = database.cardDAO()
    val mediaDao: MediaDao = database.mediaDAO()
    val mediaLinkCardDao: MediaLinkCardDao = database.mediaLinkCardDAO()
    val tagDao: TagDao = database.tagDAO()
    val tagLinkCardDao: TagLinkCardDao = database.tagLinkCardDAO()
}
