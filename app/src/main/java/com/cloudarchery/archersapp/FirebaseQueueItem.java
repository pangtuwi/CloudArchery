package com.cloudarchery.archersapp;

import org.json.JSONObject;

/**
 * Created by paulwilliams on 02/02/15.
 */
public class FirebaseQueueItem {

    private int ID;
    private String firebaseRef;
    private JSONObject JSONItem;

    public FirebaseQueueItem() {
    }

    public FirebaseQueueItem(int newID, String newFirebaseRef, JSONObject newJSONItem) {
        super();
        this.ID = newID;
        this.firebaseRef = newFirebaseRef;
        this.JSONItem = newJSONItem;
    }

    //getters
    public int getID() {
        return ID;
    }
    public String getFirebaseRef() {
        return firebaseRef;
    }
    public JSONObject getJSONItem() {
        return JSONItem;
    }
    public String getString () {return firebaseRef+"   set to  "+JSONItem.toString();}

    //& setters
    public void setID(int ID) {
        this.ID = ID;
    }
    public void setFirebaseRef(String firebaseRef) {
        this.firebaseRef = firebaseRef;
    }
    public void setJSONItem(JSONObject JSONItem) {
        this.JSONItem = JSONItem;
    }


}