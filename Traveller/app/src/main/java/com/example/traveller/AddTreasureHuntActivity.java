package com.example.traveller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.traveller.models.TreasureHunt;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AddTreasureHuntActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    TreasureHunt th=new TreasureHunt();
    //ovde staviti ArrayList od Treasure, i u ActivityResult preuzeti
    //dodati treasure i staviti ga ovde, plus njegovo ime smestiti u
    //listu od tekuceg th

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_treasure_hunt);

        th.treasures=new ArrayList<String>();
        Button btnCancel=findViewById(R.id.btnCancelTreasureHunt);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageButton imgBtnAddTreasure=findViewById(R.id.imgBtnAddTreasure);
        imgBtnAddTreasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(AddTreasureHuntActivity.this,AddTreasureActivity.class);
                startActivity(i);
            }
        });

        Button btnAdd=findViewById(R.id.btnAddTreasureHunt);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etName=findViewById(R.id.editTextTreasureHuntName);
                EditText etDesc=findViewById(R.id.editTextTreasureHuntDesc);

                String thName=etName.getText().toString();
                String thDesc=etDesc.getText().toString();

                RadioButton rbLocal=findViewById(R.id.radioButtonLocal);
                RadioButton rbWorld=findViewById(R.id.radioButtonWorld);

                th.name=thName;
                th.description=thDesc;

                if(rbLocal.isChecked()){
                    th.category="local";
                }
                else{
                    th.category="world";
                }

                //dodati ovde za treasures logiku, za njihov upis u bazu
                if(th.name.isEmpty() || th.description.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please add a name and description", Toast.LENGTH_LONG).show();
                }
                else{
                    myRef.child("treasureHunts").child(thName).setValue(th);
                    finish();

                }
            }
        });
    }
}