package com.davidtwilliams.dtw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by paulwilliams on 02/02/15.
 */

public class SQLiteLocal  extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1000000001;
    private static final String DATABASE_NAME = "LocalRounds";

    // table name
    private static final String TABLE_ROUNDS = "rounds";
    private static final String TABLE_SCORES = "scores";

    // Table Columns names
    private static final String KEY_UUID = "uuid";
    private static final String KEY_SCORE = "scorejson";
    private static final String KEY_ROUND = "roundjson";


    private static final String[] COLUMNS_SCORES = {KEY_UUID, KEY_SCORE};
    private static final String[] COLUMNS_ROUNDS = {KEY_UUID, KEY_ROUND};

    Firebase myFirebaseRef;

    public SQLiteLocal(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_ROUNDS_TABLE = "CREATE TABLE rounds ( " +
                "uuid TEXT PRIMARY KEY, " +
                "roundjson TEXT)";
        String CREATE_SCORES_TABLE = "CREATE TABLE scores ( " +
                "uuid TEXT PRIMARY KEY, " +
                "scorejson TEXT)";

        db.execSQL(CREATE_ROUNDS_TABLE);
        db.execSQL(CREATE_SCORES_TABLE);
    } //OnCreate

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        //ToDO Allow user to save data as JSON before dropping
        db.execSQL("DROP TABLE IF EXISTS rounds");
        db.execSQL("DROP TABLE IF EXISTS scores");

        Log.d("MCCArchers", " * * * * * *  DATABASE VERSION CHANGE  * * * * * * * ");


        // create fresh tables
        this.onCreate(db);
    } //onUpgrade

    public void addLocalRound(LocalRound newRound){
        //for logging
        Log.d("MCCArchers", "Add Local Round: " + newRound.toString());

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_UUID, newRound.getUUID());
        values.put(KEY_ROUND, newRound.getDetailJSON().toString());

        // 3. insert
        db.insert(TABLE_ROUNDS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // Second Table   create ContentValues to add key "column"/value
        ContentValues values2 = new ContentValues();
        values2.put(KEY_UUID, newRound.getUUID());
        values2.put(KEY_SCORE, newRound.getScoreJSON().toString());

        // 3. insert
        db.insert(TABLE_SCORES, // table
                null, //nullColumnHack
                values2); // key/value -> keys = column names/ values = column values


        // 4. close
        db.close();
    } //addRound


    public LocalRound getLocalRound(String roundUUID){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor1 =
                db.query(TABLE_ROUNDS, // a. table
                        COLUMNS_ROUNDS, // b. column names
                        " uuid = ?", // c. selections
                        new String[] { String.valueOf(roundUUID) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        if (cursor1 != null)
            cursor1.moveToFirst();

        Cursor cursor2 =
                db.query(TABLE_SCORES, // a. table
                        COLUMNS_SCORES, // b. column names
                        " uuid = ?", // c. selections
                        new String[] { String.valueOf(roundUUID) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        if (cursor2 != null)
            cursor2.moveToFirst();


        LocalRound localRound = new LocalRound();
        localRound.setUUID(cursor1.getString(0));
        String detailJSONString = cursor1.getString(1);
        String scoreJSONString = cursor2.getString(1);
        try {
            JSONObject detailJSONObject = new JSONObject(detailJSONString);
            localRound.setDetailJSON(detailJSONObject);
            JSONObject scoreJSONObject = new JSONObject(scoreJSONString);
            localRound.setScoreJSON(scoreJSONObject);
        }  catch (Throwable t) {
            Log.e("MCCArchers", "Could not parse malformed JSON (Sqlite getLocalRound)");
            t.printStackTrace();
        }
        return localRound;
    } //getLocalRound

    public JSONArray getLocalRoundsJSONArray () {
        String query = "SELECT  * FROM " + TABLE_ROUNDS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row and add it to list
        //LocalRound localRound = null;
        JSONArray myJSONArray = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                //localRound = new LocalRound();
                //localRound.setUUID(cursor.getString(0));
                String detailJSONString = cursor.getString(1);
                try {
                    JSONObject detailJSON = new JSONObject(detailJSONString);
                    myJSONArray.put(detailJSON);
                    //myJSONArray += detailJSON;
                } catch (Throwable t) {
                    Log.e ("MCCArchers", "Could not make JSON Object from Sqlite String (SQLiteLocal.getLocalRoundsJSONArray");
                    t.printStackTrace();
                }
                //if (!cursor.isLast()){ myJSONArray +=", ";}
            } while (cursor.moveToNext());
        }
        //myJSONArray += "]";
        return myJSONArray;
    }

    public List<LocalRound> getAllLocalRounds() {
        List<LocalRound> localRounds = new LinkedList<LocalRound>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_ROUNDS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row and add it to list
        LocalRound localRound = null;
        if (cursor.moveToFirst()) {
            do {
                localRound = new LocalRound();

                localRound.setUUID(cursor.getString(0));

               // localRound.setDetailJSON(cursor.getString(1));

                localRounds.add(localRound);
            } while (cursor.moveToNext());
        }
        return localRounds;
    } //getAllLocalRounds

    public boolean syncLocalRound(LocalRound localRound) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectString = "SELECT * FROM " + TABLE_SCORES
                + " WHERE " + KEY_UUID + " = '" + localRound.getUUID()+"'";

        //String selectString = "SELECT * FROM " + TABLE_SCORES;
        Log.d("MCCArchers ", selectString);
        Cursor cursor = db.rawQuery(selectString, null);

        boolean hasObject = false;
        if(cursor.moveToFirst()){
            Log.d("MCCArchers ", "...found "+localRound.getUUID());
            hasObject = true;
            //TODO : Check for timestamp before updating
            updateLocalRound(localRound);
        } else {
            Log.d("MCCArchers ", "...NOT found "+localRound.getUUID());
            hasObject = false;
            addLocalRound(localRound);
        }
        cursor.close();          // Don't forget to close your cursor
        db.close();              //AND your Database!
        return hasObject;
    }

    public int updateLocalRound(LocalRound localRound) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        //values.put("uuid", localRound.getUUID());   ? I think I dont need if using update
        values.put("roundjson", localRound.getDetailJSON().toString());

        // 3. updating row
        int i = db.update(TABLE_ROUNDS, //table
                values, // column/value
                KEY_UUID+" = ?", // selections
                new String[] {localRound.getUUID()}); //selection args

        // 2. create ContentValues to add key "column"/value
        ContentValues values2 = new ContentValues();
        //values.put("uuid", localRound.getUUID());   ? I think I dont need if using update
        values2.put("scorejson", localRound.getScoreJSON().toString());

        // 3. updating row
        int j = db.update(TABLE_SCORES, //table
                values, // column/value
                KEY_UUID+" = ?", // selections
                new String[] {localRound.getUUID()}); //selection args

        // 4. close
        db.close();

        return i;

    }//updateLocalRound

    public void deleteLocalRound(LocalRound localRound) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_ROUNDS, //table name
                KEY_UUID+" = "+localRound.getUUID(), null);

        // 3. close
        db.close();

        //log
        Log.d("MCCArchers", "delete localRound: "+ localRound.toString());

    } //deleteLocalRound

    public void syncFirebase (String userID) {
        //Firebase.setAndroidContext(this);   //not needed - only once in the application (I think!)

        Context myContext = App.getContext();
        myFirebaseRef = new Firebase(myContext.getString(R.string.firebase_club_url));

        ValueEventListener mccArchers = myFirebaseRef.child("scores/" + userID + "/rounds/").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot snapshot) {

                try {
                    String myRoundJSONString = snapshot.getValue().toString();
                    JSONObject roundScores = new JSONObject(myRoundJSONString);

                    Iterator<?> keys = roundScores.keys();

                    while( keys.hasNext() ) {
                        final String thisRoundUUID = (String)keys.next();
                        //final String thisRoundUUID = key;
                        if ( roundScores.get(thisRoundUUID) instanceof JSONObject ) {
                            final JSONObject thisRoundScoreJSON = roundScores.getJSONObject(thisRoundUUID);
                            //final String thisRoundScoreJSON = String.valueOf(roundScores.getJSONObject(thisRoundUUID));//JSONObject.quote(thisScore.get(key).toString());
                            Log.d("MCCArchers ", "Firebase Sync : reading : " + thisRoundUUID + " : " + thisRoundScoreJSON);

                            myFirebaseRef.child("rounds/" + thisRoundUUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    try {
                                        //String thisRoundDetailJSONString = dataSnapshot.getValue().toString();
                                        final Map<String, Object> thisRoundDetailMap = (Map<String, Object>) dataSnapshot.getValue();

                                        JSONObject thisRoundDetailJSON = new JSONObject(thisRoundDetailMap);
                                        LocalRound firebaseRound = new LocalRound(thisRoundUUID, thisRoundScoreJSON, thisRoundDetailJSON);
                                        if (syncLocalRound(firebaseRound)) {
                                            Log.d("MCCArchers ", "Firebase Sync : match local round with UUID " + thisRoundUUID);
                                        } else {
                                            Log.d("MCCArchers ", "Firebase Sync : created local round with UUID " + thisRoundUUID);
                                        }
                                        ;
                                    } catch (Throwable t) {
                                        Log.e("MCCArchers", "Could not parse malformed JSON (SQLiteLocalJava.SyncFirebase.OnDataChange )");
                                        t.printStackTrace();
                                    }

                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });  //child rounds listener
                        }  // if instance of JSON
                    } // while keys.hasnext


                } catch (Throwable t) {
                    Log.e("MCCArchers", "Could not parse malformed JSON (SQLiteLocalJava.syncDatabse.OnDataChange(2)");
                    t.printStackTrace();
                }
            } //onDataChange

            @Override
            public void onCancelled(FirebaseError error) {
            }

        });

    } //syncFirebase

    public void saveScores (String roundID, JSONObject newScoreJSON) {

        //First Local SQLiteDatabase
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            //Check to make sure new timestamp is later than  DB timestamp
            int newScoreUpdatedAt = newScoreJSON.getInt("updatedAt");

            String selectString = "SELECT * FROM " + TABLE_SCORES
                + " WHERE " + KEY_UUID + " = '" + roundID + "'";
            Cursor cursor = db.rawQuery(selectString, null);
            cursor.moveToFirst();
            String dbCurrentScoreString = cursor.getString(1);
            JSONObject dbCurrentScoreJSON = new JSONObject(dbCurrentScoreString);
            int dbCurrentScoreUpdatedAt = dbCurrentScoreJSON.getInt("updatedAt");
            cursor.close();
            if (dbCurrentScoreUpdatedAt > newScoreUpdatedAt) {
                ContentValues values = new ContentValues();
                values.put("scorejson", newScoreJSON.toString());
                int i = db.update(TABLE_SCORES, //table
                        values, // column/value
                        KEY_UUID+" = ?", // selections
                        new String[] {roundID}); //selection args
            }

        } catch (Throwable t) {
            Log.e ("MCCArchers", "Could not update Scores (SQLiteLocal.saveScores");
            t.printStackTrace();
        }
                  // Don't forget to close your cursor
        db.close();              //AND your Database!


    } //SaveEnd


 /*   private static final String KEY_ID = "id";
    private static final String KEY_CLOUDID = "cloudid";
    private static final String KEY_ROUNDTYPEID = "roundtypeid";
    private static final String KEY_ROUNDTYPE = "roundtype";
    private static final String KEY_ROUNDDESCRIPTOR = "rounddescriptor";
    private static final String KEY_DATE = "date";
    private static final String KEY_NUMENDS = "numends";
    private static final String KEY_NUMARROWSPEREND = "numarrowsperend";
    private static final String KEY_CURRENTEND = "currentend";
    private static final String KEY_CURRENTARROW = "currentarrow";
    private static final String KEY_ARROWCOUNT = "arrowcount";
    private static final String KEY_ROUNDTOTAL = "roundtotal";
    private static final String KEY_ROUNDCOMPLETE = "roundcomplete";
    private static final String KEY_UPDATETIMEDATESTAMP = "updatetimedatestamp";
    private static final String KEY_ARROWDATA = "arrowdata";


    private static final String[] COLUMNS = {KEY_ID, KEY_CLOUDID, KEY_ROUNDTYPEID, KEY_ROUNDTYPE,
                                            KEY_ROUNDDESCRIPTOR,KEY_DATE,
                                            KEY_NUMENDS,KEY_NUMARROWSPEREND,
                                            KEY_CURRENTEND, KEY_CURRENTARROW, KEY_ARROWCOUNT,
                                            KEY_ROUNDTOTAL, KEY_ROUNDCOMPLETE, KEY_UPDATETIMEDATESTAMP,
                                            KEY_ARROWDATA};

    Firebase myFirebaseRef;

    public SQLiteLocal(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_ROUNDS_TABLE = "CREATE TABLE rounds ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "cloudid INT, "+
                "roundtypeid INT, "+
                "roundtype TEXT, "+
                "rounddescriptor TEXT, "+
                "date TEXT, "+
                "numends INT, "+
                "numarrowsperend INT, "+
                "currentend INT, "+
                "currentarrow INT, "+
                "arrowcount INT, "+
                "roundtotal INT, "+
                "roundcomplete INT, "+
                "updatetimedatestamp INT, "+
                "arrowdata TEXT)";
        //TODO : incorporate RoundTypeIDs into cloud version
        //TODO : incorporate RoundTypeIDs into interface
        db.execSQL(CREATE_ROUNDS_TABLE);
    } //OnCreate

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS rounds");
        //ToDO Allow user to save data as JSON before dropping
        // create fresh tables
        this.onCreate(db);
    } //onUpgrade

    public void addLocalRound(LocalRound round){
        //for logging
        Log.d("MCCArchers", "Add Local Round: " + round.toString());

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_CLOUDID, round.getCloudID());
        values.put(KEY_ROUNDTYPEID, round.getRoundTypeID());
        values.put(KEY_ROUNDTYPE, round.getRoundType());
        values.put(KEY_ROUNDDESCRIPTOR, round.getRoundDescriptor());
        values.put(KEY_DATE, round.getDate());
        values.put(KEY_NUMENDS, round.getNumEnds());
        values.put(KEY_NUMARROWSPEREND, round.getNumArrowsPerEnd());
        values.put(KEY_CURRENTEND, round.getCurrentEnd());
        values.put(KEY_CURRENTARROW, round.getCurrentArrow());
        values.put(KEY_ARROWCOUNT, round.getArrowCount());
        values.put(KEY_ROUNDTOTAL, round.getRoundTotal());
        values.put(KEY_ROUNDCOMPLETE, round.getRoundComplete());
        values.put(KEY_UPDATETIMEDATESTAMP, round.getUpdateTimeDateStamp());
        values.put(KEY_ARROWDATA, round.getArrowData());

        // 3. insert
        db.insert(TABLE_ROUNDS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    } //addRound


    public LocalRound getLocalRound(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_ROUNDS, // a. table
                        COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build object
        LocalRound localRound = new LocalRound();
        localRound.setId(Integer.parseInt(cursor.getString(0)));
        localRound.setCloudID(cursor.getString(1));
        localRound.setRoundTypeID(cursor.getInt(2));
        localRound.setRoundType(cursor.getString(3));
        localRound.setRoundDescriptor(cursor.getString(4));
        localRound.setDate(cursor.getString(5));
        localRound.setNumEnds(cursor.getInt(6));
        localRound.setNumArrowsPerEnd(cursor.getInt(7));
        localRound.setCurrentEnd(cursor.getInt(8));
        localRound.setCurrentArrow(cursor.getInt(9));
        localRound.setArrowCount(cursor.getInt(10));
        localRound.setRoundTotal(cursor.getInt(11));
        localRound.setRoundComplete((cursor.getInt(12) != 0));
        int dBTimeStamp = cursor.getInt(13);
        long dBTimeStampLong = (long)dBTimeStamp;
        localRound.setUpdateTimeDateStamp(dBTimeStampLong);
        localRound.setArrowData(cursor.getString(14));

        return localRound;
    } //getLocalRound

    public List<LocalRound> getAllLocalRounds() {
        List<LocalRound> localRounds = new LinkedList<LocalRound>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_ROUNDS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row and add it to list
        LocalRound localRound = null;
        if (cursor.moveToFirst()) {
            do {
                localRound = new LocalRound();

                localRound.setId(Integer.parseInt(cursor.getString(0)));
                localRound.setCloudID(cursor.getString(1));
                localRound.setRoundTypeID(cursor.getInt(2));
                localRound.setRoundType(cursor.getString(3));
                localRound.setRoundDescriptor(cursor.getString(4));
                localRound.setDate(cursor.getString(5));
                localRound.setNumEnds(cursor.getInt(6));
                localRound.setNumArrowsPerEnd(cursor.getInt(7));
                localRound.setCurrentEnd(cursor.getInt(8));
                localRound.setCurrentArrow(cursor.getInt(9));
                localRound.setArrowCount(cursor.getInt(10));
                localRound.setRoundTotal(cursor.getInt(11));
                localRound.setRoundComplete((cursor.getInt(12) !=0));
                int dBTimeStamp = cursor.getInt(13);
                long dBTimeStampLong = (long)dBTimeStamp;
                localRound.setUpdateTimeDateStamp(dBTimeStampLong);
                localRound.setArrowData(cursor.getString(14));

                localRounds.add(localRound);
            } while (cursor.moveToNext());
        }
        return localRounds;
    } //getAllLocalRounds

    public int updateLocalRound(LocalRound localRound) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("cloudid", localRound.getCloudID());
        values.put("roundtypeid", localRound.getRoundTypeID());
        values.put("roundtype", localRound.getRoundType());
        values.put("rounddescriptor", localRound.getRoundDescriptor());
        values.put("date", localRound.getDate());
        values.put("numends", localRound.getNumEnds());
        values.put("numarrowsperend", localRound.getNumArrowsPerEnd());
        values.put("currentend", localRound.getCurrentEnd());
        values.put("currentarrow", localRound.getCurrentArrow());
        values.put("arrowcount", localRound.getArrowCount());
        values.put("roundtotal", localRound.getRoundTotal());
        values.put("roundcomplete", localRound.getRoundComplete());
        values.put("updatetimedatestamp", localRound.getUpdateTimeDateStamp());
        values.put("arrowdata", localRound.getArrowData());

        // 3. updating row
        int i = db.update(TABLE_ROUNDS, //table
                values, // column/value
                KEY_ID+" = ?", // selections
                new String[] { String.valueOf(localRound.getId()) }); //selection args

        // 4. close
        db.close();

        return i;

    }//updateLocalRound

    public void deleteLocalRound(LocalRound localRound) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_ROUNDS, //table name
                KEY_ID+" = ?",  // selections
                new String[] { String.valueOf(localRound.getId()) }); //selections args

        // 3. close
        db.close();

        //log
        Log.d("MCCArchers", "delete localRound: "+ localRound.toString());

    } //deleteLocalRound

    public void getMyFirebaseRounds (String userID) {
        //Firebase.setAndroidContext(this);   //not needed - only once in the application (I think!)

        Context myContext = App.getContext();
        myFirebaseRef = new Firebase(myContext.getString(R.string.firebase_club_url));
                //.getResources().getString(R.string.text1)

        myFirebaseRef.child("scores/"+userID+"/rounds/").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot snapshot) {

                try {
                    Log.d("MCCArchers ", "Updating localDB with cloud Rounds..");
                    String myRoundJSONString = snapshot.getValue().toString();
                    Log.d("MCCArchers ", "read: " + myRoundJSONString);
                    final Map<String, Object> thisRound = (Map<String, Object>) snapshot.getValue();
                    String thisRoundID = snapshot.getKey();
                    myFirebaseRef.child("rounds/"+thisRoundID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String thisRoundDetail = dataSnapshot.getValue().toString();
                            Log.d("MCCArchers ", "read: " + thisRoundDetail);
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });

                } catch (Throwable t) {
                    Log.e("MCCArchers", "Could not parse malformed JSON ");
                    t.printStackTrace();
                }
            } //onDataChange

            @Override
            public void onCancelled(FirebaseError error) {
            }

        });
    }

    */
}

