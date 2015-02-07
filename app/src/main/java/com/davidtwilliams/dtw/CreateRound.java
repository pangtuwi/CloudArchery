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
import android.widget.Spinner;

import java.util.List;

/**
 * Created by paulwilliams on 28/01/15.
 */



public class CreateRound extends Activity  {

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void saveToDb (String newRoundType, String newDate, int newEnds, int newArrowsPerEnd) {
        SQLiteLocal db = new SQLiteLocal(this);

        Log.d("MCCArchers", "Inserting into DB ..");
        db.addLocalRound(new LocalRound(newRoundType, newDate, newEnds, newArrowsPerEnd));

        // Reading all entries
        Log.d("MCCArchers ", "Reading all Local Rounds..");
        List<LocalRound> localRounds = db.getAllLocalRounds();

        for (LocalRound lr : localRounds) {
            String log = "LocalRound = Id: "+lr.getId()+" ,roundtype: " + lr.getRoundtype() + " ,Date: " + lr.getDate();
            // Writing Contacts to log
            Log.d("MCCArchers", log);
        }

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

       // mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        Button buttonCreate = (Button) findViewById(R.id.buttonCreateOnline);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextRoundType = (EditText) findViewById(R.id.editTextRoundType);
                String inputRoundType = editTextRoundType.getText().toString();

                EditText editTextDate = (EditText) findViewById(R.id.editTextDate);
                String inputDate = editTextDate.getText().toString();

                EditText editTextEnds = (EditText) findViewById(R.id.editTextEnds);
                String inputEnds = editTextEnds.getText().toString();
                //int inputEndsInt = (int) inputEnds;

                Spinner spinner1 = (Spinner) findViewById(R.id.spinnerArrows);
                //String inputArrows =

                saveToDb(inputRoundType, inputDate, 5, 6);
                // TODO - convert Strings to ints to send

                Intent returnIntent = new Intent();
                returnIntent.putExtra("roundtype", inputRoundType);


                setResult(RESULT_OK, returnIntent);
                Log.d("MCCArchers", "In CreateRound.onClickListener"); //Debugging
                finish();
            }
        });  //button0 OnClickListener
    }
}


