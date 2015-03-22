package com.davidtwilliams.dtw;

import android.app.Application;
import android.content.Context;

/**
 * Created by paulwilliams on 17/03/15.
 */
public class App extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }
}
