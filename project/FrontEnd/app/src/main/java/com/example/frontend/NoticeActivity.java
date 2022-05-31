package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NoticeActivity extends AppCompatActivity {

    private final LinkedList<String> mNameList = new LinkedList<>();
    private final LinkedList<String> mAvatarList = new LinkedList<>();
    private final LinkedList<String> mNoticeDetailList = new LinkedList<>();
    private final LinkedList<String> mCommentDetailList = new LinkedList<>();
    private final LinkedList<String> mDynamicTitleList = new LinkedList<>();
    private final LinkedList<String> mDynamicContentList = new LinkedList<>();
    private final LinkedList<String> mDynamicTypeList = new LinkedList<>();
    private final LinkedList<Integer> mDynamicIDList = new LinkedList<>();

    private int total_num_data = 0;  // 个数上限
    private RecyclerView mRecyclerView;
    private NoticeAdapter mAdapter;
    private static final int handlerStateWarning = 0;
    private static final int handlerStateUpdateInfo = 1;
    private static final String LOG_TAG = NoticeActivity.class.getSimpleName();
    private Handler handler = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(NoticeActivity.this)
                        .setTitle("Tips:")
                        .setMessage(res)
                        .create();
                textTips.show();
            } else if (msg.what == handlerStateUpdateInfo) {
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private void getData(){
        // 引入数据
        String requestUrl = "http://43.138.84.226:8080/interact/user/notice_list";
        NoticeActivity.MyThreadGetData myThread = new NoticeActivity.MyThreadGetData(requestUrl);// TO DO
        myThread.start();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacklist);

        // Create recycler view.
        mRecyclerView = findViewById(R.id.recyclerview);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new NoticeAdapter(this, mNameList, mAvatarList, mNoticeDetailList,  mCommentDetailList,
                                    mDynamicTitleList, mDynamicContentList, mDynamicTypeList, mDynamicIDList);

        // Connect the adapter with the recycler view.
        mRecyclerView.setAdapter(mAdapter);
        // Give the recycler view a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        mNameList.clear();
        mAvatarList.clear();
        mNoticeDetailList.clear();
        mCommentDetailList.clear();
        mDynamicTitleList.clear();
        mDynamicContentList.clear();
        mDynamicTypeList.clear();
        mDynamicIDList.clear();
        getData();

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

                        total_num_data = result.getInt("notice_num");
                        JSONArray jsonArray = result.getJSONArray("notice_list");
                        String requestUrl = "http://43.138.84.226:8080/user/show_avator/";

                        for (int i = 0; i < total_num_data; i++) {
                            JSONObject t = (JSONObject) jsonArray.get(i);
                            mNameList.addLast(t.getString("nickname"));
                            mAvatarList.addLast(requestUrl + t.getString("avatar"));

                            int type =t.getInt("type");
                            if (type == 1) {
                                mNoticeDetailList.addLast("赞了你");
                                mCommentDetailList.addLast(null);
                            }
                            else if (type == 2) {
                                mNoticeDetailList.addLast("评论了你");
                                mCommentDetailList.addLast(t.getString("comment"));
                            }
                            else if (type == 3){
                                mNoticeDetailList.addLast("有新动态");
                                mCommentDetailList.addLast(null);
                            }
                            else {
                                mNoticeDetailList.addLast(null);
                                mCommentDetailList.addLast(null);
                            }

                            mDynamicTitleList.addLast(t.getString("title"));
                            mDynamicContentList.addLast(t.getString("content"));

                            int dynamic_type = t.getInt("dynamic_type");
                            if (dynamic_type == 1) {
                                mDynamicTypeList.addLast("图文");
                            }
                            else if (dynamic_type == 2) {
                                mDynamicTypeList.addLast("音频");
                            }
                            else if (dynamic_type == 3) {
                                mDynamicTypeList.addLast("视频");
                            }
                            else {
                                mDynamicTypeList.addLast("未知");
                            }
                            mDynamicIDList.addLast(t.getInt("dynamic_id"));

                        }
                        Message msg = handler.obtainMessage(handlerStateUpdateInfo);
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