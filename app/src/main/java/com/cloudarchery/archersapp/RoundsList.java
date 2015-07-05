package com.cloudarchery.archersapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;


//public class RoundsList extends Activity implements AdapterView.OnItemClickListener {
public class RoundsList extends Fragment implements AdapterView.OnItemClickListener {


    TextView mainTextView;
    ImageView imageViewCloudConnected;
    ImageView imageViewLoggedIn;
    ListView mainListView;
    JSONAdapterRoundsList mJSONAdapter;

    String userID = "";
    String roundID = "";

    MyApp myAppState;
    //ClubFirebase CDS;


    public RoundsList() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        userID = args.get("userID").toString();
        myAppState = ((MyApp) getActivity().getApplicationContext());


        View rootView = inflater.inflate(R.layout.rounds_list, container, false);
        getActivity().getActionBar().setTitle("CloudArchery");


        mainListView = (ListView) rootView.findViewById(R.id.main_listview);
        mainListView.setOnItemClickListener(this);
        mJSONAdapter = new JSONAdapterRoundsList(getActivity(), getActivity().getLayoutInflater());
        mainListView.setAdapter(mJSONAdapter);

        loadRoundData();

        myAppState.CDS.myRoundsListListener = new ClubFirebase.OnRoundsListUpdatedListener() {
            @Override
            public void onRoundsListUpdated() {
                loadRoundData();
                Log.d("CloudArchery", "loading round data");
            }
        };


        //Button
        rootView.findViewById(R.id.pink_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Clicked Floating Action Button", Toast.LENGTH_SHORT).show();
                //Bundle args = new Bundle();
                Fragment fragment = new NewRound();
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    //fragment.setArguments(args);
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment)
                            .addToBackStack("")
                            .commit();
                }
            }
        });

        return rootView;
    } //onCreateView


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        JSONObject roundJSON = mJSONAdapter.getItem(position);

        try {
            // String selectedRoundID = scoreRoundJSON.get("id").toString();
            //JSONObject  scoreDetailRoundJSON = roundJSON.getJSONObject("detail");
            roundID = roundJSON.getString("id");
            myAppState.setCurrentEnd(0);

            Bundle args = new Bundle();
            args.putString("roundID", roundID);
            //args.putString("userID", userID);
            Fragment fragment = new RoundScores();
            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                fragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .addToBackStack("")
                        .commit();
            }


        } catch (Throwable t) {
            Log.e("MCCArchers", "Error getting id from JSON (MainActivity.onItemClick)");
            t.printStackTrace();
        }
    }// OnItemClick


    public void loadRoundData() {

        //Log.d ("MCCArchers", "load local rounds.......");
        //String stringJSONLocalRounds = "";
        if (getActivity() != null) try {
            SQLiteRounds db = new SQLiteRounds(getActivity());
            JSONArray myLocalArray = db.getRoundsJSONArray();
            //Log.d("MCCArchers ", "JSON Parse OK");
            myAppState.setNumRounds(myLocalArray.length());
            mJSONAdapter.updateData(myLocalArray);
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("MCCArchers", "Could not parse JSON for local round data ");
        }

    } //loadRoundData


}
