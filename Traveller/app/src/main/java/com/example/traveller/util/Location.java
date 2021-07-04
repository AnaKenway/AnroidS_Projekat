package com.example.traveller.util;

public class Location {
    private String latitude;
    private String longitude;

    public Location(){}

    public Location(String la, String lo){
        latitude=la;
        longitude=lo;
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
}
