package com.aqilix.mobile.aqilix.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.MimeTypeMap;

import com.aqilix.mobile.aqilix.R;
import com.aqilix.mobile.aqilix.database.PairDataTable;
import com.aqilix.mobile.aqilix.database.UserProfileTable;
import com.aqilix.mobile.aqilix.model.PairDataModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by surya on 9/18/16.
 */
public class MainFunction {

    protected Context context;
    public MainFunction(Context context) {
        this.context = context;
    }

    public static String getToken(Context context) {
        PairDataTable pairTable = new PairDataTable(context);
        Long insertTime = Long.valueOf(pairTable.getValueOfKey("insert_time"));
        Long now = System.currentTimeMillis();
        Long expiresIn = Long.valueOf(pairTable.getValueOfKey("expires_in"));
        Long duration = (now - insertTime) / 1000;
        String token = null;
        if (duration < expiresIn) {
            token = pairTable.getValueOfKey("access_token");
        }
        else {
            String refreshToken = pairTable.getValueOfKey("refresh_token");
            if (refreshToken != null) {
                try {
                    String url = context.getString(R.string.host) + "/oauth";
                    PostTask post = new PostTask(url);
                    post.setHeader("Content-Type", "application/json")
                            .setHeader("Accept", "application/json");

                    JSONObject body = new JSONObject();
                    body.put("grant_type", "refresh_token")
                            .put("client_secret", context.getString(R.string.client_secret))
                            .put("client_id", context.getString(R.string.client_id))
                            .put("refresh_token", refreshToken);
                    post.setBody(body.toString());
                    post.execute();
                    JSONObject result = post.get();
                    if (result.getBoolean("success")) {
                        token = result.getString("access_token");
                        Iterator<String> keys = result.keys();
                        List<PairDataModel> listModel = new ArrayList<>();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String value = result.getString(key);
                            PairDataModel model = new PairDataModel(key, value);
                            listModel.add(model);
                        }

                        if (listModel.size() > 0) {
                            PairDataModel timeModel = new PairDataModel("insert_time", String.valueOf(System.currentTimeMillis()));
                            listModel.add(timeModel);
                            pairTable.bulkInsert(listModel);
                        }
                    }
                }
                catch (JSONException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        return token;
    }

    public static Long DateToLong(String dateString, String format) {
        if (dateString == null) return null;
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
        Long result = 0L;
        try {
            Date date = formatter.parse(dateString);
            result = date.getTime();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Boolean isNetworkConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    public static String createRandomString(Integer len) {
        Random rand = new Random();
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String unique = "";
        for (Integer i = 0; i <= len; i++) {
            unique += characters.charAt(rand.nextInt(characters.length()));
        }
        return unique;
    }

    public static Bitmap decodeFileToBitmap(File file, int reqWidth, int reqHeight) {
        if (file == null || !file.exists()) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        Integer angle = 0;
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            String orient = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            Integer exifOrient = orient != null ? Integer.valueOf(orient) : ExifInterface.ORIENTATION_NORMAL;
            switch (exifOrient) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                default:
                    angle = 0;
                    break;
            }
        }
        catch (IOException ie) {
            ie.printStackTrace();
        }

        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        final int oHeight = options.outHeight;
        final int oWidth = options.outWidth;
        Integer inSampleSize = 1;
        if (oHeight > reqHeight || oWidth > reqWidth) {
            Integer hFactor = oHeight / reqHeight;
            Integer wFactor = oWidth / reqWidth;
            inSampleSize = Math.max(hFactor, wFactor);
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        Bitmap decoded = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        Bitmap result = Bitmap.createBitmap(decoded, 0, 0, decoded.getWidth(), decoded.getHeight(), matrix, true);
        if (result.getByteCount() > 2000000) {
            int newHeight = reqHeight / 2;
            int newWidth = reqWidth / 2;
            result = decodeFileToBitmap(file, newWidth, newHeight);
        }
        return result;
    }

    public static String getMimeType(String path) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getExtensionFromMimeType(extension);
        }
        return type;
    }

    public static void clearAll(Context context) {
        new PairDataTable(context).deleteAllPairData();
        new UserProfileTable(context).deleteAllUser();
    }
}
