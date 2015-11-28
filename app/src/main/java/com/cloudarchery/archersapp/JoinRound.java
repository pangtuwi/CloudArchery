package com.cloudarchery.archersapp;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class JoinRound extends Fragment implements AdapterView.OnItemClickListener, ClubFirebase.OnJoinableRoundChangedListener{

    MyApp myAppState;
    ListView roundListView;
    TextView warningTextView;
    JSONAdapterJoinRoundList JSONAdapterJRL;

    public JoinRound(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.join_cloud_round, container, false);
        getActivity().getActionBar().setTitle("Join Cloud Round");
        myAppState = ((MyApp)getActivity().getApplicationContext());

        roundListView = (ListView) rootView.findViewById(R.id.joinround_listview);
        warningTextView = (TextView) rootView.findViewById(R.id.joinround_textview);
        roundListView.setOnItemClickListener(this);
        JSONAdapterJRL = new JSONAdapterJoinRoundList(getActivity(), getActivity().getLayoutInflater());
        roundListView.setAdapter(JSONAdapterJRL);
        myAppState.CDS.myJoinableRoundListener = new ClubFirebase.OnJoinableRoundChangedListener() {
            @Override
            public void onJoinableRoundChanged(JSONArray joinableRoundJSONArray) {
                JSONAdapterJRL.updateData(joinableRoundJSONArray);
                if (JSONAdapterJRL.getCount()>0) {
                    warningTextView.setVisibility(View.INVISIBLE);
                } else {
                    warningTextView.setVisibility(View.VISIBLE);
                }
            }
        };
        myAppState.CDS.startJoinableRoundListener();

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        JSONObject roundJSON = JSONAdapterJRL.getItem(position);

        try {
            String roundID = roundJSON.get("id").toString();
            int numEndsInt = roundJSON.getInt("numEnds");
            int numArrowsPerEndInt = roundJSON.getInt("numArrowsPerEnd");
            long updatedAt = System.currentTimeMillis();

            JSONArray newDataJSONArray = new JSONArray();
            for (int i=0; i<numEndsInt; i++) {
                JSONArray endArr = new JSONArray();
                for (int j = 0; j < numArrowsPerEndInt; j++) {
                    endArr.put(j, -1);
                }
                newDataJSONArray.put(i, endArr);
            }

            JSONObject newUserStatus = new JSONObject();
            newUserStatus.put("complete", false);
            newUserStatus.put("currentArrow", 0);
            newUserStatus.put("currentEnd", 0);
            newUserStatus.put("totalArrows", 0);
            newUserStatus.put("totalScore", 0);

            JSONObject newRoundUser = new JSONObject();
            newRoundUser.put("data", newDataJSONArray);
            newRoundUser.put("status", newUserStatus);
            newRoundUser.put ("name", myAppState.CDS.name);
            newRoundUser.put("updatedAt", updatedAt);

            myAppState.CDS.joinRound (roundID, updatedAt, newRoundUser);
            myAppState.setRoundID(roundID);
            myAppState.setCurrentEnd(0);


            getFragmentManager().popBackStackImmediate();
        } catch (Throwable t) {
            Log.e("MCCArchers", "Error getting RoundType id from JSON (JoinRound.onItemClick)");
            t.printStackTrace();
        }

    }

    @Override
    public void onJoinableRoundChanged(JSONArray joinableRoundJSONArray) {
       // JSONAdapterJRL.updateData(joinableRoundJSONArray);
    }

}