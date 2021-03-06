package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InfoeditActivity extends AppCompatActivity {
    private static final String LOG_TAG = InfoeditActivity.class.getSimpleName();
    private Button button, confirm_edit_button, password_edit_button;
    private Bitmap image;
    private ImageView pic;
    private Uri uri;
    private EditText name, introduction, old_password, new_password, new_password_confirm;
    private static final int PERMISSION_APPLY = 1;
    private static final int PHOTO_PICK = 2;
    private static final int PICTURE_CROPPING_CODE = 200;
    private static final int handlerStateWarning = 0;
    private static final int handlerStateUpdatePhoto = 1;
    private static final int handlerStateUpdateName = 2;
    private static final int handlerStateUpdateIntroduction = 3;
    private static final int handlerStateGetPhoto = 4;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == handlerStateWarning) {
                String res = (String) msg.obj;
                AlertDialog textTips = new AlertDialog.Builder(InfoeditActivity.this)
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
                InfoeditActivity.MyThreadGetPhoto myThread = new InfoeditActivity.MyThreadGetPhoto(requestUrl, res);// TO DO
                myThread.start();// TO DO

//                String filename = "http://43.138.84.226:8080/user/show_avator/" + res;
//                // activity?????? getContext() ?????? context
//                Picasso.with(InfoeditActivity.this).load(filename).into(pic);

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infoedit);
        button = (Button)findViewById(R.id.pic_button);
        confirm_edit_button = (Button)findViewById(R.id.confirm_edit);
        password_edit_button = (Button)findViewById(R.id.password_edit);
        pic = (ImageView) findViewById(R.id.pic);
        name = (EditText) findViewById(R.id.person_name_edit);
        old_password =  (EditText) findViewById(R.id.old_password);
        new_password =  (EditText) findViewById(R.id.new_password);
        new_password_confirm =  (EditText) findViewById(R.id.new_password_confirm);
        introduction = (EditText) findViewById(R.id.person_Introduction_edit);
        String requestUrl = "http://43.138.84.226:8080/user/show_user_data";
        InfoeditActivity.MyThreadInitData myThread = new InfoeditActivity.MyThreadInitData(requestUrl);// TO DO
        myThread.start();// TO DO

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ??????????????????
                if (Build.VERSION.SDK_INT >= 23) {
                    if((ContextCompat.checkSelfPermission(InfoeditActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)
                            || (ContextCompat.checkSelfPermission(InfoeditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED )) {
                        // ????????????????????????????????????
                        ActivityCompat.requestPermissions(InfoeditActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_APPLY);
                        return;
                    }
                }

                System.out.println("onClick");
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, PHOTO_PICK);
            }
        });

        confirm_edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name_new = name.getText().toString();
                String introduction_new = introduction.getText().toString();
                String requestUrl = "http://43.138.84.226:8080/user/modify_data";
                InfoeditActivity.MyThreadUpdateInfo myThread = new InfoeditActivity.MyThreadUpdateInfo(requestUrl, name_new, introduction_new );// TO DO
                myThread.start();// TO DO
            }
        });

        password_edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String old_passwordStr = old_password.getText().toString();
                String new_passwordStr = new_password.getText().toString();
                String new_password_confirmStr = new_password_confirm.getText().toString();
                if (! new_passwordStr.equals(new_password_confirmStr)){
                    Message msg = handler.obtainMessage(handlerStateWarning);
                    msg.obj = "??????????????????";
                    handler.sendMessage(msg);
                }
                else {
                    String requestUrl = "http://43.138.84.226:8080/user/modify_password";
                    InfoeditActivity.MyThreadModifyPassword myThread = new InfoeditActivity.MyThreadModifyPassword(requestUrl, old_passwordStr, new_passwordStr );// TO DO
                    myThread.start();// TO DO
                    old_password.setText("");
                    new_password.setText("");
                    new_password_confirm.setText("");
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO ???????????????????????????
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(requestCode+"");
        if(requestCode==PERMISSION_APPLY){
            System.out.println("resultCode" + resultCode);
        }
        if(requestCode==PHOTO_PICK){
            uri = data.getData();
            pictureCropping(uri);
        }
        if(requestCode==PICTURE_CROPPING_CODE)
        {
            if (resultCode == 0){
                pic.setImageURI(uri);
                InputStream image_stream = null;
                try {
                    image_stream = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                image = BitmapFactory.decodeStream(image_stream );
                String requestUrl = "http://43.138.84.226:8080/user/modify_avator";
                InfoeditActivity.MyThreadPhoto myThread = new InfoeditActivity.MyThreadPhoto(requestUrl);// TO DO
                myThread.start();// TO DO
                return;
            }
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                //??????????????????????????????Bitmap???????????????????????????
                image = bundle.getParcelable("data");
                //?????????ImageView???
                pic.setImageBitmap(image);

                // ????????????

                String requestUrl = "http://43.138.84.226:8080/user/modify_avator";
                InfoeditActivity.MyThreadPhoto myThread = new InfoeditActivity.MyThreadPhoto(requestUrl);// TO DO
                myThread.start();// TO DO
            }

        }
//            //???????????????uri
//            Uri uri = data.getData();
//
//            //?????????????????????ContentProvider??????????????? ????????????ContentResolver??????
//            ContentResolver cr = this.getContentResolver();
//            Bitmap bitmap;
//            //Bitmap bm; //?????????????????????????????????
//            try
//            {
//                //bm = MediaStore.Images.Media.getBitmap(cr, uri);
//                //pic.setImageBitmap(bm);
//                bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
//                pic.setImageBitmap(bitmap);
//                System.out.println("GOOD");
//
//                String[] filePc = {MediaStore.Images.Media.DATA};
//                Cursor cursor = getContentResolver().query(uri, filePc, null, null, null);
//                cursor.moveToFirst();
//                int col = cursor.getColumnIndex(filePc[0]);
//                filepath = cursor.getString(col);
//                cursor.close();
//
//
////                String uri2Str = uri.toString();
////                filepath = uri2Str.substring(uri2Str.indexOf(":") + 3);
////                System.out.println(uri2Str);
//
//            }
//            catch (Exception e)
//            {
//                // TODO ??????????????? catch ???
//                e.printStackTrace();
//                System.out.println("BAD");
//            }
    }


    class MyThreadPhoto extends Thread{
        private String requestUrl;
        MyThreadPhoto(String request){
            requestUrl = request;
        }
        @Override
        public void run() {
            try {
                File f;
                // create a file to write bitmap data
                f = new File(InfoeditActivity.this.getCacheDir(), "portrait.png");
                f.createNewFile();
                // convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();
                // write the bytes in file
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                OkHttpClient client = new OkHttpClient();
//                file = new File(filepath);
                MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
                RequestBody fileBody = RequestBody.create(MEDIA_TYPE_PNG, f);
                //3.??????MultipartBody
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");
                Log.d(LOG_TAG, cookie);
                Log.d(LOG_TAG, f.getName());
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", f.getName(), fileBody)
                        .build();

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

    /**
     * ????????????
     *
     * @param uri ??????uri
     */
    private void pictureCropping(Uri uri) {
        // ????????????????????????????????????
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // ????????????crop=true?????????????????????Intent??????????????????VIEW?????????
        intent.putExtra("crop", "true");
        // aspectX aspectY ??????????????????
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY ?????????????????????
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        // ????????????????????????
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PICTURE_CROPPING_CODE);
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
                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
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

    class MyThreadUpdateInfo extends Thread{
        private String requestUrl, name, introduction;
        MyThreadUpdateInfo(String request, String nameStr, String introductionStr){
            requestUrl = request;
            name = nameStr;
            introduction = introductionStr;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();

                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                RequestBody formBody = new FormBody.Builder()
                        .add("nickname", name)
                        .add("introduction", introduction)
                        .build();

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(formBody)
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

    class MyThreadModifyPassword extends Thread{
        private String requestUrl, oldPassword, newPassword;
        MyThreadModifyPassword(String request, String oldStr, String newStr){
            requestUrl = request;
            oldPassword = oldStr;
            newPassword = newStr;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();

                SharedPreferences sharedPreferences = getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                RequestBody formBody = new FormBody.Builder()
                        .add("old_password", oldPassword)
                        .add("new_password", newPassword)
                        .build();

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(formBody)
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