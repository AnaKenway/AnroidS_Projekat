package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class AllFriendsActivity extends AppCompatActivity {

    private String userName;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference();
    private String userFirstName, userLastName;
    private ListView list;
    private ArrayList<String> listForListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_friends);
        list = (ListView) findViewById(R.id.listViewList);


        Intent i = getIntent();
        userName = i.getStringExtra("userName");
        boolean bool = i.getBooleanExtra("isAllFriends",false);

        if( bool )
        {
            myRef.child("users").child(userName).child("friends").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.getResult().exists()) {
                        Toast.makeText(getApplicationContext(),"You don't have any friends. Go to Map!",Toast.LENGTH_LONG).show();
                    }
                    else {
                        Object u=task.getResult().getValue();
                        listForListView = (ArrayList<String>) u;
                        list.setAdapter(new ArrayAdapter<String>(AllFriendsActivity.this, android.R.layout.simple_list_item_1, listForListView ));

                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                String friendUserName = (String) adapterView.getAdapter().getItem(i);

                                Intent intent = new Intent(AllFriendsActivity.this, FriendProfileActivity.class);
                                intent.putExtra("friendUserName", friendUserName);
                                intent.putExtra("userName", userName);
                                startActivity(intent);
                            }
                        });
                    }
                }
            });
        }
        else
        {
            myRef.child("users").child(userName).child("friendRequests").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.getResult().exists()) {
                        Toast.makeText(getApplicationContext(),"You don't have any requests!",Toast.LENGTH_LONG).show();
                    }
                    else {
                        Object u=task.getResult().getValue();
                        HashMap<String, String> hm = (HashMap<String, String>) u;
                        listForListView = new ArrayList<String>();
                        for(String key: hm.keySet()){
                            listForListView.add(key);
                        }
                        list.setAdapter(new ArrayAdapter<String>(AllFriendsActivity.this, android.R.layout.simple_list_item_1, listForListView ));
                        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                String friendUserName = (String) adapterView.getAdapter().getItem(i);

                                Intent intent = new Intent(AllFriendsActivity.this, FriendProfileActivity.class);
                                intent.putExtra("friendUserName", friendUserName);
                                intent.putExtra("userName", userName);
                                intent.putExtra("isEnteredFromRequests",true);
                                startActivity(intent);
                            }
                        });
                    }
                }
            });
        }
    }
}