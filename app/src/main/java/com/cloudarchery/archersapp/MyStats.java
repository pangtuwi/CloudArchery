package com.cloudarchery.archersapp;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyStats extends Fragment {

    MyApp myAppState;

    TextView TV_myInfo_club;
    TextView TV_myInfo_name;
    TextView TV_myInfo_login;

    TextView TV_myStats_totalRounds;
    TextView TV_myStats_totalArrows;
    TextView TV_myStats_totalScore;
    TextView TV_myStats_Average;

    public MyStats(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myAppState = ((MyApp)getActivity().getApplicationContext());
        View rootView = inflater.inflate(R.layout.my_stats, container, false);

        TV_myInfo_club = (TextView) rootView.findViewById(R.id.mystats_textView_club);
        TV_myInfo_name = (TextView) rootView.findViewById(R.id.mystats_textView_name);
        TV_myInfo_login = (TextView) rootView.findViewById(R.id.mystats_textView_login);

        TV_myInfo_club.setText("Club : " + myAppState.CDS.clubID);
        TV_myInfo_login.setText("Login : " + myAppState.CDS.email);
        TV_myInfo_name.setText("Name : "  + myAppState.CDS.name);

        TV_myStats_totalRounds = (TextView) rootView.findViewById(R.id.mystats_textview_totalrounds);
        TV_myStats_totalArrows = (TextView) rootView.findViewById(R.id.mystats_textview_totalarrows);
        TV_myStats_totalScore = (TextView) rootView.findViewById(R.id.mystats_textview_totalscore);
        TV_myStats_Average = (TextView) rootView.findViewById(R.id.mystats_textview_average);


        try {
            SQLiteRounds db = new SQLiteRounds(getActivity());
            JSONObject myStats = db.getUserStatistics(myAppState.CDS.userID);
            TV_myStats_totalRounds.setText("Total Rounds Completed : " + myStats.getInt("TotalRounds"));
            TV_myStats_totalArrows.setText("Total Arrows Scored : " + myStats.getInt("TotalArrows"));
            TV_myStats_totalScore.setText("Total Score : " + myStats.getInt("TotalScore"));
            TV_myStats_Average.setText("Average Arrow Score : " + myStats.getString("Average"));
        } catch (JSONException e) {
            Log.e ("CloudArchery", "Error Getting User Stats from JSON");
            e.printStackTrace();
        }

        return rootView;
    }


}
