package com.cloudarchery.archersapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by paulwilliams on 28/01/15.
 */
//public class Login extends Activity {
public class Login extends Fragment {

    public Login(){};

    SharedPreferences mSharedPreferences;
    MyApp myAppState;

    boolean originalCDSSync = false;
    String originalEmail = "";
    String originalPassword = "";
    String originalClubID = "";

    boolean inputCDSSync;
    String inputEmail;
    String inputPassword;
    String inputClubID;
    final Integer FRAGMENT_CLOUDSTATUS_CODE = 1;

    EditText editTextClub;
    EditText editTextEmail;
    EditText editTextPassword;
    CheckBox checkBoxCDS;
    Button buttonSignIn;

    Fragment thisFragment;

    //ToDo : Remove Automatic Login on first runx



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.login, container, false);
        thisFragment = this;

            myAppState = ((MyApp) getActivity().getApplicationContext());
            getActivity().getActionBar().setTitle("CloudArchery Database Login");

            mSharedPreferences = myAppState.getSharedPreferences(getString(R.string.PREFS), myAppState.MODE_PRIVATE);
            originalCDSSync = mSharedPreferences.getBoolean(getString(R.string.PREF_SYNC), false);
            originalClubID = mSharedPreferences.getString(getString(R.string.PREF_CLUBID), "");
            originalEmail = mSharedPreferences.getString(getString(R.string.PREF_EMAIL), "");
            originalPassword = mSharedPreferences.getString(getString(R.string.PREF_PASSWORD), "");

            editTextClub = (EditText) rootView.findViewById(R.id.login_editText_Club);
            editTextClub.setText(originalClubID);
            editTextEmail = (EditText) rootView.findViewById(R.id.login_editText_Email);
            editTextEmail.setText(originalEmail);
            editTextPassword = (EditText) rootView.findViewById(R.id.login_editText_Password);
            editTextPassword.setText(originalPassword);
            checkBoxCDS = (CheckBox) rootView.findViewById(R.id.login_checkBox_CDSSync);
            checkBoxCDS.setChecked(originalCDSSync);
            buttonSignIn = (Button) rootView.findViewById(R.id.login_button_SignIn);
            TextView TVStatus = (TextView) rootView.findViewById(R.id.login_textView_status1);
            if (originalCDSSync) {
                buttonSignIn.setText("Sign In");
            } else {
                buttonSignIn.setText("Use without cloud sync");
            }


            if (myAppState.CDS.linked) {
                buttonSignIn.setEnabled(false);
                TVStatus.setText("Signed in to " + myAppState.CDS.clubID);
                buttonSignIn.setEnabled(false);
            } else {
                TVStatus.setText("Not signed in");
                buttonSignIn.setEnabled(true);
            }

            //CheckBox checkBoxCDS = (CheckBox) findViewById(R.id.checkBoxCDSSync);
            if (checkBoxCDS.isChecked()) {
                editTextClub.setEnabled(true);
                editTextEmail.setEnabled(true);
                editTextPassword.setEnabled(true);
                //buttonLogin.setEnabled(false);
            }

            checkBoxCDS.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editTextClub.setEnabled(checkBoxCDS.isChecked());
                    editTextEmail.setEnabled(checkBoxCDS.isChecked());
                    editTextPassword.setEnabled(checkBoxCDS.isChecked());
                    if (checkBoxCDS.isChecked()) {
                        buttonSignIn.setText("Sign In");
                    } else {
                        buttonSignIn.setText("Use without cloud sync");
                    }
                }
            });

            editTextClub.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                    buttonSignIn.setEnabled(true);
                }
            });

            editTextEmail.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void afterTextChanged(Editable s) {buttonSignIn.setEnabled(true);}
            });

            editTextPassword.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                    buttonSignIn.setEnabled(true);
                }
            });


            buttonSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    inputCDSSync = checkBoxCDS.isChecked();
                    inputClubID = editTextClub.getText().toString();
                    inputClubID = inputClubID.replaceAll("\\s+", "");
                    inputEmail = editTextEmail.getText().toString();
                    inputEmail = inputEmail.replaceAll("\\s+", "");
                    inputPassword = editTextPassword.getText().toString();
                    inputPassword = inputPassword.replaceAll("\\s+", "");

                    if (!inputCDSSync) {
                        saveCredentialsNoSync();
                        getFragmentManager().popBackStackImmediate();
                    } else if ((!inputClubID.equals(myAppState.CDS.clubID)) || (!inputEmail.equals(myAppState.CDS.email))){
                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("New Login Credentials");
                        alert.setMessage("You have entered new login credentials.  If you sign in with these credentials, all of the rounds stored on your device will be merged with those stored in the cloud database.");
                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                saveCredentialsSync();
                                startCloudStatusCheck();
                            } //OnClick (dialog)
                        }); //OnClickListener
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                getFragmentManager().popBackStackImmediate();
                            }
                        });
                        alert.show();
                    } else {
                        saveCredentialsSync();
                        startCloudStatusCheck();
                    }

                }
            });  //buttonLogin OnClickListener

     //   };
    return rootView;

    } //OnCreate

    private void saveCredentialsNoSync (){
        SharedPreferences.Editor e = mSharedPreferences.edit();
        e.putBoolean(getString(R.string.PREF_SYNC), false);
        e.putString(getString(R.string.PREF_CLUBID), inputClubID);
        e.putString(getString(R.string.PREF_EMAIL), inputEmail);
        e.putString(getString(R.string.PREF_PASSWORD), inputPassword);
        e.putString(getString(R.string.PREF_USERID), myAppState.CDS.userID);
        e.putString(getString(R.string.PREF_NAME), myAppState.CDS.name);
        e.putString(getString(R.string.PREF_CLUBURL), "");
        e.commit();
    }//SaveCredentialsNoSync

    private void saveCredentialsSync (){
        SharedPreferences.Editor e = mSharedPreferences.edit();
        e.putBoolean(getString(R.string.PREF_SYNC), true);
        e.putString(getString(R.string.PREF_CLUBID), inputClubID);
        e.putString(getString(R.string.PREF_EMAIL), inputEmail);
        e.putString(getString(R.string.PREF_PASSWORD), inputPassword);
        e.putString(getString(R.string.PREF_USERID), myAppState.CDS.userID);
        e.putString(getString(R.string.PREF_NAME), myAppState.CDS.name);
        e.putString(getString(R.string.PREF_CLUBURL), "");
        e.commit();
    }//SaveCredentialsSync


    private void startCloudStatusCheck (){
        Bundle args = new Bundle();
        args.putString("email", inputEmail);
        args.putString("club", inputClubID);
        Fragment fragment = new CloudStatus();
        fragment.setTargetFragment(thisFragment, FRAGMENT_CLOUDSTATUS_CODE);
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragment.setArguments(args);
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .addToBackStack("")
                    .commit();
        }
    }//startCloudStatusCheck

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FRAGMENT_CLOUDSTATUS_CODE && resultCode == Activity.RESULT_OK) {
            if(data != null) {
                String value = data.getStringExtra("newloginverified");
                if(value != null) {
                    getFragmentManager().popBackStackImmediate();
                    getFragmentManager().popBackStackImmediate();
                }
            }
        }
    } //onActivityResult
} //Class Login




