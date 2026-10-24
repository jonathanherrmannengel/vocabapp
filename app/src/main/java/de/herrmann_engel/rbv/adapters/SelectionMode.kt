package de.herrmann_engel.rbv.adapters

import android.app.Activity
import android.content.Context
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.utils.findActivity

/**
 * Selection of several items of a list in a contextual action mode, started by a long click on an
 * item; shared by the card and pack lists. Items are identified by their ids.
 *
 * The adapter provides the menu: prepareMenu sets which actions are visible for the current
 * selection, onAction runs an action and returns false if the item id is not handled. The adapter
 * also redraws an item whose selection changed (redrawItem) or all items (redrawAll, e.g. after
 * selecting all), see PAYLOAD, and tells the number of items for the title ("x of y selected").
 */
class SelectionMode(
    @MenuRes private val menuId: Int,
    private val itemCount: () -> Int,
    private val redrawItem: (id: Int) -> Unit,
    private val redrawAll: () -> Unit,
    private val prepareMenu: (menu: Menu, selectedIds: Set<Int>) -> Unit,
    private val onAction: (itemId: Int, selectedIds: List<Int>) -> Boolean
) {
    private var actionMode: ActionMode? = null
    private var activity: Activity? = null
    // In the order of selection (e.g. the order of printed cards)
    private val selectedIds = LinkedHashSet<Int>()

    private val callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(menuId, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            prepareMenu(menu, selectedIds)
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean =
            onAction(item.itemId, selectedIds.toList())

        // The selected items are drawn as unselected again
        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            selectedIds.clear()
            redrawAll()
        }
    }

    val isActive: Boolean
        get() = actionMode != null

    fun isSelected(id: Int): Boolean = id in selectedIds

    // Long click on an item: starts the selection with it; false if a selection is already running
    // or cannot be started
    fun start(context: Context, id: Int): Boolean {
        if (isActive) {
            return false
        }
        activity = context.findActivity()
        actionMode = activity?.startActionMode(callback) ?: return false
        toggle(id)
        return true
    }

    // Click on an item while selecting: selects or deselects it; ends the selection if it is empty
    fun toggle(id: Int) {
        if (id in selectedIds) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }
        redrawItem(id)
        if (selectedIds.isEmpty()) {
            finish()
            return
        }
        updateMenuAndTitle()
    }

    fun selectAll(ids: List<Int>) {
        selectedIds.addAll(ids)
        redrawAll()
        updateMenuAndTitle()
    }

    fun finish() {
        actionMode?.finish()
    }

    // The visible actions may depend on the number of selected items
    private fun updateMenuAndTitle() {
        actionMode?.apply {
            invalidate()
            title = activity?.getString(R.string.selected_title, selectedIds.size, itemCount())
        }
    }

    companion object {
        // Passed to notifyItemChanged when only the selection of an item changed, so that the item
        // is redrawn in place without the crossfade animation
        const val PAYLOAD = "selection"
    }
}
