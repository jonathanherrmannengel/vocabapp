package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterPacks
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding
import de.herrmann_engel.rbv.databinding.DiaExportBinding
import de.herrmann_engel.rbv.db.DB_Pack_With_Meta
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.export_import.AsyncExport
import de.herrmann_engel.rbv.export_import.AsyncExportFinish
import de.herrmann_engel.rbv.export_import.AsyncExportProgress
import java.io.File
import java.util.Locale

class ListPacks : PackActionsActivity(), AsyncExportFinish, AsyncExportProgress {
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list_packs, menu)
        val startNewPack = menu.findItem(R.id.start_new_pack)
        val packDetails = menu.findItem(R.id.pack_details)
        val export = menu.findItem(R.id.export_single)
        if (collectionNo == Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL) {
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
            export.setOnMenuItemClickListener {
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
                    AsyncExport(
                        this,
                        this,
                        this,
                        collectionNo,
                        bindingStartExportDialog.diaExportIncludeMedia.isChecked
                    ).execute()
                    startExportDialog.dismiss()
                    Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show()
                }
                startExportDialog.show()
                return@setOnMenuItemClickListener true
            }
        }
        if (collectionNo >= 0) {
            title = dbHelperGet.getSingleCollection(collectionNo).name
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        if (settings.getBoolean("ui_bg_images", true)) {
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
        val uiFontSizeBig = settings.getBoolean("ui_font_size", false)
        adapter?.updateContent(loadContent()) ?: {
            adapter = AdapterPacks(loadContent(), uiFontSizeBig, collectionNo)
            binding.recDefault.adapter = adapter
            binding.recDefault.layoutManager = LinearLayoutManager(this)
        }()
        if (collectionNo >= 0) {
            val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
            val colorsBackground =
                resources.obtainTypedArray(R.array.pack_color_background_list)
            val minimalLength = colorsStatusBar.length().coerceAtMost(colorsBackground.length())
            val collectionColors = dbHelperGet.getSingleCollection(collectionNo).colors
            if (collectionColors in 0..<minimalLength) {
                val colorStatusBar = colorsStatusBar.getColor(collectionColors, 0)
                val colorBackground = colorsBackground.getColor(collectionColors, 0)
                supportActionBar?.setBackgroundDrawable(colorStatusBar.toDrawable())
                binding.root.setBackgroundColor(colorBackground)
            }
            colorsStatusBar.recycle()
            colorsBackground.recycle()
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

    override fun exportCardsResult(result: File?) {
        runOnUiThread {
            if (result == null) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
            } else {
                val share = Intent(Intent.ACTION_SEND)
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                share.type = Globals.EXPORT_FILE_TYPE
                share.putExtra(
                    Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                        this,
                        "$packageName.fileprovider", result
                    )
                )
                startActivity(
                    Intent.createChooser(
                        share,
                        getString(R.string.export_cards)
                    )
                )
            }
        }
    }

    override fun exportCardsProgress(progress: String) {
        runOnUiThread { Toast.makeText(this, progress, Toast.LENGTH_SHORT).show() }
    }


    override fun deletedPacks(packIds: ArrayList<Int>) {
        adapter!!.updateContent(loadContent())

    }

    override fun movedPacks(packIds: ArrayList<Int>) {
        adapter!!.updateContent(loadContent())
    }
}
