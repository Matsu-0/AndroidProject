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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.LinkedList;

/**
 * Shows how to implement a simple Adapter for a RecyclerView.
 * Demonstrates how to add a click handler for each item in the ViewHolder.
 */
public class NoticeAdapter extends
        RecyclerView.Adapter<NoticeAdapter.WordViewHolder> {

    private final LinkedList<String> mNameList;
    private final LinkedList<String> mAvatarList;
    private final LinkedList<String> mNoticeDetailList;
    private final LinkedList<String> mCommentDetailList;
    private final LinkedList<String> mDynamicTitleList;
    private final LinkedList<String> mDynamicContentList;
    private final LinkedList<String> mDynamicTypeList;
    private final LinkedList<Integer> mDynamicIDList;

    private final LayoutInflater mInflater;
    private int TYPE_ITEM = 0;//正常的Item
    private int TYPE_FOOT = 1;//尾部刷新
    private Context context;

    class WordViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final ImageView avatarItemView;
        public final TextView nameItemView, noticeItemView, commentItemView;
        public final TextView dynamicTitleItemView, dynamicContentItemView, dynamicTypeItemView;
        public int DynamicID;
        public String author_email;
        final NoticeAdapter mAdapter;

        /**
         * Creates a new custom view holder to hold the view to display in
         * the RecyclerView.
         *
         * @param itemView The view in which to display the data.
         * @param adapter The adapter that manages the the data and views
         *                for the RecyclerView.
         */
        public WordViewHolder(View itemView, NoticeAdapter adapter) {
            super(itemView);
            avatarItemView = itemView.findViewById(R.id.notice_avatar);
            nameItemView = itemView.findViewById(R.id.notice_nickname);
            noticeItemView = itemView.findViewById(R.id.notice_detail);
            commentItemView = itemView.findViewById(R.id.comment_detail);
            dynamicTitleItemView = itemView.findViewById(R.id.dynamic_title);
            dynamicContentItemView = itemView.findViewById(R.id.dynamic_content);
            dynamicTypeItemView = itemView.findViewById(R.id.dynamic_type);

            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(context, ShowDynamicActivity.class);
            intent.putExtra("dynamic_id", DynamicID);
            context.startActivity(intent);
            // Get the position of the item that was clicked.
//            int mPosition = getLayoutPosition();
//
//            // Use that to access the affected item in mAvatarList.
//            String element = mNameList.get(mPosition);

        }
    }

    public NoticeAdapter(Context context, LinkedList<String> NameList, LinkedList<String> AvatarList,
                         LinkedList<String> NoticeDetailList, LinkedList<String> CommentDetailList,
                         LinkedList<String> DynamicTitleList, LinkedList<String> DynamicContentList,
                         LinkedList<String> DynamicTypeList, LinkedList<Integer> DynamicIDList) {
        mInflater = LayoutInflater.from(context);
        this.mNameList = NameList;
        this.mAvatarList = AvatarList;
        this.mNoticeDetailList = NoticeDetailList;
        this.mCommentDetailList = CommentDetailList;
        this.mDynamicTitleList = DynamicTitleList;
        this.mDynamicContentList = DynamicContentList;
        this.mDynamicTypeList = DynamicTypeList;
        this.mDynamicIDList = DynamicIDList;
        this.context = context;
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
    public NoticeAdapter.WordViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.notice_item, parent, false);
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
    public void onBindViewHolder(NoticeAdapter.WordViewHolder holder,
                                 int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            Log.d("path", mAvatarList.get(position));
            Picasso.with(context).load(mAvatarList.get(position)).into(holder.avatarItemView);
//            holder.avatarItemView.setImageBitmap(mBitmapList.get(position));

            // Add the data to the view holder.
            holder.nameItemView.setText(mNameList.get(position));
            holder.noticeItemView.setText(mNoticeDetailList.get(position));
            String comment = mCommentDetailList.get(position);
            if (comment != null && comment.length() != 0){
                holder.commentItemView.setText(comment);
                holder.commentItemView.setVisibility(View.VISIBLE);
            }
            holder.dynamicTitleItemView.setText(mDynamicTitleList.get(position));
            holder.dynamicContentItemView.setText(mDynamicContentList.get(position));
            holder.dynamicTypeItemView.setText(mDynamicTypeList.get(position));

            holder.DynamicID = (int) mDynamicIDList.get(position);
        }
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
}