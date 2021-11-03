package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewTreasuresListActivity extends AppCompatActivity {

    private boolean isAdmin=false;
    private boolean fromEdit=false;
    private String thName="";
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private TreasureHunt th=new TreasureHunt();
    private ArrayList<Treasure> newlyAddedTreasures=new ArrayList<>();

    int LAUNCH_SECOND_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_treasures_list);

        Intent i=getIntent();
        isAdmin=i.getBooleanExtra("isAdmin",false);
        thName=i.getStringExtra("name");
        fromEdit=i.getBooleanExtra("fromEdit",false);
        //Button btnAddTreasure=findViewById(R.id.btnAddTreasure_treasures_list);
        //Button btnSave=findViewById(R.id.btnSave_treasures_list);
        Button btnBack=findViewById(R.id.btnBack_treasures_list);
        ListView listViewTreasures=findViewById(R.id.listViewTreasures_treasures_list);

        if(!fromEdit){
            //btnAddTreasure.setEnabled(false);
            //btnAddTreasure.setVisibility(View.INVISIBLE);
            //btnSave.setEnabled(false);
            //btnSave.setVisibility(View.INVISIBLE);
        }
        else{

            /*btnAddTreasure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ViewTreasuresListActivity.this,AddTreasureActivity.class);
                    startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myRef.child("treasureHunts").child(thName).setValue(th);
                    for (Treasure t:newlyAddedTreasures) {
                        myRef.child("treasures").child(t.name).setValue(t);
                    }
                    //finish();
                }
            });*/
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        myRef.child("treasureHunts").child(thName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object o=snapshot.getValue(TreasureHunt.class);
                if(o.equals("")) return;
                if(o==null) return;
                th=(TreasureHunt) o;
                listViewTreasures.setAdapter(new ArrayAdapter<String>(ViewTreasuresListActivity.this, android.R.layout.simple_list_item_1,th.treasures));
                if(fromEdit){
                    registerForContextMenu(listViewTreasures);
                }
                listViewTreasures.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String toView=th.treasures.get((int)id); //toView postaje ime Treasure-a koji zelimo da view
                        Intent i=new Intent(ViewTreasuresListActivity.this,ViewTreasureActivity.class);
                        i.putExtra("name",toView);
                        i.putExtra("isAdmin",isAdmin);
                        startActivity(i);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                Treasure t= (Treasure) data.getSerializableExtra("result");
                th.treasures.add(t.name);
                newlyAddedTreasures.add(t);
                Toast.makeText(getApplicationContext(), "testing:"+t.name+", "+ t.hint, Toast.LENGTH_LONG).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Adding a treasure canceled", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_treasure_options, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.ctxMenuDeleteTreasure:
                //Toast.makeText(getApplicationContext(), "Delete", Toast.LENGTH_LONG).show();
                deleteTreasure(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void deleteTreasure(long id){
        String toDelete=th.treasures.get((int)id); //toDelete postaje ime Treasure-a koji zelimo da obrisemo
        myRef.child("treasureHunts").child(th.name).child("treasures").child(Integer.toString((int)id)).removeValue(); //brisemo ga iz liste treasures u ovo treasure hunt-u; jedan treasure moze pripadati samo jednom treasure hunt-u
        myRef.child("treasures").child(toDelete).removeValue();
    }

}