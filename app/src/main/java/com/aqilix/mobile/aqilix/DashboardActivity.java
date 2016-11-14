package com.aqilix.mobile.aqilix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.aqilix.mobile.aqilix.database.PairDataTable;
import com.aqilix.mobile.aqilix.library.MainFunction;
import com.aqilix.mobile.aqilix.library.PostTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class DashboardActivity extends AppCompatActivity {

    protected PairDataTable pairTable;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        pairTable = new PairDataTable(getApplication());
        String content = getIntent().getStringExtra("content");
        TextView sample = (TextView) findViewById(R.id.sampleContent);
        sample.setText(content);
        if (content != null && !content.equals("")) {
        }

        progress = new ProgressDialog(DashboardActivity.this);
        progress.setMessage(getString(R.string.progress_message));
        progress.setCancelable(false);
        progress.setInverseBackgroundForced(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                showMyProfile();
                break;
            case R.id.logout:
                progress.show();
                doLogout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMyProfile() {
        String uuid = pairTable.getValueOfKey("uuid");
        if (uuid != null) {
            Intent profile = new Intent(getApplication(), ProfileActivity.class);
            startActivity(profile);
        }
    }

    private void dismissProgress() {
        if (progress.isShowing()) {
            progress.dismiss();
        }
    }

    private void doLogout() {
        try {
            String url = getString(R.string.host) + "/oauth/revoke";
            String token = MainFunction.getToken(getApplication());
            PostTask post = new PostTask(url);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .setHeader("Accept", "application/json");
            String body = "token=" + token + "&token_type_hint=access_token";
            post.setBody(body);
            post.execute();
            JSONObject result = post.get();
            dismissProgress();
            String message = "Logout Failed";
            if (result.getBoolean("success")) {
                message = "Logout success";
                MainFunction.clearAll(getApplication());
                Intent login = new Intent(getApplication(), LoginActivity.class);
                login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(login);
                finish();
            }
            else {
                message = message + ", " + result.getString("detail");
            }
            Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
        }
        catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
    }
}
