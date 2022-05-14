package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.w4lle.library.NineGridAdapter;
import com.w4lle.library.NineGridlayout;

import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class PublishPicActivity extends AppCompatActivity {
    private Button button_loadPic, button_loadPos, button_launch;
    private NineGridlayout nineGridlayout;
    static final int PHOTO_RETURN_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_pic);
        button_loadPic = (Button) findViewById(R.id.add_pic);
        button_loadPos = (Button) findViewById(R.id.add_position_pic);
        button_launch = (Button) findViewById(R.id.publish_button_pic);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHOTO_RETURN_CODE){
            if(resultCode == RESULT_OK){
                // Get the result list of select image paths
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                nineGridlayout = findViewById(R.id.iv_ngrid_layout);
                PicListAdapter adapter = new PicListAdapter(PublishPicActivity.this, path);
                nineGridlayout.setVisibility(View.GONE);
                nineGridlayout.setAdapter(adapter);
                Log.d("111", path.get(0));
                // do your logic ....
            }
        }
    }
}
