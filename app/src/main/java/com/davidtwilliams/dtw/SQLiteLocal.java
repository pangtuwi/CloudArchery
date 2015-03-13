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
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "LocalRounds";

    // Books table name
    private static final String TABLE_ROUNDS = "rounds";

    // ROUNDS Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_ROUNDTYPEID = "roundtypeid";
    private static final String KEY_ROUNDTYPE = "roundtype";
    private static final String KEY_ROUNDDESCRIPTOR = "rounddescriptor";
    private static final String KEY_DATE = "date";
    private static final String KEY_ENDS = "ends";
    private static final String KEY_ARROWSPEREND = "arrowsperend";
    private static final String KEY_CURRENTEND = "currentend";
    private static final String KEY_CURRENTARROW = "currentarrow";
    private static final String KEY_ARROWCOUNT = "arrowcount";
    private static final String KEY_ARROWDATA = "arrowdata";


    private static final String[] COLUMNS = {KEY_ID,KEY_ROUNDTYPEID, KEY_ROUNDTYPE, KEY_ROUNDDESCRIPTOR,
                                            KEY_DATE,KEY_ENDS,KEY_ARROWSPEREND,
                                            KEY_CURRENTEND, KEY_CURRENTARROW, KEY_ARROWCOUNT, KEY_ARROWDATA};


    public SQLiteLocal(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_ROUNDS_TABLE = "CREATE TABLE rounds ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "roundtypeid INT, "+
                "roundtype TEXT, "+
                "rounddescriptor TEXT, "+
                "date TEXT, "+
                "ends INT, "+
                "arrowsperend INT, "+
                "currentend INT, "+
                "currentarrow INT, "+
                "arrowcount INT, "+
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
        values.put(KEY_ROUNDTYPEID, round.getRoundTypeID());
        values.put(KEY_ROUNDTYPE, round.getRoundType());
        values.put(KEY_ROUNDDESCRIPTOR, round.getRoundDescriptor());
        values.put(KEY_DATE, round.getDate());
        values.put(KEY_ENDS, round.getEnds());
        values.put(KEY_ARROWSPEREND, round.getArrowsPerEnd());
        values.put(KEY_CURRENTEND, round.getCurrentEnd());
        values.put(KEY_CURRENTARROW, round.getCurrentArrow());
        values.put(KEY_ARROWCOUNT, round.getArrowCount());
        values.put(KEY_ARROWDATA, round.getArrowData());

        // 3. insert
        db.insert(TABLE_ROUNDS, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values
        //TODO What is a nullColumnHAck
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
        localRound.setRoundTypeID(Integer.parseInt(cursor.getString(1)));
        localRound.setRoundType(cursor.getString(2));
        localRound.setRoundDescriptor(cursor.getString(3));
        localRound.setDate(cursor.getString(4));
        localRound.setEnds(cursor.getInt(5));
        localRound.setArrowsPerEnd(cursor.getInt(6));
        localRound.setCurrentEnd(cursor.getInt(7));
        localRound.setCurrentArrow(cursor.getInt(8));
        localRound.setArrowCount(cursor.getInt(9));
        localRound.setArrowData(cursor.getString(10));

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
                localRound.setRoundTypeID(Integer.parseInt(cursor.getString(1)));
                localRound.setRoundType(cursor.getString(2));
                localRound.setRoundDescriptor(cursor.getString(3));
                localRound.setDate(cursor.getString(4));
                localRound.setEnds(cursor.getInt(5));
                localRound.setArrowsPerEnd(cursor.getInt(6));
                localRound.setCurrentEnd(cursor.getInt(7));
                localRound.setCurrentArrow(cursor.getInt(8));
                localRound.setArrowCount(cursor.getInt(9));
                localRound.setArrowData(cursor.getString(10));

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
        values.put("roundtypeid", localRound.getRoundTypeID());
        values.put("roundtype", localRound.getRoundType());
        values.put("rounddescriptor", localRound.getRoundDescriptor());
        values.put("date", localRound.getDate());
        values.put("ends", localRound.getEnds());
        values.put("arrowsperend", localRound.getArrowsPerEnd());
        values.put("currentend", localRound.getCurrentEnd());
        values.put("currentarrow", localRound.getCurrentArrow());
        values.put("arrowcount", localRound.getArrowCount());
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
}

