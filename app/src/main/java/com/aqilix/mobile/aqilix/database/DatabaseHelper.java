package com.aqilix.mobile.aqilix.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by surya on 9/15/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    protected static final String DATABASE_NAME = "aqilix_db";
    protected static final Integer DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(PairData.CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public static final class PairData implements BaseColumns {
        public static final String TABLE_NAME = "pair_data";
        public static final String COL_KEY = "key";
        public static final String COL_VALUE = "value";
        public static final String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                COL_KEY + " VARCHAR NOT NULL PRIMARY KEY, " +
                COL_VALUE + " TEXT)";
    }
}
