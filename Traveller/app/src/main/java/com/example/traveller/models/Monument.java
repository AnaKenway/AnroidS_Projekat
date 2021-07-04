package com.example.traveller.models;

import com.example.traveller.util.Location;
import com.example.traveller.util.Type;

public class Monument extends Place {
    String age;

    public Monument(){}

    public Monument(String n, String d, String a, String lat,String lon, float rating, String age, String imgUrl){
        super(n,d,a,lat,lon,rating,imgUrl);
        this.age=age;
    }
}
