package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.PackActions
import de.herrmann_engel.rbv.databinding.ActivityViewCollectionOrPackBinding
import de.herrmann_engel.rbv.db.DbPack
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.ui.showDetails
import de.herrmann_engel.rbv.utils.getListCollection
import de.herrmann_engel.rbv.utils.getPackId
import de.herrmann_engel.rbv.utils.putListCollection
import de.herrmann_engel.rbv.utils.putPackId

/**
 * Details of a pack (name, emoji, description, creation date) with options to edit,
 * delete and move it.
 */
class ViewPack : RBVActivity(), PackActions.Listener {
    private lateinit var binding: ActivityViewCollectionOrPackBinding
    private lateinit var dbHelperGet: DbHelperGet
    private lateinit var pack: DbPack
    private var packId = 0
    // Collection of the pack list the pack was opened from, null for the list of all packs
    private var listCollectionId: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCollectionOrPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DbHelperGet(this)
        listCollectionId = intent.getListCollection()
        packId = intent.getPackId() ?: run {
            finish()
            return
        }
        setUpEdgeToEdge(binding.viewCollectionOrPackLayout)
    }

    // Load the pack again on every resume, as it may have been edited
    public override fun onResume() {
        super.onResume()
        // Close if the pack no longer exists (e.g. deleted in the meantime)
        pack = dbHelperGet.getSinglePack(packId) ?: run {
            finish()
            return
        }
        title = getString(R.string.pack_details)
        binding.showDetails(
            pack.name,
            pack.emoji,
            pack.desc,
            pack.date,
            pack.colors,
            AppSettings(this).uiFontSizeBig
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_pack, menu)
        menu.findItem(R.id.menu_view_pack_edit).setOnMenuItemClickListener {
            val intent = Intent(this, EditPack::class.java)
            intent.putPackId(packId)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_pack_delete).setOnMenuItemClickListener {
            PackActions(this, this).delete(pack)
            return@setOnMenuItemClickListener true
        }
        // Moving is only offered when the pack was opened from within its collection
        if (listCollectionId == null) {
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
            pack = dbHelperGet.getSinglePack(packId) ?: run {
                finish()
                return
            }
            val intent = Intent(this, ListPacks::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putListCollection(pack.collectionId)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }
}
