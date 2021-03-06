package com.example.frontend.ui.person;

import static android.content.Context.MODE_PRIVATE;
import android.content.Context;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import androidx.core.app.Person;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.DraftListAdapter;
import com.example.frontend.DynamicListAdapter;
import com.example.frontend.FollowersActivity;
import com.example.frontend.InfoeditActivity;
import com.example.frontend.FollowersActivity;
import com.example.frontend.BlackListActivity;
import com.example.frontend.LoginActivity;
import com.example.frontend.NoticeActivity;
import com.example.frontend.OthersActivity;
import com.example.frontend.R;
import com.example.frontend.SignupActivity;
import com.example.frontend.databinding.FragmentPersonBinding;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PersonFragment extends Fragment {

    private FragmentPersonBinding binding;
    private RecyclerView mRecyclerView;
    private DynamicListAdapter mAdapter;

    private static final String LOG_TAG = PersonFragment.class.getSimpleName();
    private Button edit_button;
    private Bitmap image;
    private ImageView pic;
    private TextView name, introduction, notice, blacker, follower;
    private Boolean isFinish;
    private int page = 1;
    private static final int handlerStateWarning = 0;
    private static final int handlerStateUpdatePhoto = 1;
    private static final int handlerStateUpdateName = 2;
    private static final int handlerStateUpdateIntroduction = 3;
    private static final int handlerStateGetPhoto = 4;
    private static final int handlerStateGetFollowerNum = 5;
    private static final int handlerStateGetBlackerNum = 6;
    private static final int handlerStateGetDynamics = 7;
    private static final int handlerStateDynamicsFinish = 8;

    private static final int handlerStateDeleteSucceed = 100;
    private JSONArray dynamic_list;
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

//                String filename = "http://43.138.84.226:8080/user/show_avator/" + res;
//                // activity?????? getContext() ?????? context
//                Picasso.with(getContext()).load(filename).into(pic);

            }
            else if (msg.what == handlerStateGetFollowerNum) {
                String res = "??????\n" + (String) msg.obj + "???";
                follower.setText(res);
            }
            else if (msg.what == handlerStateGetBlackerNum) {
                String res = "?????????\n" + (String) msg.obj + "???";
                blacker.setText(res);
            }
            else if (msg.what == handlerStateGetDynamics) {
                try {
                    JSONObject result = new JSONObject(Objects.requireNonNull(msg.obj).toString()); // String ??? JSONObject
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
            else if (msg.what == handlerStateDeleteSucceed) {
                try {
                    int res = (int) msg.obj;
                    for (int i = 0; i < dynamic_list.length(); ++i) {
                        if (dynamic_list.getJSONObject(i).getInt("dynamic_id") == res) {
                            dynamic_list.remove(i);
                            mAdapter.notifyDataSetChanged();
                            return;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

        return root;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        edit_button = (Button)getActivity().findViewById(R.id.edit);
        pic = (ImageView) getActivity().findViewById(R.id.person_image);
        name = (TextView) getActivity().findViewById(R.id.person_name);
        introduction = (TextView) getActivity().findViewById(R.id.person_introduction);
        notice = (TextView) getActivity().findViewById(R.id.notice);
        follower = (TextView) getActivity().findViewById(R.id.follow);
        blacker =(TextView) getActivity().findViewById(R.id.blacklist);

        dynamic_list = new JSONArray();
        isFinish = false;
        mRecyclerView = getActivity().findViewById(R.id.dynamic_recycle_view_person);
        // Create an adapter and supply the data to be displayed.

    }

    @Override
    public void onStart() {
        super.onStart();
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),InfoeditActivity.class);//???????????????????????????login?????????????????????activity???
                startActivity(intent);
            }
        });

        notice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NoticeActivity.class);//???????????????????????????login?????????????????????activity???
                startActivity(intent);
            }
        });

        follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FollowersActivity.class);//???????????????????????????login?????????????????????activity???
                startActivity(intent);
            }
        });

        blacker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),BlackListActivity.class);//???????????????????????????login?????????????????????activity???
                startActivity(intent);
            }
        });

        String requestUrl1 = "http://43.138.84.226:8080/user/show_user_data";
        PersonFragment.MyThreadInitData myThread1 = new PersonFragment.MyThreadInitData(requestUrl1);// TO DO
        myThread1.start();// TO DO

        String requestUrl2 = "http://43.138.84.226:8080/interact/show_followers_num";
        PersonFragment.MyThreadGetFollowerNum myThread2 = new PersonFragment.MyThreadGetFollowerNum(requestUrl2);// TO DO
        myThread2.start();// TO DO

        String requestUrl3 = "http://43.138.84.226:8080/interact/show_ignore_num";
        PersonFragment.MyThreadGetBlackerNum myThread3 = new PersonFragment.MyThreadGetBlackerNum(requestUrl3);// TO DO
        myThread3.start();// TO DO


        mAdapter = new DynamicListAdapter(getActivity(), dynamic_list, 1, handler);
        // Connect the adapter with the recycler view.
        mRecyclerView.setAdapter(mAdapter);
        // Give the recycler view a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
                //???????????????
                if (newState == mRecyclerView.SCROLL_STATE_IDLE) {
                    //recyclerview???????????????,????????????
                    //??????????????????
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
        getMoreData();
    }

    private void getMoreData(){
        if (isFinish)
            return;
        String requestUrl4 = "http://43.138.84.226:8080/demonstrate/own_dynamics";
        PersonFragment.MyThreadGetPersonDynamic myThread4 = new PersonFragment.MyThreadGetPersonDynamic(requestUrl4, page);// TO DO
        myThread4.start();// TO DO

        page++;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //binding = null;
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
                //3.??????MultipartBody
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
                        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string()); // String ??? JSONObject
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
                //3.??????MultipartBody
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
                //3.??????MultipartBody
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
                //3.??????MultipartBody
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

    class MyThreadGetPersonDynamic extends Thread{
        private  String requestUrl;
        MyThreadGetPersonDynamic(String request, int page){
            requestUrl = request + "/" + page;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                //3.??????MultipartBody
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