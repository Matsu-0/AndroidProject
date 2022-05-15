package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import com.w4lle.library.NineGridlayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class PublishVideoActivity extends AppCompatActivity {
    private Button button_loadVideo, button_loadPos, button_launch, button_takeVideo, button_clearVideo;
    private VideoView videoLayout;
    static final int VIDEO_RETURN_CODE = 0;
    static final int TAKE_VIDEO_RETURN_CODE = 1;
    private String currentVideoPath;
    private static final int handlerStateWarning = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(PublishVideoActivity.this)
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
        setContentView(R.layout.activity_publish_video);

        button_loadVideo = (Button) findViewById(R.id.add_video);
        button_loadPos = (Button) findViewById(R.id.add_position_video);
        button_launch = (Button) findViewById(R.id.publish_button_video);
        button_takeVideo = (Button) findViewById(R.id.take_video);
        button_clearVideo = (Button) findViewById(R.id.clear_all_video);
        videoLayout = findViewById(R.id.video_layout);

        button_takeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(takeVideoIntent, TAKE_VIDEO_RETURN_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == TAKE_VIDEO_RETURN_CODE){
            if(resultCode == RESULT_OK){
                Uri videoUri = data.getData();
                videoLayout.setVideoURI(videoUri);
            }
        }
    }
}