package de.herrmann_engel.rbv;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import java.util.List;

public class DB_Helper_Export {

    private final DB_Helper dbHelper;

    public DB_Helper_Export(Context context){
        dbHelper = new DB_Helper(context);
    }

    List<Integer> getAllCollectionIDs(){
        return dbHelper.collection_dao.getAllIDs();
    }

    Cursor getSingleCollection(int collection) {
        return dbHelper.collection_dao.getOneExport(collection);
    }

    Cursor getAllPacksByCollection(int collection) {
        return dbHelper.pack_dao.getAllExportByCollection(collection);
    }
    Cursor getAllCardsByCollection(int collection) {
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
}
