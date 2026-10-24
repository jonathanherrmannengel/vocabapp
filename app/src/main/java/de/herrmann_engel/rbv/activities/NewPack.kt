package de.herrmann_engel.rbv.activities

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityCollectionOrPackFormBinding
import de.herrmann_engel.rbv.db.utils.DbHelperCreate
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.ui.CollectionOrPackForm
import de.herrmann_engel.rbv.utils.getCollectionId

/**
 * Form to create a new pack in the collection passed as "collection" extra: name, description,
 * emoji and color. The color of the collection is preselected.
 */
class NewPack : RBVActivity() {
    private lateinit var form: CollectionOrPackForm
    private var collectionId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCollectionOrPackFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        collectionId = intent.getCollectionId() ?: run {
            finish()
            return
        }
        // Close if the collection no longer exists (e.g. deleted in the meantime)
        val collection = DbHelperGet(this).getSingleCollection(collectionId) ?: run {
            finish()
            return
        }
        form = CollectionOrPackForm(
            this,
            binding,
            R.string.pack_name,
            CollectionOrPackForm.Values("", "", "", collection.colors)
        )
        setUpEdgeToEdge(binding.collectionOrPackLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        menu.findItem(R.id.menu_add_insert).setOnMenuItemClickListener {
            try {
                DbHelperCreate(this).createPack(
                    form.name,
                    form.desc,
                    collectionId,
                    form.selectedColors,
                    form.emoji,
                    System.currentTimeMillis() / 1000L
                )
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show()
            }
            return@setOnMenuItemClickListener true
        }
        return true
    }
}
