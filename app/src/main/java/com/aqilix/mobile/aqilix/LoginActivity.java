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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import com.aqilix.mobile.aqilix.library.GetTask;
import com.aqilix.mobile.aqilix.orm.model.PairData;
import com.aqilix.mobile.aqilix.orm.helper.PairDataOpenDB;

public class LoginActivity extends AppCompatActivity {

    private ProgressDialog progress;

    private PairDataOpenDB pairDataOpenDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progress = new ProgressDialog(LoginActivity.this);
        progress.setMessage(getString(R.string.progress_message));
        progress.setCancelable(false);
        progress.setInverseBackgroundForced(false);

        // set PairData DB Helper
        pairDataOpenDBHelper = OpenHelperManager.getHelper(this, PairDataOpenDB.class);
        final EditText email = (EditText) findViewById(R.id.editTextEmail);
        final EditText password = (EditText) findViewById(R.id.editTextPassword);
        TextView reset = (TextView)findViewById(R.id.resetPassword);
        Button loginBtn = (Button) findViewById(R.id.btnLogin);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reset = new Intent(getApplication(),ResetActivity.class);
                startActivity(reset);
            }
        });
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
                    Toast.makeText(getApplication(), "Login failed, please fill username and password", Toast.LENGTH_LONG).show();
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

    /**
     * Do Login
     * @param email
     * @param password
     */
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

    /**
     * Dismiss progress
     */
    private void dismissProgress() {
        if (progress.isShowing()) {
            progress.dismiss();
        }
    }

    /**
     * Tasks after login success
     *
     * @param jsonResponse
     */
    public void successLogin(JSONObject jsonResponse) {
        try {
            Boolean isSuccess = jsonResponse.getBoolean("success");
            if (isSuccess) {
                pairDataOpenDBHelper.populateLoginData(jsonResponse);
                // retrieve uuid from /api/me
                String token = jsonResponse.getString("access_token");
                String url   = getString(R.string.host) + "/api/me";
                GetTask meResource = new GetTask(url);
                meResource.setHeader("Content-Type", "application/vnd.aqilix.bootstrap.v1+json")
                        .setHeader("Accept", "application/json")
                        .setHeader("Authorization", "Bearer " + token);
                meResource.execute();
                JSONObject meResourceResponse = meResource.get();
                Log.i("successLogin.meResource", meResourceResponse.toString());
                if (meResourceResponse.getBoolean("success")) {
                    String uuid = meResourceResponse.getString("uuid");
                    // insert uuid to pair data
                    PairData uuidPairData = new PairData("uuid", uuid);
                    pairDataOpenDBHelper.insert(uuidPairData);

                    // stop progress
                    dismissProgress();
                    // go to dashboardActivity
                    Intent dashboard = new Intent(getApplication(), DashboardActivity.class);
                    dashboard.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // put uuid on dashboard content
                    dashboard.putExtra("content", uuid);
                    startActivity(dashboard);
                    finish();
                } else {
                    dismissProgress();
                    Toast.makeText(getApplication(), meResourceResponse.getString("detail"), Toast.LENGTH_LONG).show();
                }
            } else {
                dismissProgress();
                String message = "Login failed, " + jsonResponse.getString("detail");
                Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException | InterruptedException | ExecutionException e) {
            dismissProgress();
            e.printStackTrace();
        }
    }

    /**
     * LoginTask
     */
    private class LoginTask extends AsyncTask<Void, Long, JSONObject> {

        private HttpURLConnection connection;

        private List<String> body;

        /**
         * Constructor
         *
         * @param url
         */
        public LoginTask(String url) {
            try {
                URL post = new URL(url);
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

        /**
         * Set Header
         *
         * @param type
         * @param value
         * @return
         */
        public LoginTask setHeader(String type, String value) {
            if (connection != null) {
                connection.setRequestProperty(type, value);
            }
            return this;
        }

        /**
         * Set Connection TimeOut
         *
         * @param timeout
         */
        public void setConnectTimeOut(Integer timeout) {
            connection.setConnectTimeout(timeout);
        }

        /**
         * Set Request Body
         *
         * @param body
         */
        public void setBody(String body) {
            if (body != null) {
                this.body.add(body);
            }
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            JSONObject jsonResponse = new JSONObject();

            try {
                connection.connect();
                OutputStream stream = new BufferedOutputStream(connection.getOutputStream());
                for (String strBody : body) {
                    stream.write(strBody.getBytes());
                }

                stream.flush();
                stream.close();
                InputStream iStream = null;
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    iStream = connection.getInputStream();
                } else {
                    iStream = connection.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
                StringBuilder builder = new StringBuilder();
                String temp;
                while ((temp = reader.readLine()) != null) {
                    builder.append(temp);
                }

                reader.close();
                Log.i("doinbackground.login", builder.toString());
                jsonResponse = new JSONObject(builder.toString());
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    jsonResponse.put("success", true);
                } else {
                    jsonResponse.put("success", false);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            finally {
                connection.disconnect();
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject jsonResponse) {
            super.onPostExecute(jsonResponse);
            successLogin(jsonResponse);
        }
    }
}
