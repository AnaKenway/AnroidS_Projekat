/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.traveller.common.cloudanchor;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.traveller.CloudAnchorActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.base.Preconditions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/** A helper class to manage all communications with Firebase. */
public class FirebaseManager {
  private static final String TAG =
      CloudAnchorActivity.class.getSimpleName() + "." + FirebaseManager.class.getSimpleName();

  /** Listener for a new room code. */
  public interface RoomCodeListener {

    /** Invoked when a new room code is available from Firebase. */
    void onNewRoomCode(/*Long newRoomCode*/);

    /** Invoked if a Firebase Database Error happened while fetching the room code. */
    void onError(DatabaseError error);
  }

  /** Listener for a new cloud anchor ID. */
  public interface CloudAnchorIdListener {

    /** Invoked when a new cloud anchor ID is available. */
    void onNewCloudAnchorId(String cloudAnchorId);
  }

  /** Listener for getting completed treasure hunts*/
  public interface CompletedTreasureHuntsListener{

    /** Invoked when the list of completed treasure hunts is available*/
    void onCompletedTreasureHunts(ArrayList<String> completedTHs);
  }

  /** Listener for an Active Treasure Hunt*/
  public interface ActiveTreasureHuntListener{

    /** Invoked when the list of completed treasure hunts is available*/
    void onActiveTreasureHunt(String activeTH);
  }

  /** Listener for the list of found treasures of the active treasure hunt*/
  public interface FoundTreasuresListener{

    /** Invoked when the list of found treasures is available*/
    void onFoundTreasures(ArrayList<String> foundTreasures);
  }

  /** Listener for the list of treasures of a treasure hunt*/
  public interface TreasuresOfATreasureHuntListener{

    /** Invoked when the list of treasures is available*/
    void onTreasuresOfATreasureHunt(ArrayList<String> thTreasures);
  }

  /** Listener for the question for a treasure*/
  public interface TreasureQuestionListener{
    void onTreasureQuestion(String tQuestion);
  }

  /** Listener for the answer of a treasure question */
  public interface TreasureAnswerListener{
    void onTreasureAnswer(String tAnswer);
  }

  /** Listener for the points a treasure brings */
  public interface TreasurePointsListener{
    void onTreasurePoints(int points);
  }


  // Names of the nodes used in the Firebase Database
  private static final String ROOT_FIREBASE_HOTSPOTS = "hotspot_list";
  private static final String ROOT_LAST_ROOM_CODE = "last_room_code";
  private static final String ROOT_TREASURES = "treasures";
  private static final String ROOT_USERS="users";
  private static final String ROOT_TREASURE_HUNT="treasureHunts";
  private static final String USER_COMPLETED_TREASURE_HUNTS="completedTreasureHunts";
  private static final String USER_ACTIVE_TREASURE_HUNT="activeTreasureHunt";
  private static final String USER_FOUND_TREASURES="foundTreasures";
  private static final String TREASURE_QUESTION="question";
  private static final String TREASURE_ANSWER="answer";
  private static final String TREASURE_POINTS="points";

  // Some common keys and values used when writing to the Firebase Database.
  private static final String KEY_DISPLAY_NAME = "display_name";
  private static final String KEY_ANCHOR_ID = "hostedAnchorID";
  private static final String KEY_TIMESTAMP = "updated_at_timestamp";
  private static final String DISPLAY_NAME_VALUE = "Android EAP Sample";

  private final FirebaseApp app;
  //private final DatabaseReference hotspotListRef;
  //private final DatabaseReference roomCodeRef;
  private DatabaseReference treasuresRef;
  private DatabaseReference treasureHuntsRef;
  private DatabaseReference usersRef;
  private DatabaseReference currentTreasureRef = null;
  private ValueEventListener currentTreasureListener = null;
  private int points=0;

  /**
   * Default constructor for the FirebaseManager.
   *
   * @param context The application context.
   */
  public FirebaseManager(Context context) {
    app = FirebaseApp.initializeApp(context);
    if (app != null) {
      DatabaseReference rootRef = FirebaseDatabase.getInstance(app).getReference();
      //hotspotListRef = rootRef.child(ROOT_FIREBASE_HOTSPOTS);
      //roomCodeRef = rootRef.child(ROOT_LAST_ROOM_CODE);
      treasuresRef=rootRef.child(ROOT_TREASURES);
      usersRef=rootRef.child(ROOT_USERS);
      treasureHuntsRef=rootRef.child(ROOT_TREASURE_HUNT);

      DatabaseReference.goOnline();
    } else {
      Log.d(TAG, "Could not connect to Firebase Database!");
      //hotspotListRef = null;
      //roomCodeRef = null;
    }
  }

  /**
   * Gets a new room code from the Firebase Database. Invokes the listener method when a new room
   * code is available.
   */
  public void getNewRoomCode(RoomCodeListener listener) {
    /*Preconditions.checkNotNull(app, "Firebase App was null");
    roomCodeRef.runTransaction(
        new Transaction.Handler() {
          @Override
          public Transaction.Result doTransaction(MutableData currentData) {
            Long nextCode = Long.valueOf(1);
            Object currVal = currentData.getValue();
            if (currVal != null) {
              Long lastCode = Long.valueOf(currVal.toString());
              nextCode = lastCode + 1;
            }
            currentData.setValue(nextCode);
            return Transaction.success(currentData);
          }

          @Override
          public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
            if (!committed) {
              listener.onError(error);
              return;
            }
            Long roomCode = currentData.getValue(Long.class);
            listener.onNewRoomCode(/*roomCode*/ //);
         // }
        //});
    listener.onNewRoomCode(/*roomCode*/);
  }

  /** Stores the given anchor ID in the given room code. */
  public void storeAnchorIdInRoom(Long roomCode, String cloudAnchorId) {
    Preconditions.checkNotNull(app, "Firebase App was null");
    //DatabaseReference roomRef = hotspotListRef.child(String.valueOf(roomCode));
    //roomRef.child(KEY_DISPLAY_NAME).setValue(DISPLAY_NAME_VALUE);
    //roomRef.child(KEY_ANCHOR_ID).setValue(cloudAnchorId);
    //roomRef.child(KEY_TIMESTAMP).setValue(System.currentTimeMillis());
  }

  /**
   * Registers a new listener for the given treasure name. The listener is invoked whenever the data for
   * the treasure name is changed.
   */
  public void registerNewListenerForTreasureName(String treasureName, CloudAnchorIdListener listener) {
    Preconditions.checkNotNull(app, "Firebase App was null");
    clearRoomListener();
    currentTreasureRef = treasuresRef.child(treasureName);
    currentTreasureListener =
        new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            Object valObj = dataSnapshot.child(KEY_ANCHOR_ID).getValue();
            if (valObj != null) {
              String anchorId = String.valueOf(valObj);
              if (!anchorId.isEmpty()) {
                listener.onNewCloudAnchorId(anchorId);
              }
            }
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "The Firebase operation was cancelled.", databaseError.toException());
          }
        };
    currentTreasureRef.addValueEventListener(currentTreasureListener);
  }

  /**
   * Resets the current room listener registered using {@link #registerNewListenerForTreasureName(String,
   * CloudAnchorIdListener)}.
   */
  public void clearRoomListener() {
    if (currentTreasureListener != null && currentTreasureRef != null) {
      currentTreasureRef.removeEventListener(currentTreasureListener);
      currentTreasureListener = null;
      currentTreasureRef = null;
    }
  }

  /** Gets the list of all completed treasure hunts for the user with username
   * @param username*/
  public void getCompletedTreasureHunts(String username, CompletedTreasureHuntsListener listener){

    usersRef.child(username).child(USER_COMPLETED_TREASURE_HUNTS).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Object o=snapshot.getValue();
        if(o==null) return;
        ArrayList<String> completedTreasureHunts=(ArrayList<String>)o;
        listener.onCompletedTreasureHunts(completedTreasureHunts);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "The Firebase operation was cancelled.", error.toException());
      }
    });
  }

  /** Gets the active treasure hunt for the user with username
   * @param username*/
  public void getActiveTreasureHunt(String username, ActiveTreasureHuntListener listener){

    usersRef.child(username).child(USER_ACTIVE_TREASURE_HUNT).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Object o=snapshot.getValue();
        if(o==null || o.equals("")) {
          listener.onActiveTreasureHunt(null);
          return;
        }
        else {
          String activeTHName = (String) o;
          listener.onActiveTreasureHunt(activeTHName);
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "The Firebase operation was cancelled.", error.toException());
      }
    });
  }

  public void getFoundTreasures(String username, FoundTreasuresListener listener) {

    usersRef.child(username).child(USER_FOUND_TREASURES).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
      @Override
      public void onSuccess(@NonNull DataSnapshot snapshot) {
        Object o = snapshot.getValue();
        if (o == null) return;
        ArrayList<String> foundTreasures = (ArrayList<String>) o;
        listener.onFoundTreasures(foundTreasures);
      }

      /*@Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "The Firebase operation was cancelled.", error.toException());
      }
    });*/
    });
  }

  public void getTreasuresOfATreasureHunt(String tHuntName, TreasuresOfATreasureHuntListener listener ){
    treasureHuntsRef.child(tHuntName).child("treasures").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
      @Override
      public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
          Object o=dataSnapshot.getValue();
          if(o==null) return;
          ArrayList<String> treasures=new ArrayList<>();
          treasures=(ArrayList<String>) o;
          listener.onTreasuresOfATreasureHunt(treasures);
      }
    });
  }

  public void ActivateTreasureHunt(String username, String treasureHuntName){
    usersRef.child(username).child(USER_ACTIVE_TREASURE_HUNT).setValue(treasureHuntName);
  }

  public void DeactivateTreasureHunt(String username, String treasureHuntName){
    usersRef.child(username).child(USER_ACTIVE_TREASURE_HUNT).setValue("");
    usersRef.child(username).child(USER_FOUND_TREASURES).removeValue();
  }

  public void getTreasureQuestion(String tName, TreasureQuestionListener listener){
    treasuresRef.child(tName).child(TREASURE_QUESTION).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Object o=snapshot.getValue();
        if(o==null) return;
        String question=(String)o;
        if(question.equals("")) return;
        listener.onTreasureQuestion(question);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "The Firebase operation was cancelled.", error.toException());
      }
    });
  }

  public void getTreasureAnswer(String tName, TreasureAnswerListener listener){
    treasuresRef.child(tName).child(TREASURE_ANSWER).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Object o=snapshot.getValue();
        if(o==null) return;
        String answer=(String)o;
        if(answer.equals("")) return;
        listener.onTreasureAnswer(answer);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "The Firebase operation was cancelled.", error.toException());
      }
    });
  }

  public void getTreasurePoints(String tName, TreasurePointsListener listener){
    treasuresRef.child(tName).child(TREASURE_POINTS).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Object o=snapshot.getValue();
        if(o==null) return;
        Long p=(Long)o;
        points=p.intValue();
        listener.onTreasurePoints(points);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "The Firebase operation was cancelled.", error.toException());
      }
    });
  }

  public void addPointsToUser(String username,int pointsToAdd){
    //first get the number of points the user has now
    //and then inside that onSuccess, add the pointsToAdd and write back to database
    usersRef.child(username).child("points").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
      @Override
      public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
        Object o=dataSnapshot.getValue();
        if(o==null) return;
        String points=(String)o;
        if(points.equals("")) return;
        int numPoints=Integer.parseInt(points);
        numPoints+=pointsToAdd;

        usersRef.child(username).child("points").setValue(String.valueOf(numPoints));
      }
    });
  }

  public void addTreasureToFoundTreasures(String username, String treasureName){
    usersRef.child(username).child(USER_FOUND_TREASURES).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
      @Override
      public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
        if(dataSnapshot.getValue()==null){
          //znaci da je ovo prvi treasure iz ovog treasure hunt-a koji je korisnik nasao
          ArrayList<String> foundTreasures=new ArrayList<>();
          foundTreasures.add(treasureName);
          usersRef.child(username).child(USER_FOUND_TREASURES).setValue(foundTreasures);
        } else{
          //ako nije null, znaci da vec postoje nadjeni treasures, i treba dodati ovaj u listu
          Object o=dataSnapshot.getValue();
          ArrayList<String> foundTreasures=(ArrayList<String>)o;
          foundTreasures.add(treasureName);
          usersRef.child(username).child(USER_FOUND_TREASURES).setValue(foundTreasures);
        }
      }
    });
  }

  public void makeTreasureHuntCompleted(String username, ArrayList<String> listOfAllCompletedTHs){
    //funkciji prosledimo listu svih completed treasure hunts
    //a ona samo upise tu listu u bazu
    usersRef.child(username).child(USER_COMPLETED_TREASURE_HUNTS).setValue(listOfAllCompletedTHs);
  }

}
