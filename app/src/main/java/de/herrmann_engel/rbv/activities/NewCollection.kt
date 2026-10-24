package de.herrmann_engel.rbv.activities

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityCollectionOrPackFormBinding
import de.herrmann_engel.rbv.db.utils.DbHelperCreate
import de.herrmann_engel.rbv.ui.CollectionOrPackForm

/**
 * Form to create a new collection: name, description, emoji and color.
 */
class NewCollection : RBVActivity() {
    private lateinit var form: CollectionOrPackForm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCollectionOrPackFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        form = CollectionOrPackForm(
            this,
            binding,
            R.string.collection_name,
            CollectionOrPackForm.Values("", "", "", 0)
        )
        setUpEdgeToEdge(binding.collectionOrPackLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        menu.findItem(R.id.menu_add_insert).setOnMenuItemClickListener {
            try {
                DbHelperCreate(this).createCollection(
                    form.name,
                    form.desc,
                    form.selectedColors,
                    form.emoji
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
