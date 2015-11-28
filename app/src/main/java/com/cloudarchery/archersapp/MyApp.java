package com.cloudarchery.archersapp;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * Created by paulwilliams on 17/03/15.
 */
public class MyApp extends Application {

    private static Context mContext;
    private int currentEnd;
    private int currentArrow;
    private int numRounds;
    private String roundTypeID;
    private String roundID;
    private static boolean activityVisible;
    private static boolean deQueueLoopMustRun;

    public static ClubFirebase CDS;


    //// TODO: 20/09/15 : Add Date to RoundScores Page 
    // TODO: 20/09/15 : double check if loading rounds in correct order
    // TODO: 20/09/15 multiple "could not extract required data (ClubFirebase.startJoinableRoundListener)
    //todo : all rounds shown as public
    //todo : BUG : club statistics not being updated

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.mContext = getApplicationContext(); //this;
        setCurrentEnd(0);
        CDS = new ClubFirebase();
        CDS.initialise(true);
        startDeQueueLoop();
    }


    public static Context getAppContext(){
        return MyApp.mContext;
    }

    public static void startDeQueueLoop() {
        //Set a handler to periodically check the connection and reinitialise if necessary
        deQueueLoopMustRun = true;
        final Handler connectionCheckHandler = new Handler();
        connectionCheckHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (CDS.authenticated) {
                    CDS.deQueue();
                } else {
                    CDS.CheckErrors();
                }
                if (CDS.syncError) {
                    Log.e("CloudArchery", "Sync Error Triggered - registered in MyApp.OnCreate.connectionCheckHandler");
                    CDS.sync();
                }
                if (deQueueLoopMustRun) connectionCheckHandler.postDelayed(this, 5000);
            }

        }, 5000);
    }

    public static void stopDeQueueLoop(){
        deQueueLoopMustRun = false;
    }//stopDeQueueLoop

    public void setNumRounds (int i) { numRounds = i;}
    public int getNumRounds () {return numRounds;}

    public boolean isConnected () {return CDS.authenticated;}

    public int getCurrentEnd(){return currentEnd;}
    public void setCurrentEnd(int i){
        currentEnd = i;
    }

    public int getCurrentArrow(){return currentArrow;}
    public void setCurrentArrow(int i){
        currentArrow = i;
    }

    public boolean hasRoundTypeID (){return (roundTypeID != null);}
    public String getRoundTypeID(){return roundTypeID;}
    public void setRoundTypeID(String newRoundTypeID){
        roundTypeID = newRoundTypeID;
    }

    public boolean hasRoundID (){return (roundID != null);}
    public String getRoundID(){return roundID;}
    public void setRoundID(String newRoundID){
        roundID = newRoundID;
    }

    public boolean isActivityVisible() {
        Log.d("CloudArchery", "activity status requested : is currently "+ activityVisible);
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
        startDeQueueLoop();
        Log.d("CloudArchery", "activity Resumed");
    }

    public static void activityPaused() {
        activityVisible = false;
        stopDeQueueLoop();
        Log.d("CloudArchery", "activity Paused");

    }

}
