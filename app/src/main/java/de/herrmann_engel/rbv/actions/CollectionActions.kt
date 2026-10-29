package de.herrmann_engel.rbv.actions

import androidx.activity.ComponentActivity
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.db.AppDatabase
import de.herrmann_engel.rbv.db.DbCollection
import de.herrmann_engel.rbv.db.utils.DbHelperDelete
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.utils.runInBackground
import de.herrmann_engel.rbv.utils.showConfirmDialog

/**
 * Dialogs for actions on one or more collections: delete.
 * The listener is notified after collections have been deleted.
 */
class CollectionActions(val activity: ComponentActivity, private val listener: Listener) {
    interface Listener {
        fun deletedCollections(collectionIds: ArrayList<Int>)
    }

    /**
     * Asks for confirmation and deletes the collections with their packs. If multiple collections
     * or a collection containing cards are to be deleted, a second confirmation is required
     * (forceDelete is set for the second dialog) and the cards are deleted as well.
     */
    private fun delete(collections: ArrayList<DbCollection>, forceDelete: Boolean) {
        val dbHelperGet = DbHelperGet(activity)
        val text = when {
            forceDelete -> null
            collections.size > 1 -> {
                val numberOfCards = collections.sumOf { dbHelperGet.countCardsInCollection(it.uid) }
                activity.resources.getQuantityString(
                    R.plurals.delete_multiple_collections,
                    numberOfCards,
                    collections.size,
                    numberOfCards
                )
            }

            dbHelperGet.countCardsInCollection(collections[0].uid) > 0 ->
                activity.getString(R.string.delete_collection_with_cards)

            else -> null
        }
        showConfirmDialog(activity, activity.getString(R.string.delete), text) {
            val withoutCards = collections.size <= 1 &&
                dbHelperGet.countCardsInCollection(collections[0].uid) == 0
            if (withoutCards || forceDelete) {
                val dbHelperDelete = DbHelperDelete(activity)
                val collectionIds = ArrayList(collections.map { it.uid })
                runInBackground(activity, {
                    // All collections are deleted or none
                    AppDatabase.getInstance(activity).runInTransaction {
                        collections.forEach { dbHelperDelete.deleteCollection(it, forceDelete) }
                    }
                }) { listener.deletedCollections(collectionIds) }
            } else {
                delete(collections, true)
            }
        }
    }

    fun delete(collections: ArrayList<DbCollection>) {
        delete(collections, false)
    }

    fun delete(collection: DbCollection) {
        delete(arrayListOf(collection))
    }

}
