package com.cloudarchery.archersapp;


import android.app.Fragment;
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
    ListView roundTypeListView;
    JSONAdapterRoundTypes mJSONAdapterRT;

    public RoundTypeSelect(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.round_type_select, container, false);
        getActivity().getActionBar().setTitle("Select Round Type");
        myAppState = ((MyApp)getActivity().getApplicationContext());

        roundTypeListView = (ListView) rootView.findViewById(R.id.roundtype_listview);
        roundTypeListView.setOnItemClickListener(this);
        mJSONAdapterRT = new JSONAdapterRoundTypes(getActivity(), getActivity().getLayoutInflater());
        roundTypeListView.setAdapter(mJSONAdapterRT);

        String stringJSONRoundTypes = "";
        try {
            SQLiteRoundTypes dbRT = new SQLiteRoundTypes(getActivity());
            JSONArray myLocalArray = dbRT.getRoundTypesJSONArray();
            mJSONAdapterRT.updateData(myLocalArray);
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("MCCArchers", "Could not parse JSON for local round data (RoundTypeSelect.oncreate");
        }

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
}