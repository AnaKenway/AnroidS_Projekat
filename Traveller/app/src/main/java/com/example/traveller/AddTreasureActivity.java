package com.example.traveller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.traveller.models.Treasure;
import com.example.traveller.models.TreasureHunt;

public class AddTreasureActivity extends AppCompatActivity {

    //u ovom activity se nece upisivati treasure u bazu
    //bice upisan tek kad se doda ceo treasure hunt
    //ova activity kao rezultat vraca objekat Treasure
    Treasure t=new Treasure();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_treasure);

        Button addTreasure=findViewById(R.id.btnAddTreasure);
        addTreasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextTName=findViewById(R.id.editTextTreasureName);
                EditText editTextTDesc=findViewById(R.id.editTextTreasureDescription);
                EditText editTextTHint=findViewById(R.id.editTextTreasureHint);
                EditText editTextTQuestion=findViewById(R.id.editTextTreasureQuestion);
                EditText editTextTPoints=findViewById(R.id.editTextTreasurePoints);

                t.name=editTextTName.getText().toString();
                t.description=editTextTDesc.getText().toString();
                t.hint=editTextTHint.getText().toString();
                t.question=editTextTQuestion.getText().toString();
                String points=editTextTPoints.getText().toString();

                if(!points.isEmpty())
                    t.points=Integer.parseInt(points);

                if(t.name.isEmpty() || t.description.isEmpty() || t.hint.isEmpty() || t.question.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill in all the fields", Toast.LENGTH_LONG).show();
                }
                else{
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", t);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });

        Button cancel=findViewById(R.id.btnCancelTreasure);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });
    }
}