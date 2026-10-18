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
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.CardActions
import de.herrmann_engel.rbv.databinding.ActivityViewCardBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.media.MediaFileManager
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.ui.TagSpan
import de.herrmann_engel.rbv.utils.StringTools
import de.herrmann_engel.rbv.utils.formatDateTime
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import java.util.Locale

/**
 * Shows a single card with all its details (both sides, tags, notes, dates, progress, media)
 * and lets the user change its progress manually. Expects "collection", "pack" and "card" extras.
 */
class ViewCard : RBVActivity(), CardActions.Listener {
    private val mediaFileManager = MediaFileManager(
        this,
        onFolderSet = { setMediaButtons() },
        onMissingFileAction = { openCardMedia(it) }
    )
    private lateinit var binding: ActivityViewCardBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private lateinit var dbHelperUpdate: DB_Helper_Update
    private lateinit var card: DB_Card
    // Current progress of the card
    private var known = 0
    private var collectionNo = 0
    private var packNo = 0
    private var cardNo = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        dbHelperUpdate = DB_Helper_Update(this)

        collectionNo = intent.extras!!.getInt("collection")
        packNo = intent.extras!!.getInt("pack")
        cardNo = intent.extras!!.getInt("card")

        // Return to the card list, which then reloads this card as it may have changed
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ViewCard, ListCards::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("cardUpdated", cardNo)
                startActivity(intent)
            }
        })

        // Edge-to-edge: keep the content clear of the system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.viewCardLayout) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        enableEdgeToEdge()
    }

    // (Re)load the card on every resume, as it may have been edited
    override fun onResume() {
        super.onResume()
        val settings = AppSettings(this)
        val formatCards = settings.formatCards
        val increaseFontSize = settings.uiFontSizeBig
        // Close if the card no longer exists (e.g. deleted in the meantime)
        card = dbHelperGet.getSingleCard(cardNo) ?: run {
            finish()
            return
        }
        if (formatCards) {
            val formatString = StringTools()
            binding.cardFront.text = formatString.format(card.front)
            binding.cardBack.text = formatString.format(card.back)
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
                if (!tagColor.isNullOrBlank()) {
                    try {
                        color = tagColor.toColorInt()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                var tagText = it.name
                if (!it.emoji.isNullOrBlank()) {
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
        if (!notes.isNullOrBlank()) {
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
        // Each change of the progress counts as a repetition
        binding.cardMinus.setOnClickListener {
            known = 0.coerceAtLeast(--known)
            card.known = known
            card.lastRepetition = System.currentTimeMillis() / 1000L
            card.countRepetitions++
            dbHelperUpdate.updateCard(card)
            updateCardKnown()
        }
        binding.cardPlus.setOnClickListener {
            known++
            card.known = known
            card.lastRepetition = System.currentTimeMillis() / 1000L
            card.countRepetitions++
            dbHelperUpdate.updateCard(card)
            updateCardKnown()
        }
        updateColors()
        title = getString(R.string.card_details)
        setMediaButtons()
    }

    // Missing media file: open the card's media settings
    private fun openCardMedia(id: Int) {
        val intent = Intent(this, EditCardMedia::class.java)
        intent.putExtra("card", cardNo)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_card, menu)
        menu.findItem(R.id.menu_view_card_edit).setOnMenuItemClickListener {
            val intent = Intent(this, EditCard::class.java)
            intent.putExtra("card", cardNo)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_card_delete).setOnMenuItemClickListener {
            CardActions(this, this, mediaFileManager).delete(card)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_card_edit_media).setOnMenuItemClickListener {
            val intent = Intent(this, EditCardMedia::class.java)
            intent.putExtra("card", cardNo)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_card_edit_tags).setOnMenuItemClickListener {
            val intent = Intent(this, EditCardTags::class.java)
            intent.putExtra("card", cardNo)
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        menu.findItem(R.id.menu_view_card_print).setOnMenuItemClickListener {
            CardActions(this, this, mediaFileManager).print(card)
            return@setOnMenuItemClickListener true
        }
        // Moving is only offered when the card was opened from within its pack
        if (packNo >= 0) {
            menu.findItem(R.id.menu_view_card_move).setOnMenuItemClickListener {
                CardActions(this, this, mediaFileManager).move(card, collectionNo)
                return@setOnMenuItemClickListener true
            }
        } else {
            menu.findItem(R.id.menu_view_card_move).isVisible = false
        }
        return true
    }

    // Colors the screen like the card's pack
    private fun updateColors() {
        val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
        val colorsBackgroundLight =
            resources.obtainTypedArray(R.array.pack_color_background_light)
        val minimalLength = colorsBackground.length().coerceAtMost(colorsBackgroundLight.length())
        val packColors = dbHelperGet.getSinglePack(card.pack)?.colors
        if (packColors != null && packColors in 0..<minimalLength) {
            val colorBackground = colorsBackground.getColor(packColors, 0)
            val colorBackgroundLight = colorsBackgroundLight.getColor(packColors, 0)
            binding.root.setBackgroundColor(colorBackground)
            val cardKnownProgressDrawable = GradientDrawable()
            cardKnownProgressDrawable.setColor(colorBackgroundLight)
            cardKnownProgressDrawable.cornerRadius = 50f
            binding.cardKnownProgress.background = cardKnownProgressDrawable
        }
        colorsBackground.recycle()
        colorsBackgroundLight.recycle()
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
            card = dbHelperGet.getSingleCard(cardNo) ?: run {
                finish()
                return
            }
            packNo = card.pack
            updateColors()
            val intent = Intent(this, ListCards::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("collection", collectionNo)
            intent.putExtra("pack", packNo)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun deletedCards(cardIds: ArrayList<Int>) {
        val intent = Intent(this, ListCards::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("cardDeleted", cardNo)
        startActivity(intent)
    }

    // Shows buttons for the card's images and other media only if there are any
    private fun setMediaButtons() {
        val imageList =
            dbHelperGet.getCardImageMedia(cardNo) as ArrayList<DB_Media>
        if (imageList.isEmpty()) {
            binding.viewCardImages.visibility = View.GONE
        } else {
            binding.viewCardImages.visibility = View.VISIBLE
        }
        binding.viewCardImages.setOnClickListener {
            mediaFileManager.showImageListDialog(
                imageList,
                cardNo
            )
        }
        val mediaList = dbHelperGet.getCardMedia(cardNo) as ArrayList<DB_Media>
        if (mediaList.isEmpty()) {
            binding.viewCardMedia.visibility = View.GONE
        } else {
            binding.viewCardMedia.visibility = View.VISIBLE
        }
        binding.viewCardMedia.setOnClickListener {
            mediaFileManager.showMediaListDialog(
                mediaList,
                cardNo
            )
        }
    }
}
