package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_LIST
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_LIST
import de.herrmann_engel.rbv.adapters.AdapterPacksAdvancedSearch
import de.herrmann_engel.rbv.adapters.AdapterTagsAdvancedSearch
import de.herrmann_engel.rbv.databinding.ActivityAdvancedSearchBinding
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class AdvancedSearch : RBVActivity() {
    private val packList = ArrayList<Int>()
    private val tagList = ArrayList<Int>()
    private lateinit var dbHelperGet: DB_Helper_Get
    private lateinit var binding: ActivityAdvancedSearchBinding
    private var pack = LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL
    private var tag = LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL
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
        if (pack == LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL) {
            binding.recAdvancedSearch.visibility = View.GONE
        }
        binding.advancedSearchPacksAll.isChecked =
            pack == LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL
        binding.advancedSearchPacksAll.setOnClickListener {
            pack = LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL
            binding.recAdvancedSearch.visibility = View.GONE
        }
        binding.advancedSearchPacksSelect.isChecked =
            pack == LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_LIST
        binding.advancedSearchPacksSelect.setOnClickListener {
            pack = LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_LIST
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
                progressNumber.toString()
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
                repetitionNumber.toString()
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
            intent.putExtra("collection", LIST_CARDS_GET_DB_COLLECTIONS_ALL)
            intent.putExtra("pack", pack)
            if (pack == LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_LIST) {
                intent.putIntegerArrayListExtra("packs", packList)
            }
            intent.putExtra("tag", tag)
            if (tag == LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_LIST) {
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
            if (tag == LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL) {
                binding.recAdvancedSearchTags.visibility = View.GONE
            } else {
                binding.recAdvancedSearchTags.visibility = View.VISIBLE
            }
            binding.advancedSearchTagsAll.isChecked =
                tag == LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL
            binding.advancedSearchTagsAll.setOnClickListener {
                tag = LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL
                binding.recAdvancedSearchTags.visibility = View.GONE
            }
            binding.advancedSearchTagsSelect.isChecked =
                tag == LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_LIST
            binding.advancedSearchTagsSelect.setOnClickListener {
                tag = LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_LIST
                binding.recAdvancedSearchTags.visibility = View.VISIBLE
            }
        } else {
            binding.advancedSearchContainerTags.visibility = View.GONE
            tag = LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL
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
