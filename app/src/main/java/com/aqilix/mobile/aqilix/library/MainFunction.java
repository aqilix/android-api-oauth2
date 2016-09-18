package com.aqilix.mobile.aqilix.library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.aqilix.mobile.aqilix.R;
import com.aqilix.mobile.aqilix.database.PairDataTable;
import com.aqilix.mobile.aqilix.model.PairDataModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by surya on 9/18/16.
 */
public class MainFunction {

    protected Context context;
    public MainFunction(Context context) {
        this.context = context;
    }

    public static String getToken(Context context) {
        PairDataTable pairTable = new PairDataTable(context);
        Long insertTime = Long.valueOf(pairTable.getValueOfKey("insert_time"));
        Long now = System.currentTimeMillis();
        // Long expiresIn = Long.valueOf(pairTable.getValueOfKey("expires_in"));
        Long expiresIn = 300L;
        Long duration = (now - insertTime) / 1000;
        String token = null;
        if (duration < expiresIn) {
            token = pairTable.getValueOfKey("access_token");
        }
        else {
            String refreshToken = pairTable.getValueOfKey("refresh_token");
            if (refreshToken != null) {
                try {
                    String url = context.getString(R.string.host) + "/oauth";
                    PostTask post = new PostTask(url);
                    post.setHeader("Content-Type", "application/json")
                            .setHeader("Accept", "application/json");

                    JSONObject body = new JSONObject();
                    body.put("grant_type", "refresh_token")
                            .put("client_secret", context.getString(R.string.client_secret))
                            .put("client_id", context.getString(R.string.client_id))
                            .put("refresh_token", refreshToken);
                    post.setBody(body.toString());
                    post.execute();
                    JSONObject result = post.get();
                    if (result.getBoolean("success")) {
                        token = result.getString("access_token");
                        Iterator<String> keys = result.keys();
                        List<PairDataModel> listModel = new ArrayList<>();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String value = result.getString(key);
                            PairDataModel model = new PairDataModel(key, value);
                            listModel.add(model);
                        }

                        if (listModel.size() > 0) {
                            PairDataModel timeModel = new PairDataModel("insert_time", String.valueOf(System.currentTimeMillis()));
                            listModel.add(timeModel);
                            pairTable.bulkInsert(listModel);
                        }
                    }
                }
                catch (JSONException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i("token", token);
        return token;
    }

    public static Long DateToLong(String dateString, String format) {
        if (dateString == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
        Long result = 0L;
        try {
            Date date = formatter.parse(dateString);
            result = date.getTime();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }
}
