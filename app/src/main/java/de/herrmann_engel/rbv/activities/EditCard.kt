package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityEditCardBinding
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update

class EditCard : AppCompatActivity() {
    private lateinit var binding: ActivityEditCardBinding
    private var card: DB_Card? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val cardNo = intent.extras!!.getInt("card")
        val dbHelperGet = DB_Helper_Get(this)
        try {
            card = dbHelperGet.getSingleCard(cardNo)
            binding.editCardFront.setText(card!!.front)
            if (System.getProperty("line.separator")?.let { card!!.front.contains(it) } == true) {
                binding.editCardFront.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                binding.editCardFront.isSingleLine = false
            }
            binding.editCardBack.setText(card!!.back)
            if (System.getProperty("line.separator")?.let { card!!.back.contains(it) } == true) {
                binding.editCardBack.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                binding.editCardBack.isSingleLine = false
            }
            binding.editCardNotes.setText(card!!.notes)
            binding.editCardNotes.hint =
                String.format(getString(R.string.optional), getString(R.string.card_notes))
            val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
            val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
            val packColors = dbHelperGet.getSinglePack(card!!.pack).colors
            if (packColors >= 0 && packColors < colorsStatusBar.length() && packColors < colorsBackground.length()) {
                val colorStatusBar = colorsStatusBar.getColor(packColors, 0)
                val colorBackground = colorsBackground.getColor(packColors, 0)
                supportActionBar?.setBackgroundDrawable(ColorDrawable(colorStatusBar))
                val window = this.window
                window.statusBarColor = colorStatusBar
                binding.root.setBackgroundColor(colorBackground)
            }
            colorsStatusBar.recycle()
            colorsBackground.recycle()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val front = binding.editCardFront.text.toString()
                val back = binding.editCardBack.text.toString()
                val notes = binding.editCardNotes.text.toString()
                if (card == null || card!!.front == front && card!!.back == back && card!!.notes == notes) {
                    finish()
                } else {
                    val confirmCancelDialog = Dialog(this@EditCard, R.style.dia_view)
                    val bindingConfirmCancelDialog = DiaConfirmBinding.inflate(
                        layoutInflater
                    )
                    confirmCancelDialog.setContentView(bindingConfirmCancelDialog.root)
                    confirmCancelDialog.setTitle(resources.getString(R.string.discard_changes))
                    confirmCancelDialog.window!!.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    bindingConfirmCancelDialog.diaConfirmYes.setOnClickListener { finish() }
                    bindingConfirmCancelDialog.diaConfirmNo.setOnClickListener { confirmCancelDialog.dismiss() }
                    confirmCancelDialog.show()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        menu.findItem(R.id.menu_edit_save).setOnMenuItemClickListener {
            card!!.front = binding.editCardFront.text.toString()
            card!!.back = binding.editCardBack.text.toString()
            card!!.notes = binding.editCardNotes.text.toString()
            val dbHelperUpdate = DB_Helper_Update(this)
            if (dbHelperUpdate.updateCard(card)) {
                finish()
            } else {
                Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show()
            }
            return@setOnMenuItemClickListener true
        }
        return true
    }
}
