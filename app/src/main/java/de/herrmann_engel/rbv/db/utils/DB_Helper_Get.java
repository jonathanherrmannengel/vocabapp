package de.herrmann_engel.rbv.db.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.herrmann_engel.rbv.Globals;
import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Pack;
import de.herrmann_engel.rbv.utils.StringTools;
import io.noties.markwon.Markwon;

public class DB_Helper_Get {

    private final DB_Helper dbHelper;
    private final Context context;

    public DB_Helper_Get(Context context) {
        dbHelper = new DB_Helper(context);
        this.context = context;
    }

    public DB_Collection getSingleCollection(int collection) throws Exception {
        List<DB_Collection> list = dbHelper.collection_dao.getOne(collection);
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new Exception();
    }

    public DB_Pack getSinglePack(int pack) throws Exception {
        List<DB_Pack> list = dbHelper.pack_dao.getOne(pack);
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new Exception();
    }

    public DB_Card getSingleCard(int card) throws Exception {
        List<DB_Card> list = dbHelper.card_dao.getOne(card);
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new Exception();
    }

    public List<DB_Collection> getAllCollections() {
        return dbHelper.collection_dao.getAll();
    }

    public List<DB_Collection> getAllCollectionsByName(String name) {
        return dbHelper.collection_dao.getAllByName(name);
    }

    public List<DB_Pack> getAllPacks() {
        return dbHelper.pack_dao.getAll();
    }

    public List<DB_Pack> getAllPacksByCollection(int collection) {
        return dbHelper.pack_dao.getAll(collection);
    }

    public List<DB_Pack> getAllPacksByCollectionAndNameAndDesc(int collection, String name, String desc) {
        return dbHelper.pack_dao.getAllByCollectionAndNameAndDesc(collection, name, desc);
    }

    private int compareCardsAlphabetical(String a, String b) {
        return a.compareToIgnoreCase(b);
    }

    public List<DB_Card> formatCards(List<DB_Card> list) {
        SharedPreferences settings = context.getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
        boolean formatCards = settings.getBoolean("format_cards", false);
        boolean formatCardsNotes = settings.getBoolean("format_card_notes", false);
        if (formatCards || formatCardsNotes) {
            StringTools formatString = new StringTools();
            Markwon markwon = Markwon.create(context);
            list.forEach(l -> {
                if (formatCards) {
                    l.front = formatString.format(l.front).toString();
                    l.back = formatString.format(l.back).toString();
                }
                if (formatCardsNotes) {
                    l.notes = markwon.toMarkdown(l.notes).toString();
                }
            });
        }
        return list;
    }

    public List<DB_Card> sortCards(List<DB_Card> list, int sort) {
        list = formatCards(list);
        if (sort == Globals.SORT_ALPHABETICAL) {
            list.sort((a, b) -> {
                int c0 = compareCardsAlphabetical(a.front, b.front);
                if (c0 == 0) {
                    return compareCardsAlphabetical(a.back, b.back);
                }
                return c0;
            });
            return list;
        } else if (sort == Globals.SORT_RANDOM) {
            Collections.shuffle(list);
            return list;
        }
        list.sort((a, b) -> {
            int c0 = Integer.compare(a.known, b.known);
            if (c0 == 0) {
                return Long.compare(b.date, a.date);
            }
            return c0;
        });
        return list;
    }

    public List<DB_Card> getAllCards(int sort) {
        return sortCards(dbHelper.card_dao.getAll(), sort);
    }

    public List<DB_Card> getAllCards() {
        return dbHelper.card_dao.getAll();
    }

    public List<DB_Card> getAllCardsByIds(ArrayList<Integer> ids) {
        List<DB_Card> list = new ArrayList<>();
        ids.forEach(id -> {
            try {
                list.add(getSingleCard(id));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return formatCards(list);
    }

    public List<DB_Card> getAllCardsByProgress(int sort, boolean progressGreater, int progressNumber) {
        List<DB_Card> cards = new ArrayList<>();
        if (progressNumber >= 0) {
            if (progressGreater) {
                cards.addAll(dbHelper.card_dao.getAllGreaterEqual(progressNumber));
            } else {
                cards.addAll(dbHelper.card_dao.getAllLessEqual(progressNumber));
            }
        } else {
            cards.addAll(dbHelper.card_dao.getAll());
        }
        return sortCards(cards, sort);
    }

    public List<DB_Card> getAllCardsByCollection(int collection, int sort) {
        List<DB_Pack> packs = getAllPacksByCollection(collection);
        List<DB_Card> list = new ArrayList<>();
        packs.forEach((currentPack) -> list.addAll(getAllCardsByPack(currentPack.uid)));
        return sortCards(list, sort);
    }

    public List<DB_Card> getAllCardsByCollection(int collection) {
        List<DB_Pack> packs = getAllPacksByCollection(collection);
        List<DB_Card> list = new ArrayList<>();
        packs.forEach((currentPack) -> list.addAll(getAllCardsByPack(currentPack.uid)));
        return list;
    }

    public List<DB_Card> getAllCardsByPack(int pack, int sort) {
        return sortCards(dbHelper.card_dao.getAll(pack), sort);
    }

    public List<DB_Card> getAllCardsByPack(int pack) {
        return dbHelper.card_dao.getAll(pack);
    }

    public List<DB_Card> getAllCardsByPacksAndProgress(List<Integer> packs, int sort, boolean progressGreater,
                                                       int progressNumber) {
        List<DB_Card> cards = new ArrayList<>();
        packs.forEach(pack -> {
            if (progressNumber >= 0) {
                if (progressGreater) {
                    cards.addAll(dbHelper.card_dao.getAllGreaterEqual(pack, progressNumber));
                } else {
                    cards.addAll(dbHelper.card_dao.getAllLessEqual(pack, progressNumber));
                }
            } else {
                cards.addAll(dbHelper.card_dao.getAll(pack));
            }
        });
        return sortCards(cards, sort);
    }

    public List<DB_Card> getAllCardsByPackAndFrontAndBackAndNotes(int pack, String front, String back, String notes) {
        return dbHelper.card_dao.getAllByPackAndFrontAndBackAndNotes(pack, front, back, notes);
    }

}
