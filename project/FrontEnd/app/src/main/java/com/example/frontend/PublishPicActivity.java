package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.w4lle.library.NineGridAdapter;
import com.w4lle.library.NineGridlayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishPicActivity extends AppCompatActivity {
    private Button button_loadPic, button_loadPos, button_launch;
    private NineGridlayout nineGridlayout;
    static final int PHOTO_RETURN_CODE = 0;
    private List<String> path;
    private static final int handlerStateWarning = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(PublishPicActivity.this)
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
        setContentView(R.layout.activity_publish_pic);
        button_loadPic = (Button) findViewById(R.id.add_pic);
        button_loadPos = (Button) findViewById(R.id.add_position_pic);
        button_launch = (Button) findViewById(R.id.publish_button_pic);
        nineGridlayout = findViewById(R.id.iv_ngrid_layout);

        button_loadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PublishPicActivity.this, MultiImageSelectorActivity.class);
// whether show camera
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
// max select image amount
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);
// select mode (MultiImageSelectorActivity.MODE_SINGLE OR MultiImageSelectorActivity.MODE_MULTI)
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
// default select images (support array list)
                // intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, defaultDataArray);
                startActivityForResult(intent, PHOTO_RETURN_CODE);
            }
        });

        button_launch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestUrl = "http://43.138.84.226:8080/publish/picture";
                PublishPicActivity.MyThreadPhoto myThread = new PublishPicActivity.MyThreadPhoto(requestUrl);// TO DO
                myThread.start();// TO DO
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHOTO_RETURN_CODE){
            if(resultCode == RESULT_OK){
                // Get the result list of select image paths
                path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

                PicListAdapter adapter = new PicListAdapter(PublishPicActivity.this, path);
                nineGridlayout.setAdapter(adapter);

                // do your logic ....
            }
        }
    }

    class MyThreadPhoto extends Thread{
        private String requestUrl;
        MyThreadPhoto(String request){
            requestUrl = request;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
//                file = new File(filepath);
                MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);
                for(int i = 0; i < path.size(); ++i){
                    File f = new File(path.get(i));
                    RequestBody fileBody = RequestBody.create(MEDIA_TYPE_PNG, f);
                    builder.addFormDataPart("pic_"+(i+1), f.getName(), fileBody);
                }

                // TO DO：修改参数并添加判断
                RequestBody requestBody = builder
                        .addFormDataPart("title", "test")
                        .addFormDataPart("content", "test")
                        .addFormDataPart("location", "test")
                        .addFormDataPart("pic_num", "" + path.size())
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
