package com.example.frontend;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.w4lle.library.NineGridAdapter;

import java.io.File;
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
        ImageView iv = new ImageView(context);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(Color.parseColor("#f5f5f5"));

        Picasso.with(context)
                .load( new File(getUrl(i))  )
                .placeholder(new ColorDrawable(Color.parseColor("#f5f5f5")))
                .into(iv);
        return iv;
        }
}
