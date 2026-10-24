package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.PackActions
import de.herrmann_engel.rbv.adapters.AdapterListHeader
import de.herrmann_engel.rbv.adapters.AdapterPacks
import de.herrmann_engel.rbv.adapters.iconText
import de.herrmann_engel.rbv.cardlist.CardSource
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding
import de.herrmann_engel.rbv.databinding.DiaExportBinding
import de.herrmann_engel.rbv.db.PackWithMeta
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.export_import.ImportExportWork
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.createDialog
import de.herrmann_engel.rbv.utils.getListCollection
import de.herrmann_engel.rbv.utils.putCardSource
import de.herrmann_engel.rbv.utils.putCollectionId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Pack list of the collection passed as "listCollection" extra (see putListCollection), or of all
 * collections without it. A single collection can be exported and shared from here.
 */
class ListPacks : RBVActivity(), PackActions.Listener {
    private lateinit var binding: ActivityDefaultRecBinding
    private lateinit var dbHelperGet: DbHelperGet
    private var adapter: AdapterPacks? = null
    // First row: all cards of the listed packs, or a welcome message without packs
    private var headerAdapter: AdapterListHeader? = null
    // null: packs of all collections
    private var collectionId: Int? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefaultRecBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DbHelperGet(this)
        collectionId = intent.getListCollection()
        setUpEdgeToEdge(binding.recDefault)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list_packs, menu)
        val startNewPack = menu.findItem(R.id.start_new_pack)
        val packDetails = menu.findItem(R.id.pack_details)
        val export = menu.findItem(R.id.export_single)
        val collectionId = collectionId
        // Collection-specific actions are only available for an existing single collection
        if (collectionId == null || !dbHelperGet.existsCollection(collectionId)) {
            startNewPack.isVisible = false
            packDetails.isVisible = false
            export.isVisible = false
        } else {
            startNewPack.setOnMenuItemClickListener {
                val intent = Intent(this, NewPack::class.java)
                intent.putCollectionId(collectionId)
                this.startActivity(intent)
                return@setOnMenuItemClickListener true
            }
            packDetails.setOnMenuItemClickListener {
                val intent = Intent(this, ViewCollection::class.java)
                intent.putCollectionId(collectionId)
                this.startActivity(intent)
                return@setOnMenuItemClickListener true
            }
            export.setOnMenuItemClickListener {
                prepareImportExport { showExportOptions(collectionId) }
                return@setOnMenuItemClickListener true
            }
        }
        collectionId?.let { title = dbHelperGet.getSingleCollection(it)?.name }
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
        collectionId?.let {
            resources.colorAt(
                R.array.pack_color_background_list,
                dbHelperGet.getSingleCollection(it)?.colors
            )?.let { binding.root.setBackgroundColor(it) }
        }
    }

    // Export dialog for the collection (settings are never included)
    private fun showExportOptions(collectionId: Int) {
        val bindingStartExportDialog = DiaExportBinding.inflate(layoutInflater)
        val startExportDialog = createDialog(
            this,
            bindingStartExportDialog.root,
            String.format(
                Locale.ROOT,
                getString(R.string.import_export_options),
                getString(R.string.export_title)
            )
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
                ImportExportWork.startCollectionExport(
                    this,
                    collectionId,
                    bindingStartExportDialog.diaExportIncludeMedia.isChecked
                )
            )
            startExportDialog.dismiss()
        }
        startExportDialog.show()
    }

    // Load the packs in the background; create the adapters on first load, update them (and the
    // font size setting) afterwards
    private fun refreshContent() {
        val settings = AppSettings(this)
        val uiFontSizeBig = settings.uiFontSizeBig
        lifecycleScope.launch {
            val (packs, cardCount) = withContext(Dispatchers.IO) { Pair(loadPacks(), countCards()) }
            headerAdapter?.update(cardCount, packs.isEmpty(), uiFontSizeBig)
            adapter?.updateSettingsAndContent(packs, uiFontSizeBig) ?: run {
                val header = createHeaderAdapter(cardCount, packs.isEmpty(), uiFontSizeBig)
                val packsAdapter = AdapterPacks(
                    packs, uiFontSizeBig, collectionId, PackActions(this@ListPacks, this@ListPacks)
                )
                headerAdapter = header
                adapter = packsAdapter
                binding.recDefault.adapter = ConcatAdapter(header, packsAdapter)
                binding.recDefault.layoutManager =
                    LinearLayoutManager(this@ListPacks)
            }
        }
    }

    private fun createHeaderAdapter(
        cardCount: Int,
        noPacks: Boolean,
        uiFontSizeBig: Boolean
    ): AdapterListHeader {
        val collectionId = collectionId
        return AdapterListHeader(
            name = getString(R.string.all_cards),
            desc = getString(
                if (collectionId == null) {
                    R.string.all_cards_desc
                } else {
                    R.string.all_cards_desc_by_pack
                }
            ),
            // Only in a single collection packs can be created here ("+" button)
            welcomeText = { context ->
                if (collectionId == null) {
                    context.getString(R.string.welcome_pack)
                } else {
                    iconText(
                        context,
                        context.getString(R.string.welcome_pack) + " " +
                            context.getString(R.string.welcome_pack_create),
                        mapOf('+' to R.drawable.outline_add_24)
                    )
                }
            },
            onClick = {
                val intent = Intent(this, ListCards::class.java)
                intent.putCardSource(
                    collectionId?.let { CardSource.Collection(it) } ?: CardSource.AllCards
                )
                startActivity(intent)
            },
            counter = cardCount,
            listEmpty = noPacks,
            uiFontSizeBig = uiFontSizeBig
        )
    }

    // Number of cards in the listed packs
    private fun countCards(): Int =
        collectionId?.let { dbHelperGet.countCardsInCollection(it) } ?: dbHelperGet.countCards()

    // Loads the packs with the number of cards in each
    private fun loadPacks(): MutableList<PackWithMeta> =
        collectionId?.let { dbHelperGet.getAllPacksWithMetaByCollection(it) }
            ?: dbHelperGet.getAllPacksWithMeta()

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
