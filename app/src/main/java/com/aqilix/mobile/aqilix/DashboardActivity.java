package com.aqilix.mobile.aqilix;

import java.util.concurrent.ExecutionException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.aqilix.mobile.aqilix.library.MainFunction;
import com.aqilix.mobile.aqilix.library.PostTask;
import com.aqilix.mobile.aqilix.orm.helper.PairDataOpenDB;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public class DashboardActivity extends AppCompatActivity {

    ProgressDialog progress;

    private PairDataOpenDB pairDataOpenDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        String content  = getIntent().getStringExtra("content");
        TextView sample = (TextView) findViewById(R.id.sampleContent);
        sample.setText(content);

        // set PairData DB Helper
        pairDataOpenDBHelper = OpenHelperManager.getHelper(this, PairDataOpenDB.class);
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

    /**
     * Show My Profile Activity
     */
    private void showMyProfile() {
        String uuid = pairDataOpenDBHelper.getValue("uuid");
        if (uuid != null) {
            Log.i("showMyProfile", uuid);
            Intent profile = new Intent(getApplication(), ProfileActivity.class);
            startActivity(profile);
        }
    }

    /**
     * Dismiss Progress
     */
    private void dismissProgress() {
        if (progress.isShowing()) {
            progress.dismiss();
        }
    }

    /**
     * Logout
     */
    private void doLogout() {
        try {
            String url = getString(R.string.host) + "/oauth/revoke";
            // TODO: 11/18/16 Optimize MainFunction
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
//                MainFunction.clearAll(getApplication());
                pairDataOpenDBHelper.drop();
                Intent login = new Intent(getApplication(), LoginActivity.class);
                login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(login);
                finish();
                Log.i("logout", "success");
            } else {
                message = message + ", " + result.getString("detail");
                Log.e("logout", result.getString("detail"));
            }

            Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
        }
        catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
    }
}
