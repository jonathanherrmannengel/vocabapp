package de.herrmann_engel.rbv.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterCards;
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding;
import de.herrmann_engel.rbv.databinding.DiaInfoBinding;
import de.herrmann_engel.rbv.databinding.DiaListStatsBinding;
import de.herrmann_engel.rbv.databinding.DiaQueryBinding;
import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Card_With_Meta;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update;
import de.herrmann_engel.rbv.utils.FormatCards;
import de.herrmann_engel.rbv.utils.SearchCards;
import de.herrmann_engel.rbv.utils.SortCards;
import de.herrmann_engel.rbv.utils.StringTools;
import de.herrmann_engel.rbv.utils.SwipeEvents;
import io.noties.markwon.Markwon;
import io.noties.markwon.linkify.LinkifyPlugin;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class ListCards extends FileTools {
    private ActivityDefaultRecBinding binding;
    private DB_Helper_Get dbHelperGet;
    private DB_Helper_Update dbHelperUpdate;
    private AdapterCards adapter;
    private SharedPreferences settings;
    private List<DB_Card_With_Meta> cardsList;
    private List<DB_Card_With_Meta> cardsListFiltered;
    private int collectionNo;
    private int packNo;
    private ArrayList<Integer> packNos;
    private boolean progressGreater;
    private int progressNumber;
    private boolean frontBackReverse;
    private int listSort;
    private String searchQuery;
    private int cardPosition;
    private MenuItem changeFrontBackMenuItem;
    private MenuItem changeListSortMenuItem;
    private MenuItem searchCardsMenuItem;
    private MenuItem searchCardsOffMenuItem;
    private MenuItem showQueryModeMenuItem;
    private MenuItem showListStatsMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDefaultRecBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        listSort = settings.getInt("default_sort", Globals.SORT_DEFAULT);

        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        packNos = getIntent().getExtras().getIntegerArrayList("packs");
        progressGreater = getIntent().getExtras().getBoolean("progressGreater");
        progressNumber = getIntent().getExtras().getInt("progressNumber");

        if (settings.getBoolean("ui_bg_images", true)) {
            binding.backgroundImage.setVisibility(View.VISIBLE);
            binding.backgroundImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.bg_cards));
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (cardsList != null) {
            FormatCards formatCards = new FormatCards(this);
            int cardDeleted = intent.getExtras().getInt("cardDeleted");
            if (cardDeleted != 0) {
                cardsList.removeIf(c -> c.card.uid == cardDeleted);
            }
            int cardAdded = intent.getExtras().getInt("cardAdded");
            if (cardAdded != 0 && cardsList.stream().noneMatch(i -> i.card.uid == cardAdded)) {
                try {
                    DB_Card_With_Meta cardWithMetaNew = dbHelperGet.getSingleCardWithMeta(cardAdded);
                    formatCards.formatCard(cardWithMetaNew, false);
                    cardsList.add(cardWithMetaNew);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }
            int cardUpdated = intent.getExtras().getInt("cardUpdated");
            if (cardUpdated != 0) {
                try {
                    DB_Card_With_Meta cardWithMetaNew = dbHelperGet.getSingleCardWithMeta(cardUpdated);
                    DB_Card_With_Meta cardWithMetaOld = cardsListFiltered.stream().filter(i -> i.card.uid == cardWithMetaNew.card.uid).findFirst().orElse(null);
                    if (cardWithMetaNew != null && cardWithMetaOld != null) {
                        formatCards.formatCard(cardWithMetaNew, cardWithMetaOld.formattingIsInaccurate);
                        int index = cardsList.indexOf(cardWithMetaOld);
                        if (index != -1) {
                            cardsList.set(index, cardWithMetaNew);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void notifyFolderSet() {
    }

    @Override
    protected void notifyMissingAction(int id) {
        try {
            Intent intent = new Intent(this, EditCardMedia.class);
            intent.putExtra("card", id);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //Set title
        dbHelperGet = new DB_Helper_Get(this);
        try {
            if (collectionNo > -1 && packNo == -1) {
                setTitle(dbHelperGet.getSingleCollection(collectionNo).name);
            } else if (packNo > -1) {
                setTitle(dbHelperGet.getSinglePack(packNo).name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Colors
        if (packNo >= 0) {
            try {
                int packColors = dbHelperGet.getSinglePack(packNo).colors;
                TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
                TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
                if (packColors < Math.min(colors.length(), colorsBackground.length()) && packColors >= 0) {
                    int color = colors.getColor(packColors, 0);
                    int colorBackground = colorsBackground.getColor(packColors, 0);
                    Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                    Window window = this.getWindow();
                    window.setStatusBarColor(color);
                    binding.getRoot().setBackgroundColor(colorBackground);
                }
                colors.recycle();
                colorsBackground.recycle();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        } else if (collectionNo >= 0) {
            try {
                int packColors = dbHelperGet.getSingleCollection(collectionNo).colors;
                TypedArray colors = getResources().obtainTypedArray(R.array.pack_color_main);
                TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
                if (packColors < Math.min(colors.length(), colorsBackground.length()) && packColors >= 0) {
                    int color = colors.getColor(packColors, 0);
                    int colorBackground = colorsBackground.getColor(packColors, 0);
                    Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(color));
                    Window window = this.getWindow();
                    window.setStatusBarColor(color);
                    binding.getRoot().setBackgroundColor(colorBackground);
                }
                colors.recycle();
                colorsBackground.recycle();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }

        //Get cards
        if (cardsList == null) {
            FormatCards formatCards = new FormatCards(this);
            if (collectionNo == -1 && packNo == -1) {
                cardsList = dbHelperGet.getAllCardsWithMeta();
            } else if (packNo == -1) {
                cardsList = dbHelperGet.getAllCardsByCollectionWithMeta(collectionNo);
            } else if (packNo == -2) {
                cardsList = dbHelperGet.getAllCardsByPacksAndProgressWithMeta(packNos, progressGreater, progressNumber);
            } else if (packNo == -3) {
                cardsList = dbHelperGet.getAllCardsByProgressWithMeta(progressGreater, progressNumber);
            } else {
                cardsList = dbHelperGet.getAllCardsByPackWithMeta(packNo);
            }
            formatCards.formatCards(cardsList);
            sortList();
        }

        //Warning: Big lists
        if (cardsList.size() > Globals.LIST_ACCURATE_SIZE) {
            SharedPreferences config = this.getSharedPreferences(Globals.CONFIG_NAME, Context.MODE_PRIVATE);
            boolean formatCards = settings.getBoolean("format_cards", false);
            boolean warnInaccurateFormat = formatCards && config.getBoolean("inaccurate_warning_format", true);
            boolean warnInaccurateNoFormat = !formatCards && config.getBoolean("inaccurate_warning_no_format", true);
            if (warnInaccurateFormat || warnInaccurateNoFormat) {
                SharedPreferences.Editor configEditor = config.edit();
                Dialog infoDialog = new Dialog(this, R.style.dia_view);
                DiaInfoBinding bindingInfoDialog = DiaInfoBinding.inflate(getLayoutInflater());
                infoDialog.setContentView(bindingInfoDialog.getRoot());
                infoDialog.setTitle(getResources().getString(R.string.info));
                infoDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
                List<String> warnings = new ArrayList<>();
                warnings.add(String.format(getResources().getString(R.string.warn_inaccurate), Globals.LIST_ACCURATE_SIZE));
                if (warnInaccurateFormat) {
                    configEditor.putBoolean("inaccurate_warning_format", false);
                    warnings.add(getResources().getString(R.string.warn_inaccurate_list));
                } else {
                    configEditor.putBoolean("inaccurate_warning_no_format", false);
                }
                warnings.add(getResources().getString(R.string.warn_inaccurate_search));
                warnings.add(getResources().getString(R.string.warn_inaccurate_note));
                bindingInfoDialog.diaInfoText.setText(String.join(System.getProperty("line.separator") + System.getProperty("line.separator"), warnings));
                infoDialog.show();
                configEditor.apply();
            }
        }

        //Display content
        updateContent();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_cards, menu);
        MenuItem startNewCardMenuItem = menu.findItem(R.id.start_new_card);
        MenuItem startPackDetailsMenuItem = menu.findItem(R.id.pack_details);
        if (packNo < 0) {
            startNewCardMenuItem.setVisible(false);
            startPackDetailsMenuItem.setVisible(false);
        } else {
            startNewCardMenuItem.setOnMenuItemClickListener(v -> {
                Intent intent = new Intent(this, NewCard.class);
                intent.putExtra("pack", packNo);
                this.startActivity(intent);
                return false;
            });
            startPackDetailsMenuItem.setOnMenuItemClickListener(v -> {
                Intent intent = new Intent(this, ViewPack.class);
                intent.putExtra("collection", collectionNo);
                intent.putExtra("pack", packNo);
                this.startActivity(intent);
                return false;
            });
        }
        changeFrontBackMenuItem = menu.findItem(R.id.change_front_back);
        changeFrontBackMenuItem.setOnMenuItemClickListener(v -> {
            frontBackReverse = !frontBackReverse;
            updateContent();
            return false;
        });
        changeListSortMenuItem = menu.findItem(R.id.sort_random);
        changeListSortMenuItem.setOnMenuItemClickListener(v -> {
            listSort++;
            sortList();
            updateContent(true);
            return false;
        });
        showQueryModeMenuItem = menu.findItem(R.id.start_query);
        showQueryModeMenuItem.setOnMenuItemClickListener(v -> {
            Dialog queryModeDialog = new Dialog(this, R.style.dia_view);
            DiaQueryBinding bindingQueryModeDialog = DiaQueryBinding.inflate(getLayoutInflater());
            queryModeDialog.setContentView(bindingQueryModeDialog.getRoot());
            queryModeDialog.setTitle(getResources().getString(R.string.query_mode));
            queryModeDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            dbHelperUpdate = new DB_Helper_Update(this);
            cardPosition = Math.min(Math.min(((LinearLayoutManager) Objects.requireNonNull(binding.recDefault.getLayoutManager())).findFirstVisibleItemPosition(), cardsListFiltered.size() - 1), Objects.requireNonNull(binding.recDefault.getAdapter()).getItemCount() - 1);
            nextQuery(queryModeDialog, bindingQueryModeDialog);
            queryModeDialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    queryModeDialog.dismiss();
                    binding.recDefault.smoothScrollToPosition(Math.min(cardPosition, Objects.requireNonNull(binding.recDefault.getAdapter()).getItemCount() - 1));
                    return true;
                }
                return false;
            });
            queryModeDialog.show();
            return false;
        });
        showListStatsMenuItem = menu.findItem(R.id.show_list_stats);
        showListStatsMenuItem.setOnMenuItemClickListener(v -> {
            Dialog listStatsDialog = new Dialog(this, R.style.dia_view);
            DiaListStatsBinding bindingListStatsDialog = DiaListStatsBinding.inflate(getLayoutInflater());
            listStatsDialog.setContentView(bindingListStatsDialog.getRoot());
            listStatsDialog.setTitle(getResources().getString(R.string.list_stats));
            listStatsDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);

            bindingListStatsDialog.listStatCardsTotalContent.setText(Integer.toString(cardsListFiltered.size()));

            int statTotalProgress = cardsListFiltered.stream().mapToInt(c -> c.card.known).sum();
            bindingListStatsDialog.listStatProgressTotalContent.setText(Integer.toString(statTotalProgress));
            int statMaxProgress = cardsListFiltered.stream().mapToInt(c -> c.card.known).max().orElse(0);
            bindingListStatsDialog.listStatProgressMaxContent.setText(Integer.toString(statMaxProgress));
            int statMinProgress = cardsListFiltered.stream().mapToInt(c -> c.card.known).min().orElse(0);
            bindingListStatsDialog.listStatProgressMinContent.setText(Integer.toString(statMinProgress));
            double statAvgProgress = cardsListFiltered.stream().mapToInt(c -> c.card.known).average().orElse(0);
            statAvgProgress = Math.round(statAvgProgress * 100.0) / 100.0;
            bindingListStatsDialog.listStatProgressAvgContent.setText(Double.toString(statAvgProgress));

            int statProgressTableIs0 = (int) cardsListFiltered.stream().filter(c -> c.card.known == 0).count();
            int statProgressTableIs1 = (int) cardsListFiltered.stream().filter(c -> c.card.known == 1).count();
            int statProgressTableIs2 = (int) cardsListFiltered.stream().filter(c -> c.card.known == 2).count();
            int statProgressTableIs3 = (int) cardsListFiltered.stream().filter(c -> c.card.known == 3).count();
            int statProgressTableIs4 = (int) cardsListFiltered.stream().filter(c -> c.card.known == 4).count();
            int statProgressTableIs5OrMore = (int) cardsListFiltered.stream().filter(c -> c.card.known >= 5).count();
            float percentCurrent = statProgressTableIs0 / ((float) cardsListFiltered.size());
            bindingListStatsDialog.listStatProgressCounterNumber0.setText(Integer.toString(statProgressTableIs0));
            bindingListStatsDialog.listStatProgressCounterPercent0.setText(Integer.toString(Math.round(percentCurrent * 100)));
            percentCurrent = statProgressTableIs1 / ((float) cardsListFiltered.size());
            bindingListStatsDialog.listStatProgressCounterNumber1.setText(Integer.toString(statProgressTableIs1));
            bindingListStatsDialog.listStatProgressCounterPercent1.setText(Integer.toString(Math.round(percentCurrent * 100)));
            percentCurrent = statProgressTableIs2 / ((float) cardsListFiltered.size());
            bindingListStatsDialog.listStatProgressCounterNumber2.setText(Integer.toString(statProgressTableIs2));
            bindingListStatsDialog.listStatProgressCounterPercent2.setText(Integer.toString(Math.round(percentCurrent * 100)));
            percentCurrent = statProgressTableIs3 / ((float) cardsListFiltered.size());
            bindingListStatsDialog.listStatProgressCounterNumber3.setText(Integer.toString(statProgressTableIs3));
            bindingListStatsDialog.listStatProgressCounterPercent3.setText(Integer.toString(Math.round(percentCurrent * 100)));
            percentCurrent = statProgressTableIs4 / ((float) cardsListFiltered.size());
            bindingListStatsDialog.listStatProgressCounterNumber4.setText(Integer.toString(statProgressTableIs4));
            bindingListStatsDialog.listStatProgressCounterPercent4.setText(Integer.toString(Math.round(percentCurrent * 100)));
            percentCurrent = statProgressTableIs5OrMore / ((float) cardsListFiltered.size());
            bindingListStatsDialog.listStatProgressCounterNumber5.setText(Integer.toString(statProgressTableIs5OrMore));
            bindingListStatsDialog.listStatProgressCounterPercent5.setText(Integer.toString(Math.round(percentCurrent * 100)));

            listStatsDialog.show();
            return false;
        });
        searchCardsMenuItem = menu.findItem(R.id.search_cards);
        SearchView searchView = (SearchView) searchCardsMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchCardsMenuItem.collapseActionView();
                searchCardsOffMenuItem.setVisible(true);
                searchQuery = query;
                binding.recDefault.scrollToPosition(0);
                updateContent();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchCardsOffMenuItem = menu.findItem(R.id.search_cards_off);
        searchCardsOffMenuItem.setOnMenuItemClickListener(v -> {
            searchQuery = "";
            searchCardsOffMenuItem.setVisible(false);
            binding.recDefault.scrollToPosition(0);
            updateContent();
            return false;
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (listSort == Globals.SORT_RANDOM) {
            changeListSortMenuItem.setTitle(R.string.sort_alphabetical);
        } else if (listSort == Globals.SORT_ALPHABETICAL) {
            changeListSortMenuItem.setTitle(R.string.sort_normal);
        } else {
            listSort = Globals.SORT_DEFAULT;
            changeListSortMenuItem.setTitle(R.string.sort_random);
        }
        showQueryModeMenuItem.setVisible(cardsListFiltered.size() > 0);
        showListStatsMenuItem.setVisible(cardsListFiltered.size() > 0);
        changeFrontBackMenuItem.setTitle(frontBackReverse ? R.string.change_back_front : R.string.change_front_back);
        changeFrontBackMenuItem.setVisible(cardsListFiltered.size() > 0);
        changeListSortMenuItem.setVisible(cardsListFiltered.size() > 1);
        searchCardsMenuItem.setVisible(cardsListFiltered.size() > 0);
        if (searchQuery != null && !searchQuery.isEmpty()) {
            searchCardsOffMenuItem.setVisible(true);
        }
        return true;
    }

    private void sortList() {
        new SortCards().sortCards(cardsList, listSort);
    }

    private void queryModeSkipAction(Dialog queryModeDialog, DiaQueryBinding bindingQueryModeDialog) {
        cardPosition++;
        if (cardPosition >= cardsListFiltered.size()) {
            queryModeDialog.dismiss();
            binding.recDefault.scrollToPosition(0);
        } else {
            nextQuery(queryModeDialog, bindingQueryModeDialog);
        }
    }

    private void queryModePreviousAction(Dialog queryModeDialog, DiaQueryBinding bindingQueryModeDialog) {
        cardPosition--;
        if (cardPosition < 0) {
            queryModeDialog.dismiss();
            binding.recDefault.scrollToPosition(0);
        } else {
            nextQuery(queryModeDialog, bindingQueryModeDialog);
        }
    }

    private void queryModeCardKnownChanged(DB_Card card, int known) {
        card.known = known;
        dbHelperUpdate.updateCard(card);
        adapter.notifyItemChanged(cardPosition);
    }

    private void queryModePlusAction(Dialog queryModeDialog, DiaQueryBinding bindingQueryModeDialog, DB_Card card) {
        int known = card.known + 1;
        queryModeCardKnownChanged(card, known);
        cardPosition++;
        if (cardPosition >= cardsListFiltered.size()) {
            queryModeDialog.dismiss();
            binding.recDefault.scrollToPosition(0);
        } else {
            nextQuery(queryModeDialog, bindingQueryModeDialog);
        }
    }

    private void queryModeMinusAction(Dialog queryModeDialog, DiaQueryBinding bindingQueryModeDialog, DB_Card card) {
        int known = Math.max(0, card.known - 1);
        queryModeCardKnownChanged(card, known);
        cardPosition++;
        if (cardPosition >= cardsListFiltered.size()) {
            queryModeDialog.dismiss();
            binding.recDefault.scrollToPosition(0);
        } else {
            nextQuery(queryModeDialog, bindingQueryModeDialog);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void nextQuery(Dialog queryModeDialog, DiaQueryBinding bindingQueryModeDialog) {
        LayerDrawable rootBackground = (LayerDrawable) bindingQueryModeDialog.getRoot().getBackground();
        try {
            DB_Card_With_Meta cardWithMeta = cardsListFiltered.get(cardPosition);
            DB_Card card = cardWithMeta.card;

            try {
                TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
                TypedArray colorsBackgroundHighlight = getResources()
                        .obtainTypedArray(R.array.pack_color_background_highlight);
                int packColors = cardWithMeta.packColor;
                if (packColors < Math.min(colorsBackground.length(),
                        colorsBackgroundHighlight.length()) && packColors >= 0) {
                    int colorBackground = colorsBackground.getColor(packColors, 0);
                    int colorBackgroundHighlight = colorsBackgroundHighlight.getColor(packColors, 0);
                    GradientDrawable rootBackgroundMain = (GradientDrawable) rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_main);
                    rootBackgroundMain.setColor(colorBackground);
                    bindingQueryModeDialog.queryHide.setBackgroundColor(colorBackgroundHighlight);
                }
                colorsBackground.recycle();
                colorsBackgroundHighlight.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }

            SpannableString front;
            SpannableString back;
            StringTools formatString = new StringTools();
            if (settings.getBoolean("format_cards", false)) {
                front = frontBackReverse ? formatString.format(card.back) : formatString.format(card.front);
                back = frontBackReverse ? formatString.format(card.front) : formatString.format(card.back);
            } else {
                String frontString = frontBackReverse ? card.back : card.front;
                String backString = frontBackReverse ? card.front : card.back;
                front = new SpannableString(frontString);
                back = new SpannableString(backString);
            }
            bindingQueryModeDialog.queryShow.setText(front);
            bindingQueryModeDialog.queryHide.setText(back);
            bindingQueryModeDialog.queryHide.setVisibility(View.GONE);

            ArrayList<DB_Media_Link_Card> imageList = (ArrayList<DB_Media_Link_Card>) dbHelperGet.getImageMediaLinksByCard(card.uid);
            if (imageList.isEmpty()) {
                bindingQueryModeDialog.queryButtonMediaImage.setVisibility(View.INVISIBLE);
            } else {
                bindingQueryModeDialog.queryButtonMediaImage.setVisibility(View.VISIBLE);
                bindingQueryModeDialog.queryButtonMediaImage.setOnClickListener(v -> showImageListDialog(imageList));
            }

            ArrayList<DB_Media_Link_Card> mediaList = (ArrayList<DB_Media_Link_Card>) dbHelperGet.getAllMediaLinksByCard(card.uid);
            if (mediaList.isEmpty()) {
                bindingQueryModeDialog.queryButtonMediaOther.setVisibility(View.INVISIBLE);
            } else {
                bindingQueryModeDialog.queryButtonMediaOther.setVisibility(View.VISIBLE);
                bindingQueryModeDialog.queryButtonMediaOther.setOnClickListener(v -> showMediaListDialog(mediaList));
            }

            if (card.notes == null || card.notes.isEmpty()) {
                bindingQueryModeDialog.queryButtonNotes.setVisibility(View.INVISIBLE);
            } else {
                bindingQueryModeDialog.queryButtonNotes.setVisibility(View.VISIBLE);
                bindingQueryModeDialog.queryButtonNotes.setOnClickListener(v -> {
                    Dialog infoDialog = new Dialog(this, R.style.dia_view);
                    DiaInfoBinding bindingInfoDialog = DiaInfoBinding.inflate(getLayoutInflater());
                    infoDialog.setContentView(bindingInfoDialog.getRoot());
                    infoDialog.setTitle(getResources().getString(R.string.query_notes_title));
                    infoDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
                    bindingInfoDialog.diaInfoText.setTextIsSelectable(true);
                    if (settings.getBoolean("format_card_notes", false)) {
                        final Markwon markwon = Markwon.builder(this)
                                .usePlugin(LinkifyPlugin.create(
                                        Linkify.WEB_URLS
                                ))
                                .build();
                        bindingInfoDialog.diaInfoText.setMovementMethod(BetterLinkMovementMethod.getInstance());
                        bindingInfoDialog.diaInfoText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        markwon.setMarkdown(bindingInfoDialog.diaInfoText, card.notes);
                    } else {
                        bindingInfoDialog.diaInfoText.setAutoLinkMask(Linkify.WEB_URLS);
                        bindingInfoDialog.diaInfoText.setText(card.notes);
                    }
                    infoDialog.show();
                });
            }

            final GradientDrawable rootBackgroundLeft = (GradientDrawable) rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_left);
            final GradientDrawable rootBackgroundTop = (GradientDrawable) rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_top);
            final GradientDrawable rootBackgroundBottom = (GradientDrawable) rootBackground.findDrawableByLayerId(R.id.dia_query_root_background_bottom);
            rootBackgroundLeft.setAlpha(cardPosition > 0 ? 255 : 0);
            rootBackgroundTop.setAlpha(0);
            rootBackgroundBottom.setAlpha(0);

            bindingQueryModeDialog.getRoot().setOnTouchListener(new SwipeEvents() {
                final int ANIM_GROW_TIME = 150;
                final int ANIM_DISPlAY_TIME = 300;
                boolean allowTouchEvent = true;

                @Override
                public void onMoveX(float distance) {
                    if (allowTouchEvent) {
                        if (distance < 0) {
                            bindingQueryModeDialog.querySwipeNext.getLayoutParams().width = (int) (2 * Math.abs(distance));
                            bindingQueryModeDialog.querySwipeNext.requestLayout();
                        } else if (cardPosition > 0) {
                            bindingQueryModeDialog.querySwipePrevious.getLayoutParams().width = (int) (2 * Math.abs(distance));
                            bindingQueryModeDialog.querySwipePrevious.requestLayout();
                        }
                    }
                }

                @Override
                public void onMoveY(float distance) {
                    if (allowTouchEvent && bindingQueryModeDialog.queryHide.getVisibility() == View.VISIBLE) {
                        if (distance < 0) {
                            bindingQueryModeDialog.querySwipePlus.getLayoutParams().height = (int) (2 * Math.abs(distance));
                            bindingQueryModeDialog.querySwipePlus.requestLayout();
                        } else {
                            bindingQueryModeDialog.querySwipeMinus.getLayoutParams().height = (int) (2 * Math.abs(distance));
                            bindingQueryModeDialog.querySwipeMinus.requestLayout();
                        }
                    }
                }

                @Override
                public void onMoveCancel() {
                    super.onMoveCancel();
                    if (allowTouchEvent) {
                        bindingQueryModeDialog.querySwipeNext.getLayoutParams().width = 0;
                        bindingQueryModeDialog.querySwipeNext.requestLayout();
                        bindingQueryModeDialog.querySwipePrevious.getLayoutParams().width = 0;
                        bindingQueryModeDialog.querySwipePrevious.requestLayout();
                        bindingQueryModeDialog.querySwipeMinus.getLayoutParams().height = 0;
                        bindingQueryModeDialog.querySwipeMinus.requestLayout();
                        bindingQueryModeDialog.querySwipePlus.getLayoutParams().height = 0;
                        bindingQueryModeDialog.querySwipePlus.requestLayout();
                    }
                }

                @Override
                public void onSwipeLeft() {
                    if (allowTouchEvent) {
                        allowTouchEvent = false;
                        ValueAnimator growAnimator = ValueAnimator.ofInt(bindingQueryModeDialog.querySwipeNext.getWidth(), bindingQueryModeDialog.getRoot().getWidth());
                        growAnimator.setDuration(ANIM_GROW_TIME);
                        growAnimator.addUpdateListener(animation -> {
                            bindingQueryModeDialog.querySwipeNext.getLayoutParams().width = (int) animation.getAnimatedValue();
                            bindingQueryModeDialog.querySwipeNext.requestLayout();
                        });
                        growAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                new Handler(Looper.getMainLooper()).postDelayed(
                                        () -> {
                                            queryModeSkipAction(queryModeDialog, bindingQueryModeDialog);
                                            allowTouchEvent = true;
                                            onMoveCancel();
                                        },
                                        ANIM_DISPlAY_TIME);
                            }
                        });
                        growAnimator.start();
                    } else {
                        onMoveCancel();
                    }
                }

                @Override
                public void onSwipeRight() {
                    if (allowTouchEvent && cardPosition > 0) {
                        allowTouchEvent = false;
                        ValueAnimator growAnimator = ValueAnimator.ofInt(bindingQueryModeDialog.querySwipePrevious.getWidth(), bindingQueryModeDialog.getRoot().getWidth());
                        growAnimator.setDuration(ANIM_GROW_TIME);
                        growAnimator.addUpdateListener(animation -> {
                            bindingQueryModeDialog.querySwipePrevious.getLayoutParams().width = (int) animation.getAnimatedValue();
                            bindingQueryModeDialog.querySwipePrevious.requestLayout();
                        });
                        growAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                new Handler(Looper.getMainLooper()).postDelayed(
                                        () -> {
                                            queryModePreviousAction(queryModeDialog, bindingQueryModeDialog);
                                            allowTouchEvent = true;
                                            onMoveCancel();
                                        },
                                        ANIM_DISPlAY_TIME);
                            }
                        });
                        growAnimator.start();
                    } else {
                        onMoveCancel();
                    }
                }

                @Override
                public void onSwipeTop() {
                    if (allowTouchEvent && bindingQueryModeDialog.queryHide.getVisibility() == View.VISIBLE) {
                        allowTouchEvent = false;
                        ValueAnimator growAnimator = ValueAnimator.ofInt(bindingQueryModeDialog.querySwipePlus.getHeight(), bindingQueryModeDialog.getRoot().getHeight());
                        growAnimator.setDuration(ANIM_GROW_TIME);
                        growAnimator.addUpdateListener(animation -> {
                            bindingQueryModeDialog.querySwipePlus.getLayoutParams().height = (int) animation.getAnimatedValue();
                            bindingQueryModeDialog.querySwipePlus.requestLayout();
                        });
                        growAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                new Handler(Looper.getMainLooper()).postDelayed(
                                        () -> {
                                            queryModePlusAction(queryModeDialog, bindingQueryModeDialog, card);
                                            allowTouchEvent = true;
                                            onMoveCancel();
                                        },
                                        ANIM_DISPlAY_TIME);
                            }
                        });
                        growAnimator.start();
                    } else {
                        onMoveCancel();
                    }
                }

                @Override
                public void onSwipeBottom() {
                    if (allowTouchEvent && bindingQueryModeDialog.queryHide.getVisibility() == View.VISIBLE) {
                        allowTouchEvent = false;
                        ValueAnimator growAnimator = ValueAnimator.ofInt(bindingQueryModeDialog.querySwipeMinus.getHeight(), bindingQueryModeDialog.getRoot().getHeight());
                        growAnimator.setDuration(ANIM_GROW_TIME);
                        growAnimator.addUpdateListener(animation -> {
                            bindingQueryModeDialog.querySwipeMinus.getLayoutParams().height = (int) animation.getAnimatedValue();
                            bindingQueryModeDialog.querySwipeMinus.requestLayout();
                        });
                        growAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                new Handler(Looper.getMainLooper()).postDelayed(
                                        () -> {
                                            queryModeMinusAction(queryModeDialog, bindingQueryModeDialog, card);
                                            allowTouchEvent = true;
                                            onMoveCancel();
                                        },
                                        ANIM_DISPlAY_TIME);
                            }
                        });
                        growAnimator.start();
                    } else {
                        onMoveCancel();
                    }
                }
            });

            bindingQueryModeDialog.queryPlus.setVisibility(View.GONE);
            bindingQueryModeDialog.queryMinus.setVisibility(View.GONE);

            bindingQueryModeDialog.querySkip.setOnClickListener(vv -> queryModeSkipAction(queryModeDialog, bindingQueryModeDialog));
            if (cardPosition == 0) {
                bindingQueryModeDialog.queryBack.setVisibility(View.INVISIBLE);
            } else {
                bindingQueryModeDialog.queryBack.setVisibility(View.VISIBLE);
                bindingQueryModeDialog.queryBack.setOnClickListener(vv -> queryModePreviousAction(queryModeDialog, bindingQueryModeDialog));
            }

            bindingQueryModeDialog.queryButtonHide.setVisibility(View.VISIBLE);
            bindingQueryModeDialog.queryButtonHide.setOnClickListener(v -> {
                bindingQueryModeDialog.queryButtonHide.setVisibility(View.GONE);
                bindingQueryModeDialog.queryHide.setVisibility(View.VISIBLE);
                bindingQueryModeDialog.queryPlus.setVisibility(View.VISIBLE);
                bindingQueryModeDialog.queryMinus.setVisibility(View.VISIBLE);
                bindingQueryModeDialog.queryPlus.setOnClickListener(vv -> queryModePlusAction(queryModeDialog, bindingQueryModeDialog, card));
                bindingQueryModeDialog.queryMinus.setOnClickListener(vv -> queryModeMinusAction(queryModeDialog, bindingQueryModeDialog, card));
                rootBackgroundTop.setAlpha(255);
                rootBackgroundBottom.setAlpha(255);
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public void updateContent(boolean recreate) {
        List<DB_Card_With_Meta> tempCardList = new ArrayList<>(cardsList);
        //Search
        if (searchQuery != null && !searchQuery.isEmpty()) {
            SearchCards searchCards = new SearchCards();
            List<DB_Card_With_Meta> searchResults = new ArrayList<>(tempCardList);
            searchCards.searchCards(searchResults, searchQuery);
            if (searchResults.size() == 0) {
                searchCardsOffMenuItem.setVisible(false);
                searchQuery = "";
                Toast.makeText(this, R.string.search_no_results, Toast.LENGTH_LONG).show();
            } else {
                tempCardList = searchResults;
            }
        }
        //Set recycler view
        if (adapter == null || cardsListFiltered == null || recreate || cardsListFiltered.size() == 0 || tempCardList.size() == 0) {
            cardsListFiltered = tempCardList;
            adapter = new AdapterCards(cardsListFiltered, settings.getBoolean("ui_font_size", false), frontBackReverse, packNo, collectionNo);
            binding.recDefault.setAdapter(adapter);
            binding.recDefault.setLayoutManager(new LinearLayoutManager(this));
            binding.recDefault.scrollToPosition(0);
        } else {
            adapter.updateContent(tempCardList, frontBackReverse);
        }
        invalidateOptionsMenu();
    }

    private void updateContent() {
        updateContent(false);
    }
}
