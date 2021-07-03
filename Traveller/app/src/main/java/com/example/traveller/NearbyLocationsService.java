package com.example.traveller;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class NearbyLocationsService extends Service implements LocationListener {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    boolean isRunning=true;
    String userName;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    LatLng myLoc;

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }


    private class PrimeThread  extends Thread {
        String username;
        PrimeThread(String un){username=un;}
        public void run(){
            while(isRunning)
            {
                //logika za proveru da li je u blizini neki korisnik

                try {
                    if(username==null) {
                        isRunning=false;
                        continue;
                    }

                    Log.e("MYAPP",userName);
                    myRef.child("users").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot!=null){
                                HashMap<String,HashMap<String,String>> hm=(HashMap<String,HashMap<String,String>>) dataSnapshot.getValue();
                                HashMap<String, HashMap<String, ArrayList<String>>> hm2=(HashMap<String, HashMap<String, ArrayList<String>>>) dataSnapshot.getValue();
                                ArrayList<String> listFriends=hm2.get(userName).get("friends");
                                Double lat=Double.parseDouble(hm.get(userName).get("latitude"));
                                Double lon=Double.parseDouble(hm.get(userName).get("longitude"));
                                myLoc=new LatLng(lat,lon);
                                for (String friendUserName : listFriends) {

                                    Double latUser=Double.parseDouble(hm.get(friendUserName).get("latitude"));
                                    Double lonUser=Double.parseDouble(hm.get(friendUserName).get("longitude"));
                                    LatLng friendLoc=new LatLng(latUser,lonUser);

                                    double distance = SphericalUtil.computeDistanceBetween(myLoc,friendLoc);

                                    if(distance<=50){
                                        Intent notificationIntent = new Intent(NearbyLocationsService.this, MainActivity.class);
                                        PendingIntent pendingIntent =
                                                PendingIntent.getActivity(NearbyLocationsService.this, 0, notificationIntent, 0);

                                        Notification notification = new Notification.Builder(NearbyLocationsService.this)
                                                .setContentTitle("Nearby Friend")
                                                .setContentText(friendUserName+" is nearby!")
                                                .setSmallIcon(R.drawable.all_friends)
                                                .setContentIntent(pendingIntent)
                                                .build();

// Notification ID cannot be 0.
                                        startForeground(1, notification);
                                    }

                                }
                            }
                        }
                    });

                    if(myLoc==null) continue;
                    myRef.child("places").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                            HashMap<String,HashMap<String,HashMap<String,String>>> hm = (HashMap<String,HashMap<String,HashMap<String,String>>>) dataSnapshot.getValue();
                            for(String key : hm.keySet())
                            {
                                for(String key2 : hm.get(key).keySet()){
                                        LatLng latLng = new LatLng(Double.parseDouble(hm.get(key).get(key2).get("latitude")), Double.parseDouble(hm.get(key).get(key2).get("longitude")));{
                                            double distance = SphericalUtil.computeDistanceBetween(myLoc, latLng);
                                            if(distance<=50) {
                                                Intent notificationIntent = new Intent(NearbyLocationsService.this, MainActivity.class);
                                                PendingIntent pendingIntent =
                                                        PendingIntent.getActivity(NearbyLocationsService.this, 0, notificationIntent, 0);

                                                Notification notification = new Notification.Builder(NearbyLocationsService.this)
                                                        .setContentTitle("Nearby "+key)
                                                        .setContentText("The "+key+" "+key2+" is near you!")
                                                        .setSmallIcon(R.drawable.visited_places)
                                                        .setContentIntent(pendingIntent)
                                                        .build();

// Notification ID cannot be 0.
                                                startForeground(1, notification);
                                            }
                                        }
                                }
                            }
                        }
                    });

                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.



            Intent notificationIntent = new Intent(NearbyLocationsService.this, MainActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(NearbyLocationsService.this, 0, notificationIntent, 0);

            Notification notification = new Notification.Builder(NearbyLocationsService.this)
                            .setContentTitle("Notification")
                            .setContentText("Not2")
                            .setSmallIcon(R.drawable.all_friends)
                            .setContentIntent(pendingIntent)
                            .build();

// Notification ID cannot be 0.
            startForeground(1, notification);
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
       /*HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);*/

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        /*Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;*/

        super.onStartCommand(intent,flags,startId);
        userName=intent.getStringExtra("userName");

        PrimeThread p = new PrimeThread(userName);
        p.start();

        return START_STICKY;
    }

    public NearbyLocationsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        isRunning=false;
    }
}