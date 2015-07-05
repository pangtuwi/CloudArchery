package com.cloudarchery.archersapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by paulwilliams on 22/03/15.
 */
public class ClubFirebase {
    //Contains all firebase interaction

    Context myContext;
    MyApp myAppState;

    boolean network = false;
    boolean syncOn = false;
    boolean connected = false;
    boolean authenticated = false;
    boolean linked = false;
    String clubURL = "";
    String clubID = "";
    String email = "";
    String password = "";
    String userID = "LDS_Only"; //Temp setting, correct in oncreate
    String name = "";
    String firebaseError = "";
    String clubWebURL = "";
    int deQueueingItem = -1;
    int deQueueDelay = 250;

    SQLiteFirebaseQueue fbQ;

    SharedPreferences mSharedPreferences;
    Firebase myCentralFirebaseRef;
    Firebase myClubFirebaseRef;

    OnUserScoreUpdatedListener myScoreListener;
    OnRoundsListUpdatedListener myRoundsListListener;
    OnConnectionListener myConnectionListener;
    OnJoinableRoundChangedListener myJoinableRoundListener;
    ValueEventListener leaderBoardListener;
    OnRoundTypesUpdatedListener myRoundTypesUpdatedListener;

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void initialise() {
        Log.d("CloudArchery ", "initialising CDS...");
        myContext = MyApp.getAppContext();

        connected = false;
        authenticated = false;
        linked = false;
        network = isNetworkAvailable();
        if (myConnectionListener != null) {
            if (network) {
                myConnectionListener.onConnectionUpdated(network, null, null, null, "Active Internet connection found");
            } else {
                myConnectionListener.onConnectionUpdated(network, null, null, null, "No internet connection");
            }
        }
        userID = myContext.getString(R.string.LDS_Only_Username); //Renitialise with correct string
        Firebase.setAndroidContext(myContext);
        fbQ = new SQLiteFirebaseQueue(myContext);

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

            myCentralFirebaseRef = new Firebase(myContext.getString(R.string.firebase_central_url));
            //ToDo : (RELEASE > 2) Clean up existing Listeners?
            String childRef = "clubs/" + clubID + "/firebaseURL";
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
                            e.commit();
                            checkConnected();
                        } catch (Throwable t) {

                            Log.e("CloudArchery", "Could not read ClubURL from central firebase (clubFirebase.OnCreate) ");
                            t.printStackTrace();
                        }
                    } else {
                        if (myConnectionListener != null)
                            myConnectionListener.onConnectionUpdated(network, connected, null, null, "Cannot find Club Database");
                    } //if...else
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    if (myConnectionListener != null)
                        myConnectionListener.onConnectionUpdated(network, connected, null, null, "Cannot find Club Database");
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


    }//onCreate

    public void checkConnected() {
        if (syncOn) {
            Log.d("CloudArchery ", "sync is on, checking conection");
            try {
                myClubFirebaseRef = new Firebase(clubURL);
            } catch (Throwable t) {
                firebaseError = "IncorrectClubURL";
                t.printStackTrace();
            }
            try {


                myClubFirebaseRef.child(".info/connected").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean isConnectedCheck = snapshot.getValue(Boolean.class);
                        if (isConnectedCheck) {
                            connected = true;
                            if (myConnectionListener != null)
                                myConnectionListener.onConnectionUpdated(network, connected, null, null, "");
                            verifyLogin();
                        } else {
                            connected = false;
                            if (myConnectionListener != null)
                                myConnectionListener.onConnectionUpdated(network, connected, null, null, "No connection to Cloud Database");
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError error) {
                        connected = false;
                        if (myConnectionListener != null)
                            myConnectionListener.onConnectionUpdated(network, connected, null, null, "No connection to Cloud Database");
                    }
                });
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("CloudArchery", "Exception while checking connection ");
            }
        } //if SyncOn
    } //checkConnected

    public void verifyLogin() {

        if (syncOn && connected) {
            //imageViewLoggedIn = (ImageView) findViewById(R.id.imageViewLoggedIn);
            Log.d("CloudArchery ", "sync on, connected, verifying login...");
            myClubFirebaseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    authenticated = true;
                    if (myConnectionListener != null)
                        myConnectionListener.onConnectionUpdated(network, connected, authenticated, linked, "");
                    verifyUserName();
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    authenticated = false;
                    if (myConnectionListener != null)
                        myConnectionListener.onConnectionUpdated(network, connected, authenticated, linked, "Authentication parameters incorrect.  Contact your club administrator.");
                }
            });
        } //if SyncOn
    } //verifyLogin

  /*  public void copyUserUpload (String roundID) {
        SQLiteLocal db = new SQLiteLocal(myContext);
        JSONObject newUserJSON = db.getUserJSON(roundID);
        enQueue("users/"+userID+"/rounds/" + roundID, newUserJSON);
    } //copyUserUpload
*/

    public void verifyUserName() {
        if (syncOn && connected && authenticated) {
            Log.d("CloudArchery ", "sync on, connected, authenticated,  getting username");
            myClubFirebaseRef.child("users/").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Map<String, Object> dbUsers = (HashMap<String, Object>) snapshot.getValue();
                    myConnectionListener.onConnectionUpdated(network, connected, authenticated, linked, "User not found in Club Database");
                    try {
                        for (String key : dbUsers.keySet()) {
                            Map<String, String> dbThisUser = (HashMap<String, String>) dbUsers.get(key);
                            String thisEmail = dbThisUser.get("email");
                            String thisUserID = dbThisUser.get("id");
                            String thisName = dbThisUser.get("name");

                            if ((thisEmail != null) & (thisName != null) & (thisUserID != null)) {
                                //Log.d("CloudArchery", "checking " + thisEmail + " against " + email);
                                if (email.equals(thisEmail)) {// & (originalPassword.equals(thisPassword))) {
                                    if (thisUserID != null) userID = thisUserID;
                                    if (thisName != null) name = thisName;
                                    SharedPreferences.Editor e = mSharedPreferences.edit();
                                    e.putString(myContext.getString(R.string.PREF_USERID), userID);
                                    e.putString(myContext.getString(R.string.PREF_NAME), name);
                                    e.commit();

                                    Toast.makeText(myContext, "Connected to Cloud Archery Database", Toast.LENGTH_LONG).show();
                                    Log.d("CloudArchery", "User with ID " + userID + " selected");
                                    linked = true;
                                    Log.d("CloudArchery", "Linked = " + linked);
                                    if (myConnectionListener != null)
                                        myConnectionListener.onConnectionUpdated(network, connected, authenticated, linked, "");
                                    //deQueue();
                                    sync();
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

    public String getName() {
        return name;
    }

    public void copyRoundUpload(String roundID) {
        Log.d("CloudArchery", "copying a round from LDS to CDS (ClubFirebase.copyRoundUpload) : " + roundID);
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
            enQueue("rounds/" + roundID, newRoundJSON);

            //Create new Handler for second enqueue so that it does not happen too fast
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    enQueue("users/" + userID + "/rounds", newUserRoundIndexJSONObject);
                }
            };

            Handler h = new Handler();
            h.postDelayed(r, deQueueDelay); // <-- the deQueuDelay is the delay time in miliseconds.
        }

    } //copyRoundUpload

    public void insertRoundDownload(final String roundID) {
        myClubFirebaseRef.child("rounds/" + roundID).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void copyRoundDownload(final String roundID) {

        myClubFirebaseRef.child("rounds/" + roundID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Map<String, JSONObject> CDSRoundJSONDataObject = (HashMap<String, JSONObject>) snapshot.getValue();
                        JSONObject CDSRoundJSONObject = new JSONObject(CDSRoundJSONDataObject);
                        SQLiteRounds db = new SQLiteRounds(myContext);
                        db.updateLocalRound(roundID, CDSRoundJSONObject);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.e("CloudArchery", "error downloading round data (clubFirebase.copyRoundDownload) ");
                    }
                } else {
                    Log.d("CloudArchery", "No Data Record found  (clubFirebase.copyRoundDownload)  roundID=" + roundID);
                }
            } //onDataChange

            @Override
            public void onCancelled(FirebaseError error) {
                Log.e("CloudArchery", "Firebase Error (clubFirebase.copyRoundDownload)");
            }
        }); //addValueEventListener
    } //copyRoundDownload

    public void sync() {
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


        //Todo - deal with what happens in sync process when I have deleted a round.(ONLINE)
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
                    Log.d("CloudArchery", "Loaded following data from LDS:");
                    Iterator debugit = LDSRoundMap.entrySet().iterator();
                    while (debugit.hasNext()) {
                        Map.Entry pair = (Map.Entry) debugit.next();
                        Log.d("CloudArchery", " ... " + pair.getKey() + "  :   " + pair.getValue());
                        debugit.remove(); // avoids a ConcurrentModificationException
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
                                Log.d("CloudArchery", "---NEW CDS RECORD DOWNLOAD -- ID=" + CDSRoundID);
                                insertRoundDownload(CDSRoundID);
                                myRoundsListListener.onRoundsListUpdated(); //
                                //ToDo : Check if this listener is working.
                            } else {
                                // (5)  -> else if present in LDS, compare timestamps
                                // if both have timestamps - then use timestamps to compare
                                // if LDS has no timestamp, then assume new and assume that it is the master - set LDSUpdatedAt>CDSUpdatedAt
                                // if CDS has no timestamp then assume LDS is the master, set LDS LDSUpdatedAt>CDSUpdatedAt
                                try {
                                    LDSUpdatedAt = LDSRoundJSON.getLong("updatedAt");
                                } catch (Throwable t) {
                                    Log.e("SYNC ERROR", "Error getting LDS updatedAt from:" + LDSRoundJSON.toString());
                                    LDSUpdatedAt = System.currentTimeMillis();
                                    try {
                                        LDSRoundJSON.put("updatedAt", LDSUpdatedAt);
                                        db.updateLocalRound(CDSRoundID, LDSRoundJSON);
                                    } catch (Throwable t2) {
                                        t2.printStackTrace();
                                        Log.e("CloudArchery", "Error putting LDS updatedAt");
                                    }

                                }
                                try {
                                    //CDSUpdatedAt = CDSRoundJSON.getLong("updatedAt");
                                    CDSUpdatedAt = (Long) CDSRounds.get(CDSRoundID);
                                } catch (Throwable t) {
                                    Log.e("SYNC ERROR", "Error getting CDS updatedAt from:" + CDSRoundJSON.toString());
                                    CDSUpdatedAt = LDSUpdatedAt - 1;
                                }

                                if (LDSUpdatedAt > CDSUpdatedAt) {
                                    // (6)    --> if timestamp LDS > timestamp CDS, upload
                                    Log.d("---SYNC UP ---", "ID=" + CDSRoundID + "  |   LDStime = " + LDSUpdatedAt + "   |   CDStime = " + CDSUpdatedAt);
                                    copyRoundUpload(CDSRoundID);
                                } else if (LDSUpdatedAt < CDSUpdatedAt) {
                                    // (7)    --> if timestamp LDS < timestamp CDS, download
                                    Log.d("---SYNC DOWN ---", "ID=" + CDSRoundID + "  |   LDStime = " + LDSUpdatedAt + "   |   CDStime = " + CDSUpdatedAt);
                                    copyRoundDownload(CDSRoundID);
                                }
                                // (8)  -> mark LDS_UserData item as synced
                                LDSRoundMap.put(CDSRoundID, true); //mark LDS Tracker as synced
                                Log.d("CloudArchery", "Marked round " + CDSRoundID + " as synced (ClubFirebase.sync)");
                            }
                        }  // if instance of JSON
                    } // while keys.hasnext
                    // (9) - iterate through all LDS Rounds not marked as synced
                    Iterator it1 = LDSRoundMap.entrySet().iterator();
                    //Boolean LDSSync = false;
                    while (it1.hasNext()) {
                        Map.Entry pair = (Map.Entry) it1.next();
                        if ((Boolean) pair.getValue() == false) {
                            // (10)   -> add items to CDS (upload)
                            Log.d("CloudArchery", "Copying LDS round to CDS as marked not Synced (ClubFirebase.sync) : " + pair.getKey());
                            copyRoundUpload((String) pair.getKey());
                        }
                        it1.remove(); // avoids a ConcurrentModificationException
                    }
                    // (11) - read sqlite.roundJSON for all rounds = LDS_RoundData
                    //SQLiteLocal db = new SQLiteLocal(myContext);
                /*    final Map<String, JSONObject> LDSUserMap = db.getRoundMap();
                    // (12) - iterate through  LDS_RoundData
                    Iterator it2 = LDSUserMap.entrySet().iterator();
                    while (it2.hasNext()) {
                        Map.Entry pair = (Map.Entry)it2.next();
                        final String roundID = (String) pair.getKey();
                        // (13)  -> read CDS_RoundData for that round
                        myClubFirebaseRef.child("rounds/" + roundID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot snapshot) {
                                int CDSUpdatedAt = 0;
                                int LDSUpdatedAt = 0;
                                SQLiteLocal db = new SQLiteLocal(myContext);
                                JSONObject LDSRoundJSON = db.getRoundJSON(roundID);
                                if (LDSRoundJSON == null) {
                                    copyRoundDownload(roundID);
                                } else {
                                    try {
                                        JSONObject LDSRoundScoresJSON = LDSRoundJSON.getJSONObject("detail");
                                        LDSUpdatedAt = LDSRoundScoresJSON.getInt("updatedAt");
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                        Log.d("CloudArchery", "error getting updatedAt from LDS JSON (clubFirebase.sync)");
                                    }
                                    try {
                                        //String CDSRoundString = snapshot.getValue().toString();
                                        //JSONObject CDSRoundJSON = new JSONObject(CDSRoundString);
                                        Map <String, JSONObject> CDSRoundJSONObject = (HashMap<String, JSONObject>)snapshot.getValue();
                                        JSONObject CDSRoundJSON = new JSONObject(CDSRoundJSONObject);
                                        JSONObject CDSRoundDetailJSON = CDSRoundJSON.getJSONObject("detail");
                                        CDSUpdatedAt = CDSRoundDetailJSON.getInt("updatedAt");
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                        Log.d("CloudArchery", "error getting updatedAt from CDS JSON (clubFirebase.sync)");
                                    }

                                    if ((CDSUpdatedAt == 0) && (LDSUpdatedAt == 0)) {
                                        copyRoundUpload(roundID);

                                    } else if (CDSUpdatedAt > LDSUpdatedAt) {
                                        // (15)   -> if timestamp CDS_RoundData > LDS_RoundData, download
                                        copyRoundDownload(roundID);
                                    } else if (LDSUpdatedAt > CDSUpdatedAt) {
                                        // (16)   -> if timestamp CDS_RoundData < LDS_RoundData, upload
                                        copyRoundUpload(roundID);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError error) {
                                // (14)   -> if does not exist -> add to CDS
                                copyUserUpload(roundID);
                            }
                        });
                        it2.remove(); // avoids a ConcurrentModificationException
                    } */
                    Log.d("CloudArchery", "got to the end of sync onDataChange");
                } //onDataChange

                @Override
                public void onCancelled(FirebaseError error) {
                }
            }); //AddlistenerforSingleValueEvent


        } //if SyncOn, Connected, Authenticated,
        Log.d("CloudArchery", "got to the end of Sync");
    } //sync

    public void CheckErrors() {
        if (firebaseError.equals("IncorrectClubURL")) {
            SharedPreferences.Editor e = mSharedPreferences.edit();
            e.putString(myContext.getString(R.string.PREF_CLUBURL), "");
            e.commit();
            firebaseError = "";
            initialise();
        }//If
    }//CheckErrors

    //ToDo : (RELEASE > 2) Create function that checks CDS/LDS JSON integrity and deals with issues found.

    public void deQueue() {
        // if have connection to Firebase
        // repeat
        // -> check if SQLIte has items in queue
        // -> if yes, get first item
        // -> write item to firebase
        // -> check for confirmation that saved
        // -> if saved, delete from SQLite
        // until SQLite Queue empty

        //Log.d("CloudArchery", "DEQUEUE......");
        if (syncOn && connected && authenticated && linked) {
            //Log.d("CloudArchery", "...can deque as connected");
            if (!fbQ.queueisEmpty() && (deQueueingItem == -1)) {
                //Log.d("CloudArchery", "..queue not empty.");
                final FirebaseQueueItem fbQI = fbQ.getNextQueueItem();
                deQueueingItem = fbQI.getID();

                //replace temporary userID if used
                final String correctedFirebaseRef = fbQI.getFirebaseRef().replaceAll(myContext.getString(R.string.LDS_Only_Username), userID);
                //Log.d("CloudArchery", "...  queue item corrected");

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
                    Log.d("CloudArchery", "De Queueing item #" + deQueueingItem + " : " + correctedFirebaseRef);

                    myClubFirebaseRef.child(correctedFirebaseRef).updateChildren(JSONItem, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError != null) {
                                Log.e("CloudArchery", "Could not write to Firebase (clubFirebase.dequeue)  :" + firebaseError.getMessage());
                            } else {
                                Log.e("CloudArchery", "Wrote to Firebase: " + fbQI.getString());
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
                                Log.e("CloudArchery", "Removed from Firebase:");
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

    public void enQueue(String newChildRef, JSONObject newJSONObject) {
        Log.d("CloudArchery", "QUEUE - NEW ITEM ADDED ......");
        fbQ.enQueue(newChildRef, newJSONObject);
        deQueue();
    }//enQueueJSONObject

    // - - - - - - - - - - - - - - - - - - - - - - - - -   Queue Management Code  - - - - - - - - - - - - - - - - - - - - - - - - - }

    public void saveEnd(final String roundID, JSONObject newUserJSON) {
        //Updates LDS with new User Score Data, including score stats, enqueues for CDS
        Log.d("CloudArchery", "SAVING END ");
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
        db.updateLocalRoundwithScores(userID, roundID, userStatusJSONObject, userDataJSONArray);
        enQueue("rounds/" + roundID + "/scores/users/" + userID, newUserJSON);

        //Create new Handler for second enqueue so that it does not happen too fast
        Runnable r = new Runnable() {
            @Override
            public void run() {
                enQueue("rounds/" + roundID + "/scores/", newUpdatedAtJSON);
            }
        };

        Handler h = new Handler();
        h.postDelayed(r, deQueueDelay); // <-- the  deQueueDelay is the delay time in miliseconds.


        Log.d("CloudArchery", "FINISHED SAVING END");
    } //SaveEnd

    public ValueEventListener roundTypeUpdateAvailableListener() {
        mSharedPreferences = myContext.getSharedPreferences(myContext.getString(R.string.PREFS), myContext.MODE_PRIVATE);
        final Float roundTypesUpdatedAt = mSharedPreferences.getFloat(myContext.getString(R.string.PREF_ROUNDTYPESUPDATEDAT), 0);
        String RoundsDBRef = "roundTypes/updatedAt";

        ValueEventListener myListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        Float CDSRoundTypesUpdatedAt = (Float) dataSnapshot.getValue();
                        if (CDSRoundTypesUpdatedAt > roundTypesUpdatedAt) {
                            updateDownloadRoundTypes();
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
        };

        myCentralFirebaseRef.child(RoundsDBRef).addListenerForSingleValueEvent(myListener);
        return myListener;
    } //roundTypeUpdateListener

    public void updateDownloadRoundTypes() {
        if (syncOn && connected) {
            if (myCentralFirebaseRef == null)
                myCentralFirebaseRef = new Firebase(myContext.getString(R.string.firebase_central_url));
            myCentralFirebaseRef.child("/roundTypes/data/").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot snapshot) {
                    try {
                        Map<String, JSONObject> CDSRoundTypeMap = (HashMap<String, JSONObject>) snapshot.getValue();
                        SQLiteRoundTypes dbRT = new SQLiteRoundTypes(myContext);
                        dbRT.update(CDSRoundTypeMap);

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

    void createCustomRoundType(String newRoundTypeID, JSONObject newRoundTypeJSON) {
        SQLiteRoundTypes dbRT = new SQLiteRoundTypes(myContext);
        dbRT.createCustomRoundType(newRoundTypeID, newRoundTypeJSON);
        //enQueue();
        //ToDo : (RELEASE > 2) Save Custom Round Types on the Club database
    } //createRoundType

    void joinRound(final String newRoundID, final long newRoundUpdatedAt, final JSONObject newRoundUserJSON) {
        //Process
        // 1. GetLatestRoundData
        // 2. Add new User Data
        // 3. Modify updatedAt
        // 4. Write modified Round Data to LDS
        // 5. Queue new Round Data for upload to CDS
        // 6. Queue new UserRoundIndex Data for upload to CDS
        // 7. Queue new /scores/updatedAt for upload to CDS
        myClubFirebaseRef.child("rounds/" + newRoundID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
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
                        enQueue("/rounds/scores/users/" + userID, newRoundUserJSON);

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
                            public void run() {
                                enQueue("/users/" + userID + "/rounds/", newRoundIndexJSON);
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
                            public void run() {
                                enQueue("/rounds/scores/updatedAt/", newUpdatedAtJSON);
                            }
                        };

                        Handler h2 = new Handler();
                        h2.postDelayed(r2, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.


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

    void updateRound(final String updatedRoundID, Long updatedRoundUpdatedAt, final JSONObject updatedRoundJSON) {
        SQLiteRounds dbR = new SQLiteRounds(myContext);
        dbR.updateLocalRound(updatedRoundID, updatedRoundJSON);
        final JSONObject newRoundIndexJSON = new JSONObject();
        try {
            newRoundIndexJSON.put(updatedRoundID, updatedRoundUpdatedAt);
        } catch (JSONException e) {
            Log.e("CloudArchery", "Could not create Index JSON (ClubFirebase.updateRound)");
            e.printStackTrace();
        }

        enQueue("/rounds/" + updatedRoundID, updatedRoundJSON);

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
        while (keys.hasNext()) {
            try {
                final String key = (String) keys.next();
                if (roundUsers.get(key) instanceof JSONObject) {
                    //Create new Handler for further enqueues so that they does not happen too fast
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            enQueue("/users/" + key + "/rounds/", newRoundIndexJSON);
                        }
                    };
                    Handler h = new Handler();
                    h.postDelayed(r, i * deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.

                }
            } catch (JSONException e) {
                Log.e("CloudArchery", "Could not read from array of users (ClubFirebase.updateRound)");
                e.printStackTrace();
            }
            i++;
        }
    } //updateRound

    void deleteRound(final String deleteRoundID) {
        SQLiteRounds dbR = new SQLiteRounds(myContext);
        dbR.deleteLocalRound(deleteRoundID);
        enQueue("/users/" + userID + "/rounds/" + deleteRoundID, null);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                enQueue("/rounds/" + deleteRoundID, null);
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.
    } //deleteRound

    void mergeRounds(final String myRoundID, String cloudRoundID) {
        //Process
        //1. For each user in LDS/rounds/myroundID/scores/users
        //2.1. Copy LDS/rounds/myRoundID/scores/users/ into CDS/rounds/cloudRoundID/scores/users
        //2.2. Write [roundID:updatedAt] into CDS/users/userID/rounds/
        //2.3. Delete CDS/users/userID/rounds/myRoundID
        //3. Update /CDS/rounds/cloudRoundID/updatedAt with new timestamp
        //4. Delete LDS/rounds/myRoundID
        //5. Delete CDS/rounds/myRoundID

        SQLiteRounds dbR = new SQLiteRounds(myContext);
        long updatedAt = System.currentTimeMillis();

        //1. For each user in LDS/rounds/myroundID/scores/users
        JSONObject myRoundJSON = dbR.getRoundJSON(myRoundID);
        JSONObject myRoundUsersJSON = null;
        try {
            myRoundUsersJSON = myRoundJSON.getJSONObject("scores").getJSONObject("users");
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("CloudArchery", "Could not get USERS JSON (ClubFirebase.mergeRounds)");
        }

        Iterator<String> keys = myRoundUsersJSON.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            final JSONObject roundIndexJSON = new JSONObject();
            try {
                //2.1. Copy LDS/rounds/myRoundID/scores/users/ into CDS/rounds/cloudRoundID/scores/users
                JSONObject myRoundUserJSON = myRoundUsersJSON.getJSONObject(key);
                enQueue("/rounds/" + cloudRoundID + "/scores/users/" + key, myRoundUserJSON);
                //2.2. Write [roundID:updatedAt] into CDS/users/userID/rounds/
                roundIndexJSON.put(cloudRoundID, updatedAt);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        enQueue("/users/" + key + "/rounds/", roundIndexJSON);
                    }
                };
                Handler h = new Handler();
                h.postDelayed(r, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.


                //2.3. Delete CDS/users/userID/rounds/myRoundID
                Runnable r2 = new Runnable() {
                    @Override
                    public void run() {
                        enQueue("/users/" + key + "/rounds/" + myRoundID, null);
                    }
                };
                Handler h2 = new Handler();
                h2.postDelayed(r2, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.


            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("CloudArchery", "Could not get USER JSON from USERS JSON (ClubFirebase.mergeRounds)");
            }
        }

        //3. Update /CDS/rounds/cloudRoundID/updatedAt with new timestamp
        JSONObject updatedAtJSON = new JSONObject();
        try {
            updatedAtJSON.put("updatedAt", updatedAt);
            enQueue("/rounds/" + cloudRoundID, updatedAtJSON);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("CloudArchery", "Could not write updatedAt to JSON (ClubFirebase.mergeRounds)");
        }

        //4. Delete LDS/rounds/myRoundID
        dbR.deleteLocalRound(myRoundID);

        //5. Delete CDS/rounds/myRoundID
        Runnable r = new Runnable() {
            @Override
            public void run() {
                enQueue("rounds/" + myRoundID, null);
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.

    } //mergeRounds

    void createRound(final String newRoundID, Long newRoundUpdatedAt, final JSONObject newRoundJSON) {
        SQLiteRounds dbR = new SQLiteRounds(myContext);
        dbR.createLocalRound(newRoundID, newRoundJSON);
        JSONObject newRoundIndexJSON = new JSONObject();
        try {
            newRoundIndexJSON.put(newRoundID, newRoundUpdatedAt);
        } catch (JSONException e) {
            Log.e("CloudArchery", "Could not create Index JSON (ClubFirebase.createRound)");
            e.printStackTrace();
        }
        enQueue("/users/" + userID + "/rounds/", newRoundIndexJSON);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                enQueue("/rounds/" + newRoundID, newRoundJSON);
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, deQueueDelay); // <-- the deQueueDelay is the delay time in miliseconds.


    }

    public void changeUserIDinLDS(String newUserID) {
        //ToDo : Write code for changeUserIDinLDS
    }//changeUSerIDinLDS

    public void startLeaderboardListener(final String roundID) {
        if (syncOn && connected && authenticated && linked) {
            leaderBoardListener = myClubFirebaseRef.child("/rounds/" + roundID + "/scores/users/").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
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
                                Log.d("CloudArchery", "Downloading updated Round Data");
                                db.updateLocalRoundwithScores(thisUSerID, roundID, thisUserStatusJSON, thisUserDataJSON);
                            }
                            myScoreListener.onScoreUpdated(usersJSONArray);

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

    public void stopLeaderboardListener() {
        if ((myClubFirebaseRef != null) && (leaderBoardListener != null))
            myClubFirebaseRef.removeEventListener(leaderBoardListener);
    } //stopLeaderboardListener

    public void startJoinableRoundListener() {
        if (syncOn && connected && authenticated && linked) {
            myClubFirebaseRef.child("/rounds/").limitToLast(20).orderByChild("updatedAt").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //System.out.println(snapshot.getValue());
                    JSONArray roundsJSONArray = new JSONArray();
                    Map<String, JSONObject> roundsMap = (HashMap<String, JSONObject>) snapshot.getValue();
                    JSONObject roundsJSON = new JSONObject(roundsMap);

                    if ((roundsMap != null)) try {
                        Iterator x = roundsJSON.keys();
                        String thisRoundUserID = null;
                        while (x.hasNext()) {
                            String key = (String) x.next();
                            try {
                                JSONObject creatorJSON = roundsJSON.getJSONObject(key);
                                thisRoundUserID = creatorJSON.getJSONObject("creator").getString("id");
                            } catch (Throwable t) {
                                Log.e("CloudArchery", "Could not extract required data (ClubFirebase.startJoinableRoundListener)");
                                t.printStackTrace();
                            }
                            if (!thisRoundUserID.equals(userID))
                                roundsJSONArray.put(roundsJSON.get(key));
                        }

                        myJoinableRoundListener.onJoinableRoundChanged(roundsJSONArray);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.e("CloudArchery", "Error extracting JSON Data from Firebase Return (ClubFirebase.startJoinableRoundListener");
                    }
                }//StartJoinableRound

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("CloudArchery", "The read failed (ClubFirebase.startJoinableRoundListener) : " + firebaseError.getMessage());
                }
            });
        }
    } //startJoinablRoundListener

    public interface OnUserScoreUpdatedListener {
        public void onScoreUpdated(JSONArray newScoresJSON);
        //http://developer.android.com/training/basics/fragments/communicating.html
    }

    public interface OnRoundsListUpdatedListener {
        public void onRoundsListUpdated();
    }

    //ToDo : BUG : Dequeue happening right from first login - why?  should have no queue data? (Creates 17?)
    // ToDo : BUG : Does not seem to add upDatedAt then does not find one - Error getting LDSUpdateAt

    public interface OnConnectionListener {
        public void onConnectionUpdated(Boolean Network, Boolean CDSConnected, Boolean Authenticated, Boolean Linked, String ErrorMessage);
    }

    //ToDo: Bug : SyncDown does not seem to be updating LDSTime (e6fde16a example)

    public interface OnJoinableRoundChangedListener {
        public void onJoinableRoundChanged(JSONArray joinableRoundJSONArray);
        //http://developer.android.com/training/basics/fragments/communicating.html
    }

    public interface OnRoundTypesUpdatedListener {
        public void onRoundTypesUpdated();
    }

}
