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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
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

public class EditTreasureHuntActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    TreasureHunt th=new TreasureHunt();
    ArrayList<Treasure> newlyAddedTreasures=new ArrayList<>();
    ListView listViewTreasures;

    int LAUNCH_SECOND_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_treasure_hunt);

        Intent i=getIntent();
        String thName=i.getStringExtra("name");
        TextView thTitle=findViewById(R.id.textViewEditTreasureHuntTitle);
        thTitle.setText(thName);
        //EditText editTextName=findViewById(R.id.editTextEditTreasureHuntName);
        EditText editTextDesc=findViewById(R.id.editTextEditTreasureHuntDesc);
        RadioButton rbLocal=findViewById(R.id.radioButtonEditLocal);
        RadioButton rbWorld=findViewById(R.id.radioButtonEditWorld);
        Button btnSave=findViewById(R.id.btnSaveEditTHunt);
        Button btnCancel=findViewById(R.id.btnCancelEditTHunt);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

       /* myRef.child("treasureHunts").child(thName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                Object o=dataSnapshot.getValue(TreasureHunt.class);
                th=(TreasureHunt) o;
                //editTextName.setText(th.name);
                editTextDesc.setText(th.description);
                if(th.category.equals("local")){
                    rbLocal.setChecked(true);
                    rbWorld.setChecked(false);
                }
                else{
                    rbLocal.setChecked(false);
                    rbWorld.setChecked(true);
                }
                ListView listViewTreasures=findViewById(R.id.listViewTreasures_edit_treasure_hunt);
                listViewTreasures.setAdapter(new ArrayAdapter<String>(EditTreasureHuntActivity.this, android.R.layout.simple_list_item_1,th.treasures));
                registerForContextMenu(listViewTreasures);
            }
        });*/

        myRef.child("treasureHunts").child(thName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object o=snapshot.getValue(TreasureHunt.class);
                if(o.equals("")) return;
                if(o==null) return;
                th=(TreasureHunt) o;
                editTextDesc.setText(th.description);
                if(th.category.equals("local")){
                    rbLocal.setChecked(true);
                    rbWorld.setChecked(false);
                }
                else{
                    rbLocal.setChecked(false);
                    rbWorld.setChecked(true);
                }
                listViewTreasures=findViewById(R.id.listViewTreasures_edit_treasure_hunt);
                listViewTreasures.setAdapter(new ArrayAdapter<String>(EditTreasureHuntActivity.this, android.R.layout.simple_list_item_1,th.treasures));
                registerForContextMenu(listViewTreasures);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                th.description=editTextDesc.getText().toString();
                if(rbLocal.isChecked()){
                    th.category="local";
                }
                else{
                    th.category="world";
                }
                myRef.child("treasureHunts").child(thName).setValue(th);
                for (Treasure t:newlyAddedTreasures) {
                    myRef.child("treasures").child(t.name).setValue(t);
                }
                finish();
            }
        });

        Button btnAddTreasure=findViewById(R.id.btnAddTreasure_edit_treasure_hunt);
        btnAddTreasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EditTreasureHuntActivity.this,AddTreasureActivity.class);
                startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete_option, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                Treasure t= (Treasure) data.getSerializableExtra("result");
                th.treasures.add(t.name);
                newlyAddedTreasures.add(t);
                Toast.makeText(getApplicationContext(), "Treasure added", Toast.LENGTH_LONG).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Adding a treasure canceled", Toast.LENGTH_LONG).show();
            }
        }
    }

}