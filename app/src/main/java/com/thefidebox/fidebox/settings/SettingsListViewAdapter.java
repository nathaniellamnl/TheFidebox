package com.thefidebox.fidebox.settings;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thefidebox.fidebox.R;

public class SettingsListViewAdapter extends BaseAdapter {
    private Context mContext;
    private String[] titles;
    private int[] images;
    private LayoutInflater mInflater;
    private int layoutResource;

    public SettingsListViewAdapter(Context context, String[] text1, int[] imageIds) {
        mContext = context;
        titles = text1;
        images = imageIds;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = R.layout.activity_settings_list_item;

    }

    private static class ViewHolder {
        TextView title;
        ImageView icon;

    }


    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {

            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.title=convertView.findViewById(R.id.tv_title);
            holder.icon=convertView.findViewById(R.id.iv_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();

        }


        holder.title.setText(titles[position]);

        if(position==7){

            holder.icon.setVisibility(View.GONE);
        } else{

            holder.icon.setImageResource(images[position]);
        }
        return convertView;
    }

}
