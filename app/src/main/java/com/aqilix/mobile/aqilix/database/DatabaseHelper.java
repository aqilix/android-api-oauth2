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
        sqLiteDatabase.execSQL(UserProfile.CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public static final class PairData implements BaseColumns {
        public static final String TABLE_NAME = "pair_data";
        public static final String COL_KEY = "key";
        public static final String COL_VALUE = "value";
        public static final String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_KEY + " VARCHAR NOT NULL PRIMARY KEY, " +
                COL_VALUE + " TEXT)";
    }

    public static final class UserProfile implements BaseColumns {
        public static final String TABLE_NAME = "user_profile";
        public static final String COL_UUID = "uuid";
        public static final String COL_FIRST_NAME = "firstName";
        public static final String COL_LAST_NAME = "lastName";
        public static final String COL_DOB = "dateOfBirth";
        public static final String COL_ADDRESS = "address";
        public static final String COL_CITY = "city";
        public static final String COL_PROVINCE = "province";
        public static final String COL_POSTAL_CODE = "postalCode";
        public static final String COL_COUNTRY = "country";
        public static final String COL_USER = "user";
        public static final String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_UUID + " VARCHAR NOT NULL PRIMARY KEY, " +
                COL_FIRST_NAME + " VARCHAR, " +
                COL_LAST_NAME + " VARCHAR, " +
                COL_DOB + " LONG, " +
                COL_ADDRESS + " TEXT, " +
                COL_CITY + " VARCHAR, " +
                COL_PROVINCE + " VARCHAR, " +
                COL_POSTAL_CODE + " VARCHAR, " +
                COL_COUNTRY + " VARCHAR, " +
                COL_USER + " VARCHAR)";
    }
}
