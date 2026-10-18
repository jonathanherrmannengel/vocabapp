package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.WindowManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityViewCollectionOrPackBinding
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.db.DB_Collection
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.formatDateTime

/**
 * Details of a collection (name, emoji, description, creation date) with options to edit and delete it.
 */
class ViewCollection : RBVActivity() {
    private lateinit var binding: ActivityViewCollectionOrPackBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private lateinit var collection: DB_Collection
    private var collectionNo = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCollectionOrPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        collectionNo = intent.extras!!.getInt("collection")
        setUpEdgeToEdge(binding.viewCollectionOrPackLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_collection, menu)
        menu.findItem(R.id.menu_view_collection_edit).setOnMenuItemClickListener {
            val intent = Intent(this, EditCollection::class.java)
            intent.putExtra("collection", collectionNo)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_collection_delete).setOnMenuItemClickListener {
            deleteCollection(false)
            return@setOnMenuItemClickListener true
        }
        return true
    }

    // Load the collection again on every resume, as it may have been edited
    public override fun onResume() {
        super.onResume()
        val settings = AppSettings(this)
        val increaseFontSize = settings.uiFontSizeBig
        // Close if the collection no longer exists (e.g. deleted in the meantime)
        collection = dbHelperGet.getSingleCollection(collectionNo) ?: run {
            finish()
            return
        }
        title = getString(R.string.collection_details)
        binding.collectionOrPackName.text = collection.name
        if (collection.emoji.isNullOrBlank()) {
            binding.collectionOrPackEmoji.visibility = View.GONE
        } else {
            binding.collectionOrPackEmoji.visibility = View.VISIBLE
            binding.collectionOrPackEmoji.text = collection.emoji
        }
        if (collection.desc.isNullOrBlank()) {
            binding.collectionOrPackDesc.visibility = View.GONE
        } else {
            binding.collectionOrPackDesc.visibility = View.VISIBLE
            binding.collectionOrPackDesc.text = collection.desc
        }
        if (increaseFontSize) {
            binding.collectionOrPackEmoji.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.details_name_size_big)
            )
            binding.collectionOrPackName.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.details_name_size_big)
            )
            binding.collectionOrPackDesc.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.details_desc_size_big)
            )
        }
        // Format the creation date (stored in seconds)
        binding.collectionOrPackDate.text = formatDateTime(this, collection.date)
        resources.colorAt(R.array.pack_color_background, collection.colors)
            ?.let { binding.root.setBackgroundColor(it) }
    }

    /**
     * Asks for confirmation and deletes the collection. If it contains packs, a second
     * confirmation is required (forceDelete is set for the second dialog) and the packs
     * are deleted with all their cards.
     */
    private fun deleteCollection(forceDelete: Boolean) {
        val confirmDeleteDialog = Dialog(this, R.style.dia_view)
        val bindingConfirmDeleteDialog = DiaConfirmBinding.inflate(layoutInflater)
        confirmDeleteDialog.setContentView(bindingConfirmDeleteDialog.root)
        confirmDeleteDialog.setTitle(resources.getString(R.string.delete))
        confirmDeleteDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        if (dbHelperGet.getAllPacksByCollection(collection.uid).isNotEmpty() && !forceDelete) {
            bindingConfirmDeleteDialog.diaConfirmDesc.setText(R.string.delete_collection_with_packs)
            bindingConfirmDeleteDialog.diaConfirmDesc.visibility = View.VISIBLE
        }
        bindingConfirmDeleteDialog.diaConfirmYes.setOnClickListener {
            if (dbHelperGet.getAllPacksByCollection(collection.uid).isEmpty() || forceDelete) {
                val dbHelperDelete = DB_Helper_Delete(this)
                dbHelperDelete.deleteCollection(collection, forceDelete)
                val intent = Intent(this, ListCollections::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            } else {
                deleteCollection(true)
            }
            confirmDeleteDialog.dismiss()
        }
        bindingConfirmDeleteDialog.diaConfirmNo.setOnClickListener { confirmDeleteDialog.dismiss() }
        confirmDeleteDialog.show()
    }
}
