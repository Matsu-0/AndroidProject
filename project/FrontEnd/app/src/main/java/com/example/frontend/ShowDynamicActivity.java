package com.example.frontend;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShowDynamicActivity extends AppCompatActivity {
    private static final String LOG_TAG = ShowDynamicActivity.class.getSimpleName();
    TextView title, detail, location, audioFilename, likeListNum;
    Button audioPlayer;
    int DynamicID;
    private static final int handlerStateWarning = 0;
    private static final int handlerPicDynamic = 1;
    private static final int handlerAudioDynamic = 2;
    private static final int handlerVideoDynamic = 3;
    private static final int handlerBasicDynamic = 4;
    private static final int handlerLikeListNum = 5;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(ShowDynamicActivity.this)
                        .setTitle("Tips:")
                        .setMessage(res)
                        .create();
                textTips.show();
            }
            else if (msg.what == handlerBasicDynamic) {
                JSONObject res = (JSONObject) msg.obj;
                try{
                    title.setText(res.getString("title"));
                    detail.setText(res.getString("content"));
                    String l = res.getString("location");
                    if (l != null && l.length() != 0) {
                        location.setText("l");
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            else if (msg.what == handlerLikeListNum) {
                int res = (int) msg.obj;
                likeListNum.setText("点赞：" + res + "人");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_dynamic);
        title = (TextView) findViewById(R.id.dynamic_title);
        detail = (TextView) findViewById(R.id.dynamic_detail);
        location = (TextView) findViewById(R.id.location_text);
        audioFilename = (TextView) findViewById(R.id.audio_filename);
        likeListNum = (TextView) findViewById(R.id.dynamic_like);
        audioPlayer = (Button) findViewById(R.id.audio_play);
        Intent intent = getIntent();
        DynamicID = intent.getIntExtra("dynamic_id", 0);
        String requestUrl = "http://43.138.84.226:8080/demonstrate/show_dynamic" + DynamicID;

    }

    class MyThreadInitData extends Thread{
        private  String requestUrl;
        MyThreadInitData(String request){
            requestUrl = request;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();

                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");
                Log.d(LOG_TAG, cookie);
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .get()
                        .addHeader("cookie",cookie)
                        .build();
                Log.d(LOG_TAG, cookie);
                Call call = client.newCall(request);
                Response response = call.execute();
                Log.d(LOG_TAG, response.toString());

                if (response.isSuccessful()) {

                    if (response.code() == 200){
                        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string()); // String 转 JSONObject
                        JSONObject dynamic_info = new JSONObject(result.getString("dynamic_info"));

                        int dynamic_type = dynamic_info.getInt("type");

                        Message msg1 = handler.obtainMessage(handlerBasicDynamic);
                        msg1.obj = Objects.requireNonNull(dynamic_info);
                        handler.sendMessage(msg1);

                        Message msg2 = handler.obtainMessage(handlerLikeListNum);
                        msg2.obj = Objects.requireNonNull(result.getInt("likelist_sum"));
                        handler.sendMessage(msg2);


                    }
                    else {
                        Message msg = handler.obtainMessage(handlerStateWarning);
                        msg.obj = Objects.requireNonNull(response.body()).string();
                        handler.sendMessage(msg);
                    }
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
