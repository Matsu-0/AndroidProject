package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.w4lle.library.NineGridlayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.crypto.spec.GCMParameterSpec;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishPicActivity extends AppCompatActivity {
    private Button button_loadPic, button_loadPos, button_launch, button_takePhoto, button_clearPhoto;
    private EditText edit_title, edit_detail;
    private TextView location_text;
    private NineGridlayout nineGridlayout;
    private String title, content, location = null;
    private AlertDialog textTips;

    static final int PHOTO_RETURN_CODE = 0;
    static final int TAKE_PHOTO_RETURN_CODE = 1;
    private static final int PERMISSION_APPLY = 2;
    private static final int PERMISSION_APPLY_CAMERA = 3;
    private static final int REQUEST_EXTERNAL_STORAGE = 4;
    private static final String TAG = "PublishPic";

    private List<String> path = new ArrayList<>();
    private String currentPhotoPath;
    private static final int handlerStateWarning = 0;
    private static final int handlerGPSError = 1;
    private static final int handlerLocationSuccess = 2;

    private boolean GPSFlag = false;
    LocationManager locationManager;

    private SharedPreferences mPreferences;
    private String sharedPrefFile ="com.example.frontend.draft";
    private boolean tag_send_succeed = false;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                textTips = new AlertDialog.Builder(PublishPicActivity.this)
                        .setTitle("Tips:")
                        .setMessage(res)
                        .create();
                textTips.show();
                if (res.equals("????????????")){
                    tag_send_succeed = true;
                    finish();
                }
            }
            else if (msg.what == handlerGPSError){
                String res = (String) msg.obj;
                textTips = new AlertDialog.Builder(PublishPicActivity.this)
                        .setTitle("Tips:")
                        .setMessage(res)
                        .create();
                textTips.show();
            }
            else if (msg.what == handlerLocationSuccess){
                if (location != null && location.length() != 0){
                    if (GPSFlag){
                        location_text.setText("?????????"+location);
                    }
                    else {
                        location_text.setText(location);
                    }
                }
                else{
                    location_text.setText("?????????");
                }
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();  // ???????????????????????????

        SharedPreferences.Editor editor = mPreferences.edit();
        if (tag_send_succeed){
            editor.putInt("type", 0);
        }
        else{
            editor.putInt("type", 1);
        }
        editor.putString("title", edit_title.getText().toString());
        editor.putString("content", edit_detail.getText().toString());
        editor.putString("location", location);
        editor.putInt("pic_num", path.size());
        for (int i = 0; i < path.size(); ++i){
            editor.putString("pic_"+i, path.get(i));
        }
        editor.commit();
    }

    protected void onDestroy() {
        if (textTips != null) {
            textTips.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_pic);

        button_loadPic = (Button) findViewById(R.id.add_pic);
        button_loadPos = (Button) findViewById(R.id.add_position_pic);
        button_launch = (Button) findViewById(R.id.publish_button_pic);
        button_takePhoto = (Button) findViewById(R.id.take_photo);
        button_clearPhoto = (Button) findViewById(R.id.clear_all_pic);
        edit_title = (EditText) findViewById(R.id.publish_pic_title);
        edit_detail = (EditText) findViewById(R.id.publish_pic_detail);
        location_text = (TextView) findViewById(R.id.location_text);
        nineGridlayout = findViewById(R.id.iv_ngrid_layout);

        Intent intent = getIntent();
        int message = intent.getIntExtra("LOAD_DRAFT", -1);
        if (message != -1){
            mPreferences = getSharedPreferences(sharedPrefFile + "_" + message, MODE_PRIVATE);
            edit_title.setText( mPreferences.getString("title","") );
            edit_detail.setText( mPreferences.getString("content","") );
            location = mPreferences.getString("location","");
            if (location != null && location.length() != 0){
                location_text.setText("?????????"+location);
            }
            else{
                location_text.setText("?????????");
            }
            int num = mPreferences.getInt("pic_num", 0);
            if (num <= 9 && num > 0) {
                for (int i = 0; i < num; ++i){
                    path.add( mPreferences.getString("pic_"+i,""));
                    PicListAdapter adapter = new PicListAdapter(PublishPicActivity.this, path);
                    nineGridlayout.setAdapter(adapter);
                }
            }
        }
        else {
            // ??????????????????????????????
            SharedPreferences temp = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
            int cur = temp.getInt("size", -1);
            cur += 1;
            mPreferences = getSharedPreferences(sharedPrefFile + "_" +  cur, MODE_PRIVATE);
            SharedPreferences.Editor editor = temp.edit();
            editor.putInt("size", cur);
            editor.commit();
        }
        button_loadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPermission()) {
                    Intent intent = new Intent(PublishPicActivity.this, MultiImageSelectorActivity.class);
                    // whether show camera
                    intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, false);
                    // max select image amount
                    intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);
                    // select mode (MultiImageSelectorActivity.MODE_SINGLE OR MultiImageSelectorActivity.MODE_MULTI)
                    intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
                    // default select images (support array list)
                    intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, (ArrayList<String>) path);
                    startActivityForResult(intent, PHOTO_RETURN_CODE);
                }
            }
        });

        button_launch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestUrl = "http://43.138.84.226:8080/publish/picture";
                PublishPicActivity.MyThreadPhoto myThread = new PublishPicActivity.MyThreadPhoto(requestUrl);// TO DO
                myThread.start();// TO DO
            }
        });

        button_clearPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                path.clear();
                PicListAdapter adapter = new PicListAdapter(PublishPicActivity.this, path);
                nineGridlayout.setAdapter(adapter);
            }
        });

        button_loadPos.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                // ??????????????????
                if (Build.VERSION.SDK_INT >= 23) {
                    if(ContextCompat.checkSelfPermission(PublishPicActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        // ????????????????????????????????????
                        Log.d("????????????","false1");
                        ActivityCompat.requestPermissions(PublishPicActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_APPLY);
                    }
                }

                // ??????????????????
                if (Build.VERSION.SDK_INT >= 23) {
                    if(ContextCompat.checkSelfPermission(PublishPicActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        // ????????????????????????????????????
                        Log.d("????????????","false2");
                        ActivityCompat.requestPermissions(PublishPicActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_APPLY);
                    }
                }
                locationManager = (LocationManager) PublishPicActivity.this.getSystemService(Context.LOCATION_SERVICE);        // ??????Android GPS????????????
                if (PublishPicActivity.this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    //??????GPS???????????????????????????????????????
                    //Log.d("????????????","true");
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        //Log.d("????????????","false");
                        //?????????????????????GPS??????
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        PublishPicActivity.this.startActivity(intent);
                    }
                    else {
                        PublishPicActivity.MyThreadGetLocation locationThread = new PublishPicActivity.MyThreadGetLocation();// TO DO
                        locationThread.start();// TO DO
                    }
                }
            }
        });


        button_takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (path.size() >= 9) {
                    Message msg = handler.obtainMessage(handlerStateWarning);
                    msg.obj = "????????????????????????";
                    handler.sendMessage(msg);
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (ContextCompat.checkSelfPermission(PublishPicActivity.this,
                            Manifest.permission.CAMERA) != PackageManager
                            .PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PublishPicActivity.this, new
                                String[]{Manifest.permission.CAMERA }, PERMISSION_APPLY_CAMERA);
                        return;
                    }
                }
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent

                    // Create the File where the photo should go
                File photoFile = null;
                try {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "JPEG_" + timeStamp + "_";
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    photoFile = File.createTempFile(
                            imageFileName,  /* prefix */
                            ".jpg",         /* suffix */
                            storageDir      /* directory */
                    );
                    // Save a file: path for use with ACTION_VIEW intents
                    currentPhotoPath = photoFile.getAbsolutePath();
                } catch (IOException ex) {
                    Log.d("111","111");
                    // Error occurred while creating the File
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(PublishPicActivity.this,
                            "com.example.android.frontend",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, TAKE_PHOTO_RETURN_CODE);
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHOTO_RETURN_CODE){
            if(resultCode == RESULT_OK){
                // Get the result list of select image paths
                path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

                PicListAdapter adapter = new PicListAdapter(PublishPicActivity.this, path);
                nineGridlayout.setAdapter(adapter);

                // do your logic ....
            }
        }
        else if (requestCode == TAKE_PHOTO_RETURN_CODE){
            if(resultCode == RESULT_OK){
                // Get the result list of select image paths
                path.add(currentPhotoPath);
                PicListAdapter adapter = new PicListAdapter(PublishPicActivity.this, path);
                nineGridlayout.setAdapter(adapter);
            }
        }
        else if (requestCode == PERMISSION_APPLY_CAMERA){
            if(resultCode == RESULT_OK){
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent

                // Create the File where the photo should go
                File photoFile = null;
                try {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "JPEG_" + timeStamp + "_";
                    File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    photoFile = File.createTempFile(
                            imageFileName,  /* prefix */
                            ".jpg",         /* suffix */
                            storageDir      /* directory */
                    );
                    // Save a file: path for use with ACTION_VIEW intents
                    currentPhotoPath = photoFile.getAbsolutePath();
                } catch (IOException ex) {
                    Log.d("111","111");
                    // Error occurred while creating the File
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(PublishPicActivity.this,
                            "com.example.android.frontend",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, TAKE_PHOTO_RETURN_CODE);
                }
            }
        }
        else if (requestCode == REQUEST_EXTERNAL_STORAGE){

            if(resultCode == RESULT_OK){
                Intent intent = new Intent(PublishPicActivity.this, MultiImageSelectorActivity.class);
                // whether show camera
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, false);
                // max select image amount
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);
                // select mode (MultiImageSelectorActivity.MODE_SINGLE OR MultiImageSelectorActivity.MODE_MULTI)
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
                // default select images (support array list)
                intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, (ArrayList<String>) path);
                startActivityForResult(intent, PHOTO_RETURN_CODE);
            }
        }
    }

    private boolean getPermission() {


        String[] PERMISSIONS_STORAGE = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE" };

        int permission = ActivityCompat.checkSelfPermission(PublishPicActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    PublishPicActivity.this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE

            );
            return false;
        }
        return true;
    }

    class MyThreadPhoto extends Thread{
        private String requestUrl;
        MyThreadPhoto(String request){
            requestUrl = request;
        }
        @Override
        public void run() {
            if (path.size() >= 10) {
                Message msg = handler.obtainMessage(handlerStateWarning);
                msg.obj = "????????????????????????????????????" + path.size() + "???";
                handler.sendMessage(msg);
                return;
            }
            try {
                OkHttpClient client = new OkHttpClient();
//                file = new File(filepath);
                MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);
                for(int i = 0; i < path.size(); ++i){
                    File f = new File(path.get(i));
                    RequestBody fileBody = RequestBody.create(MEDIA_TYPE_PNG, f);
                    builder.addFormDataPart("pic_"+(i+1), f.getName(), fileBody);
                }

                // TO DO??????????????????????????????
                title = edit_title.getText().toString();

                if (title == null || title.length() == 0){
                    Message msg = handler.obtainMessage(handlerStateWarning);
                    msg.obj = "??????????????????";
                    handler.sendMessage(msg);
                    throw new IOException("Title is null");
                }

                content = edit_detail.getText().toString();

                if (content == null || content.length() == 0){
                    Message msg = handler.obtainMessage(handlerStateWarning);
                    msg.obj = "??????????????????";
                    handler.sendMessage(msg);
                    throw new IOException("Content is null");
                }

                RequestBody requestBody;

                if (location == null || location.length() == 0){
                    requestBody = builder
                            .addFormDataPart("title", title)
                            .addFormDataPart("content", content)
                            .addFormDataPart("pic_num", "" + path.size())
                            .build();
                }
                else {
                    requestBody = builder
                            .addFormDataPart("title", title)
                            .addFormDataPart("content", content)
                            .addFormDataPart("location", location)
                            .addFormDataPart("pic_num", "" + path.size())
                            .build();
                }
                // create a file to write bitmap data


                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(requestBody)
                        .addHeader("cookie",cookie)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.isSuccessful()) {
                    Message msg = handler.obtainMessage(handlerStateWarning);
                    msg.obj = Objects.requireNonNull(response.body()).string();
                    handler.sendMessage(msg);
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    class MyThreadGetLocation extends Thread {
        @Override
        public void run(){
            Looper.prepare();
            location = getProvince();
            Message msg = handler.obtainMessage(handlerLocationSuccess);
            handler.sendMessage(msg);
            Looper.loop();
        }

        @TargetApi(Build.VERSION_CODES.M)
        public String getProvince() {

            Log.i("GPS ", "getProvince");

            Location location = null;
            String p = "";
            // ??????????????????
            if (PublishPicActivity.this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //??????GPS???????????????????????????????????????
                //Log.d("????????????","true");

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        2000, 8, new LocationListener() {
                            // Provider??????????????????????????????????????????????????????????????????????????????????????????
                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                                Log.d(TAG, "onStatusChanged");
                            }
                            // Provider???enable???????????????????????????GPS?????????
                            @Override
                            public void onProviderEnabled(String provider) {
                                Log.d(TAG, "onProviderEnabled");
                            }

                            // Provider???disable???????????????????????????GPS?????????
                            @Override
                            public void onProviderDisabled(String provider) {
                                Log.d(TAG, "onProviderDisabled");
                            }

                            //??????????????????????????????????????????Provider?????????????????????????????????????????????
                            @Override
                            public void onLocationChanged(Location location) {
                                Log.d(TAG, String.format("location: longitude: %f, latitude: %f", location.getLongitude(),
                                        location.getLatitude()));
                                //??????????????????
                            }
                        });
                location = locationManager.getLastKnownLocation(provider);


                if (location != null) {
                    Log.i("GPS ", "????????????????????????");
                    Log.i("GPS ", "?????????" + location.getLatitude());
                    Log.i("GPS ", "?????????" + location.getLongitude());


                    p = getAddress(location.getLatitude(), location.getLongitude());

                } else {
                    Log.i("GPS ", "????????????????????????????????????????????????GPS,????????????");
                    Message msg = handler.obtainMessage(handlerGPSError);
                    msg.obj = "????????????????????????";
                    handler.sendMessage(msg);
                }
            }
            return p;
        }

        /*
         * ?????????????????? ?????????????????????
         * */
        public String getAddress(double latitude, double longitude) {
            String cityName = "";
            List<Address> addList = null;
            Geocoder ge = new Geocoder(PublishPicActivity.this);
            try {
                addList = ge.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addList != null && addList.size() > 0) {
                for (int i = 0; i < addList.size(); i++) {
                    Address ad = addList.get(i);
                    cityName += ad.getCountryName() + " " + ad.getLocality();
                }
                GPSFlag = true;
            }
            else {
                cityName += "?????????" + String.format("%.3f",latitude) + "\n" + "?????????" + String.format("%.3f",longitude);
                GPSFlag = false;
            }
            return cityName;
        }
    }
}
