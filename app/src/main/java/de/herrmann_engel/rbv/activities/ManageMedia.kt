package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.adapters.AdapterMediaManage
import de.herrmann_engel.rbv.databinding.ActivityManageMediaBinding
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class ManageMedia : FileTools() {
    private lateinit var binding: ActivityManageMediaBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.manageFilesButton.setOnClickListener {
            val intent = Intent(this, ManageFiles::class.java)
            startActivity(intent)
        }
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
        val dbHelperGet = DB_Helper_Get(this)
        val mediaList = dbHelperGet.allMedia as ArrayList<DB_Media>
        val adapter = AdapterMediaManage(mediaList)
        binding.recMediaManage.adapter = adapter
        binding.recMediaManage.layoutManager = LinearLayoutManager(this)
    }
}
