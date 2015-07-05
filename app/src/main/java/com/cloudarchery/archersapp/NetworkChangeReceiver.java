package com.cloudarchery.archersapp;

/**
 * Created by paulwilliams on 30/06/15.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    MyApp myAppState;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        myAppState = ((MyApp) context.getApplicationContext());

        // String status = NetworkUtil.getConnectivityStatusString(context);
        // Toast.makeText(context, status, Toast.LENGTH_LONG).show();
        if (NetworkUtil.getConnectivityStatusString(context).equals("enabled")
                && (myAppState.CDS.network == false)) myAppState.CDS.initialise();
        Log.d("CloudArchery", "Network state change");
    }
}