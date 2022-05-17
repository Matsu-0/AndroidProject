package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.VideoView;

import com.w4lle.library.NineGridlayout;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishAudio extends AppCompatActivity {
    private Button button_loadAudio, button_loadPos, button_launch, button_recordAudio, button_playAudio, button_clearAudio;
    //private VideoView audio_View;
    private MediaPlayer audio_View = new MediaPlayer();
    private static final int handlerStateWarning = 0;
    private final int REQUEST_CODE = 111;
    static final int LOAD_AUDIO_RETURN_CODE = 1;
    private static final String TAG = "PublishAudio";
    private EditText edit_title, edit_detail;
    private TextView location_text, audio_filename;
    private String title, content, location = null;
    private boolean haveAudio = false;
    private String dataFile;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(PublishAudio.this)
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

        button_launch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestUrl = "http://43.138.84.226:8080/publish/audio";
                PublishAudio.MyThreadAudio myThread = new PublishAudio.MyThreadAudio(requestUrl);// TO DO
                myThread.start();// TO DO
            }
        });

        button_recordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        button_loadAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, LOAD_AUDIO_RETURN_CODE);
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
    }

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
}

