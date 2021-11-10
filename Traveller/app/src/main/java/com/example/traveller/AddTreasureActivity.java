package com.example.traveller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.traveller.models.Treasure;
import com.example.traveller.models.TreasureHunt;

public class AddTreasureActivity extends AppCompatActivity {

    //u ovom activity se nece upisivati treasure u bazu
    //bice upisan tek kad se doda ceo treasure hunt
    //ova activity kao rezultat vraca objekat Treasure
    Treasure t=new Treasure();
    private static final int LAUNCH_CLOUD_ANCHOR_ACTIVITY=1;

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

        ImageButton btnAdd3DObject=findViewById(R.id.imgBtnAdd3DObject);
        btnAdd3DObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(AddTreasureActivity.this,CloudAnchorActivity.class);
                i.putExtra("isForAddOrEditTreasure",true);
                i.putExtra("isAdmin",true);
                startActivityForResult(i,LAUNCH_CLOUD_ANCHOR_ACTIVITY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_CLOUD_ANCHOR_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                Treasure tResult= (Treasure) data.getSerializableExtra("result");
                Toast.makeText(getApplicationContext(), "Anchor successfully created", Toast.LENGTH_LONG).show();
                t.updatedAtTimestamp=tResult.updatedAtTimestamp;
                t.roomCode=tResult.roomCode;
                t.hostedAnchorID=tResult.hostedAnchorID;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Adding an Anchor canceled", Toast.LENGTH_LONG).show();
            }
        }
    }

}