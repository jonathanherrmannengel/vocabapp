package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.util.Linkify
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.CardActions
import de.herrmann_engel.rbv.cardlist.CardSource
import de.herrmann_engel.rbv.databinding.ActivityViewCardBinding
import de.herrmann_engel.rbv.db.DbCard
import de.herrmann_engel.rbv.db.utils.DbHelperGet
import de.herrmann_engel.rbv.db.utils.DbHelperUpdate
import de.herrmann_engel.rbv.media.MediaFileManager
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.ui.TagSpan
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.formatDateTime
import de.herrmann_engel.rbv.utils.getCardId
import de.herrmann_engel.rbv.utils.getCardSource
import de.herrmann_engel.rbv.utils.putCardId
import de.herrmann_engel.rbv.utils.putCardSource
import de.herrmann_engel.rbv.utils.putDeletedCardId
import de.herrmann_engel.rbv.utils.putUpdatedCardId
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import java.util.Locale

/**
 * Shows a single card with all its details (both sides, tags, notes, dates, progress, media)
 * and lets the user change its progress manually. Expects the "card" extra and the card list it
 * was opened from (see [CardSource]).
 */
class ViewCard : RBVActivity(), CardActions.Listener {
    private val mediaFileManager = MediaFileManager(
        this,
        onFolderSet = { setMediaButtons() },
        onMissingFileAction = { openCardMedia(it) }
    )
    private lateinit var binding: ActivityViewCardBinding
    private lateinit var dbHelperGet: DbHelperGet
    private lateinit var dbHelperUpdate: DbHelperUpdate
    private lateinit var card: DbCard
    // Current progress of the card
    private var known = 0
    // The card list the card was opened from
    private var cardSource: CardSource = CardSource.AllCards
    private var cardId = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DbHelperGet(this)
        dbHelperUpdate = DbHelperUpdate(this)

        intent.getCardSource()?.let { cardSource = it }
        cardId = intent.getCardId() ?: run {
            finish()
            return
        }

        // Return to the card list, which then reloads this card as it may have changed
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ViewCard, ListCards::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putUpdatedCardId(cardId)
                startActivity(intent)
            }
        })

        setUpEdgeToEdge(binding.viewCardLayout)
    }

    // (Re)load the card on every resume, as it may have been edited
    override fun onResume() {
        super.onResume()
        val settings = AppSettings(this)
        val formatCards = settings.formatCards
        val increaseFontSize = settings.uiFontSizeBig
        // Close if the card no longer exists (e.g. deleted in the meantime)
        card = dbHelperGet.getSingleCard(cardId) ?: run {
            finish()
            return
        }
        if (formatCards) {
            binding.cardFront.text = StringTools.format(card.front)
            binding.cardBack.text = StringTools.format(card.back)
        } else {
            binding.cardFront.text = card.front
            binding.cardBack.text = card.back
        }
        // Tags are drawn as colored pills
        val cardTags = dbHelperGet.getCardTags(card.uid)
        if (cardTags.isNotEmpty()) {
            binding.cardTags.visibility = View.VISIBLE
            val builder = SpannableStringBuilder()
            builder.append(getString(R.string.tags))
            builder.append(":")
            cardTags.forEach {
                var color = ContextCompat.getColor(
                    this,
                    R.color.tag_background
                )
                val tagColor = it.color
                if (tagColor.isNotBlank()) {
                    try {
                        color = tagColor.toColorInt()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                var tagText = it.name
                if (it.emoji.isNotBlank()) {
                    tagText = it.emoji + " " + tagText
                }
                val spannableString = SpannableString(tagText)
                spannableString.setSpan(
                    TagSpan(
                        this,
                        color
                    ),
                    0,
                    spannableString.length,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
                builder.append(spannableString)
            }
            binding.cardTags.text = builder
        } else {
            binding.cardTags.visibility = View.GONE
        }
        val formatCardNotes = settings.formatCardNotes
        val notes = card.notes
        if (notes.isNotBlank()) {
            binding.cardNotesContainer.visibility = View.VISIBLE
            // Notes are rendered as Markdown; links are clickable in both modes
            if (formatCardNotes) {
                binding.cardNotesDivider.visibility = View.VISIBLE
                val markwon = Markwon.builder(this)
                    .usePlugin(
                        LinkifyPlugin.create(
                            Linkify.WEB_URLS
                        )
                    )
                    .build()
                binding.cardNotes.movementMethod = BetterLinkMovementMethod.getInstance()
                binding.cardNotes.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                markwon.setMarkdown(binding.cardNotes, notes)
            } else {
                binding.cardNotes.autoLinkMask = Linkify.WEB_URLS
                binding.cardNotes.text = notes
            }
        } else {
            binding.cardNotesContainer.visibility = View.GONE
        }
        if (increaseFontSize) {
            binding.cardFront.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.card_front_size_big)
            )
            binding.cardBack.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.card_back_size_big)
            )
            binding.cardNotes.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.card_notes_size_big)
            )
        }
        binding.cardDate.text = String.format(
            Locale.ROOT,
            getString(R.string.creation_date),
            formatDateTime(this, card.date)
        )
        known = card.known
        updateCardKnown()
        binding.cardMinus.setOnClickListener {
            known = 0.coerceAtLeast(known - 1)
            dbHelperUpdate.updateCardProgress(card, known)
            updateCardKnown()
        }
        binding.cardPlus.setOnClickListener {
            known++
            dbHelperUpdate.updateCardProgress(card, known)
            updateCardKnown()
        }
        updateColors()
        title = getString(R.string.card_details)
        setMediaButtons()
    }

    // Missing media file: open the card's media settings
    private fun openCardMedia(cardId: Int) {
        val intent = Intent(this, EditCardMedia::class.java)
        intent.putCardId(cardId)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_card, menu)
        menu.findItem(R.id.menu_view_card_edit).setOnMenuItemClickListener {
            val intent = Intent(this, EditCard::class.java)
            intent.putCardId(cardId)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_card_delete).setOnMenuItemClickListener {
            CardActions(this, this, mediaFileManager).delete(card)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_card_edit_media).setOnMenuItemClickListener {
            val intent = Intent(this, EditCardMedia::class.java)
            intent.putCardId(cardId)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_card_edit_tags).setOnMenuItemClickListener {
            val intent = Intent(this, EditCardTags::class.java)
            intent.putCardId(cardId)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_card_print).setOnMenuItemClickListener {
            CardActions(this, this, mediaFileManager).print(card)
            return@setOnMenuItemClickListener true
        }
        // Moving is only offered when the card was opened from within its pack
        val packSource = cardSource as? CardSource.Pack
        if (packSource != null) {
            menu.findItem(R.id.menu_view_card_move).setOnMenuItemClickListener {
                CardActions(this, this, mediaFileManager).move(card, packSource.listCollectionId)
                return@setOnMenuItemClickListener true
            }
        } else {
            menu.findItem(R.id.menu_view_card_move).isVisible = false
        }
        return true
    }

    // Colors the screen like the card's pack
    private fun updateColors() {
        val packColors = dbHelperGet.getSinglePack(card.packId)?.colors
        val colorBackground = resources.colorAt(R.array.pack_color_background, packColors)
        val colorBackgroundLight =
            resources.colorAt(R.array.pack_color_background_light, packColors)
        if (colorBackground != null && colorBackgroundLight != null) {
            binding.root.setBackgroundColor(colorBackground)
            val cardKnownProgressDrawable = GradientDrawable()
            cardKnownProgressDrawable.setColor(colorBackgroundLight)
            cardKnownProgressDrawable.cornerRadius = 50f
            binding.cardKnownProgress.background = cardKnownProgressDrawable
        }
    }

    private fun updateCardLastRepetition() {
        binding.cardDateLastRepetition.text =
            if (card.lastRepetition == 0L) {
                getString(R.string.last_repetition_none)
            } else {
                String.format(
                    Locale.ROOT,
                    getString(R.string.last_repetition_date),
                    formatDateTime(this, card.lastRepetition)
                )
            }
    }

    // Shows the current progress; the minus button is grayed out at 0
    private fun updateCardKnown() {
        binding.cardKnown.text = known.toString()
        binding.cardKnown.contentDescription =
            String.format(Locale.ROOT, "%s: %d", resources.getString(R.string.card_known), known)
        binding.cardMinus.setColorFilter(Color.argb(255, 255, 255, 255))
        binding.cardMinus.setColorFilter(
            ContextCompat.getColor(this, if (known > 0) R.color.dark_red else R.color.dark_grey),
            PorterDuff.Mode.MULTIPLY
        )
        updateCardLastRepetition()
    }

    // Show the card list of the new pack
    override fun movedCards(cardIds: ArrayList<Int>) {
        try {
            // Close if the card no longer exists (e.g. deleted in the meantime)
            card = dbHelperGet.getSingleCard(cardId) ?: run {
                finish()
                return
            }
            val listCollectionId = (cardSource as? CardSource.Pack)?.listCollectionId
            cardSource = CardSource.Pack(card.packId, listCollectionId)
            updateColors()
            val intent = Intent(this, ListCards::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putCardSource(cardSource)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun deletedCards(cardIds: ArrayList<Int>) {
        val intent = Intent(this, ListCards::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putDeletedCardId(cardId)
        startActivity(intent)
    }

    // Shows buttons for the card's images and other media only if there are any
    private fun setMediaButtons() {
        val imageList =
            dbHelperGet.getCardImageMedia(cardId)
        if (imageList.isEmpty()) {
            binding.viewCardImages.visibility = View.GONE
        } else {
            binding.viewCardImages.visibility = View.VISIBLE
        }
        binding.viewCardImages.setOnClickListener {
            mediaFileManager.showImageListDialog(
                imageList,
                cardId
            )
        }
        val mediaList = dbHelperGet.getCardMedia(cardId)
        if (mediaList.isEmpty()) {
            binding.viewCardMedia.visibility = View.GONE
        } else {
            binding.viewCardMedia.visibility = View.VISIBLE
        }
        binding.viewCardMedia.setOnClickListener {
            mediaFileManager.showMediaListDialog(
                mediaList,
                cardId
            )
        }
    }
}
