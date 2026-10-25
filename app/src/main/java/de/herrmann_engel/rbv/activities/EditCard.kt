package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityCardFormBinding
import de.herrmann_engel.rbv.db.DbCard
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.db.utils.DbHelperUpdate
import de.herrmann_engel.rbv.ui.CardForm
import de.herrmann_engel.rbv.utils.getBackToList
import de.herrmann_engel.rbv.utils.getCardId
import de.herrmann_engel.rbv.utils.putUpdatedCardId

/**
 * Form to edit the card passed as "card" extra. The form is colored like the card's pack.
 */
class EditCard : RBVActivity() {
    private lateinit var form: CardForm
    private lateinit var card: DbCard
    // If set, return to the card list after closing instead of the previous screen
    private var backToList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCardFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dbHelperGet = DbHelperGet(this)
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
        form = CardForm(
            this,
            binding,
            dbHelperGet.getSinglePack(card.packId)?.colors,
            CardForm.Values(card.front, card.back, card.notes),
            ::close
        )
        setUpEdgeToEdge(binding.cardFormLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.menu_edit_save).setOnMenuItemClickListener {
            card.front = form.front
            card.back = form.back
            card.notes = form.notes
            if (DbHelperUpdate(this).updateCard(card)) {
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
