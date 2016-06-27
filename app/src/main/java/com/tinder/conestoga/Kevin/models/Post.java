package com.tinder.conestoga.kevin.models;

import java.util.HashMap;
import java.util.Map;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
/**
 * Created by FM on 6/27/2016.
 */
@IgnoreExtraProperties
public class Post {
        public String uid;
        public String author;
        public String title;
        public String body;
        public String url;
        public int starCount = 0;
        public Map<String, Boolean> stars = new HashMap<>();

        public Post() {
            // Default constructor required for calls to DataSnapshot.getValue(Post.class)
        }

        public Post(String uid, String author, String title, String body, String url) {
            this.uid = uid;
            this.author = author;
            this.title = title;
            this.body = body;
            this.url = url;

        }

        // [START post_to_map]
        @Exclude
        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("uid", uid);
            result.put("author", author);
            result.put("title", title);
            result.put("body", body);
            result.put("url", url);
            result.put("starCount", starCount);
            result.put("stars", stars);
            return result;
        }
        // [END post_to_map]
}
