package de.herrmann_engel.rbv.ui

import android.content.res.ColorStateList
import android.text.InputType
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityCardFormBinding
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.showConfirmDialog

/**
 * Form for the front, back and notes of a card, shared by the screens that create and edit cards.
 * The form is colored like the card's pack. Shows the given initial values and asks before
 * discarding changes when the user leaves the screen; onClose then closes the screen.
 *
 * @param packColors index of the pack's color (see R.array.pack_color_main)
 */
class CardForm(
    private val activity: ComponentActivity,
    private val binding: ActivityCardFormBinding,
    packColors: Int?,
    initial: Values,
    onClose: () -> Unit
) {
    // The values of a card as edited in the form
    data class Values(val front: String, val back: String, val notes: String)

    // The values as shown when the form was opened; leaving the form asks only if they changed
    private val shown: Values

    val front: String
        get() = binding.cardFormFront.text.toString()

    val back: String
        get() = binding.cardFormBack.text.toString()

    val notes: String
        get() = binding.cardFormNotes.text.toString()

    init {
        binding.cardFormNotesLayout.hint = String.format(
            activity.getString(R.string.optional),
            activity.getString(R.string.card_notes)
        )
        binding.cardFormFront.setText(initial.front)
        binding.cardFormBack.setText(initial.back)
        binding.cardFormNotes.setText(initial.notes)
        // Allow multiple lines in the input field if the text already has multiple lines
        for (field in listOf(binding.cardFormFront, binding.cardFormBack)) {
            if (field.text.toString().contains(System.lineSeparator())) {
                field.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                field.isSingleLine = false
            }
        }
        val main = activity.resources.colorAt(R.array.pack_color_main, packColors)
        val background = activity.resources.colorAt(R.array.pack_color_background, packColors)
        if (main != null && background != null) {
            applyColors(main, background)
        }
        shown = currentValues()
        // Ask before discarding changes
        activity.onBackPressedDispatcher.addCallback(activity, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasChanges()) {
                    showConfirmDialog(activity, activity.getString(R.string.discard_changes)) {
                        onClose()
                    }
                } else {
                    onClose()
                }
            }
        })
    }

    private fun currentValues() = Values(front, back, notes)

    private fun hasChanges(): Boolean = currentValues() != shown

    private fun applyColors(main: Int, background: Int) {
        val mainList = ColorStateList.valueOf(main)
        for (layout in listOf(
            binding.cardFormFrontLayout,
            binding.cardFormBackLayout,
            binding.cardFormNotesLayout
        )) {
            layout.boxStrokeColor = main
            layout.hintTextColor = mainList
        }
        binding.root.setBackgroundColor(background)
    }
}
