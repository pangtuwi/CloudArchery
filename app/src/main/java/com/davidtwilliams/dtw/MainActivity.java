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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    TextView mainTextView;
  //  CheckBox checkboxConnected;
    ImageView imageViewCloudConnected;
    ImageView imageViewLoggedIn;
    Switch switchOnline;
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

    boolean userIsAdmin = true;   // set this false on compile to disable the round creation option
    //todo make userIsAdmin variable read from the database on login
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

    boolean loggedin = false;
    boolean hasusername = false;
    boolean connected = false;
    boolean modecloud = false;
    //TODO Change loggedInOK to Connected+LoggedInOK

    int REQUEST_CODE_LOGIN = 10;
    int REQUEST_CODE_CREATE_ROUND = 20;

    boolean alreadyWelcomed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);
        myFirebaseRef = new Firebase(getString(R.string.firebase_url));

        //Activate the cloud/device switch and set a listener for it
        switchOnline = (Switch) findViewById(R.id.switch_online);
        switchOnline.setChecked(false);  //i.e. use device list
        switchOnline.setEnabled(false);
        switchOnline.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MCCArchers", "switchOnline Clicked.");
                if (switchOnline.isChecked()){
                    modecloud = true;
                    loadRoundData();
                } else {
                    modecloud = false;
                    loadRoundData();
                }
            }
        });

        mainListView = (ListView) findViewById(R.id.main_listview);
        mainListView.setOnItemClickListener(this);

        // Create a JSONAdapter for the ListView and Set the ListView to use the ArrayAdapter
        mJSONAdapter = new JSONAdapter(this, getLayoutInflater());
        mainListView.setAdapter(mJSONAdapter);

        //setModeStatus("Connecting...");

        checkConnection();
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
                setModeStatus("");

                Intent loginIntent = new Intent(this, Login.class);
                loginIntent.putExtra("email", myEmail);
                loginIntent.putExtra("password", myPassword);

                startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
                return true;

            case R.id.action_create_round:
                if (userIsAdmin) {
                    Intent createRoundIntent = new Intent(this, CreateRound.class);
                    createRoundIntent.putExtra("email", myEmail);
                    startActivityForResult(createRoundIntent, REQUEST_CODE_CREATE_ROUND);
                } else {
                    Toast.makeText(getApplicationContext(), "function only available to administrators", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

   /*   Removed - sharing was part of the original tutorial used as an example to start this app
    private void setShareIntent() {

        if (mShareActionProvider != null) {

            // create an Intent with the contents of the TextView
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mainTextView.getText());

            // Make sure the provider knows it should work with that Intent
            mShareActionProvider.setShareIntent(shareIntent);
        }
    } */

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

        if (modecloud & !loggedin) {
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
                // show a dialog to ask if a round score set should be created
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Join Round?");
                alert.setMessage("You have not yet joined this round. (" + roundType + ", " + roundDate +").  Do you want to join and create a scoresheet?");
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
                        if (modecloud) {  //create data in cloud database
                            try {
                                myFirebaseRef.child("scores/" + myUserID + "/" + roundID).setValue(myScoreData);
                                myFirebaseRef.child("scores/" + myUserID + "/" + roundID + "/score").setValue(0);
                                myFirebaseRef.child("rounds/" + roundIDInt + "/scores/" + myUserID).setValue(0);
                                myFirebaseRef.child("users/" + myUserID + "/scores/" + roundID).setValue(0);
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        } else { //  !modecloud therefore create data in local database

                        }
                        startRoundScores();
                    } //OnClick (dialog)
                }); //OnClickListener

                // Make a "Cancel" button that simply dismisses the alert
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {}
                });
                alert.show();
            } else {
                startRoundScores();
            }
        }
    }// OnItemClick

    public void startRoundScores () {
        Intent detailIntent = new Intent(this, RoundScores.class);
        detailIntent.putExtra("roundDate", roundDate);
        detailIntent.putExtra("roundType", roundType);
        detailIntent.putExtra("roundID", roundID);
        detailIntent.putExtra("userID", myUserID);
        detailIntent.putExtra("roundIDInt", roundIDInt);
        startActivity(detailIntent);
    }// startRoundScores

    public void loadRoundData() {

        if (connected & loggedin & modecloud) {

            myFirebaseRef.child("rounds").addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //todo check why loads even if cant log in
                    try {
                        Log.d("MCCArchers ", "Reading all cloud Rounds..");
                        String myJSONString = snapshot.getValue().toString();
                        //Log.d("MCCArchers ", myJSONString);
                        JSONArray myArray = new JSONArray(myJSONString);
                        //Collections.reverse(Arrays.asList(myArray));  //Tried unsuccessfully to use this to reverse the order of array - did not work
                        mJSONAdapter.updateData(myArray);
                    } catch (Throwable t) {
                        Log.e("MCCArchers", "Could not parse malformed JSON ");
                        t.printStackTrace();
                    }
                } //onDataChange

                @Override
                public void onCancelled(FirebaseError error) {
                }

            }); //addValueEventListener
        } else {
            Log.d ("MCCArchers", "load local rounds.......");
            String stringJSONLocalRounds = "";
            try {
                SQLiteLocal db = new SQLiteLocal(this);
                Log.d("MCCArchers ", "Reading all Local Rounds..");
                List<LocalRound> localRounds = db.getAllLocalRounds();
                stringJSONLocalRounds = localRounds.toString();
                Log.d("MCCArchers ", stringJSONLocalRounds);
                JSONArray myLocalArray = new JSONArray(stringJSONLocalRounds);
                Log.d("MCCArchers ", "JSON Parse OK");
                mJSONAdapter.updateData(myLocalArray);
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("MCCArchers", "Could not parse JSON for local round data ");
            }
        }
    } //loadRoundData

    public void checkConnection() {
        setModeStatus("checking cloud database connection");
        try {
            //Firebase connectedRef = new Firebase(R.string.firebase_url + ".info/connected");
            myFirebaseRef.child(".info/connected").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    imageViewCloudConnected = (ImageView) findViewById(R.id.imageViewCloudConnected);
                    boolean isConnectedCheck = snapshot.getValue(Boolean.class);
                    if (isConnectedCheck) {
                        connected = true;
                        setModeStatus("cloud database connected");
                        imageViewCloudConnected.setImageResource(R.drawable.ic_cloud_connected);
                        switchOnline.setEnabled(true);
                        verifyLogin();

                    } else {
                        connected = false;
                        setModeStatus("cloud database NOT connected");
                        imageViewCloudConnected.setImageResource(R.drawable.ic_cloud_disconnected);
                        switchOnline.setChecked(false);
                        loadRoundData();
                        switchOnline.setEnabled(false);
                    }
                }

                @Override
                public void onCancelled(FirebaseError error) {
                    //System.err.println("Listener was cancelled");
                    setModeStatus("Firebase Connection Error");
                }

            });
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("MCCArchers", "Exception while checking connection ");
        }


    }//checkConnection

    public void getUserName(){
        myFirebaseRef.child("users/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Log.d("MCCArchers", "Checking Database for login values");
                //loggedin = false;
                Map<String, Object> dbUsers = (HashMap<String, Object>) snapshot.getValue();
                try {
                    for (String key : dbUsers.keySet()) {
                        //System.out.println(key  +" :: "+ dbUsers.get(key));
                        Map<String, String> dbThisUser = (HashMap<String, String>) dbUsers.get(key);
                        String thisEmail = dbThisUser.get("email");
                        //String thisPassword = dbThisUser.get("password");
                        String thisUserID = dbThisUser.get("id");
                        String thisName = dbThisUser.get("name");

                        if ((thisEmail != null) & (thisName != null) & (thisUserID !=null)) {
                            Log.e("MCCArchers", "checking " + thisEmail + " against " + myEmail);
                            if (myEmail.equals(thisEmail)) {// & (myPassword.equals(thisPassword))) {
                                if (thisUserID != null) myUserID = thisUserID;
                                if (thisName != null) myName = thisName;
                                Toast.makeText(getApplicationContext(), "Welcome " + myName, Toast.LENGTH_LONG).show();
                                setModeStatus("user :"+ myName);
                                Log.d("MCCArchers", "User with ID "+myUserID+" selected");

                                loadRoundData();
                            }
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    Log.e("MCCArchers", "Error checking database for userName to match login email ");
                }
            } //onDataChange

            @Override
            public void onCancelled(FirebaseError error) {
            }
        }); //addValueEventListener

    };   //getUserName

    public void verifyLogin() {
        //String firebase_email = getString(R.string.firebase_login_email);
        //String firebase_password = getString(R.string.firebase_login_password);
        //checkboxConnected = (CheckBox) findViewById(R.id.checkBoxConnected);

        // Access the device's key-value storage
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        myEmail = mSharedPreferences.getString(PREF_EMAIL, "");
        myPassword = mSharedPreferences.getString(PREF_PASSWORD, "");

        imageViewLoggedIn = (ImageView) findViewById(R.id.imageViewLoggedIn);
        myFirebaseRef.authWithPassword(myEmail, myPassword, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                loggedin = true;
                setModeStatus("user logged in");
                imageViewLoggedIn.setImageResource(R.drawable.ic_login_ok);
                switchOnline.setEnabled(true);
                getUserName();
                //checkboxConnected.setChecked(true);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                loggedin = false;
                setModeStatus("user credentials incorrect");
                imageViewLoggedIn.setImageResource(R.drawable.ic_change_user);
                switchOnline.setEnabled(false);
                //checkboxConnected.setChecked(false);
            }
        });

    } //verifyLogin

    private void setModeStatus(String modeStatus) {
        mainTextView = (TextView) findViewById(R.id.main_textview);
        mainTextView.setText(modeStatus);
        Log.d("MCCArchers", "Setting Mode Status: "+modeStatus);
    }//SetModeStatus

    private String createJSONRoundScores () {
        String JSONString = "{\"data\":[";
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
            //Log.d("MCCArchers", "validating new login credentials :");
            setModeStatus("verifying login");
            verifyLogin();
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CREATE_ROUND) {
            //Show local rounds
        }
    } //onActivityResult

}
