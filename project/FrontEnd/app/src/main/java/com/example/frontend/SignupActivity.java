package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;


import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {
    // 注册页面，命名时命错了，懒得改名
    private EditText nickname, email, password, password2, verication;
    private static final String LOG_TAG = SignupActivity.class.getSimpleName();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String res = (String) msg.obj;
            AlertDialog textTips = new AlertDialog.Builder(SignupActivity.this)
                    .setTitle("Tips:")
                    .setMessage(res)
                    .create();
            textTips.show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        password2 = findViewById(R.id.password2);
        nickname = findViewById(R.id.nickname);
        verication = findViewById(R.id.verification);
    }

    // TO DO
    class MyThreadSendVerification extends Thread{
        private  String requestUrl,outputStr;
        MyThreadSendVerification(String request, String output){
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

    public void onSendVerificationClick(View v) {
        String m_email = email.getText().toString();
        if(m_email.isEmpty()){
            return;
        }

        String requestUrl = "http://43.138.84.226:8080/email_verification_code";
        MyThreadSendVerification myThread = new MyThreadSendVerification(requestUrl, m_email);// TO DO
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
        String m_verification = verication.getText().toString();
        if (!m_password.equals(m_password2)){
            Message msg = new Message();
            msg.obj = "两次密码不同";
            handler.sendMessage(msg);
            return;
        }
        if(m_email.isEmpty() || m_password.isEmpty() || m_nickname.isEmpty() || m_verification.isEmpty()){
            Message msg = new Message();
            msg.obj = "未填写完全";
            handler.sendMessage(msg);
            return;
        }

        String requestUrl = "http://43.138.84.226:8080/signup";
        MyThreadSignup myThread = new MyThreadSignup(requestUrl, m_email, m_password, m_nickname, m_verification);// TO DO
        myThread.start();// TO DO
    }


}