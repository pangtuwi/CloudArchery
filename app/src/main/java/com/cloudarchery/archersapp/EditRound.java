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
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class EditRound extends Fragment {
    String roundID;
    String roundTypeID;
    JSONObject roundJSON;
    JSONObject roundTypeJSON;
    MyApp myAppState;

    EditText editTextNumEnds;
    EditText editTextNumArrowsPerEnd;
    EditText editTextRoundDescription;
    EditText editTextRoundTypeName;
    EditText editTextComment;
    Switch switchIsPublic;
    String roundTypeId;


    public EditRound(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.edit_round, container, false);
        getActivity().getActionBar().setTitle("Editing Round");
        myAppState = ((MyApp)getActivity().getApplicationContext());

        editTextNumEnds = (EditText) rootView.findViewById(R.id.editround_edittext_numends);
        editTextNumArrowsPerEnd = (EditText) rootView.findViewById(R.id.editround_edittext_numarrowsperend);
        editTextRoundDescription = (EditText) rootView.findViewById(R.id.editround_edittext_rounddescription);
        editTextRoundTypeName = (EditText) rootView.findViewById(R.id.editround_edittext_roundname);
        editTextComment = (EditText) rootView.findViewById(R.id.editround_edittext_comment);
        switchIsPublic = (Switch) rootView.findViewById(R.id.editround_switch_ispublic);

        editTextNumEnds.setEnabled(false);
        editTextNumArrowsPerEnd.setEnabled(false);
        editTextRoundDescription.setEnabled(false);
        editTextRoundTypeName.setEnabled(false);

        Bundle args = getArguments();
        if (args.containsKey("roundID")) {
            roundID = args.get("roundID").toString();
            SQLiteRounds LDS = new SQLiteRounds(getActivity());
            roundJSON = LDS.getRoundJSON(roundID);
            try {
                boolean isPublic = roundJSON.getBoolean("isPublic");
                switchIsPublic.setChecked(isPublic);
                if (roundJSON.has("comment")){
                    String comment = roundJSON.getString("comment");
                    editTextComment.setText(comment);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e ("CloudArchery", "Could not read isPublic from round " + roundID + "(EditRound.OnCreate)");
            }
        } else roundID = null;
        if (args.containsKey("roundTypeID")) {
            roundTypeID = args.get("roundTypeID").toString();
            SQLiteRoundTypes dbRT = new SQLiteRoundTypes(getActivity());
            roundTypeJSON = dbRT.getRoundTypeJSON(myAppState.getRoundTypeID());

            try {
                roundTypeId = roundTypeJSON.optString("id");

                int numEnds = roundTypeJSON.optInt("numEnds");
                String numEndsStr = ""+numEnds;
                editTextNumEnds.setText(numEndsStr);

                int numArrowsPerEnd = roundTypeJSON.optInt("numArrowsPerEnd");
                String numArrowsPerEndStr = ""+numArrowsPerEnd;
                editTextNumArrowsPerEnd.setText(numArrowsPerEndStr);

                String description = roundTypeJSON.optString("description");
                editTextRoundDescription.setText(description);

                String roundTypeName = roundTypeJSON.optString("name");
                editTextRoundTypeName.setText(roundTypeName);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MCCArchers", "Error getting RoundType detail from JSON (EditRound.OnCreate)");
            }
        } else roundTypeId = null;




        Button buttonSelectPreDefined = (Button) rootView.findViewById(R.id.editround_button_select_roundtype);
        buttonSelectPreDefined.setEnabled(false);
        buttonSelectPreDefined.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           /*     Fragment fragment = new RoundTypeSelect();
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment)
                            .addToBackStack("")
                            .commit();
                }*/
            }
        });  //buttonPreDefined OnClickListener}

        Button buttonCustom = (Button) rootView.findViewById(R.id.editround_button_custom_roundtype);
        buttonCustom.setEnabled(false);
        buttonCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    Fragment fragment = new CustomRound();
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment)
                            .addToBackStack("")
                            .commit();
                }*/
            }

        });  //buttonCustom OnClickListener}

        Button buttonCancel = (Button) rootView.findViewById(R.id.editround_button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
            }
        });  //buttonCancel OnClickListener}

        // - - - - - - - - - - - - - CREATE THE ROUND - - - - - - - - - - - - - - - - - - - -

        Button buttonSave = (Button) rootView.findViewById(R.id.editround_button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isPublic = switchIsPublic.isChecked();
                String comment = editTextComment.getText().toString();

                //JSONObject newRoundDetail = new JSONObject();

                try {
                    //Update Round
                    long updatedAt = System.currentTimeMillis();
                    roundJSON.put("updatedAt", updatedAt);
                    roundJSON.put("comment", comment);

                    boolean currentIsPublic = roundJSON.getBoolean("isPublic");
                    if ((!isPublic) && (currentIsPublic)) { // trying to change round from public to not public
                        JSONObject roundUsersJSON = roundJSON.getJSONObject("scores").getJSONObject("users");
                        if (roundUsersJSON.length() > 1) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("Edit Round");
                            alert.setMessage("Cannot change round from Public to Private as there are already other users for the round..");
                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                } //OnClick (dialog)
                            }); //OnClickListener
                            alert.show();
                        } else {
                            roundJSON.put("isPublic", isPublic);
                        }
                    } else {
                        roundJSON.put("isPublic", isPublic);
                    }

                /*    long createdAt = System.currentTimeMillis();
                    newRoundDetail.put("createdAt", createdAt);
                    newRoundDetail.put("updatedAt", createdAt);

                    UUID uuid = UUID.randomUUID();
                    final String id = uuid.toString();
                    newRoundDetail.put("id", id);

                    JSONObject newRoundCreator =new JSONObject();
                    newRoundCreator.put("name", myAppState.CDS.getName());
                    newRoundCreator.put("id",  myAppState.CDS.getUserID());
                    newRoundDetail.put("creator",newRoundCreator);



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
                    newRoundUser.put("updatedAt", createdAt);

                    JSONObject newRoundUsers = new JSONObject();
                    newRoundUsers.put (myAppState.CDS.userID, newRoundUser);

                    JSONObject newRoundScores = new JSONObject();
                    newRoundScores.put("updatedAt", createdAt); //use same time as CreatedAt above.
                    newRoundScores.put("users", newRoundUsers);
                    newRoundDetail.put ("scores", newRoundScores);

                    myAppState.CDS.createRound(id, createdAt, newRoundDetail);

                    myAppState.setCurrentEnd(0);

                    Bundle args = new Bundle();
                    args.putString("roundID", id);

                    Fragment fragment = new RoundScores();
                    if (fragment != null) {
                        FragmentManager fragmentManager = getFragmentManager();
                        fragment.setArguments(args);
                        fragmentManager.beginTransaction()
                                .replace(R.id.frame_container, fragment)
                                .addToBackStack("")
                                .commit();
                  }*/
                    myAppState.CDS.updateRound(roundID, updatedAt, roundJSON);

                    getFragmentManager().popBackStackImmediate();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("MCCArchers", "error editing round JSON (EditRound.buttonSave.onClick)");
                    Toast.makeText(getActivity(), "Could not save edited Round", Toast.LENGTH_LONG).show();
                }

            }
        });  //buttonSave OnClickListener}

        return rootView;
    } //onCreateView

    @Override
    public void onStart() {
        super.onStart();
       /* if (myAppState.hasRoundTypeID()) {
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
        } */

    } //onStart

}
