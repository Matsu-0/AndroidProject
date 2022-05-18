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
    private static final String TAG = "PublishPic";

    private List<String> path = new ArrayList<>();
    private String currentPhotoPath;
    private static final int handlerStateWarning = 0;
    private static final int handlerGPSError = 1;
    private static final int handlerOpenGPS = 2;
    private static final int handlerChangeLocation = 3;
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
                if (res.equals("发布成功")){
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
            else if (msg.what == handlerChangeLocation){
                if (location != null && location.length() != 0){
                    location_text.setText("位置："+location);
                }
                else{
                    location_text.setText("位置：");
                }
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();  // 首先调用父类的方法

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
                location_text.setText("位置："+location);
            }
            else{
                location_text.setText("位置：");
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
            // 如果不是从草稿箱导入
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
                // 动态权限申请
                if (Build.VERSION.SDK_INT >= 23) {
                    if(ContextCompat.checkSelfPermission(PublishPicActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 申请读写内存卡内容的权限
                        Log.d("是否授权","false1");
                        ActivityCompat.requestPermissions(PublishPicActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_APPLY);
                    }
                }

                // 动态权限申请
                if (Build.VERSION.SDK_INT >= 23) {
                    if(ContextCompat.checkSelfPermission(PublishPicActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 申请读写内存卡内容的权限
                        Log.d("是否授权","false2");
                        ActivityCompat.requestPermissions(PublishPicActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_APPLY);
                    }
                }
                locationManager = (LocationManager) PublishPicActivity.this.getSystemService(Context.LOCATION_SERVICE);        // 默认Android GPS定位实例
                if (PublishPicActivity.this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    //判断GPS是否开启，没有开启，则开启
                    //Log.d("是否授权","true");
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        //Log.d("是否开启","false");
                        //跳转到手机打开GPS页面
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
                    msg.obj = "至多允许九张图片";
                    handler.sendMessage(msg);
                    return;
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
                msg.obj = "至多允许九张图片，目前有" + path.size() + "张";
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

                // TO DO：修改参数并添加判断
                title = edit_title.getText().toString();

                if (title == null || title.length() == 0){
                    Message msg = handler.obtainMessage(handlerStateWarning);
                    msg.obj = "标题不能为空";
                    handler.sendMessage(msg);
                    throw new IOException("Title is null");
                }

                content = edit_detail.getText().toString();

                if (content == null || content.length() == 0){
                    Message msg = handler.obtainMessage(handlerStateWarning);
                    msg.obj = "内容不能为空";
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
            location = getProvince();
            Message msg = handler.obtainMessage(handlerChangeLocation);
            handler.sendMessage(msg);
            return;
        }

        @TargetApi(Build.VERSION_CODES.M)
        public String getProvince() {

            Log.i("GPS ", "getProvince");

            Location location = null;
            String p = "";
            // 是否已经授权
            if (PublishPicActivity.this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //判断GPS是否开启，没有开启，则开启
                //Log.d("是否授权","true");

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = locationManager.getBestProvider(criteria, true);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        2000, 8, new LocationListener() {
                            // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                                Log.d(TAG, "onStatusChanged");
                            }
                            // Provider被enable时触发此函数，比如GPS被打开
                            @Override
                            public void onProviderEnabled(String provider) {
                                Log.d(TAG, "onProviderEnabled");
                            }

                            // Provider被disable时触发此函数，比如GPS被关闭
                            @Override
                            public void onProviderDisabled(String provider) {
                                Log.d(TAG, "onProviderDisabled");
                            }

                            //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                            @Override
                            public void onLocationChanged(Location location) {
                                Log.d(TAG, String.format("location: longitude: %f, latitude: %f", location.getLongitude(),
                                        location.getLatitude()));
                                //更新位置信息
                            }
                        });
                location = locationManager.getLastKnownLocation(provider);


                if (location != null) {
                    Log.i("GPS ", "获取位置信息成功");
                    Log.i("GPS ", "经度：" + location.getLatitude());
                    Log.i("GPS ", "纬度：" + location.getLongitude());

                    // 获取地址信息
                    p = getAddress(location.getLatitude(), location.getLongitude());
                    Log.i("GPS ", "location：" + p);
                } else {
                    Log.i("GPS ", "获取位置信息失败，请检查是否开启GPS,是否授权");
                    Message msg = handler.obtainMessage(handlerGPSError);
                    msg.obj = "定位失败，请重试";
                    handler.sendMessage(msg);
                }
            }
            return p;
        }

        /*
         * 根据经度纬度 获取国家，省份
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
            }
            return cityName;
        }
    }
}
