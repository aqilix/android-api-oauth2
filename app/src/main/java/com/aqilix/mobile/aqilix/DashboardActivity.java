package com.aqilix.mobile.aqilix;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
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
}
