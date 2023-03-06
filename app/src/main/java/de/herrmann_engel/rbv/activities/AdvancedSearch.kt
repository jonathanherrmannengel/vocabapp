package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.adapters.AdapterPacksAdvancedSearch
import de.herrmann_engel.rbv.databinding.ActivityAdvancedSearchBinding
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class AdvancedSearch : AppCompatActivity() {
    private val packList = ArrayList<Int>()
    private lateinit var binding: ActivityAdvancedSearchBinding
    private var pack = -3
    private var progressGreater = false
    private var progressNumber = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvancedSearchBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        val dbHelperGet = DB_Helper_Get(this)
        val packs = dbHelperGet.allPacks
        val adapter = AdapterPacksAdvancedSearch(packs, packList)
        binding.recAdvancedSearch.adapter = adapter
        binding.recAdvancedSearch.layoutManager = LinearLayoutManager(this)
        if (pack == -3) {
            binding.recAdvancedSearch.visibility = View.GONE
        }
        binding.advancedSearchPacksAll.isChecked = pack == -3
        binding.advancedSearchPacksAll.setOnClickListener {
            pack = -3
            binding.recAdvancedSearch.visibility = View.GONE
        }
        binding.advancedSearchPacksSelect.isChecked = pack == -2
        binding.advancedSearchPacksSelect.setOnClickListener {
            pack = -2
            binding.recAdvancedSearch.visibility = View.VISIBLE
        }
        binding.advancedSearchProgressGreater.isChecked = progressGreater
        binding.advancedSearchProgressGreater.setOnClickListener {
            progressGreater = true
        }
        binding.advancedSearchProgressLess.isChecked = !progressGreater
        binding.advancedSearchProgressLess.setOnClickListener {
            progressGreater = false
        }
        if (progressNumber >= 0) {
            binding.advancedSearchProgressValue.setText(
                Integer.valueOf(progressNumber).toString()
            )
        }
        binding.advancedSearchGo.setOnClickListener {
            val progressValueInputTemp = binding.advancedSearchProgressValue.text.toString()
            if (progressValueInputTemp.isEmpty()) {
                progressNumber = -1
            } else {
                val progressNumberTemp = progressValueInputTemp.toInt()
                if (progressNumberTemp >= 0) {
                    progressNumber = progressNumberTemp
                }
            }
            val intent = Intent(this, ListCards::class.java)
            intent.putExtra("collection", -1)
            intent.putExtra("pack", pack)
            if (pack == -2) {
                intent.putIntegerArrayListExtra("packs", packList)
            }
            intent.putExtra("progressGreater", progressGreater)
            intent.putExtra("progressNumber", progressNumber)
            startActivity(intent)
        }
    }

    fun addToPackList(i: Int) {
        if (!packList.contains(i)) {
            packList.add(i)
        }
    }

    fun removeFromPackList(i: Int) {
        if (packList.contains(i)) {
            packList.remove(Integer.valueOf(i))
        }
    }
}
