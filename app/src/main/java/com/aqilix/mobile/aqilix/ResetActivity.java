package com.aqilix.mobile.aqilix;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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
import java.util.List;

public class ResetActivity extends AppCompatActivity {

    private ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        progress = new ProgressDialog(ResetActivity.this);
        progress.setMessage(getString(R.string.progress_message));
        progress.setCancelable(false);
        progress.setInverseBackgroundForced(false);

        final TextView email = (TextView) findViewById(R.id.editTextEmail);
        Button reset = (Button)findViewById(R.id.btnReset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.show();
                String strMail = email.getText().toString();
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

    public void successReset(JSONObject values) {
        try {
            Boolean isSuccess = values.getBoolean("success");
            if (isSuccess) {
                dismissProgress();
                String message = "Reset Password success";
                Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
            }
            else {
                dismissProgress();
                String message = "Reset Password failed, " + values.getString("detail");
                Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
            }
        }
        catch (JSONException e) {
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
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = new JSONObject("{}");
                    result.put("success", true);
                } else {
                    result = new JSONObject(builder.toString());
                    result.put("success", false);
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
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            successReset(jsonObject);
        }
    }
}
