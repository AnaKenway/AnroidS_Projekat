package com.example.traveller.models;

import com.example.traveller.util.Location;
import com.example.traveller.util.Type;

public class CoffeeShop extends Place{
    private String visitingHours;
    private boolean petFriendly;
    private String phoneNumber;
    private boolean hasWifi;
    private boolean cardPay;

    public CoffeeShop(){}

    public CoffeeShop(String n, String d, String a, String lat,String lon,  float rating, String imgUrl,
                      String v, boolean p, String phone, boolean wifi, boolean card){
        super(n,d,a,lat,lon,rating,imgUrl);
        visitingHours=v;
        petFriendly=p;
        phoneNumber=phone;
        hasWifi=wifi;
        cardPay=card;
    }


    public String getVisitingHours() {
        return visitingHours;
    }

    public void setVisitingHours(String visitingHours) {
        this.visitingHours = visitingHours;
    }

    public boolean isPetFriendly() {
        return petFriendly;
    }

    public void setPetFriendly(boolean petFriendly) {
        this.petFriendly = petFriendly;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isHasWifi() {
        return hasWifi;
    }

    public void setHasWifi(boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    public boolean isCardPay() {
        return cardPay;
    }

    public void setCardPay(boolean cardPay) {
        this.cardPay = cardPay;
    }
}
