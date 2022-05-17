package com.example.frontend.ui.publish;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.frontend.InfoeditActivity;
import com.example.frontend.PublishAudio;
import com.example.frontend.PublishDraftActivity;
import com.example.frontend.PublishPicActivity;
import com.example.frontend.PublishVideoActivity;
import com.example.frontend.R;
import com.example.frontend.databinding.FragmentPublishBinding;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class PublishFragment extends Fragment {
    private String sharedPrefFile ="com.example.frontend.draft";
    private FragmentPublishBinding binding;
    private Button button_pic, button_video, button_audio, button_draft;
    private static final int handlerStateWarning = 0;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PublishViewModel publishViewModel =
                new ViewModelProvider(this).get(PublishViewModel.class);

        binding = FragmentPublishBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        super.onCreate(savedInstanceState);
        // publishViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }
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
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        button_pic = (Button)getActivity().findViewById(R.id.button_publish_pic);
        button_video = (Button) getActivity().findViewById(R.id.button_publish_video);
        button_audio = (Button) getActivity().findViewById(R.id.button_publish_audio);
        button_draft = (Button) getActivity().findViewById(R.id.button_publish_draft);

        button_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PublishPicActivity.class);//想调到哪个界面就把login改成界面对应的activity名
                startActivity(intent);
            }
        });

        button_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PublishAudio.class);//想调到哪个界面就把login改成界面对应的activity名
                startActivity(intent);
            }
        });
        button_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PublishVideoActivity.class);//想调到哪个界面就把login改成界面对应的activity名
                startActivity(intent);

            }
        });

        button_draft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PublishDraftActivity.class);//想调到哪个界面就把login改成界面对应的activity名
                startActivity(intent);
//                SharedPreferences listPreferences = getActivity().getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
//                int cur = listPreferences.getInt("size", -1);
//                Log.d("1111", cur +"");
//                SharedPreferences mPreferences = getActivity().getSharedPreferences(sharedPrefFile+"_"+cur, MODE_PRIVATE);


            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}