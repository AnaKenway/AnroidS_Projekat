package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private String URI;
    private String userName;
    private String firstName;
    private String lastName;
    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_user_profile);
        Intent i = getIntent();
        userName = i.getStringExtra("userName");

        final TextView fullNameTextView = findViewById(R.id.textViewFullName);
        final TextView userNameTextView = findViewById(R.id.textViewUserName);
        final ImageView profileImgImageView = findViewById(R.id.imgViewProfilePicture);
        final ImageView allFriendsImageView = findViewById(R.id.imageViewAllFriends);

        allFriendsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(UserProfileActivity.this, AllFriendsActivity.class);
                //i.putExtra("isLogin",true);
                startActivity(i);
            }
        });
        myRef.child("users").child(userName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.getResult().exists()) {
                    Toast.makeText(getApplicationContext(),"Couldn't find data",Toast.LENGTH_LONG).show();
                }
                else {
                    Object u=task.getResult().getValue();
                    HashMap<String,String> hm=(HashMap<String, String>) u;
                    firstName = hm.get("firstName");
                    lastName = hm.get("lastName");
                    URI = hm.get("imgUrl");
                    storageRef = storage.getReference(URI);

                    fullNameTextView.setText(firstName + " " + lastName);
                    userNameTextView.setText("@"+userName);
                    final long ONE_MEGABYTE = 1024 * 1024;

                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                            Bitmap smallBitmap = Bitmap.createScaledBitmap(mutableBitmap, 500, 500, false);
                            profileImgImageView.setImageBitmap(smallBitmap);
                        }
                    });
                }
            }
        });

        Button btnEditProfile=(Button) findViewById(R.id.buttonEditProfile);
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(UserProfileActivity.this,EditProfileActivity.class);
                //i.putExtra("isLogin",true);
                startActivity(i);
            }
        });

        Button btnBack=(Button) findViewById(R.id.buttonBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

        Button btnLogOut=(Button) findViewById(R.id.buttonLogOut);
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
            }
        });
    }
}