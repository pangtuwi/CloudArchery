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


public class RoundsList extends Fragment implements AdapterView.OnItemClickListener{
    ImageView imageViewConnection;
    ListView mainListView;
    TextView textViewConnectionStatus;
    TextView warningTextView;
    JSONAdapterRoundsList mJSONAdapter;

    String userID = "";
    String roundID = "";

    MyApp myAppState;
    //ClubFirebase CDS;


    public RoundsList(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        userID = args.get("userID").toString();
        myAppState = ((MyApp)getActivity().getApplicationContext());


        View rootView = inflater.inflate(R.layout.rounds_list, container, false);
        getActivity().getActionBar().setTitle("CloudArchery");

        warningTextView = (TextView) rootView.findViewById(R.id.roundslist_textview);

        textViewConnectionStatus = (TextView) rootView.findViewById(R.id.roundslist_connectionstatus);
        imageViewConnection = (ImageView) rootView.findViewById(R.id.roundslist_status);
        mainListView = (ListView) rootView.findViewById(R.id.main_listview);
        mainListView.setOnItemClickListener(this);
        mJSONAdapter = new JSONAdapterRoundsList(getActivity(), getActivity().getLayoutInflater());
        mainListView.setAdapter(mJSONAdapter);

        loadRoundData();

        myAppState.CDS.myRoundsListListener = new ClubFirebase.OnRoundsListUpdatedListener() {
            @Override
            public void onRoundsListUpdated() {
                loadRoundData();
                //Log.d ("CloudArchery", "loading round data to list");
            }
        };

        myAppState.CDS.myConnectionListener = new ClubFirebase.OnConnectionListener() {
            @Override
            public void onConnectionUpdated(Boolean SyncOn, Boolean Network, Boolean CDSConnected, Boolean Authenticated, Boolean Linked, String ErrorMessage) {
                displayConnectionStatus(SyncOn, Network, CDSConnected, Authenticated, Linked, ErrorMessage);
            }
        }; //myConnectionListener


        //Create Round Button
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

        //Join Round Button
        if (myAppState.CDS.syncOn) {
            rootView.findViewById(R.id.roundslist_joinround).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment fragment = new JoinRound();
                    if (fragment != null) {
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.frame_container, fragment)
                                .addToBackStack("")
                                .commit();
                    }
                }
            });

            rootView.findViewById(R.id.roundslist_sync).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d ("CloudArchery", "Sync Triggered on button (RoundsList)");
                    myAppState.CDS.sync();
                }
            });
        } else {
            rootView.findViewById(R.id.roundslist_joinround).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.roundslist_sync).setVisibility(View.INVISIBLE);
        }
        displayConnectionStatus(myAppState.CDS.syncOn, myAppState.CDS.network, myAppState.CDS.connected, myAppState.CDS.authenticated, myAppState.CDS.linked, myAppState.CDS.firebaseError);
        return rootView;
    } //onCreateView

    public void displayConnectionStatus (Boolean SyncOn, Boolean Network, Boolean CDSConnected, Boolean Authenticated, Boolean Linked, String ErrorMessage) {

        if (Network == null) Network = false;
        if (CDSConnected == null) CDSConnected = false;
        if (Authenticated == null) Authenticated = false;
        if (Linked == null) Linked = false;

        if (SyncOn) {
            if (Network) {
                if (CDSConnected) {
                    if (Authenticated) {
                        if (Linked) {
                            textViewConnectionStatus.setText("connected, login OK");
                            imageViewConnection.setImageResource(R.drawable.ic_cloud_connected);
                        } else {
                            textViewConnectionStatus.setText("logged in but cannot synchronise");
                            imageViewConnection.setImageResource(R.drawable.ic_cloud_disconnected);
                        }
                    } else {
                        textViewConnectionStatus.setText("connected, checking login credentials");
                        imageViewConnection.setImageResource(R.drawable.ic_change_user);
                    }
                } else {
                    textViewConnectionStatus.setText("initialising database connection");
                    imageViewConnection.setImageResource(R.drawable.ic_change_user);
                }
            } else {
                textViewConnectionStatus.setText("no network connection");
                imageViewConnection.setImageResource(R.drawable.ic_action_nok);
            }
        } else {
            textViewConnectionStatus.setText("stand alone mode, no cloud sync");
            imageViewConnection.setImageResource(R.drawable.ic_action_nok);
        }
    }//displayConnectionStatus

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
            myAppState.setNumRounds(myLocalArray.length());
            mJSONAdapter.updateData(myLocalArray);
            if (mJSONAdapter.getCount()>0) {
                warningTextView.setVisibility(View.INVISIBLE);
            } else {
                warningTextView.setVisibility(View.VISIBLE);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("MCCArchers", "Could not parse JSON for local round data ");
        }

    } //loadRoundData

    @Override
    public void onDestroyView (){
        super.onDestroyView();
        myAppState.CDS.StopRoundChangeListener();
    } //onDestroyView


}
