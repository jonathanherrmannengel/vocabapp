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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil3.load
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCardAll
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCardImages
import de.herrmann_engel.rbv.databinding.DiaImageBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.DbMedia
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.settings.AppConfig
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.createDialog
import de.herrmann_engel.rbv.utils.showConfirmDialog

/**
 * Media file handling for an activity that works with media files.
 *
 * Media files are stored in a folder chosen by the user (Storage Access Framework); its URI is
 * saved in the config preferences. The database only stores the file names (see DbMedia).
 * This class handles selecting the folder and opening, sharing, displaying and deleting files.
 *
 * It has to be created while the activity is constructed (e.g. as a property initializer),
 * because it registers the folder picker for an activity result.
 *
 * The files of the folder are listed with a single query and kept until the activity is started
 * again or the folder changes: asking the document provider for each file separately is slow in
 * large folders.
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
    // Files of the media folder by name; null until listed (see filesByName)
    private var cachedFiles: Map<String, MediaFolderFile>? = null

    init {
        // Files may have been added or deleted (e.g. by another app) while the activity was stopped
        activity.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                clearCachedFiles()
            }
        })
    }

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
                clearCachedFiles()
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
            val dbHelperGet = DbHelperGet(activity)
            // Check: No media folder set
            if (folder.isNullOrEmpty()) {
                if (dbHelperGet.getAllMedia().isNotEmpty()) {
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
    private fun showMissingDialog(cardId: Int, parentDialog: Dialog?) {
        showConfirmDialog(
            activity,
            activity.getString(R.string.media_missing_dialog),
            activity.getString(R.string.media_missing_dialog_text)
        ) {
            parentDialog?.dismiss()
            onMissingFileAction(cardId)
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
    fun showImageListDialog(imageList: List<DbMedia>, cardId: Int) {
        if (imageList.size == 1) {
            showImageDialog(imageList[0].uid, cardId)
        } else if (imageList.size > Globals.MAX_SIZE_CARD_IMAGE_PREVIEW) {
            showMediaListDialog(
                imageList,
                cardId,
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
            val adapter = AdapterMediaLinkCardImages(imageList, cardId, imageListDialog, this)
            bindingImageListDialog.diaRec.adapter = adapter
            bindingImageListDialog.diaRec.layoutManager = GridLayoutManager(activity, 3)
        }
    }

    // Shows all media files of a card as a list
    fun showMediaListDialog(mediaList: List<DbMedia>, cardId: Int) {
        showMediaListDialog(
            mediaList,
            cardId,
            false,
            activity.getString(R.string.query_media_all_title)
        )
    }

    private fun showMediaListDialog(
        mediaList: List<DbMedia>,
        cardId: Int,
        onlyImages: Boolean,
        title: String
    ) {
        val bindingMediaListDialog = DiaRecBinding.inflate(
            activity.layoutInflater
        )
        val mediaListDialog = createDialog(activity, bindingMediaListDialog.root, title)
        mediaListDialog.show()
        val adapter = AdapterMediaLinkCardAll(mediaList, cardId, onlyImages, mediaListDialog, this)
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
            val noMediaFile = getFile(".nomedia")
            if (mediaInGallery && noMediaFile != null && noMediaFile.isFile) {
                DocumentsContract.deleteDocument(
                    activity.contentResolver,
                    noMediaFile.uri
                )
                clearCachedFiles()
            } else if (!mediaInGallery && noMediaFile == null) {
                outputDirectory.createFile("application/octet-stream", ".nomedia")
                clearCachedFiles()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getImageUri(fileId: Int): Uri? {
        val file = getFile(fileId)
        return if (file != null && file.isFile) file.uri else null
    }

    // Lists all files in the media folder, null if no folder is selected
    fun listFiles(): List<MediaFolderFile>? {
        if (!isFolderSelected) {
            return null
        }
        return filesByName().values.toList()
    }

    // Has to be called after files were added to the media folder by other code (e.g.
    // EditCardMedia)
    fun clearCachedFiles() {
        cachedFiles = null
    }

    /**
     * Returns the files of the media folder by name, listed with a single query (name and type
     * included) on first use. Empty if no folder is selected or it is inaccessible.
     */
    private fun filesByName(): Map<String, MediaFolderFile> {
        cachedFiles?.let { return it }
        val files = LinkedHashMap<String, MediaFolderFile>()
        val folder = cardMediaFolder?.takeIf { it.isNotEmpty() }?.toUri()
        if (folder != null) {
            try {
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    folder,
                    DocumentsContract.getTreeDocumentId(folder)
                )
                val projection = arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                )
                activity.contentResolver.query(childrenUri, projection, null, null, null)
                    ?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val name = cursor.getString(1) ?: continue
                            val documentId = cursor.getString(0)
                            val uri =
                                DocumentsContract.buildDocumentUriUsingTree(folder, documentId)
                            files.putIfAbsent(name, MediaFolderFile(name, uri, cursor.getString(2)))
                        }
                    }
            } catch (e: Exception) {
                // E.g. the folder has been deleted or the permission has been revoked
                e.printStackTrace()
            }
        }
        cachedFiles = files
        return files
    }

    private fun getFile(name: String): MediaFolderFile? {
        return filesByName()[name]
    }

    private fun getFile(fileId: Int): MediaFolderFile? {
        val dbHelperGet = DbHelperGet(activity)
        val fileName = dbHelperGet.getSingleMedia(fileId)?.file ?: return null
        return getFile(fileName)
    }

    private fun openFile(file: MediaFolderFile) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(file.uri, file.mimeType)
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

    override fun openFile(fileId: Int) {
        val file = getFile(fileId)
        if (file != null && file.isFile) {
            openFile(file)
        } else {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(file: MediaFolderFile) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = file.mimeType
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

    override fun shareFile(fileId: Int) {
        val file = getFile(fileId)
        if (file != null && file.isFile) {
            shareFile(file)
        } else {
            Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteMediaFile(name: String) {
        val file = getFile(name)
        if (file != null && file.isFile) {
            try {
                DocumentsContract.deleteDocument(
                    activity.contentResolver,
                    file.uri
                )
            } catch (e: Exception) {
                // E.g. the file has been deleted meanwhile or the permission has been revoked
                e.printStackTrace()
                Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
            }
            clearCachedFiles()
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
        val file = name?.let { getFile(it) }
        return file != null && file.isFile
    }

    private fun existsMediaFile(fileId: Int): Boolean {
        val dbHelperGet = DbHelperGet(activity)
        val media = dbHelperGet.getSingleMedia(fileId) ?: return false
        return existsMediaFile(media.file)
    }
}
