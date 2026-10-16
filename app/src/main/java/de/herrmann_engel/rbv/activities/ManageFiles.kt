package de.herrmann_engel.rbv.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterFilesManage
import de.herrmann_engel.rbv.databinding.ActivityManageFilesBinding
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.media.MediaFileManager

/**
 * Lists files in the media folder that are not registered as media in the database.
 */
class ManageFiles : RBVActivity() {
    private val mediaFileManager = MediaFileManager(this, onFolderSet = { setRecView() })
    private lateinit var binding: ActivityManageFilesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        if (settings.getBoolean("ui_bg_images", true)) {
            binding.filesBackgroundImage.visibility = View.VISIBLE
            binding.filesBackgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_media
                )
            )
        }
        // Edge-to-edge: keep the content clear of the system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.recFilesManage) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        enableEdgeToEdge()
        // Ask for the media folder if media entries exist but the folder is missing or inaccessible
        mediaFileManager.askForFolderIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        setRecView()
    }

    private fun setRecView() {
        val files = mediaFileManager.listFiles()
        if (files != null) {
            val filesWithoutMedia = ArrayList<DocumentFile>()
            val dbHelperGet = DB_Helper_Get(this)
            // Ignore directories and the .nomedia file
            for (file in files) {
                if (!dbHelperGet.existsMedia(file.name) && file.isFile && file.name != ".nomedia") {
                    filesWithoutMedia.add(file)
                }
            }
            val adapter = AdapterFilesManage(filesWithoutMedia, mediaFileManager)
            binding.recFilesManage.adapter = adapter
            binding.recFilesManage.layoutManager = LinearLayoutManager(this)
        }
    }
}
