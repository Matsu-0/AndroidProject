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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.w4lle.library.NineGridlayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Shows how to implement a simple Adapter for a RecyclerView.
 * Demonstrates how to add a click handler for each item in the ViewHolder.
 */
public class DynamicListAdapter extends
        RecyclerView.Adapter<DynamicListAdapter.WordViewHolder> {
    private final LayoutInflater mInflater;
    private final JSONArray dynamic_list;
    private Context context;
    private int deleteType;
    private int TYPE_ITEM = 0;
    private int TYPE_FOOT = 4;
    private int TYPE_PIC = 1;
    private int TYPE_VIDEO = 3;
    private int TYPE_AUDIO = 2;
    private int DELETE_TYPE_BAN = 0;
    private int DELETE_TYPE_USE = 1;
    private int DELETE_TYPE_JUDGE = 2;
    public Boolean hasmore;

    class WordViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final TextView dynamicTitleView, dynamicDetailView, dynamicLocationView, dynamicTimeView, dynamicTypeView, dynamicFootView;
        public final LinearLayout dynamicNormalView;
        public final Button deleteButtonView;
        public final NineGridlayout picView;
        public JSONObject obj;
        private List<String> path;
        public int type, dynamic_num;
        final DynamicListAdapter mAdapter;

        /**
         * Creates a new custom view holder to hold the view to display in
         * the RecyclerView.
         *
         * @param itemView The view in which to display the data.
         * @param adapter The adapter that manages the the data and views
         *                for the RecyclerView.
         */
        public WordViewHolder(View itemView, DynamicListAdapter adapter, Boolean ifMyDynamic) {
            super(itemView);
            dynamicTitleView = itemView.findViewById(R.id.dynamic_title);
            dynamicDetailView = itemView.findViewById(R.id.dynamic_detail);
            dynamicLocationView = itemView.findViewById(R.id.dynamic_position);
            dynamicTimeView = itemView.findViewById(R.id.dynamic_time);
            deleteButtonView = itemView.findViewById(R.id.dynamic_delete);
            dynamicTypeView = itemView.findViewById(R.id.dynamic_type);
            dynamicFootView = itemView.findViewById(R.id.dynamic_loading);
            dynamicNormalView = itemView.findViewById(R.id.dynamic_normal);
            path = new ArrayList<>();
            if (!ifMyDynamic){
                deleteButtonView.setVisibility(View.GONE);
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

    public DynamicListAdapter(Context context,JSONArray num, int type) {
        mInflater = LayoutInflater.from(context);
        this.hasmore = true;
        this.dynamic_list = num;
        this.context = context;
        this.deleteType = type;
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
        if (deleteType == DELETE_TYPE_BAN){
            isDeleteShow = false;
        }
        else if (deleteType == DELETE_TYPE_USE){
            isDeleteShow = true;
        }
        else if (deleteType == DELETE_TYPE_JUDGE){
            isDeleteShow = true;
        }
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

        return new WordViewHolder(mItemView, this, isDeleteShow);
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
                JSONArray picList = holder.obj.getJSONArray("pic_list");
                holder.path.clear();
                for (int i = 0; i < picList.length(); ++i){
                    holder.path.add("http://43.138.84.226:8080/demonstrate/show_picture/" + picList.getString(i));
                }
                PicListAdapter adapter = new PicListAdapter(context, holder.path);
                holder.picView.setAdapter(adapter);
                holder.picView.setVisibility(View.VISIBLE);
                holder.dynamicTypeView.setText("图片");
            }
            holder.dynamic_num = holder.obj.getInt("dynamic_id");
            holder.deleteButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("111", holder.dynamic_num + "");
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
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

}