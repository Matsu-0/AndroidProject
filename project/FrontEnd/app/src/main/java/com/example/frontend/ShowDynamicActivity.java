package com.example.frontend;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ShowDynamicActivity extends AppCompatActivity {
    TextView title, detail, location, audioFilename;
    Button audioPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_dynamic);
        title = (TextView) findViewById(R.id.dynamic_title);
        detail = (TextView) findViewById(R.id.dynamic_detail);
        location = (TextView) findViewById(R.id.location_text);
        audioFilename = (TextView) findViewById(R.id.audio_filename);
        audioPlayer = (Button) findViewById(R.id.audio_play);


    }
}
