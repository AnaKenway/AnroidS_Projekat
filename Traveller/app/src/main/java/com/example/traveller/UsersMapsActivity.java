package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
    private ArrayList<Marker> placesMarkers;
    private MenuItem switchShowUsers;
    private boolean showUsers=false;
    private Location currLoc;
    private String selectedItem;
    private boolean showMonuments=true, showRestaurants=true, showDoctors=true, showTravelAgencies=true, showCoffeeShop=true;
    ValueEventListener showPlacesListener;
    DataSnapshot ds;

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
        placesMarkers=new ArrayList<Marker>();

        final String[] listItems = new String[]{"Monument", "Coffee Shop", "Doctor", "Restaurant","Travel Agency"};
        final List<String> selectedItems = Arrays.asList(listItems);
        final boolean[] checkedItems = new boolean[listItems.length];
        selectedItem = listItems[0];

        Button btnAddPlace=findViewById(R.id.buttonAddPlace);
        btnAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UsersMapsActivity.this);

                // set the title for the alert dialog
                builder.setTitle("Choose a place to add:");

                builder.setSingleChoiceItems(listItems, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), listItems[which], Toast.LENGTH_LONG).show();
                        selectedItem=listItems[which];
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i=new Intent(UsersMapsActivity.this,AddPlaceActivity.class);
                        i.putExtra("userName",userName);
                        i.putExtra("type",selectedItem);
                        i.putExtra("latitude",currLoc.getLatitude());
                        i.putExtra("longitude",currLoc.getLongitude());
                        startActivity(i);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder.create();
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
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
                            userMarker.setTag("user");
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

        showPlacesListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ds=snapshot;
                HashMap<String,HashMap<String,HashMap<String,String>>> hm = (HashMap<String,HashMap<String,HashMap<String,String>>>) snapshot.getValue();

                if(!placesMarkers.isEmpty())
                    for(int i = 0; i < placesMarkers.size(); i++)
                        placesMarkers.get(i).remove();

                if(showMonuments){
                    HashMap<String,HashMap<String,String>> hashmonuments = hm.get("monuments");

                    if(hashmonuments!=null)
                        for(String key: hashmonuments.keySet()){
                            String lat = hashmonuments.get(key).get("latitude");
                            String lon = hashmonuments.get(key).get("longitude");
                            Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon))).icon(BitmapDescriptorFactory.fromResource(R.drawable.outline_museum_black_48dp)).title(key));
                            m.setTag("monument");
                            placesMarkers.add(m);
                        }
                }

                if(showCoffeeShop){
                    HashMap<String,HashMap<String,String>> hashCoffee = hm.get("coffee_shops");

                    if(hashCoffee!=null)
                        for(String key: hashCoffee.keySet()){
                            String lat = hashCoffee.get(key).get("latitude");
                            String lon = hashCoffee.get(key).get("longitude");
                            Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon))).icon(BitmapDescriptorFactory.fromResource(R.drawable.outline_local_cafe_black_48dp)).title(key));
                            m.setTag("coffeeShop");
                            placesMarkers.add(m);
                        }
                }

                if(showDoctors){
                    HashMap<String,HashMap<String,String>> hashDoctors = hm.get("doctors");

                    if(hashDoctors!=null)
                        for(String key: hashDoctors.keySet()){
                            String lat = hashDoctors.get(key).get("latitude");
                            String lon = hashDoctors.get(key).get("longitude");
                            Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon))).icon(BitmapDescriptorFactory.fromResource(R.drawable.outline_medical_services_black_48dp)).title(key));
                            m.setTag("doctor");
                            placesMarkers.add(m);
                        }
                }

                if(showRestaurants) {
                    HashMap<String, HashMap<String, String>> hashRestaurants = hm.get("restaurants");

                    if(hashRestaurants!=null)
                        for (String key : hashRestaurants.keySet()) {
                            String lat = hashRestaurants.get(key).get("latitude");
                            String lon = hashRestaurants.get(key).get("longitude");
                            Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon))).icon(BitmapDescriptorFactory.fromResource(R.drawable.outline_restaurant_black_48dp)).title(key));
                            m.setTag("restaurant");
                            placesMarkers.add(m);
                        }
                }

                if(showTravelAgencies){
                    HashMap<String, HashMap<String, String>> hashTravelAgencies = hm.get("travel_agencies");

                    if(hashTravelAgencies!=null)
                        for (String key : hashTravelAgencies.keySet()) {
                            String lat = hashTravelAgencies.get(key).get("latitude");
                            String lon = hashTravelAgencies.get(key).get("longitude");
                            Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon))).icon(BitmapDescriptorFactory.fromResource(R.drawable.outline_travel_explore_black_48dp)).title(key));
                            m.setTag("travelAgency");
                            placesMarkers.add(m);
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

       myRef.child("places").addValueEventListener(showPlacesListener);
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

        currLoc=location;
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
                    userMarker.setTag("user");

                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(@NonNull Marker marker) {

                            if(marker.getTag().equals("monument")) {

                                Intent i = new Intent(UsersMapsActivity.this, ViewPlaceActivity.class);
                                i.putExtra("userName", userName);
                                i.putExtra("tag","monument");
                                i.putExtra("placeName",marker.getTitle());
                                startActivity(i);

                            }else if(marker.getTag().equals("coffeeShop")){
                                Intent i = new Intent(UsersMapsActivity.this, ViewPlaceActivity.class);
                                i.putExtra("userName", userName);
                                i.putExtra("tag","coffeeShop");
                                i.putExtra("placeName",marker.getTitle());
                                startActivity(i);

                            }else if(marker.getTag().equals("restaurant")){
                                Intent i = new Intent(UsersMapsActivity.this, ViewPlaceActivity.class);
                                i.putExtra("userName", userName);
                                i.putExtra("tag","restaurant");
                                i.putExtra("placeName",marker.getTitle());
                                startActivity(i);
                            }
                            else if(marker.getTag().equals("doctor")){
                                Intent i = new Intent(UsersMapsActivity.this, ViewPlaceActivity.class);
                                i.putExtra("userName", userName);
                                i.putExtra("tag","doctor");
                                i.putExtra("placeName",marker.getTitle());
                                startActivity(i);
                            }
                            else if(marker.getTag().equals("travelAgency")){
                                Intent i = new Intent(UsersMapsActivity.this, ViewPlaceActivity.class);
                                i.putExtra("userName", userName);
                                i.putExtra("tag","travelAgency");
                                i.putExtra("placeName",marker.getTitle());
                                startActivity(i);
                            }
                            else if(marker.getTitle().toString().equals(userName))
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
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoomLevel));

                    myRef.child("users").child(userName).child("latitude").setValue(Double.toString(location.getLatitude()));
                    myRef.child("users").child(userName).child("longitude").setValue(Double.toString(location.getLongitude()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("MYAPP",exception.getLocalizedMessage());

                    userMarker = mMap.addMarker(new MarkerOptions().position(currentLoc).title(userName));
                    userMarker.setTag("user");
                    float zoomLevel = 16.0f; //This goes up to 21
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoomLevel));

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
                    if(!showUsers) return;
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
                                    m.setTag("friend");
                                    friendMarkers.add(m);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Log.e("MYAPP", exception.getLocalizedMessage());
                                    mMap.addMarker(new MarkerOptions().position(friendLoc).title(friendUserName)).setTag("friend");
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
                            m2.setTag("notFriend");
                            friendMarkers.add(m2);
                        }
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_toolbar_users_maps, menu);
        View view = (View) menu.findItem(R.id.item_show_users_switch).getActionView();

        Switch sw=(Switch)view.findViewById(R.id.switch_show_users);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showUsers=!showUsers;
                onLocationChanged(currLoc);
            }
        });

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
            case R.id.itemFilterMap:

                final String[] listItems = new String[]{"Monuments", "Coffee Shops", "Restaurants", "Doctors","Travel Agencies"};
                final boolean[] checkedItems = new boolean[listItems.length];

                AlertDialog.Builder builder = new AlertDialog.Builder(UsersMapsActivity.this);

                // set the title for the alert dialog
                builder.setTitle("Choose which places to show:");

                builder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                });

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < checkedItems.length; i++) {
                            if (checkedItems[i]) {
                               if(i==0)
                                   showMonuments=true;
                               else if(i==1)
                                   showCoffeeShop=true;
                               else if(i==2)
                                   showRestaurants=true;
                               else if(i==3)
                                   showDoctors=true;
                               else showTravelAgencies=true;
                            }
                            else{
                                if(i==0)
                                    showMonuments=false;
                                else if(i==1)
                                    showCoffeeShop=false;
                                else if(i==2)
                                    showRestaurants=false;
                                else if(i==3)
                                    showDoctors=false;
                                else showTravelAgencies=false;
                            }
                        }
                        showPlacesListener.onDataChange(ds);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder.create();
                AlertDialog alert = builder.create();
                alert.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}