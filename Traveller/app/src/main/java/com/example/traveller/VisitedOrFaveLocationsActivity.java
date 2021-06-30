package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class VisitedOrFaveLocationsActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private String userName;
    private boolean isFave;
    private HashMap<String,String> hm;
    ArrayList<String> listForListView=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visited_or_fave_locations);

        Intent i=getIntent();
        userName=i.getStringExtra("userName");
        isFave=i.getBooleanExtra("isFavorite",false);

        ListView list=findViewById(R.id.listViewList);

        if(isFave){
            myRef.child("users").child(userName).child("favoritePlaces").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.getResult().exists()) {
                        Toast.makeText(getApplicationContext(),"You don't have any favorite places. Go to Map!",Toast.LENGTH_LONG).show();
                    }
                    else {
                        Object u=task.getResult().getValue();
                        hm=(HashMap<String, String>)u;
                        for (String key: hm.keySet()) {
                            listForListView.add(key);
                        }
                        list.setAdapter(new ArrayAdapter<String>(VisitedOrFaveLocationsActivity.this, android.R.layout.simple_list_item_1, listForListView ));
                    }
                }
            });
        }
        else{
            myRef.child("users").child(userName).child("visitedPlaces").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.getResult().exists()) {
                        Toast.makeText(getApplicationContext(),"You don't have any visited places. Go to Map!",Toast.LENGTH_LONG).show();
                    }
                    else {
                        Object u=task.getResult().getValue();
                        hm=(HashMap<String, String>)u;
                        for (String key: hm.keySet()) {
                            listForListView.add(key);
                        }
                        list.setAdapter(new ArrayAdapter<String>(VisitedOrFaveLocationsActivity.this, android.R.layout.simple_list_item_1, listForListView ));
                    }
                }
            });
        }
    }
}