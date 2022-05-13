package com.example.frontend.ui.person;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.frontend.InfoeditActivity;
import com.example.frontend.R;
import com.example.frontend.SignupActivity;
import com.example.frontend.databinding.FragmentBrowseBinding;
import com.example.frontend.databinding.FragmentPersonBinding;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PersonFragment extends Fragment {

    private FragmentPersonBinding binding;
    private static final String LOG_TAG = PersonFragment.class.getSimpleName();
    private Button edit_button;
    private Bitmap image;
    private ImageView pic;
    private TextView name, introduction, follower, blacker;
    private static final int handlerStateWarning = 0;
    private static final int handlerStateUpdatePhoto = 1;
    private static final int handlerStateUpdateName = 2;
    private static final int handlerStateUpdateIntroduction = 3;
    private static final int handlerStateGetPhoto = 4;
    private static final int handlerStateGetFollowerNum = 5;
    private static final int handlerStateGetBlackerNum = 6;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(getActivity())
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
                PersonFragment.MyThreadGetPhoto myThread = new PersonFragment.MyThreadGetPhoto(requestUrl, res);// TO DO
                myThread.start();// TO DO
            }
            else if (msg.what == handlerStateGetFollowerNum) {
                String res = "关注\n" + (String) msg.obj + "人";
                follower.setText(res);
            }
            else if (msg.what == handlerStateGetBlackerNum) {
                String res = "黑名单\n" + (String) msg.obj + "人";
                blacker.setText(res);
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PersonViewModel personViewModel =
                new ViewModelProvider(this).get(PersonViewModel.class);
        binding = FragmentPersonBinding.inflate(inflater, container, false);
        View root = inflater.inflate(R.layout.fragment_person,container,false);

        super.onCreate(savedInstanceState);
        // setContentView(R.layout.fragment_person);

        String requestUrl1 = "http://43.138.84.226:8080/user/show_user_data";
        PersonFragment.MyThreadInitData myThread1 = new PersonFragment.MyThreadInitData(requestUrl1);// TO DO
        myThread1.start();// TO DO

        String requestUrl2 = "http://43.138.84.226:8080/interact/show_followers_num";
        PersonFragment.MyThreadGetFollowerNum myThread2 = new PersonFragment.MyThreadGetFollowerNum(requestUrl2);// TO DO
        myThread2.start();// TO DO

        String requestUrl3 = "http://43.138.84.226:8080/interact/show_ignore_num";
        PersonFragment.MyThreadGetBlackerNum myThread3 = new PersonFragment.MyThreadGetBlackerNum(requestUrl3);// TO DO
        myThread3.start();// TO DO

        return root;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        edit_button = (Button)getActivity().findViewById(R.id.edit);
        pic = (ImageView) getActivity().findViewById(R.id.person_image);
        name = (TextView) getActivity().findViewById(R.id.person_name);
        introduction = (TextView) getActivity().findViewById(R.id.person_introduction);
        follower = (TextView) getActivity().findViewById(R.id.follow);
        blacker =(TextView) getActivity().findViewById(R.id.blacklist);

        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),InfoeditActivity.class);//想调到哪个界面就把login改成界面对应的activity名
                startActivity(intent);
            }
        });

        follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),InfoeditActivity.class);//想调到哪个界面就把login改成界面对应的activity名
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login",MODE_PRIVATE);
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
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("set",MODE_PRIVATE);
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

    class MyThreadGetFollowerNum extends Thread{
        private  String requestUrl;
        MyThreadGetFollowerNum(String request){
            requestUrl = request;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                //3.构建MultipartBody
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login",MODE_PRIVATE);
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
                        Message msg = handler.obtainMessage(handlerStateGetFollowerNum);
                        msg.obj = Objects.requireNonNull(response.body()).string();
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

    class MyThreadGetBlackerNum extends Thread{
        private  String requestUrl;
        MyThreadGetBlackerNum(String request){
            requestUrl = request;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                //3.构建MultipartBody
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login",MODE_PRIVATE);
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
                        Message msg = handler.obtainMessage(handlerStateGetBlackerNum);
                        msg.obj = Objects.requireNonNull(response.body()).string();
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