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
import android.widget.ImageView;
import android.widget.PopupWindow;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InfoeditActivity extends AppCompatActivity {
    private static final String LOG_TAG = InfoeditActivity.class.getSimpleName();
    private String filepath;
    private Button button;
    private Bitmap image;
    private ImageView pic;
    private static final int PERMISSION_APPLY = 1;
    private static final int PHOTO_PICK = 2;
    private static final int PICTURE_CROPPING_CODE = 200;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String res = (String) msg.obj;
            AlertDialog textTips = new AlertDialog.Builder(InfoeditActivity.this)
                    .setTitle("Tips:")
                    .setMessage(res)
                    .create();
            textTips.show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infoedit);
        button = (Button)findViewById(R.id.button);
        pic = (ImageView) findViewById(R.id.pic);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 动态权限申请
                if (Build.VERSION.SDK_INT >= 23) {
                    if(ContextCompat.checkSelfPermission(InfoeditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 申请读写内存卡内容的权限
                        ActivityCompat.requestPermissions(InfoeditActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSION_APPLY);
                    }
                }

                System.out.println("onClick");
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, PHOTO_PICK);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO 自动生成的方法存根
        System.out.println(requestCode+"");
        if(requestCode==PERMISSION_APPLY){
            System.out.println(resultCode);
        }
        if(requestCode==PHOTO_PICK){
            Uri uri = data.getData();
            pictureCropping(uri);
        }
        if(requestCode==PICTURE_CROPPING_CODE)
        {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                //在这里获得了剪裁后的Bitmap对象，可以用于上传
                image = bundle.getParcelable("data");
                //设置到ImageView上
                pic.setImageBitmap(image);

                // 上传图片
                super.onActivityResult(requestCode, resultCode, data);
                String requestUrl = "http://43.138.84.226:8080/user/photo";
                InfoeditActivity.MyThreadPhoto myThread = new InfoeditActivity.MyThreadPhoto(requestUrl);// TO DO
                myThread.start();// TO DO
            }

        }
//            //获得图片的uri
//            Uri uri = data.getData();
//
//            //外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
//            ContentResolver cr = this.getContentResolver();
//            Bitmap bitmap;
//            //Bitmap bm; //这是一种方式去读取图片
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
//                // TODO 自动生成的 catch 块
//                e.printStackTrace();
//                System.out.println("BAD");
//            }
    }


    class MyThreadPhoto extends Thread{
        private  String requestUrl;
        MyThreadPhoto(String request){
            requestUrl = request;
        }
        @Override
        public void run() {
            try {
                File f;
                // create a file to write bitmap data
                f = new File(InfoeditActivity.this.getCacheDir(), "portrait");
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
                //3.构建MultipartBody
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", f.getName(), fileBody)
                        .build();

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(requestBody)
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

    /**
     * 图片剪裁
     *
     * @param uri 图片uri
     */
    private void pictureCropping(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        // 返回裁剪后的数据
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PICTURE_CROPPING_CODE);
    }



}