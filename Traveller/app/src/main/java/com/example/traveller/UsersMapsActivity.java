package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.traveller.models.User;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.HashMap;

public class UsersMapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private String userName;
    private Marker userMarker;
    private LocationManager locationManager;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private String URI;
    private FirebaseAuth mAuth;
    private ArrayList<Marker> friendMarkers;
    private ArrayList<Marker> monumentMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        Intent i = getIntent();
        userName = i.getStringExtra("Username");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_users_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        friendMarkers=new ArrayList<Marker>();
        monumentMarkers=new ArrayList<Marker>();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            finish();
        }
    }

    static final int PERMISSION_ACCESS_FINE_LOCATION = 1;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            mMap.setMyLocationEnabled(true);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
                            userMarker = mMap.addMarker(new MarkerOptions().position(currentLoc).title(userName));
                            float zoomLevel = 16.0f; //This goes up to 21
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoomLevel));

                            myRef.child("users").child(userName).child("latitude").setValue(Double.toString(location.getLatitude()));
                            myRef.child("users").child(userName).child("longitude").setValue(Double.toString(location.getLongitude()));

                            myRef.child("users").child(userName).child("imgUrl").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (!task.getResult().exists()) {
                                        Toast.makeText(getApplicationContext(), "Couldn't find image URI", Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        URI = (String) task.getResult().getValue();
                                        storageRef = storage.getReference(URI);
                                    }
                                    try {
                                        if (ActivityCompat.checkSelfPermission(UsersMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UsersMapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            return;
                                        }
                                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, UsersMapsActivity.this);
                                    }
                                    catch(Exception e)
                                    {
                                        Log.e("MYAPP",e.getLocalizedMessage());
                                    }
                                }
                            });
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                return;
            }
        }
    }

    Bitmap bmp;
    Canvas canvas;
    String friendUserName;
    String notFriend;
    @Override
    public void onLocationChanged(@NonNull Location location) {

        if (location != null && userMarker!=null) {

            userMarker.remove();
            //mMap.clear(); // izbrisati stare markere
            if(!friendMarkers.isEmpty())
            {
                for(int i=0;i<friendMarkers.size();i++){
                    friendMarkers.get(i).remove();
                }
            }
            LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            final long ONE_MEGABYTE = 1024 * 1024;

            storageRef = storage.getReference(URI);
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(mutableBitmap, 150, 150, false);
                    canvas = new Canvas(mutableBitmap);

                    Paint color = new Paint();
                    color.setTextSize(35);
                    color.setColor(Color.BLACK);

                    canvas.drawBitmap(smallMarker, 0,0, color);
                    canvas.drawText(userName, 30, 40, color);
                    userMarker = mMap.addMarker(new MarkerOptions().position(currentLoc).title(userName).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).anchor(0.5f,1));

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(@NonNull Marker marker) {
                            if(marker.getTitle().toString().equals(userName))
                            {
                                Intent i = new Intent(UsersMapsActivity.this, UserProfileActivity.class);
                                i.putExtra("userName", userName);
                                startActivity(i);
                            }
                            else
                            {
                                Intent i = new Intent(UsersMapsActivity.this, FriendProfileActivity.class);
                                i.putExtra("friendUserName", marker.getTitle());
                                i.putExtra("userName", userName);
                                startActivity(i);
                            }
                            return false;
                        }
                    });
                    float zoomLevel = 16.0f; //This goes up to 21
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoomLevel));

                    myRef.child("users").child(userName).child("latitude").setValue(Double.toString(location.getLatitude()));
                    myRef.child("users").child(userName).child("longitude").setValue(Double.toString(location.getLongitude()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("MYAPP",exception.getLocalizedMessage());
                    userMarker = mMap.addMarker(new MarkerOptions().position(currentLoc).title(userName));
                    float zoomLevel = 16.0f; //This goes up to 21
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoomLevel));

                    myRef.child("users").child(userName).child("latitude").setValue(Double.toString(location.getLatitude()));
                    myRef.child("users").child(userName).child("longitude").setValue(Double.toString(location.getLongitude()));
                }
            });
        }


        myRef.child("users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.getResult().exists()) {
                    Toast.makeText(getApplicationContext(),"Couldn't find users",Toast.LENGTH_LONG).show();
                }
                else {
                    Object u=task.getResult().getValue();
                    HashMap<String, HashMap<String, ArrayList<String>>> hm=(HashMap<String, HashMap<String,ArrayList<String>>>) u;
                    HashMap<String, HashMap<String, String>> hm2=(HashMap<String, HashMap<String, String>>) u;
                    ArrayList<String> friends = hm.get(userName).get("friends");
                    String imgURI;
                    String latitude, longitude;
                    if(friends!=null) {
                        for (int i = 0; i < friends.size(); i++) {
                            friendUserName = friends.get(i);
                            latitude = hm2.get(friendUserName).get("latitude");
                            longitude = hm2.get(friendUserName).get("longitude");
                            imgURI = hm2.get(friendUserName).get("imgUrl");
                            LatLng friendLoc = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                            hm2.remove(friends.get(i));

                            final long ONE_MEGABYTE = 1024 * 1024;

                            storageRef = storage.getReference(imgURI);
                            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                                    Bitmap smallMarker = Bitmap.createScaledBitmap(mutableBitmap, 150, 150, false);
                                    canvas = new Canvas(mutableBitmap);

                                    Paint color = new Paint();
                                    color.setTextSize(35);
                                    color.setColor(Color.BLACK);

                                    canvas.drawBitmap(smallMarker, 0, 0, color);
                                    canvas.drawText(friendUserName, 30, 40, color);
                                    Marker m=mMap.addMarker(new MarkerOptions().position(friendLoc).title(friendUserName).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).anchor(0.5f, 1));
                                    friendMarkers.add(m);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Log.e("MYAPP", exception.getLocalizedMessage());
                                    mMap.addMarker(new MarkerOptions().position(friendLoc).title(friendUserName));
                                }
                            });
                        }
                    }
                        hm2.remove(userName);
                        for (String key : hm2.keySet()) {

                            notFriend = key;
                            latitude = hm2.get(notFriend).get("latitude");
                            longitude = hm2.get(notFriend).get("longitude");
                            LatLng notFriendLoc = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                            Marker m2=mMap.addMarker(new MarkerOptions().position(notFriendLoc).title(notFriend));
                            friendMarkers.add(m2);
                        }
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_toolbar_users_maps, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mAuth.signOut();
                this.finish();
                return true;
            case R.id.itemProfileUserMaps:
                Intent i = new Intent(UsersMapsActivity.this, UserProfileActivity.class);
                i.putExtra("userName", userName);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}