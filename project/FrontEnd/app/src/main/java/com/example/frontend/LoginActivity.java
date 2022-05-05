package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String res = (String) msg.obj;
            AlertDialog textTips = new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("Tips:")
                    .setMessage(res)
                    .create();
            textTips.show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.email_login);
        password = findViewById(R.id.password_login);
    }

    class MyThreadLogin extends Thread{
        private  String requestUrl,emailStr, passwordStr;
        MyThreadLogin(String request, String m_email, String m_password){
            requestUrl = request;
            emailStr = m_email;
            passwordStr = m_password;
        }
        @Override
        public void run() {
            String s = "";
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()
                        .add("email", emailStr)
                        .add("password", passwordStr)
                        .build();

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(formBody)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                List<String> cookies = response.headers().values("Set-Cookie");
                String session = cookies.get(0);
                Log.d(LOG_TAG, "onResponse-size: " + cookies);
                s = session.substring(0, session.indexOf(";"));
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

            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()

                        .build();

                Request request = new Request.Builder()
                        .url("http://43.138.84.226:8080/test_session")
                        .post(formBody)
                        .addHeader("cookie", s)
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

    public void onLoginClick(View v) {
        String m_email = email.getText().toString();
        String m_password = password.getText().toString();
        if(m_email.isEmpty() || m_password.isEmpty()){
            Message msg = new Message();
            msg.obj = "未填写完全";
            handler.sendMessage(msg);
            return;
        }

        String requestUrl = "http://43.138.84.226:8080/login";
        LoginActivity.MyThreadLogin myThread = new LoginActivity.MyThreadLogin(requestUrl, m_email, m_password);// TO DO
        myThread.start();// TO DO
    }

    public void onGoToSignUp(View v) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}