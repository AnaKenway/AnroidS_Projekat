package com.example.traveller.models;

import com.example.traveller.util.Location;
import com.example.traveller.util.Type;

import java.util.ArrayList;
import java.util.List;

public abstract class Place {
    protected String name;
    protected String description;
    protected String address;
    protected String latitude;
    protected String longitude;
    protected float rating;
    protected List<String> imgUrls;
    protected String imgUrl;
    protected int numOfRaters;

    public Place(){}

    public Place(String n, String d, String a, String lat,String lon, float rating,String imgUrl){
        name=n;
        description=d;
        address=a;
        latitude=lat;
        longitude = lon;
        this.rating = rating;
        imgUrls=new ArrayList<String>();
        this.imgUrl = imgUrl;
        numOfRaters = 0;
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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public List<String> getImgUrls() {
        return imgUrls;
    }

    public void setImgUrls(List<String> imgUrls) {
        this.imgUrls = imgUrls;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getNumOfRaters() {
        return numOfRaters;
    }

    public void setNumOfRaters(int numOfRaters) {
        this.numOfRaters = numOfRaters;
    }
}
