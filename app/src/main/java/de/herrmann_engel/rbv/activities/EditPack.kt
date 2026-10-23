package de.herrmann_engel.rbv.activities

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityCollectionOrPackFormBinding
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.ui.CollectionOrPackForm
import de.herrmann_engel.rbv.utils.getPackId

/**
 * Form to edit the pack passed as "pack" extra: name, description, emoji and color.
 */
class EditPack : RBVActivity() {
    private lateinit var form: CollectionOrPackForm
    private lateinit var pack: DB_Pack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCollectionOrPackFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val packId = intent.getPackId() ?: run {
            finish()
            return
        }
        // Close if the pack no longer exists (e.g. deleted in the meantime)
        pack = DB_Helper_Get(this).getSinglePack(packId) ?: run {
            finish()
            return
        }
        form = CollectionOrPackForm(
            this,
            binding,
            R.string.pack_name,
            CollectionOrPackForm.Values(pack.name, pack.desc, pack.emoji, pack.colors)
        )
        setUpEdgeToEdge(binding.collectionOrPackLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.menu_edit_save).setOnMenuItemClickListener {
            pack.name = form.name
            pack.desc = form.desc
            pack.emoji = form.emoji
            pack.colors = form.selectedColors
            if (DB_Helper_Update(this).updatePack(pack)) {
                finish()
            } else {
                Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show()
            }
            return@setOnMenuItemClickListener true
        }
        return true
    }
}
