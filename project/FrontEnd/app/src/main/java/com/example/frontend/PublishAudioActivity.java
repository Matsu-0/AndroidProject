package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishAudioActivity extends AppCompatActivity {
    private Button button_loadAudio, button_loadPos, button_launch, button_recordAudio, button_playAudio, button_clearAudio;
    //private VideoView audio_View;
    private MediaPlayer audio_View = new MediaPlayer();
    private static final int handlerStateWarning = 0;
    private static final int handlerGPSError = 1;
    private static final int handlerLocationSuccess = 2;

    private final int REQUEST_CODE = 111;
    static final int LOAD_AUDIO_RETURN_CODE = 1;
    private static final int PERMISSION_APPLY = 2;
    private static final int PERMISSION_APPLY_EXTERNAL = 3;
    private static final String TAG = "PublishAudioActivity";
    private EditText edit_title, edit_detail;
    private TextView location_text, audio_filename;
    private String title, content, location = null;
    private AlertDialog textTips;

    private boolean GPSFlag = false;
    LocationManager locationManager;

    private SharedPreferences mPreferences;
    private String sharedPrefFile ="com.example.frontend.draft";

    private boolean haveAudio = false;
    private boolean tag_send_succeed = false;
    private String dataFile;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(PublishAudioActivity.this)
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
                textTips = new AlertDialog.Builder(PublishAudioActivity.this)
                        .setTitle("Tips:")
                        .setMessage(res)
                        .create();
                textTips.show();
            }
            else if (msg.what == handlerLocationSuccess){
                if (location != null && location.length() != 0){
                    if (GPSFlag){
                        location_text.setText("位置："+location);
                    }
                    else {
                        location_text.setText(location);
                    }
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
            editor.putInt("type", 3);
        }
        editor.putString("title", edit_title.getText().toString());
        editor.putString("content", edit_detail.getText().toString());
        editor.putString("location", location);
        editor.putString("datafile", dataFile);
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_audio);
        button_loadAudio = (Button) findViewById(R.id.add_audio);
        button_loadPos = (Button) findViewById(R.id.add_position_audio);
        button_launch = (Button) findViewById(R.id.publish_button_audio);
        button_recordAudio = (Button) findViewById(R.id.record_sound);
        button_clearAudio = (Button) findViewById(R.id.clear_all_audio);
        button_playAudio = (Button) findViewById(R.id.audio_play);
        //audio_View = findViewById(R.id.audio_layout);

        edit_title = (EditText) findViewById(R.id.publish_audio_title);
        edit_detail = (EditText) findViewById(R.id.publish_audio_detail);
        location_text = (TextView) findViewById(R.id.location_text);
        audio_filename = (TextView) findViewById(R.id.audio_filename);

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
            dataFile = mPreferences.getString("datafile",null);
            try{
                audio_View.release();
                audio_View = null;
                audio_View = new MediaPlayer();
                audio_View.setDataSource(dataFile);
                audio_View.prepare();
                changeButton(true);
                audio_filename.setText(dataFile);
            }
            catch (Exception e)
            {
                changeButton(false);
                e.printStackTrace();
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

        button_launch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestUrl = "http://43.138.84.226:8080/publish/audio";
                PublishAudioActivity.MyThreadAudio myThread = new PublishAudioActivity.MyThreadAudio(requestUrl);// TO DO
                myThread.start();// TO DO
            }
        });

        button_recordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPermission()) {
                    Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    startActivityForResult(intent, REQUEST_CODE);
                }

            }
        });

        button_loadAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPermission()) {
                    Intent intent = new Intent();
                    intent.setType("audio/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, LOAD_AUDIO_RETURN_CODE);
                }
            }
        });

        button_clearAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audio_View.release();//停止播放视频,并且释放
                changeButton(false);
                dataFile = null;
            }
        });

        button_playAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (haveAudio){
                    if (audio_View.isPlaying()){
                        audio_View.pause();
                        button_playAudio.setText("播放");
                    }
                    else {
                        audio_View.start();
                        button_playAudio.setText("暂停");
                    }
                }
                else {
                    Message msg = handler.obtainMessage(handlerStateWarning);
                    msg.obj = "未添加音频";
                    handler.sendMessage(msg);
                }
            }
        });
        button_loadPos.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                // 动态权限申请
                if (Build.VERSION.SDK_INT >= 23) {
                    if(ContextCompat.checkSelfPermission(PublishAudioActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 申请读写内存卡内容的权限
                        Log.d("是否授权","false1");
                        ActivityCompat.requestPermissions(PublishAudioActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSION_APPLY);
                    }
                }

                // 动态权限申请
                if (Build.VERSION.SDK_INT >= 23) {
                    if(ContextCompat.checkSelfPermission(PublishAudioActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 申请读写内存卡内容的权限
                        Log.d("是否授权","false2");
                        ActivityCompat.requestPermissions(PublishAudioActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_APPLY);
                    }
                }
                locationManager = (LocationManager) PublishAudioActivity.this.getSystemService(Context.LOCATION_SERVICE);        // 默认Android GPS定位实例
                if (PublishAudioActivity.this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    //判断GPS是否开启，没有开启，则开启
                    //Log.d("是否授权","true");
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        //Log.d("是否开启","false");
                        //跳转到手机打开GPS页面
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        PublishAudioActivity.this.startActivity(intent);
                    }
                    else {
                        PublishAudioActivity.MyThreadGetLocation locationThread = new PublishAudioActivity.MyThreadGetLocation();// TO DO
                        locationThread.start();// TO DO
                    }
                }
            }
        });    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        audio_View.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            //请求
            if (requestCode == REQUEST_CODE) {
                //得到录音的音频文件及路径
                Uri dataUri = data.getData();
                dataFile = getRealPathFromURI(dataUri);
                Log.d("filepath", dataFile);
                try{
                    audio_View.release();
                    audio_View = null;
                    audio_View = new MediaPlayer();
                    audio_View.setDataSource(dataFile);
                    audio_View.prepare();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                changeButton(true);
                audio_filename.setText(dataFile);
                Log.d(TAG, "dataFile: " + dataFile);
            }
            else if(requestCode == LOAD_AUDIO_RETURN_CODE) {
                //得到录音的音频文件及路径
                Uri dataUri = data.getData();
                dataFile = getRealPathFromURI(dataUri);
                try{
                    audio_View.release();
                    audio_View = null;
                    audio_View = new MediaPlayer();
                    audio_View.setDataSource(dataFile);
                    audio_View.prepare();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                changeButton(true);
                audio_filename.setText(dataFile);
                Log.d(TAG, "dataFile: " + dataFile);
            }
        }

    }

    private void changeButton(boolean nextState) {
        if (nextState) {
            haveAudio = true;
            button_playAudio.setText("播放");
        }
        else {
            haveAudio = false;
            button_playAudio.setText("无法播放");
            audio_filename.setText("未添加音频");
        }
    }

    public String getRealPathFromURI(Uri contentUri) {

        String[] proj = { MediaStore.Images.Media.DATA };

        Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(column_index);

    }

    class MyThreadAudio extends Thread{
        private String requestUrl;
        MyThreadAudio(String request){
            requestUrl = request;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
//                file = new File(filepath);
                MediaType MEDIA_TYPE_AUDIO = MediaType.parse("audio/aac");
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);

                File f = new File(dataFile);
                RequestBody fileBody = RequestBody.create(MEDIA_TYPE_AUDIO, f);
                builder.addFormDataPart("audio", f.getName(), fileBody);

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
                            .build();
                }
                else {
                    requestBody = builder
                            .addFormDataPart("title", title)
                            .addFormDataPart("content", content)
                            .addFormDataPart("location", location)
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

    private boolean getPermission() {
        String[] PERMISSIONS_STORAGE = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE" };

        int permission = ActivityCompat.checkSelfPermission(PublishAudioActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    PublishAudioActivity.this,
                    PERMISSIONS_STORAGE,
                    PERMISSION_APPLY_EXTERNAL
            );
            return false;
        }
        return true;
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
            // 是否已经授权
            if (PublishAudioActivity.this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
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


                    p = getAddress(location.getLatitude(), location.getLongitude());

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
            Geocoder ge = new Geocoder(PublishAudioActivity.this);
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
                cityName += "经度：" + String.format("%.3f",latitude) + "\n" + "纬度：" + String.format("%.3f",longitude);
                GPSFlag = false;
            }
            return cityName;
        }
    }

}

