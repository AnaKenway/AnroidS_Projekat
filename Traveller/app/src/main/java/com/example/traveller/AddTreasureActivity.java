package com.example.traveller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AddTreasureActivity extends AppCompatActivity {

    //u ovom activity se nece upisivati treasure u bazu
    //bice upisan tek kad se doda ceo treasure hunt
    //ova activity kao rezultat vraca objekat Treasure

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_treasure);
    }
}