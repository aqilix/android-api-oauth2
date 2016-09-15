package com.aqilix.mobile.aqilix.library;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Asus on 9/14/2016.
 */

public class PostTask extends AsyncTask<Void, Long, JSONObject> {

    private HttpURLConnection connection;
    private JSONObject serverReturn;
    private OutputStream stream;

    public PostTask(String Url) {
        try {
            URL post = new URL(Url);
            connection = (HttpURLConnection) post.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            stream = new BufferedOutputStream(connection.getOutputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setHeader(String type, String value) {
        if (connection != null) {
            connection.setRequestProperty(type, value);
        }
    }

    public void setConnectTimOut(Integer timeout) {
        connection.setConnectTimeout(timeout);
    }

    public void setBody(String body) {
        if (body != null) {
            try {
                stream.write(body.getBytes());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        JSONObject result = new JSONObject();
        try {
            stream.flush();
            stream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            connection.disconnect();
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        this.serverReturn = jsonObject;
        super.onPostExecute(jsonObject);
    }

    public JSONObject getResult() {
        return serverReturn;
    }
}
