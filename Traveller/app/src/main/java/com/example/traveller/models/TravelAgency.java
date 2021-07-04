package com.example.traveller.models;

import com.example.traveller.util.Location;
import com.example.traveller.util.Type;

public class TravelAgency extends Place{
    private String visitingHours;
    private String phoneNumber;

    public TravelAgency(){}

    public TravelAgency(String n, String d, String a, String lat, String lon , float rating, String imgUrl,
                        String v, String p){
        super(n,d,a,lat,lon,rating,imgUrl);
        visitingHours=v;
        phoneNumber=p;
    }

    public String getVisitingHours() {
        return visitingHours;
    }

    public void setVisitingHours(String visitingHours) {
        this.visitingHours = visitingHours;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
