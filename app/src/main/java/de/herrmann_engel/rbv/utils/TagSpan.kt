package de.herrmann_engel.rbv.utils

import android.content.Context
import android.graphics.Canvas
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
        val rect = RectF(
            x + MARGIN / 2, top.toFloat(), x + paint.measureText(
                text,
                start,
                end
            ) + PADDING + MARGIN / 2, bottom.toFloat()
        )
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
        val xPos = (x + PADDING / 2 + MARGIN / 2).roundToInt()
        val yPos = (y)
        canvas.drawText(text, start, end, xPos.toFloat(), yPos.toFloat(), paint)
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: FontMetricsInt?
    ): Int {
        return (paint.measureText(text, start, end) + PADDING + MARGIN).roundToInt()
    }

    companion object {
        private const val PADDING = 50.0f
        private const val MARGIN = 25.0f
    }
}
