package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityNewCardBinding
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class NewCard : AppCompatActivity() {
    private lateinit var binding: ActivityNewCardBinding
    private var packNo = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dbHelperGet = DB_Helper_Get(this)
        packNo = intent.extras!!.getInt("pack")
        val colors = resources.obtainTypedArray(R.array.pack_color_main)
        val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
        val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
        val minimalLength = colors.length().coerceAtMost(colorsStatusBar.length())
            .coerceAtMost(colorsBackground.length())
        val packColors = dbHelperGet.getSinglePack(packNo).colors
        if (packColors in 0..<minimalLength) {
            val color = colors.getColor(packColors, 0)
            val colorStatusBar = colorsStatusBar.getColor(packColors, 0)
            val colorBackground = colorsBackground.getColor(packColors, 0)
            supportActionBar?.setBackgroundDrawable(colorStatusBar.toDrawable())
            binding.newCardFrontLayout.boxStrokeColor = color
            binding.newCardFrontLayout.hintTextColor =
                ColorStateList.valueOf(color)
            binding.newCardBackLayout.boxStrokeColor = color
            binding.newCardBackLayout.hintTextColor =
                ColorStateList.valueOf(color)
            binding.newCardNotesLayout.boxStrokeColor = color
            binding.newCardNotesLayout.hintTextColor =
                ColorStateList.valueOf(color)
            binding.root.setBackgroundColor(colorBackground)
        }
        colors.recycle()
        colorsStatusBar.recycle()
        colorsBackground.recycle()
        binding.newCardNotesLayout.hint =
            String.format(getString(R.string.optional), getString(R.string.card_notes))

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val front = binding.newCardFront.text.toString()
                val back = binding.newCardBack.text.toString()
                val notes = binding.newCardNotes.text.toString()
                if (front.isBlank() && back.isBlank() && notes.isBlank()) {
                    finish()
                } else {
                    val confirmCancelDialog = Dialog(this@NewCard, R.style.dia_view)
                    val bindingConfirmCancelDialog = DiaConfirmBinding.inflate(layoutInflater)
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
        menuInflater.inflate(R.menu.menu_add, menu)
        menu.findItem(R.id.menu_add_insert).setOnMenuItemClickListener {
            val front = binding.newCardFront.text.toString()
            val back = binding.newCardBack.text.toString()
            val notes = binding.newCardNotes.text.toString()
            try {
                val dbHelperCreate = DB_Helper_Create(this)
                val cardNo = dbHelperCreate.createCard(front, back, notes, packNo)
                val intent = Intent(this, ListCards::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("cardAdded", cardNo.toInt())
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
