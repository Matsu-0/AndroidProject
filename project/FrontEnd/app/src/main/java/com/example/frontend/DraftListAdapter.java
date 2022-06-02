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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Shows how to implement a simple Adapter for a RecyclerView.
 * Demonstrates how to add a click handler for each item in the ViewHolder.
 */
public class DraftListAdapter extends
        RecyclerView.Adapter<DraftListAdapter.WordViewHolder> {
    private static final int handlerStateUpdate = 100;
    private final LayoutInflater mInflater;
    private final List<Integer> draft_num_list;
    private Context context;
    private String sharedPrefFile ="com.example.frontend.draft";
    private int TYPE_ITEM = 0;//正常的Item
    private Handler handler;
    class WordViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final TextView draftTitleView, draftDetailView, draftTypeView;
        public int type, draft_num;
        final DraftListAdapter mAdapter;
        public final Button deleteButtonView;
        public final LinearLayout deleteView;
        /**
         * Creates a new custom view holder to hold the view to display in
         * the RecyclerView.
         *
         * @param itemView The view in which to display the data.
         * @param adapter The adapter that manages the the data and views
         *                for the RecyclerView.
         */
        public WordViewHolder(View itemView, DraftListAdapter adapter) {
            super(itemView);
            draftTitleView = itemView.findViewById(R.id.draft_title);
            draftDetailView = itemView.findViewById(R.id.draft_detail);
            draftTypeView = itemView.findViewById(R.id.draft_type);
            deleteButtonView = itemView.findViewById(R.id.draft_delete);
            deleteView = itemView.findViewById(R.id.draft_menu);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d("111",draft_num+"");
            if (type== 1){
                Intent intent = new Intent(context, PublishPicActivity.class);//想调到哪个界面就把login改成界面对应的activity名
                intent.putExtra("LOAD_DRAFT", draft_num);
                context.startActivity(intent);
            }
            else if (type== 2){
                Intent intent = new Intent(context, PublishVideoActivity.class);//想调到哪个界面就把login改成界面对应的activity名
                intent.putExtra("LOAD_DRAFT", draft_num);
                context.startActivity(intent);
            }
            else if (type== 3){
                Intent intent = new Intent(context, PublishAudioActivity.class);//想调到哪个界面就把login改成界面对应的activity名
                intent.putExtra("LOAD_DRAFT", draft_num);
                context.startActivity(intent);
            }

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

    public DraftListAdapter(Context context, LinkedList<Integer> num, Handler handler) {
        mInflater = LayoutInflater.from(context);
        this.draft_num_list = num;
        this.context = context;
        this.handler = handler;
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
    public DraftListAdapter.WordViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // Inflate an item view.
        View mItemView = mInflater.inflate(
                R.layout.draft_item, parent, false);
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
    public void onBindViewHolder(DraftListAdapter.WordViewHolder holder,
                                 int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            SharedPreferences mPreference = context.getSharedPreferences(sharedPrefFile+"_"+draft_num_list.get(position), MODE_PRIVATE);
            holder.draftTitleView.setText(mPreference.getString("title",""));
            holder.draftDetailView.setText(mPreference.getString("content",""));
            holder.type = mPreference.getInt("type",0);
            holder.draft_num = draft_num_list.get(position);
            if (mPreference.getInt("type",0) == 1){
                holder.draftTypeView.setText("图文");
            }
            else if (mPreference.getInt("type",0) == 2){
                holder.draftTypeView.setText("视频");
            }
            else if (mPreference.getInt("type",0) == 3){
                holder.draftTypeView.setText("音频");
            }
            else{
                holder.deleteView.setVisibility(View.GONE);

            }
            holder.deleteButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = mPreference.edit();
                    editor.putInt("type", 0);
                    editor.commit();
                    Message msg = handler.obtainMessage(handlerStateUpdate);
                    msg.obj = position;
                    handler.sendMessage(msg);
                }
            });
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return draft_num_list.size();
    }

}