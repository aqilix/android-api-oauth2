package com.aqilix.mobile.aqilix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progress = new ProgressDialog(LoginActivity.this);
        progress.setMessage("Loading, Please wait...");
        progress.setCancelable(false);
        progress.setInverseBackgroundForced(false);

        final EditText email = (EditText) findViewById(R.id.editTextEmail);
        final EditText password = (EditText) findViewById(R.id.editTextPassword);
        Button loginBtn = (Button) findViewById(R.id.btnLogin);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.show();
                String emailAddr = email.getText().toString().trim().toLowerCase(Locale.US);
                String pass = password.getText().toString().trim();
                if (!emailAddr.equals("") && !pass.equals("")) {
                    doLogin(emailAddr, pass);
                }
                else {
                    if (progress.isShowing()) {
                        progress.dismiss();
                    }
                    Toast.makeText(getApplication(), "Login failed, please fill all form", Toast.LENGTH_LONG).show();
                }
            }
        });
        TextView signUp = (TextView) findViewById(R.id.signUpText);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpIntent = new Intent(getApplication(), SignupActivity.class);
                signUpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(signUpIntent);
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    public void doLogin(String email, String password) {
        String url = getString(R.string.host) + "/oauth";
        LoginTask login = new LoginTask(url);
        login.setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json");
        JSONObject bodyJson = new JSONObject();
        try {
            bodyJson.put("username", email)
                    .put("password", password)
                    .put("grant_type", "password")
                    .put("client_secret", getString(R.string.client_secret))
                    .put("client_id", getString(R.string.client_id));
            login.setBody(bodyJson.toString());
            login.execute();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void successLogin(JSONObject values) {
        Boolean isSuccess = false;
        if (progress.isShowing()) {
            progress.dismiss();
        }
        try {
            isSuccess = values.getBoolean("success");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        if (isSuccess) {
            Intent dashboard = new Intent(getApplication(), DashboardActivity.class);
            dashboard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            dashboard.putExtra("content", values.toString());
            startActivity(dashboard);
            finish();
        }
        else {
            Toast.makeText(getApplication(), "Login failed", Toast.LENGTH_LONG).show();
        }
    }

    private class LoginTask extends AsyncTask<Void, Long, JSONObject> {
        private HttpURLConnection connection;
        private List<String> body;

        public LoginTask(String Url) {
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

        public LoginTask setHeader(String type, String value) {
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

                result.put("success", false);
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String temp;
                    while ((temp = reader.readLine()) != null) {
                        builder.append(temp);
                    }
                    reader.close();
                    result = new JSONObject(builder.toString());
                    result.put("success", true);
                }
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
            Log.i("responseServer", jsonObject.toString());
            successLogin(jsonObject);
        }
    }
}