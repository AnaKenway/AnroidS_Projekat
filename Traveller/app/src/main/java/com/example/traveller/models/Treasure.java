package com.example.traveller.models;

public class Treasure {

    public String name;
    public String hint; //a hint about finding this treasure
    //to do: add everything needed for treasure: object model, location, isHidden
    //i only vaguely know about how ARCore works, i need to find out how it keeps and describes objects
    //in order to know what other attributes to put here

    public Treasure(String name, String hint){
        this.name=name;
        this.hint=hint;
    }
}
