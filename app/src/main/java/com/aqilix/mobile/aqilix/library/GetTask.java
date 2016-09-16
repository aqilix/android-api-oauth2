package com.aqilix.mobile.aqilix.library;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by surya on 9/16/16.
 */
public class GetTask extends AsyncTask<Void, Long, JSONObject> {

    private HttpURLConnection connection;

    public GetTask(String url) {
        try {
            URL link = new URL(url);
            connection = (HttpURLConnection) link.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GetTask setHeader(String type, String content) {
        if (connection != null) {
            connection.setRequestProperty(type, content);
        }
        return this;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        JSONObject result = new JSONObject();
        try {
            connection.connect();
            InputStream inputStream = connection.getResponseCode() == HttpURLConnection.HTTP_OK ? connection.getInputStream() : connection.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String temp;
            while ((temp = reader.readLine()) != null) {
                builder.append(temp);
            }
            reader.close();
            result = new JSONObject(builder.toString());
            result.put("success", connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        finally {
            connection.disconnect();
        }
        return result;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
    }
}
