package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.example.traveller.models.TreasureHunt;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewTreasureHuntActivity extends AppCompatActivity {

    private boolean isAdmin=false;
    private String THName="";
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_treasure_hunt);

        CheckedTextView chkTextViewActive=findViewById(R.id.checkedTextViewActiveTH);
        chkTextViewActive.setVisibility(View.INVISIBLE);

        Intent i = getIntent();
        isAdmin = i.getBooleanExtra("isAdmin",false);
        THName=i.getStringExtra("name");

        //dodaj da pita ako je admin, da bude vidljivo edit dugme, koje treba da dodam i na layout
        //dodaj i neku sliku, kao piratske mape sa X i onim ---- kao putanjom do x, neka bude ista slika za svaki TH,
        //cisto da ne bude onoliko prazan layout


        myRef.child("treasureHunts").child(THName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TreasureHunt th=snapshot.getValue(TreasureHunt.class);
                 fillForm(th);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
}