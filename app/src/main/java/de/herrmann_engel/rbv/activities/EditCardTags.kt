package de.herrmann_engel.rbv.activities

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterTagLinkCard
import de.herrmann_engel.rbv.databinding.ActivityEditCardTagsBinding
import de.herrmann_engel.rbv.db.utils.DB_Helper_Create
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get

class EditCardTags : AppCompatActivity() {
    private lateinit var binding: ActivityEditCardTagsBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private var cardNo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCardTagsBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        intent.extras?.getInt("card")?.also { cardNoExtra ->
            try {
                cardNo = cardNoExtra
                val card = dbHelperGet.getSingleCard(cardNo)
                val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
                val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
                val packColors = dbHelperGet.getSinglePack(card.pack).colors
                if (packColors >= 0 && packColors < colorsStatusBar.length() && packColors < colorsBackground.length()) {
                    val colorStatusBar = colorsStatusBar.getColor(packColors, 0)
                    val colorBackground = colorsBackground.getColor(packColors, 0)
                    supportActionBar?.setBackgroundDrawable(ColorDrawable(colorStatusBar))
                    window.statusBarColor = colorStatusBar
                    binding.root.setBackgroundColor(colorBackground)
                }
                colorsStatusBar.recycle()
                colorsBackground.recycle()
                binding.editCardTagsAdd.setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_NULL) {
                        createNewTag()
                        return@setOnEditorActionListener true
                    }
                    return@setOnEditorActionListener false
                }
                binding.editCardTagsGo.setOnClickListener {
                    createNewTag()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        setRecView()
    }

    private fun createNewTag() {
        val tagName =
            binding.editCardTagsAdd.text.toString()
        if (tagName.isNotBlank()) {
            val dbHelperCreate = DB_Helper_Create(this)
            try {
                dbHelperCreate.createTagLink(
                    tagName,
                    cardNo
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        setRecView()
        binding.editCardTagsAdd.text?.clear()
    }

    private fun setRecView() {
        val tagList = dbHelperGet.getCardTags(cardNo)
        val adapter = AdapterTagLinkCard(tagList, cardNo)
        binding.editCardTagsRec.adapter = adapter
        binding.editCardTagsRec.layoutManager = LinearLayoutManager(this)
    }
}
