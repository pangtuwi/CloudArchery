package com.cloudarchery.archersapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by paulwilliams on 09/04/15.
 */


public class SQLiteRoundTypes  extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "localroundtypes";

    // table name
    private static final String TABLE_NAME = "roundtypes";

    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_JSON = "jsonitem";
    private static final String[] COLUMNS_ITEMS = {KEY_ID, KEY_JSON};


    public SQLiteRoundTypes(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("MCCArchers", " * * *  CREATING ROUND TYPE DATABASE  * * * ");
        String CREATE_ITEMS_TABLE = "CREATE TABLE "+TABLE_NAME+" ( " +
                KEY_ID + " TEXT PRIMARY KEY, " +
                KEY_JSON + " TEXT)";
        db.execSQL(CREATE_ITEMS_TABLE);
    } //OnCreate

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        Log.d("MCCArchers", " * * * * * *  ROUND TYPE DATABASE VERSION CHANGE  * * * * * * * ");
        this.onCreate(db);
    } //onUpgrade

    public void deleteAllData() {
        // Drop older tables if existed
        Log.d("MCCArchers", " * * * * * *  DELETING ALL DATA FROM FIREBASE QUEUE DATABASE  * * * * * * * ");
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    } //deleteAllData



    public JSONArray getRoundTypesJSONArray() {
        //Generates a JSON Array of rounds used to create the interface in RoundTypes
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        JSONArray myUnsortedJSONArray = new JSONArray();
        //JSONArray mySortedJSONArray = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                String detailJSONString = cursor.getString(1);
                if (detailJSONString != null) try {
                    JSONObject detailJSON = new JSONObject(detailJSONString);
                    myUnsortedJSONArray.put(detailJSON);
                } catch (Throwable t) {
                    Log.e ("MCCArchers", "Could not make JSON Object from Sqlite String (SQLiteRoundTypes.getRoundTypesJSONArray");
                    t.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        db.close();
        return myUnsortedJSONArray;
    }//getRoundTypesJSONArray

    public JSONArray getFilteredRoundTypesJSONArray(boolean filterGNAS, boolean filterFITA, boolean filterCustom,
                                                    boolean filterIndoor, boolean filterOutdoor) {
        boolean isGNAS = true;
        boolean isFITA = true;
        boolean isCustom = true;
        boolean isIndoor = true;

        //Generates a Filtered JSON Array of rounds used to create the interface in RoundTypes
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        JSONArray myUnsortedJSONArray = new JSONArray();
        JSONArray mySortedJSONArray = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                String detailJSONString = cursor.getString(1);
                if (detailJSONString != null) try {
                    JSONObject detailJSON = new JSONObject(detailJSONString);
                    myUnsortedJSONArray.put(detailJSON);
                } catch (Throwable t) {
                    Log.e ("MCCArchers", "Could not make JSON Object from Sqlite String (SQLiteRoundTypes.getFilteredRoundTypesJSONArray");
                    t.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        try {
            Map <Integer, JSONObject> thisMap = new HashMap<Integer, JSONObject>();

            for (int i = 0; i < myUnsortedJSONArray.length(); i++) {

                JSONObject thisJSONObject = myUnsortedJSONArray.getJSONObject(i);
                try {
                    String classification = thisJSONObject.getString("classification");
                    isGNAS = classification.equals("GNAS");
                    isFITA = classification.equals("FITA");
                    isCustom = classification.equals("CUSTOM");
                    isIndoor = thisJSONObject.getBoolean("indoor");
                } catch (Throwable t) {
                    Log.e ("CloudArchery", "Could not get filter information from RoundType (SQLiteLocal.getRoundsJSONArray) ");
                }
                //JSONObject thisJSONObjectDetail = thisJSONObject.getJSONObject("detail");
                int displayOrder = 0;
                if (thisJSONObject.has("displayOrder")) displayOrder = thisJSONObject.getInt("displayOrder");
                while (thisMap.containsKey(displayOrder)) {
                    if (displayOrder < 1000) displayOrder = 1000;
                    displayOrder +=1;
                }
                if (((filterGNAS && isGNAS) || (filterFITA && isFITA) || (filterCustom && isCustom)) &&
                ((filterIndoor && isIndoor) || (filterOutdoor && !isIndoor))) {
                    thisMap.put(displayOrder, thisJSONObject);
                }
                //Log.d("CloudArchery", "Found RoundType in internal DB  :" + thisJSONObject.getString("name"));
            }
            List<Map.Entry<Integer, JSONObject>> entries =
                    new ArrayList<Map.Entry<Integer, JSONObject>>(thisMap.entrySet());
            Collections.sort(entries, new Comparator<Map.Entry<Integer, JSONObject>>() {
                public int compare(Map.Entry<Integer, JSONObject> a, Map.Entry<Integer, JSONObject> b) {
                    return a.getKey().compareTo(b.getKey());
                }
            });

            for (Map.Entry<Integer, JSONObject> entry : entries) {
                mySortedJSONArray.put(entry.getValue());
                //Log.d("CloudArchery", "Adding to List  :" + entry.getValue().getString("name"));
            }
        } catch (Throwable t){
            Log.e ("CloudArchery", "Could not make JSON Object from JSON Array (SQLiteLocal.getRoundsJSONArray");
            t.printStackTrace();
        }


        db.close();
        return mySortedJSONArray;
    }//getFilteredRoundTypesJSONArray

    public JSONObject getRoundTypeJSON (String roundTypeID){
        String roundTypeJSONString = "";
        JSONObject roundTypeJSONObject = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor1 = null;
        try {

            cursor1 =
                    db.query(TABLE_NAME, // a. table
                            COLUMNS_ITEMS, // b. column names
                            " id = ?", // c. selections
                            new String[]{String.valueOf(roundTypeID)}, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null); // h. limit

        } catch (Throwable t) {
            Log.e("MCCArchers", "Could not read from sqlite RT database (SqliteRoundTypes.getRoundTypeJSON)");
            t.printStackTrace();
        }
        if ((cursor1 != null) && (cursor1.getCount() > 0)) {
            cursor1.moveToFirst();
            roundTypeJSONString = cursor1.getString(1);

            try {
                roundTypeJSONObject = new JSONObject(roundTypeJSONString);
            }  catch (Throwable t) {
                Log.e("MCCArchers", "Could not parse malformed JSON (SqliteRoundTypes.getRoundTypeJSON)");
                t.printStackTrace();
            }
        }
        db.close();
        return roundTypeJSONObject;
    } //getRoundTypeJSON

    public void update (Map<String, JSONObject> newRoundTypeMap) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        JSONObject nJ = new JSONObject(newRoundTypeMap);
        Iterator it = newRoundTypeMap.entrySet().iterator();
        try {
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                String idString = pair.getKey().toString();
                String JSONString = nJ.getString(idString);
                values.put(KEY_ID, idString);
                values.put(KEY_JSON, JSONString);

                Cursor cursor1 =
                        db.query(TABLE_NAME, // a. table
                                COLUMNS_ITEMS, // b. column names
                                " id = ?", // c. selections
                                new String[]{idString}, // d. selections args
                                null, // e. group by
                                null, // f. having
                                null, // g. order by
                                null); // h. limit
                if ((cursor1 != null) && (cursor1.getCount() > 0)) {
                    //update
                    db.update(TABLE_NAME, // table
                            values, // column/value
                            KEY_ID + " = ?", // selections
                            new String[]{idString}); //selection args
                } else {
                    //insert
                    db.insert(TABLE_NAME, // table
                            null, //nullColumnHack
                            values); // key/value -> keys = column names/ values = column values
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("MCCArchers", "Could not make JSON Object from Map(SQLiteRoundTypes.upDate");
        }
        db.close();
    }//update

    public void createCustomRoundType (String roundTypeID, JSONObject newRoundTypeJSON) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, roundTypeID);
        values.put(KEY_JSON, newRoundTypeJSON.toString());

        db.insert(TABLE_NAME, //table
                null,
                values);
        db.close();
    }//createCustomRoundType

}