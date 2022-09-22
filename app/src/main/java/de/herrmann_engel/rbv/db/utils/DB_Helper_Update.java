package de.herrmann_engel.rbv.db.utils;

import android.content.Context;

import de.herrmann_engel.rbv.db.DB_Card;
import de.herrmann_engel.rbv.db.DB_Collection;
import de.herrmann_engel.rbv.db.DB_Pack;

public class DB_Helper_Update {

    private final DB_Helper dbHelper;

    public DB_Helper_Update(Context context) {
        dbHelper = new DB_Helper(context);
    }

    public boolean updateCollection(DB_Collection collection) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        try {
            dbHelperGet.getSingleCollection(collection.uid);
        } catch (Exception e) {
            return false;
        }
        if (collection.name.equals("")) {
            return false;
        }
        dbHelper.collection_dao.update(collection);
        return true;
    }

    public boolean updatePack(DB_Pack pack) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        try {
            dbHelperGet.getSinglePack(pack.uid);
        } catch (Exception e) {
            return false;
        }
        if (pack.name.equals("")) {
            return false;
        }
        dbHelper.pack_dao.update(pack);
        return true;
    }

    public boolean updateCard(DB_Card card) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        try {
            dbHelperGet.getSingleCard(card.uid);
        } catch (Exception e) {
            return false;
        }
        if (card.front.equals("") || card.back.equals("")) {
            return false;
        }
        dbHelper.card_dao.update(card);
        return true;
    }

}