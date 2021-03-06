package com.example.frontend;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.w4lle.library.NineGridlayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShowDynamicActivity extends AppCompatActivity {
    private static final String LOG_TAG = ShowDynamicActivity.class.getSimpleName();
    private TextView title, detail, location, audioFilename, timeText, likeListNum;
    private EditText commentText;
    private Button audioPlayer, addComment, likeButton, shareButton;
    private RelativeLayout audioLayout;
    private NineGridlayout nineGridlayout;
    private VideoView videoLayout;
    private int DynamicID, comment_num, like_num = 0;
    private boolean isLike = false;
    private JSONArray pic_list;
    private List<String> path = new ArrayList<>();
    private MediaPlayer audio_View = new MediaPlayer();

    private String myName, myAvatar, myEmail;
    private RecyclerView mRecyclerView;
    private CommentListAdapter mAdapter;
    private final LinkedList<String> mNameList = new LinkedList<>();
    private final LinkedList<String> mBitmapList = new LinkedList<>();
    private final LinkedList<String> mEmailList = new LinkedList<>();
    private final LinkedList<String> mCommentList = new LinkedList<>();
    private final LinkedList<Integer> mFlagList = new LinkedList<>();
    private final LinkedList<Integer> mCommentIDList = new LinkedList<>();


    private static final int handlerStateWarning = 0;
    private static final int handlerPicDynamic = 1;
    private static final int handlerAudioDynamic = 2;
    private static final int handlerVideoDynamic = 3;
    private static final int handlerBasicDynamic = 4;
    private static final int handlerLikeListNum = 5;
    private static final int handlerGetLike = 6;
    private static final int handlerAddLike = 7;
    private static final int handlerCancelLike = 8;
    private static final int handlerCommentSuccess = 9;
    private static final int handlerCommentInit = 10;

    private static final int handlerCancelCommentSuccess = 101;
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
                    Log.d("??????", l);
                    if (!l.equals("None")) {
                        location.setText("?????????" + l);
                        location.setVisibility(View.VISIBLE);
                    }
                    timeText.setText(res.getString("release_time"));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            else if (msg.what == handlerLikeListNum) {
                like_num = (int) msg.obj;
                likeListNum.setText("?????????" + like_num + "???");
            }
            else if (msg.what == handlerPicDynamic) {
                try {
                    pic_list = new JSONArray(Objects.requireNonNull(msg.obj).toString()); // String ??? JSONObject
                    int num = pic_list.length();

                    if (num <= 9 && num > 0) {
                        for (int i = 0; i < num; ++i){
                            path.add( "http://43.138.84.226:8080/demonstrate/show_picture/"+ pic_list.getString(i));
                            PicListAdapter adapter = new PicListAdapter(ShowDynamicActivity.this, path);
                            nineGridlayout.setAdapter(adapter);
                        }
                    }
                    nineGridlayout.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else if (msg.what == handlerAudioDynamic) {
                String res = (String) msg.obj;
                try {
                    audio_View.setDataSource("http://43.138.84.226:8080/demonstrate/show_audio/" + res);
                    audio_View.prepare();
                    audioLayout.setVisibility(View.VISIBLE);
                    audioFilename.setText(res);
                    audioPlayer.setText("??????");
                } catch (IOException e){
                    e.printStackTrace();
                }

            }
            else if (msg.what == handlerVideoDynamic) {
                String res = (String) msg.obj;
                Log.d("video filename", res);
                String videoUri = "http://43.138.84.226:8080/demonstrate/show_video/" + res;
                //String videoUri  = "https://poss-videocloud.cns.com.cn/oss/2021/05/08/chinanews/MEIZI_YUNSHI/onair/25AFA3CA2F394DB38420CC0A44483E82.mp4";
                Log.d("video url", videoUri);

                Uri uri = Uri.parse(videoUri);
                videoLayout.setVideoURI(uri);
                videoLayout.setVisibility(View.VISIBLE);
                videoLayout.start();
                videoLayout.requestFocus();

            }
            else if (msg.what == handlerGetLike) {
                String res = (String) msg.obj;
                if (res.equals("????????????")) {
                    isLike = true;
                    likeButton.setText("?????????");
                }
                else if (res.equals("????????????")) {
                    isLike = false;
                    likeButton.setText("??????");
                }
            }
            else if (msg.what == handlerAddLike) {
                String res = (String) msg.obj;
                if (res.equals("????????????")) {
                    isLike = true;
                    like_num += 1;
                    likeButton.setText("?????????");
                    likeListNum.setText("?????????" + like_num + "???");
                }
            }
            else if (msg.what == handlerCancelLike) {
                String res = (String) msg.obj;
                if (res.equals("???????????????")) {
                    isLike = false;
                    like_num -= 1;
                    likeButton.setText("??????");
                    likeListNum.setText("?????????" + like_num + "???");
                }
            }
            else if (msg.what == handlerCommentSuccess) {
//                JSONObject data = (JSONObject) msg.obj;
//
//                try{
//                    mNameList.addLast(data.getString("nickname"));
//                    mEmailList.addLast(data.getString("email"));
//                    mCommentList.addLast(data.getString("content"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(ShowDynamicActivity.this)
                        .setTitle("Tips:")
                        .setMessage(res)
                        .create();
                textTips.show();
                mAdapter.notifyDataSetChanged();
            }
            else if (msg.what == handlerCommentInit){
                mAdapter.notifyDataSetChanged();
            }
            else if (msg.what == handlerCancelCommentSuccess) {
                int res = (int) msg.obj;
                int index = mCommentIDList.indexOf(res);
                mNameList.remove(index);
                mEmailList.remove(index);
                mBitmapList.remove(index);
                mCommentList.remove(index);
                mFlagList.remove(index);
                mCommentIDList.remove(index);
                mAdapter.notifyDataSetChanged();
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
        timeText = (TextView) findViewById(R.id.dynamic_time);
        likeListNum = (TextView) findViewById(R.id.dynamic_like);
        audioPlayer = (Button) findViewById(R.id.audio_play);
        addComment = (Button) findViewById(R.id.add_comment);
        likeButton = (Button) findViewById(R.id.button_like);
        shareButton = (Button) findViewById(R.id.button_share);
        audioLayout = (RelativeLayout) findViewById(R.id.audio_layout);
        commentText = (EditText) findViewById(R.id.comment_text);
        nineGridlayout = findViewById(R.id.iv_ngrid_layout);
        videoLayout = (VideoView) findViewById(R.id.video_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.commentRecyclerView);

        mAdapter = new CommentListAdapter(this, mBitmapList, mNameList, mEmailList, mCommentList, mFlagList, mCommentIDList, handler);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        DynamicID = intent.getIntExtra("dynamic_id", 0);
        String requestUrl1 = "http://43.138.84.226:8080/demonstrate/show_dynamic/" + DynamicID;

        ShowDynamicActivity.MyThreadInitData myThread1 = new ShowDynamicActivity.MyThreadInitData(requestUrl1);
        myThread1.start();

        String requestUrl2 = "http://43.138.84.226:8080/user/show_user_data";
        ShowDynamicActivity.MyThreadGetUserData myThreadGetUserData = new ShowDynamicActivity.MyThreadGetUserData(requestUrl2);
        myThreadGetUserData.start();

        // ??????????????????
        String requestUrl3 = "http://43.138.84.226:8080/interact/is_like";
        ShowDynamicActivity.MyThreadGetLike myThreadGetLike = new ShowDynamicActivity.MyThreadGetLike(requestUrl3, DynamicID);
        myThreadGetLike.start();

        likeListNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowDynamicActivity.this, LikeListActivity.class);//???????????????????????????login?????????????????????activity???
                intent.putExtra("dynamic_id", DynamicID);
                startActivity(intent);
            }
        });
        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String request = "http://43.138.84.226:8080/interact/comment";
                ShowDynamicActivity.MyThreadCommentUpload myThread2 = new ShowDynamicActivity.MyThreadCommentUpload(request, DynamicID);
                myThread2.start();

//                Intent intent = new Intent(getActivity(), FollowersActivity.class);//???????????????????????????login?????????????????????activity???
//                startActivity(intent);
            }
        });
        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLike){
                    String request = "http://43.138.84.226:8080/interact/like";
                    ShowDynamicActivity.MyThreadAddLike myThread2 = new ShowDynamicActivity.MyThreadAddLike(request, DynamicID);
                    myThread2.start();
                }
                if (isLike){
                    String request = "http://43.138.84.226:8080/interact/cancel_like";
                    ShowDynamicActivity.MyThreadCancelLike myThread2 = new ShowDynamicActivity.MyThreadCancelLike(request, DynamicID);
                    myThread2.start();
                }
//                Intent intent = new Intent(getActivity(), FollowersActivity.class);//???????????????????????????login?????????????????????activity???
//                startActivity(intent);
            }
        });
        audioPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio_View.isPlaying()){
                    audio_View.pause();
                    audioPlayer.setText("??????");
                }
                else {
                    audio_View.start();
                    audioPlayer.setText("??????");
                }
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = title.getText().toString() + "\n" + detail.getText().toString();
                String mimeType = "text/plain";
                ShareCompat.IntentBuilder
                        .from(ShowDynamicActivity.this)
                        .setType(mimeType)
                        .setChooserTitle("??????????????????")
                        .setText(txt)
                        .startChooser();
            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        audio_View.release();
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
                        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string()); // String ??? JSONObject
                        JSONObject dynamic_info = new JSONObject(result.getString("dynamic_info"));

                        int dynamic_type = dynamic_info.getInt("type");

                        Message msg1 = handler.obtainMessage(handlerBasicDynamic);
                        msg1.obj = Objects.requireNonNull(dynamic_info);
                        handler.sendMessage(msg1);

                        Message msg2 = handler.obtainMessage(handlerLikeListNum);
                        msg2.obj = Objects.requireNonNull(result.getInt("likelist_sum"));
                        handler.sendMessage(msg2);

                        Message msg3 = handler.obtainMessage(dynamic_type);
                        if (dynamic_type == 1) {
                            msg3.obj = Objects.requireNonNull(dynamic_info.getJSONArray("pic_list"));
                            handler.sendMessage(msg3);
                        }
                        else if (dynamic_type == 2) {
                            msg3.obj = dynamic_info.getString("audio");;
                            handler.sendMessage(msg3);
                        }
                        else if (dynamic_type == 3) {
                            msg3.obj = dynamic_info.getString("video");;
                            handler.sendMessage(msg3);
                        }
                        else {
                            throw new IOException("Unexpected dynamic type");
                        }

                        String avatarUrl = "http://43.138.84.226:8080/user/show_avator/";
                        comment_num = result.getInt("comment_sum");
                        JSONArray array = result.getJSONArray("comment");
                        for (int i = 0; i < comment_num; i++) {
//                            String str = Objects.requireNonNull(array.get(i).toString());
//                            ShowDynamicActivity.MyThreadGetPhoto myThreadGetPhoto = new ShowDynamicActivity.MyThreadGetPhoto(requestUrl, str);
//                            myThreadGetPhoto.start();
                            JSONObject t = (JSONObject) array.getJSONObject(i);
                            mNameList.addLast(t.getString("nickname"));
                            mEmailList.addLast(t.getString("email"));
                            mBitmapList.addLast(avatarUrl + t.getString("avator"));
                            mCommentList.addLast(t.getString("content"));
                            mFlagList.addLast(t.getInt("check_user"));
                            mCommentIDList.addLast(t.getInt("comment_id"));
                        }

                        Message msg4 = handler.obtainMessage(handlerCommentInit);
                        msg4.obj = Objects.requireNonNull(dynamic_info);
                        handler.sendMessage(msg4);
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

    class MyThreadCommentUpload extends Thread {
        private String requestUrl;
        private int dynamicID;
        MyThreadCommentUpload(String request, int dynamic_id){
            requestUrl = request;
            dynamicID = dynamic_id;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);

                String comment = commentText.getText().toString();

                if (comment == null || comment.length() == 0){
                    Message msg = handler.obtainMessage(handlerStateWarning);
                    msg.obj = "??????????????????";
                    handler.sendMessage(msg);
                    throw new IOException("Comment is null");
                }

                RequestBody requestBody;

                requestBody = builder
                        .addFormDataPart("dynamic_id", dynamicID + "")
                        .addFormDataPart("comment_content", comment)
                        .build();


                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(requestBody)
                        .addHeader("cookie",cookie)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.isSuccessful()) {
                    String avatarUrl = "http://43.138.84.226:8080/user/show_avator/";
                    mNameList.addLast(myName);
                    mEmailList.addLast(myEmail);
                    mBitmapList.addLast(avatarUrl + myAvatar);
                    mCommentList.addLast(comment);
                    mFlagList.addLast(1);
                    mCommentIDList.addLast(999);

                    Message msg = handler.obtainMessage(handlerCommentSuccess);
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

    class MyThreadGetLike extends Thread {
        private String requestUrl;
        private int dynamicID;
        MyThreadGetLike (String request, int dynamic_id) {
            requestUrl = request;
            dynamicID = dynamic_id;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
//                file = new File(filepath);
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);


                RequestBody requestBody;

                requestBody = builder
                        .addFormDataPart("dynamic_id", dynamicID + "")
                        .build();

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(requestBody)
                        .addHeader("cookie",cookie)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.isSuccessful()) {
                    Message msg = handler.obtainMessage(handlerGetLike);
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

    class MyThreadAddLike extends Thread {
        private String requestUrl;
        private int dynamicID;
        MyThreadAddLike (String request, int dynamic_id) {
            requestUrl = request;
            dynamicID = dynamic_id;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
//                file = new File(filepath);
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);


                RequestBody requestBody;

                requestBody = builder
                        .addFormDataPart("dynamic_id", dynamicID + "")
                        .build();

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(requestBody)
                        .addHeader("cookie",cookie)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.isSuccessful()) {
                    Message msg = handler.obtainMessage(handlerAddLike);
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

    class MyThreadCancelLike extends Thread {
        private String requestUrl;
        private int dynamicID;
        MyThreadCancelLike (String request, int dynamic_id) {
            requestUrl = request;
            dynamicID = dynamic_id;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
//                file = new File(filepath);
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM);


                RequestBody requestBody;

                requestBody = builder
                        .addFormDataPart("dynamic_id", dynamicID + "")
                        .build();

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(requestBody)
                        .addHeader("cookie",cookie)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                if (response.isSuccessful()) {
                    Message msg = handler.obtainMessage(handlerCancelLike);
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
    class MyThreadGetUserData extends Thread{
        private  String requestUrl;
        MyThreadGetUserData(String request){
            requestUrl = request;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                //3.??????MultipartBody
                SharedPreferences sharedPreferences = ShowDynamicActivity.this.getSharedPreferences("login",MODE_PRIVATE);
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
                if (response.isSuccessful()) {

                    if (response.code() == 200){
                        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string()); // String ??? JSONObject

                        myName = result.getString("nickname");
                        myEmail = result.getString("email");
                        myAvatar = result.getString("avator");
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
