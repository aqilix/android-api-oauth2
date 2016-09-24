package com.aqilix.mobile.aqilix;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aqilix.mobile.aqilix.database.PairDataTable;
import com.aqilix.mobile.aqilix.database.UserProfileTable;
import com.aqilix.mobile.aqilix.library.FormDataTask;
import com.aqilix.mobile.aqilix.library.MainFunction;
import com.aqilix.mobile.aqilix.model.UserProfileModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class EditProfileActivity extends AppCompatActivity {

    public PairDataTable pairTable;
    public UserProfileTable profileTable;
    public ProgressDialog progress;
    public UserProfileModel profile;
    public String uuid;
    public ImageView photo;
    public EditText firstName;
    public EditText lastName;
    public TextView birth;
    public EditText address;
    public EditText city;
    public EditText province;
    public EditText postalCode;
    public EditText country;
    protected static final Integer CAMERA_ACTION_REQUEST = 101;
    protected static final Integer SELECT_IMAGE_REQUEST = 202;
    protected SharedPreferences preferences;
    protected String photoPath;

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

        photo = (ImageView) findViewById(R.id.profilePicture);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerForContextMenu(view);
                openContextMenu(view);
            }
        });
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

        if (profile.getPhoto() != null && !profile.getPhoto().equals("")) {
            String path = getExternalFilesDir(null).getAbsolutePath() + "/" + uuid + ".jpg";
            File foto = new File(path);
            if (foto.exists()) {
                Bitmap img = MainFunction.decodeFileToBitmap(foto, 150, 150);
                ((ImageView) findViewById(R.id.profilePicture)).setImageBitmap(img);
            }
        }

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

        preferences = getSharedPreferences("aqilix_preferences", Context.MODE_PRIVATE);
        photoPath = preferences.getString("tempPath", null);
    }

    @Override
    public void onBackPressed() {
        preferences.edit().clear().apply();
        super.onBackPressed();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Photo Profile");
        menu.add(Menu.NONE, CAMERA_ACTION_REQUEST, 1, "Take Photo");
        menu.add(Menu.NONE, SELECT_IMAGE_REQUEST, 2, "Select Image");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CAMERA_ACTION_REQUEST) {
            String randName = MainFunction.createRandomString(10);
            File dir = getExternalFilesDir(null);
            try {
                File images = File.createTempFile(randName, ".jpg", dir);
                preferences.edit().putString("tempPath", images.getAbsolutePath()).apply();
                Uri fotoUri = Uri.fromFile(images);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, getRequestedOrientation());
                startActivityForResult(cameraIntent, CAMERA_ACTION_REQUEST);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (item.getItemId() == SELECT_IMAGE_REQUEST) {
            Intent gallery = new Intent();
            gallery.setType("image/*");
            gallery.setAction(Intent.ACTION_GET_CONTENT);
            Intent chooser = Intent.createChooser(gallery, "Select Image");
            startActivityForResult(chooser, SELECT_IMAGE_REQUEST);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            photoPath = null;
            if (requestCode == CAMERA_ACTION_REQUEST) {
                photoPath = preferences.getString("tempPath", null);
            }
            else if (requestCode == SELECT_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri selected = data.getData();
                photoPath = getPath(selected);
            }

            if (photoPath != null) {
                File toPreview = new File(photoPath);
                Bitmap resized = MainFunction.decodeFileToBitmap(toPreview, 200, 200);
                photo.setImageBitmap(resized);
            }
        }
        else {
            if (requestCode == CAMERA_ACTION_REQUEST) {
                photoPath = preferences.getString("tempPath", null);
                if (photoPath != null) {
                    File output = new File(photoPath);
                    output.delete();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void dismissProgress() {
        if (progress.isShowing()) {
            progress.dismiss();
        }
    }

    @SuppressLint("NewApi")
    private String getPath(Uri uri) {
        if( uri == null ) return null;

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor;
        if(Build.VERSION.SDK_INT > 19) {
            String wholeID = DocumentsContract.getDocumentId(uri);
            String id = wholeID.split(":")[1];
            String sel = MediaStore.Images.Media._ID + " = ?";
            cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, sel, new String[]{ id }, null);
        }
        else {
            cursor = getContentResolver().query(uri, projection, null, null, null);
        }

        String path = null;
        try {
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index).toString();
            cursor.close();
        }
        catch(NullPointerException e) {
            e.printStackTrace();
        }
        return path;
    }

    private void patchProfile() {
        String token = MainFunction.getToken(getApplication());
        String message = "Update Profile Failed";
        if (token != null && !token.equals("")) {
            try {
                String url = getString(R.string.host) + "/api/profile/" + uuid;
                String auth = "Bearer " + token;
                FormDataTask formData = new FormDataTask(url);
                formData.setHeader("Accept", "application/json")
                        .setHeader("Authorization", auth)
                        .setRequestMethod("PUT")
                        .setBody("firstName", profile.getFirstName())
                        .setBody("lastName", profile.getLastName())
                        .setBody("dateOfBirth", profile.getStringDateOfBirth("yyyy-MM-dd"))
                        .setBody("address", profile.getAddress())
                        .setBody("city", profile.getCity())
                        .setBody("province", profile.getProvince())
                        .setBody("postalCode", profile.getPostalCode())
                        .setBody("country", profile.getCountry());

                if (photoPath != null) {
                    File foto = new File(photoPath);
                    if (foto.exists()) {
                        formData.setFiles(foto);
                    }
                }
                formData.execute();
                JSONObject result = formData.get();
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
            catch (ExecutionException | InterruptedException | JSONException e) {
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
            String photo = profile.getString("photo");
            UserProfileModel model = new UserProfileModel(uuid, fName, lName, longDOB, addr, city, prov, postal, country, user, photo);
            profileTable.insert(model);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        dismissProgress();
    }
}
