package com.tinder.conestoga.kevin.viewAdapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tinder.conestoga.kevin.R;
import com.tinder.conestoga.kevin.models.Post;

import java.util.ArrayList;

/**
 * Created by FM on 6/27/2016.
 */
public class ViewAdapter extends PagerAdapter {

    private Activity _activity;
    private ArrayList<Post> _post;
    private LayoutInflater inflater;

    // constructor
    public ViewAdapter(Activity activity,
                             ArrayList<Post> post) {
        this._activity = activity;
        this._post = post;
    }

    @Override
    public int getCount() {
        //return this._post.size();
        return this._post.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imgDisplay;
        TextView organizer ;
        TextView title ;

        inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_swipe, container,
                false);

        imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
        organizer = (TextView) viewLayout.findViewById(R.id.organizer);
        title = (TextView) viewLayout.findViewById(R.id.title);
        Picasso.with(_activity).load(_post.get(position).url).into(imgDisplay);
        organizer.setText(_post.get(position).author);
        title.setText(_post.get(position).title);

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
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}
