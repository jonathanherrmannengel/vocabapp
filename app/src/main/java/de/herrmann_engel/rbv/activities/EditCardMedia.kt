package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCard
import de.herrmann_engel.rbv.databinding.ActivityEditCardMediaBinding
import de.herrmann_engel.rbv.databinding.DiaFileExistsBinding
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.media.MediaFileManager

/**
 * Manages the media files of the card passed as "card" extra. New files are picked by the user
 * and copied into the media folder, then linked to the card.
 */
class EditCardMedia : RBVActivity() {
    private val mediaFileManager = MediaFileManager(this, onFolderSet = { setRecView() })
    private lateinit var binding: ActivityEditCardMediaBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private var cardNo = 0
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
                            if (inputFileName != null && outputDirectory.findFile(inputFileName) == null) {
                                createMediaFile(outputDirectory, mime!!, inputFileName, fileUri)
                            } else {
                                val fileExistsDialog = Dialog(this, R.style.dia_view)
                                val bindingFileExistsDialog = DiaFileExistsBinding.inflate(
                                    layoutInflater
                                )
                                fileExistsDialog.setContentView(bindingFileExistsDialog.root)
                                fileExistsDialog.setTitle(resources.getString(R.string.file_exists_title))
                                fileExistsDialog.window!!.setLayout(
                                    WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.MATCH_PARENT
                                )
                                bindingFileExistsDialog.diaFileExistsLink.setOnClickListener {
                                    addMediaLink(inputFileName, mime)
                                    fileExistsDialog.dismiss()
                                }
                                bindingFileExistsDialog.diaFileExistsNew.setOnClickListener {
                                    createMediaFile(
                                        outputDirectory,
                                        mime!!,
                                        inputFileName!!,
                                        fileUri
                                    )
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
        dbHelperGet = DB_Helper_Get(this)
        cardNo = intent.extras!!.getInt("card")
        val card = dbHelperGet.getSingleCard(cardNo)
        val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
        val minimalLength = colorsBackground.length()
        val packColors = dbHelperGet.getSinglePack(card.pack).colors
        if (packColors in 0..<minimalLength) {
            val colorBackground = colorsBackground.getColor(packColors, 0)
            binding.root.setBackgroundColor(colorBackground)
        }
        colorsBackground.recycle()
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
        // Edge-to-edge: keep the content clear of the system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.editCardMediaLayout) { v, windowInsets ->
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
            addMediaLink(outputFileName, mime)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
    }

    // Creates the media entry for the file name (if it does not exist yet) and links it to the card
    private fun addMediaLink(outputFileName: String?, mime: String?) {
        try {
            val dbHelperCreate = DB_Helper_Create(this)
            if (!dbHelperGet.existsMedia(outputFileName)) {
                dbHelperCreate.createMedia(outputFileName, mime)
            }
            if (dbHelperGet.existsMedia(outputFileName)) {
                val media = dbHelperGet.getSingleMedia(outputFileName)
                val fileId = media.uid
                dbHelperCreate.createMediaLink(fileId, cardNo)
                setRecView()
                Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
    }

    private fun setRecView() {
        val mediaList = dbHelperGet.getCardMedia(cardNo) as ArrayList<DB_Media>
        val adapter = AdapterMediaLinkCard(mediaList, cardNo, mediaFileManager.mediaFolder, mediaFileManager)
        binding.recCardMedia.adapter = adapter
        binding.recCardMedia.layoutManager = LinearLayoutManager(this)
    }

}
