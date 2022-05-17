package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private String dataFile;
    private EditText edit_title, edit_detail;
    private TextView location_text;
    private String title, content, location = null;

    private SharedPreferences mPreferences;
    private String sharedPrefFile ="com.example.frontend.draft";

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
    public void onPause() {
        super.onPause();  // 首先调用父类的方法

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt("type", 2);
        editor.putString("title", edit_title.getText().toString());
        editor.putString("content", edit_detail.getText().toString());
        editor.putString("location", location);
        editor.putString("datafile", dataFile);
        editor.commit();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_video);
        dataFile = "";
        button_loadVideo = (Button) findViewById(R.id.add_video);
        button_loadPos = (Button) findViewById(R.id.add_position_video);
        button_launch = (Button) findViewById(R.id.publish_button_video);
        button_takeVideo = (Button) findViewById(R.id.take_video);
        button_clearVideo = (Button) findViewById(R.id.clear_all_video);
        videoLayout = findViewById(R.id.video_layout);
        videoLayout.setVisibility(View.GONE);

        edit_title = (EditText) findViewById(R.id.publish_video_title);
        edit_detail = (EditText) findViewById(R.id.publish_video_detail);
        location_text = (TextView) findViewById(R.id.location_text);


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
            dataFile = mPreferences.getString("datafile","");
            if (dataFile != null && dataFile.length() != 0){
                videoUri = Uri.parse(dataFile);
                videoLayout.setVideoURI(videoUri);
                videoLayout.setVisibility(View.VISIBLE);
                videoLayout.start();
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
                dataFile = "";
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




    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == TAKE_VIDEO_RETURN_CODE){
            if(resultCode == RESULT_OK){
                videoUri = data.getData();
                File file = getFile(videoUri);
                dataFile = file.getAbsolutePath();
                videoLayout.setVideoURI(videoUri);
                videoLayout.setVisibility(View.VISIBLE);
                videoLayout.start();
            }
        }
        else if(requestCode == VIDEO_RETURN_CODE){
            if(resultCode == RESULT_OK){
                videoUri = data.getData();
                File file = getFile(videoUri);
                dataFile = file.getAbsolutePath();
                videoLayout.setVideoURI(videoUri);
                videoLayout.setVisibility(View.VISIBLE);
                videoLayout.start();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private File getFile(Uri uri){
        //android10以上转换
        File file = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = PublishVideoActivity.this.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                try {
                    InputStream is = contentResolver.openInputStream(uri);
                    File cache = new File(PublishVideoActivity.this.getExternalCacheDir().getAbsolutePath(), Math.round((Math.random() + 1) * 1000) + displayName);
                    FileOutputStream fos = new FileOutputStream(cache);
                    FileUtils.copy(is, fos);
                    file = cache;
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    class MyThreadVideo extends Thread{
        private String requestUrl;
        MyThreadVideo(String request){
            requestUrl = request;
        }
        @RequiresApi(api = Build.VERSION_CODES.Q)
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
                File file = getFile(videoUri);
                dataFile = file.getAbsolutePath();
                RequestBody fileBody = RequestBody.create(MEDIA_TYPE_VIDEO, file);
                builder.addFormDataPart("video", file.getName(), fileBody);

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
                // TO DO：修改参数并添加判断
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