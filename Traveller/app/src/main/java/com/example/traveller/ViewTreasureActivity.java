package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.traveller.models.Treasure;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ViewTreasureActivity extends AppCompatActivity {

    private boolean isAdmin=false;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private String treasureName="";
    private Treasure treasure=new Treasure();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_treasure);

        Intent i=getIntent();
        isAdmin=i.getBooleanExtra("isAdmin",false);
        treasureName=i.getStringExtra("name");

        myRef.child("treasures").child(treasureName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                Object o=dataSnapshot.getValue(Treasure.class);
                treasure=(Treasure)o;
                fillForm();
            }
        });

    }

    public void fillForm(){
        TextView treasureName=findViewById(R.id.textViewTreasureName_view_treasure);
        TextView treasureDesc=findViewById(R.id.textViewTreasureDesc_view_treasure);
        TextView treasureHint=findViewById(R.id.textViewTreasureHint_view_treasure);
        TextView treasureQuestion=findViewById(R.id.textViewTreasureQuestion_view_treasure);
        TextView treasureAnswer=findViewById(R.id.textViewTreasureAnswer_view_treasure);
        TextView treasurePoints=findViewById(R.id.textViewTreasurePoints_view_treasure);

        if(isAdmin){
            treasureQuestion.setVisibility(View.VISIBLE);
            treasureQuestion.setText(treasure.question);
            treasureAnswer.setVisibility(View.VISIBLE);
            treasureAnswer.setText(treasure.answer);
        }

        treasureName.setText(treasure.name);
        treasureDesc.setText(treasure.description);
        treasurePoints.setText("If found, this treasure brings you "+treasure.points+" points!");

        Button showHint=findViewById(R.id.btnShowHint_view_treasure);
        showHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                treasureHint.setText(treasure.hint);
            }
        });

    }

}