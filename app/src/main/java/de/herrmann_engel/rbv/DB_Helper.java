package de.herrmann_engel.rbv;

import android.content.Context;

public class DB_Helper {

    public DB_Collection_DAO collection_dao;
    public DB_Pack_DAO pack_dao;
    public DB_Card_DAO card_dao;
    public Context context;

    public DB_Helper(Context context){
        this.context = context;
        AppDatabase db = (new AppDatabaseBuilder()).get(context);
        collection_dao = db.collectionDAO();
        pack_dao = db.packDAO();
        card_dao = db.cardDAO();
    }

}
