package de.herrmann_engel.rbv;

import android.content.Context;

public class DB_Helper {

    public final DB_Collection_DAO collection_dao;
    public final DB_Pack_DAO pack_dao;
    public final DB_Card_DAO card_dao;
    public final Context context;

    public DB_Helper(Context context) {
        this.context = context;
        AppDatabase db = (new AppDatabaseBuilder()).get(context);
        collection_dao = db.collectionDAO();
        pack_dao = db.packDAO();
        card_dao = db.cardDAO();
    }

}
