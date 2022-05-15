package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
    private Button button_loadAudio, button_loadPos, button_launch, button_recordAudio;
    private NineGridlayout nineGridlayout;
    private static final int handlerStateWarning = 0;
    private final int REQUEST_CODE = 111;
    private static final String TAG = "PublishAudio";
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
        nineGridlayout = findViewById(R.id.iv_ngrid_layout);

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
                Log.d(TAG, "dataFile: " + dataFile);
            }
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

