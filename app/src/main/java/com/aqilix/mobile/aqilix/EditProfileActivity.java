package com.aqilix.mobile.aqilix;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aqilix.mobile.aqilix.database.PairDataTable;
import com.aqilix.mobile.aqilix.database.UserProfileTable;
import com.aqilix.mobile.aqilix.library.MainFunction;
import com.aqilix.mobile.aqilix.library.PostTask;
import com.aqilix.mobile.aqilix.model.UserProfileModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class EditProfileActivity extends AppCompatActivity {

    public PairDataTable pairTable;
    public UserProfileTable profileTable;
    public ProgressDialog progress;
    public UserProfileModel profile;
    public String uuid;
    public EditText firstName;
    public EditText lastName;
    public TextView birth;
    public EditText address;
    public EditText city;
    public EditText province;
    public EditText postalCode;
    public EditText country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        pairTable = new PairDataTable(getApplication());
        profileTable = new UserProfileTable(getApplication());
        progress = new ProgressDialog(EditProfileActivity.this);
        uuid = pairTable.getValueOfKey("uuid");
        profile = profileTable.getRowByUUID(uuid);
        progress.setMessage("Saving...");
        progress.setCancelable(false);
        progress.setInverseBackgroundForced(false);

        firstName = (EditText) findViewById(R.id.editFirstName);
        firstName.setText(profile.getFirstName());
        lastName = (EditText) findViewById(R.id.editLastName);
        lastName.setText(profile.getLastName());
        birth = (TextView) findViewById(R.id.editDob);
        birth.setText(profile.getStringDateOfBirth("dd-MM-yyyy"));
        address = (EditText) findViewById(R.id.editAddress);
        address.setText(profile.getAddress());
        city = (EditText) findViewById(R.id.editCity);
        city.setText(profile.getCity());
        province = (EditText) findViewById(R.id.editProvince);
        province.setText(profile.getProvince());
        postalCode = (EditText) findViewById(R.id.editPostal);
        postalCode.setText(profile.getPostalCode());
        country = (EditText) findViewById(R.id.editCountry);
        country.setText(profile.getCountry());
        ((TextView) findViewById(R.id.editUser)).setText(profile.getUser());

        final Button pickDate = (Button) findViewById(R.id.btnPickDob);
        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar dob = Calendar.getInstance();
                if (profile.getDateOfBirth() != null && profile.getDateOfBirth() > 0) {
                    dob.setTimeInMillis(profile.getDateOfBirth());
                }
                DatePickerDialog picker = new DatePickerDialog(EditProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Calendar setter = Calendar.getInstance();
                        setter.set(i, i1, i2);
                        profile.setDateOfBirth(setter.getTimeInMillis());
                        birth.setText(profile.getStringDateOfBirth("dd-MM-yyyy"));
                    }
                }, dob.get(Calendar.YEAR), dob.get(Calendar.MONTH), dob.get(Calendar.DAY_OF_MONTH));
                picker.show();
            }
        });

        Button saveBtn = (Button) findViewById(R.id.btnSaveProfile);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.show();
                profile.setFirstName(firstName.getText().toString().trim());
                profile.setLastName(lastName.getText().toString().trim());
                profile.setAddress(address.getText().toString().trim());
                profile.setCity(city.getText().toString().trim());
                profile.setProvince(province.getText().toString().trim());
                profile.setPostalCode(postalCode.getText().toString().trim());
                profile.setCountry(country.getText().toString().trim());
                patchProfile();
            }
        });
    }

    private void dismissProgress() {
        if (progress.isShowing()) {
            progress.dismiss();
        }
    }

    private void patchProfile() {
        String token = MainFunction.getToken(getApplication());
        String message = "Update Profile Failed";
        if (token != null && !token.equals("")) {
            try {
                String url = getString(R.string.host) + "/api/profile/" + uuid;
                String auth = "Bearer " + token;
                PostTask post = new PostTask(url);
                post.setHeader("Content-Type", "application/vnd.aqilix.bootstrap.v1+json")
                        .setHeader("Accept", "application/json")
                        .setHeader("Authorization", auth)
                        .setRequestMethod("PATCH");

                JSONObject body = new JSONObject();
                body.put("firstName", profile.getFirstName())
                        .put("lastName", profile.getLastName())
                        .put("dateOfBirth", profile.getStringDateOfBirth("yyyy-MM-dd"))
                        .put("address", profile.getAddress())
                        .put("city", profile.getCity())
                        .put("province", profile.getProvince())
                        .put("postalCode", profile.getPostalCode());
                post.setBody(body.toString());
                post.execute();
                JSONObject result = post.get();
                if (result.getBoolean("success")) {
                    saveUserProfile(result);
                    Toast.makeText(getApplication(), "Update Profile Success", Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
                else {
                    dismissProgress();
                    message += (", " + result.getString("detail"));
                    Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
                }
            }
            catch (JSONException | InterruptedException | ExecutionException e) {
                dismissProgress();
                e.printStackTrace();
            }
        }
        else {
            dismissProgress();
            message += ", Invalid Token";
            Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show();
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
            UserProfileModel model = new UserProfileModel(uuid, fName, lName, longDOB, addr, city, prov, postal, country, user);
            profileTable.insert(model);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        dismissProgress();
    }
}
