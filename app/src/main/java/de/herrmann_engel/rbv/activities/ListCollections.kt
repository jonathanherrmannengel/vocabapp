package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.Globals.MAX_SIZE_COUNTER
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterCollections
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding
import de.herrmann_engel.rbv.databinding.DiaExportBinding
import de.herrmann_engel.rbv.databinding.DiaImportBinding
import de.herrmann_engel.rbv.db.DB_Collection_With_Meta
import de.herrmann_engel.rbv.db.utils.DB_Helper_Delete
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.export_import.AsyncExport
import de.herrmann_engel.rbv.export_import.AsyncExportFinish
import de.herrmann_engel.rbv.export_import.AsyncExportProgress
import de.herrmann_engel.rbv.export_import.AsyncImport
import de.herrmann_engel.rbv.export_import.AsyncImportFinish
import de.herrmann_engel.rbv.export_import.AsyncImportProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

class ListCollections : FileTools(), AsyncImportFinish, AsyncImportProgress, AsyncExportFinish,
    AsyncExportProgress {
    private lateinit var binding: ActivityDefaultRecBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private var adapter: AdapterCollections? = null
    private var importMode = 0
    private var importIncludeSettings = false
    private var importIncludeMedia = false
    private val launcherImportFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            AsyncImport(
                this,
                this,
                this,
                result.data?.data!!,
                importMode,
                importIncludeSettings,
                importIncludeMedia
            ).execute()
            Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
    }
    private var exportIncludeSettings = false
    private var exportIncludeMedia = false
    private val launcherExportFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            contentResolver.takePersistableUriPermission(
                uri!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            AsyncExport(this, this, this, exportIncludeSettings, exportIncludeMedia, uri).execute()
            Toast.makeText(this, R.string.wait, Toast.LENGTH_LONG).show()
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
        MainScope().launch(Dispatchers.Default) {
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
            val startImportDialog = Dialog(this, R.style.dia_view)
            val bindingStartImportDialog = DiaImportBinding.inflate(
                layoutInflater
            )
            startImportDialog.setContentView(bindingStartImportDialog.root)
            startImportDialog.setTitle(resources.getString(R.string.options))
            startImportDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
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
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                intent.type = "text/*"
                launcherImportFile.launch(intent)
                startImportDialog.dismiss()
            }
            startImportDialog.show()
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_list_collections_export).setOnMenuItemClickListener {
            val startExportDialog = Dialog(this, R.style.dia_view)
            val bindingStartExportDialog = DiaExportBinding.inflate(
                layoutInflater
            )
            startExportDialog.setContentView(bindingStartExportDialog.root)
            startExportDialog.setTitle(resources.getString(R.string.options))
            startExportDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
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
                intent.type = "text/csv"
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
            this.startActivity(Intent(this, AppLicenses::class.java))
            return@setOnMenuItemClickListener true
        }
        return true
    }

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
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        if (settings.getBoolean("ui_bg_images", true)) {
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
        updateSettingsAndContent()
    }

    public override fun onDestroy() {
        super.onDestroy()
        try {
            val cacheDir = cacheDir
            val files = cacheDir.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile && System.currentTimeMillis() - file.lastModified() > 86400000) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanUp() {
        handleNoMediaFile()
        val dbHelperDelete = DB_Helper_Delete(this@ListCollections)
        dbHelperDelete.deleteDeadMediaLinks()
        dbHelperDelete.deleteDeadTags()
    }

    private fun loadContent(): MutableList<DB_Collection_With_Meta> {
        val currentList = if (dbHelperGet.countCollections() > MAX_SIZE_COUNTER) {
            dbHelperGet.allCollectionsWithMetaNoCounter
        } else {
            dbHelperGet.allCollectionsWithMeta
        }
        val fixedFirstItemPlaceholder = DB_Collection_With_Meta()
        fixedFirstItemPlaceholder.counter = dbHelperGet.countPacks()
        currentList.add(0, fixedFirstItemPlaceholder)
        return currentList
    }

    private fun updateSettingsAndContent() {
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        val uiFontSizeBig = settings.getBoolean("ui_font_size", false)
        if (adapter == null) {
            adapter = AdapterCollections(loadContent(), uiFontSizeBig)
            binding.recDefault.adapter = adapter
            binding.recDefault.layoutManager = LinearLayoutManager(this)
        } else {
            adapter!!.updateSettingsAndContent(loadContent(), uiFontSizeBig)
        }
    }

    override fun importCardsResult(result: Int) {
        runOnUiThread {
            if (result < Globals.IMPORT_ERROR_LEVEL_ERROR) {
                if (result == Globals.IMPORT_ERROR_LEVEL_OKAY) {
                    Toast.makeText(this, R.string.import_okay, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.import_warn, Toast.LENGTH_LONG).show()
                }
                if (adapter != null) {
                    updateSettingsAndContent()
                }
                val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
                when (settings.getInt("ui_mode", Globals.UI_MODE_DAY)) {
                    Globals.UI_MODE_NIGHT -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }

                    Globals.UI_MODE_DAY -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }

                    else -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun importCardsProgress(progress: String) {
        runOnUiThread { Toast.makeText(this, progress, Toast.LENGTH_SHORT).show() }
    }

    override fun exportCardsResult(result: File?) {
        runOnUiThread {
            if (result == null) {
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
            } else {
                val share = Intent(Intent.ACTION_SEND)
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                share.type = "text/csv"
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

    override fun notifyFolderSet() {}
    override fun notifyMissingAction(id: Int) {}
}
