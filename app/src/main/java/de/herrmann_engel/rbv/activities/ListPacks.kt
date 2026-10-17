package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.PackActions
import de.herrmann_engel.rbv.adapters.AdapterPacks
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding
import de.herrmann_engel.rbv.databinding.DiaExportBinding
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.export_import.AsyncExport
import de.herrmann_engel.rbv.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Pack list of the collection passed as "collection" extra, or of all collections if it is
 * LIST_CARDS_GET_DB_COLLECTIONS_ALL. A single collection can be exported and shared from here.
 */
class ListPacks : RBVActivity(), PackActions.Listener {
    private lateinit var binding: ActivityDefaultRecBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private var adapter: AdapterPacks? = null
    private var collectionNo = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefaultRecBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        collectionNo = intent.extras!!.getInt("collection")
        // Edge-to-edge: keep the content clear of the system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.recDefault) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        enableEdgeToEdge()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list_packs, menu)
        val startNewPack = menu.findItem(R.id.start_new_pack)
        val packDetails = menu.findItem(R.id.pack_details)
        val export = menu.findItem(R.id.export_single)
        // Collection-specific actions are only available for an existing single collection
        if (collectionNo == Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL || !dbHelperGet.existsCollection(
                collectionNo
            )
        ) {
            startNewPack.isVisible = false
            packDetails.isVisible = false
            export.isVisible = false
        } else {
            startNewPack.setOnMenuItemClickListener {
                val intent = Intent(this, NewPack::class.java)
                intent.putExtra("collection", collectionNo)
                this.startActivity(intent)
                return@setOnMenuItemClickListener true
            }
            packDetails.setOnMenuItemClickListener {
                val intent = Intent(this, ViewCollection::class.java)
                intent.putExtra("collection", collectionNo)
                this.startActivity(intent)
                return@setOnMenuItemClickListener true
            }
            // Export dialog for this collection (settings are never included)
            export.setOnMenuItemClickListener {
                if (showIfImportExportRunning()) {
                    return@setOnMenuItemClickListener true
                }
                val startExportDialog = Dialog(this, R.style.dia_view)
                val bindingStartExportDialog = DiaExportBinding.inflate(
                    layoutInflater
                )
                startExportDialog.setContentView(bindingStartExportDialog.root)
                startExportDialog.setTitle(
                    String.format(
                        Locale.ROOT,
                        resources.getString(R.string.import_export_options),
                        resources.getString(R.string.export_title)
                    )
                )
                startExportDialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                bindingStartExportDialog.diaExportIncludeSettings.isChecked = false
                bindingStartExportDialog.diaExportIncludeSettings.visibility = View.GONE
                bindingStartExportDialog.diaExportIncludeMedia.isChecked = true
                bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.visibility =
                    if (bindingStartExportDialog.diaExportIncludeMedia.isChecked) View.VISIBLE else View.GONE
                bindingStartExportDialog.diaExportIncludeMedia.setOnCheckedChangeListener { _, c: Boolean ->
                    if (c) {
                        bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.visibility =
                            View.VISIBLE
                    } else {
                        bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.visibility =
                            View.GONE
                    }
                }
                bindingStartExportDialog.diaExportStart.setOnClickListener {
                    showImportExportStarted(
                        AsyncExport(
                            this,
                            collectionNo,
                            bindingStartExportDialog.diaExportIncludeMedia.isChecked
                        ).execute()
                    )
                    startExportDialog.dismiss()
                }
                startExportDialog.show()
                return@setOnMenuItemClickListener true
            }
        }
        if (collectionNo >= 0) {
            title = dbHelperGet.getSingleCollection(collectionNo)?.name
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        val settings = AppSettings(this)
        if (settings.uiBackgroundImages) {
            binding.backgroundImage.visibility = View.VISIBLE
            binding.backgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_packs
                )
            )
        } else {
            binding.backgroundImage.visibility = View.GONE
        }
        refreshContent()
        if (collectionNo >= 0) {
            val colorsBackground =
                resources.obtainTypedArray(R.array.pack_color_background_list)
            val minimalLength = colorsBackground.length()
            dbHelperGet.getSingleCollection(collectionNo)?.let {
                if (it.colors in 0..<minimalLength) {
                    val colorBackground = colorsBackground.getColor(it.colors, 0)
                    binding.root.setBackgroundColor(colorBackground)
                }
            }
            colorsBackground.recycle()
        }
    }

    /**
     * Loads the packs including the placeholder for "all cards" as first item. For long lists,
     * the cards per pack are not counted to save time.
     */
    // Load the packs in the background; create the adapter on first load, update it (and the
    // font size setting) afterwards
    private fun refreshContent() {
        val settings = AppSettings(this)
        val uiFontSizeBig = settings.uiFontSizeBig
        lifecycleScope.launch {
            val content = withContext(Dispatchers.IO) { loadContent() }
            adapter?.updateSettingsAndContent(content, uiFontSizeBig) ?: run {
                adapter = AdapterPacks(
                    content, uiFontSizeBig, collectionNo, PackActions(this@ListPacks, this@ListPacks)
                )
                binding.recDefault.adapter = adapter
                binding.recDefault.layoutManager =
                    LinearLayoutManager(this@ListPacks)
            }
        }
    }

    private fun loadContent(): MutableList<DB_Pack_With_Meta> {
        val currentList: MutableList<DB_Pack_With_Meta> =
            if (collectionNo == Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL) {
                if (dbHelperGet.countPacks() > Globals.MAX_SIZE_COLLECTIONS_OR_PACKS_LIST_COUNTER) {
                    dbHelperGet.allPacksWithMetaNoCounter
                } else {
                    dbHelperGet.allPacksWithMeta
                }
            } else {
                if (dbHelperGet.countPacksInCollection(collectionNo) > Globals.MAX_SIZE_COLLECTIONS_OR_PACKS_LIST_COUNTER) {
                    dbHelperGet.getAllPacksWithMetaNoCounterByCollection(collectionNo)
                } else {
                    dbHelperGet.getAllPacksWithMetaByCollection(collectionNo)
                }
            }
        val fixedFirstItemPlaceholder = DB_Pack_With_Meta()
        if (collectionNo == Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL) {
            fixedFirstItemPlaceholder.counter = dbHelperGet.countCards()
        } else {
            fixedFirstItemPlaceholder.counter = dbHelperGet.countCardsInCollection(collectionNo)
        }
        currentList.add(0, fixedFirstItemPlaceholder)
        return currentList
    }

    // Reload the list after an import, which may have added packs or changed the font size setting
    override fun onImportFinished() {
        refreshContent()
    }

    override fun deletedPacks(packIds: ArrayList<Int>) {
        refreshContent()
    }

    override fun movedPacks(packIds: ArrayList<Int>) {
        refreshContent()
    }
}
