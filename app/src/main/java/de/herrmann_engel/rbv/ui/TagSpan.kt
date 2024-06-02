package de.herrmann_engel.rbv.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.RectF
import android.text.style.ReplacementSpan
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import de.herrmann_engel.rbv.R
import kotlin.math.roundToInt

class TagSpan(private val context: Context, private val backgroundColor: Int) : ReplacementSpan() {
    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val rectBorder = RectF(
            x + MARGIN / 2, top.toFloat(), x + MARGIN / 2 + paint.measureText(
                text,
                start,
                end
            ) + PADDING, bottom.toFloat()
        )
        val borderWidth = PADDING / 30
        val rect = RectF(
            x + MARGIN / 2 + borderWidth,
            top.toFloat() + borderWidth,
            x + MARGIN / 2 + paint.measureText(
                text,
                start,
                end
            ) + PADDING - borderWidth,
            bottom.toFloat() - borderWidth
        )
        val backgroundColorRed = Color.red(backgroundColor)
        val backgroundColorGreen = Color.green(backgroundColor)
        val backgroundColorBlue = Color.blue(backgroundColor)
        val highestColorValue = backgroundColorRed.coerceAtLeast(backgroundColorGreen)
            .coerceAtLeast(backgroundColorBlue)
        val maxColorToFill =
            if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
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
        paint.color = if (highestColorValue == 0) {
            Color.rgb(maxColorToFill, maxColorToFill, maxColorToFill)
        } else {
            Color.rgb(
                ((backgroundColorRed).toFloat() / highestColorValue * maxColorToFill).roundToInt(),
                ((backgroundColorGreen).toFloat() / highestColorValue * maxColorToFill).roundToInt(),
                ((backgroundColorBlue).toFloat() / highestColorValue * maxColorToFill).roundToInt()
            )
        }
        canvas.drawRoundRect(rectBorder, rectBorder.height() / 2, rectBorder.height() / 2, paint)
        paint.color = backgroundColor
        canvas.drawRoundRect(rect, rect.height() / 2, rect.height() / 2, paint)
        paint.color = ContextCompat.getColor(
            context,
            if (ColorUtils.calculateLuminance(backgroundColor) < 0.5) {
                R.color.tag_foreground_light
            } else {
                R.color.tag_foreground_dark
            }
        )
        canvas.drawText(text, start, end, (x + MARGIN / 2 + PADDING / 2), y.toFloat(), paint)
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: FontMetricsInt?
    ): Int {
        return (MARGIN + paint.measureText(text, start, end) + PADDING).roundToInt()
    }

    companion object {
        private const val PADDING = 50.0f
        private const val MARGIN = 25.0f
    }
}
