package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.traveller.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    String userName;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    String username;
    String password;
    String email;
    String firstName;
    String lastName;
    String phoneNumber;
    HashMap<String,String> hm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Button btnCancel=(Button)findViewById(R.id.btnEditProfileCancel);
        Button btnSave=(Button)findViewById(R.id.btnEditProfileSave);

        Intent i=getIntent();
        userName=i.getStringExtra("userName");

        EditText etFirstName=(EditText) findViewById(R.id.editProfileTextFirstName);
        EditText etLastName=(EditText) findViewById(R.id.editProfileTextLastName);
        EditText etPhoneNumber=(EditText) findViewById(R.id.editProfileTextPhoneNumber);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        myRef.child("users").child(userName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                Object o=dataSnapshot.getValue();
                hm=(HashMap<String,String>) o;

                firstName=hm.get("firstName");
                lastName=hm.get("lastName");
                phoneNumber=hm.get("phoneNumber");

                etFirstName.setText(hm.get("firstName"));
                etLastName.setText(hm.get("lastName"));
                etPhoneNumber.setText(hm.get("phoneNumber"));
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!etFirstName.getText().toString().equals(""))
                    firstName=etFirstName.getText().toString();
                if(!etLastName.getText().toString().equals(""))
                    lastName=etLastName.getText().toString();
                if(!etPhoneNumber.getText().toString().equals(""))
                    phoneNumber=etPhoneNumber.getText().toString();

                DatabaseReference myRefUser=myRef.child("users").child(userName);
                myRefUser.child("firstName").setValue(firstName);
                myRefUser.child("lastName").setValue(lastName);
                myRefUser.child("phoneNumber").setValue(phoneNumber);

                finish();
            }
        });
    }

}