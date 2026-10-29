package de.herrmann_engel.rbv.ui

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityViewCollectionOrPackBinding
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.formatDateTime

/**
 * Shows the details of a collection or pack (name, emoji, description, creation date in seconds)
 * on the screen shared by both, with its background color. An empty emoji or description is
 * hidden.
 */
fun ActivityViewCollectionOrPackBinding.showDetails(
    name: String,
    emoji: String,
    desc: String,
    date: Long,
    colors: Int,
    uiFontSizeBig: Boolean
) {
    val context = root.context
    fun setSize(view: TextView, big: Int, default: Int) {
        view.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            context.resources.getDimension(if (uiFontSizeBig) big else default)
        )
    }
    collectionOrPackName.text = name
    setSize(collectionOrPackName, R.dimen.details_name_size_big, R.dimen.details_name_size_default)
    collectionOrPackEmoji.text = emoji
    collectionOrPackEmoji.visibility = if (emoji.isBlank()) View.GONE else View.VISIBLE
    setSize(collectionOrPackEmoji, R.dimen.details_name_size_big, R.dimen.details_name_size_default)
    collectionOrPackDesc.text = desc
    collectionOrPackDesc.visibility = if (desc.isBlank()) View.GONE else View.VISIBLE
    setSize(collectionOrPackDesc, R.dimen.details_desc_size_big, R.dimen.details_desc_size_default)
    collectionOrPackDate.text = formatDateTime(context, date)
    context.resources.colorAt(R.array.pack_color_background, colors)
        ?.let { root.setBackgroundColor(it) }
}
