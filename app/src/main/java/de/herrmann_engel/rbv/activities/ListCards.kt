package de.herrmann_engel.rbv.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import de.herrmann_engel.rbv.Globals
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_COLLECTIONS_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_PACKS_ADVANCED_SEARCH_LIST
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_PACKS_ALL
import de.herrmann_engel.rbv.Globals.LIST_CARDS_GET_DB_TAGS_ADVANCED_SEARCH_ALL
import de.herrmann_engel.rbv.R
import de.herrmann_engel.rbv.actions.CardActions
import de.herrmann_engel.rbv.adapters.AdapterCards
import de.herrmann_engel.rbv.cardlist.ListStatsDialog
import de.herrmann_engel.rbv.cardlist.QueryMode
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding
import de.herrmann_engel.rbv.db.DB_Card_With_Meta
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get
import de.herrmann_engel.rbv.media.MediaFileManager
import de.herrmann_engel.rbv.settings.AppConfig
import de.herrmann_engel.rbv.settings.AppSettings
import de.herrmann_engel.rbv.utils.FormatCards
import de.herrmann_engel.rbv.utils.SearchCards
import de.herrmann_engel.rbv.utils.SortCards
import de.herrmann_engel.rbv.utils.colorAt
import de.herrmann_engel.rbv.utils.showInfoDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Card list of a pack, a collection, all cards or the results of an advanced search
 * (depending on the "collection", "pack" and filter extras, see the LIST_CARDS_GET_DB_*
 * constants in Globals).
 *
 * Besides the list (sortable, searchable, showing front/back/both sides) it offers statistics
 * (see [ListStatsDialog]) and the query mode (see [QueryMode]).
 */
class ListCards : RBVActivity(), CardActions.Listener {
    private val mediaFileManager = MediaFileManager(
        this,
        onMissingFileAction = { openCardMedia(it) }
    )
    private lateinit var binding: ActivityDefaultRecBinding
    private lateinit var dbHelperGet: DB_Helper_Get
    private lateinit var settings: AppSettings
    private lateinit var queryMode: QueryMode
    private lateinit var showCardSideMenuItem: MenuItem
    private lateinit var changeListSortMenuItem: MenuItem
    private lateinit var showQueryModeMenuItem: MenuItem
    private lateinit var showListStatsMenuItem: MenuItem
    private lateinit var searchCardsMenuItem: MenuItem
    private lateinit var searchCardsOffMenuItem: MenuItem
    private var adapter: AdapterCards? = null
    // Loaded cards and display settings survive the activity being recreated (see ListCardsViewModel)
    private val viewModel: ListCardsViewModel by lazy {
        ViewModelProvider(this)[ListCardsViewModel::class.java]
    }
    // All cards of this list (sorted) and the currently displayed cards (after search)
    private var cardsList: MutableList<DB_Card_With_Meta>?
        get() = viewModel.cardsList
        set(value) {
            viewModel.cardsList = value
        }
    private var cardsListFiltered: MutableList<DB_Card_With_Meta>? = null
    private var collectionNo = 0
    private var packNo = 0
    // Advanced search filters
    private var packNos: ArrayList<Int>? = null
    private var tagNo = 0
    private var tagNos: ArrayList<Int>? = null
    private var progressGreater = false
    private var progressNumber = 0
    private var repetitionOlder = false
    private var repetitionNumber = 0
    private var frontBackReverse: Boolean
        get() = viewModel.frontBackReverse
        set(value) {
            viewModel.frontBackReverse = value
        }
    private var showFrontAndBack: Boolean
        get() = viewModel.showFrontAndBack
        set(value) {
            viewModel.showFrontAndBack = value
        }
    private var listSort: Int
        get() = viewModel.listSort
        set(value) {
            viewModel.listSort = value
        }
    private var searchQuery: String?
        get() = viewModel.searchQuery
        set(value) {
            viewModel.searchQuery = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDefaultRecBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelperGet = DB_Helper_Get(this)
        settings = AppSettings(this)
        // Initial sort order and side(s) to show, can be changed via the menu. If the activity
        // is recreated, the values chosen before are kept.
        if (!viewModel.hasDisplaySettings) {
            listSort = settings.defaultSort
            when (settings.flashcardListSide) {
                Globals.FLASHCARD_LIST_SIDE_BACK -> {
                    frontBackReverse = true
                    showFrontAndBack = false
                }

                Globals.FLASHCARD_LIST_SIDE_BOTH -> {
                    frontBackReverse = false
                    showFrontAndBack = true
                }

                else -> {
                    frontBackReverse = false
                    showFrontAndBack = false
                }
            }
        }
        collectionNo = intent.extras!!.getInt("collection")
        packNo = intent.extras!!.getInt("pack")
        packNos = intent.extras!!.getIntegerArrayList("packs")
        tagNo = intent.extras!!.getInt("tag")
        tagNos = intent.extras!!.getIntegerArrayList("tags")
        progressGreater = intent.extras!!.getBoolean("progressGreater")
        progressNumber = intent.extras!!.getInt("progressNumber")
        repetitionOlder = intent.extras!!.getBoolean("repetitionOlder")
        repetitionNumber = intent.extras!!.getInt("repetitionNumber")
        if (settings.uiBackgroundImages) {
            binding.backgroundImage.visibility = View.VISIBLE
            binding.backgroundImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.bg_cards
                )
            )
        }
        // After the query mode, the list scrolls to the last queried card
        queryMode = QueryMode(
            this,
            mediaFileManager,
            onCardChanged = { adapter?.notifyItemChanged(it) },
            onClosed = { lastPosition ->
                viewModel.queryCardId = null
                updateContent()
                if (lastPosition < 0 || lastPosition >= cardsListFiltered!!.size) {
                    binding.recDefault.scrollToPosition(0)
                } else {
                    binding.recDefault.smoothScrollToPosition(
                        lastPosition.coerceAtMost(binding.recDefault.adapter!!.itemCount - 1)
                    )
                }
            }
        )
        setUpEdgeToEdge(binding.recDefault)
    }

    /**
     * Other activities return here (single top) with the id of a deleted, added or updated card,
     * so the loaded list can be updated without reloading everything from the database.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (cardsList != null) {
            val cardDeleted = intent.extras!!.getInt("cardDeleted")
            if (cardDeleted != 0) {
                deleteCardFromList(cardDeleted)
            }
            val cardAdded = intent.extras!!.getInt("cardAdded")
            if (cardAdded != 0 && cardsList?.let {
                    it.stream().noneMatch { i -> i.card.uid == cardAdded }
                } == true
            ) {
                dbHelperGet.getSingleCardWithMeta(cardAdded)?.let {
                    if (settings.formatCards) {
                        FormatCards().formatCard(it, false)
                    }
                    cardsList?.add(it)
                } ?: Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
            val cardUpdated = intent.extras!!.getInt("cardUpdated")
            if (cardUpdated != 0) {
                val cardWithMetaNew = dbHelperGet.getSingleCardWithMeta(cardUpdated)
                val cardWithMetaOld = cardsList?.stream()
                    ?.filter { i -> i.card.uid == cardWithMetaNew?.card?.uid }
                    ?.findFirst()?.orElse(null)
                if (cardWithMetaNew != null && cardWithMetaOld != null) {
                    if (settings.formatCards) {
                        FormatCards().formatCard(
                            cardWithMetaNew,
                            cardWithMetaOld.formattingIsInaccurate
                        )
                    }
                    cardsList?.let {
                        val index = it.indexOfFirst { c -> c.card.uid == cardUpdated }
                        if (index != -1) {
                            it[index] = cardWithMetaNew
                        }
                    } ?: Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                    // If the activity was recreated, the displayed list does not exist yet; it is
                    // then built from cardsList in onResume
                    cardsListFiltered?.let {
                        val index = it.indexOfFirst { c -> c.card.uid == cardUpdated }
                        if (index != -1) {
                            it[index] = cardWithMetaNew
                            adapter?.notifyItemChanged(index)
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Missing media file: open the card's media settings
    private fun openCardMedia(id: Int) {
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

        // Reset options menu
        invalidateOptionsMenu()

        // Set title
        if (collectionNo >= 0 && packNo == LIST_CARDS_GET_DB_PACKS_ALL) {
            title = dbHelperGet.getSingleCollection(collectionNo)?.name
        } else if (packNo >= 0) {
            title = dbHelperGet.getSinglePack(packNo)?.name
        }

        // Colors
        if (packNo >= 0) {
            resources.colorAt(
                R.array.pack_color_background_list,
                dbHelperGet.getSinglePack(packNo)?.colors
            )?.let { binding.root.setBackgroundColor(it) }
        } else if (collectionNo >= 0) {
            resources.colorAt(
                R.array.pack_color_background_list,
                dbHelperGet.getSingleCollection(collectionNo)?.colors
            )?.let { binding.root.setBackgroundColor(it) }
        }

        lifecycleScope.launch {
            // Get cards
            // Cards are only loaded once; later changes are applied via onNewIntent and the callbacks
            if (cardsList == null) {
                // Loaded into a local list in the background; cardsList itself is only accessed
                // on the main thread
                val sort = listSort
                val loadedCards = withContext(Dispatchers.IO) { loadCards(sort) }
                if (cardsList == null) {
                    cardsList = loadedCards
                }
            }

            // Warning: Big lists
            // Show the warning about inaccurate formatting/search only once per mode (stored in the config)
            if ((cardsList?.size ?: 0) > Globals.MAX_SIZE_CARDS_LIST_ACCURATE) {
                val config = AppConfig(this@ListCards)
                val formatCardsActivated = settings.formatCards
                val warnInaccurateFormat =
                    formatCardsActivated && config.showInaccurateWarningFormat
                val warnInaccurateNoFormat =
                    !formatCardsActivated && config.showInaccurateWarningNoFormat
                if (warnInaccurateFormat || warnInaccurateNoFormat) {
                    val warnings: MutableList<String> = ArrayList()
                    warnings.add(
                        String.format(
                            resources.getString(R.string.warn_inaccurate),
                            Globals.MAX_SIZE_CARDS_LIST_ACCURATE
                        )
                    )
                    if (warnInaccurateFormat) {
                        config.showInaccurateWarningFormat = false
                        warnings.add(resources.getString(R.string.warn_inaccurate_list))
                    }
                    if (warnInaccurateNoFormat) {
                        config.showInaccurateWarningNoFormat = false
                    }
                    warnings.add(resources.getString(R.string.warn_inaccurate_search))
                    warnings.add(resources.getString(R.string.warn_inaccurate_note))
                    showInfoDialog(
                        this@ListCards,
                        getString(R.string.info),
                        warnings.joinToString(System.lineSeparator() + System.lineSeparator())
                    )
                }
            }

            // Display content
            // Keep the query mode open after returning, e.g. from editing a card
            if (queryMode.isShowing) {
                queryMode.update(cardsListFiltered!!)
            } else {
                updateContent()
                // Reopen the query mode at the same card if it was open before the activity was
                // recreated; if the card is no longer in the list, the query mode stays closed
                viewModel.queryCardId?.let { cardId ->
                    val cards = cardsListFiltered
                    val position = cards?.indexOfFirst { it.card.uid == cardId } ?: -1
                    if (cards != null && position >= 0) {
                        queryMode.start(cards, position, frontBackReverse)
                    } else {
                        viewModel.queryCardId = null
                    }
                }
            }
        }
    }

    // Remember an open query mode, so it can be reopened if the activity is recreated
    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.queryCardId = if (queryMode.isShowing) queryMode.currentCardId else null
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        // Close the dialog without treating it as closed by the user
        queryMode.release()
        super.onDestroy()
    }

    // Loads, formats and sorts the cards of this list
    private fun loadCards(sort: Int): MutableList<DB_Card_With_Meta> {
        val cards =
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
        if (settings.formatCards) {
            FormatCards().formatCards(cards)
        }
        SortCards().sortCards(cards, sort)
        return cards
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list_cards, menu)
        val startNewCardMenuItem = menu.findItem(R.id.start_new_card)
        val startPackDetailsMenuItem = menu.findItem(R.id.pack_details)
        if (packNo < 0 || !dbHelperGet.existsPack(packNo)) {
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
        showCardSideMenuItem = menu.findItem(R.id.show_card_side)
        val showCardSideMenu = showCardSideMenuItem.subMenu
        showCardSideMenu?.findItem(R.id.show_card_side_front)?.setOnMenuItemClickListener {
            frontBackReverse = false
            showFrontAndBack = false
            updateContent()
            false
        }
        showCardSideMenu?.findItem(R.id.show_card_side_back)?.setOnMenuItemClickListener {
            frontBackReverse = true
            showFrontAndBack = false
            updateContent()
            false
        }
        showCardSideMenu?.findItem(R.id.show_card_side_both)?.setOnMenuItemClickListener {
            frontBackReverse = false
            showFrontAndBack = true
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
        sortMenu?.findItem(R.id.sort_menu_mixed)?.setOnMenuItemClickListener {
            listSort = Globals.SORT_CARDS_MIXED
            sortList()
            updateContent(true)
            false
        }
        // Query mode starts at the first visible card
        showQueryModeMenuItem = menu.findItem(R.id.start_query)
        showQueryModeMenuItem.setOnMenuItemClickListener {
            val startPosition =
                (binding.recDefault.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    .coerceAtMost(cardsListFiltered!!.size - 1)
                    .coerceAtMost(binding.recDefault.adapter!!.itemCount - 1)
            queryMode.start(cardsListFiltered!!, startPosition, frontBackReverse)
            false
        }
        showListStatsMenuItem = menu.findItem(R.id.show_list_stats)
        showListStatsMenuItem.setOnMenuItemClickListener {
            ListStatsDialog(this).show(cardsListFiltered!!)
            false
        }
        // Search on submit; the "search off" item resets the search
        searchCardsMenuItem = menu.findItem(R.id.search_cards)
        val searchView = searchCardsMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchCardsMenuItem.collapseActionView()
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
            updateContent(true)
            false
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        showCardSideMenuItem.isVisible = !cardsListFiltered.isNullOrEmpty()
        changeListSortMenuItem.isVisible = (cardsListFiltered?.size ?: 0) > 1
        showQueryModeMenuItem.isVisible = !cardsListFiltered.isNullOrEmpty()
        showListStatsMenuItem.isVisible = !cardsListFiltered.isNullOrEmpty()
        searchCardsMenuItem.isVisible = !cardsListFiltered.isNullOrEmpty()
        // updateContent() may change the search (e.g. reset it if nothing was found) before the
        // menu exists, so the visibility is only set here
        searchCardsOffMenuItem.isVisible = !searchQuery.isNullOrEmpty()
        return true
    }

    private fun sortList() {
        cardsList?.let {
            SortCards().sortCards(it, listSort)
        }
    }

    /**
     * Applies the search to the cards and displays the result. The adapter is recreated if
     * requested (e.g. after sorting) or if the list was or becomes empty; otherwise it is updated.
     */
    private fun updateContent(recreate: Boolean) {
        cardsList?.let {
            var tempCardList: MutableList<DB_Card_With_Meta> = ArrayList(it)
            // Search
            val searchQueryLocal = searchQuery
            if (!searchQueryLocal.isNullOrEmpty()) {
                val searchResults: MutableList<DB_Card_With_Meta> = ArrayList(tempCardList)
                SearchCards().searchCards(searchResults, searchQueryLocal)
                if (searchResults.isEmpty()) {
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
                    settings.uiFontSizeBig,
                    frontBackReverse,
                    showFrontAndBack,
                    packNo,
                    collectionNo,
                    CardActions(this, this, mediaFileManager)
                )
                binding.recDefault.adapter = adapter
                binding.recDefault.layoutManager = LinearLayoutManager(this)
                binding.recDefault.scrollToPosition(0)
            } else {
                adapter!!.updateContent(
                    tempCardList,
                    frontBackReverse,
                    showFrontAndBack,
                    settings.uiFontSizeBig
                )
            }
            invalidateOptionsMenu()
        } ?: Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
    }

    private fun updateContent() {
        updateContent(false)
    }

    // An import may have changed the font size setting: redraw the list (the cards are not reloaded)
    override fun onImportFinished() {
        updateContent()
    }

    private fun deleteCardFromList(cardId: Int) {
        cardsList?.removeIf { c -> c.card.uid == cardId }
    }

    override fun deletedCards(cardIds: ArrayList<Int>) {
        for (cardId in cardIds) {
            deleteCardFromList(cardId)
        }
        updateContent()
    }

    // Moving is only possible in pack lists, so moved cards no longer belong to this list
    override fun movedCards(cardIds: ArrayList<Int>) {
        for (cardId in cardIds) {
            deleteCardFromList(cardId)
        }
        updateContent()
    }
}
