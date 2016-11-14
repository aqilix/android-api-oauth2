package com.aqilix.mobile.aqilix.library;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Asus on 9/14/2016.
 */

public class PostTask extends AsyncTask<Void, Long, JSONObject> {

    private HttpURLConnection connection;
    private List<String> body;

    public PostTask(String Url) {
        try {
            URL post = new URL(Url);
            connection = (HttpURLConnection) post.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        body = new ArrayList<>();
    }

    public PostTask setRequestMethod(String method) {
        if (method != null) {
            try {
                connection.setRequestMethod(method);
            }
            catch (ProtocolException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public PostTask setHeader(String type, String value) {
        if (connection != null) {
            connection.setRequestProperty(type, value);
        }
        return this;
    }

    public void setConnectTimOut(Integer timeout) {
        connection.setConnectTimeout(timeout);
    }

    public void setBody(String body) {
        if (body != null) {
            this.body.add(body);
        }
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        JSONObject result = new JSONObject();
        try {
            connection.connect();
            OutputStream stream = new BufferedOutputStream(connection.getOutputStream());
            for (String strBody : body) {
                stream.write(strBody.getBytes());
            }
            stream.flush();
            stream.close();

            InputStream iStream = (connection.getResponseCode() == HttpURLConnection.HTTP_OK) ? connection.getInputStream() : connection.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder builder = new StringBuilder();
            String temp;
            while ((temp = reader.readLine()) != null) {
                builder.append(temp);
            }
            reader.close();
            result = new JSONObject(builder.toString());
            result.put("success", (connection.getResponseCode() == HttpURLConnection.HTTP_OK));
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
