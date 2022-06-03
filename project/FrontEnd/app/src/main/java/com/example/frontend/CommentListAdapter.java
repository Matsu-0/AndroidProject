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
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Shows how to implement a simple Adapter for a RecyclerView.
 * Demonstrates how to add a click handler for each item in the ViewHolder.
 */
public class CommentListAdapter extends
        RecyclerView.Adapter<CommentListAdapter.WordViewHolder> {

    private final LinkedList<String> mNameList;
    private final LinkedList<String> mEmailList;
    private final LinkedList<String> mCommentList;
    private final LinkedList<String> mBitmapList;
    private final LinkedList<Integer> mFlagList;
    private final LinkedList<Integer> mCommentIDList;
    private final LayoutInflater mInflater;
    private int TYPE_ITEM = 0;//正常的Item
    private int TYPE_FOOT = 1;//尾部刷新
    private Context context;
    private Handler handler;

    private static final int handlerStateWarning = 0;
    private static final int handlerCancelCommentSuccess = 101;

    class WordViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final ImageView avatarItemView;
        public final TextView nameItemView, detailItemView;
        public final Button cancelComment;
        public String email;
        public int flag, commentID;
        final CommentListAdapter mAdapter;

        /**
         * Creates a new custom view holder to hold the view to display in
         * the RecyclerView.
         *
         * @param itemView The view in which to display the data.
         * @param adapter The adapter that manages the the data and views
         *                for the RecyclerView.
         */
        public WordViewHolder(View itemView, CommentListAdapter adapter) {
            super(itemView);
            detailItemView = itemView.findViewById(R.id.comment_detail);
            nameItemView = itemView.findViewById(R.id.comment_nickname);
            avatarItemView = itemView.findViewById(R.id.comment_avatar);
            cancelComment = itemView.findViewById(R.id.cancel_comment);

            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, OthersActivity.class);
            intent.putExtra("KEY_EMAIL", email);
            context.startActivity(intent);
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

    public CommentListAdapter(Context context, LinkedList<String> AvatarList, LinkedList<String> NameList,
                              LinkedList<String> EmailList, LinkedList<String> CommentList, LinkedList<Integer> FlagList,
                              LinkedList<Integer> CommentIDList, Handler mHandler) {
        mInflater = LayoutInflater.from(context);
        this.mNameList = NameList;
        this.mBitmapList = AvatarList;
        this.mEmailList = EmailList;
        this.mCommentList = CommentList;
        this.mFlagList = FlagList;
        this.mCommentIDList = CommentIDList;
        this.context = context;
        this.handler = mHandler;
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
    public CommentListAdapter.WordViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.comment_item, parent, false);
        return new WordViewHolder(mItemView, this);
    }

    //返回不同布局
    @Override
    public int getItemViewType(int position) {
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
    public void onBindViewHolder(CommentListAdapter.WordViewHolder holder,
                                 int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            // holder.avatarItemView.setImageBitmap(mBitmapList.get(position));
            Picasso.with(context).load(mBitmapList.get(position)).into(holder.avatarItemView);
            
            // Add the data to the view holder.
            holder.nameItemView.setText(mNameList.get(position));
            holder.detailItemView.setText(mCommentList.get(position));

            holder.email = mEmailList.get(position);
            holder.flag = mFlagList.get(position);
            holder.commentID = mCommentIDList.get(position);

            if (holder.flag == 1) {
                holder.cancelComment.setVisibility(View.VISIBLE);
            }
            else if (holder.flag == 2) {
                holder.cancelComment.setVisibility(View.GONE);
            }
        }

        holder.cancelComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String requestUrl = "http://43.138.84.226:8080/interact/cancel_comment";
                CommentListAdapter.MyThreadCancelComment myThread = new CommentListAdapter.MyThreadCancelComment(requestUrl, holder.commentID );// TO DO
                myThread.start();
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mNameList.size();
    }

    class MyThreadCancelComment extends Thread{
        private String requestUrl;
        private int commentID;

        MyThreadCancelComment(String request, int comment_id){
            this.requestUrl = request;
            this.commentID = comment_id;
        }
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                //3.构建MultipartBody
                SharedPreferences sharedPreferences = context.getSharedPreferences("login",MODE_PRIVATE);
                String cookie = sharedPreferences.getString("session","");

                RequestBody formBody = new FormBody.Builder()
                        .add("comment_id", commentID + "")
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
                        Message msg1 = handler.obtainMessage(handlerCancelCommentSuccess);
                        msg1.obj = Objects.requireNonNull(commentID);
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