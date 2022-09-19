package de.herrmann_engel.rbv.db.utils;

import android.content.Context;

import de.herrmann_engel.rbv.db.AppDatabase;
import de.herrmann_engel.rbv.db.DB_Card_DAO;
import de.herrmann_engel.rbv.db.DB_Collection_DAO;
import de.herrmann_engel.rbv.db.DB_Pack_DAO;

public class DB_Helper {

    final DB_Collection_DAO collection_dao;
    final DB_Pack_DAO pack_dao;
    final DB_Card_DAO card_dao;
    final Context context;

    DB_Helper(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getInstance(context);
        collection_dao = db.collectionDAO();
        pack_dao = db.packDAO();
        card_dao = db.cardDAO();
    }

}
