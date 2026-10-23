package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityEditCardBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.getBackToList
import de.herrmann_engel.rbv.utils.getCardId
import de.herrmann_engel.rbv.utils.putUpdatedCardId
import de.herrmann_engel.rbv.utils.showConfirmDialog

/**
 * Form to edit the card passed as "card" extra. The form is colored like the card's pack.
 */
class EditCard : RBVActivity() {
    private lateinit var binding: ActivityEditCardBinding
    private lateinit var card: DB_Card
    // If set, return to the card list after closing instead of the previous screen
    private var backToList = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dbHelperGet = DB_Helper_Get(this)
        val cardId = intent.getCardId() ?: run {
            finish()
            return
        }
        backToList = intent.getBackToList()
        // Close if the card no longer exists (e.g. deleted in the meantime)
        card = dbHelperGet.getSingleCard(cardId) ?: run {
            finish()
            return
        }
        binding.editCardFront.setText(card.front)
        // Allow multiple lines in the input field if the text already has multiple lines
        if (card.front.contains(System.lineSeparator())) {
            binding.editCardFront.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            binding.editCardFront.isSingleLine = false
        }
        binding.editCardBack.setText(card.back)
        if (card.back.contains(System.lineSeparator())) {
            binding.editCardBack.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            binding.editCardBack.isSingleLine = false
        }
        binding.editCardNotes.setText(card.notes)
        binding.editCardNotesLayout.hint =
            String.format(getString(R.string.optional), getString(R.string.card_notes))
        val packColors = dbHelperGet.getSinglePack(card.packId)?.colors
        val color = resources.colorAt(R.array.pack_color_main, packColors)
        val colorBackground = resources.colorAt(R.array.pack_color_background, packColors)
        if (color != null && colorBackground != null) {
            binding.editCardFrontLayout.boxStrokeColor = color
            binding.editCardFrontLayout.hintTextColor =
                ColorStateList.valueOf(color)
            binding.editCardBackLayout.boxStrokeColor = color
            binding.editCardBackLayout.hintTextColor =
                ColorStateList.valueOf(color)
            binding.editCardNotesLayout.boxStrokeColor = color
            binding.editCardNotesLayout.hintTextColor =
                ColorStateList.valueOf(color)
            binding.root.setBackgroundColor(colorBackground)
        }
        // Ask before discarding changes
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val front = binding.editCardFront.text.toString()
                val back = binding.editCardBack.text.toString()
                val notes = binding.editCardNotes.text.toString()
                if (card.front == front && card.back == back && card.notes == notes) {
                    close()
                } else {
                    showConfirmDialog(this@EditCard, getString(R.string.discard_changes)) {
                        close()
                    }
                }
            }
        })
        setUpEdgeToEdge(binding.editCardLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.menu_edit_save).setOnMenuItemClickListener {
            card.front = binding.editCardFront.text.toString()
            card.back = binding.editCardBack.text.toString()
            card.notes = binding.editCardNotes.text.toString()
            val dbHelperUpdate = DB_Helper_Update(this)
            if (dbHelperUpdate.updateCard(card)) {
                close()
            } else {
                Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show()
            }
            return@setOnMenuItemClickListener true
        }
        return true
    }

    // Returns to the card list (which then reloads the card) or simply finishes
    private fun close() {
        if (backToList) {
            val intent = Intent(this, ListCards::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putUpdatedCardId(card.uid)
            startActivity(intent)
        } else {
            finish()
        }
    }
}
