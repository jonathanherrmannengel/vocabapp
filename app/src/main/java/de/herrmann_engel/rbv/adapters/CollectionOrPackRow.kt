package de.herrmann_engel.rbv.adapters

import android.content.Context
import android.graphics.Color
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
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.colorAt

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

/**
 * The preview of a row: the emoji, or without one the first character of the name; a check mark
 * if the row is selected.
 */
fun RecViewCollectionOrPackBinding.showPreview(
    name: String,
    emoji: String,
    selected: Boolean
) {
    recCollectionsPreviewText.text = when {
        selected -> "✓"
        emoji.isBlank() -> StringTools.firstCharacter(name)
        else -> emoji
    }
}

// The number of entries of a row (e.g. the cards of a pack), also as text for screen readers
fun RecViewCollectionOrPackBinding.showCounter(count: Int) {
    recCollectionsNumberText.text = count.toString()
    recCollectionsNumberText.contentDescription =
        root.resources.getQuantityString(R.plurals.items, count, count)
}

/**
 * Colors a row like its collection or pack (index into R.array.pack_color_list); the check mark of
 * a selected row is shown without the colored preview background.
 */
fun RecViewCollectionOrPackBinding.applyColors(colors: Int, selected: Boolean) {
    val resources = root.resources
    val color = resources.colorAt(R.array.pack_color_list, colors)
    val colorBackground = resources.colorAt(R.array.pack_color_background_light, colors)
    val colorBackgroundAlpha =
        resources.colorAt(R.array.pack_color_background_light_alpha, colors)
    if (color == null || colorBackground == null || colorBackgroundAlpha == null) {
        return
    }
    recCollectionsName.setTextColor(color)
    recCollectionsPreviewText.setTextColor(
        if (selected) ContextCompat.getColor(root.context, R.color.default_text) else color
    )
    recCollectionsPreviewText.setBackgroundColor(
        if (selected) Color.TRANSPARENT else colorBackground
    )
    val (background, backgroundBehind) = backgroundLayers()
    background.mutate()
    background.setStroke(2, color)
    background.setColor(colorBackgroundAlpha)
    backgroundBehind.mutate()
    backgroundBehind.setStroke(1, colorBackground)
}
