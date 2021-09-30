package com.example.traveller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class TreasureHuntListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure_hunt_list);

        ArrayList<String> testTreasures=new ArrayList<String>();
        testTreasures.add("TreasureHunt1");
        testTreasures.add("TreasureHunt2");
        testTreasures.add("TreasureHunt3");
        ListView listViewTreasureHunts=findViewById(R.id.listViewTreasureHunts);
        listViewTreasureHunts.setAdapter(new ArrayAdapter<String>(TreasureHuntListActivity.this, android.R.layout.simple_list_item_1, testTreasures ));
    }
}