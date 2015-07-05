package com.cloudarchery.archersapp;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
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

import java.util.Iterator;

public class MergeRound extends Fragment implements AdapterView.OnItemClickListener, ClubFirebase.OnJoinableRoundChangedListener {

    MyApp myAppState;
    String myRoundID; // This is the round I selected that I want to merge with the cloud Round
    String cloudRoundID; //This is the round I want to merge with.
    ListView roundListView;
    TextView warningTextView;
    JSONAdapterJoinRoundList JSONAdapterJRL;

    public MergeRound() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.join_cloud_round, container, false);
        getActivity().getActionBar().setTitle("Merge with Cloud Round");
        myAppState = ((MyApp) getActivity().getApplicationContext());

        Bundle args = getArguments();
        myRoundID = args.getString("roundID");

        roundListView = (ListView) rootView.findViewById(R.id.joinround_listview);
        warningTextView = (TextView) rootView.findViewById(R.id.joinround_textview);
        roundListView.setOnItemClickListener(this);
        JSONAdapterJRL = new JSONAdapterJoinRoundList(getActivity(), getActivity().getLayoutInflater());
        roundListView.setAdapter(JSONAdapterJRL);
        myAppState.CDS.myJoinableRoundListener = new ClubFirebase.OnJoinableRoundChangedListener() {
            @Override
            public void onJoinableRoundChanged(JSONArray joinableRoundJSONArray) {
                JSONAdapterJRL.updateData(joinableRoundJSONArray);
                if (JSONAdapterJRL.getCount() > 0) {
                    warningTextView.setVisibility(View.INVISIBLE);
                } else {
                    warningTextView.setVisibility(View.VISIBLE);
                }
            }
        };
        myAppState.CDS.startJoinableRoundListener();

        return rootView;
    }

    public boolean hasCommonKeys(JSONObject JA1, JSONObject JA2) {
        if (JA1 == JA2) return true;
        if (JA1 == null) return false;
        if (JA2 == null) return false;

        Iterator<String> keys = JA1.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (JA2.has(key)) return true;

            try {

            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("CloudArchery", "Error Comparing USer lists (MergeRound.hasCommonKeys");
            }
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Criteria for Merge
        // 1. I own this round
        // 2. I am connected to Cloud Database (must be Cloud Round to merge to)
        // 3. Selected rounds not same (cannot merge round with itself)
        // 4. Round to Merge to is Public Cloud Round (not local)
        // 5. Both rounds have same RoundTypeID
        // 6. Nobody has scores in both rounds.

        // Criteria 1 and 2 are checked in RoundScores.JSON
        JSONObject cloudRoundJSON = JSONAdapterJRL.getItem(position);
        SQLiteRounds LDS = new SQLiteRounds(getActivity());
        JSONObject myRoundJSON = LDS.getRoundJSON(myRoundID);

        try {
            cloudRoundID = cloudRoundJSON.get("id").toString();
        } catch (Throwable t) {
            Log.e("MCCArchers", "Error getting RoundType id from JSON (MergeRound.onItemClick)");
            t.printStackTrace();
        }
        if (!cloudRoundID.equals(myRoundID)) { //Criteria 3. Check to make sure you are not trying to merge a round with itself.
            boolean cloudRoundIsPublic = false;
            try {
                cloudRoundIsPublic = cloudRoundJSON.getBoolean("isPublic");
            } catch (Throwable t) {
                Log.e("CloudArchery", "error getting isPublic from round " + myRoundID + " (MergeRound.onItemClick)");
                t.printStackTrace();
            }
            if (cloudRoundIsPublic) {  // Criteria 4 : Check that round is public (should not have displayed non public rounds)
                String myRoundTypeID = null;
                String cloudRoundTypeID = null;
                try {
                    cloudRoundTypeID = cloudRoundJSON.getJSONObject("roundType").getString("id");
                    myRoundTypeID = myRoundJSON.getJSONObject("roundType").getString("id");
                } catch (Throwable t) {
                    Log.e("CloudArchery", "error getting roundType from round " + myRoundID + " (MergeRound.onItemClick)");
                    t.printStackTrace();
                }
                if (cloudRoundTypeID.equals(myRoundTypeID)) { //Criteria 5 : Both rounds have same RoundTypeID
                    JSONObject myRoundUsersJSON = null;
                    JSONObject cloudRoundUsersJSON = null;
                    try {
                        myRoundUsersJSON = myRoundJSON.getJSONObject("scores").getJSONObject("users");
                        cloudRoundUsersJSON = cloudRoundJSON.getJSONObject("scores").getJSONObject("users");
                    } catch (Throwable t) {
                        Log.e("CloudArchery", "error getting users JSON from round " + myRoundID + " (MergeRound.onItemClick)");
                        t.printStackTrace();
                    }
                    if (!hasCommonKeys(myRoundUsersJSON, cloudRoundUsersJSON)) { //Criteria 6 : Nobody has scores in both rounds
                        myAppState.CDS.mergeRounds(myRoundID, cloudRoundID);
                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Merge Round");
                        alert.setMessage("You cannot merge these rounds as some users have scores in both.  Merge will not take place");
                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            } //OnClick (dialog)
                        }); //OnClickListener
                        alert.show();
                    }

                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Merge Round");
                    alert.setMessage("You cannot merge these rounds as they do not have the same Round Type.  Merge will not take place");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        } //OnClick (dialog)
                    }); //OnClickListener
                    alert.show();
                }
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Merge Round");
                alert.setMessage("You cannot merge with this round as it is not Public.  Merge will not take place");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    } //OnClick (dialog)
                }); //OnClickListener
                alert.show();
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Merge Round");
            alert.setMessage("You cannot merge a round with itself.  Merge will not take place.");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                } //OnClick (dialog)
            }); //OnClickListener
            alert.show();
        }
        getFragmentManager().popBackStackImmediate();

    }

    @Override
    public void onJoinableRoundChanged(JSONArray joinableRoundJSONArray) {
        // JSONAdapterJRL.updateData(joinableRoundJSONArray);
    }

}