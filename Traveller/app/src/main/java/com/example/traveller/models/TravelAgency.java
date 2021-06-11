package com.example.traveller.models;

import com.example.traveller.util.Location;
import com.example.traveller.util.Type;

public class TravelAgency extends Place{
    private String visitingHours;
    private String phoneNumber;

    public TravelAgency(){}

    public TravelAgency(String n, String d, String a, Location l, float rating, Type t,
                        String v, String p){
        super(n,d,a,l,rating,t);
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
