package com.aqilix.mobile.aqilix.library;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by surya on 9/20/16.
 */
public class FormDataTask extends AsyncTask<Void, Long, JSONObject> {

    private HttpURLConnection connection;
    private static final String DOUBLE_DASH = "--";
    private static final String BREAK = "\r\n";
    private String boundary;
    private List<KeyValuePair> bodyText;
    private File attachment;

    public FormDataTask(String url) {
        boundary = MainFunction.createRandomString(15);
        try {
            String contentType = "multipart/form-data; boundary=" + boundary;
            URL link = new URL(url);
            connection = (HttpURLConnection) link.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", contentType);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        bodyText = new ArrayList<>();
    }

    public FormDataTask setRequestMethod(String method) {
        if (method != null) {
            try {
                connection.setRequestMethod(method);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public FormDataTask setHeader(String type, String value) {
        if (connection != null && (type != null) && (value != null)) {
            connection.setRequestProperty(type, value);
        }
        return this;
    }

    public FormDataTask setTimeout(Integer timeout) {
        if (connection != null) {
            connection.setConnectTimeout(timeout);
        }
        return this;
    }

    public FormDataTask setBody(String field, String value) {
        KeyValuePair content = new KeyValuePair(field, value);
        bodyText.add(content);
        return this;
    }

    public FormDataTask setFiles(File file) {
        this.attachment = file;
        return this;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        JSONObject result = new JSONObject();
        try {
            connection.connect();
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            if (bodyText.size() > 0) {
                for (KeyValuePair each : bodyText) {
                    out.write((DOUBLE_DASH + boundary + BREAK).getBytes());
                    out.write(("Content-Disposition: form-data; name=\"" + each.getKey() + "\"" + BREAK + BREAK).getBytes());
                    if (each.getValue() != null) {
                        out.write(String.valueOf(each.getValue()).getBytes());
                    }
                    out.write(BREAK.getBytes());
                }
            }
            if (attachment != null && attachment.exists()) {
                out.write((DOUBLE_DASH + boundary + BREAK).getBytes());
                out.write(("Content-Disposition: file; name=\"photo\"; filename=\"" + attachment.getName() + "\"" + BREAK).getBytes());
                String mime = MainFunction.getMimeType(attachment.getAbsolutePath());
                if (mime != null) {
                    out.write(("Content-Type: " + mime + BREAK).getBytes());
                }
                out.write(BREAK.getBytes());
                int size = (int) attachment.length();
                byte[] attachByte = new byte[size];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(attachment));
                bis.read(attachByte, 0, attachByte.length);
                bis.close();
                out.write(attachByte);
                out.write(BREAK.getBytes());
            }
            out.write((DOUBLE_DASH + boundary + DOUBLE_DASH + BREAK).getBytes());
            out.flush();
            out.close();

            InputStream is = (connection.getResponseCode() == HttpURLConnection.HTTP_OK) ? connection.getInputStream() : connection.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder builder = new StringBuilder();
            String temp;
            while ((temp = reader.readLine()) != null) {
                builder.append(temp);
            }
            reader.close();
            Log.i("hasilServer", builder.toString());
            result = new JSONObject(builder.toString());
            result.put("success", (connection.getResponseCode() == HttpURLConnection.HTTP_OK));
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static class KeyValuePair {
        protected String key;
        protected Object value;

        public KeyValuePair() {
        }

        public KeyValuePair(String key, Object value){
            setKey(key);
            setValue(value);
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }
}
