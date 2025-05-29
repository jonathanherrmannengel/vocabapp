package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCardAll
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCardImages
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.databinding.DiaImageBinding
import de.herrmann_engel.rbv.databinding.DiaRecBinding
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

abstract class FileTools : AppCompatActivity() {
    private val selectMediaFolder = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val destination = data?.data
            var takeFlags = data?.flags
            if (takeFlags != null && destination != null) {
                setCardMediaFolder(destination.toString())
                takeFlags =
                    takeFlags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                contentResolver.takePersistableUriPermission(destination, takeFlags)
                notifyFolderSet()
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val folder = cardMediaFolder
            val dbHelperGet = DB_Helper_Get(this)
            // Check: No media folder set
            if (folder.isNullOrEmpty()) {
                if (dbHelperGet.allMedia.isNotEmpty()) {
                    showSelectDialog(resources.getString(R.string.select_folder_help_db_entries))
                }
            } else {
                val outputDirectory = DocumentFile.fromTreeUri(this, Uri.parse(folder))
                // Check: Media folder is set, but directory not accessible
                if (outputDirectory == null || !outputDirectory.isDirectory) {
                    showSelectDialog(resources.getString(R.string.select_folder_help_reselect))
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

    protected fun showSelectDialog(text: String?) {
        val selectFolderInfoDialog = Dialog(this, R.style.dia_view)
        val bindingSelectFolderInfoDialog = DiaConfirmBinding.inflate(
            layoutInflater
        )
        selectFolderInfoDialog.setContentView(bindingSelectFolderInfoDialog.root)
        selectFolderInfoDialog.setTitle(resources.getString(R.string.select_folder_title))
        selectFolderInfoDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        bindingSelectFolderInfoDialog.diaConfirmDesc.text = text
        bindingSelectFolderInfoDialog.diaConfirmDesc.visibility = View.VISIBLE
        bindingSelectFolderInfoDialog.diaConfirmYes.setOnClickListener {
            startSelectMediaFolder()
            selectFolderInfoDialog.dismiss()
        }
        bindingSelectFolderInfoDialog.diaConfirmNo.setOnClickListener { selectFolderInfoDialog.dismiss() }
        selectFolderInfoDialog.show()
    }

    private fun showMissingDialog(card: Int, parentDialog: Dialog?) {
        val selectFolderInfoDialog = Dialog(this, R.style.dia_view)
        val bindingSelectFolderInfoDialog = DiaConfirmBinding.inflate(
            layoutInflater
        )
        selectFolderInfoDialog.setContentView(bindingSelectFolderInfoDialog.root)
        selectFolderInfoDialog.setTitle(resources.getString(R.string.media_missing_dialog))
        selectFolderInfoDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        bindingSelectFolderInfoDialog.diaConfirmDesc.text =
            resources.getString(R.string.media_missing_dialog_text)
        bindingSelectFolderInfoDialog.diaConfirmDesc.visibility = View.VISIBLE
        bindingSelectFolderInfoDialog.diaConfirmYes.setOnClickListener {
            parentDialog?.dismiss()
            selectFolderInfoDialog.dismiss()
            notifyMissingAction(card)
        }
        bindingSelectFolderInfoDialog.diaConfirmNo.setOnClickListener { selectFolderInfoDialog.dismiss() }
        selectFolderInfoDialog.show()
    }

    fun showImageDialog(fileId: Int, cardId: Int, dialog: Dialog? = null) {
        val uri = getImageUri(fileId)
        if (uri != null) {
            val imageDialog = Dialog(this, R.style.dia_view)
            val bindingImageDialog = DiaImageBinding.inflate(
                layoutInflater
            )
            imageDialog.setContentView(bindingImageDialog.root)
            imageDialog.setTitle(resources.getString(R.string.image_media))
            imageDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            Picasso.get().load(uri).fit().centerInside()
                .into(bindingImageDialog.diaImageView)
            imageDialog.show()
        } else if (!existsMediaFile(fileId)) {
            showMissingDialog(cardId, dialog)
        }
    }

    protected fun showImageListDialog(imageList: ArrayList<DB_Media>, cardNo: Int) {
        if (imageList.size == 1) {
            showImageDialog(imageList[0].uid, cardNo)
        } else if (imageList.size > Globals.MAX_SIZE_CARD_IMAGE_PREVIEW) {
            showMediaListDialog(
                imageList,
                cardNo,
                true,
                resources.getString(R.string.query_media_image_title)
            )
        } else {
            val imageListDialog = Dialog(this, R.style.dia_view)
            val bindingImageListDialog = DiaRecBinding.inflate(
                layoutInflater
            )
            imageListDialog.setContentView(bindingImageListDialog.root)
            imageListDialog.setTitle(resources.getString(R.string.query_media_image_title))
            imageListDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            imageListDialog.show()
            val adapter = AdapterMediaLinkCardImages(imageList, cardNo, imageListDialog)
            bindingImageListDialog.diaRec.adapter = adapter
            bindingImageListDialog.diaRec.layoutManager = GridLayoutManager(this, 3)
        }
    }

    protected fun showMediaListDialog(mediaList: ArrayList<DB_Media>, cardNo: Int) {
        showMediaListDialog(mediaList, cardNo, false, resources.getString(R.string.query_media_all_title))
    }

    private fun showMediaListDialog(
        mediaList: ArrayList<DB_Media>,
        cardNo: Int,
        onlyImages: Boolean,
        title: String
    ) {
        val mediaListDialog = Dialog(this, R.style.dia_view)
        val bindingMediaListDialog = DiaRecBinding.inflate(
            layoutInflater
        )
        mediaListDialog.setContentView(bindingMediaListDialog.root)
        mediaListDialog.setTitle(title)
        mediaListDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        mediaListDialog.show()
        val adapter = AdapterMediaLinkCardAll(mediaList, cardNo, onlyImages, mediaListDialog)
        bindingMediaListDialog.diaRec.adapter = adapter
        bindingMediaListDialog.diaRec.layoutManager = LinearLayoutManager(this)
    }

    @JvmOverloads
    fun showDeleteDialog(
        fileName: String,
        text: String? = resources.getString(R.string.delete_file_info)
    ): Dialog {
        val deleteFileDialog = Dialog(this, R.style.dia_view)
        val bindingDeleteFileDialog = DiaConfirmBinding.inflate(
            layoutInflater
        )
        deleteFileDialog.setContentView(bindingDeleteFileDialog.root)
        deleteFileDialog.setTitle(resources.getString(R.string.delete_file_title))
        deleteFileDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        bindingDeleteFileDialog.diaConfirmDesc.text = text
        bindingDeleteFileDialog.diaConfirmDesc.visibility = View.VISIBLE
        bindingDeleteFileDialog.diaConfirmYes.setOnClickListener {
            deleteMediaFile(fileName)
            deleteFileDialog.dismiss()
        }
        bindingDeleteFileDialog.diaConfirmNo.setOnClickListener { deleteFileDialog.dismiss() }
        deleteFileDialog.show()
        return deleteFileDialog
    }

    protected fun handleNoMediaFile() {
        try {
            val folder = cardMediaFolder
            if (folder.isNullOrEmpty()) {
                return
            }
            val outputDirectory = DocumentFile.fromTreeUri(this, Uri.parse(folder))
            if (outputDirectory == null || !outputDirectory.exists() || !outputDirectory.isDirectory) {
                return
            }
            val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
            val mediaInGallery = settings.getBoolean("media_in_gallery", true)
            val noMediaFile = outputDirectory.findFile(".nomedia")
            if (mediaInGallery && noMediaFile != null && noMediaFile.exists() && noMediaFile.isFile) {
                DocumentsContract.deleteDocument(
                    contentResolver,
                    noMediaFile.uri
                )
            } else if (!mediaInGallery && (noMediaFile == null || !noMediaFile.exists())) {
                outputDirectory.createFile("application/octet-stream", ".nomedia")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getImageUri(id: Int): Uri? {
        DocumentFile.fromTreeUri(this, Uri.parse(cardMediaFolder)) ?: return null
        val file = getFile(id) ?: return null
        return file.uri
    }

    protected fun listFiles(): Array<DocumentFile>? {
        val folderFile = DocumentFile.fromTreeUri(this, Uri.parse(cardMediaFolder))
            ?: return null
        return folderFile.listFiles()
    }

    private fun getFile(name: String): DocumentFile? {
        val folderFile = DocumentFile.fromTreeUri(this, Uri.parse(cardMediaFolder))
            ?: return null
        return folderFile.findFile(name)
    }

    private fun getFile(id: Int): DocumentFile? {
        val dbHelperGet = DB_Helper_Get(this)
        val currentMedia = dbHelperGet.getSingleMedia(id) ?: return null
        val fileName = currentMedia.file
        return getFile(fileName)
    }

    private fun openFile(file: DocumentFile) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(file.uri, file.type)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            this.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    fun openFile(name: String) {
        val file = getFile(name)
        if (file != null && file.isFile) {
            openFile(file)
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    fun openFile(id: Int) {
        val file = getFile(id)
        if (file != null && file.isFile) {
            openFile(file)
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareFile(file: DocumentFile) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = file.type
        intent.putExtra(Intent.EXTRA_STREAM, file.uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        this.startActivity(Intent.createChooser(intent, file.name))
    }

    fun shareFile(name: String) {
        val file = getFile(name)
        if (file != null && file.isFile) {
            shareFile(file)
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(id: Int) {
        val file = getFile(id)
        if (file != null && file.isFile) {
            shareFile(file)
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteMediaFile(name: String) {
        val file = getFile(name)
        if (file != null && file.isFile) {
            DocumentsContract.deleteDocument(
                contentResolver,
                file.uri
            )
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    protected val cardMediaFolder: String?
        get() {
            val config = getSharedPreferences(Globals.CONFIG_NAME, MODE_PRIVATE)
            return config.getString("card_media_folder", null)
        }

    private fun setCardMediaFolder(uri: String) {
        val config = getSharedPreferences(Globals.CONFIG_NAME, MODE_PRIVATE)
        config.edit {
            putString("card_media_folder", uri)
        }
    }

    fun existsMediaFile(name: String?): Boolean {
        val folderFile = DocumentFile.fromTreeUri(
            this, Uri.parse(
                cardMediaFolder
            )
        )
        return name?.let { folderFile?.findFile(it)?.exists() } == true
    }

    fun existsMediaFile(input: Int): Boolean {
        val dbHelperGet = DB_Helper_Get(this)
        val currentMedia = dbHelperGet.getSingleMedia(input) ?: return false
        val fileName = currentMedia.file
        return existsMediaFile(fileName)
    }

    protected abstract fun notifyFolderSet()
    protected abstract fun notifyMissingAction(id: Int)
}
