package com.tinder.conestoga.kevin.viewAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.util.List;

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
        String title = data.get(position).title;
        String bodyStr = data.get(position).body;
        String authorStr = data.get(position).author;

        String item = data.get(position).title;
        textView.setText(title);
        body.setText(bodyStr);
        author.setText(authorStr);
        city.setText(item);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Layer type: ", Integer.toString(v.getLayerType()));
                Log.i("Hwardware Accel type:", Integer.toString(View.LAYER_TYPE_HARDWARE));
                Intent i = new Intent(v.getContext(), BlankActivity.class);
                v.getContext().startActivity(i);
            }
        });
        return v;
    }

}
