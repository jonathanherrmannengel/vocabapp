package de.herrmann_engel.rbv.activities

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterTagLinkCard
import de.herrmann_engel.rbv.databinding.ActivityEditCardTagsBinding
import de.herrmann_engel.rbv.db.utils.DbHelperCreate
import de.herrmann_engel.rbv.db.utils.DbHelperDelete
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.db.utils.DbHelperUpdate
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.getCardId

/**
 * Manages the tags of the card passed as "card" extra: add tags by name (creating them
 * if needed, existing ones are suggested while typing), remove and edit them (see
 * [AdapterTagLinkCard]).
 */
class EditCardTags : RBVActivity() {
    private lateinit var binding: ActivityEditCardTagsBinding
    private lateinit var dbHelperGet: DbHelperGet
    private var cardId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditCardTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DbHelperGet(this)
        cardId = intent.getCardId() ?: run {
            finish()
            return
        }
        // Close if the card no longer exists (e.g. deleted in the meantime)
        val card = dbHelperGet.getSingleCard(cardId) ?: run {
            finish()
            return
        }
        resources.colorAt(R.array.pack_color_background, dbHelperGet.getSinglePack(card.packId)?.colors)
            ?.let { binding.root.setBackgroundColor(it) }
        // Add the tag when the keyboard's done/next/enter key is pressed
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
        // A chosen suggestion is added right away
        binding.editCardTagsAdd.setOnItemClickListener { _, _, _, _ ->
            createNewTag()
        }
        setUpEdgeToEdge(binding.editCardTagsLayout)
    }

    override fun onResume() {
        super.onResume()
        setRecView()
    }

    private fun createNewTag() {
        val tagName = binding.editCardTagsAdd.text.toString()
        if (tagName.isNotBlank()) {
            try {
                val dbHelperCreate = DbHelperCreate(this)
                dbHelperCreate.createTagLink(
                    tagName,
                    cardId
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, R.string.error_creating_duplicate, Toast.LENGTH_LONG).show()
            }
        }
        setRecView()
        binding.editCardTagsAdd.text.clear()
    }

    private fun setRecView() {
        val tagList = dbHelperGet.getCardTags(cardId)
        val adapter = AdapterTagLinkCard(
            tagList,
            onRemove = {
                DbHelperDelete(this).deleteTagLink(it.uid, cardId)
                setSuggestions()
            },
            onSave = { DbHelperUpdate(this).updateTag(it) }
        )
        binding.editCardTagsRec.adapter = adapter
        binding.editCardTagsRec.layoutManager = LinearLayoutManager(this)
        setSuggestions()
    }

    // All tags not on the card; the filter matches the start of the name or of a word in it,
    // ignoring case
    private fun setSuggestions() {
        val cardTagIds = dbHelperGet.getCardTags(cardId).map { it.uid }.toSet()
        val suggestions = dbHelperGet.getAllTags().filter { it.uid !in cardTagIds }.map { it.name }
        binding.editCardTagsAdd.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
        )
    }
}
