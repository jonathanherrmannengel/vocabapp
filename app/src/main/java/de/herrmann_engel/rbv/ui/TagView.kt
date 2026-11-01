package de.herrmann_engel.rbv.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.vanniktech.emoji.EmojiTextView
import de.herrmann_engel.rbv.R
import kotlin.math.roundToInt

/**
 * A tag as a rounded pill with the given background color, a border in a shade of that color and
 * a foreground color with good contrast. Each tag is a view of its own (not a span in a shared
 * text), so that its emoji stays inside the pill and screen readers read the tags one by one.
 */
fun createTagView(context: Context, text: String, backgroundColor: Int): TextView {
    val resources = context.resources
    // Emojis in the app's emoji style, like those of filing boxes and decks
    val view = EmojiTextView(context)
    view.text = text
    view.setTextSize(
        TypedValue.COMPLEX_UNIT_PX,
        resources.getDimension(R.dimen.card_notes_size_default)
    )
    // Light text on dark backgrounds and vice versa
    view.setTextColor(
        ContextCompat.getColor(
            context,
            if (ColorUtils.calculateLuminance(backgroundColor) < 0.5) {
                R.color.tag_foreground_light
            } else {
                R.color.tag_foreground_dark
            }
        )
    )
    val paddingHorizontal = resources.getDimensionPixelSize(R.dimen.tag_padding_horizontal)
    val paddingVertical = resources.getDimensionPixelSize(R.dimen.tag_padding_vertical)
    view.setPaddingRelative(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
    val pill = GradientDrawable()
    // Larger than half of any height, so that the ends are fully rounded
    pill.cornerRadius = resources.displayMetrics.heightPixels.toFloat()
    pill.setColor(backgroundColor)
    pill.setStroke(
        resources.getDimensionPixelSize(R.dimen.tag_border),
        borderColor(context, backgroundColor)
    )
    view.background = pill
    val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.marginStart = resources.getDimensionPixelSize(R.dimen.tag_margin_horizontal)
    params.marginEnd = resources.getDimensionPixelSize(R.dimen.tag_margin_horizontal)
    view.layoutParams = params
    return view
}

// Same hue as the background, scaled to a brightness that fits day/night mode
private fun borderColor(context: Context, backgroundColor: Int): Int {
    val red = Color.red(backgroundColor)
    val green = Color.green(backgroundColor)
    val blue = Color.blue(backgroundColor)
    val highestColorValue = red.coerceAtLeast(green).coerceAtLeast(blue)
    val nightMode = context.resources.configuration.uiMode and
        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    val maxColorToFill = if (nightMode) {
        if (highestColorValue < 220) {
            (highestColorValue * 1.15).roundToInt().coerceAtLeast(220)
        } else {
            (highestColorValue / 1.5).roundToInt().coerceAtLeast(180)
        }
    } else {
        if (highestColorValue < 100) {
            (highestColorValue * 1.15).roundToInt().coerceAtLeast(100)
        } else {
            (highestColorValue / 1.5).roundToInt()
        }
    }
    return if (highestColorValue == 0) {
        Color.rgb(maxColorToFill, maxColorToFill, maxColorToFill)
    } else {
        Color.rgb(
            (red.toFloat() / highestColorValue * maxColorToFill).roundToInt(),
            (green.toFloat() / highestColorValue * maxColorToFill).roundToInt(),
            (blue.toFloat() / highestColorValue * maxColorToFill).roundToInt()
        )
    }
}
