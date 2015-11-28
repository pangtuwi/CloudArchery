package com.cloudarchery.archersapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class NewRound extends Fragment {

    JSONObject newRound;
    JSONObject newRoundType;
    MyApp myAppState;

    EditText editTextNumEnds;
    EditText editTextNumArrowsPerEnd;
    EditText editTextRoundDescription;
    EditText editTextRoundTypeName;
    EditText editTextComment;
    Switch switchIsPublic;
    String roundTypeId;


	public NewRound(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.new_round, container, false);
        getActivity().getActionBar().setTitle("Create New Round");
        myAppState = ((MyApp)getActivity().getApplicationContext());

        roundTypeId = null;
        editTextNumEnds = (EditText) rootView.findViewById(R.id.editTextNumEnds);
        editTextNumArrowsPerEnd = (EditText) rootView.findViewById(R.id.editTextNumArrowsPerEnd);
        editTextRoundDescription = (EditText) rootView.findViewById(R.id.editTextRoundDescription);
        editTextRoundTypeName = (EditText) rootView.findViewById(R.id.edittext_newround_name);
        editTextComment = (EditText) rootView.findViewById(R.id.editTextComment);
        switchIsPublic = (Switch) rootView.findViewById(R.id.switchIsPublic);

        editTextNumEnds.setEnabled(false);
        editTextNumArrowsPerEnd.setEnabled(false);
        editTextRoundDescription.setEnabled(false);
        editTextRoundTypeName.setEnabled(false);

        Button buttonSelectPreDefined = (Button) rootView.findViewById(R.id.newround_button_select_roundtype);
        buttonSelectPreDefined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment fragment = new RoundTypeSelect();
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment)
                            .addToBackStack("")
                            .commit();
                }
            }
        });  //buttonPreDefined OnClickListener}

        Button buttonCustom = (Button) rootView.findViewById(R.id.newround_button_custom_roundtype);
        buttonCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment fragment = new CustomRound();
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment)
                            .addToBackStack("")
                            .commit();
                }
            }

        });  //buttonCustom OnClickListener}

        Button buttonCancel = (Button) rootView.findViewById(R.id.newround_button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
            }
        });  //buttonCancel OnClickListener}

        // - - - - - - - - - - - - - CREATE THE ROUND - - - - - - - - - - - - - - - - - - - -

        Button buttonCreate = (Button) rootView.findViewById(R.id.newround_button_create);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String inputNumEnds = editTextNumEnds.toString();
                //String inputNumArrowsPerEnd = editTextNumArrowsPerEnd.getText().toString();
                //String inputRoundTypeName = editTextRoundTypeName.getText().toString();
                //String inputRoundDescription = editTextRoundDescription.getText().toString();


                boolean isPublic = switchIsPublic.isChecked();
                String comment = editTextComment.getText().toString();

                JSONObject newRoundDetail = new JSONObject();
                Boolean dataCheckOK = false;
                dataCheckOK = true;
                //Todo : (RELEASE > 2) More comprehensive check of round Type Data

                 if ((dataCheckOK) && (newRoundType != null)){
                     try {
                         //Create Round
                         long createdAt = System.currentTimeMillis();
                         newRoundDetail.put("createdAt", createdAt);
                         newRoundDetail.put("updatedAt", createdAt);
                         newRoundDetail.put("comment", comment);

                         UUID uuid = UUID.randomUUID();
                         final String id = uuid.toString();
                         newRoundDetail.put("id", id);

                         JSONObject newRoundCreator = new JSONObject();
                         newRoundCreator.put("name", myAppState.CDS.getName());
                         newRoundCreator.put("id", myAppState.CDS.getUserID());
                         newRoundDetail.put("creator", newRoundCreator);

                         newRoundDetail.put("isPublic", isPublic);

                         int numEndsInt = newRoundType.getInt("numEnds");
                         int numArrowsPerEndInt = newRoundType.getInt("numArrowsPerEnd");

                         newRoundDetail.put("numEnds", numEndsInt);
                         newRoundDetail.put("numArrowsPerEnd", numArrowsPerEndInt);

                         JSONObject newRoundInsertType = new JSONObject();
                         newRoundInsertType.put("id", newRoundType.get("id"));
                         newRoundInsertType.put("name", newRoundType.get("name"));
                         newRoundInsertType.put("description", newRoundType.get("description"));
                         newRoundDetail.put("roundType", newRoundInsertType);

                         JSONArray newDataJSONArray = new JSONArray();
                         for (int i = 0; i < numEndsInt; i++) {
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
                         newRoundUser.put("name", myAppState.CDS.name);
                         newRoundUser.put("updatedAt", createdAt);

                         JSONObject newRoundUsers = new JSONObject();
                         newRoundUsers.put(myAppState.CDS.userID, newRoundUser);

                         JSONObject newRoundScores = new JSONObject();
                         newRoundScores.put("updatedAt", createdAt); //use same time as CreatedAt above.
                         newRoundScores.put("users", newRoundUsers);
                         newRoundDetail.put("scores", newRoundScores);

                         myAppState.CDS.createRound(id, createdAt, newRoundDetail);

                         myAppState.setCurrentEnd(0);

                         Bundle args = new Bundle();
                         args.putString("roundID", id);
                        /* Fragment fragment = new RoundScores();
                         if (fragment != null) {
                             FragmentManager fragmentManager = getFragmentManager();
                             fragment.setArguments(args);
                             fragmentManager.beginTransaction()
                                     .replace(R.id.frame_container, fragment)
                                     .addToBackStack("")
                                     .commit();
                         }*/
                         getFragmentManager().popBackStackImmediate();

                     } catch (JSONException e) {
                         e.printStackTrace();
                         Log.e("MCCArchers", "error creating new round JSON (NewRound.buttonCreate.onClick)");
                         Toast.makeText(getActivity(), "Could not create new Round", Toast.LENGTH_LONG).show();
                     }
                 } else {

                     AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                     alert.setTitle("Create New Round");
                     alert.setMessage("Cannot create new round as no Round Type has been selected)");
                     alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton) {
                         } //OnClick (dialog)
                     }); //OnClickListener
                     alert.show();

                 }
                    //getFragmentManager().popBackStackImmediate();

            }
        });  //buttonCreatel OnClickListener}

        return rootView;
    } //onCreateView

    @Override
    public void onStart() {
        //ToDo : (RELEASE > 2) Improve : passing of newRoundTypeID etc. should be via Bundle args, not via myAppState
        super.onStart();
        if (myAppState.hasRoundTypeID()) {
            SQLiteRoundTypes dbRT = new SQLiteRoundTypes(getActivity());
            newRoundType = dbRT.getRoundTypeJSON(myAppState.getRoundTypeID());

            try {
                roundTypeId = newRoundType.optString("id");

                int numEnds = newRoundType.optInt("numEnds");
                String numEndsStr = ""+numEnds;
                editTextNumEnds.setText(numEndsStr);

                int numArrowsPerEnd = newRoundType.optInt("numArrowsPerEnd");
                String numArrowsPerEndStr = ""+numArrowsPerEnd;
                editTextNumArrowsPerEnd.setText(numArrowsPerEndStr);

                String description = newRoundType.optString("description");
                editTextRoundDescription.setText(description);

                String roundTypeName = newRoundType.optString("name");
                editTextRoundTypeName.setText(roundTypeName);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MCCArchers", "Error getting RoundType detail from JSON (NewRound.onStart)");
            }

            myAppState.setRoundTypeID(null);
        }

    }

    private String createJSONDataArray (int numEnds, int numArrowsPerEnd) {
        String JSONString = "{\"data\":[";
        for (int i=0; i<numEnds; i++){
            String s = "[";
            for (int j=0; j < numArrowsPerEnd; j++){
                s += "-1";
                if (j!=(numArrowsPerEnd-1)) s+= ",";
            }
            JSONString+=s;
            JSONString+="]";
            if (i!=(numArrowsPerEnd-1)) {
                JSONString+= ",";
            } else {
                JSONString+= "], \"score\": 0}";
            }
        }
        Log.d("MCCArchers", "created JSON :" + JSONString);
        return JSONString;
    }  //createJSONRoundScores
}
