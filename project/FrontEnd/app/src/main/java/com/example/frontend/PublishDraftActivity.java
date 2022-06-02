package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.LinkedList;
import java.util.List;

public class PublishDraftActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private DraftListAdapter mAdapter;
    private static final int handlerStateWarning = 0;
    private static final int handlerStateUpdate = 100;
    private Button draft_clear;
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
            else if (msg.what == handlerStateUpdate) {
                int res = (int) msg.obj;
                num.remove(res);
                if (num.size() == 0){
                    Message msg2 = handler.obtainMessage(handlerStateWarning);
                    msg2.obj = "草稿箱为空";
                    handler.sendMessage(msg2);
                }
                mAdapter.notifyDataSetChanged();
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

        }

        mRecyclerView = findViewById(R.id.draft_recycle_view);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new DraftListAdapter(this, num, handler);
        // Connect the adapter with the recycler view.
        mRecyclerView.setAdapter(mAdapter);
        // Give the recycler view a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        draft_clear = findViewById(R.id.draft_clear);
        draft_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i <= size; ++i){
                    SharedPreferences tmp = getSharedPreferences(sharedPrefFile+"_"+i, MODE_PRIVATE);
                    SharedPreferences.Editor editor = tmp.edit();
                    editor.clear();
                    editor.commit();
                }
                num.clear();
                mAdapter.notifyDataSetChanged();
                SharedPreferences listPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
                SharedPreferences.Editor editor = listPreferences.edit();
                editor.clear();
                editor.commit();
            }
        });
    }
}