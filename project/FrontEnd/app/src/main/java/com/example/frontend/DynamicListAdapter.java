package com.example.frontend;
/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.ui.person.PersonFragment;
import com.squareup.picasso.Picasso;
import com.w4lle.library.NineGridlayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Shows how to implement a simple Adapter for a RecyclerView.
 * Demonstrates how to add a click handler for each item in the ViewHolder.
 */
public class DynamicListAdapter extends
        RecyclerView.Adapter<DynamicListAdapter.WordViewHolder> {
    private final LayoutInflater mInflater;
    private final JSONArray dynamic_list;
    private Context context;
    private int showtype;       // 表示显示模式，1表示本人主页，0表示他人主页，2表示动态主页
    private int TYPE_ITEM = 0;
    private int TYPE_FOOT = 4;
    private int TYPE_PIC = 1;
    private int TYPE_VIDEO = 3;
    private int TYPE_AUDIO = 2;
    private int SHOW_TYPE_OTHER = 0;
    private int SHOW_TYPE_MYSELF = 1;
    private int SHOW_TYPE_ALL = 2;
    private static final int handlerStateWarning = 0;
    private static final int handlerStateDeleteSucceed = 100;
    private static final int handlerStateUpdateAllFollowButton = 101;
    private Handler handler;
    public Boolean hasmore;

    class WordViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final TextView dynamicTitleView, dynamicDetailView, dynamicLocationView, dynamicTimeView, dynamicTypeView, dynamicFootView, authorNameView;
        public final LinearLayout dynamicNormalView;
        public final RelativeLayout authorInfoView;
        public final Button deleteButtonView, followButtonView;
        public final ImageView avatar;
        public final NineGridlayout picView;
        public JSONObject obj;
        private List<String> path;
        public int type, dynamic_num, show_type;
        public String author_email;
        final DynamicListAdapter mAdapter;
        private Boolean isFollow;

        /**
         * Creates a new custom view holder to hold the view to display in
         * the RecyclerView.
         *
         * @param itemView The view in which to display the data.
         * @param adapter The adapter that manages the the data and views
         *                for the RecyclerView.
         */
        public WordViewHolder(View itemView, DynamicListAdapter adapter, int showType) {
            super(itemView);
            dynamicTitleView = itemView.findViewById(R.id.dynamic_title);
            dynamicDetailView = itemView.findViewById(R.id.dynamic_detail);
            dynamicLocationView = itemView.findViewById(R.id.dynamic_position);
            dynamicTimeView = itemView.findViewById(R.id.dynamic_time);
            deleteButtonView = itemView.findViewById(R.id.dynamic_delete);
            dynamicTypeView = itemView.findViewById(R.id.dynamic_type);
            dynamicFootView = itemView.findViewById(R.id.dynamic_loading);
            dynamicNormalView = itemView.findViewById(R.id.dynamic_normal);
            authorNameView = itemView.findViewById(R.id.author_name);
            authorInfoView = itemView.findViewById(R.id.author_info);
            followButtonView = itemView.findViewById(R.id.author_follow);
            avatar =  itemView.findViewById(R.id.author_avatar);
            path = new ArrayList<>();
            show_type = showType;

            if (show_type == 0){
                deleteButtonView.setVisibility(View.GONE);
                authorInfoView.setVisibility(View.GONE);
            }
            if (show_type == 1){
                deleteButtonView.setVisibility(View.VISIBLE);
                authorInfoView.setVisibility(View.GONE);
            }
            if (show_type == 2){
                authorInfoView.setVisibility(View.VISIBLE);
            }
            picView  = itemView.findViewById(R.id.grid_layout_pic);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (type == TYPE_FOOT)
                return;
            Intent intent = new Intent(context, ShowDynamicActivity.class);//想调到哪个界面就把login改成界面对应的activity名
            intent.putExtra("dynamic_id", dynamic_num);
            context.startActivity(intent);
//            if (type== 1){
//                Intent intent = new Intent(context, PublishPicActivity.class);//想调到哪个界面就把login改成界面对应的activity名
//                intent.putExtra("LOAD_DRAFT", draft_num);
//                context.startActivity(intent);
//            }
//            else if (type== 2){
//                Intent intent = new Intent(context, PublishVideoActivity.class);//想调到哪个界面就把login改成界面对应的activity名
//                intent.putExtra("LOAD_DRAFT", draft_num);
//                context.startActivity(intent);
//            }
//            else if (type== 3){
//                Intent intent = new Intent(context, PublishAudioActivity.class);//想调到哪个界面就把login改成界面对应的activity名
//                intent.putExtra("LOAD_DRAFT", draft_num);
//                context.startActivity(intent);
//            }

            // Get the position of the item that was clicked.
//            int mPosition = getLayoutPosition();
//
//            // Use that to access the affected item in mAvatarList.
//            String element = mNameList.get(mPosition);


            // Change the word in the mAvatarList.

            // mAvatarList.set(mPosition, "Clicked! " + element);
            // Notify the adapter, that the data has changed so it can
            // update the RecyclerView to display the data.
            // mAdapter.notifyDataSetChanged();
        }
    }

    public DynamicListAdapter(Context context,JSONArray num, int type, Handler mhandler) {
        mInflater = LayoutInflater.from(context);
        this.hasmore = true;
        this.dynamic_list = num;
        this.context = context;
        this.showtype = type;
        this.handler = mhandler;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to
     * represent an item.
     *
     * This new ViewHolder should be constructed with a new View that can
     * represent the items of the given type. You can either create a new View
     * manually or inflate it from an XML layout file.
     *
     * The new ViewHolder will be used to display items of the adapter using
     * onBindViewHolder(ViewHolder, int, List). Since it will be reused to
     * display different items in the data set, it is a good idea to cache
     * references to sub views of the View to avoid unnecessary findViewById()
     * calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after
     *                 it is bound to an adapter position.
     * @param viewType The view type of the new View. @return A new ViewHolder
     *                 that holds a View of the given view type.
     */
    @Override
    public DynamicListAdapter.WordViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType) {
        // Inflate an item view.
        View mItemView;
        Boolean isDeleteShow = true;

        if (viewType == TYPE_PIC) {
            mItemView = mInflater.inflate(
                    R.layout.dynamic_item_pic, parent, false);
        }
        else if (viewType == TYPE_VIDEO) {
            mItemView = mInflater.inflate(
                    R.layout.dynamic_item_pic, parent, false);
        }
        else if (viewType == TYPE_AUDIO) {
            mItemView = mInflater.inflate(
                    R.layout.dynamic_item_pic, parent, false);
        }
        else {
            mItemView = mInflater.inflate(
                    R.layout.dynamic_item_pic, parent, false);
        }

        return new WordViewHolder(mItemView, this, this.showtype);
    }

    //返回不同布局
    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_FOOT;
        }
        try {
            if (dynamic_list.getJSONObject(position).getInt("type") == 1){
                return TYPE_PIC;
            }
            if (dynamic_list.getJSONObject(position).getInt("type") == 3){
                return TYPE_VIDEO;
            }
            if (dynamic_list.getJSONObject(position).getInt("type") == 2){
                return TYPE_AUDIO;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return TYPE_ITEM;
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the ViewHolder.itemView to
     * reflect the item at the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent
     *                 the contents of the item at the given position in the
     *                 data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(DynamicListAdapter.WordViewHolder holder,
                                 int position) {
        holder.type = getItemViewType(position);
        if ( holder.type == TYPE_FOOT){
            if (!hasmore){
                holder.dynamicFootView.setText("已经到底啦，不要再翻了");
            }
            holder.dynamicFootView.setVisibility(View.VISIBLE);
            holder.dynamicNormalView.setVisibility(View.GONE);
            return;
        }
        JSONObject obj = null;
        try {
            holder.obj = dynamic_list.getJSONObject(position);
            holder.dynamicTitleView.setText(holder.obj.getString("title"));
            holder.dynamicDetailView.setText(holder.obj.getString("content"));
            holder.dynamicLocationView.setText(holder.obj.getString("location"));
            holder.dynamicTimeView.setText(holder.obj.getString("release_time"));

            if (holder.type == TYPE_VIDEO){
                holder.dynamicTypeView.setText("视频");
            } else if (holder.type == TYPE_AUDIO){
                holder.dynamicTypeView.setText("音频");
            } else if (holder.type == TYPE_PIC){
                holder.dynamicTypeView.setText("图片");
//                // 注释部分用于在动态展示页面显示图片，但由于过卡，所以暂时注释掉
                JSONArray picList = holder.obj.getJSONArray("pic_list");
                holder.path.clear();
                for (int i = 0; i < picList.length(); ++i){
                    holder.path.add("http://43.138.84.226:8080/demonstrate/show_smaller_picture/" + picList.getString(i));
                }
                PicListAdapter adapter = new PicListAdapter(context, holder.path);
                holder.picView.setAdapter(adapter);
                holder.picView.setVisibility(View.VISIBLE);
            }
            holder.dynamic_num = holder.obj.getInt("dynamic_id");



            try {
                if (holder.show_type == SHOW_TYPE_ALL){
                    Log.d("111", holder.obj.toString());
//
                    holder.authorNameView.setText(holder.obj.getString("author_nickname"));
                    holder.author_email = holder.obj.getString("author");

                    if (holder.obj.getInt("author_ismfollow") == 1){
                        holder.isFollow = true;
                        holder.followButtonView.setText("取消关注");
                    } else if (holder.obj.getInt("author_ismfollow") == 0){
                        holder.isFollow = false;
                        holder.followButtonView.setText("关注");
                    }
                    if (holder.obj.getBoolean("author_ismyself") ){
                        holder.deleteButtonView.setVisibility(View.VISIBLE);
                        holder.followButtonView.setVisibility(View.GONE);
                    } else if (!holder.obj.getBoolean("author_ismyself") ){
                        holder.deleteButtonView.setVisibility(View.GONE);
                    }

                    String avatarPath = "http://43.138.84.226:8080/user/show_avator/" + holder.obj.getString("author_avator");
                    Picasso.with(context).load(avatarPath).into(holder.avatar);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

        holder.followButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DynamicListAdapter.MyThreadFollow myThread = new DynamicListAdapter.MyThreadFollow(holder.isFollow, holder.author_email );// TO DO
                myThread.start();
            }
        });

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OthersActivity.class);//想调到哪个界面就把login改成界面对应的activity名
                intent.putExtra("KEY_EMAIL", holder.author_email);
                context.startActivity(intent);
            }
        });

        holder.authorNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, OthersActivity.class);//想调到哪个界面就把login改成界面对应的activity名
                intent.putExtra("KEY_EMAIL", holder.author_email);
                context.startActivity(intent);
            }
        });


        holder.deleteButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("111", holder.dynamic_num + "");
                String requestUrl = "http://43.138.84.226:8080/demonstrate/delete_dynamic";
                DynamicListAdapter.MyThreadDelete myThread = new DynamicListAdapter.MyThreadDelete(requestUrl, holder.dynamic_num );// TO DO
                myThread.start();
            }
        });
//        if (getItemViewType(position) == TYPE_PIC){
//            try {
//                PicListAdapter adapter = new PicListAdapter(context, (List<String>) holder.obj.getJSONArray("pic_list"));
//                holder.picView.setAdapter(adapter);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return dynamic_list.length() + 1;
    }


    public class MyThreadDelete extends Thread {
        private String requestUrl;
        private int dynamicID;

        public MyThreadDelete(String request, int dynamic_id) {
            requestUrl = request + "/" + dynamic_id;
            dynamicID = dynamic_id;

        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
//                file = new File(filepath);

                SharedPreferences sharedPreferences = context.getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                Request request = new Request.Builder()
                        .url(requestUrl)
                        .get()
                        .addHeader("cookie",cookie)
                        .build();

                Call call = client.newCall(request);
                Response response = call.execute();
                Log.d("333", response.toString());
                if (response.isSuccessful()) {
                    Message msg = handler.obtainMessage(handlerStateWarning);
                    msg.obj = Objects.requireNonNull(response.body()).string();
                    handler.sendMessage(msg);
                    if (response.code()==200){
                        Message msg2 = handler.obtainMessage(handlerStateDeleteSucceed);
                        msg2.obj = this.dynamicID;
                        handler.sendMessage(msg2);
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

    class MyThreadFollow extends Thread{
        private  String requestUrl, email;
        private int FollowStatus;
        MyThreadFollow(Boolean isFollow, String email){
            this.email = email;
            if (isFollow){
                requestUrl = "http://43.138.84.226:8080/interact/not_follow_someone";
                FollowStatus = 1;
            }
            else{
                requestUrl = "http://43.138.84.226:8080/interact/follow_someone";
                FollowStatus = 0;
            }
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                //3.构建MultipartBody
                SharedPreferences sharedPreferences = context.getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                RequestBody formBody = new FormBody.Builder()
                        .add("email", email)
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

                    if (response.code() == 200){
                        Message msg1 = handler.obtainMessage(handlerStateUpdateAllFollowButton);
                        msg1.obj = Objects.requireNonNull(email);
                        msg1.arg1 = 1 - FollowStatus;
                        handler.sendMessage(msg1);
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