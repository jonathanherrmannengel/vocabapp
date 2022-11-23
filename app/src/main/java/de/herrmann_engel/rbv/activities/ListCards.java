package de.herrmann_engel.rbv.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.R;
import de.herrmann_engel.rbv.adapters.AdapterCards;
import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Media_Link_Card;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Get;
import de.herrmann_engel.rbv.db.utils.DB_Helper_Update;
import de.herrmann_engel.rbv.utils.SearchCards;
import de.herrmann_engel.rbv.utils.SortCards;
import de.herrmann_engel.rbv.utils.StringTools;
import io.noties.markwon.Markwon;

public class ListCards extends FileTools {

    SharedPreferences settings;
    MenuItem changeFrontBackItem;
    MenuItem sortRandomItem;
    MenuItem searchCardsItem;
    MenuItem searchCardsOffItem;
    MenuItem queryModeItem;
    MenuItem listStatsItem;
    RecyclerView recyclerView;
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
        setContentView(R.layout.activity_default_rec);

        settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        saveList = settings.getBoolean("list_no_update", true);
        if (settings.getBoolean("ui_bg_images", true)) {
            ImageView backgroundImage = findViewById(R.id.background_image);
            backgroundImage.setVisibility(View.VISIBLE);
            backgroundImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.bg_cards));
        }
    }

    @Override
    protected void notifyFolderSet() {
    }

    @Override
    protected void notifyMissingAction(int id) {
        try {
            Intent intent = new Intent(this.getApplicationContext(), EditCardMedia.class);
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
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_cards, menu);
        SharedPreferences settings = getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
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

        recyclerView = this.findViewById(R.id.rec_default);

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
        updateCardList();

        if (cardsList.size() > Globals.LIST_ACCURATE_SIZE) {
            SharedPreferences config = this.getSharedPreferences(Globals.CONFIG_NAME, Context.MODE_PRIVATE);
            boolean warnInaccurateSaved = config.getBoolean("inaccurate_warning_saved", true);
            boolean warnInaccurateFormat = config.getBoolean("inaccurate_warning_format", true);
            boolean formatCardsOrNotes = settings.getBoolean("format_cards", false) || settings.getBoolean("format_card_notes", false);
            if ((saveList && warnInaccurateSaved) || (formatCardsOrNotes && warnInaccurateFormat)) {
                SharedPreferences.Editor configEditor = config.edit();
                Dialog info = new Dialog(this, R.style.dia_view);
                info.setContentView(R.layout.dia_info);
                info.setTitle(getResources().getString(R.string.info));
                info.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT);
                TextView infoText = info.findViewById(R.id.dia_info_text);
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
                infoText.setText(String.join(System.getProperty("line.separator") + System.getProperty("line.separator"), warnings));
                info.show();
                configEditor.apply();
            }
        }
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
                    findViewById(R.id.rec_default_root).setBackgroundColor(colorBackground);
                }
                colors.recycle();
                colorsBackground.recycle();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
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
                    findViewById(R.id.rec_default_root).setBackgroundColor(colorBackground);
                }
                colors.recycle();
                colorsBackground.recycle();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
        setRecView();
        return true;
    }

    private void updateCardList() {
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

    private void nextQuery(Dialog queryMode) {
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
                    queryMode.findViewById(R.id.dia_query_root).setBackgroundColor(colorBackground);
                    queryMode.findViewById(R.id.query_hide).setBackgroundColor(colorBackgroundHighlight);
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
            TextView showQuery = queryMode.findViewById(R.id.query_show);
            showQuery.setText(front);

            TextView showSolution = queryMode.findViewById(R.id.query_hide);
            showSolution.setText(back);
            showSolution.setVisibility(View.GONE);

            TextView showImageButton = queryMode.findViewById(R.id.query_button_media_image);
            ArrayList<DB_Media_Link_Card> imageList = (ArrayList<DB_Media_Link_Card>) dbHelperGet.getImageMediaLinksByCard(card.uid);
            if (imageList.isEmpty()) {
                showImageButton.setVisibility(View.INVISIBLE);
            } else {
                showImageButton.setVisibility(View.VISIBLE);
                showImageButton.setOnClickListener(v -> showImageListDialog(imageList));
            }

            TextView showMediaButton = queryMode.findViewById(R.id.query_button_media_other);
            ArrayList<DB_Media_Link_Card> mediaList = (ArrayList<DB_Media_Link_Card>) dbHelperGet.getAllMediaLinksByCard(card.uid);
            if (mediaList.isEmpty()) {
                showMediaButton.setVisibility(View.INVISIBLE);
            } else {
                showMediaButton.setVisibility(View.VISIBLE);
                showMediaButton.setOnClickListener(v -> showMediaListDialog(mediaList));
            }

            TextView showNotesButton = queryMode.findViewById(R.id.query_button_notes);
            if (card.notes == null || card.notes.isEmpty()) {
                showNotesButton.setVisibility(View.INVISIBLE);
            } else {
                showNotesButton.setVisibility(View.VISIBLE);
                showNotesButton.setOnClickListener(v -> {
                    Dialog info = new Dialog(this, R.style.dia_view);
                    info.setContentView(R.layout.dia_info);
                    info.setTitle(getResources().getString(R.string.query_notes_title));
                    info.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
                    TextView infoText = info.findViewById(R.id.dia_info_text);
                    if (settings.getBoolean("format_card_notes", false)) {
                        final Markwon markwon = Markwon.create(this);
                        infoText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        markwon.setMarkdown(infoText, card.notes);
                    } else {
                        infoText.setText(card.notes);
                    }
                    info.show();
                });
            }

            ImageButton plus = queryMode.findViewById(R.id.query_plus);
            ImageButton minus = queryMode.findViewById(R.id.query_minus);
            plus.setVisibility(View.GONE);
            minus.setVisibility(View.GONE);

            ImageButton skip = queryMode.findViewById(R.id.query_skip);
            skip.setOnClickListener(vv -> {
                cardPosition++;
                if (cardPosition >= currentCardsList.size()) {
                    cardPosition = 0;
                    if (!saveList) {
                        updateCardList();
                    }
                    setRecView();
                    queryMode.dismiss();
                } else {
                    nextQuery(queryMode);
                }
            });
            ImageButton previous = queryMode.findViewById(R.id.query_back);
            if (position == 0) {
                previous.setVisibility(View.INVISIBLE);
            } else {
                previous.setVisibility(View.VISIBLE);
                previous.setOnClickListener(vv -> {
                    cardPosition--;
                    if (cardPosition < 0) {
                        cardPosition = 0;
                        if (!saveList) {
                            updateCardList();
                        }
                        setRecView();
                        queryMode.dismiss();
                    } else {
                        nextQuery(queryMode);
                    }
                });
            }

            Button hideQueryButton = queryMode.findViewById(R.id.query_button_hide);
            hideQueryButton.setVisibility(View.VISIBLE);
            hideQueryButton.setOnClickListener(v -> {
                hideQueryButton.setVisibility(View.GONE);
                showSolution.setVisibility(View.VISIBLE);
                plus.setVisibility(View.VISIBLE);
                minus.setVisibility(View.VISIBLE);
                plus.setOnClickListener(vv -> {
                    cardsList.stream().filter(i -> i.uid == card.uid).findFirst().get().known++;
                    card.known++;
                    dbHelperUpdate.updateCard(card);
                    cardPosition++;
                    if (cardPosition >= currentCardsList.size()) {
                        cardPosition = 0;
                        if (!saveList) {
                            updateCardList();
                        }
                        setRecView();
                        queryMode.dismiss();
                    } else {
                        nextQuery(queryMode);
                    }
                });
                minus.setOnClickListener(vv -> {
                    int known = Math.max(0, --card.known);
                    cardsList.stream().filter(i -> i.uid == card.uid).findFirst().get().known = known;
                    card.known = known;
                    dbHelperUpdate.updateCard(card);
                    cardPosition++;
                    if (cardPosition >= currentCardsList.size()) {
                        cardPosition = 0;
                        if (!saveList) {
                            updateCardList();
                        }
                        setRecView();
                        queryMode.dismiss();
                    } else {
                        nextQuery(queryMode);
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setCardPosition() {
        cardPosition = ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager()))
                .findFirstVisibleItemPosition();
        cardPosition = Math.min(cardPosition, Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1);
    }

    public void startQueryMode(MenuItem menuItem) {
        Dialog queryMode = new Dialog(this, R.style.dia_view);
        queryMode.setContentView(R.layout.dia_query);
        queryMode.setTitle(getResources().getString(R.string.query_mode));
        queryMode.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        dbHelperUpdate = new DB_Helper_Update(this);
        setCardPosition();
        nextQuery(queryMode);
        queryMode.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (!saveList || (savedList == null && savedListSeed == 0)) {
                    Dialog exitQueryModeDialog = new Dialog(ListCards.this, R.style.dia_view);
                    exitQueryModeDialog.setContentView(R.layout.dia_confirm);
                    exitQueryModeDialog.setTitle(getResources().getString(R.string.query_mode_exit));
                    exitQueryModeDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
                    Button exitQueryModeY = exitQueryModeDialog.findViewById(R.id.dia_confirm_yes);
                    Button exitQueryModeN = exitQueryModeDialog.findViewById(R.id.dia_confirm_no);
                    exitQueryModeY.setOnClickListener(v -> {
                        cardPosition = 0;
                        updateCardList();
                        setRecView();
                        exitQueryModeDialog.dismiss();
                        queryMode.dismiss();
                    });
                    exitQueryModeN.setOnClickListener(v -> exitQueryModeDialog.dismiss());
                    exitQueryModeDialog.show();
                } else {
                    setRecView();
                    queryMode.dismiss();
                }
                return true;
            }
            return false;
        });
        queryMode.show();
    }

    public void showListStats(MenuItem menuItem) {
        Dialog listStatsDialog = new Dialog(this, R.style.dia_view);
        listStatsDialog.setContentView(R.layout.dia_list_stats);
        listStatsDialog.setTitle(getResources().getString(R.string.list_stats));
        listStatsDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        TextView statTotalCardsView = listStatsDialog.findViewById(R.id.list_stat_cards_total_content);
        statTotalCardsView.setText(Integer.toString(currentCardsList.size()));

        int statTotalProgress = currentCardsList.stream().mapToInt(c -> c.known).sum();
        TextView statTotalProgressView = listStatsDialog.findViewById(R.id.list_stat_progress_total_content);
        statTotalProgressView.setText(Integer.toString(statTotalProgress));
        int statMaxProgress = currentCardsList.stream().mapToInt(c -> c.known).max().orElse(0);
        TextView statMaxProgressView = listStatsDialog.findViewById(R.id.list_stat_progress_max_content);
        statMaxProgressView.setText(Integer.toString(statMaxProgress));
        int statMinProgress = currentCardsList.stream().mapToInt(c -> c.known).min().orElse(0);
        TextView statMinProgressView = listStatsDialog.findViewById(R.id.list_stat_progress_min_content);
        statMinProgressView.setText(Integer.toString(statMinProgress));
        double statAvgProgress = currentCardsList.stream().mapToInt(c -> c.known).average().orElse(0);
        statAvgProgress = Math.round(statAvgProgress * 100.0) / 100.0;
        TextView statAvgProgressView = listStatsDialog.findViewById(R.id.list_stat_progress_avg_content);
        statAvgProgressView.setText(Double.toString(statAvgProgress));

        int statProgressTableIs0 = (int) currentCardsList.stream().filter(c -> c.known == 0).count();
        int statProgressTableIs1 = (int) currentCardsList.stream().filter(c -> c.known == 1).count();
        int statProgressTableIs2 = (int) currentCardsList.stream().filter(c -> c.known == 2).count();
        int statProgressTableIs3 = (int) currentCardsList.stream().filter(c -> c.known == 3).count();
        int statProgressTableIs4 = (int) currentCardsList.stream().filter(c -> c.known == 4).count();
        int statProgressTableIs5OrMore = (int) currentCardsList.stream().filter(c -> c.known >= 5).count();
        float percentCurrent = statProgressTableIs0 / ((float) currentCardsList.size());
        TextView statProgressTableKnown0 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_number_0);
        TextView statProgressTablePercent0 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_percent_0);
        statProgressTableKnown0.setText(Integer.toString(statProgressTableIs0));
        statProgressTablePercent0.setText(Integer.toString(Math.round(percentCurrent * 100)));
        percentCurrent = statProgressTableIs1 / ((float) currentCardsList.size());
        TextView statProgressTableKnown1 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_number_1);
        TextView statProgressTablePercent1 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_percent_1);
        statProgressTableKnown1.setText(Integer.toString(statProgressTableIs1));
        statProgressTablePercent1.setText(Integer.toString(Math.round(percentCurrent * 100)));
        percentCurrent = statProgressTableIs2 / ((float) currentCardsList.size());
        TextView statProgressTableKnown2 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_number_2);
        TextView statProgressTablePercent2 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_percent_2);
        statProgressTableKnown2.setText(Integer.toString(statProgressTableIs2));
        statProgressTablePercent2.setText(Integer.toString(Math.round(percentCurrent * 100)));
        percentCurrent = statProgressTableIs3 / ((float) currentCardsList.size());
        TextView statProgressTableKnown3 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_number_3);
        TextView statProgressTablePercent3 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_percent_3);
        statProgressTableKnown3.setText(Integer.toString(statProgressTableIs3));
        statProgressTablePercent3.setText(Integer.toString(Math.round(percentCurrent * 100)));
        percentCurrent = statProgressTableIs4 / ((float) currentCardsList.size());
        TextView statProgressTableKnown4 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_number_4);
        TextView statProgressTablePercent4 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_percent_4);
        statProgressTableKnown4.setText(Integer.toString(statProgressTableIs4));
        statProgressTablePercent4.setText(Integer.toString(Math.round(percentCurrent * 100)));
        percentCurrent = statProgressTableIs5OrMore / ((float) currentCardsList.size());
        TextView statProgressTableKnown5 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_number_5);
        TextView statProgressTablePercent5 = listStatsDialog.findViewById(R.id.list_stat_progress_counter_percent_5);
        statProgressTableKnown5.setText(Integer.toString(statProgressTableIs5OrMore));
        statProgressTablePercent5.setText(Integer.toString(Math.round(percentCurrent * 100)));

        listStatsDialog.show();
    }

    public void startNewCard(MenuItem menuItem) {
        Intent intent = new Intent(this.getApplicationContext(), NewCard.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager()))
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
        Intent intent = new Intent(getApplicationContext(), ViewPack.class);
        intent.putExtra("collection", collectionNo);
        intent.putExtra("pack", packNo);
        intent.putExtra("reverse", reverse);
        intent.putExtra("sort", sort);
        intent.putExtra("searchQuery", searchQuery);
        intent.putExtra("cardPosition", ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager()))
                .findFirstVisibleItemPosition());
        intent.putIntegerArrayListExtra("savedList", savedList);
        intent.putExtra("savedListSeed", savedListSeed);
        this.startActivity(intent);
        this.finish();
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
                Toast.makeText(getApplicationContext(), R.string.search_no_results, Toast.LENGTH_LONG).show();
            } else {
                currentCardsList = cardsListFiltered;
            }
        }
        //Set recycler view
        AdapterCards adapter = new AdapterCards(currentCardsList, this, reverse, sort, packNo, packNos, searchQuery,
                collectionNo, progressGreater, progressNumber, savedList, savedListSeed);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.scrollToPosition(
                Math.min(cardPosition, Objects.requireNonNull(recyclerView.getAdapter()).getItemCount() - 1));
    }

    @Override
    public void onBackPressed() {
        Intent intent;
        if (packNo == -2 || packNo == -3) {
            intent = new Intent(getApplicationContext(), AdvancedSearch.class);
            intent.putExtra("pack", packNo);
            if (packNo == -2) {
                intent.putIntegerArrayListExtra("packs", packNos);
            }
            intent.putExtra("progressGreater", progressGreater);
            intent.putExtra("progressNumber", progressNumber);
        } else {
            intent = new Intent(getApplicationContext(), ListPacks.class);
            intent.putExtra("collection", collectionNo);
        }
        startActivity(intent);
        this.finish();
    }
}
