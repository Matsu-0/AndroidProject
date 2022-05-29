package com.example.frontend.ui.browse;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.DynamicListAdapter;
import com.example.frontend.LikeListActivity;
import com.example.frontend.OthersActivity;
import com.example.frontend.R;
import com.example.frontend.ShowDynamicActivity;
import com.example.frontend.databinding.FragmentBrowseBinding;
import com.example.frontend.ui.person.PersonFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BrowseFragment extends Fragment {

    private FragmentBrowseBinding binding;

    private RecyclerView mRecyclerView;
    private DynamicListAdapter mAdapter;
    private JSONArray dynamic_list;

    private static final String LOG_TAG = BrowseFragment.class.getSimpleName();

    private RadioGroup radio_choose_sequence, radio_choose_range, radio_choose_type;
    private RelativeLayout search_list;
    private Button search_option, search, search_clear;
    private Boolean isTimeSeq, isAllRange, isFinish;
    private int page;
    private static final int handlerStateWarning = 0;
    private static final int handlerStateGetDynamics = 1;
    private static final int handlerStateDynamicsFinish = 2;
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
            else if (msg.what == handlerStateGetDynamics) {
                try {
                    JSONObject result = new JSONObject(Objects.requireNonNull(msg.obj).toString()); // String 转 JSONObject
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
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BrowseViewModel browseViewModel =
                new ViewModelProvider(this).get(BrowseViewModel.class);

        binding = FragmentBrowseBinding.inflate(inflater, container, false);

        View root = inflater.inflate(R.layout.fragment_browse,container,false);

        super.onCreate(savedInstanceState);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        radio_choose_sequence = (RadioGroup) getActivity().findViewById(R.id.choose_sequence);
        radio_choose_range = (RadioGroup) getActivity().findViewById(R.id.choose_range);
        radio_choose_type = (RadioGroup) getActivity().findViewById(R.id.search_dynamic_type);

        search_list = (RelativeLayout) getActivity().findViewById(R.id.search_list);
        search_option = (Button) getActivity().findViewById(R.id.search_option);
        search = (Button) getActivity().findViewById(R.id.search_begin);
        search_clear = (Button) getActivity().findViewById(R.id.search_clear);

        mRecyclerView = getActivity().findViewById(R.id.dynamic_recycle_view_all);
            // Create an adapter and supply the data to be displayed.
        isTimeSeq = true;
        isAllRange = true;
        reset();

        search_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (search_option.getText().equals("搜索选项")){
                    search_list.setVisibility(View.VISIBLE);
                    search_option.setText("隐藏搜索选项");
                }
                else {
                    search_list.setVisibility(View.GONE);
                    search_option.setText("搜索选项");
                }
            }
        });
        radio_choose_sequence.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //滑动到底部
                switch (checkedId) {
                    case R.id.choose_time_sequence:
                        isTimeSeq = true;
                        break;
                    case R.id.choose_like_sequence:
                        isTimeSeq = false;
                        break;
                    default:
                        break;

                }
                reset();
                getMoreData();
            }
        });

        radio_choose_range.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //滑动到底部
                switch (checkedId) {
                    case R.id.choose_all_range:
                        isAllRange = true;
                        break;
                    case R.id.choose_followers_range:
                        isAllRange = false;
                        break;
                    default:
                        break;

                }
                reset();
                getMoreData();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter = new DynamicListAdapter(getActivity(), dynamic_list, 0);
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
                //滑动到底部
                if (newState == mRecyclerView.SCROLL_STATE_IDLE) {
                    //recyclerview滑动到底部,更新数据
                    //加载更多数据
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
        String requestUrl = "http://43.138.84.226:8080/demonstrate/all_dynamics";
        BrowseFragment.MyThreadGetAllDynamic myThread = new BrowseFragment.MyThreadGetAllDynamic(requestUrl, page, isTimeSeq, isAllRange);// TO DO
        myThread.start();// TO DO

        page++;
    }

    private void reset(){
        page = 1;
        dynamic_list = new JSONArray();
        isFinish = false;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class MyThreadGetAllDynamic extends Thread{
        private  String requestUrl;
        private int page;
        private Boolean isTimeSeq, isAllRange;
        MyThreadGetAllDynamic(String request, int page, Boolean seq, Boolean range){
            requestUrl = request;
            this.page = page;
            isTimeSeq = seq;
            isAllRange = range;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                //3.构建MultipartBody
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");
                Log.d(LOG_TAG, cookie);
                Log.d(LOG_TAG, page + " "+ isTimeSeq + " " + isAllRange);
                FormBody.Builder builder = new FormBody.Builder()
                        .add("page", page+"");
                if (isTimeSeq) {
                    builder.add("sorted", "1");
                }
                else {
                    builder.add("sorted", "2");
                }

                if (isAllRange) {
                    builder.add("range", "1");
                }
                else {
                    builder.add("range", "2");
                }
                RequestBody formBody = builder.build();
                Request request = new Request.Builder()
                        .url(requestUrl)
                        .post(formBody)
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