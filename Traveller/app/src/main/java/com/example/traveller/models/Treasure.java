package com.example.traveller.models;

import java.io.Serializable;

public class Treasure implements Serializable {

    public String name;
    public String description; //This treasure is hidden in a place where that and that happened
    public String hint; //a hint about finding this treasure
    public String question; //a question that the user can answer and that will bring extra points
    public String answer; //the answer to the previous question
    public int points;
    //to do: add everything needed for treasure: object model, location, isHidden
    //i only vaguely know about how ARCore works, i need to find out how it keeps and describes objects
    //in order to know what other attributes to put here
    public String hostedAnchorID;
    public long updatedAtTimestamp;
    public int roomCode;

    public Treasure(){}

    public Treasure(String name, String description, String hint, String question,String answer, int points,String id,long timestamp, int code){
        this.name=name;
        this.description=description;
        this.hint=hint;
        this.question=question;
        this.points=points;
        this.answer=answer;
        hostedAnchorID=id;
        updatedAtTimestamp=timestamp;
        roomCode=code;
    }

    public Treasure(String name, String hint, String question,String answer, int points){
        this.name=name;
        this.hint=hint;
        this.question=question;
        this.answer=answer;
        this.points=points;
        this.hostedAnchorID="";
    }
}
