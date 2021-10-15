package com.example.traveller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.example.traveller.models.TreasureHunt;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddTreasureHuntActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_treasure_hunt);

        Button btnCancel=findViewById(R.id.btnCancelTreasureHunt);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

                TreasureHunt th;
                if(rbLocal.isChecked()){
                    th=new TreasureHunt(thName,"local",thDesc);
                }
                else{
                    th=new TreasureHunt(thName,"world",thDesc);
                }

                myRef.child("treasureHunts").child(thName).setValue(th);
                finish();
            }
        });
    }
}