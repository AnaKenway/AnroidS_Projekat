package com.example.traveller.models;

import com.example.traveller.util.Location;
import com.example.traveller.util.Type;

import java.util.ArrayList;
import java.util.List;

public class Doctor extends Place {
    private String fieldOfWork;
    private String workingHours;
    private String phoneNumber;

    public Doctor(){}

    public Doctor(String n, String d, String a, String lat,String lon, float rating, String imgUrl,
                  String f, String w, String phone){
        super(n, d, a, lat, lon, rating,imgUrl);
        fieldOfWork=f;
        workingHours=w;
        phoneNumber=phone;
    }

    public String getFieldOfWork() {
        return fieldOfWork;
    }

    public void setFieldOfWork(String fieldOfWork) {
        this.fieldOfWork = fieldOfWork;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
