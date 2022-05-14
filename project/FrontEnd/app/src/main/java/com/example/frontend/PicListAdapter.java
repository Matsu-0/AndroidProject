package com.example.frontend;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.w4lle.library.NineGridAdapter;

import java.util.List;

public class PicListAdapter extends NineGridAdapter {

public PicListAdapter(Context context, List list) {
        super(context, list);
        }

@Override
public int getCount() {
        return (list == null) ? 0 : list.size();
        }

@Override
public String getUrl(int position) {
        Log.d("1111",getItem(position).toString());
        return getItem(position) == null ? null : getItem(position).toString();
        }

@Override
public Object getItem(int position) {
        return (list == null) ? null : list.get(position);
        }

@Override
public long getItemId(int position) {
        return position;
        }

@Override
public View getView(int i, View view) {
//        ImageView imageView = new ImageView(context);
//        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//
//        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageView.setBackgroundColor(Color.parseColor("#f5f5f5"));
//        imageView.setLayoutParams(lp);
//        Glide.with(context).load(getUrl(i)).into(imageView);
//        return imageView;
        ImageView iv = new ImageView(context);
        Log.d("234", "1");
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(Color.parseColor("#f5f5f5"));
        Picasso.with(context).load(getUrl(i)).placeholder(new ColorDrawable(Color.parseColor("#f5f5f5"))).into(iv);
        return iv;
        }
}
