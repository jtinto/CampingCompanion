package com.concepttech.campingcompanionbluetooth;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
        return TimeStamps.size();
    }

    @Override
    public Object getItem(int pos) {
        return TimeStamps.get(pos);
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
        }

        //Handle TextView and display string from your list
        if (!TimeStamps.isEmpty() && view != null) {
            ImageView listItemImage =view.findViewById(R.id.FeedListItemImageView);
            TextView listItemText = view.findViewById(R.id.FeedListItemText);
            if(position < Pictures.size() && Pictures.get(position) != null) {
                listItemImage.setImageURI(Uri.fromFile(Pictures.get(position)));
                String temp = "Uploaded on: " + TimeStamps.get(position).get_time_string();
                listItemText.setText(temp);
            }
            else {
                if (position < Pictures.size() && Pictures.get(position) == null) {
                    listItemImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cached_white_48dp));
                    listItemImage.setBackgroundColor(context.getResources().getColor(R.color.SelectedButtonColor));
                    String temp = "Uploaded on: " + TimeStamps.get(position).get_time_string();
                    listItemText.setText(temp);
                }else {
                    if(Pictures.size() == 0) {
                        listItemImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cached_white_48dp));
                        listItemImage.setBackgroundColor(context.getResources().getColor(R.color.SelectedButtonColor));
                        String no_text = "Be the First to Upload Pictures";
                        listItemText.setText(no_text);
                    }else{
                        while(Pictures.size() < TimeStamps.size())TimeStamps.remove(TimeStamps.size() - 1);
                        notifyDataSetChanged();
                    }
                }
            }
        }
        return view;
    }
    public void SetPictureFile(File file, int index){
        while(index >= Pictures.size())Pictures.add(null);
        Pictures.set(index,file);
    }
    public void SetTimeStamps(ArrayList<TimeStamp> arg){ this.TimeStamps = arg;}
}
