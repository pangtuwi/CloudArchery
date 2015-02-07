package com.davidtwilliams.dtw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by paulwilliams on 02/02/15.
 */

public class SQLiteLocal  extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LocalRounds";

    // Books table name
    private static final String TABLE_ROUNDS = "rounds";
    private static final String TABLE_DATA = "data";

    // ROUNDS Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_ROUNDTYPE = "roundtype";
    private static final String KEY_DATE = "date";
    private static final String KEY_ENDS = "ends";
    private static final String KEY_ARROWSPEREND = "arrowsperend";

    private static final String[] COLUMNS = {KEY_ID,KEY_ROUNDTYPE,KEY_DATE,KEY_ENDS,KEY_ARROWSPEREND};

    public SQLiteLocal(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_ROUNDS_TABLE = "CREATE TABLE rounds ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "roundtype TEXT, "+
                "ends INT, "+
                "arrowsperend INT, "+
                "date TEXT )";

        String CREATE_ARROWS_TABLE = "CREATE TABLE data ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "roundid INT, "+
                "endid INT, "+
                "arrowid INT, "+
                "score INT )";


        // create books table
        db.execSQL(CREATE_ROUNDS_TABLE);
        db.execSQL(CREATE_ARROWS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS rounds");
        db.execSQL("DROP TABLE IF EXISTS arrows");

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
        values.put(KEY_ROUNDTYPE, round.getRoundtype());
        values.put(KEY_DATE, round.getDate());
        values.put(KEY_ENDS, round.getEnds());
        values.put(KEY_ARROWSPEREND, round.getArrowsperend());

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

        // 4. build book object
        LocalRound localRound = new LocalRound();
        localRound.setId(Integer.parseInt(cursor.getString(0)));
        localRound.setRoundtype(cursor.getString(1));
        localRound.setDate(cursor.getString(2));
        localRound.setEnds(cursor.getInt(3));
        localRound.setArrowsperend(cursor.getInt(4));

        //log
        Log.d("MCCArchers", "get Round(" + id + ")" + localRound.toString());

        // 5. return book
        return localRound;
    } //getLocalRound

    public List<LocalRound> getAllLocalRounds() {
        List<LocalRound> localRounds = new LinkedList<LocalRound>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_ROUNDS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        LocalRound localRound = null;
        if (cursor.moveToFirst()) {
            do {
                localRound = new LocalRound();
                localRound.setId(Integer.parseInt(cursor.getString(0)));
                localRound.setRoundtype(cursor.getString(1));
                localRound.setDate(cursor.getString(2));
                localRound.setEnds(cursor.getInt(3));
                localRound.setArrowsperend(cursor.getInt(4));

                // Add book to books
                localRounds.add(localRound);
            } while (cursor.moveToNext());
        }

        Log.d("MCCArchers","get All Rounds: "+localRounds.toString());

        // return books
        return localRounds;
    } //getAllLocalRounds

    public int updateLocalRound(LocalRound localRound) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("roundtype", localRound.getRoundtype());
        values.put("date", localRound.getDate());
        values.put("ends", localRound.getEnds());
        values.put("arrowsperend", localRound.getArrowsperend());

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

    }
}

