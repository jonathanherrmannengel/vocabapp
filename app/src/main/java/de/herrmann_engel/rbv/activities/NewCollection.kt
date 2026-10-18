package de.herrmann_engel.rbv.activities

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityNewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create
import de.herrmann_engel.rbv.utils.showConfirmDialog

/**
 * Form to create a new collection.
 */
class NewCollection : RBVActivity() {
    private lateinit var binding: ActivityNewCollectionOrPackBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCollectionOrPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.newCollectionOrPackNameLayout.hint = String.format(
            getString(R.string.collection_or_pack_name_format),
            getString(R.string.collection_name), getString(R.string.collection_or_pack_name)
        )
        binding.newCollectionOrPackDescLayout.hint = String.format(
            getString(R.string.optional),
            getString(R.string.collection_or_pack_desc)
        )

        // Ask before discarding entered text
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val name = binding.newCollectionOrPackName.text.toString()
                val desc = binding.newCollectionOrPackDesc.text.toString()
                if (name.isBlank() && desc.isBlank()) {
                    finish()
                } else {
                    showConfirmDialog(this@NewCollection, getString(R.string.discard_changes)) {
                        finish()
                    }
                }
            }
        })

        setUpEdgeToEdge(binding.newCollectionOrPackLayout)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        menu.findItem(R.id.menu_add_insert).setOnMenuItemClickListener {
            val name = binding.newCollectionOrPackName.text.toString()
            val desc = binding.newCollectionOrPackDesc.text.toString()
            try {
                val dbHelperCreate = DB_Helper_Create(this)
                dbHelperCreate.createCollection(name, desc)
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
