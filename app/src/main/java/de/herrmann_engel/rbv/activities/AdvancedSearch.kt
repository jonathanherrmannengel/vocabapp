package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.adapters.AdapterPacksAdvancedSearch
import de.herrmann_engel.rbv.adapters.AdapterTagsAdvancedSearch
import de.herrmann_engel.rbv.databinding.ActivityAdvancedSearchBinding
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class AdvancedSearch : AppCompatActivity() {
    private lateinit var dbHelperGet: DB_Helper_Get
    private val packList = ArrayList<Int>()
    private val tagList = ArrayList<Int>()
    private lateinit var binding: ActivityAdvancedSearchBinding
    private var pack = -3
    private var tag = -3
    private var progressGreater = false
    private var progressNumber = -1
    private var repetitionOlder = false
    private var repetitionNumber = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvancedSearchBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
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
        binding.advancedSearchRepetitionOlder.isChecked = repetitionOlder
        binding.advancedSearchRepetitionOlder.setOnClickListener {
            repetitionOlder = true
        }
        binding.advancedSearchRepetitionNewer.isChecked = !repetitionOlder
        binding.advancedSearchRepetitionNewer.setOnClickListener {
            repetitionOlder = false
        }
        if (repetitionNumber >= 0) {
            binding.advancedSearchRepetitionValue.setText(
                Integer.valueOf(repetitionNumber).toString()
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
            val repetitionValueInputTemp = binding.advancedSearchRepetitionValue.text.toString()
            if (repetitionValueInputTemp.isEmpty()) {
                repetitionNumber = -1
            } else {
                val repetitionNumberTemp = repetitionValueInputTemp.toInt()
                if (repetitionNumberTemp >= 0) {
                    repetitionNumber = repetitionNumberTemp
                }
            }
            val intent = Intent(this, ListCards::class.java)
            intent.putExtra("collection", -1)
            intent.putExtra("pack", pack)
            if (pack == -2) {
                intent.putIntegerArrayListExtra("packs", packList)
            }
            intent.putExtra("tag", tag)
            if (tag == -2) {
                intent.putIntegerArrayListExtra("tags", tagList)
            }
            intent.putExtra("progressGreater", progressGreater)
            intent.putExtra("progressNumber", progressNumber)
            intent.putExtra("repetitionOlder", repetitionOlder)
            intent.putExtra("repetitionNumber", repetitionNumber)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (dbHelperGet.hasTags()) {
            binding.advancedSearchContainerTags.visibility = View.VISIBLE
            val tags = dbHelperGet.allTags
            val adapterTags = AdapterTagsAdvancedSearch(tags, tagList)
            binding.recAdvancedSearchTags.adapter = adapterTags
            binding.recAdvancedSearchTags.layoutManager = LinearLayoutManager(this)
            if (tag == -3) {
                binding.recAdvancedSearchTags.visibility = View.GONE
            } else {
                binding.recAdvancedSearchTags.visibility = View.VISIBLE
            }
            binding.advancedSearchTagsAll.isChecked = tag == -3
            binding.advancedSearchTagsAll.setOnClickListener {
                tag = -3
                binding.recAdvancedSearchTags.visibility = View.GONE
            }
            binding.advancedSearchTagsSelect.isChecked = tag == -2
            binding.advancedSearchTagsSelect.setOnClickListener {
                tag = -2
                binding.recAdvancedSearchTags.visibility = View.VISIBLE
            }
        } else {
            binding.advancedSearchContainerTags.visibility = View.GONE
            tag = -3
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

    fun addToTagList(i: Int) {
        if (!tagList.contains(i)) {
            tagList.add(i)
        }
    }

    fun removeFromTagList(i: Int) {
        if (tagList.contains(i)) {
            tagList.remove(Integer.valueOf(i))
        }
    }
}
