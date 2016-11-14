package com.aqilix.mobile.aqilix;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aqilix.mobile.aqilix.database.PairDataTable;
import com.aqilix.mobile.aqilix.database.UserProfileTable;
import com.aqilix.mobile.aqilix.library.DownloadTask;
import com.aqilix.mobile.aqilix.library.GetTask;
import com.aqilix.mobile.aqilix.library.MainFunction;
import com.aqilix.mobile.aqilix.model.UserProfileModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class ProfileActivity extends AppCompatActivity {

    public ProgressDialog progress;
    public PairDataTable pairTable;
    public UserProfileTable profileTable;
    public static String uuid;
    public SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        pairTable = new PairDataTable(getApplication());
        profileTable = new UserProfileTable(getApplication());
        uuid = pairTable.getValueOfKey("uuid");
        progress = new ProgressDialog(ProfileActivity.this);
        progress.setMessage(getString(R.string.progress_message));
        progress.setCancelable(false);
        progress.setInverseBackgroundForced(false);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.profileRefresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                syncProfile();
            }
        });
        Button editBtn = (Button) findViewById(R.id.btnEditProfile);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editProfile = new Intent(getApplication(), EditProfileActivity.class);
                startActivity(editProfile);
            }
        });
    }

    @Override
    protected void onStart() {
        progress.show();
        super.onStart();
        if (uuid != null && !uuid.equals("")) {
            UserProfileModel user = profileTable.getRowByUUID(uuid);
            if (user.getUuid() != null && !user.getUuid().equals("")) {
                updateUI(user);
            }
            else {
                syncProfile();
            }
        }
        else {
            progress.dismiss();
            Toast.makeText(getApplication(), "Failed fetch user, user not found", Toast.LENGTH_LONG).show();
        }
    }

    private void syncProfile() {
        String bearer = MainFunction.getToken(getApplication());
        if (bearer != null && !bearer.equals("")) {
            try {
                String auth = "Bearer " + bearer;
                String url = getString(R.string.host) + "/api/profile/" + uuid;
                GetTask get = new GetTask(url);
                get.setHeader("Content-Type", "application/vnd.aqilix.bootstrap.v1+json")
                        .setHeader("Accept", "application/json")
                        .setHeader("Authorization", auth);
                get.execute();
                JSONObject result = get.get();
                saveUserProfile(result);
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveUserProfile(JSONObject profile) {
        try {
            String uuid = profile.getString("uuid");
            String fName = profile.getString("firstName");
            String lName = profile.getString("lastName");
            String birth = profile.getString("dateOfBirth");
            Long longDOB = null;
            if (birth != null) {
                longDOB = MainFunction.DateToLong(birth, "yyyy-MM-dd");
            }
            String addr = profile.getString("address");
            String city = profile.getString("city");
            String prov = profile.getString("province");
            String postal = profile.getString("postalCode");
            String country = profile.getString("country");
            String user = profile.getString("user");
            String photo = profile.getString("photo");
            UserProfileModel model = new UserProfileModel(uuid, fName, lName, longDOB, addr, city, prov, postal, country, user, photo);
            if (photo != null && !photo.equals("")) {
                File dir = getExternalFilesDir(null);
                String filename = uuid + ".jpg";
                DownloadTask download = new DownloadTask(photo, dir.getAbsolutePath(), filename);
                download.execute();
            }

            if (profileTable.insert(model)) {
                updateUI(model);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateUI(UserProfileModel profileModel) {
        String name = "- No Name -";
        if (profileModel.getFirstName() != null) {
            name = profileModel.getFirstName();
        }
        if (profileModel.getLastName() != null) {
            name += (" " + profileModel.getLastName());
        }
        ((TextView) findViewById(R.id.profileName)).setText(name);
        String dob = (profileModel.getDateOfBirth() != null) ? profileModel.getStringDateOfBirth("dd-MM-yyyy") : "- No Data Available -";
        ((TextView) findViewById(R.id.profileDob)).setText(dob);
        String addr = (profileModel.getAddress() != null) ? profileModel.getAddress() : "- No Data Available -";
        ((TextView) findViewById(R.id.profileAddress)).setText(addr);
        String city = (profileModel.getCity() != null) ? profileModel.getCity() : "- No Data Available -";
        ((TextView) findViewById(R.id.profileCity)).setText(city);
        String prov = (profileModel.getProvince() != null) ? profileModel.getProvince() : "- No Data Available -";
        ((TextView) findViewById(R.id.profileProvince)).setText(prov);
        String postal = (profileModel.getPostalCode() != null) ? profileModel.getPostalCode() : "- No Data Available -";
        ((TextView) findViewById(R.id.profilePostal)).setText(postal);
        String country = (profileModel.getCountry() != null) ? profileModel.getCountry() : "- No Data Available -";
        ((TextView) findViewById(R.id.profileCountry)).setText(country);
        String user = (profileModel.getUser() != null) ? profileModel.getUser() : "- No Data Available -";
        ((TextView) findViewById(R.id.profileEmail)).setText(user);
        if (profileModel.getPhoto() != null && !profileModel.getPhoto().equals("")) {
            String path = getExternalFilesDir(null).getAbsolutePath() + "/" + uuid + ".jpg";
            File foto = new File(path);
            if (foto.exists()) {
                Bitmap img = MainFunction.decodeFileToBitmap(foto, 150, 150);
                ((ImageView) findViewById(R.id.profilePicture)).setImageBitmap(img);
            }
        }
        if (progress.isShowing()) {
            progress.dismiss();
        }
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
    }
}
