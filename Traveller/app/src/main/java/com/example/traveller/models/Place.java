package com.example.traveller.models;

import com.example.traveller.util.Location;
import com.example.traveller.util.Type;

import java.util.ArrayList;
import java.util.List;

public abstract class Place {
    protected String name;
    protected String description;
    protected String address;
    protected Location location;
    protected float rating;
    protected Type type;
    protected List<String> imgUrls;

    public Place(){}

    public Place(String n, String d, String a, Location l, float rating, Type t){
        name=n;
        description=d;
        address=a;
        location=l;
        this.rating = rating;
        type=t;
        imgUrls=new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<String> getImgUrls() {
        return imgUrls;
    }

    public void setImgUrls(List<String> imgUrls) {
        this.imgUrls = imgUrls;
    }
}
