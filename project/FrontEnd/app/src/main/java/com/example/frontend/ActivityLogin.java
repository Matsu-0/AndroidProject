package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActivityLogin extends AppCompatActivity {

    private EditText nickname, email, password, password2, verication;
    private TextView result;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String res = (String) msg.obj;
            result.setText(res);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        password2 = findViewById(R.id.password2);
        nickname = findViewById(R.id.nickname);
        verication = findViewById(R.id.verication);
        result = findViewById(R.id.result);
    }

    // TO DO
    class MyThreadSendVerication extends Thread{
        private  String requestUrl,outputStr;
        MyThreadSendVerication(String request, String output){
            requestUrl = request;
            outputStr = output;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()
                        .add("email", outputStr)
                        .build();

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(formBody)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();

                if (response.isSuccessful()) {
                    Message msg = new Message();
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

    public void onSendVericationClick(View v) {
        String m_email = email.getText().toString();
        if(m_email.isEmpty()){
            return;
        }

        String requestUrl = "http://43.138.84.226:8080/email_vertification_code";
        MyThreadSendVerication myThread = new MyThreadSendVerication(requestUrl, m_email);// TO DO
        myThread.start();// TO DO
    }

    // TO DO
    class MyThreadSignup extends Thread{
        private  String requestUrl,emailStr, passwordStr, nicknameStr, vericationStr;
        MyThreadSignup(String request, String m_email, String m_password, String m_nickname, String m_verication){
            requestUrl = request;
            emailStr = m_email;
            passwordStr = m_password;
            nicknameStr = m_nickname;
            vericationStr = m_verication;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()
                        .add("email", emailStr)
                        .add("password", passwordStr)
                        .add("nickname", nicknameStr)
                        .add("code", vericationStr)
                        .build();

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(formBody)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();

                if (response.isSuccessful()) {
                    Message msg = new Message();
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

    public void onSignUpClick(View v) {
        String m_email = email.getText().toString();
        String m_password = password.getText().toString();
        String m_password2 = password2.getText().toString();
        String m_nickname = nickname.getText().toString();
        String m_verication = verication.getText().toString();
        if (!m_password.equals(m_password2)){
            Message msg = new Message();
            msg.obj = "两次密码不同";
            handler.sendMessage(msg);
            return;
        }
        if(m_email.isEmpty() || m_password.isEmpty() || m_nickname.isEmpty() || m_verication.isEmpty()){
            Message msg = new Message();
            msg.obj = "未填写完全";
            handler.sendMessage(msg);
            return;
        }

        String requestUrl = "http://43.138.84.226:8080/signup";
        MyThreadSignup myThread = new MyThreadSignup(requestUrl, m_email, m_password, m_nickname, m_verication);// TO DO
        myThread.start();// TO DO
    }


}