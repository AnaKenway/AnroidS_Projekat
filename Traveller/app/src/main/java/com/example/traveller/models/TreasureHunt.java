package com.example.traveller.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TreasureHunt {

    public String name; //the name of the Treasure Hunt Challenge
    public String category; //can be local, all treasures in one city/area
                            //or world, treasures scattered around the globe
    public String description; //short description of the treasure hunt
    public ArrayList<String> treasures; //list of (for now) treasure names that are a part of this treasure hunt
    //maybe this should be ArrayList<Treasure>?? i think that would be much better
    //on firebase i should keep them as Strings, just the names of treasures, but when i get them, i make them into objects
    public String imgURI; //some kind of picture/icon that represents this treasure hunt
    public Map<String, Boolean> treasureHunts = new HashMap<>();

    public TreasureHunt(){} //default constructor for retrieving from firebase db

    public TreasureHunt(String name, String category, String description, ArrayList<String> treasures, String imgURI){
        this.name=name;
        this.category=category;
        this.description=description;
        this.treasures=new ArrayList<String>();
        this.treasures.addAll(treasures);//this is the first time i'm using this, so i should check if this works
        this.imgURI=imgURI;
    }

    public TreasureHunt(String name, String category, String description, String imgURI){
        this.name=name;
        this.category=category;
        this.description=description;
        this.treasures=new ArrayList<String>();
        this.imgURI=imgURI;
    }

    public TreasureHunt(String name, String category, String description){
        this.name=name;
        this.category=category;
        this.description=description;
        this.treasures=new ArrayList<String>();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("description", description);
        result.put("category", category);

        return result;
    }


}
