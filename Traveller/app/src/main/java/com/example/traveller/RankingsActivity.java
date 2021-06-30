package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class RankingsActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private String[] userNames;
    private String[] points;
    private Integer[] icons;
    private Integer[] pointsInt;
    int i,k;
    boolean flag;

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        myRef.child("users").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,HashMap<String,String>> hm=(HashMap<String,HashMap<String,String>>) dataSnapshot.getValue();
                i=0;
                userNames=new String[hm.size()];
                points=new String[hm.size()];
                icons =new Integer[hm.size()];
                pointsInt=new Integer[hm.size()];
                for (String key: hm.keySet()) {
                    userNames[i]=key;
                    //points[i]=hm.get(key).get("points");
                    pointsInt[i]=Integer.parseInt(hm.get(key).get("points"));
                    if(i>2)
                    {
                        icons[i]=R.drawable.baseline_workspace_premium_black_48dp;
                    }
                    else
                    {
                        icons[i]=R.drawable.baseline_emoji_events_black_48dp;
                    }
                    i++;
                }
                int n = pointsInt.length;

                i=0;
                // One by one move boundary of unsorted subarray
                for (i = 0; i < n-1; i++) {
                    // Find the minimum element in unsorted array
                    int min_idx = i;
                    for (int j = i + 1; j < n; j++)
                        if (pointsInt[j] > pointsInt[min_idx])
                            min_idx = j;

                    // Swap the found minimum element with the first
                    // element
                    int temp = pointsInt[min_idx];
                    pointsInt[min_idx] = pointsInt[i];
                    pointsInt[i] = temp;

                    String tempS = userNames[min_idx];
                    userNames[min_idx] =userNames[i];
                    userNames[i] = tempS;
                }

                for(i=0;i<pointsInt.length;i++){
                    points[i]=pointsInt[i].toString();
                }

                MyListAdapter adapter=new MyListAdapter(RankingsActivity.this,userNames,points, icons);
                list=(ListView)findViewById(R.id.listViewRankings);
                list.setAdapter(adapter);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //getMenuInflater().inflate(R.menu.menu_toolbar_user_profile, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}