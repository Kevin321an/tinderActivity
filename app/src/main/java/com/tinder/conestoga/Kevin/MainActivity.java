package com.tinder.conestoga.kevin;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.daprlabs.cardstack.SwipeDeck;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tinder.conestoga.kevin.models.Post;
import com.tinder.conestoga.kevin.viewAdapter.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    //request ID
    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_STORAGE_PERMS = 102;
    private final String TAG = this.getClass().getSimpleName();
    private FirebaseAuth mAuth;
    // declare DB ref
    private DatabaseReference mDatabase;
    // declare storage ref
    private StorageReference mStorageRef;

    private ProgressDialog mProgressDialog;
    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private LocationManager myLocationManager;
    public static double la = 0.0;
    public static double lng = 0.0;

    Post currentPost;
    //keep the data from cloud
    ArrayList<Post> _post;
    Activity _activity = this;

    //ViewAdapter adapter;
    ViewPager viewPager;


    private SwipeDeck cardStack;

    private SwipeDeckAdapter adapter;
    private ArrayList<String> testData;

    //Uri of Could https://firebasestorage.googleapis.com/v0/b/cloudmessaging-e6225.appspot.com/o/photos%2Fc68ef52f-9149-4d93-9b96-46310fa143bb.jpg?alt=media&token=0dd28019-29a5-4718-8d37-39c4660030ea
    private Uri mDownloadUrl = null;
    //Uri for the local file:///storage/emulated/0/c68ef52f-9149-4d93-9b96-46310fa143bb.jpg
    private Uri mFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      /*  Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        // Initialize Firebase Storage Ref
        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END get_storage_ref]

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //for now sing in anonymously otherwise the photo cannot be updated
        signInAnonymously();
        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }

        //pass the data to the ViewAdapter
//        viewPager = (ViewPager) findViewById(R.id.view_pager);
//        getData();

        //setContentView(R.layout.activity_swipe_deck);
        cardStack = (SwipeDeck) findViewById(R.id.swipe_deck);
        cardStack.setHardwareAccelerationEnabled(true);

        /*testData = new ArrayList<>();
        testData.add("0");
        testData.add("1");
        testData.add("2");
        testData.add("3");
        testData.add("4");

        adapter = new SwipeDeckAdapter(testData, this);
        cardStack.setAdapter(adapter);*/

        getData();


        cardStack.setEventCallback(new SwipeDeck.SwipeEventCallback() {
            @Override
            public void cardSwipedLeft(int position) {
                Log.i("MainActivity", "card was swiped left, position in adapter: " + position);
            }

            @Override
            public void cardSwipedRight(int position) {
                Log.i("MainActivity", "card was swiped right, position in adapter: " + position);
            }

            @Override
            public void cardsDepleted() {
                Log.i("MainActivity", "no more cards");
            }

            @Override
            public void cardActionDown() {
                Log.i(TAG, "cardActionDown");
            }

            @Override
            public void cardActionUp() {
                Log.i(TAG, "cardActionUp");
            }

        });
        cardStack.setLeftImage(R.id.left_image);
        cardStack.setRightImage(R.id.right_image);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        FloatingActionButton distance = (FloatingActionButton) findViewById(R.id.distance);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                distanceDialog();
            }
        });


        String perm[] = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.INTERNET};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !EasyPermissions.hasPermissions(this, perm)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage),
                    RC_STORAGE_PERMS, perm);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }


    // upload the file to the cloud
    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");

                        // Get the public download URL
                        mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();

                        writeNewPost(currentPost.author, currentPost.title, currentPost.body, mDownloadUrl.toString(), la, lng);

                        // [START_EXCLUDE]
                        hideProgressDialog();

                        // updateUI(mAuth.getCurrentUser());
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        mDownloadUrl = null;

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        Toast.makeText(MainActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();
                        //updateUI(mAuth.getCurrentUser());
                        // [END_EXCLUDE]
                    }
                });
    }

    private void writeNewPost(String username, String title, String body, String url, double lat, double lng) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        latlngToGeo(lat, lng);
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post("1", username, title, body, url, lat, lng);
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        //childUpdates.put("/user-posts/" + userId + "/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
    }

    //display the progressing dialog
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    //close the progressing dialog
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    @AfterPermissionGranted(RC_STORAGE_PERMS)
    public void launchCamera() {
        Log.d(TAG, "launchCamera");
        // Check that we have permission to read images from external storage.
        String perm = android.Manifest.permission.READ_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !EasyPermissions.hasPermissions(this, perm)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage),
                    RC_STORAGE_PERMS, perm);
            return;
        }

        // Create intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Choose file storage location
        File file = new File(Environment.getExternalStorageDirectory(), UUID.randomUUID().toString() + ".jpg");
        mFileUri = Uri.fromFile(file);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);

        // Launch intent
        startActivityForResult(takePictureIntent, RC_TAKE_PICTURE);
    }


    //deal with the result backing from camera implicit intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                if (mFileUri != null) {
                    uploadFromUri(mFileUri);

                } else {
                    Log.w(TAG, "File URI is null");
                }
            } else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Dialog
    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }

    //sign in anonymously
    private void signInAnonymously() {
        // Sign in anonymously. Authentication is required to read or write from Firebase Storage.
        showProgressDialog();
        mAuth.signInAnonymously()
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "signInAnonymously:SUCCESS");
                        hideProgressDialog();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "signInAnonymously:FAILURE", exception);
                        hideProgressDialog();
                    }
                });
    }

    //real time data listener
    public void getData() {
        showProgressDialog();
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                if (adapter == null)
                    adapter = new SwipeDeckAdapter(getData(dataSnapshot, previousChildName), _activity);
                cardStack.setAdapter(adapter);

                //adapter = new ViewAdapter(_activity, getData(dataSnapshot, previousChildName));

                //viewPager.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                hideProgressDialog();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                showProgressDialog();
                //dapter.swapData(getData(dataSnapshot, previousChildName));
                adapter.notifyDataSetChanged();
                hideProgressDialog();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(_activity, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        mDatabase.addChildEventListener(childEventListener);
    }

    //get the updated data from cloud
    private ArrayList<Post> getData(DataSnapshot dataSnapshot, String previousChildName) {
        ArrayList<Post> t = new ArrayList<>();
        if (previousChildName == null) {
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                Post post = postSnapshot.getValue(Post.class);
                double lat = post.lat;
                double lng = post.lng;
                Location l = getLastKnownLocation();
                double distance = distFrom(lat, lng, l.getLatitude(), l.getLongitude());
                Log.v("distance b/w two points", Double.toString(distance));
                System.out.println(post.url + " - " + post.author);
                //only add the result within the search radius
                if (distance < getDistance(_activity))
                    t.add(post);
            }
        }
        return t;
    }

    //add activity dialog
    public void dialog() {
        // custom dialog
        final Dialog dialog = new Dialog(_activity);
        dialog.setContentView(R.layout.layout_dialog);
        dialog.setTitle("Add new activity");

        // set the custom dialog components - text, image and button
        final EditText title = (EditText) dialog.findViewById(R.id.title);
        final EditText desc = (EditText) dialog.findViewById(R.id.des);
        final EditText address = (EditText) dialog.findViewById(R.id.address);
        final EditText organizer = (EditText) dialog.findViewById(R.id.author_add);

        Button add = (Button) dialog.findViewById(R.id.add_activity);
        // if button is clicked, close the custom dialog
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "execute", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                currentPost = new Post();
                currentPost.title = title.getText().toString();
                currentPost.body = desc.getText().toString();
                currentPost.author = organizer.getText().toString();
                getposition();
                launchCamera();
                dialog.dismiss();
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);

    }

    public void distanceDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(_activity);
        dialog.setContentView(R.layout.layout_distance);
        dialog.setTitle("Add new activity");

        // set the custom dialog components - text, image and button
        final EditText distance = (EditText) dialog.findViewById(R.id.distance);

        Button add = (Button) dialog.findViewById(R.id.set);
        // if button is clicked, close the custom dialog
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "execute", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                distanceSet(_activity, Integer.parseInt(distance.getText().toString()));
                dialog.dismiss();
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);

    }

    final String DISTANCE = "distance";

    public void distanceSet(Activity ac, int s) {

        SharedPreferences preferences = ac.getPreferences(ac.MODE_PRIVATE);
        int mFirst = preferences.getInt(DISTANCE, s);

    }

    public int getDistance(Activity ac) {
        SharedPreferences preferences = ac.getPreferences(ac.MODE_PRIVATE);
        int mFirst = preferences.getInt(DISTANCE, Integer.MAX_VALUE);
        return mFirst;
    }


    //distance between two lat and lng
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        float dist = (float) (earthRadius * c);
        return dist;
    }

    //call getLastKnownLocation() passing values to la and lng
    public void getposition() {
        Location location = getLastKnownLocation();
        la = location.getLatitude();
        lng = location.getLongitude();
    }

    // get the current location in Location Class
    private Location getLastKnownLocation() {
        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = myLocationManager.getProviders(true);
        Location bestLocation = null;
        String perm[] = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.INTERNET};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !EasyPermissions.hasPermissions(this, perm)) {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage),
                    RC_STORAGE_PERMS, perm);
        }
        for (String provider : providers) {
            Location l = myLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }

        }
        return bestLocation;
    }


    //transfer the lat and lng to geo address
    public void latlngToGeo(double lat, double lng) {
        Geocoder geocoder;
        List<Address> yourAddresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        String yourAddress, yourCity, yourCountry;

        try {
            yourAddresses = geocoder.getFromLocation(lat, lng, 1);

            if (yourAddresses.size() > 0) {
                yourAddress = yourAddresses.get(0).getAddressLine(0);
                yourCity = yourAddresses.get(0).getAddressLine(1);
                yourCountry = yourAddresses.get(0).getAddressLine(2);
                Log.v("transform address", yourAddress + yourCity + yourCountry);
            }
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

    }

    /*Request the permission via easy permission */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
