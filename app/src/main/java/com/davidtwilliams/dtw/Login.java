package com.davidtwilliams.dtw;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by paulwilliams on 28/01/15.
 */
public class Login extends Activity {

    SharedPreferences mSharedPreferences;

    String myEmail = "";
    String myPassword = "";

    private static final String PREFS = "prefs";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the activity which XML layout is right
        setContentView(R.layout.login);

        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        myEmail = mSharedPreferences.getString(PREF_EMAIL, "");
        myPassword = mSharedPreferences.getString(PREF_PASSWORD, "");

        EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextEmail.setText(myEmail);
        EditText editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPassword.setText(myPassword);

        Button buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Put it into memory (don't forget to commit!)
                EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
                String inputEmail = editTextEmail.getText().toString();
                EditText editTextPassword = (EditText) findViewById(R.id.editTextPassword);
                String inputPassword = editTextPassword.getText().toString();

                SharedPreferences.Editor e = mSharedPreferences.edit();
                e.putString(PREF_EMAIL, inputEmail);
                e.putString(PREF_PASSWORD, inputPassword);
                e.commit();

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);

                finish();
            }
        });  //button0 OnClickListener
    }
}


