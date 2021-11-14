package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.traveller.common.cloudanchor.FirebaseManager;
import com.example.traveller.models.TreasureHunt;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewTreasureHuntActivity extends AppCompatActivity {

    private boolean isAdmin=false;
    private String THName="";
    private String username;
    private String activeTHName;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private TreasureHuntsListener thListener=new TreasureHuntsListener();
    private FirebaseManager firebaseManager=new FirebaseManager(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_treasure_hunt);

        Intent i = getIntent();
        isAdmin = i.getBooleanExtra("isAdmin",false);
        THName=i.getStringExtra("name");
        username=i.getStringExtra("username");
        thListener.thNameForChecking=THName;
        CheckedTextView chkTextViewActive=findViewById(R.id.checkedTextViewActiveTH);
        chkTextViewActive.setVisibility(View.GONE);
        TextView txtViewCompletedTH=findViewById(R.id.textViewTHCompleted);
        txtViewCompletedTH.setVisibility(View.GONE);

        firebaseManager.getActiveTreasureHunt(username,thListener);

       /* myRef.child("users").child(username).child("activeTreasureHunt").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                Object o=dataSnapshot.getValue();
                if(o==null){
                    chkTextViewActive.setVisibility(View.GONE);
                }else{
                    activeTHName=(String)o;
                    if(!activeTHName.equals(THName))
                        chkTextViewActive.setVisibility(View.GONE);
                }
                //sad isto na osnovu ovih if-s videti da li enable ili disable dugme za activate/deactivate TH
                //i sta na njemu da pise

                //mozda je bolje da ovo nekako realizujem u FirebaseManager, kad ga vec imam
                //i onda ovde da se pozove neki callback, koji ce da uradi ovo za dugmice i CheckedTextView
                //a u FirebaseManager da proveri da li ima aktivanTH, da li je bas to ovaj, da li je completed
                //pa onda rezultat (boolean) da vraca preko nekog listener-a
            }
        });*/

        Button btnEdit=findViewById(R.id.btnEditTHnView);
        Button btnDelete=findViewById(R.id.btnDeleteTHInView);

        if(!isAdmin){

            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);

            btnEdit.setEnabled(false);
            btnDelete.setEnabled(false);
        }
        else{

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myRef.child("treasureHunts").child(THName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                            TreasureHunt th=new TreasureHunt();
                            th=dataSnapshot.getValue(TreasureHunt.class);
                            for (String tName:th.treasures) {
                                myRef.child("treasures").child(tName).removeValue();
                            }
                            //sad mogu da obrisem i sam treasureHunt
                            myRef.child("treasureHunts").child(THName).removeValue();
                            Toast.makeText(getApplicationContext(), "Treasure Hunt deleted", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                }
            });

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i=new Intent(ViewTreasureHuntActivity.this,EditTreasureHuntActivity.class);
                    i.putExtra("name",THName);
                    startActivity(i);
                }
            });
        }


        myRef.child("treasureHunts").child(THName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null) {
                    TreasureHunt th = snapshot.getValue(TreasureHunt.class);
                    fillForm(th);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Button viewTreasures=findViewById(R.id.btnViewTreasures_view_treasure_hunt);
        viewTreasures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(ViewTreasureHuntActivity.this,ViewTreasuresListActivity.class);
                i.putExtra("name",THName);
                i.putExtra("isAdmin",isAdmin);
                i.putExtra("fromEdit",false);//ako je iz Treasure Hunt edita, onda treba da mu omogucim context meni za delete i da omogucim add dugme
                startActivity(i);
            }
        });
    }

    private void fillForm(TreasureHunt t){
        TextView thName=findViewById(R.id.textViewTreasureHuntName);
        thName.setText(t.name);

        TextView thCategory=findViewById(R.id.textViewTHCategory);
        thCategory.setText("Category: "+t.category);

        TextView thDesc=findViewById(R.id.textViewTHDesc);
        thDesc.setText(t.description);


        //pronaci u bazi da li je ulogovanom korisniku
        //ovo aktivan treasure hunt, ako uopste ima aktivnog
        //ako jeste, vratiti visible na onaj CheckedTextViewActive
    }

    private void ShowOrHideActiveTHCheckedView(boolean isActive){
        //sakrije ili prikaze active th checked view
        //u zavisnosti sta mu TreasureHuntsListener prosledi
        // i da napise Activate ili Deactivate na activation button
        CheckedTextView chkTextViewActive=findViewById(R.id.checkedTextViewActiveTH);
        if(isActive){
            chkTextViewActive.setVisibility(View.VISIBLE);
            //dodati da na novom dugmetu pise Deactivate
        }
        else{
            chkTextViewActive.setVisibility(View.GONE);
            //dodati da na novom dugmetu pise Activate
        }
    }

    private void ShowOrHideCompletedTHCheckedView(boolean isCompleted){
        //sakrije ili prikaze completed th checked view
        //u zavisnosti sta mu TreasureHuntsListener prosledi
        //takodje activate/deactivate dugme treba sakriti
    }

    private final class TreasureHuntsListener
            implements FirebaseManager.CompletedTreasureHuntsListener,
                        FirebaseManager.ActiveTreasureHuntListener{

        /** The name of the treasure hunt we are comparing to the ones of the user
         * in this case, it's the treasure hunt we are viewing on this activity*/
        public String thNameForChecking;

        @Override
        public void onCompletedTreasureHunts(ArrayList<String> completedTHs) {


            //ShowOrHideCompletedTHCheckedView(
        }

        @Override
        public void onActiveTreasureHunt(String activeTH) {
            if(activeTH.equals(thNameForChecking))
                ShowOrHideActiveTHCheckedView(true);
            else ShowOrHideActiveTHCheckedView(false);
        }
    }

}

