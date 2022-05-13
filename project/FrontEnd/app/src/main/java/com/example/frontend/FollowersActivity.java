package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;


import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FollowersActivity extends AppCompatActivity {

    private final LinkedList<String> mNameList = new LinkedList<>();
    private final LinkedList<Bitmap> mBitmapList = new LinkedList<>();
    private final LinkedList<String> mEmailList = new LinkedList<>();
    private int total_num_data = 0;  // 个数上限
    private int avatar_count = 0;
    private Bitmap image;
    private RecyclerView mRecyclerView;
    private FollowerListAdapter mAdapter;
    private static final int handlerStateWarning = 0;
    private static final int handlerStateUpdateInfo = 1;
    private static final String LOG_TAG = FollowersActivity.class.getSimpleName();
    private Handler handler = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(FollowersActivity.this)
                        .setTitle("Tips:")
                        .setMessage(res)
                        .create();
                textTips.show();
            } else if (msg.what == handlerStateUpdateInfo) {
                mBitmapList.addLast(image);
                JSONObject data = (JSONObject) msg.obj;

                try {
                    mNameList.addLast(data.getString("nickname"));
                    mEmailList.addLast(data.getString("email"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private void getData(){
        // 引入数据
        String requestUrl = "http://43.138.84.226:8080/interact/show_followers_list";
        FollowersActivity.MyThreadGetData myThread = new FollowersActivity.MyThreadGetData(requestUrl);// TO DO
        myThread.start();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        getData();

        // Create recycler view.
        mRecyclerView = findViewById(R.id.recyclerview);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new FollowerListAdapter(this, mBitmapList, mNameList, mEmailList);
        // Connect the adapter with the recycler view.
        mRecyclerView.setAdapter(mAdapter);
        // Give the recycler view a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                //滑动到底部
//                if (newState == mRecyclerView.SCROLL_STATE_IDLE) {
//                    //recyclerview滑动到底部,更新数据
//                    //加载更多数据
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            if (mWordList.size() <= total_num_data) {
//                                // 因为最后一位是存储状态位，如果二者数目相同，说明还有一项未引入，这里是小于等于
//                                getMoreData();
//                                mAdapter.notifyDataSetChanged();
//
//                                //告诉他是否需要更多数据
//                                mAdapter.hasMore(mWordList.size() <= total_num_data);
//
//                            } else {
//                                //没有数据了
//                                mAdapter.hasMore(false);
//                                mAdapter.notifyDataSetChanged();
//                            }
//                        }
//                    }, 3000);
//                }
//            }
//        });
    }
    class MyThreadGetData extends Thread{
        private  String requestUrl;
        MyThreadGetData(String request){
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
                        Log.d(LOG_TAG, result.toString());

                        total_num_data = result.getInt("sum");
                        JSONArray jsonArray = result.getJSONArray("followers_list");
                        for (int i = 0; i < total_num_data; i++) {
                            String str = Objects.requireNonNull(jsonArray.get(i)).toString();

                            String requestUrl = "http://43.138.84.226:8080/user/show_avator";
                            FollowersActivity.MyThreadGetPhoto myThread = new FollowersActivity.MyThreadGetPhoto(requestUrl, str);// TO DO
                            myThread.start();// TO DO
                        }
//                        Message msg = handler.obtainMessage(handlerStateGetInfo);
//                        handler.sendMessage(msg);

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
        private String requestUrl;
        private JSONObject t;
        private int pos;
        MyThreadGetPhoto(String request, String str){
            try {
                t = new JSONObject(str);
                requestUrl = request + "/" + t.getString("avator");
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                        Message msg = handler.obtainMessage(handlerStateUpdateInfo);
                        JSONObject data = new JSONObject();
                        data.put("nickname", t.getString("nickname"));
                        data.put("email", t.getString("email"));
                        msg.obj = data;
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