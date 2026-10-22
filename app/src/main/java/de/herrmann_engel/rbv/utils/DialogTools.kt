package de.herrmann_engel.rbv.utils

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.databinding.DiaInfoBinding
import de.herrmann_engel.rbv.databinding.DiaWaitBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Operations that finish faster are not worth a message
private const val WAIT_DIALOG_DELAY_MILLIS = 500L
// Time for one way of a jump of the dots in the wait dialog (up or down)
private const val WAIT_DOT_JUMP_MILLIS = 300L

/**
 * Creates a dialog in the app's dialog style that fills the screen and shows the given view.
 * The dialog is not shown yet.
 */
fun createDialog(context: Context, view: View, title: CharSequence): Dialog {
    val dialog = Dialog(context, R.style.dia_view)
    dialog.setContentView(view)
    dialog.setTitle(title)
    dialog.window?.setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT
    )
    return dialog
}

/**
 * Shows a dialog with yes and no buttons and an optional explanation. "Yes" closes the dialog and
 * then calls onYes; "no" only closes it.
 */
fun showConfirmDialog(
    context: Context,
    title: CharSequence,
    text: CharSequence? = null,
    onYes: () -> Unit
) {
    val binding = DiaConfirmBinding.inflate(LayoutInflater.from(context))
    val dialog = createDialog(context, binding.root, title)
    if (text != null) {
        binding.diaConfirmDesc.text = text
        binding.diaConfirmDesc.visibility = View.VISIBLE
    }
    binding.diaConfirmYes.setOnClickListener {
        dialog.dismiss()
        onYes()
    }
    binding.diaConfirmNo.setOnClickListener { dialog.dismiss() }
    dialog.show()
}

/**
 * Shows a text with an OK button. onClose is called when the dialog is closed, by the button or
 * the back button.
 */
fun showMessageDialog(
    context: Context,
    title: CharSequence,
    text: CharSequence,
    onClose: () -> Unit
) {
    val binding = DiaConfirmBinding.inflate(LayoutInflater.from(context))
    val dialog = createDialog(context, binding.root, title)
    binding.diaConfirmDesc.text = text
    binding.diaConfirmDesc.visibility = View.VISIBLE
    binding.diaConfirmYes.setText(android.R.string.ok)
    binding.diaConfirmYes.setOnClickListener { dialog.dismiss() }
    binding.diaConfirmNo.visibility = View.GONE
    dialog.setOnDismissListener { onClose() }
    dialog.show()
}

// Shows a text in a dialog that is closed with the back button
fun showInfoDialog(context: Context, title: CharSequence, text: CharSequence) {
    val binding = DiaInfoBinding.inflate(LayoutInflater.from(context))
    binding.diaInfoText.text = text
    createDialog(context, binding.root, title).show()
}

/**
 * Shows a "please wait" dialog with jumping dots that cannot be closed by the user. It blocks the
 * activity at once, but stays invisible until WAIT_DIALOG_DELAY_MILLIS have passed. Closing it is
 * up to the caller.
 */
fun CoroutineScope.showWaitDialog(context: Context): Dialog {
    val binding = DiaWaitBinding.inflate(LayoutInflater.from(context))
    val dialog = createDialog(context, binding.root, context.getString(R.string.wait))
    dialog.setCancelable(false)
    val window = dialog.window
    val dimAmount = window?.attributes?.dimAmount ?: 0f
    window?.attributes = window.attributes.apply {
        alpha = 0f
        this.dimAmount = 0f
    }
    dialog.show()
    val showJob = launch {
        delay(WAIT_DIALOG_DELAY_MILLIS)
        window?.attributes = window.attributes.apply {
            alpha = 1f
            this.dimAmount = dimAmount
        }
    }
    // The dots jump one after the other, half a jump apart, so they move like a wave
    val jumpHeight = context.resources.getDimension(R.dimen.wait_dot_jump)
    val dots = listOf(binding.diaWaitDotRed, binding.diaWaitDotBlue, binding.diaWaitDotYellow)
    val jumps = dots.mapIndexed { index, dot ->
        ObjectAnimator.ofFloat(dot, View.TRANSLATION_Y, 0f, -jumpHeight).apply {
            duration = WAIT_DOT_JUMP_MILLIS
            startDelay = index * WAIT_DOT_JUMP_MILLIS / 2
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
    dialog.setOnDismissListener {
        showJob.cancel()
        jumps.forEach { it.cancel() }
    }
    return dialog
}
