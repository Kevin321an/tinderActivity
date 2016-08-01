package com.tinder.conestoga.kevin.viewAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ShareCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tinder.conestoga.kevin.R;
import com.tinder.conestoga.kevin.activities.BlankActivity;
import com.tinder.conestoga.kevin.models.Post;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by FM on 7/31/2016.
 */
public class SwipeDeckAdapter extends BaseAdapter {

    private List<Post> data;
    private Activity context;

    public SwipeDeckAdapter(List<Post> data, Activity context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater inflater =  context.getLayoutInflater();
            // normally use a viewholder
            v = inflater.inflate(R.layout.test_card2, parent, false);
        }
        ImageView imageView = (ImageView) v.findViewById(R.id.offer_image);
        Picasso.with(context).load(data.get(position).url).fit().centerCrop().into(imageView);
        TextView textView = (TextView) v.findViewById(R.id.title);
        TextView body = (TextView) v.findViewById(R.id.body);
        TextView author = (TextView) v.findViewById(R.id.author);
        TextView city = (TextView) v.findViewById(R.id.city);
        final String title = data.get(position).title;
        String bodyStr = data.get(position).body;
        String authorStr = data.get(position).author;
       final String cityStr = latlngToGeo(data.get(position).lat, data.get(position).lng) ;


        textView.setText(title);
        body.setText(bodyStr);
        author.setText(authorStr);
        city.setText(cityStr);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                shareInfo("Join the activity  " +  title + "at " + cityStr);
            }
        });
        return v;
    }

    //convert the lat lng to geo locaton
    public String latlngToGeo(double lat, double lng) {
        Geocoder geocoder;
        List<Address> yourAddresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        String yourAddress, yourCity, yourCountry;
        try {
            yourAddresses = geocoder.getFromLocation(lat, lng, 1);
            if (yourAddresses.size() > 0) {
                yourAddress = yourAddresses.get(0).getAddressLine(0);
                yourCity = yourAddresses.get(0).getAddressLine(1);
                yourCountry = yourAddresses.get(0).getAddressLine(2);
                Log.v("transform address", yourAddress + yourCity + yourCountry);
                return  yourAddresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

        return "no address";



    }


    //share the activity
    public void shareInfo(String str){
        context.startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(context)
                .setType("text/plain")
                .setText(str)
                .getIntent(), context.getString(R.string.action_share)));
    }

}
