package com.example.traveller.models;

import java.io.Serializable;
import java.util.ArrayList;

public class Treasure implements Serializable {

    public String name;
    public String description; //This treasure is hidden in a place where that and that happened
    public String hint; //a hint about finding this treasure
    public String question; //a question that the user can answer and that will bring extra points
    public String answer; //the answer to the previous question
    public ArrayList<String> wrongAnswers=new ArrayList<>(); //a list of wrong answers that will be on the quiz
    public int points;
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
        wrongAnswers=new ArrayList<>();
    }

    public Treasure(String name, String hint, String question,String answer, int points){
        this.name=name;
        this.hint=hint;
        this.question=question;
        this.answer=answer;
        this.points=points;
        this.hostedAnchorID="";
        wrongAnswers=new ArrayList<>();
    }
}
