package de.herrmann_engel.rbv.media

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil3.load
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCardAll
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCardImages
import de.herrmann_engel.rbv.databinding.DiaImageBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.settings.AppConfig
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.createDialog
import de.herrmann_engel.rbv.utils.showConfirmDialog

/**
 * Media file handling for an activity that works with media files.
 *
 * Media files are stored in a folder chosen by the user (Storage Access Framework); its URI is
 * saved in the config preferences. The database only stores the file names (see DB_Media).
 * This class handles selecting the folder and opening, sharing, displaying and deleting files.
 *
 * It has to be created while the activity is constructed (e.g. as a property initializer),
 * because it registers the folder picker for an activity result.
 *
 * @param onFolderSet called after the user has selected a new media folder
 * @param onMissingFileAction called if the user wants to handle a missing file of the card with
 * the given id
 */
class MediaFileManager(
    private val activity: ComponentActivity,
    private val onFolderSet: () -> Unit = {},
    private val onMissingFileAction: (cardId: Int) -> Unit = {}
) : MediaFiles {

    // Result of the folder picker: save the folder and keep the access permission across restarts
    private val selectMediaFolder = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val destination = data?.data
            var takeFlags = data?.flags
            if (takeFlags != null && destination != null) {
                setCardMediaFolder(destination.toString())
                takeFlags =
                    takeFlags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                activity.contentResolver.takePersistableUriPermission(destination, takeFlags)
                onFolderSet()
            } else {
                Toast.makeText(activity, R.string.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Asks the user to (re)select the media folder if media entries exist but the folder is
     * missing or inaccessible.
     */
    fun askForFolderIfNeeded() {
        try {
            val folder = cardMediaFolder
            val dbHelperGet = DB_Helper_Get(activity)
            // Check: No media folder set
            if (folder.isNullOrEmpty()) {
                if (dbHelperGet.allMedia.isNotEmpty()) {
                    showSelectDialog(activity.getString(R.string.select_folder_help_db_entries))
                }
            } else {
                val outputDirectory = DocumentFile.fromTreeUri(activity, folder.toUri())
                // Check: Media folder is set, but directory not accessible
                if (outputDirectory == null || !outputDirectory.isDirectory) {
                    showSelectDialog(activity.getString(R.string.select_folder_help_reselect))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startSelectMediaFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        selectMediaFolder.launch(intent)
    }

    // Explains why a media folder is needed and lets the user select one
    fun showSelectDialog(text: String?) {
        showConfirmDialog(activity, activity.getString(R.string.select_folder_title), text) {
            startSelectMediaFolder()
        }
    }

    // Informs the user that a media file linked to the card is missing; on confirmation onMissingFileAction is called
    private fun showMissingDialog(card: Int, parentDialog: Dialog?) {
        showConfirmDialog(
            activity,
            activity.getString(R.string.media_missing_dialog),
            activity.getString(R.string.media_missing_dialog_text)
        ) {
            parentDialog?.dismiss()
            onMissingFileAction(card)
        }
    }

    override fun showImageDialog(fileId: Int, cardId: Int, dialog: Dialog?) {
        val uri = getImageUri(fileId)
        if (uri != null) {
            val bindingImageDialog = DiaImageBinding.inflate(
                activity.layoutInflater
            )
            val imageDialog = createDialog(
                activity,
                bindingImageDialog.root,
                activity.getString(R.string.image_media)
            )
            bindingImageDialog.diaImageView.load(uri)
            imageDialog.show()
        } else if (!existsMediaFile(fileId)) {
            showMissingDialog(cardId, dialog)
        }
    }

    /**
     * Shows the images of a card: a single image directly, up to MAX_SIZE_CARD_IMAGE_PREVIEW images
     * as a grid of previews, more as a list of file names.
     */
    fun showImageListDialog(imageList: List<DB_Media>, cardNo: Int) {
        if (imageList.size == 1) {
            showImageDialog(imageList[0].uid, cardNo)
        } else if (imageList.size > Globals.MAX_SIZE_CARD_IMAGE_PREVIEW) {
            showMediaListDialog(
                imageList,
                cardNo,
                true,
                activity.getString(R.string.query_media_image_title)
            )
        } else {
            val bindingImageListDialog = DiaRecBinding.inflate(
                activity.layoutInflater
            )
            val imageListDialog = createDialog(
                activity,
                bindingImageListDialog.root,
                activity.getString(R.string.query_media_image_title)
            )
            imageListDialog.show()
            val adapter = AdapterMediaLinkCardImages(imageList, cardNo, imageListDialog, this)
            bindingImageListDialog.diaRec.adapter = adapter
            bindingImageListDialog.diaRec.layoutManager = GridLayoutManager(activity, 3)
        }
    }

    // Shows all media files of a card as a list
    fun showMediaListDialog(mediaList: List<DB_Media>, cardNo: Int) {
        showMediaListDialog(
            mediaList,
            cardNo,
            false,
            activity.getString(R.string.query_media_all_title)
        )
    }

    private fun showMediaListDialog(
        mediaList: List<DB_Media>,
        cardNo: Int,
        onlyImages: Boolean,
        title: String
    ) {
        val bindingMediaListDialog = DiaRecBinding.inflate(
            activity.layoutInflater
        )
        val mediaListDialog = createDialog(activity, bindingMediaListDialog.root, title)
        mediaListDialog.show()
        val adapter = AdapterMediaLinkCardAll(mediaList, cardNo, onlyImages, mediaListDialog, this)
        bindingMediaListDialog.diaRec.adapter = adapter
        bindingMediaListDialog.diaRec.layoutManager = LinearLayoutManager(activity)
    }

    override fun showDeleteDialog(fileName: String, text: String?, onDeleted: () -> Unit) {
        showConfirmDialog(
            activity,
            activity.getString(R.string.delete_file_title),
            text ?: activity.getString(R.string.delete_file_info)
        ) {
            deleteMediaFile(fileName)
            onDeleted()
        }
    }

    /**
     * Creates or deletes the ".nomedia" file in the media folder according to the mediaInGallery
     * setting (see [AppSettings]), so that gallery apps show or hide the media files.
     */
    fun handleNoMediaFile() {
        try {
            val outputDirectory = mediaFolder
            if (outputDirectory == null || !outputDirectory.exists() || !outputDirectory.isDirectory) {
                return
            }
            val settings = AppSettings(activity)
            val mediaInGallery = settings.mediaInGallery
            val noMediaFile = outputDirectory.findFile(".nomedia")
            if (mediaInGallery && noMediaFile != null && noMediaFile.exists() && noMediaFile.isFile) {
                DocumentsContract.deleteDocument(
                    activity.contentResolver,
                    noMediaFile.uri
                )
            } else if (!mediaInGallery && (noMediaFile == null || !noMediaFile.exists())) {
                outputDirectory.createFile("application/octet-stream", ".nomedia")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getImageUri(id: Int): Uri? {
        val file = getFile(id) ?: return null
        return file.uri
    }

    // Lists all files in the media folder, null if no folder is selected
    fun listFiles(): Array<DocumentFile>? {
        return mediaFolder?.listFiles()
    }

    private fun getFile(name: String): DocumentFile? {
        return mediaFolder?.findFile(name)
    }

    private fun getFile(id: Int): DocumentFile? {
        val dbHelperGet = DB_Helper_Get(activity)
        val fileName = dbHelperGet.getSingleMedia(id)?.file ?: return null
        return getFile(fileName)
    }

    private fun openFile(file: DocumentFile) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(file.uri, file.type)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            activity.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun openFile(name: String) {
        val file = getFile(name)
        if (file != null && file.isFile) {
            openFile(file)
        } else {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun openFile(id: Int) {
        val file = getFile(id)
        if (file != null && file.isFile) {
            openFile(file)
        } else {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(file: DocumentFile) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = file.type
        intent.putExtra(Intent.EXTRA_STREAM, file.uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivity(Intent.createChooser(intent, file.name))
    }

    override fun shareFile(name: String) {
        val file = getFile(name)
        if (file != null && file.isFile) {
            shareFile(file)
        } else {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun shareFile(id: Int) {
        val file = getFile(id)
        if (file != null && file.isFile) {
            shareFile(file)
        } else {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteMediaFile(name: String) {
        val file = getFile(name)
        if (file != null && file.isFile) {
            DocumentsContract.deleteDocument(
                activity.contentResolver,
                file.uri
            )
        } else {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    // URI of the media folder as string, null if not set yet
    private val cardMediaFolder: String?
        get() = AppConfig(activity).mediaFolder

    // True if the user has selected a media folder (it may still be inaccessible)
    val isFolderSelected: Boolean
        get() = !cardMediaFolder.isNullOrEmpty()

    // The media folder, null if no folder is selected yet
    val mediaFolder: DocumentFile?
        get() {
            val folder = cardMediaFolder
            if (folder.isNullOrEmpty()) {
                return null
            }
            return DocumentFile.fromTreeUri(activity, folder.toUri())
        }

    private fun setCardMediaFolder(uri: String) {
        AppConfig(activity).mediaFolder = uri
    }

    override fun existsMediaFile(name: String?): Boolean {
        return name?.let { mediaFolder?.findFile(it)?.exists() } == true
    }

    private fun existsMediaFile(input: Int): Boolean {
        val dbHelperGet = DB_Helper_Get(activity)
        val currentMedia = dbHelperGet.getSingleMedia(input) ?: return false
        val fileName = currentMedia.file
        return existsMediaFile(fileName)
    }
}
