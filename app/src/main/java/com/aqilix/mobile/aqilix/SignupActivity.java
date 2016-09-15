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

public class SignupActivity extends AppCompatActivity {

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        progress = new ProgressDialog(SignupActivity.this);
        progress.setMessage("Loading...");
        progress.setCancelable(false);
        progress.setInverseBackgroundForced(false);

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

    private void goToLogin() {
        Intent login = new Intent(getApplication(), LoginActivity.class);
        login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(login);
        overridePendingTransition(0, 0);
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

    private void successRegister(Boolean success) {
        if (progress.isShowing()) {
            progress.dismiss();
        }
        if (success) {
            Toast.makeText(getApplication(), "Register success", Toast.LENGTH_LONG).show();
            goToLogin();
        }
        else {
            Toast.makeText(getApplication(), "Register failed", Toast.LENGTH_LONG).show();
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
                result.put("success", false);
                connection.connect();
                OutputStream stream = new BufferedOutputStream(connection.getOutputStream());
                for (String strBody : body) {
                    stream.write(strBody.getBytes());
                }
                stream.flush();
                stream.close();

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
            try {
                Boolean success = jsonObject.getBoolean("success");
                successRegister(success);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
