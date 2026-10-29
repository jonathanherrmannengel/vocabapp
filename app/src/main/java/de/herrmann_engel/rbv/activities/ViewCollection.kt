package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.CollectionActions
import de.herrmann_engel.rbv.databinding.ActivityViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.DbCollection
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.ui.showDetails
import de.herrmann_engel.rbv.utils.getCollectionId
import de.herrmann_engel.rbv.utils.putCollectionId

/**
 * Details of a collection (name, emoji, description, creation date) with options to edit and delete it.
 */
class ViewCollection : RBVActivity(), CollectionActions.Listener {
    private lateinit var binding: ActivityViewCollectionOrPackBinding
    private lateinit var dbHelperGet: DbHelperGet
    private lateinit var collection: DbCollection
    private var collectionId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCollectionOrPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DbHelperGet(this)
        collectionId = intent.getCollectionId() ?: run {
            finish()
            return
        }
        setUpEdgeToEdge(binding.viewCollectionOrPackLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_collection, menu)
        menu.findItem(R.id.menu_view_collection_edit).setOnMenuItemClickListener {
            val intent = Intent(this, EditCollection::class.java)
            intent.putCollectionId(collectionId)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_collection_delete).setOnMenuItemClickListener {
            CollectionActions(this, this).delete(collection)
            return@setOnMenuItemClickListener true
        }
        return true
    }

    // Load the collection again on every resume, as it may have been edited
    public override fun onResume() {
        super.onResume()
        // Close if the collection no longer exists (e.g. deleted in the meantime)
        collection = dbHelperGet.getSingleCollection(collectionId) ?: run {
            finish()
            return
        }
        title = getString(R.string.collection_details)
        binding.showDetails(
            collection.name,
            collection.emoji,
            collection.desc,
            collection.date,
            collection.colors,
            AppSettings(this).uiFontSizeBig
        )
    }

    override fun deletedCollections(collectionIds: ArrayList<Int>) {
        val intent = Intent(this, ListCollections::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }
}
