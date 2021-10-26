package com.example.traveller;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.traveller.models.Treasure;
import com.example.traveller.models.TreasureHunt;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AddTreasureHuntActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    TreasureHunt th=new TreasureHunt();
    Treasure t;
    ArrayList<Treasure> treasures=new ArrayList<>();

    int LAUNCH_SECOND_ACTIVITY = 1;

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
                Intent i = new Intent(AddTreasureHuntActivity.this,AddTreasureActivity.class);
                startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
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

                if(th.name.isEmpty() || th.description.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please add a name and description", Toast.LENGTH_LONG).show();
                }
                else{
                    myRef.child("treasureHunts").child(thName).setValue(th);
                    //dodati ovde za treasures logiku, za njihov upis u bazu
                    //treba sve treasures iz ArrayList treasures upisati u myRef.child("treasures")  na primer
                    for (Treasure tt:treasures) {
                        myRef.child("treasures").child(tt.name).setValue(tt);
                    }
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                t= (Treasure) data.getSerializableExtra("result");
                th.treasures.add(t.name);
                treasures.add(t);
                Toast.makeText(getApplicationContext(), "testing:"+t.name+", "+ t.hint, Toast.LENGTH_LONG).show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Adding a treasure canceled", Toast.LENGTH_LONG).show();
            }
        }
    }
}