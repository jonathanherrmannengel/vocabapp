package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityCardFormBinding
import de.herrmann_engel.rbv.db.utils.DbHelperCreate
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.ui.CardForm
import de.herrmann_engel.rbv.utils.getPackId
import de.herrmann_engel.rbv.utils.putAddedCardId

/**
 * Form to create a new card in the pack passed as "pack" extra. The form is colored like the pack.
 */
class NewCard : RBVActivity() {
    private lateinit var form: CardForm
    private var packId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCardFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        packId = intent.getPackId() ?: run {
            finish()
            return
        }
        // Close if the pack no longer exists (e.g. deleted in the meantime)
        val pack = DbHelperGet(this).getSinglePack(packId) ?: run {
            finish()
            return
        }
        form = CardForm(
            this,
            binding,
            pack.colors,
            CardForm.Values("", "", ""),
            ::finish
        )
        setUpEdgeToEdge(binding.cardFormLayout)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        menu.findItem(R.id.menu_add_insert).setOnMenuItemClickListener {
            try {
                val cardId = DbHelperCreate(this).createCard(
                    form.front,
                    form.back,
                    form.notes,
                    packId
                )
                // Return to the existing card list, which adds the new card to its list
                val intent = Intent(this, ListCards::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putAddedCardId(cardId.toInt())
                this.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show()
            }
            return@setOnMenuItemClickListener true
        }
        return true
    }
}
