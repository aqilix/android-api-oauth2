package com.aqilix.mobile.aqilix.library;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by surya on 9/23/16.
 */
public class DownloadTask extends AsyncTask<Void, Long, File> {

    private String url;
    private String path;
    private String filename;
    private List<HeaderCollections> headers;
    private List<String> bodies;
    private String method;
    private Integer timeOut;

    public DownloadTask(String url, String filePath, String fileName) {
        this.url = url;
        this.path = filePath;
        this.filename = fileName;
        headers = new ArrayList<>();
        bodies = new ArrayList<>();
        method = "GET";
    }

    public DownloadTask setRequestMethod(String method) {
        if (method != null) {
            this.method = method;
        }
        return this;
    }

    public DownloadTask setHeader(String type, String value) {
        if (type != null && value != null) {
            HeaderCollections header = new HeaderCollections(type, value);
            headers.add(header);
        }
        return this;
    }

    public DownloadTask setConnectTimOut(Integer timeout) {
        if (timeout != null) {
            this.timeOut = timeout;
        }
        return this;
    }

    public DownloadTask setBody(String body) {
        if (body != null) {
            bodies.add(body);
        }
        return this;
    }

    @Override
    protected File doInBackground(Void... voids) {
        File result = null;
        try {
            URL link = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) link.openConnection();
            if (timeOut != null) {
                connection.setConnectTimeout(timeOut);
            }

            if (headers.size() > 0) {
                for (HeaderCollections header : headers) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }
            connection.setRequestMethod(method);
            connection.connect();

            if (bodies.size() > 0) {
                OutputStream os = new BufferedOutputStream(connection.getOutputStream());
                for (String body : bodies) {
                    os.write(body.getBytes());
                }
                os.flush();
                os.close();
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Integer contentLength = connection.getContentLength();
                DataInputStream dis = new DataInputStream(link.openStream());
                byte[] data = new byte[contentLength];
                dis.readFully(data);
                dis.close();

                result = new File(path + "/" + filename);
                if (result.exists()) {
                    result.delete();
                }
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(result));
                dos.write(data);
                dos.flush();
                dos.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static class HeaderCollections {

        protected String key;
        protected String value;

        public HeaderCollections() {
        }

        public HeaderCollections(String key, String value) {
            setKey(key);
            setValue(value);
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
