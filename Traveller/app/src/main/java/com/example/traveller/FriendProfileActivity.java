package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendProfileActivity extends AppCompatActivity {

    private String userName, friendUserName, URI;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private boolean isFriend=false;
    private String userFirstName, userLastName;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef;
    private Bitmap bmp;
    private Boolean isEnteredFromRequests=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        userName = i.getStringExtra("userName");
        friendUserName = i.getStringExtra("friendUserName");
        isEnteredFromRequests = i.getBooleanExtra("isEnteredFromRequests", false);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        myRef.child("users").child(userName).child("friends").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Object u=task.getResult().getValue();
                ArrayList<String> friends =(ArrayList<String>) u;


                if(friends!=null) {
                    for (int i = 0; i < friends.size(); i++) {
                        if (friends.get(i).equals(friendUserName))
                            isFriend = true;
                    }
                }

                myRef.child("users").child(friendUserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        Object u = task.getResult().getValue();
                        HashMap<String,String> hm = (HashMap<String,String>) u;
                        userFirstName = hm.get("firstName");
                        userLastName = hm.get("lastName");
                        URI = hm.get("imgUrl");
                        storageRef = storage.getReference(URI);

                        if(isFriend)
                        {
                            setContentView(R.layout.activity_friend_profile);

                            Button unfriend = (Button) findViewById(R.id.buttonUnfriend);
                            unfriend.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getApplicationContext(), "Unfriend kliknuto!", Toast.LENGTH_LONG).show();
                                }
                            });
                            ImageView friendVisitedLocations = (ImageView) findViewById(R.id.imageViewFriendsVisitedLoations);
                            friendVisitedLocations.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getApplicationContext(), "Visited friend locations kliknuto!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        else if(isEnteredFromRequests)
                        {
                            setContentView(R.layout.activity_profile_from_request);

                            Button accept = (Button) findViewById(R.id.buttonAccept);
                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getApplicationContext(), "Accept!", Toast.LENGTH_LONG).show();
                                    myRef.child("users").child(userName).child("friendRequests").child(friendUserName).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //ovde
                                            myRef.child("users").child(userName).child("friends").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                                @Override
                                                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                                                    ArrayList<String> myfriendList=(ArrayList<String>) dataSnapshot.getValue();
                                                    if(myfriendList==null)
                                                        myfriendList=new ArrayList<String>();
                                                    myfriendList.add(friendUserName);
                                                    myRef.child("users").child(userName).child("friends").setValue(myfriendList).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(@NonNull Void aVoid) {
                                                            myRef.child("users").child(friendUserName).child("friends").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                                                @Override
                                                                public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                                                                    ArrayList<String> friendList=(ArrayList<String>) dataSnapshot.getValue();
                                                                    if(friendList==null)
                                                                        friendList=new ArrayList<String>();
                                                                    friendList.add(userName);
                                                                    myRef.child("users").child(friendUserName).child("friends").setValue(friendList).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(@NonNull Void aVoid) {
                                                                            Toast.makeText(getApplicationContext(), "Friend request accepted", Toast.LENGTH_LONG).show();
                                                                            finish();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });

                            Button decline = (Button) findViewById(R.id.buttonDecline);
                            decline.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    myRef.child("users").child(userName).child("friendRequests").child(friendUserName).removeValue().
                                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getApplicationContext(), "Friend request declined", Toast.LENGTH_LONG).show();
                                            finish();
                                        }
                                    });

                                }
                            });
                        }
                        else
                        {
                            setContentView(R.layout.layout_not_friend);

                            Button addFriend = (Button) findViewById(R.id.buttonAddFriend);
                            addFriend.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getApplicationContext(), "Add friend kliknuto!", Toast.LENGTH_LONG).show();
                                    myRef.child("users").child(friendUserName).child("friendRequests").child(userName).setValue("true");
                                }
                            });
                        }

                        //ActionBar actionBar = getSupportActionBar();
                        //actionBar.setDisplayHomeAsUpEnabled(true);

                        final long ONE_MEGABYTE = 1024 * 1024;
                        ImageView friendImageView = (ImageView) findViewById(R.id.imgViewFriendProfilePicture);

                        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                                Bitmap smallBitmap = Bitmap.createScaledBitmap(mutableBitmap, 500, 500, false);
                                friendImageView.setImageBitmap(smallBitmap);
                            }
                        });
                        TextView friendFullName =(TextView) findViewById(R.id.textViewFriendFullName);
                        TextView textViewfriendUserName =(TextView) findViewById(R.id.textViewFriendUserName);
                        friendFullName.setText(userFirstName+" "+userLastName);
                        textViewfriendUserName.setText("@" + friendUserName);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
               /* myRef.child("users").child(friendUserName).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        Object u = task.getResult().getValue();
                        HashMap<String,String> hm = (HashMap<String,String>) u;
                        userFirstName = hm.get("firstName");
                        userLastName = hm.get("lastName");
                        URI = hm.get("imgUrl");
                        storageRef = storage.getReference(URI);

                        if(isEnteredFromRequests)
                        {
                            setContentView(R.layout.activity_profile_from_request);

                            Button accept = (Button) findViewById(R.id.buttonAccept);
                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getApplicationContext(), "Accept!", Toast.LENGTH_LONG).show();
                                    // myRef.child("users").child(friendUserName).child("friendRequests").child(userName).setValue("true");
                                }
                            });

                            Button decline = (Button) findViewById(R.id.buttonDecline);
                            decline.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getApplicationContext(), "Decline!", Toast.LENGTH_LONG).show();
                                    // myRef.child("users").child(friendUserName).child("friendRequests").child(userName).setValue("true");
                                }
                            });
                        }
                        else
                        {
                            setContentView(R.layout.layout_not_friend);

                            Button addFriend = (Button) findViewById(R.id.buttonAddFriend);
                            addFriend.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(getApplicationContext(), "Add friend kliknuto!", Toast.LENGTH_LONG).show();
                                    myRef.child("users").child(friendUserName).child("friendRequests").child(userName).setValue("true");
                                }
                            });
                        }

                        //ActionBar actionBar = getSupportActionBar();
                        //actionBar.setDisplayHomeAsUpEnabled(true);

                        final long ONE_MEGABYTE = 1024 * 1024;
                        ImageView friendImageView = (ImageView) findViewById(R.id.imgViewFriendProfilePicture);

                        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                                Bitmap smallBitmap = Bitmap.createScaledBitmap(mutableBitmap, 500, 500, false);
                                friendImageView.setImageBitmap(smallBitmap);
                            }
                        });
                        TextView friendFullName =(TextView) findViewById(R.id.textViewFriendFullName);
                        TextView textViewfriendUserName =(TextView) findViewById(R.id.textViewFriendUserName);
                        friendFullName.setText(userFirstName+" "+userLastName);
                        textViewfriendUserName.setText("@" + friendUserName);
                    }
                });*/

                Toast.makeText(getApplicationContext(), "You have no friends! :P", Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}