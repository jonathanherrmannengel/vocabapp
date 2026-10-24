package de.herrmann_engel.rbv.activities

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityCollectionOrPackFormBinding
import de.herrmann_engel.rbv.db.DbCollection
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.db.utils.DbHelperUpdate
import de.herrmann_engel.rbv.ui.CollectionOrPackForm
import de.herrmann_engel.rbv.utils.getCollectionId

/**
 * Form to edit the collection passed as "collection" extra: name, description, emoji and color.
 */
class EditCollection : RBVActivity() {
    private lateinit var form: CollectionOrPackForm
    private lateinit var collection: DbCollection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCollectionOrPackFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val collectionId = intent.getCollectionId() ?: run {
            finish()
            return
        }
        // Close if the collection no longer exists (e.g. deleted in the meantime)
        collection = DbHelperGet(this).getSingleCollection(collectionId) ?: run {
            finish()
            return
        }
        form = CollectionOrPackForm(
            this,
            binding,
            R.string.collection_name,
            CollectionOrPackForm.Values(
                collection.name,
                collection.desc,
                collection.emoji,
                collection.colors
            )
        )
        setUpEdgeToEdge(binding.collectionOrPackLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.menu_edit_save).setOnMenuItemClickListener {
            collection.name = form.name
            collection.desc = form.desc
            collection.emoji = form.emoji
            collection.colors = form.selectedColors
            if (DbHelperUpdate(this).updateCollection(collection)) {
                finish()
            } else {
                Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show()
            }
            return@setOnMenuItemClickListener true
        }
        return true
    }
}
