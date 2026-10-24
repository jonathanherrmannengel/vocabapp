package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCard
import de.herrmann_engel.rbv.databinding.ActivityEditCardMediaBinding
import de.herrmann_engel.rbv.databinding.DiaFileExistsBinding
import de.herrmann_engel.rbv.db.utils.DbHelperCreate
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.media.MediaFileManager
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.createDialog
import de.herrmann_engel.rbv.utils.getCardId

/**
 * Manages the media files of the card passed as "card" extra. New files are picked by the user
 * and copied into the media folder, then linked to the card.
 */
class EditCardMedia : RBVActivity() {
    private val mediaFileManager = MediaFileManager(this, onFolderSet = { setRecView() })
    private lateinit var binding: ActivityEditCardMediaBinding
    private lateinit var dbHelperGet: DbHelperGet
    private var cardId = 0
    // Result of the file picker: copy the file into the media folder. If a file with the same name
    // already exists there, the user can link the existing file or copy the new one anyway
    // (the document provider then chooses a different name).
    private val openFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            try {
                val outputDirectory = mediaFileManager.mediaFolder
                if (outputDirectory == null || !outputDirectory.isDirectory) {
                    mediaFileManager.showSelectDialog(resources.getString(R.string.select_folder_help_reselect))
                } else {
                    val data = result.data
                    val fileUri = data?.data
                    if (fileUri != null) {
                        val input = DocumentFile.fromSingleUri(this, fileUri)
                        if (input != null) {
                            val inputFileName = input.name
                            val mime = input.type
                            if (inputFileName == null || mime == null) {
                                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
                            } else if (!mediaFileManager.existsMediaFile(inputFileName)) {
                                createMediaFile(outputDirectory, mime, inputFileName, fileUri)
                            } else {
                                val bindingFileExistsDialog = DiaFileExistsBinding.inflate(
                                    layoutInflater
                                )
                                val fileExistsDialog = createDialog(
                                    this,
                                    bindingFileExistsDialog.root,
                                    getString(R.string.file_exists_title)
                                )
                                bindingFileExistsDialog.diaFileExistsLink.setOnClickListener {
                                    addMediaLink(inputFileName, mime)
                                    fileExistsDialog.dismiss()
                                }
                                bindingFileExistsDialog.diaFileExistsNew.setOnClickListener {
                                    createMediaFile(outputDirectory, mime, inputFileName, fileUri)
                                    fileExistsDialog.dismiss()
                                }
                                fileExistsDialog.show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCardMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DbHelperGet(this)
        cardId = intent.getCardId() ?: run {
            finish()
            return
        }
        // Close if the card no longer exists (e.g. deleted in the meantime)
        val card = dbHelperGet.getSingleCard(cardId) ?: run {
            finish()
            return
        }
        resources.colorAt(R.array.pack_color_background, dbHelperGet.getSinglePack(card.packId)?.colors)
            ?.let { binding.root.setBackgroundColor(it) }
        // A media folder has to be selected before files can be added
        binding.addMediaButton.setOnClickListener {
            if (!mediaFileManager.isFolderSelected) {
                mediaFileManager.showSelectDialog(resources.getString(R.string.select_folder_help))
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                openFile.launch(intent)
            }
        }
        setUpEdgeToEdge(binding.editCardMediaLayout)
        // Ask for the media folder if media entries exist but the folder is missing or inaccessible
        mediaFileManager.askForFolderIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        setRecView()
    }

    // Copies the picked file into the media folder and links it to the card
    private fun createMediaFile(
        outputDirectory: DocumentFile,
        mime: String,
        inputFileName: String,
        fileUri: Uri
    ) {
        try {
            val output = outputDirectory.createFile(mime, inputFileName)!!
            val outputFileName = output.name
            if (outputFileName == null) {
                output.delete()
                throw IllegalStateException("Created file has no name")
            }
            val outputStream = contentResolver.openOutputStream(output.uri)!!
            val inputStream = contentResolver.openInputStream(fileUri)!!
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            inputStream.close()
            outputStream.flush()
            outputStream.close()
            mediaFileManager.clearCachedFiles()
            addMediaLink(outputFileName, mime)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
    }

    // Creates the media entry for the file name (if it does not exist yet) and links it to the card
    private fun addMediaLink(outputFileName: String, mime: String) {
        try {
            val dbHelperCreate = DbHelperCreate(this)
            val fileId = dbHelperGet.getSingleMedia(outputFileName)?.uid
                ?: dbHelperCreate.createMedia(outputFileName, mime).toInt()
            dbHelperCreate.createMediaLink(fileId, cardId)
            setRecView()
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
    }

    private fun setRecView() {
        val mediaList = dbHelperGet.getCardMedia(cardId).toMutableList()
        val adapter = AdapterMediaLinkCard(mediaList, cardId, mediaFileManager)
        binding.recCardMedia.adapter = adapter
        binding.recCardMedia.layoutManager = LinearLayoutManager(this)
    }

}
