package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityEditCardBinding
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update

class EditCard : AppCompatActivity() {
    private lateinit var binding: ActivityEditCardBinding
    private lateinit var card: DB_Card
    private var backToList = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dbHelperGet = DB_Helper_Get(this)
        val cardNo = intent.extras!!.getInt("card")
        backToList = intent.extras!!.getBoolean("backToList")
        card = dbHelperGet.getSingleCard(cardNo)
        binding.editCardFront.setText(card.front)
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
        val colors = resources.obtainTypedArray(R.array.pack_color_main)
        val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
        val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
        val minimalLength = colors.length().coerceAtMost(colorsStatusBar.length())
            .coerceAtMost(colorsBackground.length())
        val packColors = dbHelperGet.getSinglePack(card.pack).colors
        if (packColors in 0..<minimalLength) {
            val color = colors.getColor(packColors, 0)
            val colorStatusBar = colorsStatusBar.getColor(packColors, 0)
            val colorBackground = colorsBackground.getColor(packColors, 0)
            supportActionBar?.setBackgroundDrawable(colorStatusBar.toDrawable())
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
        colors.recycle()
        colorsStatusBar.recycle()
        colorsBackground.recycle()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val front = binding.editCardFront.text.toString()
                val back = binding.editCardBack.text.toString()
                val notes = binding.editCardNotes.text.toString()
                if (card.front == front && card.back == back && card.notes == notes) {
                    close()
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
                    bindingConfirmCancelDialog.diaConfirmYes.setOnClickListener { close() }
                    bindingConfirmCancelDialog.diaConfirmNo.setOnClickListener { confirmCancelDialog.dismiss() }
                    confirmCancelDialog.show()
                }
            }
        })
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

    private fun close() {
        if (backToList) {
            val intent = Intent(this, ListCards::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("cardUpdated", card.uid)
            startActivity(intent)
        } else {
            finish()
        }
    }
}
