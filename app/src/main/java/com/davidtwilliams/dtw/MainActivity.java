package com.davidtwilliams.dtw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    TextView mainTextView;
    Button mainButton;
    EditText mainEditText;
    ListView mainListView;
    JSONAdapter mJSONAdapter;
    ArrayList mNameList = new ArrayList();
    ShareActionProvider mShareActionProvider;

    private static final String PREFS = "prefs";
    //private static final String PREF_NAME = "name";
    //private static final String PREF_USERID = "user1";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";

    SharedPreferences mSharedPreferences;

    Firebase myFirebaseRef;

    String roundID;
    int roundIDInt;
    String roundDate;
    String roundType;
    int roundEnds = 5;
    int roundArrowsPerEnd = 6;

    String myUserID = "";
    String myEmail = "";
    String myPassword = "";
    String myName = "";
    boolean loggedinOK = false;

    int REQUEST_CODE_LOGIN = 10;
    int REQUEST_CODE_CREATE_ROUND = 20;

    boolean alreadyWelcomed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.e("My App","I'm In");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase(getString(R.string.firebase_url));

        mainListView = (ListView) findViewById(R.id.main_listview);
        mainListView.setOnItemClickListener(this);

        setModeStatus("Connecting...");

        verifyLogin();
        loadRoundData();

        // 10. Create a JSONAdapter for the ListView
        mJSONAdapter = new JSONAdapter(this, getLayoutInflater());

        // Set the ListView to use the ArrayAdapter
        mainListView.setAdapter(mJSONAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu.   Adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem changeUser = menu.findItem(R.id.action_change_user);
        MenuItem addRound = menu.findItem(R.id.action_create_round);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_change_user:
                setModeStatus("OFFLINE MODE");

                Intent loginIntent = new Intent(this, Login.class);
                loginIntent.putExtra("email", myEmail);
                loginIntent.putExtra("password", myPassword);

                startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
                return true;

            case R.id.action_create_round:
                Intent createRoundIntent = new Intent(this, CreateRound.class);
                createRoundIntent.putExtra("email", myEmail);
                startActivityForResult(createRoundIntent, REQUEST_CODE_CREATE_ROUND);
                //Log.d("MCCArchers", "Finishing Create_round"); //Debugging
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setShareIntent() {

        if (mShareActionProvider != null) {

            // create an Intent with the contents of the TextView
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mainTextView.getText());

            // Make sure the provider knows
            // it should work with that Intent
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onClick(View view) {
        // Take what was typed into the EditText
        // and use in TextView
        //mainTextView.setText(mainEditText.getText().toString()
         //       + " is learning Android development!");
        //setShareIntent();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (!loggedinOK) {
            Toast.makeText(getApplicationContext(), "Cannot select round, not logged in", Toast.LENGTH_LONG).show();
        } else {
            roundIDInt = position;
            JSONObject jsonObject = (JSONObject) mJSONAdapter.getItem(position);
            roundDate = jsonObject.optString("date", "");
            roundType = jsonObject.optString("roundType", "");
            roundID = jsonObject.optString("id", "");
            int thisUserScore = -1;
            try {
                roundEnds = jsonObject.getInt("ends");
                roundArrowsPerEnd = jsonObject.getInt("arrowsperend");
                JSONObject roundUsers = jsonObject.getJSONObject("scores");
                thisUserScore = roundUsers.getInt(myUserID);
            } catch ( Throwable t) {
                thisUserScore = -1;
                t. printStackTrace();
            }
            if (thisUserScore == -1 ) {
                //Toast.makeText(getApplicationContext(), "No record found for you for this round", Toast.LENGTH_LONG).show();
                // show a dialog to ask if a round score set should be created
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Join Round?");
                alert.setMessage("You have not yet joined this round. (" + roundType + ", " + roundDate +").  Do you want to join and create a scoresheet?");

                // Create EditText for entry
                //final EditText input = new EditText(this);
                //alert.setView(input);

                // Make an "OK" button to save the name
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Array roundArray = new Array();
                        int[][] num = new int[roundEnds][roundArrowsPerEnd];
                        for (int i=0; i<roundEnds; i++){
                            for (int j=0; j < roundArrowsPerEnd; j++){
                                num[i][j]=-1;
                            }
                        }

                        ScoreData myScoreData  = new ScoreData(roundEnds, roundArrowsPerEnd, num);
                        //Firebase usersRef = ref.child("users");

                        //Map<String, ScoreData> scores = new HashMap<String, ScoreData>();
                        //scores.put("data", myScoreData);

                        try {
                            myFirebaseRef.child("scores/"+myUserID+"/" + roundID).setValue(myScoreData);
                            myFirebaseRef.child("scores/"+myUserID+"/" + roundID+"/score").setValue(0);
                            myFirebaseRef.child("rounds2/"+roundIDInt+"/scores/"+myUserID).setValue(0);
                            myFirebaseRef.child("users/"+myUserID+"/scores/"+roundID).setValue(0);
                        } catch(Throwable t) {
                            t.printStackTrace();
                        }


                    // Grab the EditText's input
                    //String inputName = input.getText().toString();

                    // Put it into memory (don't forget to commit!)
                    //SharedPreferences.Editor e = mSharedPreferences.edit();
                    //e.putString(PREF_NAME, inputName);
                    //e.commit();

                    // Welcome the new user
                    //Toast.makeText(getApplicationContext(), "Welcome, " + inputName + "!", Toast.LENGTH_LONG).show();
                }
            });

                // Make a "Cancel" button
                // that simply dismisses the alert
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {}
                });

                alert.show();
            } else {



                // create an Intent to take you over to a new DetailActivity
                Intent detailIntent = new Intent(this, RoundScores.class);

                // pack away the data about the cover into your Intent before you head out
                detailIntent.putExtra("roundDate", roundDate);
                detailIntent.putExtra("roundType", roundType);
                detailIntent.putExtra("roundID", roundID);
                detailIntent.putExtra("userID", myUserID);
                detailIntent.putExtra("roundIDInt", roundIDInt);

                // start the next Activity using your prepared Intent
                startActivity(detailIntent);
            }
        }
    }// OnItemClick

    public void loadRoundData() {
        myFirebaseRef.child("rounds").addValueEventListener(new ValueEventListener() {

            @Override public void onDataChange(DataSnapshot snapshot) {

                try {
                    String myJSONString = snapshot.getValue().toString();
                    Log.d("MCCArchers", myJSONString); //Debugging
                    JSONArray myArray = new JSONArray(myJSONString);
                    mJSONAdapter.updateData(myArray);
                } catch (Throwable t) {
                    Log.e("MCCArchers", "Could not parse malformed JSON ");
                    t. printStackTrace();
                }
            } //onDataChange

            @Override public void onCancelled(FirebaseError error) { }

        }); //addValueEventListener
    } //loadRoundData

    public void verifyLogin() {
        // Access the device's key-value storage
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        myEmail = mSharedPreferences.getString(PREF_EMAIL, "");
        myPassword = mSharedPreferences.getString(PREF_PASSWORD, "");
        Log.d("MCCArchers", "SharedPreferences Loaded: PREF_EMAIL: "+myEmail+"  PREF_PASSWORD:"+myPassword);
        //if values exist in sharedPreferences, then check if match in online database
        if ((myPassword.length() > 0) & (myEmail.length() >0 )) {

            myFirebaseRef.child("users/").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Log.d("MCCArchers", "Checking Database for login values");
                    loggedinOK = false;
                    Map<String, Object> dbUsers = (HashMap<String, Object>)snapshot.getValue();
                    try {
                        for(String key: dbUsers.keySet()){
                            //System.out.println(key  +" :: "+ dbUsers.get(key));
                            Map<String, String> dbThisUser = (HashMap<String, String>)dbUsers.get(key);
                            String thisEmail = dbThisUser.get("email");
                            String thisPassword = dbThisUser.get("password");
                            String thisUserID = dbThisUser.get("id");
                            String thisName = dbThisUser.get("name");
                            if (thisEmail != null) Log.e("MCCArchers", "checking "+thisEmail+ " against "+myEmail);
                            if (myEmail.equals(thisEmail) & (myPassword.equals(thisPassword))){
                                if (thisUserID !=null) myUserID = thisUserID;
                                if (thisName !=null) myName = thisName;
                                loggedinOK = true;
                                mainTextView.setText("Available Rounds : " + myName);
                                Toast.makeText(getApplicationContext(), "Login successful " + myName, Toast.LENGTH_LONG).show();
                            }
                        }  // iterate over Hashmap
                        if (!loggedinOK) {
                            Log.d("MCCArchers", "could not match username and password - running login Activity");
                            Toast.makeText(getApplicationContext(), "Login unsuccessful", Toast.LENGTH_LONG).show();
                            mainTextView = (TextView) findViewById(R.id.main_textview);
                            mainTextView.setText("OFFLINE MODE");


                            /* old - used to run login page directly if auto login unsuccessful - now go to offline mode
                            Intent loginIntent = new Intent(getApplicationContext(), Login.class);

                            startActivityForResult(loginIntent, REQUEST_CODE);
                            */
                        }
                    } catch (Throwable t) {
                        t. printStackTrace();
                        Log.e("MCCArchers", "Error checking database for Login Credentials ");
                    }
                } //onDataChange

                @Override
                public void onCancelled(FirebaseError error) {
                }
            }); //addValueEventListener

        } else {
            // do not have email and password in savedpreferences, go to Login page to establish new credentials
            Log.d("MCCArchers", "no credentials in savedpreferences - running login Activity");
            setModeStatus("OFFLINE : no email and password set");
            /*Intent loginIntent = new Intent(this, Login.class);
            startActivityForResult(loginIntent, REQUEST_CODE);*/

        }
    } //displaywelcome

    private void setModeStatus(String modeStatus) {
        mainTextView = (TextView) findViewById(R.id.main_textview);
        mainTextView.setText(modeStatus);
        Log.d("MCCArchers", "Setting Mode Status: "+modeStatus);
    }//SetModeStatus

    private String createJSONRoundScores () {
    /*    {
            data: [
            [-1,-1,-1,-1,-1,-1],
            [-1,-1,-1,-1,-1,-1],
            [-1,-1,-1,-1,-1,-1],
            [-1,-1,-1,-1,-1,-1],
            [-1,-1,-1,-1,-1,-1],
            [-1,-1,-1,-1,-1,-1],
            [-1,-1,-1,-1,-1,-1],
            [-1,-1,-1,-1,-1,-1],
            [-1,-1,-1,-1,-1,-1],
            [-1,-1,-1,-1,-1,-1],
            ],

            score: 0
        }
*/        String JSONString = "{\"data\":[";
        for (int i=0; i<roundEnds; i++){
            String s = "[";
            for (int j=0; j < roundArrowsPerEnd; j++){
                s += "-1";
                if (j!=(roundArrowsPerEnd-1)) s+= ",";
            }
            JSONString+=s;
            JSONString+="]";
            if (i!=(roundArrowsPerEnd-1)) {
                JSONString+= ",";
            } else {
                JSONString+= "], \"score\": 0}";
            }
        }
        Log.d("MCCArchers", "created JSON :"+ JSONString);
        return JSONString;
    }  //createJSONRoundScores

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        //Toast.makeText(this, "saved...", Toast.LENGTH_SHORT).show();
        Log.d("MCCArchers", "Running onActivityResult on return from Login :"+resultCode+"  "+requestCode);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_LOGIN) {
            //Try logging in again with new credentials
            Log.d("MCCArchers", "validating new login credentials :");
            verifyLogin();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CREATE_ROUND) {
            //Show local rounds
        }
    } //onActivityResult

}
