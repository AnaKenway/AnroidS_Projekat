package com.example.traveller.models;

import com.example.traveller.util.Location;
import com.example.traveller.util.Type;

import java.util.ArrayList;
import java.util.List;

public class Doctor extends Place {
    private String fieldOfWork;
    private String workingHours;
    private String phoneNumber;
    private List<String> services;

    public Doctor(){}

    public Doctor(String n, String d, String a, Location l, float rating, Type t,
                  String f, String w, String phone){
        super(n,d,a,l,rating,t);
        fieldOfWork=f;
        workingHours=w;
        phoneNumber=phone;
        services=new ArrayList<String>();
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

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }
}
