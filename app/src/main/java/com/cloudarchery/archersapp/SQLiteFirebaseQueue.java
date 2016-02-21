package com.cloudarchery.archersapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by paulwilliams on 09/04/15.
 */


public class SQLiteFirebaseQueue  extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "firebasequeue";

    // table name
    private static final String TABLE_ITEMS = "items";

    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_REF = "firebaseref";
    private static final String KEY_JSON = "jsonitem";
    private static final String[] COLUMNS_ITEMS = {KEY_ID, KEY_REF, KEY_JSON};


    public SQLiteFirebaseQueue(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d("CloudArchery", " * * *  CREATING FIREBASE QUEUE DATABASE  * * * ");

        String CREATE_ITEMS_TABLE = "CREATE TABLE "+TABLE_ITEMS+" ( " +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_REF + " TEXT, " +
                KEY_JSON + " TEXT)";

        db.execSQL(CREATE_ITEMS_TABLE);

    } //OnCreate

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        Log.d("CloudArchery", " * * * * * *  FIREBASE QUEUE DATABASE VERSION CHANGE  * * * * * * * ");
        this.onCreate(db);
    } //onUpgrade

    public void deleteAllData() {
        // Drop older tables if existed
        Log.d("CloudArchery" , " * * * * * *  DELETING ALL DATA FROM FIREBASE QUEUE DATABASE  * * * * * * * ");
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        this.onCreate(db);
        db.close();
    } //deleteAllData

    public boolean queueisEmpty (){
        boolean isEmpty = false;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ITEMS;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            isEmpty = (cursor.getCount()==0);
        } else {
            isEmpty = true;
        }
        db.close();
        return isEmpty;
    }//queueHasItems


    public FirebaseQueueItem getNextQueueItem (){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ITEMS + " order by " + KEY_ID + " limit 1";
        Cursor cursor = db.rawQuery(query, null);
        if ((cursor != null) && (cursor.getCount() == 1)) {
            cursor.moveToFirst();
            JSONObject itemJSON = null;
            try {
                if (cursor.getString(2).equals("")) {
                    itemJSON = null;
                } else {
                    itemJSON = new JSONObject(cursor.getString(2));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e ("CloudArchery", "error reading JSON Object from Firebase SQLite Queue");
            }
            FirebaseQueueItem queueItem = new FirebaseQueueItem(cursor.getInt(0), cursor.getString(1),itemJSON );
            String myItem = null;
            if (itemJSON == null) {
                myItem = "";
            }  else {
                myItem = itemJSON.toString();
            }
            //Log.d ("CloudArchery", "Found Item in FirebaseQueue : item no "+ cursor.getInt(0)+".  REF : "+cursor.getString(1)+", JSON: "+myItem);
            db.close();
            return queueItem;
        }
        else {
            db.close();
            return null;
        }
    } //getNextQueueItem

    public void enQueue (String newFirebaseRef, JSONObject newJSONItem){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_REF, newFirebaseRef);
        if (newJSONItem != null) {
            values.put(KEY_JSON, newJSONItem.toString()) ;
        } else {
            values.put(KEY_JSON, "");
        }

        db.insert(TABLE_ITEMS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        db.close();
    } //enQueueJSONObject


    public void deQueue (int queueItemID){
        SQLiteDatabase db = this.getWritableDatabase();
        /*String query = "DELETE FROM " + TABLE_ITEMS +
                            " WHERE " + KEY_ID + "=" +
                            " (SELECT "+ KEY_ID +  " FROM "+ TABLE_ITEMS + " order by "+ KEY_ID + " limit 1)";
        Cursor cursor = db.rawQuery(query, null);*/
        db.delete(TABLE_ITEMS, //table name
                KEY_ID+" = "+queueItemID, null);
        //Log.d ("MCCArchers", "Deleted item from queue ("+queueItemID+")");
        db.close();
    } //deQueue

}