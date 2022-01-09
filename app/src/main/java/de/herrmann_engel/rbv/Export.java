package de.herrmann_engel.rbv;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import androidx.core.content.FileProvider;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Export {

    private final Context context;
    private int collectionNo;
    private final boolean singleCollection;

    public Export(Context context) {
        singleCollection = false;
        this.context = context;
    }

    public Export(Context context, int collectionNo) {
        singleCollection = true;
        this.collectionNo = collectionNo;
        this.context = context;
    }

    private boolean exportCSV(String name, String filename, Cursor cursor, boolean append){
        try {
            File file = new File(context.getCacheDir(), filename);
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file, append));
            String[] columns = cursor.getColumnNames();
            String[] schema = new String[columns.length+1];
            schema[0] = name + "_schema";
            System.arraycopy(columns, 0, schema, 1, columns.length);
            csvWrite.writeNext(schema);
            if(cursor.moveToFirst()) {
                while(!cursor.isAfterLast()) {
                    String[] row = new String[columns.length+1];
                    row[0] = name;
                    for (int i = 0; i < columns.length; i++) {
                        row[i + 1] = cursor.getString(cursor.getColumnIndex(columns[i]));
                    }
                    csvWrite.writeNext(row);
                    cursor.moveToNext();
                }
            }
            csvWrite.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private boolean exportCSV(String name, String filename, Cursor cursor) {
        return exportCSV(name, filename, cursor, true);
    }
    public boolean exportFile() {
        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.setType("text/csv");
            DB_Helper_Export dbHelperExport = new DB_Helper_Export(context);
            String index;
            List<Integer> collectionNos = new ArrayList<>();
            if(singleCollection) {
                index = Integer.toString(collectionNo);
                collectionNos.add(collectionNo);
            } else {
                collectionNos = dbHelperExport.getAllCollectionIDs();
                index =  "all";
            }
            File file = new File(context.getCacheDir(), String.format("%s_%s.%s", Globals.EXPORT_FILENAME, index, Globals.EXPORT_FILEEXTENSION));
            boolean isFirst = true;
            for(int i = 0; i < collectionNos.size(); i++) {
                int currentCollectionNo = collectionNos.get(i);
                if (!exportCSV("collection", file.getName(), dbHelperExport.getSingleCollection(currentCollectionNo), !isFirst) || !exportCSV("packs", file.getName(), dbHelperExport.getAllPacksByCollection(currentCollectionNo)) || !exportCSV("cards", file.getName(), dbHelperExport.getAllCardsByCollection(currentCollectionNo))) {
                    return false;
                }
                isFirst = false;
            }
            share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context.getApplicationContext(), context.getPackageName() + ".fileprovider", file));
            context.startActivity(Intent.createChooser(share, context.getResources().getString(R.string.export_cards)));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
