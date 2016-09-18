package com.aqilix.mobile.aqilix.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aqilix.mobile.aqilix.model.UserProfileModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by surya on 9/17/16.
 */
public class UserProfileTable extends DatabaseHelper {

    private String[] columns = { UserProfile.COL_UUID, UserProfile.COL_FIRST_NAME, UserProfile.COL_LAST_NAME, UserProfile.COL_DOB,
            UserProfile.COL_ADDRESS, UserProfile.COL_CITY, UserProfile.COL_PROVINCE, UserProfile.COL_POSTAL_CODE, UserProfile.COL_COUNTRY,
            UserProfile.COL_USER };

    public UserProfileTable(Context context) {
        super(context);
    }

    protected UserProfileModel select(String where, String[] args, String groupBy, String having, String order, String limit) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(UserProfile.TABLE_NAME, columns, where, args, groupBy, having, order, limit);
        UserProfileModel model = new UserProfileModel();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            model.setUuid(cursor.getString(cursor.getColumnIndex(UserProfile.COL_UUID)));
            model.setFirstName(cursor.getString(cursor.getColumnIndex(UserProfile.COL_FIRST_NAME)));
            model.setLastName(cursor.getString(cursor.getColumnIndex(UserProfile.COL_LAST_NAME)));
            model.setDateOfBirth(cursor.getLong(cursor.getColumnIndex(UserProfile.COL_DOB)));
            model.setAddress(cursor.getString(cursor.getColumnIndex(UserProfile.COL_ADDRESS)));
            model.setCity(cursor.getString(cursor.getColumnIndex(UserProfile.COL_CITY)));
            model.setProvince(cursor.getString(cursor.getColumnIndex(UserProfile.COL_PROVINCE)));
            model.setPostalCode(cursor.getString(cursor.getColumnIndex(UserProfile.COL_POSTAL_CODE)));
            model.setCountry(cursor.getString(cursor.getColumnIndex(UserProfile.COL_COUNTRY)));
            model.setUser(cursor.getString(cursor.getColumnIndex(UserProfile.COL_USER)));
        }
        cursor.close();
        return model;
    }

    protected List<UserProfileModel> selectAll(String where, String[] args, String groupBy, String having, String order, String limit) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(UserProfile.TABLE_NAME, columns, where, args, groupBy, having, order, limit);
        List<UserProfileModel> result = new ArrayList<>();
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String uuid = cursor.getString(cursor.getColumnIndex(UserProfile.COL_UUID));
                String firstName = cursor.getString(cursor.getColumnIndex(UserProfile.COL_FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndex(UserProfile.COL_LAST_NAME));
                Long dob = cursor.getLong(cursor.getColumnIndex(UserProfile.COL_DOB));
                String address = cursor.getString(cursor.getColumnIndex(UserProfile.COL_ADDRESS));
                String city = cursor.getString(cursor.getColumnIndex(UserProfile.COL_CITY));
                String province = cursor.getString(cursor.getColumnIndex(UserProfile.COL_PROVINCE));
                String postalCode = cursor.getString(cursor.getColumnIndex(UserProfile.COL_POSTAL_CODE));
                String country = cursor.getString(cursor.getColumnIndex(UserProfile.COL_COUNTRY));
                String user = cursor.getString(cursor.getColumnIndex(UserProfile.COL_USER));
                UserProfileModel model = new UserProfileModel(uuid, firstName, lastName, dob, address, city, province, postalCode, country, user);
                result.add(model);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return result;
    }

    public UserProfileModel getRowByUUID(String uuid) {
        String where = UserProfile.COL_UUID + " = ?";
        String args[] = { uuid };
        return select(where, args, null, null, null, null);
    }

    public Boolean insert(UserProfileModel model) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserProfile.COL_FIRST_NAME, model.getFirstName());
        values.put(UserProfile.COL_LAST_NAME, model.getLastName());
        values.put(UserProfile.COL_DOB, model.getDateOfBirth());
        values.put(UserProfile.COL_ADDRESS, model.getAddress());
        values.put(UserProfile.COL_CITY, model.getCity());
        values.put(UserProfile.COL_PROVINCE, model.getProvince());
        values.put(UserProfile.COL_POSTAL_CODE, model.getPostalCode());
        values.put(UserProfile.COL_COUNTRY, model.getCountry());
        values.put(UserProfile.COL_USER, model.getUser());

        UserProfileModel exists = getRowByUUID(model.getUuid());
        Boolean result = false;
        if (exists.getUuid() != null && !exists.getUuid().equals("")) {
            String where = UserProfile.COL_UUID + " = ?";
            String args[] = { model.getUuid() };
            result = (db.update(UserProfile.TABLE_NAME, values, where, args) > -1);
        }
        else {
            values.put(UserProfile.COL_UUID, model.getUuid());
            result = (db.insert(UserProfile.TABLE_NAME, null, values) > -1);
        }
        return result;
    }
}
