package com.example.knowyourcity;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FriendsListAdapter extends ArrayAdapter<Prijatelj> {

    ArrayList<Prijatelj> list; // your person arraylist
    Context context; // the activity context
    int resource; // this will be your xml file

    public FriendsListAdapter(Context context, int resource,ArrayList<Prijatelj> objects) {
        super(context, resource, objects);
        // TODO Auto-generated constructor stub

        Collections.sort(objects, Prijatelj::compareTo);
        this.list = objects;
        this.context = context;
        this.resource = resource;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if(list.size() == 0){
            return 0;
        }else{
            return list.size();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View child = convertView;
        RecordHolder holder;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater(); // inflating your xml layout

        if (child == null) {
            child = inflater.inflate(resource, parent, false);
            holder = new RecordHolder();
            holder.username = (TextView) child.findViewById(R.id.FLAusername); // fname is the reference to a textview
            holder.points = (TextView) child.findViewById(R.id.FLApoints); // in your xml layout file
            holder.image = (ImageView) child.findViewById(R.id.FLAprofilePic);

            child.setTag(holder);
        }
        else
        {
            holder = (RecordHolder) child.getTag();
        }

        final Prijatelj user = list.get(position); // you can remove the final modifieer.

        holder.username.setText(user.getUsername());
        holder.points.setText(String.valueOf(user.getPoeni()));
        if(user.getProfiSlikaBit()!=null)
            holder.image.setImageBitmap(user.getProfiSlikaBit());

        else
        {
            String link = user.getProfilePicID();
            Picasso.get().load(link).fit().into(holder.image, new Callback()
            {
                @Override
                public void onSuccess()
                {
                    Drawable drawable = holder.image.getDrawable();
                    if (drawable != null && drawable instanceof BitmapDrawable)
                    {
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        user.setProfiSlikaBit(bitmap);
                        int i = MojiPodaci.getInstance().getFriendIndex(user.getUsername());
                        if(i>=0)
                            MojiPodaci.getInstance().getMyFriends().set(i,user);
                    }
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }



       // holder.image.setImageBitmap(user.getImage()); // if you use string then you download the image using
        // the string as url and set it to your imageview..s
        return child;
    }

    static class RecordHolder {
        TextView username,points;
        ImageView image;
    }


    @Override
    public void notifyDataSetChanged() { // you can remove this..
        // TODO Auto-generated method stub
        if(getCount() == 0){
            //show layout or something that notifies that no list is in..
        }else{
            // this is to make sure that you can call notifyDataSetChanged in any place and any thread
            new Handler(getContext().getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    FriendsListAdapter.super.notifyDataSetChanged();
                }
            });
        }
    }

}