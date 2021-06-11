package com.example.traveller.models;

import com.example.traveller.util.Location;
import com.example.traveller.util.Type;

public class Monument extends Place {
    String age;

    public Monument(){}

    public Monument(String n, String d, String a, Location l, float rating, Type t, String age){
        super(n,d,a,l,rating,t);
        this.age=age;
    }
}
