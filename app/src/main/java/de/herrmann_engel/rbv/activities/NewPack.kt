package de.herrmann_engel.rbv.activities

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.databinding.ActivityNewCollectionOrPackBinding
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class NewPack : AppCompatActivity() {
    private lateinit var binding: ActivityNewCollectionOrPackBinding
    private var collectionNo = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCollectionOrPackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dbHelperGet = DB_Helper_Get(this)
        collectionNo = intent.extras!!.getInt("collection")
        val colors = resources.obtainTypedArray(R.array.pack_color_main)
        val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
        val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
        val minimalLength = colors.length().coerceAtMost(colorsStatusBar.length())
            .coerceAtMost(colorsBackground.length())
        val collectionColors = dbHelperGet.getSingleCollection(collectionNo).colors
        if (collectionColors in 0..<minimalLength) {
            val color = colors.getColor(collectionColors, 0)
            val colorStatusBar = colorsStatusBar.getColor(collectionColors, 0)
            val colorBackground = colorsBackground.getColor(collectionColors, 0)
            supportActionBar?.setBackgroundDrawable(colorStatusBar.toDrawable())
            binding.newCollectionOrPackNameLayout.boxStrokeColor = color
            binding.newCollectionOrPackNameLayout.hintTextColor =
                ColorStateList.valueOf(color)
            binding.newCollectionOrPackDescLayout.boxStrokeColor = color
            binding.newCollectionOrPackDescLayout.hintTextColor =
                ColorStateList.valueOf(color)
            binding.root.setBackgroundColor(colorBackground)
        }
        colors.recycle()
        colorsStatusBar.recycle()
        colorsBackground.recycle()
        binding.newCollectionOrPackNameLayout.hint = String.format(
            getString(R.string.collection_or_pack_name_format),
            getString(R.string.pack_name), getString(R.string.collection_or_pack_name)
        )
        binding.newCollectionOrPackDescLayout.hint =
            String.format(getString(R.string.optional), getString(R.string.collection_or_pack_desc))

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val name = binding.newCollectionOrPackName.text.toString()
                val desc = binding.newCollectionOrPackDesc.text.toString()
                if (name.isBlank() && desc.isBlank()) {
                    finish()
                } else {
                    val confirmCancelDialog = Dialog(this@NewPack, R.style.dia_view)
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
        menuInflater.inflate(R.menu.menu_add, menu)
        menu.findItem(R.id.menu_add_insert).setOnMenuItemClickListener {
            val name = binding.newCollectionOrPackName.text.toString()
            val desc = binding.newCollectionOrPackDesc.text.toString()
            try {
                val dbHelperCreate = DB_Helper_Create(this)
                dbHelperCreate.createPack(name, desc, collectionNo)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, R.string.error_values, Toast.LENGTH_SHORT).show()
            }
            return@setOnMenuItemClickListener true
        }
        return true
    }
}
