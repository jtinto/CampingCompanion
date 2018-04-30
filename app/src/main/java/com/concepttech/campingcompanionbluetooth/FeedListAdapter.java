package com.concepttech.campingcompanionbluetooth;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class FeedListAdapter extends BaseAdapter implements ListAdapter {
    ArrayList<TimeStamp> TimeStamps = new ArrayList<>();
    ArrayList<File> Pictures = new ArrayList<>();
    Context context;
    public FeedListAdapter(Context context){this.context = context;}

    @Override
    public int getCount() {
        return Pictures.size();
    }

    @Override
    public Object getItem(int pos) {
        return Pictures.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(inflater != null) view = inflater.inflate(R.layout.feed_list_item, null);
            else return null;
        }

        //Handle TextView and display string from your list
        if (!Pictures.isEmpty() && view != null) {
            ImageView listItemImage =view.findViewById(R.id.FeedListItemImageView);
            TextView listItemText = view.findViewById(R.id.FeedListItemText);
            listItemImage.setImageURI(Uri.fromFile(Pictures.get(position)));
            listItemText.setText(TimeStamps.get(position).get_time_string());
        }else if(view != null){
            TextView listItemText = view.findViewById(R.id.FeedListItemText);
            String no_text = "Be the First to Upload Pictures";
            listItemText.setText(no_text);
        }
        return view;
    }

}
