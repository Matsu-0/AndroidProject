package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class PublishDraftActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private DraftListAdapter mAdapter;
    private static final int handlerStateWarning = 0;
    private LinkedList<Integer> num;
    private String sharedPrefFile ="com.example.frontend.draft";
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(PublishDraftActivity.this)
                        .setTitle("Tips:")
                        .setMessage(res)
                        .create();
                textTips.show();
            }
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_publish_draft);
        SharedPreferences listPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        int size = listPreferences.getInt("size", -1);
        num  = new LinkedList<>();

        for (int i = 0; i <= size; ++i){
            SharedPreferences tmp = getSharedPreferences(sharedPrefFile+"_"+i, MODE_PRIVATE);
            if (tmp.getInt("type", 0) > 0){
                num.addLast(i);
            }
        }
        if (num.size() == 0){
            Message msg = handler.obtainMessage(handlerStateWarning);
            msg.obj = "草稿箱为空";
            handler.sendMessage(msg);
            finish();
        }

        mRecyclerView = findViewById(R.id.draft_recycle_view);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new DraftListAdapter(this, num);
        // Connect the adapter with the recycler view.
        mRecyclerView.setAdapter(mAdapter);
        // Give the recycler view a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}