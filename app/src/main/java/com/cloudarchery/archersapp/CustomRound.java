package com.cloudarchery.archersapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by paulwilliams on 29/04/15.
 */
public class CustomRound extends Fragment {

    MyApp myAppState;
    EditText ETName;
    EditText ETDescription;
    EditText ETDistance;
    EditText ETTargetSize;
    EditText ETNumEnds;
    EditText ETNumArrowsPerEnd;
    RadioButton RBIndoor;
    RadioButton RBScore10Zone;

    public CustomRound(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.round_type_custom, container, false);
        getActivity().getActionBar().setTitle("Create Custom Round Type");
        myAppState = ((MyApp)getActivity().getApplicationContext());

        ETName = (EditText) rootView.findViewById(R.id.rtcustom_name);
        ETDescription = (EditText) rootView.findViewById(R.id.rtcustom_description);
        ETDistance = (EditText) rootView.findViewById(R.id.rtcustom_distance);
        ETTargetSize = (EditText) rootView.findViewById(R.id.rtcustom_targetsize);
        ETNumEnds = (EditText) rootView.findViewById(R.id.rtcustom_numends);
        ETNumArrowsPerEnd = (EditText) rootView.findViewById(R.id.rtcustom_numarrowsperend);
        RBIndoor = (RadioButton) rootView.findViewById(R.id.rtcustom_indoor);
        RBScore10Zone = (RadioButton) rootView.findViewById(R.id.rtcustom_score_10Zone);

        Button buttonCancel = (Button) rootView.findViewById(R.id.rtcustom_button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
            }
        });  //buttonCancel OnClickListener}

        Button buttonCreate = (Button) rootView.findViewById(R.id.rtcustom_button_create);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int inputNumEndsInt = 0;
                int inputNumArrowsPerEndInt = 0;
                boolean inputNumsAreInts = true;
                boolean inputStringsOK = true;
                try {

                    //Test to see if all fields are populated and Ok
                    try {
                        String inputNumEnds = ETNumEnds.getText().toString();
                        inputNumEndsInt = Integer.parseInt(inputNumEnds);
                        String inputNumArrowsPerEnd = ETNumArrowsPerEnd.getText().toString();
                        inputNumArrowsPerEndInt = Integer.parseInt(inputNumArrowsPerEnd);
                    } catch ( Throwable t){
                        inputNumsAreInts = false;
                    }

                    try {
                        String inputName = ETName.getText().toString();
                        String inputDescription = ETDescription.getText().toString();
                        String inputDistance = ETDistance.getText().toString();
                        String inputTargetSize = ETTargetSize.getText().toString();
                        inputStringsOK = (!inputName.equals("")
                                            && !inputDescription.equals("")
                                            && !inputDistance.equals("")
                                            && !inputTargetSize.equals(""));
                    } catch ( Throwable t){
                        inputStringsOK = false;
                    }

                    boolean inputOK =   (inputNumEndsInt>0) &&
                            (inputNumArrowsPerEndInt >0) &&
                            (inputNumsAreInts) &&
                            (inputStringsOK);

                    if (inputOK) {

                        final JSONObject newRoundTypeJSON = new JSONObject();
                        UUID roundtypeUUID = UUID.randomUUID();
                        final String roundTypeId = roundtypeUUID.toString();
                        myAppState.setRoundTypeID(roundTypeId);
                        newRoundTypeJSON.put("id", roundTypeId);
                        newRoundTypeJSON.put("classification", "CUSTOM");
                        newRoundTypeJSON.put("name", ETName.getText());
                        newRoundTypeJSON.put("description", ETDescription.getText());
                        newRoundTypeJSON.put("distance", ETDistance.getText());
                        newRoundTypeJSON.put("targetSize", ETTargetSize.getText());
                        newRoundTypeJSON.put("numEnds", ETNumEnds.getText());
                        newRoundTypeJSON.put("numArrowsPerEnd", ETNumArrowsPerEnd.getText());
                        newRoundTypeJSON.put("indoor", RBIndoor.isActivated());
                        if (RBScore10Zone.isActivated()) {
                            newRoundTypeJSON.put("scoring", "10 Zone");
                        } else {
                            newRoundTypeJSON.put("scoring", "5 Zone");
                        }

                        myAppState.CDS.createCustomRoundType(roundTypeId, newRoundTypeJSON);
                        myAppState.setRoundTypeID(roundTypeId);
                        getFragmentManager().popBackStackImmediate();

                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Custom Round Type");
                        alert.setMessage("Your custom round type cannot be created as you have not entered all data correctly ");
                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            } //OnClick (dialog)
                        }); //OnClickListener
                        alert.show();
                    }

                } catch (JSONException e) {
                    Log.e ("CloudArchery", "Could not create new Round Type JSON (CustomRound.onButtonCreate)");
                    e.printStackTrace();
                }


            }
        });  //buttonCancel OnClickListener}



        return rootView;
    }



}