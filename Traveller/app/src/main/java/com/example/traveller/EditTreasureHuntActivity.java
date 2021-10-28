package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.traveller.models.TreasureHunt;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditTreasureHuntActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    TreasureHunt th=new TreasureHunt();

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

        myRef.child("treasureHunts").child(thName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
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
                finish();
            }
        });

    }
}