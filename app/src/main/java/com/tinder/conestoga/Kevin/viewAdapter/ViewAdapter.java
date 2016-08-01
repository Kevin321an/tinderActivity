package com.tinder.conestoga.kevin.viewAdapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.tinder.conestoga.kevin.R;
import com.tinder.conestoga.kevin.models.Post;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FM on 6/27/2016.
 */
public class ViewAdapter extends PagerAdapter {
    final String TAG = this.getClass().getSimpleName();

    private Activity _activity;
    static private ArrayList<Post> _post;
    private LayoutInflater inflater;

    // constructor
    public ViewAdapter(Activity activity,
                       ArrayList<Post> post) {
        this._activity = activity;
        this._post = post;
    }

    public void swapData(ArrayList<Post> data){
        _post=data;
    }

    @Override
    public void notifyDataSetChanged() {
        Log.v("Notification", Integer.toString(_post.size()));
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        //return this._post.size();
        Log.v("count", Integer.toString(_post.size()));
        return this._post.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

   /* @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }*/

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imgDisplay;
        TextView organizer;
        TextView title,body;

        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_swipe, container,
                false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
        organizer = (TextView) viewLayout.findViewById(R.id.organizer);
        title = (TextView) viewLayout.findViewById(R.id.title);
        body = (TextView) viewLayout.findViewById(R.id.body);


        Log.v("adapter", _post.get(position).url + _post.get(position).author + _post.get(position).title);
        Picasso.with(_activity).load(_post.get(position).url).resize(200, 200)
                .centerCrop().into(imgDisplay);
        organizer.setText(_post.get(position).author);
        title.setText(_post.get(position).title);
        body.setText(_post.get(position).body);


        // close button click event
      /*  btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _activity.finish();
            }
        });*/
        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((LinearLayout) object);
    }
}
