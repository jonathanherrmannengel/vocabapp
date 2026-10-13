package de.herrmann_engel.rbv.db.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import java.util.List;

/**
 * Provides raw database cursors for the CSV export, either for everything or for a single collection.
 */
public class DB_Helper_Export {

    private final DB_Helper dbHelper;

    public DB_Helper_Export(Context context) {
        dbHelper = new DB_Helper(context);
    }

    public List<Integer> getAllCollectionIDs() {
        return dbHelper.collection_dao.getAllIds();
    }

    public Cursor getSingleCollection(int collection) {
        return dbHelper.collection_dao.getOneExport(collection);
    }

    public Cursor getAllPacksByCollection(int collection) {
        return dbHelper.pack_dao.getAllExportByCollection(collection);
    }

    /**
     * Returns all cards of the packs in the given collection. The rows are copied as strings
     * into a new cursor.
     */
    public Cursor getAllCardsByCollection(int collection) {
        List<Integer> packIDs = dbHelper.pack_dao.getAllIdsByCollection(collection);
        Cursor cardsCursor = dbHelper.card_dao.getAllExport();
        String[] names = cardsCursor.getColumnNames();
        MatrixCursor cursor = new MatrixCursor(names);
        if (cardsCursor.moveToFirst()) {
            do {
                // Column 3 is the pack of the card
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


    public Cursor getAllTags() {
        return dbHelper.tag_dao.getAllExport();
    }

    public Cursor getAllTagsByCollection(int collection) {
        return dbHelper.tag_dao.getAllExportByCollection(collection);
    }

    public Cursor getAllTagLinks() {
        return dbHelper.tag_link_card_dao.getAllExport();
    }

    public Cursor getAllTagLinksByCollection(int collection) {
        return dbHelper.tag_link_card_dao.getAllExportByCollection(collection);
    }

}
