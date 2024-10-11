package de.herrmann_engel.rbv.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
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
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.CardActions
import de.herrmann_engel.rbv.databinding.ActivityViewCardBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Media_Link_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.ui.TagSpan
import de.herrmann_engel.rbv.utils.StringTools
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale

class ViewCard : CardActionsActivity() {
    private lateinit var binding: ActivityViewCardBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private lateinit var dbHelperUpdate: DB_Helper_Update
    private var card: DB_Card? = null
    private var known = 0
    private var collectionNo = 0
    private var packNo = 0
    private var cardNo = 0
    private var formatCardNotes = false
    private var imageList: ArrayList<DB_Media_Link_Card>? = null
    private var mediaList: ArrayList<DB_Media_Link_Card>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        dbHelperUpdate = DB_Helper_Update(this)

        intent.extras?.let {
            collectionNo = it.getInt("collection")
            packNo = it.getInt("pack")
            cardNo = it.getInt("card")
        } ?: run {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ViewCard, ListCards::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("cardUpdated", cardNo)
                startActivity(intent)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        val formatCards = settings.getBoolean("format_cards", false)
        val increaseFontSize = settings.getBoolean("ui_font_size", false)
        try {
            card = dbHelperGet.getSingleCard(cardNo)
            val cardFront: String
            if (formatCards) {
                val formatString = StringTools()
                val cardFrontSpannable = formatString.format(card!!.front)
                cardFront = cardFrontSpannable.toString()
                binding.cardFront.text = cardFrontSpannable
                binding.cardBack.text = formatString.format(card!!.back)
            } else {
                cardFront = card!!.front
                binding.cardFront.text = cardFront
                binding.cardBack.text = card!!.back
            }
            val cardTags = dbHelperGet.getCardTags(card!!.uid)
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
                    if (!it.color.isNullOrBlank()) {
                        try {
                            color = Color.parseColor(it.color)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    var tagText = it.name
                    if (!it.emoji.isNullOrBlank()) {
                        tagText = it.emoji + " " + tagText
                    }
                    val spannableString = SpannableString(tagText)
                    builder.append(spannableString)
                    builder.setSpan(
                        TagSpan(
                            this,
                            color
                        ),
                        builder.length - spannableString.length,
                        builder.length,
                        SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                binding.cardTags.text = builder
            } else {
                binding.cardTags.visibility = View.GONE
            }
            formatCardNotes = settings.getBoolean("format_card_notes", false)
            if (card!!.notes != null && card!!.notes.isNotEmpty()) {
                binding.cardNotes.visibility = View.VISIBLE
                if (formatCardNotes) {
                    val markwon = Markwon.builder(this)
                        .usePlugin(
                            LinkifyPlugin.create(
                                Linkify.WEB_URLS
                            )
                        )
                        .build()
                    binding.cardNotes.movementMethod = BetterLinkMovementMethod.getInstance()
                    binding.cardNotes.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    markwon.setMarkdown(binding.cardNotes, card!!.notes)
                } else {
                    binding.cardNotes.autoLinkMask = Linkify.WEB_URLS
                    binding.cardNotes.text = card!!.notes
                }
            } else {
                binding.cardNotes.visibility = View.GONE
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val instant = Instant.ofEpochSecond(card!!.date)
                val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                    .withLocale(Locale.ROOT)
                    .withZone(ZoneId.systemDefault())
                binding.cardDate.text = dateTimeFormatter.format(instant)
            } else {
                binding.cardDate.text = Date(card!!.date * 1000).toString()
            }
            known = card!!.known
            updateCardKnown()
            binding.cardMinus.setOnClickListener {
                known = 0.coerceAtLeast(--known)
                card!!.known = known
                card!!.lastRepetition = System.currentTimeMillis() / 1000L
                dbHelperUpdate.updateCard(card)
                updateCardKnown()
            }
            binding.cardPlus.setOnClickListener {
                known++
                card!!.known = known
                card!!.lastRepetition = System.currentTimeMillis() / 1000L
                dbHelperUpdate.updateCard(card)
                updateCardKnown()
            }
            updateColors()
            title = cardFront
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
        setMediaButtons()
    }

    override fun notifyFolderSet() {
        setMediaButtons()
    }

    override fun notifyMissingAction(id: Int) {
        val intent = Intent(this, EditCardMedia::class.java)
        intent.putExtra("card", cardNo)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_view_card, menu)
        if (collectionNo == 0 || packNo == 0 || cardNo == 0) {
            menu.findItem(R.id.menu_view_card_edit).isVisible = false
            menu.findItem(R.id.menu_view_card_delete).isVisible = false
            menu.findItem(R.id.menu_view_card_edit_media).isVisible = false
            menu.findItem(R.id.menu_view_card_print).isVisible = false
            menu.findItem(R.id.menu_view_card_move).isVisible = false
        } else {
            menu.findItem(R.id.menu_view_card_edit).setOnMenuItemClickListener {
                val intent = Intent(this, EditCard::class.java)
                intent.putExtra("card", cardNo)
                startActivity(intent)
                return@setOnMenuItemClickListener true
            }
            menu.findItem(R.id.menu_view_card_delete).setOnMenuItemClickListener {
                CardActions(this).delete(card!!)
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
                CardActions(this).print(card!!)
                return@setOnMenuItemClickListener true
            }
            if (packNo < 0) {
                menu.findItem(R.id.menu_view_card_move).isVisible = false
            } else {
                menu.findItem(R.id.menu_view_card_move).setOnMenuItemClickListener {
                    CardActions(this).move(card!!, collectionNo)
                    return@setOnMenuItemClickListener true
                }
            }
        }
        return true
    }

    private fun updateColors() {
        try {
            val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
            val colorsBackground = resources.obtainTypedArray(R.array.pack_color_background)
            val colorsBackgroundLight =
                resources.obtainTypedArray(R.array.pack_color_background_light)
            val packColors = dbHelperGet.getSinglePack(card!!.pack).colors
            if (packColors >= 0 && packColors < colorsStatusBar.length() && packColors < colorsBackground.length() && packColors < colorsBackgroundLight.length()) {
                val colorStatusBar = colorsStatusBar.getColor(packColors, 0)
                val colorBackground = colorsBackground.getColor(packColors, 0)
                val colorBackgroundLight = colorsBackgroundLight.getColor(packColors, 0)
                supportActionBar?.setBackgroundDrawable(ColorDrawable(colorStatusBar))
                window.statusBarColor = colorStatusBar
                binding.root.setBackgroundColor(colorBackground)
                binding.cardKnownProgress.setBackgroundColor(colorBackgroundLight)
            }
            colorsStatusBar.recycle()
            colorsBackground.recycle()
            colorsBackgroundLight.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCardKnown() {
        binding.cardKnown.text = known.toString()
        binding.cardKnown.contentDescription =
            String.format(Locale.ROOT, "%s: %d", resources.getString(R.string.card_known), known)
        binding.cardMinus.setColorFilter(Color.argb(255, 255, 255, 255))
        binding.cardMinus.setColorFilter(
            ContextCompat.getColor(this, if (known > 0) R.color.dark_red else R.color.dark_grey),
            PorterDuff.Mode.MULTIPLY
        )

    }

    override fun movedCards(cardIds: ArrayList<Int>) {
        try {
            card = dbHelperGet.getSingleCard(cardNo)
            packNo = card!!.pack
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

    private fun setMediaButtons() {
        imageList = dbHelperGet.getImageMediaLinksByCard(cardNo) as ArrayList<DB_Media_Link_Card>
        if (imageList!!.isEmpty()) {
            binding.viewCardImages.visibility = View.GONE
        } else {
            binding.viewCardImages.visibility = View.VISIBLE
        }
        binding.viewCardImages.setOnClickListener {
            showImageListDialog(
                imageList!!
            )
        }
        mediaList = dbHelperGet.getAllMediaLinksByCard(cardNo) as ArrayList<DB_Media_Link_Card>
        if (mediaList!!.isEmpty()) {
            binding.viewCardMedia.visibility = View.GONE
        } else {
            binding.viewCardMedia.visibility = View.VISIBLE
        }
        binding.viewCardMedia.setOnClickListener {
            showMediaListDialog(
                mediaList!!
            )
        }
    }
}
