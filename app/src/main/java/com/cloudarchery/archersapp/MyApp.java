package com.cloudarchery.archersapp;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * Created by paulwilliams on 17/03/15.
 */
public class MyApp extends Application {

    private static Context mContext;
    public ClubFirebase CDS;
    private int currentEnd;
    private int currentArrow;
    private int numRounds;
    private String roundTypeID;
    private String roundID;

    public static Context getAppContext() {
        return MyApp.mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.mContext = getApplicationContext(); //this;
        setCurrentEnd(0);
        CDS = new ClubFirebase();
        CDS.initialise();

        //Set a handler to periodically check the connection and reinitialise if necessary
        final Handler connectionCheckHandler = new Handler();
        connectionCheckHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (CDS.authenticated) {
                    CDS.deQueue();
                } else {
                    CDS.CheckErrors();
                }
                connectionCheckHandler.postDelayed(this, 10000);
            }
        }, 10000);
    }

    public int getNumRounds() {
        return numRounds;
    }

    public void setNumRounds(int i) {
        numRounds = i;
    }

    public boolean isConnected() {
        return CDS.authenticated;
    }

    public int getCurrentEnd() {
        return currentEnd;
    }

    public void setCurrentEnd(int i) {
        currentEnd = i;
    }

    public int getCurrentArrow() {
        return currentArrow;
    }

    public void setCurrentArrow(int i) {
        currentArrow = i;
    }

    public boolean hasRoundTypeID() {
        return (roundTypeID != null);
    }

    public String getRoundTypeID() {
        return roundTypeID;
    }

    public void setRoundTypeID(String newRoundTypeID) {
        roundTypeID = newRoundTypeID;
    }

    public boolean hasRoundID() {
        return (roundID != null);
    }

    public String getRoundID() {
        return roundID;
    }

    public void setRoundID(String newRoundID) {
        roundID = newRoundID;
    }
}
