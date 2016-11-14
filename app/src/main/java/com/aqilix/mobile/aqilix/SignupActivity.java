package com.aqilix.mobile.aqilix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class SignupActivity extends AppCompatActivity {

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        progress = new ProgressDialog(SignupActivity.this);
        progress.setMessage(getString(R.string.progress_message));
        progress.setCancelable(false);
        progress.setInverseBackgroundForced(false);
        isLogin();

        final EditText email = (EditText) findViewById(R.id.editEmail);
        final EditText password = (EditText) findViewById(R.id.editPassword);
        Button signUp = (Button) findViewById(R.id.btnSignup);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.show();
                String strEmail = email.getText().toString().trim().toLowerCase(Locale.US);
                String strPassword = password.getText().toString().trim();
                if (!strEmail.equals("") && !strPassword.equals("")) {
                    doSignUp(strEmail, strPassword);
                }
                else {
                    progress.dismiss();
                    Toast.makeText(getApplication(), "Register failed, some field is empty", Toast.LENGTH_LONG).show();
                }
            }
        });
        TextView login = (TextView) findViewById(R.id.loginText);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLogin();
            }
        });
    }

    private void isLogin() {
        PairDataTable pair = new PairDataTable(getApplication());
        String uuid = pair.getValueOfKey("uuid");
        if (uuid != null && !uuid.equals("")) {
            goToDashboard(uuid);
        }
    }

    private void goToLogin() {
        Intent login = new Intent(getApplication(), LoginActivity.class);
        login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(login);
        overridePendingTransition(0, 0);
        finish();
    }

    private void goToDashboard(String uuid) {
        Intent dashboard = new Intent(getApplication(), DashboardActivity.class);
        dashboard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        dashboard.putExtra("content", uuid);
        startActivity(dashboard);
        finish();
    }

    private void doSignUp(String email, String password) {
        String url = getString(R.string.host) + "/api/signup";
        SignUp sign = new SignUp(url);
        sign.setHeader("Content-Type", "application/vnd.aqilix.bootstrap.v1+json")
                .setHeader("Accept", "application/vnd.aqilix.bootstrap.v1+json");
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("email", email).put("password", password);
            sign.setBody(jObj.toString());
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        sign.execute();
    }

    private void dismissProgress() {
        if (progress.isShowing()) {
            progress.dismiss();
        }
    }

    private void successRegister(JSONObject json) {
        try {
            Boolean success = json.getBoolean("success");
            if (success) {
                String token = json.getString("access_token");

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
                    goToDashboard(json.toString());
                }
                else {
                    dismissProgress();
                    Toast.makeText(getApplication(), getResult.getString("detail"), Toast.LENGTH_LONG).show();
                }
            }
            else {
                dismissProgress();
                String message = "Register failed, " + json.getString("detail");
                Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException | ExecutionException | InterruptedException e) {
            dismissProgress();
            e.printStackTrace();
        }
    }

    private class SignUp extends AsyncTask<Void, Long, JSONObject> {
        private HttpURLConnection connection;
        private List<String> body;

        public SignUp(String Url) {
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

        public SignUp setHeader(String type, String value) {
            if (connection != null) {
                connection.setRequestProperty(type, value);
            }
            return this;
        }

        public void setConnectTimeOut(Integer timeout) {
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
                result.put("success", false);
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
                    // Write to SQLite pair data
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
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            successRegister(jsonObject);
        }
    }
}
