package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterMediaLinkCard
import de.herrmann_engel.rbv.databinding.ActivityEditCardMediaBinding
import de.herrmann_engel.rbv.databinding.DiaFileExistsBinding
import de.herrmann_engel.rbv.db.DB_Media_Link_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class EditCardMedia : FileTools() {
    private lateinit var binding: ActivityEditCardMediaBinding
    private var cardNo = 0
    private val dbHelperGet = DB_Helper_Get(this)
    private val openFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            try {
                val outputDirectory = DocumentFile.fromTreeUri(
                    this, Uri.parse(
                        cardMediaFolder
                    )
                )
                if (outputDirectory == null || !outputDirectory.isDirectory) {
                    showSelectDialog(resources.getString(R.string.select_folder_help_reselect))
                } else {
                    val data = result.data
                    val fileUri = data?.data
                    if (fileUri != null) {
                        val input = DocumentFile.fromSingleUri(this, fileUri)
                        if (input != null) {
                            val inputFileName = input.name
                            val mime = input.type
                            if (inputFileName != null && outputDirectory.findFile(inputFileName) == null) {
                                createMediaFile(outputDirectory, mime, inputFileName, fileUri)
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
        binding = ActivityEditCardMediaBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        intent.extras?.getInt("card")?.also {
            try {
                cardNo = it
                val card = dbHelperGet.getSingleCard(cardNo)
                val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
                val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
                val packColors = dbHelperGet.getSinglePack(card.pack).colors
                if (packColors >= 0 && packColors < colorsStatusBar.length() && packColors < colorsBackground.length()) {
                    val colorStatusBar = colorsStatusBar.getColor(packColors, 0)
                    val colorBackground = colorsBackground.getColor(packColors, 0)
                    supportActionBar?.setBackgroundDrawable(ColorDrawable(colorStatusBar))
                    window.statusBarColor = colorStatusBar
                    binding.root.setBackgroundColor(colorBackground)
                }
                colorsStatusBar.recycle()
                colorsBackground.recycle()
                binding.addMediaButton.setOnClickListener {
                    val folder = cardMediaFolder
                    if (folder.isNullOrEmpty()) {
                        showSelectDialog(resources.getString(R.string.select_folder_help))
                    } else {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.type = "*/*"
                        openFile.launch(intent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        setRecView()
    }

    private fun createMediaFile(
        outputDirectory: DocumentFile,
        mime: String?,
        inputFileName: String?,
        fileUri: Uri?
    ) {
        try {
            val output = outputDirectory.createFile(mime!!, inputFileName!!)
            val outputFileName = output!!.name
            val outputStream = contentResolver.openOutputStream(
                output.uri
            )
            val inputStream = contentResolver.openInputStream(fileUri!!)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream!!.read(buffer).also { read = it } != -1) {
                outputStream!!.write(buffer, 0, read)
            }
            inputStream.close()
            outputStream!!.flush()
            outputStream.close()
            addMediaLink(outputFileName, mime)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show()
        }
    }

    private fun addMediaLink(outputFileName: String?, mime: String?) {
        try {
            val dbHelperCreate = DB_Helper_Create(this)
            dbHelperCreate.createMedia(outputFileName, mime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (dbHelperGet.existsMedia(outputFileName)) {
                val media = dbHelperGet.getSingleMedia(outputFileName)
                val fileId = media.uid
                val dbHelperCreate = DB_Helper_Create(this)
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
        val mediaList = dbHelperGet.getAllMediaLinksByCard(cardNo) as ArrayList<DB_Media_Link_Card>
        val adapter = AdapterMediaLinkCard(mediaList, cardNo, cardMediaFolder)
        binding.recCardMedia.adapter = adapter
        binding.recCardMedia.layoutManager = LinearLayoutManager(this)
    }

    public override fun notifyFolderSet() {
        setRecView()
    }

    override fun notifyMissingAction(id: Int) {}
}
