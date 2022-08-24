package de.herrmann_engel.rbv;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DB_Helper_Get {

    private final DB_Helper dbHelper;
    private final Context context;

    public DB_Helper_Get(Context context) {
        dbHelper = new DB_Helper(context);
        this.context = context;
    }

    DB_Collection getSingleCollection(int collection) throws Exception {
        List<DB_Collection> list = dbHelper.collection_dao.getOne(collection);
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new Exception();
    }

    DB_Pack getSinglePack(int pack) throws Exception {
        List<DB_Pack> list = dbHelper.pack_dao.getOne(pack);
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new Exception();
    }

    DB_Card getSingleCard(int card) throws Exception {
        List<DB_Card> list = dbHelper.card_dao.getOne(card);
        if (list.size() == 1) {
            return list.get(0);
        }
        throw new Exception();
    }

    List<DB_Collection> getAllCollections() {
        return dbHelper.collection_dao.getAll();
    }

    List<DB_Collection> getAllCollectionsByName(String name) {
        return dbHelper.collection_dao.getAllByName(name);
    }

    List<DB_Pack> getAllPacks() {
        return dbHelper.pack_dao.getAll();
    }

    List<DB_Pack> getAllPacksByCollection(int collection) {
        return dbHelper.pack_dao.getAll(collection);
    }

    List<DB_Pack> getAllPacksByCollectionAndNameAndDesc(int collection, String name, String desc) {
        return dbHelper.pack_dao.getAllByCollectionAndNameAndDesc(collection, name, desc);
    }

    private int compareCardsAlphabetical (String a, String b, boolean formatCards) {
        if(formatCards) {
            a = (new FormatString(a)).formatString().toString();
            b = (new FormatString(b)).formatString().toString();
        }
        return a.compareToIgnoreCase(b);
    }

    List<DB_Card> sortCards(List<DB_Card> list, int sort) {
        if (sort == Globals.SORT_ALPHABETICAL) {

            SharedPreferences settings = context.getSharedPreferences(Globals.SETTINGS_NAME, MODE_PRIVATE);
            boolean formatCards = settings.getBoolean("format_cards", false);
            list.sort((a, b) -> {
                int c0 = compareCardsAlphabetical(a.front, b.front, formatCards);
                if (c0 == 0) {
                    return compareCardsAlphabetical(a.back, b.back, formatCards);
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

    List<DB_Card> getAllCards(int sort) {
        List<DB_Pack> packs = getAllPacks();
        List<DB_Card> list = new ArrayList<>();
        packs.forEach((currentPack) -> list.addAll(getAllCardsByPack(currentPack.uid, sort)));
        return sortCards(list, sort);
    }

    List<DB_Card> getAllCardsByCollection(int collection, int sort) {
        List<DB_Pack> packs = getAllPacksByCollection(collection);
        List<DB_Card> list = new ArrayList<>();
        packs.forEach((currentPack) -> list.addAll(getAllCardsByPack(currentPack.uid, sort)));
        return sortCards(list, sort);
    }

    List<DB_Card> getAllCardsByPack(int pack, int sort) {
        return sortCards(dbHelper.card_dao.getAll(pack), sort);
    }

    List<DB_Card> getAllCardsByPack(int pack) {
        return getAllCardsByPack(pack, Globals.SORT_DEFAULT);
    }

    List<DB_Card> getAllCardsByPackAndFrontAndBackAndNotes(int pack, String front, String back, String notes) {
        return sortCards(dbHelper.card_dao.getAllByPackAndFrontAndBackAndNotes(pack, front, back, notes),
                Globals.SORT_DEFAULT);
    }

}
