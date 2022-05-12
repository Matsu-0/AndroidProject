package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OthersActivity extends AppCompatActivity {
    private static final String LOG_TAG = OthersActivity.class.getSimpleName();
    private String othersEmail;

    private Button follow_button, black_button;
    private Bitmap image;
    private ImageView pic;
    private TextView name, introduction;

    private static final int handlerStateWarning = 0;
    private static final int handlerStateUpdatePhoto = 1;
    private static final int handlerStateUpdateName = 2;
    private static final int handlerStateUpdateIntroduction = 3;
    private static final int handlerStateGetPhoto = 4;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(OthersActivity.this)
                        .setTitle("Tips:")
                        .setMessage(res)
                        .create();
                textTips.show();
            }
            else if (msg.what == handlerStateUpdatePhoto) {
                pic.setImageBitmap(image);
            }
            else if (msg.what == handlerStateUpdateName) {
                String res = (String) msg.obj;
                name.setText(res);
            }
            else if (msg.what == handlerStateUpdateIntroduction) {
                String res = (String) msg.obj;
                introduction.setText(res);
            }
            else if (msg.what == handlerStateGetPhoto) {
                String res = (String) msg.obj;
                String requestUrl = "http://43.138.84.226:8080/user/show_avator";
                OthersActivity.MyThreadGetPhoto myThread = new OthersActivity.MyThreadGetPhoto(requestUrl, res);// TO DO
                myThread.start();// TO DO
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others);
        //needs Intent.putExtra("email", email)
        othersEmail = getIntent().getStringExtra("email");
        follow_button = (Button)findViewById(R.id.follow_button);
        black_button = (Button)findViewById(R.id.black_button);
        pic = (ImageView) findViewById(R.id.person_image);
        name = (TextView) findViewById(R.id.person_name);
        introduction = (TextView) findViewById(R.id.person_introduction);
        String requestUrl = "http://43.138.84.226:8080/user/show_user_data";
        OthersActivity.MyThreadInitData myThread = new OthersActivity.MyThreadInitData(requestUrl);// TO DO
        myThread.start();// TO DO

        follow_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理关注/取关
                // TO DO
            }
        });


        black_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理屏蔽/取消
                // TO DO
            }
        });
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
                //3.构建MultipartBody
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
                        Message msg1 = handler.obtainMessage(handlerStateUpdateName);
                        msg1.obj = Objects.requireNonNull(result.getString("nickname"));
                        handler.sendMessage(msg1);

                        Message msg2 = handler.obtainMessage(handlerStateUpdateIntroduction);
                        msg2.obj = Objects.requireNonNull(result.getString("introduction"));
                        handler.sendMessage(msg2);

                        Message msg3 = handler.obtainMessage(handlerStateGetPhoto);
                        msg3.obj = Objects.requireNonNull(result.getString("avator"));
                        handler.sendMessage(msg3);
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

    class MyThreadGetPhoto extends Thread{
        private  String requestUrl;
        MyThreadGetPhoto(String request, String avator){
            requestUrl = request + "/" + avator;
        }
        @Override
        public void run() {
            try {
                Log.d(LOG_TAG, "1");
                OkHttpClient client = new OkHttpClient();
                //3.构建MultipartBody
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");
                Log.d(LOG_TAG, cookie);

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .get()
                        .addHeader("cookie",cookie)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                Log.d(LOG_TAG, response.toString());
                if (response.isSuccessful()){
                    if (response.code() == 200){
                        InputStream inputStream = response.body().byteStream();
                        image = BitmapFactory.decodeStream(inputStream);
                        Message msg = handler.obtainMessage(handlerStateUpdatePhoto);
                        // msg.obj = Objects.requireNonNull(inputStream);
                        handler.sendMessage(msg);

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

    class MyThreadUpdateInfo extends Thread{
        private String requestUrl, name, introduction;
        MyThreadUpdateInfo(String request, String nameStr, String introductionStr){
            requestUrl = request;
            name = nameStr;
            introduction = introductionStr;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();

                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                RequestBody formBody = new FormBody.Builder()
                        .add("nickname", name)
                        .add("introduction", introduction)
                        .build();

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(formBody)
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