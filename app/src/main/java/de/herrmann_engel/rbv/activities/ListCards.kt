package de.herrmann_engel.rbv.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.util.Linkify
import android.util.TypedValue
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_LIST
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_PACKS_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.adapters.AdapterCards
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding
import de.herrmann_engel.rbv.databinding.DiaInfoBinding
import de.herrmann_engel.rbv.databinding.DiaListStatsBinding
import de.herrmann_engel.rbv.databinding.DiaQueryBinding
import de.herrmann_engel.rbv.db.DB_Card
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.db.DB_Media_Link_Card
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update
import de.herrmann_engel.rbv.ui.SwipeEvents
import de.herrmann_engel.rbv.utils.FormatCards
import de.herrmann_engel.rbv.utils.SearchCards
import de.herrmann_engel.rbv.utils.SortCards
import de.herrmann_engel.rbv.utils.StringTools
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import kotlin.math.abs
import kotlin.math.roundToInt

class ListCards : CardActionsActivity() {
    private lateinit var binding: ActivityDefaultRecBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private lateinit var dbHelperUpdate: DB_Helper_Update
    private lateinit var settings: SharedPreferences
    private lateinit var queryModeDialog: Dialog
    private lateinit var bindingQueryModeDialog: DiaQueryBinding
    private lateinit var changeFrontBackMenuItem: MenuItem
    private lateinit var changeListSortMenuItem: MenuItem
    private lateinit var showQueryModeMenuItem: MenuItem
    private lateinit var showListStatsMenuItem: MenuItem
    private lateinit var searchCardsMenuItem: MenuItem
    private lateinit var searchCardsOffMenuItem: MenuItem
    private var adapter: AdapterCards? = null
    private var cardsList: MutableList<DB_Card_With_Meta>? = null
    private var cardsListFiltered: MutableList<DB_Card_With_Meta>? = null
    private var collectionNo = 0
    private var packNo = 0
    private var packNos: ArrayList<Int>? = null
    private var tagNo = 0
    private var tagNos: ArrayList<Int>? = null
    private var progressGreater = false
    private var progressNumber = 0
    private var repetitionOlder = false
    private var repetitionNumber = 0
    private var frontBackReverse = false
    private var listSort = 0
    private var searchQuery: String? = null
    private var cardPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefaultRecBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        dbHelperUpdate = DB_Helper_Update(this)
        settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE)
        listSort = settings.getInt("default_sort", Globals.SORT_CARDS_DEFAULT)
        collectionNo = intent.extras!!.getInt("collection")
        packNo = intent.extras!!.getInt("pack")
        packNos = intent.extras!!.getIntegerArrayList("packs")
        tagNo = intent.extras!!.getInt("tag")
        tagNos = intent.extras!!.getIntegerArrayList("tags")
        progressGreater = intent.extras!!.getBoolean("progressGreater")
        progressNumber = intent.extras!!.getInt("progressNumber")
        repetitionOlder = intent.extras!!.getBoolean("repetitionOlder")
        repetitionNumber = intent.extras!!.getInt("repetitionNumber")
        if (settings.getBoolean("ui_bg_images", true)) {
            binding.backgroundImage.visibility = View.VISIBLE
            binding.backgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_cards
                )
            )
        }
        queryModeDialog = Dialog(this, R.style.dia_view)
        bindingQueryModeDialog = DiaQueryBinding.inflate(
            layoutInflater
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (cardsList != null) {
            val cardDeleted = intent.extras!!.getInt("cardDeleted")
            if (cardDeleted != 0) {
                deleteCardFromList(cardDeleted)
            }
            val cardAdded = intent.extras!!.getInt("cardAdded")
            if (cardAdded != 0 && cardsList!!.stream()
                    .noneMatch { i: DB_Card_With_Meta? -> i!!.card.uid == cardAdded }
            ) {
                try {
                    val cardWithMetaNew = dbHelperGet.getSingleCardWithMeta(cardAdded)
                    if (settings.getBoolean("format_cards", false)) {
                        FormatCards().formatCard(cardWithMetaNew, false)
                    }
                    cardsList!!.add(cardWithMetaNew)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
            val cardUpdated = intent.extras!!.getInt("cardUpdated")
            if (cardUpdated != 0) {
                try {
                    val cardWithMetaNew = dbHelperGet.getSingleCardWithMeta(cardUpdated)
                    val cardWithMetaOld = cardsList!!.stream()
                        .filter { i: DB_Card_With_Meta? -> i!!.card.uid == cardWithMetaNew!!.card.uid }
                        .findFirst().orElse(null)
                    if (cardWithMetaNew != null && cardWithMetaOld != null) {
                        if (settings.getBoolean("format_cards", false)) {
                            FormatCards().formatCard(
                                cardWithMetaNew,
                                cardWithMetaOld.formattingIsInaccurate
                            )
                        }
                        var index = cardsList!!.indexOf(cardWithMetaOld)
                        if (index != -1) {
                            cardsList!![index] = cardWithMetaNew
                        }
                        index = cardsListFiltered!!.indexOf(cardWithMetaOld)
                        if (index != -1) {
                            cardsListFiltered!![index] = cardWithMetaNew
                            adapter!!.notifyItemChanged(index)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun notifyFolderSet() {}
    override fun notifyMissingAction(id: Int) {
        try {
            val intent = Intent(this, EditCardMedia::class.java)
            intent.putExtra("card", id)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
        }
    }

    public override fun onResume() {
        super.onResume()

        // Set title
        try {
            if (collectionNo >= 0 && packNo == LIST_CARDS_GET_DB_PACKS_ALL) {
                title = dbHelperGet.getSingleCollection(collectionNo).name
            } else if (packNo >= 0) {
                title = dbHelperGet.getSinglePack(packNo).name
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Colors
        if (packNo >= 0) {
            val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
            val colorsBackground =
                resources.obtainTypedArray(R.array.pack_color_background_list)
            val packColors = dbHelperGet.getSinglePack(packNo).colors
            val minimalLength = colorsStatusBar.length().coerceAtMost(colorsBackground.length())
            if (packColors in 0..<minimalLength) {
                val colorStatusBar = colorsStatusBar.getColor(packColors, 0)
                val colorBackground = colorsBackground.getColor(packColors, 0)
                supportActionBar?.setBackgroundDrawable(ColorDrawable(colorStatusBar))
                window.statusBarColor = colorStatusBar
                binding.root.setBackgroundColor(colorBackground)
            }
            colorsStatusBar.recycle()
            colorsBackground.recycle()
        } else if (collectionNo >= 0) {
            val colorsStatusBar = resources.obtainTypedArray(R.array.pack_color_statusbar)
            val colorsBackground =
                resources.obtainTypedArray(R.array.pack_color_background_list)
            val packColors = dbHelperGet.getSingleCollection(collectionNo).colors
            val minimalLength = colorsStatusBar.length().coerceAtMost(colorsBackground.length())
            if (packColors in 0..<minimalLength) {
                val colorStatusBar = colorsStatusBar.getColor(packColors, 0)
                val colorBackground = colorsBackground.getColor(packColors, 0)
                supportActionBar?.setBackgroundDrawable(ColorDrawable(colorStatusBar))
                window.statusBarColor = colorStatusBar
                binding.root.setBackgroundColor(colorBackground)
            }
            colorsStatusBar.recycle()
            colorsBackground.recycle()
        }

        // Get cards
        if (cardsList == null) {
            cardsList =
                if (collectionNo == LIST_CARDS_GET_DB_COLLECTIONS_ALL && packNo == LIST_CARDS_GET_DB_PACKS_ALL) {
                    dbHelperGet.allCardsWithMeta
                } else if (packNo == LIST_CARDS_GET_DB_PACKS_ALL) {
                    dbHelperGet.getAllCardsByCollectionWithMeta(collectionNo)
                } else if (packNo == LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_LIST) {
                    dbHelperGet.getAllCardsWithMetaFiltered(
                        packNos,
                        if (tagNo == LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL) {
                            null
                        } else {
                            tagNos
                        },
                        progressGreater,
                        progressNumber,
                        repetitionOlder,
                        repetitionNumber
                    )
                } else if (packNo == LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL) {
                    dbHelperGet.getAllCardsWithMetaFiltered(
                        null,
                        if (tagNo == LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL) {
                            null
                        } else {
                            tagNos
                        },
                        progressGreater,
                        progressNumber,
                        repetitionOlder,
                        repetitionNumber
                    )
                } else {
                    dbHelperGet.getAllCardsByPackWithMeta(packNo)
                }
            if (settings.getBoolean("format_cards", false)) {
                FormatCards().formatCards(cardsList!!)
            }
            sortList()
        }

        // Warning: Big lists
        if (cardsList!!.size > Globals.MAX_SIZE_CARDS_LIST_ACCURATE) {
            val config = getSharedPreferences(Globals.CONFIG_NAME, MODE_PRIVATE)
            val formatCardsActivated = settings.getBoolean("format_cards", false)
            val warnInaccurateFormat =
                formatCardsActivated && config.getBoolean("inaccurate_warning_format", true)
            val warnInaccurateNoFormat =
                !formatCardsActivated && config.getBoolean("inaccurate_warning_no_format", true)
            if (warnInaccurateFormat || warnInaccurateNoFormat) {
                val configEditor = config.edit()
                val infoDialog = Dialog(this, R.style.dia_view)
                val bindingInfoDialog = DiaInfoBinding.inflate(
                    layoutInflater
                )
                infoDialog.setContentView(bindingInfoDialog.root)
                infoDialog.setTitle(resources.getString(R.string.info))
                infoDialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                val warnings: MutableList<String> = ArrayList()
                warnings.add(
                    String.format(
                        resources.getString(R.string.warn_inaccurate),
                        Globals.MAX_SIZE_CARDS_LIST_ACCURATE
                    )
                )
                if (warnInaccurateFormat) {
                    configEditor.putBoolean("inaccurate_warning_format", false)
                    warnings.add(resources.getString(R.string.warn_inaccurate_list))
                }
                if (warnInaccurateNoFormat) {
                    configEditor.putBoolean("inaccurate_warning_no_format", false)
                }
                warnings.add(resources.getString(R.string.warn_inaccurate_search))
                warnings.add(resources.getString(R.string.warn_inaccurate_note))
                bindingInfoDialog.diaInfoText.text = java.lang.String.join(
                    System.lineSeparator().plus(System.lineSeparator()),
                    warnings
                )
                infoDialog.show()
                configEditor.apply()
            }
        }

        // Display content
        if (queryModeDialog.isShowing) {
            nextQuery(true)
        } else {
            updateContent()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list_cards, menu)
        val startNewCardMenuItem = menu.findItem(R.id.start_new_card)
        val startPackDetailsMenuItem = menu.findItem(R.id.pack_details)
        if (packNo < 0) {
            startNewCardMenuItem.isVisible = false
            startPackDetailsMenuItem.isVisible = false
        } else {
            startNewCardMenuItem.setOnMenuItemClickListener {
                val intent = Intent(this, NewCard::class.java)
                intent.putExtra("pack", packNo)
                this.startActivity(intent)
                false
            }
            startPackDetailsMenuItem.setOnMenuItemClickListener {
                val intent = Intent(this, ViewPack::class.java)
                intent.putExtra("collection", collectionNo)
                intent.putExtra("pack", packNo)
                this.startActivity(intent)
                false
            }
        }
        changeFrontBackMenuItem = menu.findItem(R.id.change_front_back)
        changeFrontBackMenuItem.setOnMenuItemClickListener {
            frontBackReverse = !frontBackReverse
            updateContent()
            false
        }
        changeListSortMenuItem = menu.findItem(R.id.sort_menu)
        val sortMenu = changeListSortMenuItem.subMenu
        sortMenu?.findItem(R.id.sort_menu_default)?.setOnMenuItemClickListener {
            listSort = Globals.SORT_CARDS_DEFAULT
            sortList()
            updateContent(true)
            false
        }
        sortMenu?.findItem(R.id.sort_menu_random)?.setOnMenuItemClickListener {
            listSort = Globals.SORT_CARDS_RANDOM
            sortList()
            updateContent(true)
            false
        }
        sortMenu?.findItem(R.id.sort_menu_alphabetical)?.setOnMenuItemClickListener {
            listSort = Globals.SORT_CARDS_ALPHABETICAL
            sortList()
            updateContent(true)
            false
        }
        sortMenu?.findItem(R.id.sort_menu_repetition)?.setOnMenuItemClickListener {
            listSort = Globals.SORT_CARDS_REPETITION
            sortList()
            updateContent(true)
            false
        }
        showQueryModeMenuItem = menu.findItem(R.id.start_query)
        showQueryModeMenuItem.setOnMenuItemClickListener {
            queryModeDialog.setContentView(bindingQueryModeDialog.root)
            queryModeDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            cardPosition =
                (binding.recDefault.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    .coerceAtMost(cardsListFiltered!!.size - 1)
                    .coerceAtMost(binding.recDefault.adapter!!.itemCount - 1)
            nextQuery()
            queryModeDialog.setOnKeyListener { _, keyCode: Int, event: KeyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    queryModeDialog.dismiss()
                    updateContent()
                    binding.recDefault.smoothScrollToPosition(
                        cardPosition.coerceAtMost(binding.recDefault.adapter!!.itemCount - 1)
                    )
                    return@setOnKeyListener true
                }
                false
            }
            queryModeDialog.show()
            false
        }
        showListStatsMenuItem = menu.findItem(R.id.show_list_stats)
        showListStatsMenuItem.setOnMenuItemClickListener {
            val listStatsDialog = Dialog(this, R.style.dia_view)
            val bindingListStatsDialog = DiaListStatsBinding.inflate(
                layoutInflater
            )
            listStatsDialog.setContentView(bindingListStatsDialog.root)
            listStatsDialog.setTitle(resources.getString(R.string.list_stats))
            listStatsDialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            bindingListStatsDialog.listStatCardsTotalContent.text =
                cardsListFiltered!!.size.toString()
            val statTotalProgress =
                cardsListFiltered!!.stream().mapToInt { c: DB_Card_With_Meta? -> c!!.card.known }
                    .sum()
            bindingListStatsDialog.listStatProgressTotalContent.text =
                statTotalProgress.toString()
            val statMaxProgress =
                cardsListFiltered!!.stream().mapToInt { c: DB_Card_With_Meta? -> c!!.card.known }
                    .max().orElse(0)
            bindingListStatsDialog.listStatProgressMaxContent.text =
                statMaxProgress.toString()
            val statMinProgress =
                cardsListFiltered!!.stream().mapToInt { c: DB_Card_With_Meta? -> c!!.card.known }
                    .min().orElse(0)
            bindingListStatsDialog.listStatProgressMinContent.text =
                statMinProgress.toString()
            var statAvgProgress =
                cardsListFiltered!!.stream().mapToInt { c: DB_Card_With_Meta? -> c!!.card.known }
                    .average().orElse(0.0)
            statAvgProgress = (statAvgProgress * 100.0).roundToInt() / 100.0
            bindingListStatsDialog.listStatProgressAvgContent.text =
                statAvgProgress.toString()
            val statProgressTableIs0 =
                cardsListFiltered!!.stream().filter { c: DB_Card_With_Meta? -> c!!.card.known == 0 }
                    .count().toInt()
            val statProgressTableIs1 =
                cardsListFiltered!!.stream().filter { c: DB_Card_With_Meta? -> c!!.card.known == 1 }
                    .count().toInt()
            val statProgressTableIs2 =
                cardsListFiltered!!.stream().filter { c: DB_Card_With_Meta? -> c!!.card.known == 2 }
                    .count().toInt()
            val statProgressTableIs3 =
                cardsListFiltered!!.stream().filter { c: DB_Card_With_Meta? -> c!!.card.known == 3 }
                    .count().toInt()
            val statProgressTableIs4 =
                cardsListFiltered!!.stream().filter { c: DB_Card_With_Meta? -> c!!.card.known == 4 }
                    .count().toInt()
            val statProgressTableIs5OrMore =
                cardsListFiltered!!.stream().filter { c: DB_Card_With_Meta? -> c!!.card.known >= 5 }
                    .count().toInt()
            var percentCurrent = statProgressTableIs0 / cardsListFiltered!!.size.toFloat()
            bindingListStatsDialog.listStatProgressCounterNumber0.text =
                statProgressTableIs0.toString()
            bindingListStatsDialog.listStatProgressCounterPercent0.text =
                (percentCurrent * 100).roundToInt().toString()
            percentCurrent = statProgressTableIs1 / cardsListFiltered!!.size.toFloat()
            bindingListStatsDialog.listStatProgressCounterNumber1.text =
                statProgressTableIs1.toString()
            bindingListStatsDialog.listStatProgressCounterPercent1.text =
                (percentCurrent * 100).roundToInt().toString()
            percentCurrent = statProgressTableIs2 / cardsListFiltered!!.size.toFloat()
            bindingListStatsDialog.listStatProgressCounterNumber2.text =
                statProgressTableIs2.toString()
            bindingListStatsDialog.listStatProgressCounterPercent2.text =
                (percentCurrent * 100).roundToInt().toString()
            percentCurrent = statProgressTableIs3 / cardsListFiltered!!.size.toFloat()
            bindingListStatsDialog.listStatProgressCounterNumber3.text =
                statProgressTableIs3.toString()
            bindingListStatsDialog.listStatProgressCounterPercent3.text =
                (percentCurrent * 100).roundToInt().toString()
            percentCurrent = statProgressTableIs4 / cardsListFiltered!!.size.toFloat()
            bindingListStatsDialog.listStatProgressCounterNumber4.text =
                statProgressTableIs4.toString()
            bindingListStatsDialog.listStatProgressCounterPercent4.text =
                (percentCurrent * 100).roundToInt().toString()
            percentCurrent = statProgressTableIs5OrMore / cardsListFiltered!!.size.toFloat()
            bindingListStatsDialog.listStatProgressCounterNumber5.text =
                statProgressTableIs5OrMore.toString()
            bindingListStatsDialog.listStatProgressCounterPercent5.text =
                (percentCurrent * 100).roundToInt().toString()
            listStatsDialog.show()
            false
        }
        searchCardsMenuItem = menu.findItem(R.id.search_cards)
        val searchView = searchCardsMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchCardsMenuItem.collapseActionView()
                searchCardsOffMenuItem.isVisible = true
                searchQuery = query
                updateContent(true)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        searchCardsOffMenuItem = menu.findItem(R.id.search_cards_off)
        searchCardsOffMenuItem.setOnMenuItemClickListener {
            searchQuery = ""
            searchCardsOffMenuItem.isVisible = false
            updateContent(true)
            false
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        changeFrontBackMenuItem.setTitle(if (frontBackReverse) R.string.change_back_front else R.string.change_front_back)
        changeFrontBackMenuItem.isVisible = cardsListFiltered!!.isNotEmpty()
        changeListSortMenuItem.isVisible = cardsListFiltered!!.size > 1
        showQueryModeMenuItem.isVisible = cardsListFiltered!!.isNotEmpty()
        showListStatsMenuItem.isVisible = cardsListFiltered!!.isNotEmpty()
        searchCardsMenuItem.isVisible = cardsListFiltered!!.isNotEmpty()
        if (searchQuery?.isNotEmpty() == true) {
            searchCardsOffMenuItem.isVisible = true
        }
        return true
    }

    private fun sortList() {
        SortCards().sortCards(cardsList!!, listSort)
    }

    private fun queryModeNextAction() {
        cardPosition++
        if (cardPosition >= cardsListFiltered!!.size) {
            queryModeDialog.dismiss()
            updateContent()
            binding.recDefault.scrollToPosition(0)
        } else {
            nextQuery()
        }
    }

    private fun queryModePreviousAction() {
        cardPosition--
        if (cardPosition < 0) {
            queryModeDialog.dismiss()
            updateContent()
            binding.recDefault.scrollToPosition(0)
        } else {
            nextQuery()
        }
    }

    private fun queryModeCardKnownChanged(card: DB_Card, known: Int) {
        card.known = known
        card.lastRepetition = System.currentTimeMillis() / 1000L
        dbHelperUpdate.updateCard(card)
        adapter!!.notifyItemChanged(cardPosition)
    }

    private fun queryModePlusAction(card: DB_Card) {
        val known = card.known + 1
        queryModeCardKnownChanged(card, known)
        queryModeNextAction()
    }

    private fun queryModeMinusAction(card: DB_Card) {
        val known = 0.coerceAtLeast(card.known - 1)
        queryModeCardKnownChanged(card, known)
        queryModeNextAction()
    }

    private fun nextQuery() {
        nextQuery(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun nextQuery(onlyUpdate: Boolean) {
        val cardWithMeta = cardsListFiltered!![cardPosition]
        val card = cardWithMeta.card

        val front: SpannableString
        val back: SpannableString
        val formatString = StringTools()
        if (settings.getBoolean("format_cards", false)) {
            front =
                if (frontBackReverse) formatString.format(card.back) else formatString.format(
                    card.front
                )
            back =
                if (frontBackReverse) formatString.format(card.front) else formatString.format(
                    card.back
                )
        } else {
            val frontString = if (frontBackReverse) card.back else card.front
            val backString = if (frontBackReverse) card.front else card.back
            front = SpannableString(frontString)
            back = SpannableString(backString)
        }
        bindingQueryModeDialog.queryShow.text = front
        bindingQueryModeDialog.queryHide.text = back
        if (settings.getBoolean("ui_font_size", false)) {
            bindingQueryModeDialog.queryShow.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.card_front_size_big)
            )
            bindingQueryModeDialog.queryHide.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.card_front_size_big)
            )
        }

        if (card.notes.isNullOrEmpty()) {
            bindingQueryModeDialog.queryButtonNotes.visibility = View.GONE
        } else {
            bindingQueryModeDialog.queryButtonNotes.visibility = View.VISIBLE
            bindingQueryModeDialog.queryButtonNotes.setOnClickListener {
                val infoDialog = Dialog(this, R.style.dia_view)
                val bindingInfoDialog = DiaInfoBinding.inflate(
                    layoutInflater
                )
                infoDialog.setContentView(bindingInfoDialog.root)
                infoDialog.setTitle(resources.getString(R.string.query_notes_title))
                infoDialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                bindingInfoDialog.diaInfoText.setTextIsSelectable(true)
                if (settings.getBoolean("format_card_notes", false)) {
                    val markwon = Markwon.builder(this)
                        .usePlugin(
                            LinkifyPlugin.create(
                                Linkify.WEB_URLS
                            )
                        )
                        .build()
                    bindingInfoDialog.diaInfoText.movementMethod =
                        BetterLinkMovementMethod.getInstance()
                    bindingInfoDialog.diaInfoText.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                    markwon.setMarkdown(bindingInfoDialog.diaInfoText, card.notes)
                } else {
                    bindingInfoDialog.diaInfoText.autoLinkMask = Linkify.WEB_URLS
                    bindingInfoDialog.diaInfoText.text = card.notes
                }
                infoDialog.show()
            }
        }

        val rootBackground = bindingQueryModeDialog.root.background as LayerDrawable
        val rootBackgroundLeft =
            rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_left) as GradientDrawable
        val rootBackgroundTop =
            rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_top) as GradientDrawable
        val rootBackgroundBottom =
            rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_bottom) as GradientDrawable
        rootBackgroundLeft.alpha = if (cardPosition > 0) 255 else 0
        bindingQueryModeDialog.root.setOnTouchListener(object : SwipeEvents() {
            val ANIM_GROW_TIME = 150
            val ANIM_DISPlAY_TIME = 300
            var allowTouchEvent = true
            override fun onMoveX(distance: Float) {
                if (allowTouchEvent) {
                    if (distance < 0) {
                        bindingQueryModeDialog.querySwipeNext.layoutParams.width =
                            (2 * abs(distance)).toInt()
                        bindingQueryModeDialog.querySwipeNext.requestLayout()
                    } else if (cardPosition > 0) {
                        bindingQueryModeDialog.querySwipePrevious.layoutParams.width =
                            (2 * abs(distance)).toInt()
                        bindingQueryModeDialog.querySwipePrevious.requestLayout()
                    }
                }
            }

            override fun onMoveY(distance: Float) {
                if (allowTouchEvent && bindingQueryModeDialog.queryHide.visibility == View.VISIBLE) {
                    if (distance < 0) {
                        bindingQueryModeDialog.querySwipePlus.layoutParams.height =
                            (2 * abs(distance)).toInt()
                        bindingQueryModeDialog.querySwipePlus.requestLayout()
                    } else {
                        bindingQueryModeDialog.querySwipeMinus.layoutParams.height =
                            (2 * abs(distance)).toInt()
                        bindingQueryModeDialog.querySwipeMinus.requestLayout()
                    }
                }
            }

            override fun onMoveCancel() {
                super.onMoveCancel()
                if (allowTouchEvent) {
                    bindingQueryModeDialog.querySwipeNext.layoutParams.width = 0
                    bindingQueryModeDialog.querySwipeNext.requestLayout()
                    bindingQueryModeDialog.querySwipePrevious.layoutParams.width = 0
                    bindingQueryModeDialog.querySwipePrevious.requestLayout()
                    bindingQueryModeDialog.querySwipeMinus.layoutParams.height = 0
                    bindingQueryModeDialog.querySwipeMinus.requestLayout()
                    bindingQueryModeDialog.querySwipePlus.layoutParams.height = 0
                    bindingQueryModeDialog.querySwipePlus.requestLayout()
                }
            }

            override fun onSwipeLeft() {
                if (allowTouchEvent) {
                    allowTouchEvent = false
                    val growAnimator = ValueAnimator.ofInt(
                        bindingQueryModeDialog.querySwipeNext.width,
                        bindingQueryModeDialog.root.width
                    )
                    growAnimator.duration = ANIM_GROW_TIME.toLong()
                    growAnimator.addUpdateListener { animation: ValueAnimator ->
                        bindingQueryModeDialog.querySwipeNext.layoutParams.width =
                            animation.animatedValue as Int
                        bindingQueryModeDialog.querySwipeNext.requestLayout()
                    }
                    growAnimator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    queryModeNextAction()
                                    allowTouchEvent = true
                                    onMoveCancel()
                                },
                                ANIM_DISPlAY_TIME.toLong()
                            )
                        }
                    })
                    growAnimator.start()
                } else {
                    onMoveCancel()
                }
            }

            override fun onSwipeRight() {
                if (allowTouchEvent && cardPosition > 0) {
                    allowTouchEvent = false
                    val growAnimator = ValueAnimator.ofInt(
                        bindingQueryModeDialog.querySwipePrevious.width,
                        bindingQueryModeDialog.root.width
                    )
                    growAnimator.duration = ANIM_GROW_TIME.toLong()
                    growAnimator.addUpdateListener { animation: ValueAnimator ->
                        bindingQueryModeDialog.querySwipePrevious.layoutParams.width =
                            animation.animatedValue as Int
                        bindingQueryModeDialog.querySwipePrevious.requestLayout()
                    }
                    growAnimator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    queryModePreviousAction()
                                    allowTouchEvent = true
                                    onMoveCancel()
                                },
                                ANIM_DISPlAY_TIME.toLong()
                            )
                        }
                    })
                    growAnimator.start()
                } else {
                    onMoveCancel()
                }
            }

            override fun onSwipeTop() {
                if (allowTouchEvent && bindingQueryModeDialog.queryHide.visibility == View.VISIBLE) {
                    allowTouchEvent = false
                    val growAnimator = ValueAnimator.ofInt(
                        bindingQueryModeDialog.querySwipePlus.height,
                        bindingQueryModeDialog.root.height
                    )
                    growAnimator.duration = ANIM_GROW_TIME.toLong()
                    growAnimator.addUpdateListener { animation: ValueAnimator ->
                        bindingQueryModeDialog.querySwipePlus.layoutParams.height =
                            animation.animatedValue as Int
                        bindingQueryModeDialog.querySwipePlus.requestLayout()
                    }
                    growAnimator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    queryModePlusAction(card)
                                    allowTouchEvent = true
                                    onMoveCancel()
                                },
                                ANIM_DISPlAY_TIME.toLong()
                            )
                        }
                    })
                    growAnimator.start()
                } else {
                    onMoveCancel()
                }
            }

            override fun onSwipeBottom() {
                if (allowTouchEvent && bindingQueryModeDialog.queryHide.visibility == View.VISIBLE) {
                    allowTouchEvent = false
                    val growAnimator = ValueAnimator.ofInt(
                        bindingQueryModeDialog.querySwipeMinus.height,
                        bindingQueryModeDialog.root.height
                    )
                    growAnimator.duration = ANIM_GROW_TIME.toLong()
                    growAnimator.addUpdateListener { animation: ValueAnimator ->
                        bindingQueryModeDialog.querySwipeMinus.layoutParams.height =
                            animation.animatedValue as Int
                        bindingQueryModeDialog.querySwipeMinus.requestLayout()
                    }
                    growAnimator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    queryModeMinusAction(card)
                                    allowTouchEvent = true
                                    onMoveCancel()
                                },
                                ANIM_DISPlAY_TIME.toLong()
                            )
                        }
                    })
                    growAnimator.start()
                } else {
                    onMoveCancel()
                }
            }
        })
        bindingQueryModeDialog.queryPlus.setOnClickListener {
            queryModePlusAction(card)
        }
        bindingQueryModeDialog.queryMinus.setOnClickListener {
            queryModeMinusAction(card)
        }

        if (!onlyUpdate) {
            queryModeDialog.setTitle(
                String.format(
                    resources.getString(R.string.query_mode_title),
                    resources.getString(R.string.query_mode),
                    cardPosition + 1,
                    cardsListFiltered!!.size
                )
            )

            val colorsBackgroundQuery =
                resources.obtainTypedArray(R.array.pack_color_background_query)
            val colorsBackgroundHighlight = resources
                .obtainTypedArray(R.array.pack_color_background_highlight)
            val packColors = cardWithMeta.packColor
            val minimalLength = colorsBackgroundQuery.length().coerceAtMost(colorsBackgroundHighlight.length())
            if (packColors in 0..<minimalLength) {
                val colorBackgroundQuery = colorsBackgroundQuery.getColor(packColors, 0)
                val colorBackgroundHighlight =
                    colorsBackgroundHighlight.getColor(packColors, 0)
                val rootBackgroundMain =
                    rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_main) as GradientDrawable
                rootBackgroundMain.setColor(colorBackgroundQuery)
                bindingQueryModeDialog.queryHide.setBackgroundColor(colorBackgroundHighlight)
            }
            colorsBackgroundQuery.recycle()
            colorsBackgroundHighlight.recycle()

            val imageList =
                dbHelperGet.getImageMediaLinksByCard(card.uid) as ArrayList<DB_Media_Link_Card>
            if (imageList.isEmpty()) {
                bindingQueryModeDialog.queryButtonMediaImage.visibility = View.GONE
            } else {
                bindingQueryModeDialog.queryButtonMediaImage.visibility = View.VISIBLE
                bindingQueryModeDialog.queryButtonMediaImage.setOnClickListener {
                    showImageListDialog(
                        imageList
                    )
                }
            }
            val mediaList =
                dbHelperGet.getAllMediaLinksByCard(card.uid) as ArrayList<DB_Media_Link_Card>
            if (mediaList.isEmpty()) {
                bindingQueryModeDialog.queryButtonMediaOther.visibility = View.GONE
            } else {
                bindingQueryModeDialog.queryButtonMediaOther.visibility = View.VISIBLE
                bindingQueryModeDialog.queryButtonMediaOther.setOnClickListener {
                    showMediaListDialog(
                        mediaList
                    )
                }
            }

            rootBackgroundTop.alpha = 0
            rootBackgroundBottom.alpha = 0

            bindingQueryModeDialog.queryPlus.visibility = View.GONE
            bindingQueryModeDialog.queryMinus.visibility = View.GONE
            bindingQueryModeDialog.querySkip.setOnClickListener {
                queryModeNextAction()
            }
            if (cardPosition == 0) {
                bindingQueryModeDialog.queryBack.visibility = View.INVISIBLE
            } else {
                bindingQueryModeDialog.queryBack.visibility = View.VISIBLE
                bindingQueryModeDialog.queryBack.setOnClickListener {
                    queryModePreviousAction()
                }
            }
            bindingQueryModeDialog.queryHide.visibility = View.GONE
            bindingQueryModeDialog.queryButtonEdit.visibility = View.GONE
            bindingQueryModeDialog.queryButtonHide.visibility = View.VISIBLE
            bindingQueryModeDialog.queryButtonHide.setOnClickListener {
                bindingQueryModeDialog.queryButtonHide.visibility = View.GONE
                bindingQueryModeDialog.queryHide.visibility = View.VISIBLE
                bindingQueryModeDialog.queryPlus.visibility = View.VISIBLE
                bindingQueryModeDialog.queryMinus.visibility = View.VISIBLE
                rootBackgroundTop.alpha = 255
                rootBackgroundBottom.alpha = 255
                bindingQueryModeDialog.queryButtonEdit.visibility = View.VISIBLE
                bindingQueryModeDialog.queryButtonEdit.setOnClickListener {
                    val intent = Intent(this, EditCard::class.java)
                    intent.putExtra("card", card.uid)
                    intent.putExtra("backToList", true)
                    this.startActivity(intent)
                }
            }
        }
    }

    private fun updateContent(recreate: Boolean) {
        var tempCardList: MutableList<DB_Card_With_Meta> = ArrayList(cardsList!!)
        // Search
        if (searchQuery != null && searchQuery!!.isNotEmpty()) {
            val searchResults: MutableList<DB_Card_With_Meta> = ArrayList(tempCardList)
            SearchCards().searchCards(searchResults, searchQuery!!)
            if (searchResults.isEmpty()) {
                searchCardsOffMenuItem.isVisible = false
                searchQuery = ""
                Toast.makeText(this, R.string.search_no_results, Toast.LENGTH_LONG).show()
            } else {
                tempCardList = searchResults
            }
        }
        // Set recycler view
        if (adapter == null || cardsListFiltered == null || recreate || cardsListFiltered!!.isEmpty() || tempCardList.isEmpty()) {
            cardsListFiltered = tempCardList
            adapter = AdapterCards(
                cardsListFiltered!!,
                settings.getBoolean("ui_font_size", false),
                frontBackReverse,
                packNo,
                collectionNo
            )
            binding.recDefault.adapter = adapter
            binding.recDefault.layoutManager = LinearLayoutManager(this)
            binding.recDefault.scrollToPosition(0)
        } else {
            adapter!!.updateContent(tempCardList, frontBackReverse)
        }
        invalidateOptionsMenu()
    }

    private fun updateContent() {
        updateContent(false)
    }

    private fun deleteCardFromList(cardId: Int) {
        cardsList!!.removeIf { c: DB_Card_With_Meta? -> c!!.card.uid == cardId }
    }

    override fun deletedCards(cardIds: ArrayList<Int>) {
        for (cardId in cardIds) {
            deleteCardFromList(cardId)
        }
        updateContent()
    }

    override fun movedCards(cardIds: ArrayList<Int>) {
        for (cardId in cardIds) {
            deleteCardFromList(cardId)
        }
        updateContent()
    }
}
