package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.traveller.models.TreasureHunt;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class TreasureHuntListActivity extends AppCompatActivity {

    private boolean isAdmin=false;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure_hunt_list);

        Intent i = getIntent();
        isAdmin = i.getBooleanExtra("isAdmin",false);

        Button addTreasureHunt=findViewById(R.id.buttonAddTreasureHunt);

        if(!isAdmin){
            addTreasureHunt.setVisibility(View.INVISIBLE);
            addTreasureHunt.setEnabled(false);
        }

        addTreasureHunt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(TreasureHuntListActivity.this,AddTreasureHuntActivity.class);
                startActivity(i);
            }
        });

        ArrayList<String> testTreasures=new ArrayList<String>();
        ListView listViewTreasureHunts=findViewById(R.id.listViewTreasureHunts);
        myRef.child("treasureHunts").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,TreasureHunt> hm=(HashMap<String,TreasureHunt>)dataSnapshot.getValue();
                for (String key:hm.keySet()
                     ) {
                    testTreasures.add(key);
                }
                listViewTreasureHunts.setAdapter(new ArrayAdapter<String>(TreasureHuntListActivity.this, android.R.layout.simple_list_item_1, testTreasures ));
            }
        });
        //testTreasures.add("TreasureHunt1");
        //testTreasures.add("TreasureHunt2");
        //testTreasures.add("TreasureHunt3");
    }
}