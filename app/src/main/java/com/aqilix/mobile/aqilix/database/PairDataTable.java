package com.aqilix.mobile.aqilix.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.aqilix.mobile.aqilix.model.PairDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by surya on 9/15/16.
 */
public class PairDataTable extends DatabaseHelper {

    private String[] columns = { PairData.COL_KEY, PairData.COL_VALUE };

    public PairDataTable(Context context) {
        super(context);
    }

    protected PairDataModel fetchRow(String where, String[] args, String groupBy, String having, String order, String limit) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(PairData.TABLE_NAME, columns, where, args, groupBy, having, order, limit);
        PairDataModel pair = new PairDataModel();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            pair.setKey(cursor.getString(cursor.getColumnIndex(PairData.COL_KEY)));
            pair.setValue(cursor.getString(cursor.getColumnIndex(PairData.COL_VALUE)));
        }
        cursor.close();
        return pair;
    }

    protected List<PairDataModel> fetchAll(String where, String[] args, String groupBy, String having, String order, String limit) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(PairData.TABLE_NAME, columns, where, args, groupBy, having, order, limit);
        List<PairDataModel> list = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String key = cursor.getString(cursor.getColumnIndex(PairData.COL_KEY));
                String value = cursor.getString(cursor.getColumnIndex(PairData.COL_VALUE));
                PairDataModel model = new PairDataModel(key, value);
                list.add(model);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return list;
    }

    public void bulkInsert(List<PairDataModel> modelList) {
        String query = "INSERT OR REPLACE INTO " + PairData.TABLE_NAME + " VALUES (?, ?)";
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            SQLiteStatement stmt = db.compileStatement(query);
            for (PairDataModel model : modelList) {
                stmt.clearBindings();
                stmt.bindString(1, model.getKey());

                if (model.getValue() == null) {
                    stmt.bindNull(2);
                }
                else {
                    stmt.bindString(2, model.getValue());
                }
                stmt.execute();
            }
            db.setTransactionSuccessful();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            db.endTransaction();
        }
    }

    public void insert(String key, String value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put(PairData.COL_KEY, key);
        content.put(PairData.COL_VALUE, value);
        db.insert(PairData.TABLE_NAME, null, content);
    }

    public String getValueOfKey(String key) {
        String where = PairData.COL_KEY + " = ?";
        String[] args = { key };
        return fetchRow(where, args, null, null, null, null).getValue();
    }
}
