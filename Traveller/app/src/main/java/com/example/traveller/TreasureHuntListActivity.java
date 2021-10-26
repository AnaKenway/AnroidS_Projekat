package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.traveller.models.Treasure;
import com.example.traveller.models.TreasureHunt;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class TreasureHuntListActivity extends AppCompatActivity {

    private boolean isAdmin=false;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    ArrayList<String> treasureHuntsList=new ArrayList<String>();

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


        ListView listViewTreasureHunts=findViewById(R.id.listViewTreasureHunts);
       /* myRef.child("treasureHunts").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,TreasureHunt> hm=(HashMap<String,TreasureHunt>)dataSnapshot.getValue();
                for (String key:hm.keySet()
                     ) {
                    treasureHuntsList.add(key);
                }
                listViewTreasureHunts.setAdapter(new ArrayAdapter<String>(TreasureHuntListActivity.this, android.R.layout.simple_list_item_1, treasureHuntsList ));
                if(isAdmin)
                    registerForContextMenu(listViewTreasureHunts);
            }
        });*/

        myRef.child("treasureHunts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                treasureHuntsList.removeAll(treasureHuntsList);
                Object o=snapshot.getValue();
                if(o.equals("")) return;
                if(o==null) return;
                HashMap<String,TreasureHunt>hm=(HashMap<String,TreasureHunt>)o;
                for (String key:hm.keySet()
                ) {
                    treasureHuntsList.add(key);
                }
                listViewTreasureHunts.setAdapter(new ArrayAdapter<String>(TreasureHuntListActivity.this, android.R.layout.simple_list_item_1, treasureHuntsList ));
                if(isAdmin)
                    registerForContextMenu(listViewTreasureHunts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        listViewTreasureHunts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i=new Intent(TreasureHuntListActivity.this,ViewTreasureHuntActivity.class);
                i.putExtra("name",treasureHuntsList.get(position));
                i.putExtra("isAdmin",isAdmin);
                startActivity(i);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_treasure_hunt_admin_options, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.ctxMenuEditTreasureHunt:
                Toast.makeText(getApplicationContext(), "Edit", Toast.LENGTH_LONG).show();
                editTreasureHunt(info.id);
                return true;
            case R.id.ctxMenuDeleteTreasureHunt:
                Toast.makeText(getApplicationContext(), "Delete", Toast.LENGTH_LONG).show();
                deleteTreasureHunt(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void deleteTreasureHunt(long id){
        String toDelete=treasureHuntsList.get((int)id); //toDelete postaje ime TreasureHunt-a koji zelimo da obrisemo
        //prvo brisem sve treasures iz baze koji pripadaju ovom TreasureHunt-u

        myRef.child("treasureHunts").child(toDelete).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                TreasureHunt th=new TreasureHunt();
                th=dataSnapshot.getValue(TreasureHunt.class);
                for (String tName:th.treasures) {
                    myRef.child("treasures").child(tName).removeValue();
                }
                //sad mogu da obrisem i sam treasureHunt
                myRef.child("treasureHunts").child(toDelete).removeValue();
            }
        });
    }

    public void editTreasureHunt(long id){

    }

}