package com.example.traveller.models;

public class Treasure {

    public String name;
    public String hint; //a hint about finding this treasure
    public String question; //a question that the user can answer and that will bring extra points
    //to do: add everything needed for treasure: object model, location, isHidden
    //i only vaguely know about how ARCore works, i need to find out how it keeps and describes objects
    //in order to know what other attributes to put here

    public Treasure(String name, String hint, String question){
        this.name=name;
        this.hint=hint;
        this.question=question;
    }
}
