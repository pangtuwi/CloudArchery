package com.cloudarchery.archersapp;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyClub extends Fragment {

    MyApp myAppState;

    TextView TV_clubInfo_club;
    TextView TV_clubInfo_name;
    TextView TV_clubInfo_manager;
    TextView TV_clubInfo_contact;
    TextView TV_clubInfo_clubBest;

    TextView statsText_updatedAt;
    TextView statsText_numMembers;
    TextView statsText_roundsCompleted;
    TextView statsText_totalArrows;
    TextView statsText_averageScore;
    TextView statsText_clubBest;

    String updatedAtString;
	public MyClub(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        myAppState = ((MyApp)getActivity().getApplicationContext());
        View rootView = inflater.inflate(R.layout.my_club, container, false);

        TV_clubInfo_club = (TextView) rootView.findViewById(R.id.myclub_textView_club);
        TV_clubInfo_name = (TextView) rootView.findViewById(R.id.myclub_textView_name);
        TV_clubInfo_manager = (TextView) rootView.findViewById(R.id.myclub_textView_manager);
        TV_clubInfo_contact = (TextView) rootView.findViewById(R.id.myclub_textView_contact);
        TV_clubInfo_clubBest = (TextView) rootView.findViewById(R.id.myclub_textview_bestscore);

        TV_clubInfo_club.setText("Club : "+myAppState.CDS.clubID);
        try {
            if (myAppState.CDS.myClubInfoJSON.has("longname")) TV_clubInfo_name.setText("Club Name : " +myAppState.CDS.myClubInfoJSON.getString("longname"));
            if (myAppState.CDS.myClubInfoJSON.has("manager")) TV_clubInfo_manager.setText("CloudArchery Manager : "+myAppState.CDS.myClubInfoJSON.getString("manager"));
            if (myAppState.CDS.myClubInfoJSON.has("contact")) TV_clubInfo_contact.setText("Club Contact : " + myAppState.CDS.myClubInfoJSON.getString("contact"));
        } catch (Exception e) {
            Log.e ("CloudArchery", "error getting club stats from JSON (MyClub.java)");
            e.printStackTrace();
        }

        statsText_updatedAt = (TextView) rootView.findViewById(R.id.myclub_textview_updatedat);
        statsText_numMembers = (TextView) rootView.findViewById(R.id.myclub_textview_nummembers);
        statsText_roundsCompleted = (TextView) rootView.findViewById(R.id.myclub_textview_roundscompleted);
        statsText_totalArrows = (TextView) rootView.findViewById(R.id.myclub_textview_totalarrows);
        statsText_averageScore = (TextView) rootView.findViewById(R.id.myclub_textview_average);
        statsText_clubBest = (TextView) rootView.findViewById(R.id.myclub_textview_bestscore);

        myAppState.CDS.myClubStatsUpdatedListener = new ClubFirebase.OnClubStatsUpdatedListener() {
            @Override
            public void onClubStatsUpdated(JSONObject clubStatsJSON) {

                try {
                    if (clubStatsJSON.has("updatedAt")) {
                        Long updatedAtLong = clubStatsJSON.optLong("updatedAt");
                        Date updatedAtDate = new Date(updatedAtLong);
                        String DATE_FORMAT_NOW = "EEEE dd MMMM yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                        updatedAtString = sdf.format(updatedAtDate);
                        statsText_updatedAt.setText("Statistics last updated on "+ updatedAtString);
                    }
                    if (clubStatsJSON.has("numUsers")) statsText_numMembers.setText("Number of Members : "+ clubStatsJSON.get("numUsers").toString());
                    if (clubStatsJSON.has("totalRoundsCompleted")) statsText_roundsCompleted.setText("Total Rounds Completed : "+ clubStatsJSON.get("totalRoundsCompleted").toString());
                    if (clubStatsJSON.has("totalArrows")) statsText_totalArrows.setText("Total Arrows Shot : "+ clubStatsJSON.get("totalArrows").toString());
                    if (clubStatsJSON.has("averageScore")) statsText_averageScore.setText("Average Score : "+ clubStatsJSON.get("averageScore").toString());
                    if (clubStatsJSON.has("bestScore")) {
                        statsText_clubBest.setText("Club Best : "+
                                        clubStatsJSON.getJSONObject("bestScore").getString("name") + " ("+
                                        clubStatsJSON.getJSONObject("bestScore").getString("roundName") +", "+
                                        clubStatsJSON.getJSONObject("bestScore").get("totalScore").toString() +")");
                    }
                } catch (Exception e) {
                    Log.e ("CloudArchery", "error getting club stats from JSON (MyClub.java)");
                    e.printStackTrace();
                }
            }
        };

        myAppState.CDS.fetchClubStatistics();

        return rootView;
    }


}
