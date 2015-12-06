package com.cloudarchery.archersapp;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

public class RoundTypeSelect extends Fragment implements AdapterView.OnItemClickListener{

    MyApp myAppState;
    CheckBox CB_GNAS;
    CheckBox CB_FITA;
    CheckBox CB_CUSTOM;
    CheckBox CB_Indoor;
    CheckBox CB_Outdoor;

    boolean filter_GNAS = true;
    boolean filter_FITA = true;
    boolean filter_CUSTOM = true;
    boolean filter_Indoor = true;
    boolean filter_Outdoor = true;

    ListView roundTypeListView;
    JSONAdapterRoundTypes mJSONAdapterRT;
    JSONArray myFilteredJSONArray;

    public RoundTypeSelect(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.round_type_select, container, false);
        getActivity().getActionBar().setTitle("Select Round Type");
        myAppState = ((MyApp)getActivity().getApplicationContext());

        roundTypeListView = (ListView) rootView.findViewById(R.id.roundtype_listview);
        roundTypeListView.setOnItemClickListener(this);

        CB_GNAS = (CheckBox) rootView.findViewById(R.id.roundtypeselect_checkbox_GNAS);
        CB_FITA = (CheckBox) rootView.findViewById(R.id.roundtypeselect_checkbox_FITA);
        CB_CUSTOM = (CheckBox) rootView.findViewById(R.id.roundtypeselect_checkbox_CUSTOM);
        CB_Indoor = (CheckBox) rootView.findViewById(R.id.roundtypeselect_checkbox_indoor);
        CB_Outdoor = (CheckBox) rootView.findViewById(R.id.roundtypeselect_checkbox_outdoor);

        mJSONAdapterRT = new JSONAdapterRoundTypes(getActivity(), getActivity().getLayoutInflater());
        roundTypeListView.setAdapter(mJSONAdapterRT);

        //String stringJSONRoundTypes = "";
        myFilteredJSONArray = new JSONArray();
        try {
            SQLiteRoundTypes dbRT = new SQLiteRoundTypes(getActivity());
            JSONArray myLocalArray = dbRT.getFilteredRoundTypesJSONArray(true, true, true, true, true);
            for (int i = 0; i < myLocalArray.length(); i++) {
                JSONObject row = myLocalArray.getJSONObject(i);
                myFilteredJSONArray.put(row);
            }
            mJSONAdapterRT.updateData(myFilteredJSONArray);
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("MCCArchers", "Could not parse JSON for local round data (RoundTypeSelect.oncreate");
        }

        CB_GNAS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_GNAS =  ((CheckBox) v).isChecked();
                updateList();
            }
        });  //CB_GNAS.setonclicklistener

        CB_FITA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_FITA =  ((CheckBox) v).isChecked();
                updateList();
            }
        });  //CB_FITA.setonclicklistener

        CB_CUSTOM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_CUSTOM =  ((CheckBox) v).isChecked();
                updateList();
            }
        });  //CB_CUSTOM.setonclicklistener

        CB_Indoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_Indoor = ((CheckBox) v).isChecked();
                updateList();
            }
        });  //CB_Indoor.setonclicklistener

        CB_Outdoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_Outdoor =  ((CheckBox) v).isChecked();
                updateList();
            }
        });  //CB_OutDoor.setonclicklistener
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        JSONObject roundTypeJSON = mJSONAdapterRT.getItem(position);

        try {
            String roundTypeID = roundTypeJSON.get("id").toString();
            myAppState.setRoundTypeID(roundTypeID);
            getFragmentManager().popBackStackImmediate();
        } catch (Throwable t) {
            Log.e("MCCArchers", "Error getting RoundType id from JSON (RoundTypeSelect.onItemClick)");
            t.printStackTrace();
        }

    }


    private void updateList (){
        myFilteredJSONArray = new JSONArray();
        try {
            SQLiteRoundTypes dbRT = new SQLiteRoundTypes(getActivity());
            JSONArray myLocalArray = dbRT.getFilteredRoundTypesJSONArray(filter_GNAS, filter_FITA, filter_CUSTOM, filter_Indoor, filter_Outdoor);
            for (int i = 0; i < myLocalArray.length(); i++) {
                JSONObject row = myLocalArray.getJSONObject(i);
                myFilteredJSONArray.put(row);
            }
            mJSONAdapterRT.updateData(myFilteredJSONArray);
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("MCCArchers", "Could not parse JSON for local round data (RoundTypeSelect.oncreate");
        }
    }

}