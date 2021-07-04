package com.example.traveller;

import android.app.Activity;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] userNames;
    private final String[] points;
    private final Integer[] icons;

    public MyListAdapter(Activity context, String[] userNames, String[] points, Integer[] icons) {
        super(context, R.layout.mylist, userNames);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.userNames = userNames;
        this.points = points;
        this.icons = icons;

    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.mylist, null,true);

        TextView titleText = (TextView) rowView.findViewById(R.id.title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView subtitleText = (TextView) rowView.findViewById(R.id.subtitle);

        titleText.setText(userNames[position]);
        imageView.setImageResource(icons[position]);
        //imageView.setImageBitmap(profilePics[position]);
        subtitleText.setText("Points: "+ points[position]);

        if(position==0){
            imageView.setColorFilter(getContext().getResources().getColor(R.color.goldenWind));
        }
        else if(position==1){
            imageView.setColorFilter(getContext().getResources().getColor(R.color.silverChariot));
        }

        else if(position==2){
            imageView.setColorFilter(getContext().getResources().getColor(R.color.bronze));
        }
        else {
            imageView.setColorFilter(getContext().getResources().getColor(R.color.roseGold));
        }
        return rowView;

    };
}
