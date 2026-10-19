package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.Globals.IMPORT_MODE_SIMPLE_LIST
import de.herrmann_engel.rbv.Globals.MAX_SIZE_COLLECTIONS_OR_PACKS_LIST_COUNTER
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterCollections
import de.herrmann_engel.rbv.adapters.AdapterListHeader
import de.herrmann_engel.rbv.adapters.iconText
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding
import de.herrmann_engel.rbv.databinding.DiaExportBinding
import de.herrmann_engel.rbv.databinding.DiaImportBinding
import de.herrmann_engel.rbv.db.DB_Collection_With_Meta
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.export_import.AsyncExport
import de.herrmann_engel.rbv.export_import.AsyncImport
import de.herrmann_engel.rbv.media.MediaFileManager
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.createDialog
import de.herrmann_engel.rbv.utils.putListCollection
import de.herrmann_engel.rbv.utils.showConfirmDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Start screen: list of all collections. The menu offers creating collections, import and
 * export of all data, advanced search, media management, settings and app information.
 */
class ListCollections : RBVActivity() {
    private val mediaFileManager = MediaFileManager(this)
    private lateinit var binding: ActivityDefaultRecBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private var adapter: AdapterCollections? = null
    // First row: all packs, or a welcome message without collections
    private var headerAdapter: AdapterListHeader? = null
    // Import options chosen in the import dialog, used once the user has picked the file
    private var importMode = 0
    private var importIncludeSettings = false
    private var importIncludeMedia = false
    // Result of the file picker for the import: start the import in the background
    private val launcherImportFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            showImportExportStarted(
                AsyncImport(
                    this,
                    result.data?.data!!,
                    importMode,
                    importIncludeSettings,
                    importIncludeMedia
                ).execute()
            )
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
    }
    // Export options chosen in the export dialog, used once the user has picked the destination
    private var exportIncludeSettings = false
    private var exportIncludeMedia = false
    // Result of the file picker for the export destination: start the export in the background
    private val launcherExportFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            contentResolver.takePersistableUriPermission(
                uri!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            showImportExportStarted(
                AsyncExport(this, exportIncludeSettings, exportIncludeMedia, uri).execute()
            )
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefaultRecBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        setTitle(R.string.app_name)
        setUpEdgeToEdge(binding.recDefault)
        // Ask for the media folder if media entries exist but the folder is missing or inaccessible
        mediaFileManager.askForFolderIfNeeded()
        // Housekeeping in the background on app start
        lifecycleScope.launch(Dispatchers.IO) {
            cleanUp()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list_collections, menu)
        menu.findItem(R.id.menu_list_collections_new).setOnMenuItemClickListener {
            this.startActivity(Intent(this, NewCollection::class.java))
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_list_collections_import).setOnMenuItemClickListener {
            if (showIfImportExportRunning()) {
                return@setOnMenuItemClickListener true
            }
            val bindingStartImportDialog = DiaImportBinding.inflate(layoutInflater)
            val startImportDialog = createDialog(
                this,
                bindingStartImportDialog.root,
                String.format(
                    Locale.ROOT,
                    getString(R.string.import_export_options),
                    getString(R.string.import_title)
                )
            )
            // Defaults: merge into existing data; for an empty app, import everything including settings
            if (dbHelperGet.hasCollections()) {
                bindingStartImportDialog.diaImportRadioIntegrate.isChecked = true
                bindingStartImportDialog.diaImportRadioDuplicates.isChecked = false
                bindingStartImportDialog.diaImportIncludeSettings.isChecked = false
            } else {
                bindingStartImportDialog.diaImportRadioIntegrate.isChecked = false
                bindingStartImportDialog.diaImportRadioDuplicates.isChecked = true
                bindingStartImportDialog.diaImportIncludeSettings.isChecked = true
            }
            bindingStartImportDialog.diaImportRadioSkip.isChecked = false
            bindingStartImportDialog.diaImportIncludeMedia.isChecked = true
            bindingStartImportDialog.diaImportIncludeMedia.setOnCheckedChangeListener { _, c: Boolean ->
                if (c) {
                    bindingStartImportDialog.diaImportIncludeMediaWarnNoFiles.visibility =
                        View.VISIBLE
                } else {
                    bindingStartImportDialog.diaImportIncludeMediaWarnNoFiles.visibility = View.GONE
                }
            }
            bindingStartImportDialog.diaImportStart.setOnClickListener {
                importMode =
                    when (bindingStartImportDialog.diaImportRadio.checkedRadioButtonId) {
                        R.id.dia_import_radio_integrate -> {
                            Globals.IMPORT_MODE_INTEGRATE
                        }

                        R.id.dia_import_radio_duplicates -> {
                            Globals.IMPORT_MODE_DUPLICATES
                        }

                        else -> {
                            Globals.IMPORT_MODE_SKIP
                        }
                    }
                importIncludeSettings = bindingStartImportDialog.diaImportIncludeSettings.isChecked
                importIncludeMedia = bindingStartImportDialog.diaImportIncludeMedia.isChecked
                startImport()
                startImportDialog.dismiss()
            }
            startImportDialog.show()
            return@setOnMenuItemClickListener true
        }
        // Import of a simple CSV list of cards into a new pack
        menu.findItem(R.id.menu_list_collections_import_simple_list).setOnMenuItemClickListener {
            if (showIfImportExportRunning()) {
                return@setOnMenuItemClickListener true
            }
            showConfirmDialog(
                this,
                getString(R.string.import_simple_list),
                getString(R.string.import_simple_list_info)
            ) {
                importMode = IMPORT_MODE_SIMPLE_LIST
                importIncludeSettings = false
                importIncludeMedia = false
                startImport()
            }
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_list_collections_export).setOnMenuItemClickListener {
            if (showIfImportExportRunning()) {
                return@setOnMenuItemClickListener true
            }
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
            bindingStartExportDialog.diaExportIncludeMedia.isChecked = true
            bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.visibility =
                if (bindingStartExportDialog.diaExportIncludeMedia.isChecked) View.VISIBLE else View.GONE
            bindingStartExportDialog.diaExportIncludeMedia.setOnCheckedChangeListener { _, c: Boolean ->
                if (c) {
                    bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.visibility =
                        View.VISIBLE
                } else {
                    bindingStartExportDialog.diaExportIncludeMediaWarnNoFiles.visibility = View.GONE
                }
            }
            bindingStartExportDialog.diaExportStart.setOnClickListener {
                exportIncludeSettings = bindingStartExportDialog.diaExportIncludeSettings.isChecked
                exportIncludeMedia = bindingStartExportDialog.diaExportIncludeMedia.isChecked
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                intent.type = Globals.EXPORT_FILE_TYPE
                intent.putExtra(
                    Intent.EXTRA_TITLE,
                    Globals.EXPORT_FILE_NAME + "." + Globals.EXPORT_FILE_EXTENSION
                )
                launcherExportFile.launch(intent)
                startExportDialog.dismiss()
            }
            startExportDialog.show()
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_list_collections_start_advanced_search).setOnMenuItemClickListener {
            this.startActivity(Intent(this, AdvancedSearch::class.java))
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_list_collections_start_manage_media).setOnMenuItemClickListener {
            this.startActivity(Intent(this, ManageMedia::class.java))
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_list_collections_start_settings).setOnMenuItemClickListener {
            this.startActivity(Intent(this, Settings::class.java))
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_list_collections_start_about_app).setOnMenuItemClickListener {
            this.startActivity(Intent(this, AppInformation::class.java))
            return@setOnMenuItemClickListener true
        }
        return true
    }

    // Hide menu items that are useless without collections, cards or media
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val exportAllMenuItem = menu.findItem(R.id.menu_list_collections_export)
        val startAdvancedSearchMenuItem =
            menu.findItem(R.id.menu_list_collections_start_advanced_search)
        val startManageMediaMenuItem = menu.findItem(R.id.menu_list_collections_start_manage_media)
        exportAllMenuItem.isVisible = dbHelperGet.hasCollections()
        startAdvancedSearchMenuItem.isVisible = dbHelperGet.hasCards()
        startManageMediaMenuItem.isVisible = dbHelperGet.hasMedia()
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
                    R.drawable.bg_collections
                )
            )
        } else {
            binding.backgroundImage.visibility = View.GONE
        }
        refreshContent()
    }

    /**
     * Updates the .nomedia file and deletes cached files (e.g. old exports) older than one day.
     */
    private fun cleanUp() {
        mediaFileManager.handleNoMediaFile()
        val cacheDir = cacheDir
        val files = cacheDir.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isFile && System.currentTimeMillis() - file.lastModified() > 86400000) {
                    file.delete()
                }
            }
        }
    }

    // Load the collections in the background; create the adapters on first load, update them
    // afterwards
    private fun refreshContent() {
        val settings = AppSettings(this)
        val uiFontSizeBig = settings.uiFontSizeBig
        lifecycleScope.launch {
            val (collections, packCount) = withContext(Dispatchers.IO) {
                Pair(loadCollections(), dbHelperGet.countPacks())
            }
            headerAdapter?.update(packCount, collections.isEmpty(), uiFontSizeBig)
            adapter?.updateSettingsAndContent(collections, uiFontSizeBig) ?: run {
                val header = createHeaderAdapter(packCount, collections.isEmpty(), uiFontSizeBig)
                val collectionsAdapter = AdapterCollections(collections, uiFontSizeBig)
                headerAdapter = header
                adapter = collectionsAdapter
                binding.recDefault.adapter = ConcatAdapter(header, collectionsAdapter)
                binding.recDefault.layoutManager =
                    LinearLayoutManager(this@ListCollections)
            }
        }
    }

    private fun createHeaderAdapter(
        packCount: Int,
        noCollections: Boolean,
        uiFontSizeBig: Boolean
    ): AdapterListHeader = AdapterListHeader(
        name = getString(R.string.all_packs),
        desc = getString(R.string.all_packs_desc),
        // Refers to the "+" (new collection) and "↓" (import) buttons
        welcomeText = { context ->
            iconText(
                context,
                context.getString(R.string.welcome_collection),
                mapOf('+' to R.drawable.outline_add_24, '↓' to R.drawable.outline_file_download_24)
            )
        },
        onClick = {
            val intent = Intent(this, ListPacks::class.java)
            intent.putListCollection(null)
            startActivity(intent)
        },
        counter = packCount,
        listEmpty = noCollections,
        uiFontSizeBig = uiFontSizeBig
    )

    // Loads the collections; for long lists, the packs per collection are not counted to save time
    private fun loadCollections(): MutableList<DB_Collection_With_Meta> =
        if (dbHelperGet.countCollections() > MAX_SIZE_COLLECTIONS_OR_PACKS_LIST_COUNTER) {
            dbHelperGet.allCollectionsWithMetaNoCounter
        } else {
            dbHelperGet.allCollectionsWithMeta
        }

    // Reload the list after an import (started here or elsewhere)
    override fun onImportFinished() {
        refreshContent()
    }


    // Lets the user pick the file to import
    private fun startImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        intent.type = "text/*"
        launcherImportFile.launch(intent)
    }
}
