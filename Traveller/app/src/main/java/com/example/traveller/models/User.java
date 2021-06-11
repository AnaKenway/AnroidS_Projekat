package com.example.traveller.models;

import com.example.traveller.util.Location;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String imgUrl;
    private String phoneNumber;
    private Location myLocation;
    private List<String> friends;
    private List<Location> visited;
    private List<Location> favoriteLocation;

    public User(){}

    //ovde izbrisati lokaciju, ako slucajno treba naknadno da se doda lokacija
    //koristiti get i set za my Location
    public User(String u, String p, String e, String f, String l, String i, String ph, Location loc){
        username=u;
        password=p;
        email=e;
        firstName=f;
        lastName=l;
        imgUrl=i;
        phoneNumber=ph;
        myLocation=loc;
        friends=new ArrayList<String>();
        visited= new ArrayList<Location>();
        favoriteLocation=new ArrayList<Location>();
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

    public Location getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(Location myLocation) {
        this.myLocation = myLocation;
    }
}
