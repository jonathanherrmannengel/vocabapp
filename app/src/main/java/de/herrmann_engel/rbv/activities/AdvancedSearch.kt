package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterPacksAdvancedSearch
import de.herrmann_engel.rbv.adapters.AdapterTagsAdvancedSearch
import de.herrmann_engel.rbv.cardlist.CardSource
import de.herrmann_engel.rbv.databinding.ActivityAdvancedSearchBinding
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.putCardSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Advanced search form: filter cards by packs, tags, progress and date of last repetition.
 * The results are shown in [ListCards].
 */
class AdvancedSearch : RBVActivity() {
    // Selections survive the activity being recreated (see AdvancedSearchViewModel)
    private val viewModel: AdvancedSearchViewModel by lazy {
        ViewModelProvider(this)[AdvancedSearchViewModel::class.java]
    }
    // Ids of the selected packs and tags (only used if "select" is chosen instead of "all")
    private val packList: ArrayList<Int>
        get() = viewModel.packList
    private val tagList: ArrayList<Int>
        get() = viewModel.tagList
    private lateinit var dbHelperGet: DbHelperGet
    private lateinit var binding: ActivityAdvancedSearchBinding
    private var allPacks: Boolean
        get() = viewModel.allPacks
        set(value) {
            viewModel.allPacks = value
        }
    private var allTags: Boolean
        get() = viewModel.allTags
        set(value) {
            viewModel.allTags = value
        }
    private var progressGreater: Boolean
        get() = viewModel.progressGreater
        set(value) {
            viewModel.progressGreater = value
        }
    private var repetitionOlder: Boolean
        get() = viewModel.repetitionOlder
        set(value) {
            viewModel.repetitionOlder = value
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdvancedSearchBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        dbHelperGet = DbHelperGet(this)
        // Packs including card counts and collection names, loaded in the background
        lifecycleScope.launch {
            val packs = withContext(Dispatchers.IO) { dbHelperGet.getAllPacksWithMeta() }
            binding.recAdvancedSearch.adapter = AdapterPacksAdvancedSearch(packs, viewModel::isPackSelected) { packId, checked ->
                viewModel.setPackSelected(packId, checked)
            }
        }
        binding.recAdvancedSearch.layoutManager = LinearLayoutManager(this)
        if (allPacks) {
            binding.recAdvancedSearch.visibility = View.GONE
        }
        binding.advancedSearchPacksAll.isChecked = allPacks
        binding.advancedSearchPacksAll.setOnClickListener {
            allPacks = true
            binding.recAdvancedSearch.visibility = View.GONE
        }
        binding.advancedSearchPacksSelect.isChecked = !allPacks
        binding.advancedSearchPacksSelect.setOnClickListener {
            allPacks = false
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
        binding.advancedSearchRepetitionOlder.isChecked = repetitionOlder
        binding.advancedSearchRepetitionOlder.setOnClickListener {
            repetitionOlder = true
        }
        binding.advancedSearchRepetitionNewer.isChecked = !repetitionOlder
        binding.advancedSearchRepetitionNewer.setOnClickListener {
            repetitionOlder = false
        }
        binding.advancedSearchGo.setOnClickListener {
            // Empty fields disable the corresponding filter
            val progressThreshold =
                binding.advancedSearchProgressValue.text.toString().toIntOrNull()
            val repetitionDays = binding.advancedSearchRepetitionValue.text.toString().toIntOrNull()
            val intent = Intent(this, ListCards::class.java)
            intent.putCardSource(
                CardSource.Search(
                    packIds = if (allPacks) null else packList,
                    tagIds = if (allTags) null else tagList,
                    progressGreater = progressGreater,
                    progressThreshold = progressThreshold,
                    repetitionOlder = repetitionOlder,
                    repetitionDays = repetitionDays
                )
            )
            startActivity(intent)
        }
        val settings = AppSettings(this)
        if (settings.uiBackgroundImages) {
            binding.backgroundImage.visibility = View.VISIBLE
            binding.backgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_advanced_search
                )
            )
        }
        setUpEdgeToEdge(binding.advancedSearchContainer)
    }

    override fun onResume() {
        super.onResume()
        // The tag filter is only shown if there are tags
        if (dbHelperGet.hasTags()) {
            binding.advancedSearchContainerTags.visibility = View.VISIBLE
            val tags = dbHelperGet.getAllTags()
            val adapterTags = AdapterTagsAdvancedSearch(tags, viewModel::isTagSelected) { tagId, checked ->
                viewModel.setTagSelected(tagId, checked)
            }
            binding.recAdvancedSearchTags.adapter = adapterTags
            binding.recAdvancedSearchTags.layoutManager = LinearLayoutManager(this)
            if (allTags) {
                binding.recAdvancedSearchTags.visibility = View.GONE
            } else {
                binding.recAdvancedSearchTags.visibility = View.VISIBLE
            }
            binding.advancedSearchTagsAll.isChecked = allTags
            binding.advancedSearchTagsAll.setOnClickListener {
                allTags = true
                binding.recAdvancedSearchTags.visibility = View.GONE
            }
            binding.advancedSearchTagsSelect.isChecked = !allTags
            binding.advancedSearchTagsSelect.setOnClickListener {
                allTags = false
                binding.recAdvancedSearchTags.visibility = View.VISIBLE
            }
        } else {
            binding.advancedSearchContainerTags.visibility = View.GONE
            allTags = true
        }
    }
}
