package de.herrmann_engel.rbv;

import android.content.Context;

import java.util.List;

public class DB_Helper_Delete {

    private final DB_Helper dbHelper;

    public DB_Helper_Delete(Context context){
        dbHelper = new DB_Helper(context);
    }


    boolean deleteCollection(DB_Collection collection, boolean force) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        try {
            dbHelperGet.getSingleCollection(collection.uid);
        } catch (Exception e) {
            return false;
        }
        List<DB_Pack> packs = dbHelperGet.getAllPacksByCollection(collection.uid);
        boolean containsPacks = packs.size() > 0;
        if(containsPacks && force) {
            packs.forEach(pack -> deletePack(pack, force));
        } else if (containsPacks) {
            return false;
        }
        dbHelper.collection_dao.delete(collection);
        return true;
    }
    boolean deleteCollection(DB_Collection collection) {
        return deleteCollection(collection, false);
    }

    boolean deletePack(DB_Pack pack, boolean force) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        try {
            dbHelperGet.getSinglePack(pack.uid);
        } catch (Exception e) {
            return false;
        }
        boolean containsCards = dbHelperGet.getAllCardsByPack(pack.uid).size() > 0;
        if(containsCards && force) {
            dbHelper.card_dao.deleteAllByPack(pack.uid);
        } else if (containsCards) {
            return false;
        }
        dbHelper.pack_dao.delete(pack);
        return true;
    }
    boolean deletePack(DB_Pack pack) {
        return deletePack(pack, false);
    }

    boolean deleteCard(DB_Card card) {
        DB_Helper_Get dbHelperGet = new DB_Helper_Get(dbHelper.context);
        try {
            dbHelperGet.getSingleCard(card.uid);
        } catch (Exception e) {
            return false;
        }
        dbHelper.card_dao.delete(card);
        return true;
    }
}
