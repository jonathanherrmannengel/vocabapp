package de.herrmann_engel.rbv.utils

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.WaitDotsBinding

// Operations that finish faster are not worth a wait indicator
const val WAIT_INDICATOR_DELAY_MILLIS = 500L

// Time for one way of a jump of the dots (up or down)
private const val WAIT_DOT_JUMP_MILLIS = 300L

/**
 * Jumping dots in the app colors (layout wait_dots), shown while something takes longer.
 * The dots jump one after the other, half a jump apart, so they move like a wave.
 */
class WaitDots(private val binding: WaitDotsBinding) {
    private var jumps: List<ObjectAnimator> = emptyList()

    fun show() {
        binding.root.visibility = View.VISIBLE
        if (jumps.isNotEmpty()) {
            return
        }
        val jumpHeight = binding.root.resources.getDimension(R.dimen.wait_dot_jump)
        val dots = listOf(binding.waitDotRed, binding.waitDotBlue, binding.waitDotYellow)
        jumps = dots.mapIndexed { index, dot ->
            ObjectAnimator.ofFloat(dot, View.TRANSLATION_Y, 0f, -jumpHeight).apply {
                duration = WAIT_DOT_JUMP_MILLIS
                startDelay = index * WAIT_DOT_JUMP_MILLIS / 2
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.REVERSE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    // Also stops the animation, which would otherwise run on endlessly
    fun hide() {
        binding.root.visibility = View.GONE
        jumps.forEach { it.cancel() }
        jumps = emptyList()
    }
}
