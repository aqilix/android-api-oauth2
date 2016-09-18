package com.aqilix.mobile.aqilix;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.aqilix.mobile.aqilix.database.PairDataTable;

import org.json.JSONException;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity {

    protected PairDataTable pairTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        pairTable = new PairDataTable(getApplication());
        String content = getIntent().getStringExtra("content");
        TextView sample = (TextView) findViewById(R.id.sampleContent);
        sample.setText(content);
        if (content != null && !content.equals("")) {
            try {
                JSONObject json = new JSONObject(content);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
}
