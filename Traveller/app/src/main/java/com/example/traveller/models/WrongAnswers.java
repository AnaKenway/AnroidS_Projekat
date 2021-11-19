package com.example.traveller.models;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**Class that will be used to store the wrong answers from
 * the firebase database
 * */
public class WrongAnswers {

    public ArrayList<String> listOfAnswers;

    public WrongAnswers(){
        listOfAnswers=new ArrayList<>();
    }

    public WrongAnswers(ArrayList<String> listOfAnswers){
        this.listOfAnswers=listOfAnswers;
    }

}
