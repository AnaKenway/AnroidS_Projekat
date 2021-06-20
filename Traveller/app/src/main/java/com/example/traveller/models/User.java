package com.example.traveller.models;

import com.example.traveller.util.Location;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class User {
    public String username;
    public String password;
    public String email;
    public String firstName;
    public String lastName;
    public String imgUrl;
    public String phoneNumber;
    //public Location myLocation;
    public String latitude;
    public String longitude;
    public List<String> friends;
    public List<Location> visited;
    public List<Location> favoriteLocation;
    public String points;

    public User(){}

    //ovde izbrisati lokaciju, ako slucajno treba naknadno da se doda lokacija
    //koristiti get i set za my Location
    public User(String u, String p, String e, String f, String l, String i, String ph,String lat, String lon, String points){
        username=u;
        password=p;
        email=e;
        firstName=f;
        lastName=l;
        imgUrl=i;
        phoneNumber=ph;
        //myLocation=loc;
        setLatitude(lat);
        setLongitude(lon);
        friends=new ArrayList<String>();
        visited= new ArrayList<Location>();
        favoriteLocation=new ArrayList<Location>();
        this.points=points;
    }
    public User(String p, String e, String f, String l, String i, String ph,String points){
        password=p;
        email=e;
        firstName=f;
        lastName=l;
        imgUrl=i;
        phoneNumber=ph;
        friends=new ArrayList<String>();
        visited= new ArrayList<Location>();
        favoriteLocation=new ArrayList<Location>();
        setLatitude("");
        setLongitude("");
        this.points=points;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<Location> getVisited() {
        return visited;
    }

    public void setVisited(List<Location> visited) {
        this.visited = visited;
    }

    public List<Location> getFavoriteLocation() {
        return favoriteLocation;
    }

    public void setFavoriteLocation(List<Location> favoriteLocation) {
        this.favoriteLocation = favoriteLocation;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

//    public Location getMyLocation() {
//        return myLocation;
//    }
//
//    public void setMyLocation(Location myLocation) {
//        this.myLocation = myLocation;
//    }
}
