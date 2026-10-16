package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterMediaManage
import de.herrmann_engel.rbv.databinding.ActivityManageMediaBinding
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.media.MediaFileManager

/**
 * Lists all media entries of the database (see [AdapterMediaManage]) and links to
 * [ManageFiles] for files in the media folder that are not used as media.
 */
class ManageMedia : RBVActivity() {
    private val mediaFileManager = MediaFileManager(this, onFolderSet = { setRecView() })
    private lateinit var binding: ActivityManageMediaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        if (settings.getBoolean("ui_bg_images", true)) {
            binding.mediaBackgroundImage.visibility = View.VISIBLE
            binding.mediaBackgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_media
                )
            )
        }
        binding.manageFilesButton.setOnClickListener {
            val intent = Intent(this, ManageFiles::class.java)
            startActivity(intent)
        }
        // Edge-to-edge: keep the content clear of the system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.mediaContainer) { v, windowInsets ->
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
        val dbHelperGet = DB_Helper_Get(this)
        val mediaList = dbHelperGet.allMedia as ArrayList<DB_Media>
        val adapter = AdapterMediaManage(mediaList, mediaFileManager)
        binding.recMediaManage.adapter = adapter
        binding.recMediaManage.layoutManager = LinearLayoutManager(this)
    }
}
