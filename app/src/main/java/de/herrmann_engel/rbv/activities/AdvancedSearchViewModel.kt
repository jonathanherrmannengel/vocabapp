package de.herrmann_engel.rbv.activities

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL

/**
 * State of [AdvancedSearch] that has to survive the activity being recreated (e.g. after a
 * theme or language change) or the app process being killed in the background.
 *
 * The progress and repetition values are not stored here: their input fields keep their text
 * themselves.
 */
class AdvancedSearchViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    // LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL or ..._LIST (only the selected packs)
    var pack: Int
        get() = savedState[KEY_PACK] ?: LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL
        set(value) {
            savedState[KEY_PACK] = value
        }

    // LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL or ..._LIST (only the selected tags)
    var tag: Int
        get() = savedState[KEY_TAG] ?: LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL
        set(value) {
            savedState[KEY_TAG] = value
        }

    var progressGreater: Boolean
        get() = savedState[KEY_PROGRESS_GREATER] ?: false
        set(value) {
            savedState[KEY_PROGRESS_GREATER] = value
        }

    var repetitionOlder: Boolean
        get() = savedState[KEY_REPETITION_OLDER] ?: false
        set(value) {
            savedState[KEY_REPETITION_OLDER] = value
        }

    // Ids of the selected packs and tags (copies; change them via the functions below)
    val packList: ArrayList<Int>
        get() = ArrayList(savedState.get<ArrayList<Int>>(KEY_PACK_LIST) ?: arrayListOf())
    val tagList: ArrayList<Int>
        get() = ArrayList(savedState.get<ArrayList<Int>>(KEY_TAG_LIST) ?: arrayListOf())

    fun setPackSelected(packId: Int, selected: Boolean) {
        savedState[KEY_PACK_LIST] = changeList(packList, packId, selected)
    }

    fun setTagSelected(tagId: Int, selected: Boolean) {
        savedState[KEY_TAG_LIST] = changeList(tagList, tagId, selected)
    }

    private fun changeList(list: ArrayList<Int>, id: Int, selected: Boolean): ArrayList<Int> {
        if (selected && !list.contains(id)) {
            list.add(id)
        } else if (!selected) {
            list.remove(id)
        }
        return list
    }

    companion object {
        private const val KEY_PACK = "pack"
        private const val KEY_TAG = "tag"
        private const val KEY_PROGRESS_GREATER = "progressGreater"
        private const val KEY_REPETITION_OLDER = "repetitionOlder"
        private const val KEY_PACK_LIST = "packList"
        private const val KEY_TAG_LIST = "tagList"
    }
}
