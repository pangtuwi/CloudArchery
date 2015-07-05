package com.cloudarchery.archersapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by paulwilliams on 02/02/15.
 */

public class SQLiteRounds extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1000000005;
    private static final String DATABASE_NAME = "LocalRounds";

    // table name
    private static final String TABLE_ROUNDS = "rounds";

    // Table Columns names
    private static final String KEY_ROUNDID = "roundid";
    private static final String KEY_ROUNDJSON = "roundjson";


    private static final String[] COLUMNS_ROUNDS = {KEY_ROUNDID, KEY_ROUNDJSON};

    public SQLiteRounds(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d("MCCArchers", " * * *  CREATING   DATABASE  * * * ");

        String CREATE_ROUNDS_TABLE = "CREATE TABLE rounds ( " +
                "roundid TEXT PRIMARY KEY, " +
                "roundjson TEXT)";

        db.execSQL(CREATE_ROUNDS_TABLE);
    } //OnCreate

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        // ToDo : FEATURE :  Allow user to save data as JSON before dropping
        db.execSQL("DROP TABLE IF EXISTS rounds");
        db.execSQL("DROP TABLE IF EXISTS scores");

        Log.d("MCCArchers", " * * * * * *  DATABASE VERSION CHANGE  * * * * * * * ");


        // create fresh tables
        this.onCreate(db);
    } //onUpgrade

    public void deleteAllData() {
        // Drop older tables if existed
        Log.d("MCCArchers", " * * * * * *  DELETING ALL DATA FROM DATABASE  * * * * * * * ");
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS rounds");
        db.execSQL("DROP TABLE IF EXISTS scores");
        this.onCreate(db);
    } //deleteAllData


    /*public JSONObject getUserJSON(String roundUUID){
        String userJSONString = "";
        JSONObject userJSONObject = null;
        Cursor cursor1 = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            cursor1 =
                    db.query(TABLE_ROUNDS, // a. table
                            COLUMNS_ROUNDS, // b. column names
                            " uuid = ?", // c. selections
                            new String[]{String.valueOf(roundUUID)}, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null); // h. limit
        } catch (Throwable t) {
            Log.e("MCCArchers", "Could not read from sqlite database (Sqlite getUserJSON)");
            t.printStackTrace();
        }
        if ((cursor1 != null) && (cursor1.getCount() > 0)) {
            cursor1.moveToFirst();
            userJSONString = cursor1.getString(2);

            try {
                 userJSONObject = new JSONObject(userJSONString);
            }  catch (Throwable t) {
                Log.e("MCCArchers", "Could not get JSON Object (Sqlite getUserJSON)");
                t.printStackTrace();
            }
        }
        if (userJSONObject!= null) Log.d("MCCArchers", "Read User JSON Object : "+userJSONObject.toString());
        return userJSONObject;
    } //getUserJSON
    */

    public JSONObject getRoundJSON(String roundID) {
        String roundJSONString = "";
        JSONObject roundJSONObject = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor1 = null; // h. limit
        try {

            cursor1 = db.query(TABLE_ROUNDS, // a. table
                    COLUMNS_ROUNDS, // b. column names
                    " roundid = ?", // c. selections
                    new String[]{String.valueOf(roundID)}, // d. selections args
                    null, // e. group by
                    null, // f. having
                    null, // g. order by
                    null);

        } catch (Exception e) {
            Log.e("MCCArchers", "Could not read from sqlite database (Sqlite getRoundJSON)");
            e.printStackTrace();
        }

        if ((cursor1 != null) && (cursor1.getCount() > 0)) {
            cursor1.moveToFirst();
            roundJSONString = cursor1.getString(1);
            Log.d("CloudArchery", "SQLiteRounds:  Read round JSON for " + roundID);

            try {
                roundJSONObject = new JSONObject(roundJSONString);
            } catch (Throwable t) {
                Log.e("MCCArchers", "Could not get JSON Object (Sqlite getRoundJSON) => read " + roundJSONString);
                t.printStackTrace();
            }

        }
        //if (roundJSONObject!=null)Log.d("MCCArchers", "Read Round JSON Object : "+roundJSONObject.toString());
        db.close();
        return roundJSONObject;
    } //getRoundJSON


    public JSONArray getRoundUsersJSONArray(String roundID) {
        //Creates JSON Array of users for a given Round, used for leaderboard display
        JSONArray myUnsortedJSONArray = new JSONArray();
        JSONObject roundJSON = getRoundJSON(roundID);
        JSONObject scoresJSON = null;
        JSONObject usersJSON = null;
        try {
            if (roundJSON.has("scores")) {
                scoresJSON = roundJSON.getJSONObject("scores");
                if (scoresJSON.has("users")) usersJSON = scoresJSON.getJSONObject("users");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Convert to JSONArray
        Iterator<?> keys = usersJSON.keys();
        while (keys.hasNext()) {
            final String userID = (String) keys.next();
            JSONObject userJSON = null;
            JSONObject userStatusJSON = null;
            try {
                if (usersJSON.has(userID)) {
                    userJSON = usersJSON.getJSONObject(userID);
                    myUnsortedJSONArray.put(userJSON);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }  //while keys.hasnext
        return myUnsortedJSONArray;
    }//getRoundUsersJSONArray

    public JSONArray getRoundsJSONArray() {
        //Generates a JSON Array of rounds used to create the interface in RoundsList
        String query = "SELECT * FROM " + TABLE_ROUNDS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        JSONArray myUnsortedJSONArray = new JSONArray();
        JSONArray mySortedJSONArray = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                String roundJSONString = cursor.getString(1);
                if (roundJSONString != null) try {
                    JSONObject detailJSON = new JSONObject(roundJSONString);
                    myUnsortedJSONArray.put(detailJSON);
                } catch (Throwable t) {
                    Log.e("MCCArchers", "Could not make JSON Object from Sqlite String (SQLiteLocal.getRoundsJSONArray");
                    t.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        try {
            Map<Long, JSONObject> thisMap = new HashMap<Long, JSONObject>();

            for (int i = 0; i < myUnsortedJSONArray.length(); i++) {

                JSONObject thisJSONObject = myUnsortedJSONArray.getJSONObject(i);
                //JSONObject thisJSONObjectDetail = thisJSONObject.getJSONObject("detail");
                Long createdAt = 0L;
                if (thisJSONObject.has("createdAt"))
                    createdAt = thisJSONObject.getLong("createdAt");
                while (thisMap.containsKey(createdAt)) {
                    createdAt += 1;
                }
                thisMap.put(createdAt, thisJSONObject);
            }
            List<Map.Entry<Long, JSONObject>> entries =
                    new ArrayList<Map.Entry<Long, JSONObject>>(thisMap.entrySet());
            Collections.sort(entries, new Comparator<Map.Entry<Long, JSONObject>>() {
                public int compare(Map.Entry<Long, JSONObject> a, Map.Entry<Long, JSONObject> b) {
                    return b.getKey().compareTo(a.getKey());
                }
            });

            for (Map.Entry<Long, JSONObject> entry : entries) {
                mySortedJSONArray.put(entry.getValue());
            }
        } catch (Throwable t) {
            Log.e("MCCArchers", "Could not make JSON Object from JSON Array (SQLiteLocal.getRoundsJSONArray");
            t.printStackTrace();
        }
        db.close();
        return mySortedJSONArray;
    }//getRoundsJSONArray


    public Map<String, JSONObject> getRoundMap() {
        //creates a map of roundid:roundJSON
        Map<String, JSONObject> roundMap = new HashMap<String, JSONObject>();
        String query = "SELECT  * FROM " + TABLE_ROUNDS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                try {
                    JSONObject dbCurrentUserJSON = new JSONObject(cursor.getString(1));
                    roundMap.put(cursor.getString(0), dbCurrentUserJSON);
                } catch (Throwable t) {
                    t.printStackTrace();
                    Log.e("MCCArchers", "convert local Round Data from db to JSON : " + cursor.getString(2));
                }
            } while (cursor.moveToNext());
        }
        db.close();
        return roundMap;
    } //getRoundMap

    public Map<String, Boolean> getRoundBooleanMap() {
        Map<String, Boolean> roundMap = new HashMap<String, Boolean>();
        String query = "SELECT  * FROM " + TABLE_ROUNDS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                roundMap.put(cursor.getString(0), false);
            } while (cursor.moveToNext());
        }
        db.close();
        return roundMap;
    } //getRoundMap

    public void createLocalRound(String roundID, JSONObject roundJSON) {
        //Creates new record in  LDS with just User Data
        // ToDo : RISK : Check if record exists in Database first, if so, then use updateLocalRound
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROUNDID, roundID);
        values.put(KEY_ROUNDJSON, roundJSON.toString());

        db.insert(TABLE_ROUNDS, //table
                null,
                values);

        db.close();
    }//createLocalRound

    public int updateLocalRound(String roundID, JSONObject roundJSON) {
        //Updates LDS with just User Data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROUNDJSON, roundJSON.toString());

        int i = db.update(TABLE_ROUNDS, //table
                values, // column/value
                KEY_ROUNDID + " = ?", // selections
                new String[]{roundID}); //selection args

        db.close();
        return i;
    }//updateLocalRound

    public void deleteLocalRound(String roundID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROUNDS,
                KEY_ROUNDID + " = ?",
                new String[]{roundID});
        db.close();
        //ToDo : BUG : Test if this delete works
    }//deleteLocalRound

  /*  public int updateLocalRoundwithRound(String roundID, JSONObject roundData ) {
        //Updates LDS with just Round Data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("roundjson", roundData.toString());

        int i = db.update(TABLE_ROUNDS, //table
                values, // column/value
                KEY_ROUNDID +" = ?", // selections
                new String[] {roundID}); //selection args

        db.close();
        return i;
    }//updateLocalRoundwithRound

    */

    public void updateLocalRoundwithScores(String userID, String roundID, JSONObject roundStatus, JSONArray roundData) {
        //Updates LDS with Score Status Data for given User
        //Get Current Round data from LDS
        JSONObject currentRoundJSON = getRoundJSON(roundID);
        JSONObject currentRoundThisUserJSON = new JSONObject();
        try {
            JSONObject currentRoundScoresJSON = currentRoundJSON.getJSONObject("scores");
            JSONObject currentRoundScoresUsersJSON = currentRoundScoresJSON.getJSONObject("users");
            if (currentRoundScoresUsersJSON.has(userID))
                currentRoundThisUserJSON = currentRoundScoresUsersJSON.getJSONObject(userID);
            currentRoundThisUserJSON.put("status", roundStatus);
            currentRoundThisUserJSON.put("data", roundData);
            currentRoundScoresUsersJSON.put(userID, currentRoundThisUserJSON);
            currentRoundScoresJSON.put("users", currentRoundScoresUsersJSON);
            currentRoundJSON.put("scores", currentRoundScoresJSON);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MCCArchers", "error insert new roundStatus into roundJSON (SQLiteLocal.updateLocaRoundWithStatus)");
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROUNDJSON, currentRoundJSON.toString());
        Log.d("CloudArchery", "SQLiteRounds:  Writing Round Score data : " + roundID);

        int i = db.update(TABLE_ROUNDS, //table
                values, // column/value
                KEY_ROUNDID + " = ?", // selections
                new String[]{roundID}); //selection args

        db.close();

    }//updateLocalRoundwithStatus

    public void setCurrentEnd(String roundID, String userID, int newCurrentEnd) {
        JSONObject currentRoundJSON = getRoundJSON(roundID);
        try {
            JSONObject currentRoundScoresJSON = currentRoundJSON.getJSONObject("scores");
            JSONObject currentRoundScoresUsersJSON = currentRoundScoresJSON.getJSONObject("users");
            if (currentRoundScoresUsersJSON.has(userID)) {
                JSONObject currentRoundScoresUsersUserJSON = currentRoundScoresUsersJSON.getJSONObject(userID);
                JSONObject currentRoundScoresUsersUserStatusJSON = currentRoundScoresUsersUserJSON.getJSONObject("status");
                currentRoundScoresUsersUserStatusJSON.put("currentEnd", newCurrentEnd);
                //currentRoundJSON.put("status", currentUserStatusJSON);
                updateLocalRound(roundID, currentRoundJSON);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("MCCArchers", "Could not update current End (SQLiteLocal.setCurrentEnd");
        }
    }//setCurrentEnd

    public boolean backupLocal() {
        boolean fileWriteOK = false;
        try {
            Long now = System.currentTimeMillis();
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!storageDir.isDirectory()) storageDir = new File("/sdcard");
            if (!storageDir.isDirectory()) storageDir = new File("/mnt/extSdCard");
            if (!storageDir.isDirectory()) storageDir = new File("/mnt/external_sd");

            if (storageDir.isDirectory()) {
                File myFile = File.createTempFile(
                        "/cloudarchery_backup_",  /* prefix */
                        ".txt",         /* suffix */
                        storageDir      /* directory */
                );

                myFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter =
                        new OutputStreamWriter(fOut);

                String query = "SELECT  * FROM " + TABLE_ROUNDS;
                SQLiteDatabase db = this.getWritableDatabase();
                Cursor cursor = db.rawQuery(query, null);
                myOutWriter.append("{");
                if (cursor.moveToFirst()) {
                    do {
                        myOutWriter.append("\"" + cursor.getString(0) + "\":" + cursor.getString(1));
                        if (cursor.isBeforeFirst()) myOutWriter.append(", \n");
                    } while (cursor.moveToNext());
                }
                myOutWriter.append("}");
                myOutWriter.close();
                fOut.close();
                db.close();
                fileWriteOK = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MCCArchers", " Error saving round data to local file (SQLiteRounds.backupLocal");
        }

        return fileWriteOK;
    }
}

