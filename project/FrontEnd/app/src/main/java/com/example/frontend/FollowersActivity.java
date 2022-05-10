package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;


import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FollowersActivity extends AppCompatActivity {

    private final LinkedList<String> mAvatarlist = new LinkedList<>();
    private final LinkedList<String> mNameList = new LinkedList<>();
    private int total_num_data = 0;  // 个数上限
    private RecyclerView mRecyclerView;
    private FollowerListAdapter mAdapter;
    private static final int handlerStateWarning = 0;
    private static final int handlerStateGetInfo = 1;
    private static final String LOG_TAG = FollowersActivity.class.getSimpleName();
    private Handler handler = new Handler(){
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
            } else if (msg.what == handlerStateGetInfo) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private void getData(){
        // 引入数据
        String requestUrl = "http://43.138.84.226:8080/user/show_followers_list";
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
        mAdapter = new FollowerListAdapter(this, mAvatarlist, mNameList);
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
                            JSONObject t = new JSONObject(Objects.requireNonNull(jsonArray.get(i)).toString());
                            mAvatarlist.addLast(t.getString("avator"));
                            mNameList.addLast(t.getString("nickname"));
                        }
                        Message msg = handler.obtainMessage(handlerStateGetInfo);
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