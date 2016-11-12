package com.aqilix.mobile.aqilix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.aqilix.mobile.aqilix.database.PairDataTable;
import com.aqilix.mobile.aqilix.library.GetTask;
import com.aqilix.mobile.aqilix.model.PairDataModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ResetActivity extends AppCompatActivity {

    private ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        progress = new ProgressDialog(ResetActivity.this);
        progress.setMessage("Loading, Please wait...");
        progress.setCancelable(false);
        progress.setInverseBackgroundForced(false);

        final TextView mail = (TextView)findViewById(R.id.editTextEmail);
        Button reset = (Button)findViewById(R.id.btnReset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strMail = mail.getText().toString();
                doReset(strMail);
            }
        });
    }

    public void doReset(String mail){
        String url = getString(R.string.host) + "/api/resetpassword/email";
        ResetTask reset = new ResetTask(url);
        reset.setHeader("Content-Type","application/vnd.aqilix.bootstrap.v1+json")
                .setHeader("Accept", "application/vnd.aqilix.bootstrap.v1+json");
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("emailAddress", mail);
            reset.setBody(bodyJson.toString());
            reset.execute();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void dismissProgress() {
        if (progress.isShowing()) {
            progress.dismiss();
        }
    }

    public void successLogin(JSONObject values) {
        try {
            Boolean isSuccess = values.getBoolean("success");
            if (isSuccess) {
                String token = values.getString("access_token");

                String url = getString(R.string.host) + "/api/me";
                String auth = "Bearer " + token;
                GetTask task = new GetTask(url);
                task.setHeader("Content-Type", "application/vnd.aqilix.bootstrap.v1+json")
                        .setHeader("Accept", "application/json")
                        .setHeader("Authorization", auth);
                task.execute();
                JSONObject getResult = task.get();

                if (getResult.getBoolean("success")) {
                    PairDataTable pair = new PairDataTable(getApplication());
                    pair.insert("uuid", getResult.getString("uuid"));
                    dismissProgress();
                    Intent dashboard = new Intent(getApplication(), LoginActivity.class);
                    dashboard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    dashboard.putExtra("content", values.toString());
                    startActivity(dashboard);
                    finish();
                }
                else {
                    dismissProgress();
                    Toast.makeText(getApplication(), getResult.getString("detail"), Toast.LENGTH_LONG).show();
                }
            }
            else {
                dismissProgress();
                String message = "Login failed, " + values.getString("detail");
                Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException | InterruptedException | ExecutionException e) {
            dismissProgress();
            e.printStackTrace();
        }
    }

    private class ResetTask extends AsyncTask<Void, Long, JSONObject> {
        private HttpURLConnection connection;
        private List<String> body;

        public ResetTask(String Url) {
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

        public ResetTask setHeader(String type, String value) {
            if (connection != null) {
                connection.setRequestProperty(type, value);
            }
            return this;
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

                InputStream iStream = connection.getResponseCode() == HttpURLConnection.HTTP_OK ? connection.getInputStream() : connection.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
                StringBuilder builder = new StringBuilder();
                String temp;
                while ((temp = reader.readLine()) != null) {
                    builder.append(temp);
                }
                reader.close();
                result = new JSONObject(builder.toString());

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
                        PairDataTable table = new PairDataTable(getApplication());
                        table.bulkInsert(listModel);
                    }
                }
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
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            successLogin(jsonObject);
        }
    }
}
