package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.frontend.ui.person.PersonFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
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
    private RecyclerView mRecyclerView;
    private DynamicListAdapter mAdapter;
    private JSONArray dynamic_list;

    private String othersEmail;

    private Button follow_button, black_button;
    private Bitmap image;
    private ImageView pic;
    private TextView name, introduction;

    private Boolean isFollow, isBlackList, isFinish;
    private int page = 1;
    private static final int handlerStateWarning = 0;
    private static final int handlerStateUpdatePhoto = 1;
    private static final int handlerStateUpdateName = 2;
    private static final int handlerStateUpdateIntroduction = 3;
    private static final int handlerStateGetPhoto = 4;
    private static final int handlerStateUpdateFollowButton = 5;
    private static final int handlerStateUpdateBlackButton = 6;
    private static final int handlerStateGetDynamics = 7;
    private static final int handlerStateDynamicsFinish = 8;

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
            else if (msg.what == handlerStateUpdateFollowButton) {
                int res = (int) msg.obj;
                if (res == 0) {
                    follow_button.setText("关注");
                    isFollow = false;
                }
                else{
                    follow_button.setText("取消关注");
                    isFollow = true;
                }
            }
            else if (msg.what == handlerStateUpdateBlackButton) {
                int res = (int) msg.obj;
                if (res == 0) {
                    black_button.setText("屏蔽");
                    isBlackList = false;
                }
                else{
                    black_button.setText("取消屏蔽");
                    isBlackList = true;
                }
            }
            else if (msg.what == handlerStateGetPhoto) {
                String res = (String) msg.obj;

                String filename = "http://43.138.84.226:8080/user/show_avator/" + res;
                // activity中将 getContext() 换成 context
                Picasso.with(OthersActivity.this).load(filename).into(pic);

//                String requestUrl = "http://43.138.84.226:8080/user/show_avator";
//                OthersActivity.MyThreadGetPhoto myThread = new OthersActivity.MyThreadGetPhoto(requestUrl, res);// TO DO
//                myThread.start();// TO DO
            }
            else if (msg.what == handlerStateGetDynamics) {
                try {
                    JSONObject result = new JSONObject(Objects.requireNonNull(msg.obj).toString()); // String 转 JSONObject
                    JSONArray temp = result.getJSONArray("dynamics_list");
                    for (int i = 0; i < result.getInt("dynamics_num"); ++i ){ //
                        dynamic_list.put(temp.getJSONObject(i));
                    }


                    mAdapter.notifyDataSetChanged();
                    Log.d("111",dynamic_list.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if (msg.what == handlerStateDynamicsFinish) {
                isFinish = true;
                mAdapter.hasmore = false;
                mAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others);
        //needs Intent.putExtra("email", email)
        othersEmail = getIntent().getStringExtra("KEY_EMAIL");
        follow_button = (Button)findViewById(R.id.follow_button);
        black_button = (Button)findViewById(R.id.black_button);
        pic = (ImageView) findViewById(R.id.person_image);
        name = (TextView) findViewById(R.id.person_name);
        introduction = (TextView) findViewById(R.id.person_introduction);
        dynamic_list = new JSONArray();
        mRecyclerView = findViewById(R.id.dynamic_recycle_view_others);

        isFollow = false;
        isBlackList = false;
        isFinish = false;
        follow_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理关注/取关
                Log.d("AAA", isFollow.toString());
                OthersActivity.MyThreadFollow myThread = new OthersActivity.MyThreadFollow(isFollow, othersEmail);// TO DO
                myThread.start();// TO DO
                // TO DO
            }
        });


        black_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 处理屏蔽/取消
                // TO DO
                Log.d("AAA", isFollow.toString());
                OthersActivity.MyThreadBlack myThread = new OthersActivity.MyThreadBlack(isBlackList, othersEmail);// TO DO
                myThread.start();// TO DO
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter = new DynamicListAdapter(OthersActivity.this, dynamic_list, 0);
        // Connect the adapter with the recycler view.
        mRecyclerView.setAdapter(mAdapter);
        // Give the recycler view a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(OthersActivity.this){
//            @Override
//            public boolean canScrollVertically() {
//                return false;
//            }
//        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //滑动到底部
                if (newState == mRecyclerView.SCROLL_STATE_IDLE) {
                    //recyclerview滑动到底部,更新数据
                    //加载更多数据
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("111", page + "");
                            getMoreData();
                        }
                    }, 3000);
                }
            }
        });
        String requestUrl = "http://43.138.84.226:8080/demonstrate/show_other_user_data/" + othersEmail;
        OthersActivity.MyThreadInitData myThread = new OthersActivity.MyThreadInitData(requestUrl);// TO DO
        myThread.start();// TO DO

        getMoreData();
    }

    private void getMoreData(){
        if (isFinish)
            return;
        String requestUrl2 = "http://43.138.84.226:8080/demonstrate/other_dynamics";
        OthersActivity.MyThreadGetPersonDynamic myThread2 = new OthersActivity.MyThreadGetPersonDynamic(requestUrl2, page, othersEmail);// TO DO
        myThread2.start();// TO DO
        page++;
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

                        Message msg4 = handler.obtainMessage(handlerStateUpdateFollowButton);
                        msg4.obj = Objects.requireNonNull(result.getInt("is_follow"));
                        handler.sendMessage(msg4);

                        Message msg5 = handler.obtainMessage(handlerStateUpdateBlackButton);
                        msg5.obj = Objects.requireNonNull(result.getInt("is_ignore"));
                        handler.sendMessage(msg5);
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

//    class MyThreadGetPhoto extends Thread{
//        private  String requestUrl;
//        MyThreadGetPhoto(String request, String avator){
//            requestUrl = request + "/" + avator;
//        }
//        @Override
//        public void run() {
//            try {
//                Log.d(LOG_TAG, "1");
//                OkHttpClient client = new OkHttpClient();
//                //3.构建MultipartBody
//                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
//                String cookie = sharedPreferences.getString("session","");
//                Log.d(LOG_TAG, cookie);
//
//                Request request = new Request.Builder()
//                        .url(requestUrl)
//                        .get()
//                        .addHeader("cookie",cookie)
//                        .build();
//
//                Call call = client.newCall(request);
//                Response response = call.execute();
//                Log.d(LOG_TAG, response.toString());
//                if (response.isSuccessful()){
//                    if (response.code() == 200){
//                        InputStream inputStream = response.body().byteStream();
//                        image = BitmapFactory.decodeStream(inputStream);
//                        Message msg = handler.obtainMessage(handlerStateUpdatePhoto);
//                        // msg.obj = Objects.requireNonNull(inputStream);
//                        handler.sendMessage(msg);
//
//                    }
//                    else {
//                        Message msg = handler.obtainMessage(handlerStateWarning);
//                        msg.obj = Objects.requireNonNull(response.body()).string();
//                        handler.sendMessage(msg);
//                    }
//
//
//                } else {
//                    throw new IOException("Unexpected code " + response);
//                }
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        }
//    }

    class MyThreadFollow extends Thread{
        private  String requestUrl, email;
        private int FollowStatus;
        MyThreadFollow(Boolean isFollow, String email){
            this.email = email;
            if (isFollow){
                requestUrl = "http://43.138.84.226:8080/interact/not_follow_someone";
                FollowStatus = 1;
            }
            else{
                requestUrl = "http://43.138.84.226:8080/interact/follow_someone";
                FollowStatus = 0;
            }
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                //3.构建MultipartBody
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");
                Log.d(LOG_TAG, cookie);
                RequestBody formBody = new FormBody.Builder()
                        .add("email", email)
                        .build();
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(formBody)
                        .addHeader("cookie",cookie)
                        .build();
                Log.d(LOG_TAG, cookie);
                Call call = client.newCall(request);
                Response response = call.execute();
                Log.d(LOG_TAG, response.toString());
                if (response.isSuccessful()) {

                    if (response.code() == 200){
                        Message msg1 = handler.obtainMessage(handlerStateUpdateFollowButton);
                        msg1.obj = Objects.requireNonNull(1-FollowStatus);
                        handler.sendMessage(msg1);


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

    class MyThreadBlack extends Thread{
        private  String requestUrl, email;
        private int BlackStatus;
        MyThreadBlack(Boolean isFollow, String email){
            this.email = email;
            if (isFollow){
                requestUrl = "http://43.138.84.226:8080/interact/not_ignore_someone";
                BlackStatus = 1;
            }
            else{
                requestUrl = "http://43.138.84.226:8080/interact/ignore_someone";
                BlackStatus = 0;
            }
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                //3.构建MultipartBody
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");
                Log.d(LOG_TAG, cookie);
                RequestBody formBody = new FormBody.Builder()
                        .add("email", email)
                        .build();
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(formBody)
                        .addHeader("cookie",cookie)
                        .build();
                Log.d(LOG_TAG, cookie);
                Call call = client.newCall(request);
                Response response = call.execute();
                Log.d(LOG_TAG, response.toString());
                if (response.isSuccessful()) {

                    if (response.code() == 200){
                        Message msg1 = handler.obtainMessage(handlerStateUpdateBlackButton);
                        msg1.obj = Objects.requireNonNull(1-BlackStatus);
                        handler.sendMessage(msg1);


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

    class MyThreadGetPersonDynamic extends Thread{
        private  String requestUrl, requestEmail;
        private int curPage;
        MyThreadGetPersonDynamic(String request, int page, String email){
            curPage = page;
            requestEmail = email;
            requestUrl = request;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                //3.构建MultipartBody
                SharedPreferences sharedPreferences = OthersActivity.this.getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");
                Log.d(LOG_TAG, cookie);
                RequestBody formBody = new FormBody.Builder()
                        .add("email", requestEmail)
                        .add("page", ""+ curPage)
                        .build();
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(formBody)
                        .addHeader("cookie",cookie)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                Log.d(LOG_TAG, response.toString());
                if (response.isSuccessful()){
                    if (response.code() == 200){
                        Message msg = handler.obtainMessage(handlerStateGetDynamics);
                        msg.obj = Objects.requireNonNull(response.body()).string();

                        handler.sendMessage(msg);
                    }
                    else if (response.code() == 202){
                        Message msg = handler.obtainMessage(handlerStateDynamicsFinish);
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
}