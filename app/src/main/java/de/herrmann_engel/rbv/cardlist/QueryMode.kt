package de.herrmann_engel.rbv.cardlist

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.activities.EditCard
import de.herrmann_engel.rbv.databinding.DiaInfoBinding
import de.herrmann_engel.rbv.databinding.DiaQueryBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.db.DB_Media
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.media.MediaFileManager
import de.herrmann_engel.rbv.ui.SwipeEvents
import de.herrmann_engel.rbv.utils.StringTools
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import kotlin.math.abs

/**
 * Query mode of the card list: a dialog asking the cards one after another. First only one side
 * of a card is shown; after revealing the other side, the user rates the card as known or not
 * known, which updates its progress.
 *
 * Swipe gestures: left/right go to the next/previous card, up/down (only after revealing) rate
 * the card as known/not known. While swiping, a colored bar grows from the edge; a completed
 * swipe fills the screen with it shortly before the action is executed.
 *
 * The dialog is created once and reused.
 *
 * @param onCardChanged called with the position of a card whose progress has changed
 * @param onClosed called with the position of the last shown card when the dialog is closed
 * (-1 or the size of the list if the user went past the first or last card)
 */
class QueryMode(
    private val activity: Activity,
    private val mediaFileManager: MediaFileManager,
    private val onCardChanged: (position: Int) -> Unit,
    private val onClosed: (lastPosition: Int) -> Unit
) {
    private val dialog = Dialog(activity, R.style.dia_view)
    private val binding = DiaQueryBinding.inflate(activity.layoutInflater)
    private val settings =
        activity.getSharedPreferences(Globals.SETTINGS_NAME, Context.MODE_PRIVATE)
    private val dbHelperGet = DB_Helper_Get(activity)
    private val dbHelperUpdate = DB_Helper_Update(activity)

    // The colored edges of the dialog hint at the available swipe directions
    private val rootBackground = binding.root.background as LayerDrawable
    private val rootBackgroundLeft =
        rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_left) as GradientDrawable
    private val rootBackgroundTop =
        rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_top) as GradientDrawable
    private val rootBackgroundBottom =
        rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_bottom) as GradientDrawable

    private var cards: List<DB_Card_With_Meta> = emptyList()
    // Position of the current card
    private var position = 0
    // Show the back side first
    private var reverse = false

    val isShowing: Boolean
        get() = dialog.isShowing

    // Id of the current card, null if there is none
    val currentCardId: Int?
        get() = cards.getOrNull(position)?.card?.uid

    init {
        dialog.setContentView(binding.root)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.setOnDismissListener { onClosed(position) }
        setTouchListener()
        binding.queryPlus.setOnClickListener { plusAction(cards[position].card) }
        binding.queryMinus.setOnClickListener { minusAction(cards[position].card) }
        binding.querySkip.setOnClickListener { nextAction() }
        binding.queryBack.setOnClickListener { previousAction() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener() {
        binding.root.setOnTouchListener(QuerySwipeEvents())
    }

    /**
     * Starts the query mode with the card at startPosition.
     *
     * @param reverse show the back side first
     */
    fun start(cards: List<DB_Card_With_Meta>, startPosition: Int, reverse: Boolean) {
        this.cards = cards
        this.reverse = reverse
        position = startPosition
        showCard(false)
        dialog.show()
    }

    /**
     * Closes the dialog without calling onClosed, e.g. because the activity is destroyed.
     */
    fun release() {
        dialog.setOnDismissListener(null)
        dialog.dismiss()
    }

    /**
     * Updates the current card (e.g. after returning from editing it), keeping its state such as
     * the revealed side.
     */
    fun update(cards: List<DB_Card_With_Meta>) {
        this.cards = cards
        if (position !in cards.indices) {
            dialog.dismiss()
            return
        }
        showCard(true)
    }

    // Goes to the next/previous card; closes the dialog at the end of the list
    private fun nextAction() {
        position++
        if (position >= cards.size) {
            dialog.dismiss()
        } else {
            showCard(false)
        }
    }

    private fun previousAction() {
        position--
        if (position < 0) {
            dialog.dismiss()
        } else {
            showCard(false)
        }
    }

    // Saves the new progress as a repetition of the card
    private fun cardKnownChanged(card: DB_Card, known: Int) {
        card.known = known
        card.lastRepetition = System.currentTimeMillis() / 1000L
        card.countRepetitions++
        dbHelperUpdate.updateCard(card)
        onCardChanged(position)
    }

    private fun plusAction(card: DB_Card) {
        cardKnownChanged(card, card.known + 1)
        nextAction()
    }

    // Card not known: decrease the progress by 1 or reset it to 0, depending on the settings
    private fun minusAction(card: DB_Card) {
        val known = if (settings.getBoolean("query_mode_reset_progress", false)) {
            0
        } else {
            0.coerceAtLeast(card.known - 1)
        }
        cardKnownChanged(card, known)
        nextAction()
    }

    /**
     * Shows the card at the current position.
     *
     * @param onlyUpdate only update texts and actions, keep the current state (e.g. revealed side)
     */
    private fun showCard(onlyUpdate: Boolean) {
        val cardWithMeta = cards[position]
        val card = cardWithMeta.card

        val front: SpannableString
        val back: SpannableString
        val formatString = StringTools()
        if (settings.getBoolean("format_cards", false)) {
            front =
                if (reverse) {
                    formatString.format(card.back)
                } else {
                    formatString.format(card.front)
                }
            back =
                if (reverse) {
                    formatString.format(card.front)
                } else {
                    formatString.format(card.back)
                }
        } else {
            val frontString = if (reverse) card.back else card.front
            val backString = if (reverse) card.front else card.back
            front = SpannableString(frontString)
            back = SpannableString(backString)
        }
        binding.queryShow.text = front
        binding.queryHide.text = back
        if (settings.getBoolean("ui_font_size", false)) {
            binding.queryShow.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                activity.resources.getDimension(R.dimen.card_front_size_big)
            )
            binding.queryHide.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                activity.resources.getDimension(R.dimen.card_front_size_big)
            )
        }

        if (card.notes.isNullOrBlank()) {
            binding.queryButtonNotes.visibility = View.GONE
        } else {
            binding.queryButtonNotes.visibility = View.VISIBLE
            binding.queryButtonNotes.setOnClickListener {
                showNotes(card.notes)
            }
        }

        rootBackgroundLeft.alpha = if (position > 0) 255 else 0

        // Reset the dialog for a new card: title, colors, media buttons, hidden back side
        if (!onlyUpdate) {
            dialog.setTitle(
                String.format(
                    activity.getString(R.string.query_mode_title),
                    activity.getString(R.string.query_mode),
                    position + 1,
                    cards.size
                )
            )

            val colorsBackgroundQuery =
                activity.resources.obtainTypedArray(R.array.pack_color_background_query)
            val colorsBackgroundHighlight = activity.resources
                .obtainTypedArray(R.array.pack_color_background_highlight)
            val minimalLength =
                colorsBackgroundQuery.length().coerceAtMost(colorsBackgroundHighlight.length())
            val packColors = cardWithMeta.packColor
            if (packColors in 0..<minimalLength) {
                val colorBackgroundQuery = colorsBackgroundQuery.getColor(packColors, 0)
                val colorBackgroundHighlight =
                    colorsBackgroundHighlight.getColor(packColors, 0)
                val rootBackgroundMain =
                    rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_main) as GradientDrawable
                rootBackgroundMain.setColor(colorBackgroundQuery)
                binding.queryHide.setBackgroundColor(colorBackgroundHighlight)
            }
            colorsBackgroundQuery.recycle()
            colorsBackgroundHighlight.recycle()

            val imageList =
                dbHelperGet.getCardImageMedia(card.uid) as ArrayList<DB_Media>
            if (imageList.isEmpty()) {
                binding.queryButtonMediaImage.visibility = View.GONE
            } else {
                binding.queryButtonMediaImage.visibility = View.VISIBLE
                binding.queryButtonMediaImage.setOnClickListener {
                    mediaFileManager.showImageListDialog(
                        imageList,
                        card.uid
                    )
                }
            }
            val mediaList =
                dbHelperGet.getCardMedia(card.uid) as ArrayList<DB_Media>
            if (mediaList.isEmpty()) {
                binding.queryButtonMediaOther.visibility = View.GONE
            } else {
                binding.queryButtonMediaOther.visibility = View.VISIBLE
                binding.queryButtonMediaOther.setOnClickListener {
                    mediaFileManager.showMediaListDialog(
                        mediaList,
                        card.uid
                    )
                }
            }

            rootBackgroundTop.alpha = 0
            rootBackgroundBottom.alpha = 0

            binding.queryPlus.visibility = View.GONE
            binding.queryMinus.visibility = View.GONE
            binding.queryBack.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
            binding.queryHide.visibility = View.GONE
            binding.queryButtonEdit.visibility = View.GONE
            binding.queryButtonHide.visibility = View.VISIBLE
            // Reveal the other side and enable rating the card
            binding.queryButtonHide.setOnClickListener {
                binding.queryButtonHide.visibility = View.GONE
                binding.queryHide.visibility = View.VISIBLE
                binding.queryPlus.visibility = View.VISIBLE
                binding.queryMinus.visibility = View.VISIBLE
                rootBackgroundTop.alpha = 255
                rootBackgroundBottom.alpha = 255
                binding.queryButtonEdit.visibility = View.VISIBLE
                binding.queryButtonEdit.setOnClickListener {
                    val intent = Intent(activity, EditCard::class.java)
                    intent.putExtra("card", card.uid)
                    intent.putExtra("backToList", true)
                    activity.startActivity(intent)
                }
            }
        }
    }

    // Shows the notes of the card, as Markdown if enabled in the settings
    private fun showNotes(notes: String) {
        val infoDialog = Dialog(activity, R.style.dia_view)
        val bindingInfoDialog = DiaInfoBinding.inflate(
            activity.layoutInflater
        )
        infoDialog.setContentView(bindingInfoDialog.root)
        infoDialog.setTitle(activity.getString(R.string.query_notes_title))
        infoDialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        bindingInfoDialog.diaInfoText.setTextIsSelectable(true)
        if (settings.getBoolean("format_card_notes", false)) {
            val markwon = Markwon.builder(activity)
                .usePlugin(
                    LinkifyPlugin.create(
                        Linkify.WEB_URLS
                    )
                )
                .build()
            bindingInfoDialog.diaInfoText.movementMethod =
                BetterLinkMovementMethod.getInstance()
            bindingInfoDialog.diaInfoText.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            markwon.setMarkdown(bindingInfoDialog.diaInfoText, notes)
        } else {
            bindingInfoDialog.diaInfoText.autoLinkMask = Linkify.WEB_URLS
            bindingInfoDialog.diaInfoText.text = notes
        }
        infoDialog.show()
    }

    // Swipe gestures of the dialog (see the class description)
    private inner class QuerySwipeEvents : SwipeEvents() {
        // Blocks further gestures while an animation is running
        private var allowTouchEvent = true

        override fun onMoveX(distance: Float) {
            if (allowTouchEvent) {
                if (distance < 0) {
                    binding.querySwipeNext.layoutParams.width = (2 * abs(distance)).toInt()
                    binding.querySwipeNext.requestLayout()
                } else if (position > 0) {
                    binding.querySwipePrevious.layoutParams.width = (2 * abs(distance)).toInt()
                    binding.querySwipePrevious.requestLayout()
                }
            }
        }

        override fun onMoveY(distance: Float) {
            if (allowTouchEvent && binding.queryHide.isVisible) {
                if (distance < 0) {
                    binding.querySwipePlus.layoutParams.height = (2 * abs(distance)).toInt()
                    binding.querySwipePlus.requestLayout()
                } else {
                    binding.querySwipeMinus.layoutParams.height = (2 * abs(distance)).toInt()
                    binding.querySwipeMinus.requestLayout()
                }
            }
        }

        override fun onMoveCancel() {
            super.onMoveCancel()
            if (allowTouchEvent) {
                binding.querySwipeNext.layoutParams.width = 0
                binding.querySwipeNext.requestLayout()
                binding.querySwipePrevious.layoutParams.width = 0
                binding.querySwipePrevious.requestLayout()
                binding.querySwipeMinus.layoutParams.height = 0
                binding.querySwipeMinus.requestLayout()
                binding.querySwipePlus.layoutParams.height = 0
                binding.querySwipePlus.requestLayout()
            }
        }

        override fun onSwipeLeft() {
            if (allowTouchEvent) {
                completeSwipe(binding.querySwipeNext, true) { nextAction() }
            } else {
                onMoveCancel()
            }
        }

        override fun onSwipeRight() {
            if (allowTouchEvent && position > 0) {
                completeSwipe(binding.querySwipePrevious, true) { previousAction() }
            } else {
                onMoveCancel()
            }
        }

        override fun onSwipeTop() {
            if (allowTouchEvent && binding.queryHide.isVisible) {
                val card = cards[position].card
                completeSwipe(binding.querySwipePlus, false) { plusAction(card) }
            } else {
                onMoveCancel()
            }
        }

        override fun onSwipeBottom() {
            if (allowTouchEvent && binding.queryHide.isVisible) {
                val card = cards[position].card
                completeSwipe(binding.querySwipeMinus, false) { minusAction(card) }
            } else {
                onMoveCancel()
            }
        }

        // Grows the bar until it fills the dialog, then executes the action after a short delay
        private fun completeSwipe(bar: View, horizontal: Boolean, action: () -> Unit) {
            allowTouchEvent = false
            val growAnimator = if (horizontal) {
                ValueAnimator.ofInt(bar.width, binding.root.width)
            } else {
                ValueAnimator.ofInt(bar.height, binding.root.height)
            }
            growAnimator.duration = ANIM_GROW_TIME
            growAnimator.addUpdateListener { animation: ValueAnimator ->
                if (horizontal) {
                    bar.layoutParams.width = animation.animatedValue as Int
                } else {
                    bar.layoutParams.height = animation.animatedValue as Int
                }
                bar.requestLayout()
            }
            growAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            action()
                            allowTouchEvent = true
                            onMoveCancel()
                        },
                        ANIM_DISPLAY_TIME
                    )
                }
            })
            growAnimator.start()
        }
    }

    companion object {
        // Duration of the bar growing to full size and of showing it before the action, in ms
        private const val ANIM_GROW_TIME = 150L
        private const val ANIM_DISPLAY_TIME = 300L
    }
}
