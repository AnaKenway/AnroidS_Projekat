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
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SearchView;
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
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.SphericalUtil;



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
    private ArrayList<Marker> userMarkers;
    private ArrayList<Marker> placesMarkers;
    private MenuItem switchShowUsers;
    private boolean showUsers=false;
    private Location currLoc;
    private String selectedItem;
    private String selectedItemRadius="Unlimited";
    private boolean showMonuments=true, showRestaurants=true, showDoctors=true, showTravelAgencies=true, showCoffeeShop=true;
    ValueEventListener showPlacesListener;
    private double selectedRaduius;
    private DataSnapshot ds;
    private boolean startService = false;
    private boolean isAdmin=false;
    private Button buttonOpenCamera;
    private Button buttonTreasureHunt;
    private Session mSession;
    private boolean mUserRequestedInstall = true;


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
        userMarkers = new ArrayList<Marker>();
        SearchView search = findViewById(R.id.searchItem);

        //isAdmin Test

        myRef.child("users").child(userName).child("isAdmin").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                Object o=dataSnapshot.getValue();
                isAdmin=(boolean)o;
                if(isAdmin){
                    Toast.makeText(getApplicationContext(), "You are an Admin", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "You are not an Admin", Toast.LENGTH_LONG).show();
                }
            }
        });

        //end isAdmin Test

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                HashMap<String,HashMap<String,HashMap<String,String>>> hm = (HashMap<String,HashMap<String,HashMap<String,String>>>) ds.getValue();
                for(String key : hm.keySet())
                {
                    for(String key2 : hm.get(key).keySet()){
                        if(key2.equals(s))
                        {
                            LatLng latLng = new LatLng(Double.parseDouble(hm.get(key).get(key2).get("latitude")), Double.parseDouble(hm.get(key).get(key2).get("longitude")));
                            if(!selectedItemRadius.equals("Unlimited"))
                            {
                                double distance = SphericalUtil.computeDistanceBetween(new LatLng(currLoc.getLatitude(),currLoc.getLongitude()), latLng);
                                if(selectedRaduius<distance) {
                                    Toast.makeText(getApplicationContext(), "This place is outside radius of " + selectedItemRadius + "!", Toast.LENGTH_LONG).show();
                                    return false;
                                }
                            }

                            float zoomLevel = 16.0f; //This goes up to 21
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                            return false;
                        }
                    }
                }
                Toast.makeText(getApplicationContext(), "No place with that name!", Toast.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

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
        myRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onLocationChanged(currLoc);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Button btnAddTreasureHunts=findViewById(R.id.buttonTreasureHunt);
        btnAddTreasureHunts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(UsersMapsActivity.this,TreasureHuntListActivity.class);
                i.putExtra("isAdmin",isAdmin);
                startActivity(i);
            }
        });

        //for AR supported apps
        buttonOpenCamera=findViewById(R.id.buttonOpenCamera);
        buttonOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(UsersMapsActivity.this,CloudAnchorActivity.class);
                i.putExtra("isAdmin",isAdmin);
                i.putExtra("isForAddOrEditTreasure",false);
                startActivity(i);
            }
        });
        buttonTreasureHunt=findViewById(R.id.buttonTreasureHunt);
        maybeEnableArButtons();
    }

    void maybeEnableArButtons() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Continue to query availability at 5Hz while compatibility is checked in the background.
            //ovde me pitao koji Handler da importujem, ako nesto ne valja, pogledaj ovde;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    maybeEnableArButtons();
                }
            }, 200);
        }
        if (availability.isSupported()) {
            buttonOpenCamera.setVisibility(View.VISIBLE);
            buttonOpenCamera.setEnabled(true);
            buttonTreasureHunt.setVisibility(View.VISIBLE);
            buttonTreasureHunt.setEnabled(true);
        } else { // The device is unsupported or unknown.
            buttonOpenCamera.setVisibility(View.INVISIBLE);
            buttonOpenCamera.setEnabled(false);
            buttonTreasureHunt.setVisibility(View.INVISIBLE);
            buttonTreasureHunt.setEnabled(false);
        }
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

    @Override
    protected void onResume() {
        super.onResume();

        // Ensure that Google Play Services for AR and ARCore device profile data are
        // installed and up to date.
        Exception exception = null;
        String message = null;
        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        // Success: Safe to create the AR session.
                        mSession = new Session(this);
                        break;
                    case INSTALL_REQUESTED:
                        // When this method returns `INSTALL_REQUESTED`:
                        // 1. ARCore pauses this activity.
                        // 2. ARCore prompts the user to install or update Google Play
                        //    Services for AR (market://details?id=com.google.ar.core).
                        // 3. ARCore downloads the latest device profile data.
                        // 4. ARCore resumes this activity. The next invocation of
                        //    requestInstall() will either return `INSTALLED` or throw an
                        //    exception if the installation or update did not succeed.
                        mUserRequestedInstall = false;
                        return;
                }
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            // Display an appropriate message to the user and return gracefully.
            Toast.makeText(this, "TODO: handle exception " + e, Toast.LENGTH_LONG)
                    .show();
            return;
        } catch (UnavailableApkTooOldException e) {
            message = "Please update ARCore";
            exception = e;
        } catch (UnavailableSdkTooOldException e) {
            message = "Please update this app";
            exception = e;
        } catch (UnavailableDeviceNotCompatibleException e) {
            message = "This device does not support AR";
            exception = e;
        } catch (Exception e) {
            message = "Failed to create AR session";
            exception = e;
        }

    }

    @Override
    protected void onDestroy() {

        if (mSession != null) {
            // Explicitly close ARCore Session to release native resources.
            // Review the API reference for important considerations before calling close() in apps with
            // more complicated lifecycle requirements:
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
            mSession.close();
            mSession = null;
        }

        super.onDestroy();
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
                            userMarkers.add( mMap.addMarker(new MarkerOptions().position(currentLoc).title(userName)) );
                            //userMarker.setTag("user");
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
                            if(!selectedItemRadius.equals("Unlimited"))
                            {

                                double distance = SphericalUtil.computeDistanceBetween(new LatLng(currLoc.getLatitude(),currLoc.getLongitude()),new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)));
                                if(selectedRaduius<distance)
                                    continue;

                            }
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
                            if(!selectedItemRadius.equals("Unlimited"))
                            {

                                double distance = SphericalUtil.computeDistanceBetween(new LatLng(currLoc.getLatitude(),currLoc.getLongitude()),new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)));
                                if(selectedRaduius<distance)
                                    continue;

                            }
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
                            if(!selectedItemRadius.equals("Unlimited"))
                            {

                                double distance = SphericalUtil.computeDistanceBetween(new LatLng(currLoc.getLatitude(),currLoc.getLongitude()),new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)));
                                if(selectedRaduius<distance)
                                    continue;

                            }
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
                            if(!selectedItemRadius.equals("Unlimited"))
                            {

                                double distance = SphericalUtil.computeDistanceBetween(new LatLng(currLoc.getLatitude(),currLoc.getLongitude()),new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)));
                                if(selectedRaduius<distance)
                                    continue;

                            }
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
                            if(!selectedItemRadius.equals("Unlimited"))
                            {

                                double distance = SphericalUtil.computeDistanceBetween(new LatLng(currLoc.getLatitude(),currLoc.getLongitude()),new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)));
                                if(selectedRaduius<distance)
                                    continue;

                            }
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
        if (location != null && userMarkers!=null) {

            if(!userMarkers.isEmpty())
            for(int i=0;i<userMarkers.size();i++){
                userMarkers.get(i).remove();
            }

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
                    Marker setTag = mMap.addMarker(new MarkerOptions().position(currentLoc).title(userName).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).anchor(0.5f,1));
                    setTag.setTag("user");
                    userMarkers.add(setTag);

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
                    Marker setTag = mMap.addMarker(new MarkerOptions().position(currentLoc).title(userName));
                    setTag.setTag("user");
                    userMarkers.add(setTag);
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
                            String localFriendUserName = new String (friends.get(i));
                            if(!selectedItemRadius.equals("Unlimited"))
                            {

                                double distance = SphericalUtil.computeDistanceBetween(new LatLng(currLoc.getLatitude(),currLoc.getLongitude()),friendLoc);
                                if(selectedRaduius<distance)
                                    continue;

                            }
                            hm2.remove(friends.get(i));

                            final long ONE_MEGABYTE = 512 * 512;

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
                                    Marker m=mMap.addMarker(new MarkerOptions().position(friendLoc).title(localFriendUserName).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).anchor(0.5f, 1));
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
                            if(!selectedItemRadius.equals("Unlimited"))
                            {
                                double distance = SphericalUtil.computeDistanceBetween(new LatLng(currLoc.getLatitude(),currLoc.getLongitude()), notFriendLoc);
                                if(selectedRaduius<distance)
                                    continue;
                            }
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
            case R.id.itemRanking:
                Intent i2 = new Intent(UsersMapsActivity.this, RankingsActivity.class);
                startActivity(i2);
                return true;
            case R.id.itemRadius:

                final String[] listItem = new String[]{"Unlimited", "100m", "1km", "10km","100km"};
                selectedItemRadius = listItem[0];

                AlertDialog.Builder build = new AlertDialog.Builder(UsersMapsActivity.this);

                // set the title for the alert dialog
                build.setTitle("Choose radius:");

                build.setSingleChoiceItems(listItem, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), listItem[which], Toast.LENGTH_LONG).show();
                        selectedItemRadius=listItem[which];
                    }
                });

                build.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(selectedItemRadius.equals("100m"))
                        selectedRaduius = 100.0;
                        else if(selectedItemRadius.equals("1km"))
                            selectedRaduius = 1000.0;
                        else if(selectedItemRadius.equals("10km"))
                            selectedRaduius = 10000.0;
                        else if(selectedItemRadius.equals("100km"))
                            selectedRaduius = 100000.0;

                        showPlacesListener.onDataChange(ds);
                        onLocationChanged(currLoc);

                    }
                });

                build.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                build.create();
                AlertDialog alertDialog = build.create();
                alertDialog.show();

                return true;
            case R.id.itemService:
                startService = ! startService;
                if(startService) {
                    Intent intent = new Intent(getBaseContext(), NearbyLocationsService.class);
                    intent.putExtra("userName", userName);
                    startService(intent);
                }
                else
                stopService(new Intent(getBaseContext(), NearbyLocationsService.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}