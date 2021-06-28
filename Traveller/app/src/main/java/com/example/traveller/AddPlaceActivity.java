package com.example.traveller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.maps.model.LatLng;

public class AddPlaceActivity extends AppCompatActivity {

    private String userName;
    private LatLng loc;
    private boolean isMonument=false;
    private boolean isDoctor=false;
    private boolean isCoffeeShop=false;
    private boolean isRestaurant=false;
    private boolean isTravelAgency=false;
    String imgUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent i=getIntent();
        String type=i.getStringExtra("type");
        userName=i.getStringExtra("userName");
        double lat=i.getDoubleExtra("latitude",0.0);
        double lon=i.getDoubleExtra("longitude",0.0);
        loc=new LatLng(lat,lon);

        //"Monument", "Coffee Shop", "Doctor", "Restaurant","Travel Agency"
        switch (type) {
            case "Monument":
                setContentView(R.layout.activity_add_monument);
                isMonument=true;
                break;
            case "Coffee Shop":
                setContentView(R.layout.layout_add_coffee_shop);
                isCoffeeShop=true;
                break;
            case "Doctor":
                setContentView(R.layout.layout_add_doctor);
                isDoctor=true;
                break;
            case "Restaurant":
                setContentView(R.layout.layout_add_restaurant);
                isRestaurant=true;
                break;
            case "Travel Agency":
                setContentView(R.layout.layout_add_travel_agency);
                isTravelAgency=true;
                break;
        }

        EditText etPlaceName=findViewById(R.id.editTextPlaceName);
        EditText etPlaceDesc=findViewById(R.id.editTextPlaceDesc);
        EditText etPlaceAddress=findViewById(R.id.editTextPlaceAdress);
        ImageButton imgBtn=findViewById(R.id.imgBtnCamera);
        Button btnSave=findViewById(R.id.btnAddPlace);
        Button btnCancel=findViewById(R.id.btnCancelPlace);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}