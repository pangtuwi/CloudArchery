package com.cloudarchery.archersapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by paulwilliams on 22/03/15.
 */
public class ClubFirebase {
    //Contains all firebase interaction

    Context myContext;
    //MyApp myAppState;

    boolean network = false;
    boolean syncOn = false;
    boolean connected = false;
    boolean authenticated = false;
    boolean linked = false;
    String clubURL = "";
    String clubID = "";
    String email = "";
    String password = "";
    JSONObject myClubInfoJSON;

    String userID = "LDS_Only"; //Temp setting, correct in oncreate
    String name = "";
    String firebaseError = "";
    String clubWebURL = "";
    int deQueueingItem = -1;
    int deQueueDelay = 250;

    boolean syncError  = false;

    SQLiteFirebaseQueue fbQ;

    SharedPreferences mSharedPreferences;
    Firebase myCentralFirebaseRef;
    Firebase myClubFirebaseRef;

    //firebase Listeners
    ValueEventListener leaderBoardListener;
    ValueEventListener roundsListener;
    ValueEventListener firebaseJoinableRoundListener;

    //Local App Listeners
    OnUserScoreUpdatedListener myScoreListener;
    OnRoundsListUpdatedListener myRoundsListListener;
    OnConnectionListener myConnectionListener;
    OnJoinableRoundChangedListener myJoinableRoundListener;
    OnRoundTypesUpdatedListener myRoundTypesUpdatedListener;
    OnClubStatsUpdatedListener myClubStatsUpdatedListener;

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    ClubFirebase(){
        myContext = MyApp.getAppContext();
        Firebase.setAndroidContext(myContext);
        myCentralFirebaseRef = new Firebase(myContext.getString(R.string.firebase_central_url));
        fbQ = new SQLiteFirebaseQueue(myContext);
    }

    public void initialise( boolean activityIsVisible) {
        Log.d("CloudArchery ", "initialising CDS...");
        if (activityIsVisible) {

            connected = false;
            authenticated = false;
            linked = false;
            network = isNetworkAvailable();
            if (myConnectionListener != null) {
                if (network) {
                    myConnectionListener.onConnectionUpdated(syncOn, network, null, null, null, "Active Internet connection found");
                } else {
                    myConnectionListener.onConnectionUpdated(syncOn, network, null, null, null, "No internet connection");
                }
            }


            mSharedPreferences = myContext.getSharedPreferences(myContext.getString(R.string.PREFS), myContext.MODE_PRIVATE);
            syncOn = mSharedPreferences.getBoolean(myContext.getString(R.string.PREF_SYNC), false);
            userID = mSharedPreferences.getString(myContext.getString(R.string.PREF_USERID), "");
            clubURL = mSharedPreferences.getString(myContext.getString(R.string.PREF_CLUBURL), "");
            clubWebURL = mSharedPreferences.getString(myContext.getString(R.string.PREF_CLUBWEBURL), "http://cloudarchery.com");
            clubID = mSharedPreferences.getString(myContext.getString(R.string.PREF_CLUBID), "");
            email = mSharedPreferences.getString(myContext.getString(R.string.PREF_EMAIL), "");
            password = mSharedPreferences.getString(myContext.getString(R.string.PREF_PASSWORD), "");
            name = mSharedPreferences.getString(myContext.getString(R.string.PREF_NAME), "Me");

            if (network && syncOn && (clubURL.equals(""))) {
                Log.d("CloudArchery ", "network on, sync On, clubURL is blank");


                //ToDo : (RELEASE > 2) Clean up existing Listeners?
                String childRef = "clubs/" + clubID + "/firebaseURL";
                Log.d("CloudArchery", "Looking for clubURL at " + childRef);
                myCentralFirebaseRef.child(childRef).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            try {
                                Log.d("CloudArchery ", "matched clubID in Central database");
                                clubURL = dataSnapshot.getValue().toString();

                                //save it to shared preferences
                                SharedPreferences.Editor e = mSharedPreferences.edit();
                                e.putString(myContext.getString(R.string.PREF_CLUBURL), clubURL);
                                Log.d("CloudArchery ", "got club URL");
                                e.commit();
                                checkConnected();
                            } catch (Throwable t) {

                                Log.e("CloudArchery", "Could not read ClubURL from central firebase (clubFirebase.OnCreate) ");
                                t.printStackTrace();
                            }
                        } else {
                            if (myConnectionListener != null)
                                myConnectionListener.onConnectionUpdated(syncOn, network, connected, null, null, "Cannot find Club Database");
                        } //if...else
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        if (myConnectionListener != null)
                            myConnectionListener.onConnectionUpdated(syncOn, network, connected, null, null, "Cannot find Club Database");
                    }
                });

                String WebRef = "clubs/" + clubID + "/webURL";
                myCentralFirebaseRef.child(WebRef).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            try {
                                clubWebURL = dataSnapshot.getValue().toString();
                                SharedPreferences.Editor e = mSharedPreferences.edit();
                                e.putString(myContext.getString(R.string.PREF_CLUBWEBURL), clubWebURL);
                                e.commit();
                                Log.d("CloudArchery ", "got club Web URL");
                            } catch (Throwable t) {
                                Log.e("CloudArchery", "Could not read ClubWebURL from central firebase (clubFirebase.OnCreate) ");
                                t.printStackTrace();
                            }
                        } else {
                        }
                    } //onDataChange

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
            } else if (network && syncOn && (!clubURL.equals(""))) {
                checkConnected();
            } // if

            String clubRef = "clubs/" + clubID;
            myCentralFirebaseRef.child(clubRef).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        try {
                            Map<String, JSONObject> myClubInfoJSONMap = (HashMap<String, JSONObject>) dataSnapshot.getValue();
                            myClubInfoJSON = new JSONObject(myClubInfoJSONMap);
                        } catch (Throwable t) {
                            Log.e("CloudArchery", "Could not read Club information from central firebase (clubFirebase.OnCreate) ");
                            t.printStackTrace();
                        }
                    } else {
                    } //if...else
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    if (myConnectionListener != null)
                        myConnectionListener.onConnectionUpdated(syncOn, network, connected, null, null, "Cannot find Club Database");
                }
            });
        }

    }//initialise

    public void checkConnected () {
        if (syncOn) {
            Log.d("CloudArchery ", "sync is on, checking conection");
            try {
                myClubFirebaseRef = new Firebase(clubURL);
            } catch (Throwable t) {
                firebaseError = "IncorrectClubURL";
                Log.e ("CloudArchery", "incorrect Club URL");
                t.printStackTrace();
            }
            try {


                myClubFirebaseRef.child(".info/connected").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean isConnected = snapshot.getValue(Boolean.class);
                        if (isConnected) {
                            //System.out.println("connected");
                            Log.d("CloudArchery ", "connected set true");
                            connected = true;
                            if (myConnectionListener != null)
                                myConnectionListener.onConnectionUpdated(syncOn, network, connected, null, null, "");
                            verifyLogin();
                        } else {
                            //System.out.println("not connected");
                            connected = false;
                            Log.d("CloudArchery ", "connected set false");
                            if (myConnectionListener != null)
                                myConnectionListener.onConnectionUpdated(syncOn, network, connected, null, null, "No connection to Cloud Database");
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                        //System.err.println("Listener was cancelled");
                        connected = false;
                        Log.d("CloudArchery ", "connected set false as listener cancelled");
                        if (myConnectionListener != null)
                            myConnectionListener.onConnectionUpdated(syncOn, network, connected, null, null, "No connection to Cloud Database");
                    }
                });
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("CloudArchery", "Exception while checking connection ");
            }
        } //if SyncOn
    } //checkConnected

    public void verifyLogin (){
        Log.d("CloudArchery ", "verifying login... Sync is "+ syncOn+ "   , connected is "+ connected);
        if (syncOn && connected) {
            //imageViewLoggedIn = (ImageView) findViewById(R.id.imageViewLoggedIn);
            Log.d("CloudArchery ", "sync on");
            myClubFirebaseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    Log.d("CloudArchery ", "authenticated set true");
                    authenticated = true;
                    if (myConnectionListener != null)
                        myConnectionListener.onConnectionUpdated(syncOn, network, connected, authenticated, linked, "");
                    verifyUserName();
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    authenticated = false;
                    Log.d("CloudArchery ", "authenticated set false");
                    if (myConnectionListener != null)
                        myConnectionListener.onConnectionUpdated(syncOn, network, connected, authenticated, linked, "Authentication parameters incorrect.  Contact your club administrator.");
                }
            });
        } //if SyncOn
    } //verifyLogin

    public void verifyUserName(){
        if (syncOn && connected && authenticated) {
            Log.d("CloudArchery ", "sync on, connected, authenticated,  getting username");
            myClubFirebaseRef.child("users/").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Map<String, Object> dbUsers = (HashMap<String, Object>) snapshot.getValue();
                    if (myConnectionListener != null) myConnectionListener.onConnectionUpdated(syncOn, network, connected, authenticated, linked, "User not found in Club Database");
                    try {
                        for (String key : dbUsers.keySet()) {
                            Map<String, String> dbThisUser = (HashMap<String, String>) dbUsers.get(key);
                            String CDSEmail = dbThisUser.get("email");
                            String CDSUserID = dbThisUser.get("id");
                            String CDSName = dbThisUser.get("name");

                            if ((CDSEmail != null) & (CDSName != null) & (CDSUserID != null)) {
                                if (email.equals(CDSEmail)) {
                                    //Found user - check if CDS userID matches the one in SavedPreferences
                                    if (!userID.equals(CDSUserID)){
                                        String oldUserID = userID;
                                        userID = CDSUserID;
                                        String oldUserName = name;
                                        name = CDSName;
                                        SQLiteRounds db = new SQLiteRounds(myContext);
                                        db.changeUserID(oldUserID, userID, oldUserName, name);
                                        SharedPreferences.Editor e = mSharedPreferences.edit();
                                        e.putString(myContext.getString(R.string.PREF_NAME), name);
                                        e.putString(myContext.getString(R.string.PREF_USERID), userID);
                                        e.commit();
                                        Log.d("CloudArchery ", "New Login : updated LDS UserID from "+oldUserID + " to "+ userID);
                                    }


                                    //Toast.makeText(myContext, "Connected to Cloud Archery Database", Toast.LENGTH_LONG).show();
                                    Log.d("CloudArchery", "User with ID " + userID + " selected");
                                    linked = true;
                                    Log.d("CloudArchery", "Linked = "+linked);
                                    if (myConnectionListener != null) myConnectionListener.onConnectionUpdated(syncOn, network, connected, authenticated, linked, "");
                                    //deQueue();
                                    sync();
                                    break;
                                }
                            }
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.e("CloudArchery", "Error checking database for userName to match login email ");
                    }
                } //onDataChange

                @Override
                public void onCancelled(FirebaseError error) {
                }
            }); //addValueEventListener
        } //if syncon
    }//getUserName

    public String getUserID() {
     return userID;
    }
    public String getName() {return name;}


// - - - - - - - - - - - INTERFACE LISTENER DEFINITIONS - - - - - - - - - - //

    public interface OnUserScoreUpdatedListener {
        public void onScoreUpdated(JSONArray newScoresJSON);
        //http://developer.android.com/training/basics/fragments/communicating.html
    }

    public interface OnRoundsListUpdatedListener {
        public void onRoundsListUpdated();
    }

    public interface OnConnectionListener {
        public void onConnectionUpdated(Boolean syncOn, Boolean Network, Boolean CDSConnected, Boolean Authenticated, Boolean Linked, String ErrorMessage);
    }

    public interface OnJoinableRoundChangedListener {
        public void onJoinableRoundChanged(JSONArray joinableRoundJSONArray);
        //http://developer.android.com/training/basics/fragments/communicating.html
    }

    public interface OnRoundTypesUpdatedListener{
        public void onRoundTypesUpdated();
    }

    public interface OnClubStatsUpdatedListener {
        public void onClubStatsUpdated(JSONObject clubStatsJSON);
    }

    // - - - - - - - - - - - MAIN FUNCTIONALITY - - - - - - - - - - //


    public void copyRoundUpload(String roundID) {
        Log.d("CloudArchery", ">>> CopyRoundUpload from LDS to CDS (ClubFirebase.copyRoundUpload) : " + roundID);
        SQLiteRounds db = new SQLiteRounds(myContext);
        JSONObject newRoundJSON = db.getRoundJSON(roundID);
        String newRoundUserID = null;
        try {
            newRoundUserID = newRoundJSON.getJSONObject("creator").getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("CloudArchery", "Could not get creator ID from JSON (ClubFirebase.copyRoundupload");
        }
        if (userID.equals(newRoundUserID)) {
            long updatedAt = System.currentTimeMillis();
            try {
                updatedAt = newRoundJSON.getLong("updatedAt");
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("CloudArchery", "could not get updatedAt from LDS JSON (ClubFirebase.copyRoundUpload) ");
            }
            final JSONObject newUserRoundIndexJSONObject = new JSONObject();
            try {
                newUserRoundIndexJSONObject.put(roundID, updatedAt);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("CloudArchery", "could not write updatedAt to newUserIndexVal (ClubFirebase.copyRoundUpload) ");
            }
            CDSAddToQueue("rounds/" + roundID, newRoundJSON);

            //Create new Handler for second enqueue so that it does not happen too fast
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    CDSAddToQueue("users/" + userID + "/rounds", newUserRoundIndexJSONObject);
                }
            };

            Handler h = new Handler();
            h.postDelayed(r, deQueueDelay); // <-- the deQueuDelay is the delay time in miliseconds.
        }

    } //copyRoundUpload


    public void insertRoundDownload(final String roundID){
        myClubFirebaseRef.child("rounds/"+roundID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Map<String, JSONObject> CDSRoundJSONMap = (HashMap<String, JSONObject>) snapshot.getValue();
                        JSONObject CDSRoundJSONObject = new JSONObject(CDSRoundJSONMap);
                        SQLiteRounds db = new SQLiteRounds(myContext);
                        db.createLocalRound(roundID, CDSRoundJSONObject);
                        //copyRoundDownload(roundID);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.e("CloudArchery", "error downloading round data (clubFirebase.insertRoundDownload) ");
                    }
                } else {
                    Log.d("CloudArchery", "No Data Record found (clubFirebase.insertRoundDownload)");
                }
            } //onDataChange

            @Override
            public void onCancelled(FirebaseError error) {
                Log.e("CloudArchery", "Firebase Error (clubFirebase.copyUserDownload)");
            }
        }); //addValueEventListener
    } //insertRoundDownload


    public void copyRoundDownload (final String roundID) {

        myClubFirebaseRef.child("rounds/"+roundID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Map <String, JSONObject> CDSRoundJSONDataObject = (HashMap<String, JSONObject>)snapshot.getValue();
                        JSONObject CDSRoundJSONObject = new JSONObject(CDSRoundJSONDataObject);
                        SQLiteRounds db = new SQLiteRounds(myContext);
                        db.updateLocalRound(roundID, CDSRoundJSONObject);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.e("CloudArchery", "error downloading round data (clubFirebase.copyRoundDownload) ");
                    }
                } else {
                    Log.d("CloudArchery", "No Data Record found  (clubFirebase.copyRoundDownload)  roundID="+roundID);
                }
            } //onDataChange

            @Override
            public void onCancelled(FirebaseError error) {
                Log.e("CloudArchery", "Firebase Error (clubFirebase.copyRoundDownload)");
            }
        }); //addValueEventListener
    } //copyRoundDownload

    //ToDo : (RELEASE > 2) Create function that checks CDS/LDS JSON integrity and deals with issues found.

    public void sync() {
        StopRoundChangeListener();
        syncError = false;
        //Synchronise Process
        // (1) - read CDS for all /users/:userid/rounds = CDS_RoundIndex
        // (2) - read LDS for all rounds  = LDS_Rounds
        // (3) - iterate through CDS_RoundIndex
        // (4)  -> if not present in LDS - add to LDS
        // (5)  -> else if present in LDS, compare timestamps
        // (6)    --> if timestamp LDS < timestamp CDS, download
        // (7)    --> if timestamp LDS > timestamp CDS, upload
        // (8)  -> mark LDS Rounds item as synced
        // (9) - iterate through all LDS Rounds not marked as synced
        // (10)   -> add items to CDS (upload)

        if (syncOn && connected && authenticated && linked) {
            if (myClubFirebaseRef == null) {
                myClubFirebaseRef = new Firebase(clubURL);
            }
            // (1) - read firebase for all /users/:userid/rounds = CDS_RoundIndex
            myClubFirebaseRef.child("users/" + userID + "/rounds/").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot snapshot) {
                    long CDSUpdatedAt = 0;
                    long LDSUpdatedAt = 0;

                    // (2) - read sqlite.userJSON for all rounds  = LDS_UserData
                    SQLiteRounds db = new SQLiteRounds(myContext);
                    final Map<String, Boolean> LDSRoundMap = db.getRoundBooleanMap();

                    //DEBUG CODE ONLY
                    Log.d ("CloudArchery", "Loaded following data from LDS:");
                    Iterator debugit = LDSRoundMap.entrySet().iterator();
                    while (debugit.hasNext()) {
                        Map.Entry pair = (Map.Entry)debugit.next();
                        Log.d("CloudArchery", " ... " + pair.getKey() + "  :   " + pair.getValue());
                        //debugit.remove(); // avoids a ConcurrentModificationException
                    }
                    // END OF DEBUG CODE



                    JSONObject CDSRounds = null;  // JSON object with ALL rounds
                    if (snapshot.getValue() != null) {
                        try {
                            Map<String, JSONObject> CDSRoundMap = (HashMap<String, JSONObject>) snapshot.getValue();
                            CDSRounds = new JSONObject(CDSRoundMap);
                        } catch (Throwable t) {
                            t.printStackTrace();
                            Log.d("CloudArchery", "Firebase read error (clubFirebase.sync) - - - 1");
                        }
                    }

                    if (CDSRounds != null) {
                        Iterator<?> keys = CDSRounds.keys();
                        //(3) - iterate through CDSRounds
                        while (keys.hasNext()) {   //CDS record for userID/roundID
                            final String CDSRoundID = (String) keys.next();
                            JSONObject CDSRoundJSON = null;
                            JSONObject LDSRoundJSON = null;
                            try {
                                //extract single round user JSON
                             //   if (CDSRounds.get(CDSRoundID) instanceof JSONObject) {
                             //       CDSRoundJSON = CDSRounds.getJSONObject(CDSRoundID);
                             //   }
                            } catch (Throwable t) {
                                t.printStackTrace();
                                Log.d("CloudArchery", "Firebase read error (clubFirebase.sync)");
                            }
                            //Check if have LDS record for roundID
                            LDSRoundJSON = db.getRoundJSON(CDSRoundID);
                            if (LDSRoundJSON == null) {
                                // (4)  -> if not present in LDS - add to LDS
                                Log.d("CloudArchery","---NEW CDS RECORD DOWNLOAD -- ID="+CDSRoundID);
                                insertRoundDownload(CDSRoundID);
                                myRoundsListListener.onRoundsListUpdated(); //
                            } else {
                                // (5)  -> else if present in LDS, compare timestamps
                                // if both have timestamps - then use timestamps to compare
                                // if LDS has no timestamp, then assume new and assume that it is the master - set LDSUpdatedAt>CDSUpdatedAt
                                // if CDS has no timestamp then assume LDS is the master, set LDS LDSUpdatedAt>CDSUpdatedAt
                                try {
                                    LDSUpdatedAt = LDSRoundJSON.getLong("updatedAt");
                                } catch (Throwable t) {
                                    Log.e("SYNC ERROR", "Error getting LDS updatedAt from:"+LDSRoundJSON.toString());
                                    LDSUpdatedAt = System.currentTimeMillis();
                                    try {
                                        LDSRoundJSON.put("updatedAt", LDSUpdatedAt);
                                        db.updateLocalRound(CDSRoundID, LDSRoundJSON);
                                    } catch (Throwable t2){
                                        t2.printStackTrace();
                                        Log.e("CloudArchery", "Error putting LDS updatedAt");
                                    }
                                }
                                try {
                                    //CDSUpdatedAt = CDSRoundJSON.getLong("updatedAt");
                                    CDSUpdatedAt = (Long) CDSRounds.get(CDSRoundID);
                                } catch (Throwable t) {
                                    Log.e("SYNC ERROR", "Error getting CDS updatedAt from:"+CDSRoundJSON.toString());
                                    CDSUpdatedAt = LDSUpdatedAt-1;
                                }

                                if (LDSUpdatedAt > CDSUpdatedAt) {
                                    // (6)    --> if timestamp LDS > timestamp CDS, upload
                                    Log.d("CloudArchery", "--- SYNC UP ---"+"ID="+CDSRoundID+"  |   LDStime = "+LDSUpdatedAt + "   |   CDStime = "+CDSUpdatedAt);
                                    copyRoundUpload(CDSRoundID);
                                } else if (LDSUpdatedAt < CDSUpdatedAt) {
                                    // (7)    --> if timestamp LDS < timestamp CDS, download
                                    Log.d("CloudArchery","--- SYNC DOWN ---"+"ID="+CDSRoundID+"  |   LDStime = "+LDSUpdatedAt + "   |   CDStime = "+CDSUpdatedAt);
                                    copyRoundDownload(CDSRoundID);
                                    myRoundsListListener.onRoundsListUpdated(); //
                                } else {
                                    Log.d ("CloudArchery", "--- NO SYNC --- as LDSUpdatedAt = "+LDSUpdatedAt + " and CDSUpdatedAt = "+ CDSUpdatedAt);
                                }
                                // (8)  -> mark LDS_UserData item as synced
                                LDSRoundMap.put(CDSRoundID, true); //mark LDS Tracker as synced
                                //Log.d ("CloudArchery", "Marked round "+ CDSRoundID + " as synced (ClubFirebase.sync)");
                            }
                        }  // end iterator loop
                    } else {
                        Log.d ("CloudArchery", "ERROR : CDSRounds returned as Null (ClubFirebase.Sync)");
                        syncError = true;
                    }// if CDSRounds != null
                    // (9) - iterate through all LDS Rounds not marked as synced
                    if (!syncError) {
                        Iterator it1 = LDSRoundMap.entrySet().iterator();
                        //Boolean LDSSync = false;
                        while (it1.hasNext()) {
                            Map.Entry pair = (Map.Entry) it1.next();
                            if ((Boolean) pair.getValue() == false) {
                                // (10)   -> add items to CDS (upload)
                                //Log.d("CloudArchery", "Copying LDS round to CDS as marked not Synced (ClubFirebase.sync) : " + pair.getKey());
                                copyRoundUpload((String) pair.getKey());
                            }
                            it1.remove(); // avoids a ConcurrentModificationException
                        }
                    }

                    //give it a few seconds (5000ms) and then trigger interface update with callback liatener
                    Runnable r = new Runnable() {
                        @Override
                        public void run(){
                            myRoundsListListener.onRoundsListUpdated();
                        }
                    };
                    Handler h = new Handler();
                    h.postDelayed(r, 5000); //  5000 isdelay time in miliseconds.


                   // Log.d("CloudArchery", "got to the end of sync onDataChange");
                } //onDataChange

                @Override
                public void onCancelled(FirebaseError error) {
                }
            }); //AddlistenerforSingleValueEvent


         } //if SyncOn, Connected, Authenticated,
        //Log.d ("CloudArchery", "got to the end of Sync");

        StartRoundChangeListener();
    } //sync

    public void StartRoundChangeListener () {
        //Keeps watch on CDS to see if any of the rounds are changed online
        if (syncOn && connected && authenticated && linked) {
            roundsListener = myClubFirebaseRef.child("users/" + userID + "/rounds/").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                /*
                    Not sure what I was trying to do here, or why I commented it out.   Revisited this and decided
                    to try sync() instead.
                    sync() does not work - creates an infinite loop of updates (

                    JSONArray usersJSONArray = new JSONArray();
                    if (snapshot.getValue() != null) {
                        Map<String, JSONObject> usersMap = (HashMap<String, JSONObject>) snapshot.getValue();
                        JSONObject usersJSON = new JSONObject(usersMap);
                        if (usersMap != null) try {
                            Iterator x = usersJSON.keys();
                            while (x.hasNext()) {
                                String thisUSerID = (String) x.next();
                                usersJSONArray.put(usersJSON.get(thisUSerID));

                                JSONObject thisUserJSON = usersJSON.getJSONObject(thisUSerID);
                                SQLiteRounds db = new SQLiteRounds(myContext);
                                JSONObject thisUserStatusJSON = thisUserJSON.getJSONObject("status");
                                JSONArray thisUserDataJSON = thisUserJSON.getJSONArray("data");
                                //Log.d("CloudArchery", "Downloading updated Round Data");
                                db.updateLocalRoundwithScores(thisUSerID, roundID, thisUserStatusJSON, thisUserDataJSON);
                            }
                            myScoreListener.onScoreUpdated(usersJSONArray);

                        } catch (Throwable t) {
                            t.printStackTrace();
                            Log.e("CloudArchery", "Error extracting JSON Data from User Update (ClubFirebase.startLeaderboard");
                        }

                    } */
                    //sync();
                }///OnDataChange

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("CloudArchery", "The read failed (ClubFirebase.startCDSRoundChangeListener) : " + firebaseError.getMessage());
                }

            });
        } else {

        }
    } //StartCDSRoundChangeListener

    public void StopRoundChangeListener () {
        if ((myClubFirebaseRef != null) && (roundsListener != null)) myClubFirebaseRef.removeEventListener(roundsListener);
    } //StopCDSRoundChangeListener


    public void CheckErrors (){
        if (firebaseError.equals("IncorrectClubURL")) {
            SharedPreferences.Editor e = mSharedPreferences.edit();
            e.putString(myContext.getString(R.string.PREF_CLUBURL), "");
            e.commit();
            firebaseError = "";
            initialise(true);
        }//If
    }//CheckErrors

    // - - - - - - - - - - - - - - - - - - - - - - - - -   Queue Management Code  - - - - - - - - - - - - - - - - - - - - - - - - - }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public void deQueue() {
        // if have connection to Firebase
        // repeat
        // -> check if SQLIte has items in queue
        // -> if yes, get first item
        // -> write item to firebase
        // -> check for confirmation that saved
        // -> if saved, delete from SQLite
        // until SQLite Queue empty

        if (syncOn && connected && authenticated && linked) {
            //Log.d("CloudArchery", "...can dequeue as connected");
            if (!fbQ.queueisEmpty() && (deQueueingItem == -1)) {
                //Log.d("CloudArchery", "..queue not empty.");
                final FirebaseQueueItem fbQI = fbQ.getNextQueueItem();
                deQueueingItem = fbQI.getID();

                final String correctedFirebaseRef = fbQI.getFirebaseRef();
                //Removed correction of temporary UserID

                //JSONObject queueJSON = new JSONObject();
                JSONObject queueJSON = fbQI.getJSONItem();
                Map<String, Object> JSONItem = null;

                if (queueJSON != null) {
                    try {
                        JSONItem = jsonToMap(queueJSON);
                        //Log.d("CloudArchery", "... got JSONItem as Map");

                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.e("CloudArchery", "Could not convert JSON to map (clubFirebase.dequeue)");
                    }

                    //Log.d("CloudArchery", "...writing");
                    Log.d("CloudArchery", "De Queueing item #"+deQueueingItem + " : " + correctedFirebaseRef);

                    myClubFirebaseRef.child(correctedFirebaseRef).updateChildren(JSONItem, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError != null) {
                                Log.e("CloudArchery", "Could not write to Firebase (clubFirebase.dequeue)  :" + firebaseError.getMessage());
                            } else {
                                //Log.d("CloudArchery", "Wrote to Firebase: " + fbQI.getString());
                                fbQ.deQueue(fbQI.getID());
                            }
                            deQueueingItem = -1;
                        }
                    });
                } else {
                    myClubFirebaseRef.child(correctedFirebaseRef).removeValue(new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError != null) {
                                Log.e("CloudArchery", "Could not remove item from Firebase (clubFirebase.dequeue)  :" + firebaseError.getMessage());
                            } else {
                                //Log.d("CloudArchery", "Removed from Firebase:");
                                fbQ.deQueue(fbQI.getID());
                            }
                            deQueueingItem = -1;
                        }
                    });
                }

            } else {
                //Log.d ("CloudArchery", "......QUEUE EMPTY");
            }
        }
    }//deQueue

    public void CDSAddToQueue(String newChildRef, JSONObject newJSONObject) {
       if (syncOn) {
           fbQ.enQueue(newChildRef, newJSONObject);
           Log.d("CloudArchery", "QUEUE - NEW ITEM ADDED ......");
           deQueue();
       }
    }//CDSAddToQueue


    public void saveEnd (final String roundID, JSONObject newUserJSON) {
        //Updates LDS with new User Score Data, including score stats, enqueues for CDS
        Log.d ("CloudArchery", ">>>  SAVING END ");
        JSONObject userStatusJSONObject = null;
        JSONArray userDataJSONArray = null;
        long updatedAt = 0L;
        final JSONObject newUpdatedAtJSON = new JSONObject();
        try {
            userStatusJSONObject = newUserJSON.getJSONObject("status");
            userDataJSONArray = newUserJSON.getJSONArray("data");
            updatedAt = newUserJSON.getLong("updatedAt");
            newUpdatedAtJSON.put("updatedAt", updatedAt);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("CloudArchery", "could not extract stats JSON from newScore JSON (clubFirebase.saveEnd)");
        }

        SQLiteRounds db = new SQLiteRounds(myContext);
        db.updateLocalRoundwithScores(userID, roundID, userStatusJSONObject, userDataJSONArray, name, updatedAt);
        CDSAddToQueue("rounds/" + roundID + "/scores/users/" + userID, newUserJSON);

        //Create new Handler for second enqueue so that it does not happen too fast
        Runnable r = new Runnable() {
            @Override
            public void run(){
                CDSAddToQueue("rounds/" + roundID + "/scores/", newUpdatedAtJSON);
            }
        };

        Handler h = new Handler();
        h.postDelayed(r, deQueueDelay); // <-- the  deQueueDelay is the delay time in miliseconds.


        Log.d("CloudArchery", "FINISHED SAVING END");
    } //SaveEnd


 //   public ValueEventListener roundTypeUpdateAvailableListener (){
    public void startRoundTypeUpdate (){
        mSharedPreferences = myContext.getSharedPreferences(myContext.getString(R.string.PREFS), myContext.MODE_PRIVATE);
        final Float roundTypesUpdatedAt = mSharedPreferences.getFloat(myContext.getString(R.string.PREF_ROUNDTYPESUPDATEDAT), 0);
        String RoundsDBRef = "roundTypes/updatedAt";
        Log.d("CloudArchery", "Looking for RoundTypes Update");

        myCentralFirebaseRef.child(RoundsDBRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        Long CDSRoundTypesUpdatedAt = (Long) dataSnapshot.getValue();
                        if (CDSRoundTypesUpdatedAt > roundTypesUpdatedAt) {
                            updateDownloadRoundTypes();
                            if (myRoundTypesUpdatedListener != null)
                                myRoundTypesUpdatedListener.onRoundTypesUpdated();
                        }

                    } catch (Throwable t) {

                        Log.e("CloudArchery", "Could not read updatedAt from central firebase/roundTypes (clubFirebase.roundTypeUpdateListener) ");
                        t.printStackTrace();
                    }
                } else {

                }
            } //onDataChange

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });

      //return myListener;
    } //startRoundTypeUpdate


    public void updateDownloadRoundTypes() {
        if (isNetworkAvailable()) {
            if (myCentralFirebaseRef == null) myCentralFirebaseRef = new Firebase(myContext.getString(R.string.firebase_central_url));
            myCentralFirebaseRef.child("/roundTypes/data/").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot snapshot) {
                    try {
                        Map<String, JSONObject> CDSRoundTypeMap = (HashMap<String, JSONObject>) snapshot.getValue();
                        SQLiteRoundTypes dbRT = new SQLiteRoundTypes(myContext);
                        dbRT.update(CDSRoundTypeMap);
                        Log.d ("CloudArchery", "Updating Round Types Database...");

                    } catch (Throwable t) {
                        Log.e("CloudArchery", "Firebase read error (clubFirebase.updateDownloadRoundTypes)");
                        t.printStackTrace();

                    }
                }

                @Override
                public void onCancelled(FirebaseError error) {
                }
            });
        }

    }//updateDownloadRoundTypes

    void createCustomRoundType (String newRoundTypeID, JSONObject newRoundTypeJSON){
        SQLiteRoundTypes dbRT = new SQLiteRoundTypes(myContext);
        dbRT.createCustomRoundType(newRoundTypeID, newRoundTypeJSON);
        //ToDo : (RELEASE > 2) Save Custom Round Types on the Club database
    } //createRoundType


    void joinRound (final String newRoundID, final long newRoundUpdatedAt, final JSONObject newRoundUserJSON){
        //Process
        // 1. GetLatestRoundData
        // 2. Add new User Data
        // 3. Modify updatedAt
        // 4. Write modified Round Data to LDS
        // 5. Queue new Round Data for upload to CDS
        // 6. Queue new UserRoundIndex Data for upload to CDS
        // 7. Queue new /scores/updatedAt for upload to CDS
        // 8. Show list update
        myClubFirebaseRef.child("rounds/"+newRoundID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Log.d ("CloudArchery", ">>> Joining Round");
                        // 1. GetLatestRoundData
                        Map<String, JSONObject> CDSRoundJSONDataObject = (HashMap<String, JSONObject>) snapshot.getValue();
                        JSONObject CDSRoundJSONObject = new JSONObject(CDSRoundJSONDataObject);
                        SQLiteRounds db = new SQLiteRounds(myContext);
                        JSONObject roundScoresJSON = CDSRoundJSONObject.getJSONObject("scores");
                        JSONObject roundUsersJSON = roundScoresJSON.getJSONObject("users");

                        // 2. Add new User Data
                        roundUsersJSON.put(userID, newRoundUserJSON);

                        // 3. Modify updatedAt
                        roundScoresJSON.put("updatedAt", newRoundUpdatedAt);

                        // 4. Write modified Round Data to LDS
                        db.createLocalRound(newRoundID, CDSRoundJSONObject);

                        // 5. Queue new Round Data for upload to CDS
                        CDSAddToQueue("/rounds/"+newRoundID+"/scores/users/" + userID, newRoundUserJSON);

                        // 6. Queue new UserRoundIndex Data for upload to CDS
                        final JSONObject newRoundIndexJSON = new JSONObject();
                        try {
                            newRoundIndexJSON.put(newRoundID, newRoundUpdatedAt);
                        } catch (JSONException e) {
                            Log.e("CloudArchery", "Could not create Index JSON (ClubFirebase.updateRound)");
                            e.printStackTrace();
                        }
                        //Create new Handler for second enqueue so that it does not happen too fast
                        Runnable r = new Runnable() {
                            @Override
                            public void run(){
                                CDSAddToQueue("/users/" + userID + "/rounds/", newRoundIndexJSON);
                            }
                        };

                        Handler h = new Handler();
                        h.postDelayed(r, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.


                        // 7. Queue new /scores/updatedAt for upload to CDS
                        final JSONObject newUpdatedAtJSON = new JSONObject();
                        newUpdatedAtJSON.put("updatedAt", newRoundUpdatedAt);

                        //Create new Handler for second enqueue so that it does not happen too fast
                        Runnable r2 = new Runnable() {
                            @Override
                            public void run(){
                                CDSAddToQueue("/rounds/"+newRoundID+"/scores/", newUpdatedAtJSON);
                            }
                        };

                        Handler h2 = new Handler();
                        h2.postDelayed(r2, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.

                        //8. Update the rounds list
                        myRoundsListListener.onRoundsListUpdated();

                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.e("CloudArchery", "error downloading round data (clubFirebase.joinRound) ");
                    }
                } else {
                    Log.d("CloudArchery", "No Data Record found  (clubFirebase.joinRound)  roundID=" + newRoundID);
                }
            } //onDataChange

            @Override
            public void onCancelled(FirebaseError error) {
                Log.e("CloudArchery", "Firebase Error (clubFirebase.joinRound)");
            }
        }); //addValueEventListener
    } //joinRound


    void updateRound (final String updatedRoundID, Long updatedRoundUpdatedAt,  final JSONObject updatedRoundJSON){
        Log.d ("CloudArchery", ">>>  Updating Round");
        SQLiteRounds dbR = new SQLiteRounds(myContext);
        dbR.updateLocalRound(updatedRoundID, updatedRoundJSON);
        final JSONObject newRoundIndexJSON = new JSONObject();
        try {
            newRoundIndexJSON.put(updatedRoundID, updatedRoundUpdatedAt);
        } catch (JSONException e) {
            Log.e("CloudArchery", "Could not create Index JSON (ClubFirebase.updateRound)");
            e.printStackTrace();
        }

        CDSAddToQueue("/rounds/" + updatedRoundID, updatedRoundJSON);

        //Now write the updated timestamp to all users
        JSONObject roundUsers = null;
        try {
            roundUsers = updatedRoundJSON.getJSONObject("scores").getJSONObject("users");
        } catch (JSONException e) {
            Log.e("CloudArchery", "Could not create Array of users (ClubFirebase.updateRound)");
            e.printStackTrace();
        }

        int i = 1;
        Iterator<?> keys = roundUsers.keys();
        while( keys.hasNext() ) {
            try {
                final String key = (String)keys.next();
                if (roundUsers.get(key) instanceof JSONObject ) {
                    //Create new Handler for further enqueues so that they does not happen too fast
                    Runnable r = new Runnable() {
                        @Override
                        public void run(){
                            CDSAddToQueue("/users/" + key + "/rounds/", newRoundIndexJSON);
                        }
                    };
                    Handler h = new Handler();
                    h.postDelayed(r, i*deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.

                }
            } catch (JSONException e) {
                Log.e("CloudArchery", "Could not read from array of users (ClubFirebase.updateRound)");
                e.printStackTrace();
            }
            i++;
        }
    } //updateRound


    void deleteRound (final String deleteRoundID) {
        Log.d ("CloudArchery", ">>>  Deleting Round");
        SQLiteRounds dbR = new SQLiteRounds(myContext);
        dbR.deleteLocalRound(deleteRoundID);
        CDSAddToQueue("/users/" + userID + "/rounds/" + deleteRoundID, null);

        Runnable r = new Runnable() {
            @Override
            public void run(){
                CDSAddToQueue("/rounds/" + deleteRoundID, null);
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.
    } //deleteRound

    void mergeRounds (final String myRoundID, String cloudRoundID){
        //Process
        //1. For each user in LDS/rounds/myroundID/scores/users
        //2.1. Copy LDS/rounds/myRoundID/scores/users/ into CDS/rounds/cloudRoundID/scores/users
        //2.2. Write [roundID:updatedAt] into CDS/users/userID/rounds/
        //2.3. Delete CDS/users/userID/rounds/myRoundID
        //3. Update /CDS/rounds/cloudRoundID/updatedAt with new timestamp
        //4. Delete LDS/rounds/myRoundID
        //5. Delete CDS/rounds/myRoundID

        Log.d ("CloudArchery", ">>> Merging Rounds");
        SQLiteRounds dbR = new SQLiteRounds(myContext);
        long updatedAt = System.currentTimeMillis();

        //1. For each user in LDS/rounds/myroundID/scores/users
        JSONObject myRoundJSON = dbR.getRoundJSON(myRoundID);
        JSONObject myRoundUsersJSON = null;
        try {
            myRoundUsersJSON = myRoundJSON.getJSONObject("scores").getJSONObject("users");
        } catch (Throwable t){
            t.printStackTrace();
            Log.e("CloudArchery", "Could not get USERS JSON (ClubFirebase.mergeRounds)");
        }

        Iterator<String> keys = myRoundUsersJSON.keys();
        while(keys.hasNext()) {
            final String key = keys.next();
            final JSONObject roundIndexJSON = new JSONObject();
            try {
                //2.1. Copy LDS/rounds/myRoundID/scores/users/ into CDS/rounds/cloudRoundID/scores/users
                JSONObject myRoundUserJSON = myRoundUsersJSON.getJSONObject(key);
                CDSAddToQueue("/rounds/" + cloudRoundID + "/scores/users/" + key, myRoundUserJSON);
                //2.2. Write [roundID:updatedAt] into CDS/users/userID/rounds/
                roundIndexJSON.put(cloudRoundID, updatedAt);
                Runnable r = new Runnable() {
                    @Override
                    public void run(){
                        CDSAddToQueue("/users/" + key + "/rounds/", roundIndexJSON);
                    }
                };
                Handler h = new Handler();
                h.postDelayed(r, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.


                //2.3. Delete CDS/users/userID/rounds/myRoundID
                Runnable r2 = new Runnable() {
                    @Override
                    public void run(){
                        CDSAddToQueue("/users/" + key + "/rounds/" + myRoundID, null);
                    }
                };
                Handler h2 = new Handler();
                h2.postDelayed(r2, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.



            } catch (Throwable t){
                t.printStackTrace();
                Log.e("CloudArchery", "Could not get USER JSON from USERS JSON (ClubFirebase.mergeRounds)");
            }
        }

        //3. Update /CDS/rounds/cloudRoundID/updatedAt with new timestamp
        JSONObject updatedAtJSON = new JSONObject();
        try {
            updatedAtJSON.put("updatedAt", updatedAt);
            CDSAddToQueue("/rounds/" + cloudRoundID, updatedAtJSON);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("CloudArchery", "Could not write updatedAt to JSON (ClubFirebase.mergeRounds)");
        }

        //4. Delete LDS/rounds/myRoundID
        dbR.deleteLocalRound(myRoundID);

        //5. Delete CDS/rounds/myRoundID
        Runnable r = new Runnable() {
            @Override
            public void run(){
                CDSAddToQueue("rounds/" + myRoundID, null);
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.

    } //mergeRounds

    void createRound (final String newRoundID, Long newRoundUpdatedAt,  final JSONObject newRoundJSON){
        Log.d ("CloudArchery", ">>> Creating Round");
        SQLiteRounds dbR = new SQLiteRounds(myContext);
        dbR.createLocalRound(newRoundID, newRoundJSON);
        JSONObject newRoundIndexJSON = new JSONObject();
        try {
            newRoundIndexJSON.put(newRoundID, newRoundUpdatedAt);
        } catch (JSONException e) {
            Log.e("CloudArchery", "Could not create Index JSON (ClubFirebase.createRound)");
            e.printStackTrace();
        }
        CDSAddToQueue("/users/" + userID + "/rounds/", newRoundIndexJSON);

        Runnable r = new Runnable() {
            @Override
            public void run(){
                CDSAddToQueue("/rounds/" + newRoundID, newRoundJSON);
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.
    }

    public void startLeaderboardListener (final String roundID) {
        if (syncOn && connected && authenticated && linked) {
            leaderBoardListener = myClubFirebaseRef.child("/rounds/" + roundID + "/scores/users/").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    JSONArray usersJSONArray = new JSONArray();
                    JSONArray sortedUsersJSONArray = new JSONArray();
                    String StK = null;
                    Long LV = null;
                    int totalScore;
                   // JSONObject roundJSONObject = null;
                    if (snapshot.getValue() != null) {
                        Map<String, JSONObject> usersMap = (Map<String, JSONObject>) snapshot.getValue();
                        //Map <Long, JSONObject> thisMap = new HashMap<Long, JSONObject>();


                      //  myScoreListener.onScoreUpdated(sortedUsersJSONArray);

                        JSONObject usersJSON = new JSONObject(usersMap);
                        if (usersMap != null) try {
                            Iterator x = usersJSON.keys();
                            while (x.hasNext()) {
                                String thisUSerID = (String) x.next();
                                //usersJSONArray.put(usersJSON.get(thisUSerID));

                                JSONObject thisUserJSON = usersJSON.getJSONObject(thisUSerID);
                                SQLiteRounds db = new SQLiteRounds(myContext);
                                JSONObject thisUserStatusJSON = thisUserJSON.getJSONObject("status");
                                JSONArray thisUserDataJSON = thisUserJSON.getJSONArray("data");
                                String name = thisUserJSON.getString("name");
                                Long updatedAt = thisUserJSON.getLong("updatedAt");
                                //Log.d("CloudArchery", "Downloading updated Round Data");
                                db.updateLocalRoundwithScores(thisUSerID, roundID, thisUserStatusJSON, thisUserDataJSON, name, updatedAt);
                                sortedUsersJSONArray = db.getRoundUsersJSONArray(roundID);
                                myScoreListener.onScoreUpdated(sortedUsersJSONArray);
                            }


                        } catch (Throwable t) {
                            t.printStackTrace();
                            Log.e("CloudArchery", "Error extracting JSON Data from User Update (ClubFirebase.startLeaderboard");
                        }
                    }
                }//StartLeaderboard

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("CloudArchery", "The read failed (ClubFirebase.startLeaderboard) : " + firebaseError.getMessage());
                }

            });
        } else {

        }
    } //startLeaderboardListener

    public void stopLeaderboardListener(){
        if ((myClubFirebaseRef != null) && (leaderBoardListener != null)) myClubFirebaseRef.removeEventListener(leaderBoardListener);
    } //stopLeaderboardListener

    public void startJoinableRoundListener () {
        if (syncOn && connected && authenticated && linked) {
            firebaseJoinableRoundListener = myClubFirebaseRef.child("/rounds/").limitToLast(100).orderByChild("updatedAt").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //System.out.println(snapshot.getValue());
                    JSONArray unsortedRoundsJSONArray = new JSONArray();
                    JSONArray sortedRoundsJSONArray = new JSONArray();
                    Map<String, JSONObject> roundsMap = (HashMap<String, JSONObject>) snapshot.getValue();
                    JSONObject roundsJSON = new JSONObject(roundsMap);
                    boolean joinedAlready = false;

                    if ((roundsMap != null)) try {
                        Iterator x = roundsJSON.keys();
                        String thisRoundUserID = null;
                        while (x.hasNext()) {
                            String key = (String) x.next();
                            try {
                                joinedAlready = false;
                                JSONObject creatorJSON = roundsJSON.getJSONObject(key);
                                thisRoundUserID = creatorJSON.getJSONObject("creator").getString("id");
                                joinedAlready = creatorJSON.getJSONObject("scores").getJSONObject("users").has(userID);
                                if ((!thisRoundUserID.equals(userID)) && (!joinedAlready))
                                    unsortedRoundsJSONArray.put(roundsJSON.get(key));
                            } catch (Throwable t) {
                                Log.e("CloudArchery", "Could not extract required data (ClubFirebase.startJoinableRoundListener)");
                                t.printStackTrace();
                            }
                        }


                        long updatedAt = 0;
                        try {
                            Map<Long, JSONObject> thisMap = new HashMap<Long, JSONObject>();

                            for (int i = 0; i < unsortedRoundsJSONArray.length(); i++) {

                                JSONObject thisJSONObject = unsortedRoundsJSONArray.getJSONObject(i);
                                if (thisJSONObject.has("updatedAt")) {
                                    updatedAt = thisJSONObject.getLong("updatedAt");
                                } else if ((thisJSONObject.has("createdAt"))) {
                                    updatedAt = thisJSONObject.getLong("createdAt");
                                }
                                thisMap.put(updatedAt, thisJSONObject);
                            }
                            List<Map.Entry<Long, JSONObject>> entries =
                                    new ArrayList<Map.Entry<Long, JSONObject>>(thisMap.entrySet());
                            Collections.sort(entries, new Comparator<Map.Entry<Long, JSONObject>>() {
                                public int compare(Map.Entry<Long, JSONObject> a, Map.Entry<Long, JSONObject> b) {
                                    return b.getKey().compareTo(a.getKey());
                                }
                            });

                            for (Map.Entry<Long, JSONObject> entry : entries) {
                                sortedRoundsJSONArray.put(entry.getValue());
                            }

                            myJoinableRoundListener.onJoinableRoundChanged(sortedRoundsJSONArray);
                        } catch (Throwable t) {
                            t.printStackTrace();
                            Log.e("CloudArchery", "Error extracting JSON Data from Firebase Return (ClubFirebase.startJoinableRoundListener");
                        }

                    } catch (Throwable t) {
                        Log.e("CloudArchery", "Could not make JSON Object from JSON Array (SQLiteLocal.getRoundUsersJSONArray");
                        t.printStackTrace();
                    }
                }


                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("CloudArchery", "The read failed (ClubFirebase.startJoinableRoundListener) : " + firebaseError.getMessage());
                }
            });//new valueEventlistener
        }//if syncon.....
    } //startJoinablRoundListener

    public void stopJoinableRoundListener(){
        if ((myClubFirebaseRef != null) && (firebaseJoinableRoundListener != null)) myClubFirebaseRef.removeEventListener(firebaseJoinableRoundListener);
    } //stopLeaderboardListener

  /*  public void updateClubStatistics(){

        //example
        /*
          "statistics" : {
            "averageScore" : 6.23,
            "bestScore" : {
              "average" : 8.33,
              "date" : "2015-07-25",
              "name" : "No Record",
              "roundName" : "Portsmouth",
              "roundType" : "02b96ac7-e553-4e1d-a0fb-758838165657",
              "totalScore" : 500
            },
            "numUsers" : 12,
            "totalArrows" : 2345,
            "totalRoundsCompleted" : 67,
            "updatedAt" : 1437846316000
          },




        myClubFirebaseRef.child("rounds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                double averageScore = 0;
                double bestScoreAverage = 0;
                String bestScoreDate = "";
                String bestScoreName = "";
                String bestScoreRoundTypeName = "";
                int bestScoreTotalScore = 0;
                int numUsers = 0;
                int totalArrows = 0;
                int totalRoundsCompleted = 0;

                Map<String, JSONObject> roundsMap = (HashMap<String, JSONObject>) snapshot.getValue();
                JSONObject roundsJSON = new JSONObject(roundsMap);

                if ((roundsMap != null)) try {
                    Iterator x = roundsJSON.keys();
                    // schema
                    while (x.hasNext()) {
                        String key = (String) x.next();
                        try {
                            JSONObject roundJSON = roundsJSON.getJSONObject(key);
                            if (roundJSON.has("scores")) {
                                JSONObject scoresJSON = roundJSON.getJSONObject("scores");
                                if (scoresJSON.has("users")) {
                                    JSONObject usersJSON = scoresJSON.getJSONObject("users");
                                    Iterator y = userRoundScoresJSON.keys();
                                    while (y.hasNext()){
                                        String key2 = (String) y.next();
                                        JSONObject statusJSON = userRoundScoresJSON.getJSONObject(key2);
                                        try {
                                            boolean complete = statusJSON.getBoolean("complete");
                                            if (complete) totalRoundsCompleted++;
                                            int thisUserTotalArrows = statusJSON.getInt("totalArrows");
                                            totalArrows+= thisUserTotalArrows;
                                            int thisUserTotalScore = statusJSON.getInt("totalScore");
                                            if (thisUserTotalArrows != 0) {
                                                double thisUserAverage = (double) thisUserTotalScore / thisUserTotalArrows;
                                                if (thisUserAverage > bestScoreAverage) {

                                                }
                                            }

                                        } catch (Throwable t){
                                            t.printStackTrace();
                                        }
                                    }
                                }
                            }
                            joinedAlready = creatorJSON.getJSONObject("scores").getJSONObject("users").has(userID);
                        } catch (Throwable t) {
                            Log.e("CloudArchery", "Could not extract required data (ClubFirebase.startJoinableRoundListener)");
                            t.printStackTrace();
                        }

                    }


                } catch (Throwable t) {
                    t.printStackTrace();
                    Log.e("CloudArchery", "Error extracting JSON Data from Firebase Return (ClubFirebase.startJoinableRoundListener");
                }
            } //onDataChange

            @Override
            public void onCancelled(FirebaseError error) {
                Log.e("CloudArchery", "Firebase Error (clubFirebase.updateClubStatistics)");
            }
        }); //addValueEventListener
    }//updateClubStatistics

    */

    public void fetchClubStatistics(){
        if (syncOn && connected && authenticated && linked) {
            myClubFirebaseRef.child("/statistics/").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        Map<String, JSONObject> myClubStatsJSONMap = (HashMap<String, JSONObject>) snapshot.getValue();
                        JSONObject myClubStatsJSON = new JSONObject(myClubStatsJSONMap);
                        myClubStatsUpdatedListener.onClubStatsUpdated(myClubStatsJSON);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.e("CloudArchery", "Error extracting JSON Stats Data from Firebase (ClubFirebase.fetchClubStatistics");
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("CloudArchery", "The read failed (ClubFirebase.fetchClubStatistics) : " + firebaseError.getMessage());
                }
            });
        }
    }//fetchClubStatistics

}
