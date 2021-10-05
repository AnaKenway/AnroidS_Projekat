package com.example.traveller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class TreasureHuntListActivity extends AppCompatActivity {

    private boolean isAdmin=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure_hunt_list);

        Intent i = getIntent();
        isAdmin = i.getBooleanExtra("isAdmin",false);

        if(!isAdmin){
            Button addTreasureHunt=findViewById(R.id.buttonAddTreasureHunt);
            addTreasureHunt.setVisibility(View.INVISIBLE);
            addTreasureHunt.setEnabled(false);
        }

        ArrayList<String> testTreasures=new ArrayList<String>();
        testTreasures.add("TreasureHunt1");
        testTreasures.add("TreasureHunt2");
        testTreasures.add("TreasureHunt3");
        ListView listViewTreasureHunts=findViewById(R.id.listViewTreasureHunts);
        listViewTreasureHunts.setAdapter(new ArrayAdapter<String>(TreasureHuntListActivity.this, android.R.layout.simple_list_item_1, testTreasures ));
    }
}