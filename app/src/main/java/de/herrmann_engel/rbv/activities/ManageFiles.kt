package de.herrmann_engel.rbv.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterFilesManage
import de.herrmann_engel.rbv.databinding.ActivityManageFilesBinding
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.media.MediaFileManager
import de.herrmann_engel.rbv.media.MediaFolderFile
import de.herrmann_engel.rbv.settings.AppSettings

/**
 * Lists files in the media folder that are not registered as media in the database.
 */
class ManageFiles : RBVActivity() {
    private val mediaFileManager = MediaFileManager(this, onFolderSet = { setRecView() })
    private lateinit var binding: ActivityManageFilesBinding
    // Set when the list is shown; deleting is only started from that list
    private lateinit var adapter: AdapterFilesManage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageFilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val settings = AppSettings(this)
        if (settings.uiBackgroundImages) {
            binding.filesBackgroundImage.visibility = View.VISIBLE
            binding.filesBackgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_media
                )
            )
        }
        setUpEdgeToEdge(binding.recFilesManage)
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
            // Names of all media entries, so the files can be checked without a database query each
            val mediaNames = DbHelperGet(this).getAllMedia().mapTo(HashSet()) { it.file }
            // Ignore directories and the .nomedia file
            val filesWithoutMedia = files.filter {
                it.isFile && it.name != ".nomedia" && it.name !in mediaNames
            }.toMutableList()
            adapter = AdapterFilesManage(filesWithoutMedia, mediaFileManager) { deleteFile(it) }
            binding.recFilesManage.adapter = adapter
            binding.recFilesManage.layoutManager = LinearLayoutManager(this)
        }
    }

    // Asks for confirmation, deletes the file and removes it from the list if it is gone
    private fun deleteFile(file: MediaFolderFile) {
        mediaFileManager.showDeleteDialog(
            file.name,
            getString(R.string.delete_file_without_media_info)
        ) {
            if (!mediaFileManager.existsMediaFile(file.name)) {
                adapter.remove(file)
            }
        }
    }
}
