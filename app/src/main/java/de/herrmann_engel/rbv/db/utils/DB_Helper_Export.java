package de.herrmann_engel.rbv.db.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import java.util.List;

public class DB_Helper_Export {

    private final DB_Helper dbHelper;

    public DB_Helper_Export(Context context) {
        dbHelper = new DB_Helper(context);
    }

    public List<Integer> getAllCollectionIDs() {
        return dbHelper.collection_dao.getAllIDs();
    }

    public Cursor getSingleCollection(int collection) {
        return dbHelper.collection_dao.getOneExport(collection);
    }

    public Cursor getAllPacksByCollection(int collection) {
        return dbHelper.pack_dao.getAllExportByCollection(collection);
    }

    public Cursor getAllCardsByCollection(int collection) {
        List<Integer> packIDs = dbHelper.pack_dao.getAllIDs(collection);
        Cursor cardsCursor = dbHelper.card_dao.getAllExport();
        String[] names = cardsCursor.getColumnNames();
        MatrixCursor cursor = new MatrixCursor(names);
        if (cardsCursor.moveToFirst()) {
            do {
                if (packIDs.contains(cardsCursor.getInt(3))) {
                    Object[] row = new Object[names.length];
                    for (int i = 0; i < names.length; i++) {
                        row[i] = cardsCursor.getString(i);
                    }
                    cursor.addRow(row);
                }
            } while (cardsCursor.moveToNext());
        }
        return cursor;
    }

    public Cursor getAllMedia() {
        return dbHelper.media_dao.getAllExport();
    }

    public Cursor getAllMediaByCollection(int collection) {
        return dbHelper.media_dao.getAllExportByCollection(collection);
    }

    public Cursor getAllMediaLinks() {
        return dbHelper.media_link_card_dao.getAllExport();
    }

    public Cursor getAllMediaLinksByCollection(int collection) {
        return dbHelper.media_link_card_dao.getAllExportByCollection(collection);
    }

}
