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

import com.example.traveller.CloudAnchorActivity;
import com.google.common.base.Preconditions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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



  // Names of the nodes used in the Firebase Database
  private static final String ROOT_FIREBASE_HOTSPOTS = "hotspot_list";
  private static final String ROOT_LAST_ROOM_CODE = "last_room_code";
  private static final String ROOT_TREASURES = "treasures";
  private static final String ROOT_USERS="users";

  // Some common keys and values used when writing to the Firebase Database.
  private static final String KEY_DISPLAY_NAME = "display_name";
  private static final String KEY_ANCHOR_ID = "hostedAnchorID";
  private static final String KEY_TIMESTAMP = "updated_at_timestamp";
  private static final String DISPLAY_NAME_VALUE = "Android EAP Sample";

  private final FirebaseApp app;
  //private final DatabaseReference hotspotListRef;
  //private final DatabaseReference roomCodeRef;
  private DatabaseReference treasuresRef;
  private DatabaseReference usersRef;
  private DatabaseReference currentTreasureRef = null;
  private ValueEventListener currentTreasureListener = null;

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
}
