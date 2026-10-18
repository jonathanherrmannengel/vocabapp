package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.widget.Toast
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.PackActions
import de.herrmann_engel.rbv.databinding.ActivityViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.DB_Pack
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.formatDateTime

/**
 * Details of a pack (name, emoji, description, creation date) with options to edit,
 * delete and move it.
 */
class ViewPack : RBVActivity(), PackActions.Listener {
    private lateinit var binding: ActivityViewCollectionOrPackBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private lateinit var pack: DB_Pack
    private var packNo = 0
    private var collectionNo = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCollectionOrPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        collectionNo = intent.extras!!.getInt("collection")
        packNo = intent.extras!!.getInt("pack")
        setUpEdgeToEdge(binding.viewCollectionOrPackLayout)
    }

    // Load the pack again on every resume, as it may have been edited
    public override fun onResume() {
        super.onResume()
        val settings = AppSettings(this)
        val increaseFontSize = settings.uiFontSizeBig
        // Close if the pack no longer exists (e.g. deleted in the meantime)
        pack = dbHelperGet.getSinglePack(packNo) ?: run {
            finish()
            return
        }
        title = getString(R.string.pack_details)
        if (pack.emoji.isNullOrBlank()) {
            binding.collectionOrPackEmoji.visibility = View.GONE
        } else {
            binding.collectionOrPackEmoji.visibility = View.VISIBLE
            binding.collectionOrPackEmoji.text = pack.emoji
        }
        binding.collectionOrPackName.text = pack.name
        if (pack.desc.isNullOrBlank()) {
            binding.collectionOrPackDesc.visibility = View.GONE
        } else {
            binding.collectionOrPackDesc.visibility = View.VISIBLE
            binding.collectionOrPackDesc.text = pack.desc
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
        binding.collectionOrPackDate.text = formatDateTime(this, pack.date)
        resources.colorAt(R.array.pack_color_background, pack.colors)
            ?.let { binding.root.setBackgroundColor(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_pack, menu)
        menu.findItem(R.id.menu_view_pack_edit).setOnMenuItemClickListener {
            val intent = Intent(this, EditPack::class.java)
            intent.putExtra("pack", packNo)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_pack_delete).setOnMenuItemClickListener {
            PackActions(this, this).delete(pack)
            return@setOnMenuItemClickListener true
        }
        // Moving is only offered when the pack was opened from within its collection
        if (collectionNo == Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL) {
            menu.findItem(R.id.menu_view_pack_move).isVisible = false
        } else {
            menu.findItem(R.id.menu_view_pack_move).setOnMenuItemClickListener {
                PackActions(this, this).move(pack)
                return@setOnMenuItemClickListener true
            }
        }
        return true
    }

    override fun deletedPacks(packIds: ArrayList<Int>) {
        val intent = Intent(this, ListPacks::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    // Show the pack list of the new collection
    override fun movedPacks(packIds: ArrayList<Int>) {
        try {
            // Close if the pack no longer exists (e.g. deleted in the meantime)
            pack = dbHelperGet.getSinglePack(packNo) ?: run {
                finish()
                return
            }
            collectionNo = pack.collection
            val intent = Intent(this, ListPacks::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("collection", collectionNo)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }
}
