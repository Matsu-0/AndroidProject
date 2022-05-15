package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishVideoActivity extends AppCompatActivity {
    private Button button_loadVideo, button_loadPos, button_launch, button_takeVideo, button_clearVideo;
    private VideoView videoLayout;
    static final int VIDEO_RETURN_CODE = 0;
    static final int TAKE_VIDEO_RETURN_CODE = 1;
    private Uri videoUri;
    private static final int handlerStateWarning = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(PublishVideoActivity.this)
                        .setTitle("Tips:")
                        .setMessage(res)
                        .create();
                textTips.show();
                if (res == "发布成功"){
                    finish();
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_video);

        button_loadVideo = (Button) findViewById(R.id.add_video);
        button_loadPos = (Button) findViewById(R.id.add_position_video);
        button_launch = (Button) findViewById(R.id.publish_button_video);
        button_takeVideo = (Button) findViewById(R.id.take_video);
        button_clearVideo = (Button) findViewById(R.id.clear_all_video);
        videoLayout = findViewById(R.id.video_layout);
        videoLayout.setVisibility(View.GONE);

        button_launch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestUrl = "http://43.138.84.226:8080/publish/video";
                PublishVideoActivity.MyThreadVideo myThread = new PublishVideoActivity.MyThreadVideo(requestUrl);// TO DO
                myThread.start();// TO DO
            }
        });

        button_takeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(takeVideoIntent, TAKE_VIDEO_RETURN_CODE);
            }
        });

        button_clearVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoLayout.stopPlayback();//停止播放视频,并且释放
                videoLayout.suspend();
                videoLayout.setVisibility(View.GONE);
            }
        });

        button_loadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*"); //选择视频 (mp4 3gp 是android支持的视频格式)
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, VIDEO_RETURN_CODE);
            }
        });

        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoLayout.isPlaying()){
                    videoLayout.pause();
                }
                else {
                    videoLayout.start();
                }
            }
        });
    }

    public String getRealPathFromURI(Uri contentUri) {

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == TAKE_VIDEO_RETURN_CODE){
            if(resultCode == RESULT_OK){
                videoUri = data.getData();
                videoLayout.setVideoURI(videoUri);
                videoLayout.setVisibility(View.VISIBLE);
                videoLayout.start();
            }
        }
        else if(requestCode == VIDEO_RETURN_CODE){
            if(resultCode == RESULT_OK){
                videoUri = data.getData();
                videoLayout.setVideoURI(videoUri);
                videoLayout.setVisibility(View.VISIBLE);
                videoLayout.start();
            }
        }
    }

    class MyThreadVideo extends Thread{
        private String requestUrl;
        MyThreadVideo(String request){
            requestUrl = request;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
//                file = new File(filepath);
                MediaType MEDIA_TYPE_VIDEO = MediaType.parse("video/*");
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);
                Log.d("videoUri",videoUri.toString());
                File f = new File(new URI(videoUri.toString()));
                RequestBody fileBody = RequestBody.create(MEDIA_TYPE_VIDEO, f);
                builder.addFormDataPart("video", f.getName(), fileBody);

                // TO DO：修改参数并添加判断
                RequestBody requestBody = builder
                        .addFormDataPart("title", "test")
                        .addFormDataPart("content", "test")
                        .addFormDataPart("location", "test")
                        .build();
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
}