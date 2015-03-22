package com.davidtwilliams.dtw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paulwilliams on 28/01/15.
 */



public class CreateRound extends Activity  {

    String inputRoundType;
    String inputDate;
    String inputEnds;
    String inputArrowsPerEnd;
    int inputEndsInt = 0;
    int inputArrowsPerEndInt = 0;
    Firebase myFirebaseRef;

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void saveToDevice (String newRoundType, String newDate, int newEnds, int newArrowsPerEnd) {
        SQLiteLocal db = new SQLiteLocal(this);

        //TODO: Get the new Round Variables from the Activity interface
        int newRoundTypeID = 0;
        String newRoundDescriptor = "";
        int newCurrentEnd = 0;
        int newCurrentArrow = 0;
        int newArrowCount = 0;
        String newArrowData = "";
        String newRoundCloudID = "";
        int newroundTotal = 0;
        boolean newRoundComplete = false;
        long newUpdateAt = 0;

        Log.d("MCCArchers", "Inserting into DB ..");
        /*db.addLocalRound(new LocalRound(newRoundCloudID, newRoundTypeID, newRoundType, newRoundDescriptor, newDate,
                        newEnds, newArrowsPerEnd, newCurrentEnd, newCurrentArrow, newArrowCount,
                        newroundTotal, newRoundComplete, newUpdateAt, newArrowData));

        // Reading all entries
        Log.d("MCCArchers ", "Reading all Local Rounds..");
        List<LocalRound> localRounds = db.getAllLocalRounds();

        for (LocalRound lr : localRounds) {
            String log = "LocalRound = Id: "+lr.getId()+" ,roundtype: " + lr.getRoundType() + " ,Date: " + lr.getDate();
            Log.d("MCCArchers", log);
        } */

    } //saveToDb

    public void saveToOnlineDb (final String newRoundType, final String newDate, final int newEnds, final int newArrowsPerEnd) {
        Log.d("MCCArchers", "Inserting into OnlineDB ..");
        myFirebaseRef = new Firebase(getString(R.string.firebase_club_url));
        myFirebaseRef.child("rounds").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        List<Object> existingRounds = (List<Object>) snapshot.getValue();
                        Integer nextRoundIndex = existingRounds.size();
                        Integer nextRoundID =nextRoundIndex +1;
                        String newRoundId = "round"+nextRoundID;

                        //TODO : Load these additional variables from Database
                        int newRoundTypeID = 0;
                        String newRoundDescriptor = "";
                        int newCurrentEnd = 0;
                        int newCurrentArrow = 0;
                        int newArrowCount = 0;
                        String newArrowData = "";


                        Round newRound = new Round(newRoundId, newRoundTypeID, newRoundType, newRoundDescriptor,
                                                    newDate, newEnds, newArrowsPerEnd, newCurrentEnd, newCurrentArrow,
                                                    newArrowCount, newArrowData);

                        Map<Integer, Round> rounds = new HashMap<Integer, Round>();
                        rounds.put(nextRoundIndex, newRound);

                        myFirebaseRef.child("rounds/"+nextRoundIndex).setValue(newRound);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Log.e("MCCArchers", "error loading list of rounds (CreateRound) ");
                        Toast.makeText(getApplicationContext(), "Error loading list of rounds", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Log.d("MCCArchers", "No Data Record found (CreateRound)");
                    Toast.makeText(getApplicationContext(), "No round records found", Toast.LENGTH_LONG).show();
                }
            } //onDataChange

            @Override
            public void onCancelled(FirebaseError error) {
            }

        }); //addValueEventListener
    } //saveToDb

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d("MCCArchers", "CreateRound.onCreate super Done"); //Debugging

        // Tell the activity which XML layout is right
        setContentView(R.layout.create_round);

        Spinner spinner1 = (Spinner) findViewById(R.id.spinnerArrows);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.end_arrow_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);// Specify the layout to use when the list of choices appears
        spinner1.setAdapter(adapter);  // Apply the adapter to the spinner
        //spinner1.setOnItemSelectedListener(this);
        //TODO : Fix spinner1 onitemselected above

        Button buttonCreate = (Button) findViewById(R.id.buttonCreateRound);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextRoundType = (EditText) findViewById(R.id.editTextRoundType);
                inputRoundType = editTextRoundType.getText().toString();

                EditText editTextDate = (EditText) findViewById(R.id.editTextDate);
                inputDate = editTextDate.getText().toString();

                EditText editTextEnds = (EditText) findViewById(R.id.editTextEnds);
                inputEnds = editTextEnds.getText().toString();

                Spinner spinner1 = (Spinner) findViewById(R.id.spinnerArrows);
                inputArrowsPerEnd = String.valueOf(spinner1.getSelectedItem());

                try {
                    inputEndsInt = Integer.parseInt(inputEnds);
                } catch ( Throwable t){
                    Toast.makeText(getApplicationContext(), "number of ends entered incorrectly", Toast.LENGTH_LONG).show();
                }

                try {
                    inputArrowsPerEndInt = Integer.parseInt(inputArrowsPerEnd);
                } catch ( Throwable t){
                    Toast.makeText(getApplicationContext(), "number of arrows per end entered incorrectly", Toast.LENGTH_LONG).show();
                }

                RadioButton radioButtonDevice = (RadioButton) findViewById(R.id.radioButtonDevice);
                boolean inputRadioButtonDevice = radioButtonDevice.isChecked();
                RadioButton radioButtonOnline = (RadioButton) findViewById(R.id.radioButtonOnline);
                boolean inputRadioButtonOnline = radioButtonOnline.isChecked();

                //validate inputs
                if ((inputRoundType == null)
                     || (inputDate == null)
                        || (inputEndsInt > 20)
                            || (inputArrowsPerEndInt < 6)
                                || (inputArrowsPerEndInt > 6)
                                    ) {
                    Toast.makeText(getApplicationContext(), "Error in input, round not created", Toast.LENGTH_LONG).show();
                } else {
                    if (inputRadioButtonDevice) {
                        saveToDevice(inputRoundType, inputDate, inputEndsInt, inputArrowsPerEndInt);
                    }
                    if (inputRadioButtonOnline){
                        saveToOnlineDb(inputRoundType, inputDate, inputEndsInt, inputArrowsPerEndInt);
                    }

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("roundtype", inputRoundType);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });  //button0 OnClickListener
    }
}


