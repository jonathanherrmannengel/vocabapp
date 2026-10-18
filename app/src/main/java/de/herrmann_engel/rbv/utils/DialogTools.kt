package de.herrmann_engel.rbv.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.databinding.DiaInfoBinding

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

// Shows a text in a dialog that is closed with the back button
fun showInfoDialog(context: Context, title: CharSequence, text: CharSequence) {
    val binding = DiaInfoBinding.inflate(LayoutInflater.from(context))
    binding.diaInfoText.text = text
    createDialog(context, binding.root, title).show()
}
