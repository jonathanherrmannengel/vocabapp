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
import java.util.Random;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterCards;
import de.herrmann_engel.rbv.databinding.ActivityDefaultRecBinding;
import de.herrmann_engel.rbv.databinding.DiaConfirmBinding;
import de.herrmann_engel.rbv.databinding.DiaInfoBinding;
import de.herrmann_engel.rbv.databinding.DiaListStatsBinding;
import de.herrmann_engel.rbv.databinding.DiaQueryBinding;
import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update;
import de.herrmann_engel.rbv.utils.SearchCards;
import de.herrmann_engel.rbv.utils.SortCards;
import de.herrmann_engel.rbv.utils.StringTools;
import de.herrmann_engel.rbv.utils.SwipeEvents;
import io.noties.markwon.Markwon;
import io.noties.markwon.linkify.LinkifyPlugin;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class ListCards extends FileTools {
    MenuItem changeFrontBackItem;
    MenuItem sortRandomItem;
    MenuItem searchCardsItem;
    MenuItem searchCardsOffItem;
    MenuItem queryModeItem;
    MenuItem listStatsItem;
    private ActivityDefaultRecBinding binding;
    private SharedPreferences settings;
    private DB_Helper_Get dbHelperGet;
    private DB_Helper_Update dbHelperUpdate;
    private boolean saveList;
    private int collectionNo;
    private int packNo;
    private ArrayList<Integer> packNos;
    private boolean reverse;
    private int sort;
    private String searchQuery;
    private int cardPosition;
    private boolean progressGreater;
    private int progressNumber;
    private ArrayList<Integer> savedList;
    private Long savedListSeed;
    private List<DB_Card> cardsList;
    private List<DB_Card> originalCardsList;
    private List<DB_Card> currentCardsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDefaultRecBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        saveList = settings.getBoolean("list_no_update", true);

        collectionNo = getIntent().getExtras().getInt("collection");
        packNo = getIntent().getExtras().getInt("pack");
        packNos = getIntent().getExtras().getIntegerArrayList("packs");
        reverse = getIntent().getExtras().getBoolean("reverse");
        sort = getIntent().getExtras().getInt("sort", settings.getInt("default_sort", Globals.SORT_DEFAULT));
        searchQuery = getIntent().getExtras().getString("searchQuery");
        cardPosition = getIntent().getExtras().getInt("cardPosition");
        progressGreater = getIntent().getExtras().getBoolean("progressGreater");
        progressNumber = getIntent().getExtras().getInt("progressNumber");
        savedList = getIntent().getExtras().getIntegerArrayList("savedList");
        savedListSeed = getIntent().getExtras().getLong("savedListSeed");

        if (settings.getBoolean("ui_bg_images", true)) {
            binding.backgroundImage.setVisibility(View.VISIBLE);
            binding.backgroundImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.bg_cards));
        }
    }

    @Override
    protected void notifyFolderSet() {
    }

    @Override
    protected void notifyMissingAction(int id) {
        try {
            Intent intent = new Intent(this, EditCardMedia.class);
            intent.putExtra("collection", collectionNo);
            intent.putExtra("pack", packNo);
            intent.putIntegerArrayListExtra("packs", packNos);
            intent.putExtra("card", id);
            intent.putExtra("reverse", reverse);
            intent.putExtra("sort", sort);
            intent.putExtra("searchQuery", searchQuery);
            intent.putExtra("cardPosition", 0);
            intent.putExtra("progressGreater", progressGreater);
            intent.putExtra("progressNumber", progressNumber);
            intent.putIntegerArrayListExtra("savedList", savedList);
            intent.putExtra("savedListSeed", savedListSeed);
            this.startActivity(intent);
            this.finish();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_cards, menu);

        //Menu items
        if (packNo < 0) {
            MenuItem startNewCard = menu.findItem(R.id.start_new_card);
            startNewCard.setVisible(false);
            MenuItem packDetails = menu.findItem(R.id.pack_details);
            packDetails.setVisible(false);
        }
        changeFrontBackItem = menu.findItem(R.id.change_front_back);
        sortRandomItem = menu.findItem(R.id.sort_random);
        queryModeItem = menu.findItem(R.id.start_query);
        listStatsItem = menu.findItem(R.id.show_list_stats);
        searchCardsItem = menu.findItem(R.id.search_cards);
        searchCardsOffItem = menu.findItem(R.id.search_cards_off);
        SearchView searchView = (SearchView) searchCardsItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchCardsOffItem.setVisible(true);
                searchQuery = query;
                cardPosition = 0;
                setRecView();
                searchCardsItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        if (searchQuery != null && !searchQuery.isEmpty()) {
            searchCardsOffItem.setVisible(true);
        }

        //Get cards
        if (saveList && savedList != null) {
            originalCardsList = dbHelperGet.getAllCardsByIds(savedList);
        } else if (collectionNo == -1 && packNo == -1) {
            originalCardsList = dbHelperGet.getAllCards();
        } else if (packNo == -1) {
            originalCardsList = dbHelperGet.getAllCardsByCollection(collectionNo);
        } else if (packNo == -2) {
            originalCardsList = dbHelperGet.getAllCardsByPacksAndProgress(packNos, progressGreater, progressNumber);
        } else if (packNo == -3) {
            originalCardsList = dbHelperGet.getAllCardsByProgress(progressGreater, progressNumber);
        } else {
            originalCardsList = dbHelperGet.getAllCardsByPack(packNo);
        }
        if (saveList && savedList == null && savedListSeed == 0) {
            cardPosition = 0;
            if (originalCardsList.size() > Globals.LIST_ACCURATE_SIZE) {
                generateSavedListSeed();
                sortList();
            } else {
                sortList();
                generateSavedList();
            }
        } else if (!saveList) {
            cardPosition = 0;
            sortList();
        } else {
            sortList();
        }

        //Warning: Big lists
        if (cardsList.size() > Globals.LIST_ACCURATE_SIZE) {
            SharedPreferences config = this.getSharedPreferences(Globals.CONFIG_NAME, Context.MODE_PRIVATE);
            boolean warnInaccurateSaved = config.getBoolean("inaccurate_warning_saved", true);
            boolean warnInaccurateFormat = config.getBoolean("inaccurate_warning_format", true);
            boolean formatCardsOrNotes = settings.getBoolean("format_cards", false) || settings.getBoolean("format_card_notes", false);
            if ((saveList && warnInaccurateSaved) || (formatCardsOrNotes && warnInaccurateFormat)) {
                SharedPreferences.Editor configEditor = config.edit();
                Dialog infoDialog = new Dialog(this, R.style.dia_view);
                DiaInfoBinding bindingInfoDialog = DiaInfoBinding.inflate(getLayoutInflater());
                infoDialog.setContentView(bindingInfoDialog.getRoot());
                infoDialog.setTitle(getResources().getString(R.string.info));
                infoDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
                List<String> warnings = new ArrayList<>();
                warnings.add(String.format(getResources().getString(R.string.warn_inaccurate), Globals.LIST_ACCURATE_SIZE));
                if (saveList) {
                    configEditor.putBoolean("inaccurate_warning_saved", false);
                    warnings.add(getResources().getString(R.string.warn_inaccurate_saved));
                }
                if (formatCardsOrNotes) {
                    configEditor.putBoolean("inaccurate_warning_format", false);
                    warnings.add(getResources().getString(R.string.warn_inaccurate_format));
                }
                warnings.add(getResources().getString(R.string.warn_inaccurate_note));
                bindingInfoDialog.diaInfoText.setText(String.join(System.getProperty("line.separator") + System.getProperty("line.separator"), warnings));
                infoDialog.show();
                configEditor.apply();
            }
        }

        //Display content
        setRecView();
        return true;
    }

    private void generateSavedList() {
        savedList = new ArrayList<>();
        cardsList.forEach(card -> savedList.add(card.uid));
    }

    private void generateSavedListSeed() {
        savedListSeed = new Random().nextLong();
    }

    private void sortList() {
        if (savedList != null) {
            cardsList = originalCardsList;
        } else if (savedListSeed != 0 && sort == Globals.SORT_RANDOM) {
            Random random = new Random();
            random.setSeed(savedListSeed);
            cardsList = new SortCards().sortCards(originalCardsList, sort, random);
        } else {
            cardsList = new SortCards().sortCards(originalCardsList, sort);
        }
    }

    public void searchCardsOff(MenuItem menuItem) {
        searchCardsOffItem.setVisible(false);
        searchQuery = "";
        cardPosition = 0;
        setRecView();
    }

    private void queryModeSkipAction(Dialog queryModeDialog, DiaQueryBinding bindingQueryModeDialog) {
        cardPosition++;
        if (cardPosition >= currentCardsList.size()) {
            cardPosition = 0;
            if (!saveList) {
                sortList();
            }
            setRecView();
            queryModeDialog.dismiss();
        } else {
            nextQuery(queryModeDialog, bindingQueryModeDialog);
        }
    }

    private void queryModePreviousAction(Dialog queryModeDialog, DiaQueryBinding bindingQueryModeDialog) {
        cardPosition--;
        if (cardPosition < 0) {
            cardPosition = 0;
            if (!saveList) {
                sortList();
            }
            setRecView();
            queryModeDialog.dismiss();
        } else {
            nextQuery(queryModeDialog, bindingQueryModeDialog);
        }
    }

    private void queryModePlusAction(Dialog queryModeDialog, DiaQueryBinding bindingQueryModeDialog, DB_Card card) {
        int known = card.known + 1;
        cardsList.stream().filter(i -> i.uid == card.uid).findFirst().get().known = known;
        originalCardsList.stream().filter(i -> i.uid == card.uid).findFirst().get().known = known;
        card.known = known;
        dbHelperUpdate.updateCard(card);
        cardPosition++;
        if (cardPosition >= currentCardsList.size()) {
            cardPosition = 0;
            if (!saveList) {
                sortList();
            }
            setRecView();
            queryModeDialog.dismiss();
        } else {
            nextQuery(queryModeDialog, bindingQueryModeDialog);
        }
    }

    private void queryModeMinusAction(Dialog queryModeDialog, DiaQueryBinding bindingQueryModeDialog, DB_Card card) {
        int known = Math.max(0, card.known - 1);
        cardsList.stream().filter(i -> i.uid == card.uid).findFirst().get().known = known;
        originalCardsList.stream().filter(i -> i.uid == card.uid).findFirst().get().known = known;
        card.known = known;
        dbHelperUpdate.updateCard(card);
        cardPosition++;
        if (cardPosition >= currentCardsList.size()) {
            cardPosition = 0;
            if (!saveList) {
                sortList();
            }
            setRecView();
            queryModeDialog.dismiss();
        } else {
            nextQuery(queryModeDialog, bindingQueryModeDialog);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void nextQuery(Dialog queryModeDialog, DiaQueryBinding bindingQueryModeDialog) {
        LayerDrawable rootBackground = (LayerDrawable) bindingQueryModeDialog.getRoot().getBackground();
        try {
            int position = Math.min(cardPosition, currentCardsList.size() - 1);
            DB_Card card = dbHelperGet.getSingleCard(currentCardsList.get(position).uid);

            try {
                TypedArray colorsBackground = getResources().obtainTypedArray(R.array.pack_color_background);
                TypedArray colorsBackgroundHighlight = getResources()
                        .obtainTypedArray(R.array.pack_color_background_highlight);
                int packColors = dbHelperGet.getSinglePack(card.pack).colors;
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
                front = reverse ? formatString.format(card.back) : formatString.format(card.front);
                back = reverse ? formatString.format(card.front) : formatString.format(card.back);
            } else {
                String frontString = reverse ? card.back : card.front;
                String backString = reverse ? card.front : card.back;
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
            rootBackgroundLeft.setAlpha(position > 0 ? 255 : 0);
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
                        } else if (position > 0) {
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
                    if (allowTouchEvent && position > 0) {
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
                        @SuppressLint("ClickableViewAccessibility") ValueAnimator growAnimator = ValueAnimator.ofInt(bindingQueryModeDialog.querySwipeMinus.getHeight(), bindingQueryModeDialog.getRoot().getHeight());
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
            if (position == 0) {
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

    private void setCardPosition() {
        cardPosition = ((LinearLayoutManager) Objects.requireNonNull(binding.recDefault.getLayoutManager()))
                .findFirstVisibleItemPosition();
        cardPosition = Math.min(cardPosition, Objects.requireNonNull(binding.recDefault.getAdapter()).getItemCount() - 1);
    }

    public void startQueryMode(MenuItem menuItem) {
        Dialog queryModeDialog = new Dialog(this, R.style.dia_view);
        DiaQueryBinding bindingQueryModeDialog = DiaQueryBinding.inflate(getLayoutInflater());
        queryModeDialog.setContentView(bindingQueryModeDialog.getRoot());
        queryModeDialog.setTitle(getResources().getString(R.string.query_mode));
        queryModeDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        dbHelperUpdate = new DB_Helper_Update(this);
        setCardPosition();
        nextQuery(queryModeDialog, bindingQueryModeDialog);
        queryModeDialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (!saveList || (savedList == null && savedListSeed == 0)) {
                    Dialog exitQueryModeDialog = new Dialog(ListCards.this, R.style.dia_view);
                    DiaConfirmBinding bindingExitQueryModeDialog = DiaConfirmBinding.inflate(getLayoutInflater());
                    exitQueryModeDialog.setContentView(bindingExitQueryModeDialog.getRoot());
                    exitQueryModeDialog.setTitle(getResources().getString(R.string.query_mode_exit));
                    exitQueryModeDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
                    bindingExitQueryModeDialog.diaConfirmYes.setOnClickListener(v -> {
                        cardPosition = 0;
                        sortList();
                        setRecView();
                        exitQueryModeDialog.dismiss();
                        queryModeDialog.dismiss();
                    });
                    bindingExitQueryModeDialog.diaConfirmNo.setOnClickListener(v -> exitQueryModeDialog.dismiss());
                    exitQueryModeDialog.show();
                } else {
                    setRecView();
                    queryModeDialog.dismiss();
                }
                return true;
            }
            return false;
        });
        queryModeDialog.show();
    }

    public void showListStats(MenuItem menuItem) {
        Dialog listStatsDialog = new Dialog(this, R.style.dia_view);
        DiaListStatsBinding bindingListStatsDialog = DiaListStatsBinding.inflate(getLayoutInflater());
        listStatsDialog.setContentView(bindingListStatsDialog.getRoot());
        listStatsDialog.setTitle(getResources().getString(R.string.list_stats));
        listStatsDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        bindingListStatsDialog.listStatCardsTotalContent.setText(Integer.toString(currentCardsList.size()));

        int statTotalProgress = currentCardsList.stream().mapToInt(c -> c.known).sum();
        bindingListStatsDialog.listStatProgressTotalContent.setText(Integer.toString(statTotalProgress));
        int statMaxProgress = currentCardsList.stream().mapToInt(c -> c.known).max().orElse(0);
        bindingListStatsDialog.listStatProgressMaxContent.setText(Integer.toString(statMaxProgress));
        int statMinProgress = currentCardsList.stream().mapToInt(c -> c.known).min().orElse(0);
        bindingListStatsDialog.listStatProgressMinContent.setText(Integer.toString(statMinProgress));
        double statAvgProgress = currentCardsList.stream().mapToInt(c -> c.known).average().orElse(0);
        statAvgProgress = Math.round(statAvgProgress * 100.0) / 100.0;
        bindingListStatsDialog.listStatProgressAvgContent.setText(Double.toString(statAvgProgress));

        int statProgressTableIs0 = (int) currentCardsList.stream().filter(c -> c.known == 0).count();
        int statProgressTableIs1 = (int) currentCardsList.stream().filter(c -> c.known == 1).count();
        int statProgressTableIs2 = (int) currentCardsList.stream().filter(c -> c.known == 2).count();
        int statProgressTableIs3 = (int) currentCardsList.stream().filter(c -> c.known == 3).count();
        int statProgressTableIs4 = (int) currentCardsList.stream().filter(c -> c.known == 4).count();
        int statProgressTableIs5OrMore = (int) currentCardsList.stream().filter(c -> c.known >= 5).count();
        float percentCurrent = statProgressTableIs0 / ((float) currentCardsList.size());
        bindingListStatsDialog.listStatProgressCounterNumber0.setText(Integer.toString(statProgressTableIs0));
        bindingListStatsDialog.listStatProgressCounterPercent0.setText(Integer.toString(Math.round(percentCurrent * 100)));
        percentCurrent = statProgressTableIs1 / ((float) currentCardsList.size());
        bindingListStatsDialog.listStatProgressCounterNumber1.setText(Integer.toString(statProgressTableIs1));
        bindingListStatsDialog.listStatProgressCounterPercent1.setText(Integer.toString(Math.round(percentCurrent * 100)));
        percentCurrent = statProgressTableIs2 / ((float) currentCardsList.size());
        bindingListStatsDialog.listStatProgressCounterNumber2.setText(Integer.toString(statProgressTableIs2));
        bindingListStatsDialog.listStatProgressCounterPercent2.setText(Integer.toString(Math.round(percentCurrent * 100)));
        percentCurrent = statProgressTableIs3 / ((float) currentCardsList.size());
        bindingListStatsDialog.listStatProgressCounterNumber3.setText(Integer.toString(statProgressTableIs3));
        bindingListStatsDialog.listStatProgressCounterPercent3.setText(Integer.toString(Math.round(percentCurrent * 100)));
        percentCurrent = statProgressTableIs4 / ((float) currentCardsList.size());
        bindingListStatsDialog.listStatProgressCounterNumber4.setText(Integer.toString(statProgressTableIs4));
        bindingListStatsDialog.listStatProgressCounterPercent4.setText(Integer.toString(Math.round(percentCurrent * 100)));
        percentCurrent = statProgressTableIs5OrMore / ((float) currentCardsList.size());
        bindingListStatsDialog.listStatProgressCounterNumber5.setText(Integer.toString(statProgressTableIs5OrMore));
        bindingListStatsDialog.listStatProgressCounterPercent5.setText(Integer.toString(Math.round(percentCurrent * 100)));

        listStatsDialog.show();
    }

    public void startNewCard(MenuItem menuItem) {
        Intent intent = new Intent(this, NewCard.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", ((LinearLayoutManager) Objects.requireNonNull(binding.recDefault.getLayoutManager()))
                .findFirstVisibleItemPosition());
        intent.putIntegerArrayListExtra("savedList", savedList);
        this.startActivity(intent);
        this.finish();
    }

    public void changeFrontBack(MenuItem menuItem) {
        setCardPosition();
        reverse = !reverse;
        setRecView();
    }

    public void sort(MenuItem menuItem) {
        sort++;
        cardPosition = 0;
        if (saveList && savedListSeed != 0) {
            generateSavedListSeed();
        } else {
            savedListSeed = 0L;
        }
        boolean hadSavedList = savedList != null;
        savedList = null;
        sortList();
        if (saveList && hadSavedList) {
            generateSavedList();
        }
        setRecView();
    }

    public void packDetails(MenuItem menuItem) {
        Intent intent = new Intent(this, ViewPack.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        this.startActivity(intent);
    }

    public void setRecView() {
        currentCardsList = cardsList;
        //Menu Items
        if (sort == Globals.SORT_RANDOM) {
            sortRandomItem.setTitle(R.string.sort_alphabetical);
        } else if (sort == Globals.SORT_ALPHABETICAL) {
            sortRandomItem.setTitle(R.string.sort_normal);
        } else {
            sort = Globals.SORT_DEFAULT;
            sortRandomItem.setTitle(R.string.sort_random);
        }
        queryModeItem.setVisible(currentCardsList.size() > 0);
        listStatsItem.setVisible(currentCardsList.size() > 0);
        changeFrontBackItem.setTitle(reverse ? R.string.change_back_front : R.string.change_front_back);
        changeFrontBackItem.setVisible(currentCardsList.size() > 0);
        sortRandomItem.setVisible(currentCardsList.size() > 1);
        searchCardsItem.setVisible(currentCardsList.size() > 0);
        //Search
        if (searchQuery != null && !searchQuery.isEmpty()) {
            List<DB_Card> cardsListFiltered = new ArrayList<>(currentCardsList);
            SearchCards searchCards = new SearchCards();
            cardsListFiltered = searchCards.searchCards(cardsListFiltered, searchQuery);
            if (cardsListFiltered.size() == 0) {
                searchCardsOffItem.setVisible(false);
                searchQuery = "";
                cardPosition = 0;
                Toast.makeText(this, R.string.search_no_results, Toast.LENGTH_LONG).show();
            } else {
                currentCardsList = cardsListFiltered;
            }
        }
        //Set recycler view
        AdapterCards adapter = new AdapterCards(currentCardsList, reverse, sort, packNo, packNos, searchQuery, collectionNo, progressGreater, progressNumber, savedList, savedListSeed);
        binding.recDefault.setAdapter(adapter);
        binding.recDefault.setLayoutManager(new LinearLayoutManager(this));
        binding.recDefault.scrollToPosition(
                Math.min(cardPosition, Objects.requireNonNull(binding.recDefault.getAdapter()).getItemCount() - 1));
    }
}
