package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.traveller.models.Treasure;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditTreasureActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private String treasureName="";
    private Treasure treasure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_treasure);

        Intent i=getIntent();
        treasureName=i.getStringExtra("name");
        TextView tName=findViewById(R.id.textViewTreasureName_edit_treasure);
        EditText etTreasureDesc=findViewById(R.id.editTextTreasureDescription_edit_treasure);
        EditText etTreasureHint=findViewById(R.id.editTextTreasureHint_edit_treasure);
        EditText etTreasureQuestion=findViewById(R.id.editTextTreasureQuestion_edit_treasure);
        EditText etTreasureAnswer=findViewById(R.id.editTextTreasureAnswer_edit_treasure);
        EditText etTreasurePoints=findViewById(R.id.editTextTreasurePoints_edit_treasure);

        myRef.child("treasures").child(treasureName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                Object o=dataSnapshot.getValue(Treasure.class);
                treasure=(Treasure)o;

                tName.setText(treasureName);
                etTreasureDesc.setText(treasure.description);
                etTreasureHint.setText(treasure.hint);
                etTreasureQuestion.setText(treasure.question);
                etTreasureAnswer.setText(treasure.answer);
                etTreasurePoints.setText(Integer.toString(treasure.points));
            }
        });

        Button cancel=findViewById(R.id.btnCancelEditTreasure_edit_treasure);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });

        Button save=findViewById(R.id.btnSaveEditTreasure_edit_treasure);
        save.setOnClickListener(v -> {
            Treasure toReturn=new Treasure();
            toReturn.name=treasureName;
            toReturn.description=etTreasureDesc.getText().toString();
            toReturn.hint=etTreasureHint.getText().toString();
            toReturn.question=etTreasureQuestion.getText().toString();
            toReturn.answer=etTreasureAnswer.getText().toString();
            toReturn.points=Integer.parseInt(etTreasurePoints.getText().toString());

            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", toReturn);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });
    }
}