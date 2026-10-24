package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterMediaManage
import de.herrmann_engel.rbv.adapters.AdapterMediaManageLinkedCards
import de.herrmann_engel.rbv.cardlist.CardSort
import de.herrmann_engel.rbv.databinding.ActivityManageMediaBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.CardWithMeta
import de.herrmann_engel.rbv.db.DbMedia
import de.herrmann_engel.rbv.db.utils.DbHelperDelete
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.media.MediaFileManager
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.createDialog
import de.herrmann_engel.rbv.utils.fillFormattedTexts
import de.herrmann_engel.rbv.utils.showConfirmDialog
import de.herrmann_engel.rbv.utils.sortCards

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
        val settings = AppSettings(this)
        if (settings.uiBackgroundImages) {
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
        setUpEdgeToEdge(binding.mediaContainer)
        // Ask for the media folder if media entries exist but the folder is missing or inaccessible
        mediaFileManager.askForFolderIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        setRecView()
    }

    private fun setRecView() {
        val dbHelperGet = DbHelperGet(this)
        val mediaList = dbHelperGet.getAllMedia().toMutableList()
        val adapter = AdapterMediaManage(mediaList) { showMedia(it) }
        binding.recMediaManage.adapter = adapter
        binding.recMediaManage.layoutManager = LinearLayoutManager(this)
    }

    // Shows the cards the media entry is linked to; if there are none, offers to delete it
    private fun showMedia(media: DbMedia) {
        val cards: MutableList<CardWithMeta> =
            DbHelperGet(this).getAllCardsByMediaWithMeta(media.uid)
        if (AppSettings(this).formatCards) {
            fillFormattedTexts(cards)
        }
        sortCards(cards, CardSort.ALPHABETICAL)
        if (cards.isEmpty()) {
            showConfirmDialog(this, getString(R.string.delete), getString(R.string.media_no_card)) {
                DbHelperDelete(this).deleteMedia(media.uid)
                (binding.recMediaManage.adapter as? AdapterMediaManage)?.remove(media)
                // The file itself is only deleted after another confirmation
                if (mediaFileManager.existsMediaFile(media.file)) {
                    mediaFileManager.showDeleteDialog(media.file)
                }
            }
        } else {
            val bindingDialog = DiaRecBinding.inflate(layoutInflater)
            val dialog =
                createDialog(this, bindingDialog.root, getString(R.string.media_linked_cards))
            bindingDialog.diaRec.adapter = AdapterMediaManageLinkedCards(cards, dialog)
            bindingDialog.diaRec.layoutManager = LinearLayoutManager(this)
            dialog.show()
        }
    }
}
