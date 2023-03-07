package de.herrmann_engel.rbv.activities

import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.adapters.AdapterFilesManage
import de.herrmann_engel.rbv.databinding.ActivityManageFilesBinding
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class ManageFiles : FileTools() {
    private lateinit var binding: ActivityManageFilesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageFilesBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        setRecView()
    }

    override fun notifyFolderSet() {
        setRecView()
    }

    override fun notifyMissingAction(id: Int) {}
    private fun setRecView() {
        val files = listFiles()
        if (files != null) {
            val filesWithoutMedia = ArrayList<DocumentFile>()
            val dbHelperGet = DB_Helper_Get(this)
            for (file in files) {
                if (!dbHelperGet.existsMedia(file.name) && file.isFile && file.name != ".nomedia") {
                    filesWithoutMedia.add(file)
                }
            }
            val adapter = AdapterFilesManage(filesWithoutMedia)
            binding.recFilesManage.adapter = adapter
            binding.recFilesManage.layoutManager = LinearLayoutManager(this)
        }
    }
}
