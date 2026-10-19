package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.RecViewCollectionOrPackBinding

// Text sizes of a row in the collection and pack lists, depending on the font size setting
fun RecViewCollectionOrPackBinding.applyFontSize(uiFontSizeBig: Boolean) {
    val resources = root.resources
    fun setSize(view: TextView, big: Int, default: Int) {
        view.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(if (uiFontSizeBig) big else default)
        )
    }
    setSize(recCollectionsName, R.dimen.rec_view_font_size_big, R.dimen.rec_view_font_size_default)
    setSize(
        recCollectionsDesc,
        R.dimen.rec_view_font_size_below_big,
        R.dimen.rec_view_font_size_below_default
    )
    setSize(
        recCollectionsParent,
        R.dimen.rec_view_font_size_above_big,
        R.dimen.rec_view_font_size_above_default
    )
}

// The main layer and the layer behind it of the row background
fun RecViewCollectionOrPackBinding.backgroundLayers(): Pair<GradientDrawable, GradientDrawable> {
    val layers = root.background as LayerDrawable
    val main = layers.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_main)
    val behind = layers.findDrawableByLayerId(R.id.rec_view_collection_or_pack_background_behind)
    return Pair(main as GradientDrawable, behind as GradientDrawable)
}

/**
 * The text with each of the given characters replaced by an icon (drawable resource), e.g. "+" by
 * the icon of the "add" button, so that the text can refer to buttons of the toolbar.
 */
fun iconText(context: Context, text: String, icons: Map<Char, Int>): SpannableString {
    val result = SpannableString(text)
    for ((character, icon) in icons) {
        val index = text.indexOf(character)
        if (index < 0) continue
        val drawable = ContextCompat.getDrawable(context, icon) ?: continue
        drawable.setTint(context.getColor(R.color.light_black))
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        result.setSpan(
            ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM),
            index,
            index + 1,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
    }
    return result
}
