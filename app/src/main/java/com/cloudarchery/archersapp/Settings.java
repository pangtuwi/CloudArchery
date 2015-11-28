package com.cloudarchery.archersapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Settings extends Fragment {

    MyApp myAppState;
    int REQUEST_CODE_LOGIN = 10;
    final Integer FRAGMENT_CLOUDSTATUS_CODE = 1;
    Fragment thisFragment;
    boolean backupOK = false;

	public Settings(){}
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
        final View rootView = inflater.inflate(R.layout.settings, container, false);
        myAppState = ((MyApp)getActivity().getApplicationContext());
        thisFragment = this;

        final TextView TVLogin = (TextView) rootView.findViewById(R.id.settings_login);
        TVLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent loginIntent = new Intent(getActivity(), Login.class);
                //startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
                Bundle args = new Bundle();
                //Boolean linked = myAppState.CDS.linked;
                //args.putBoolean("linked", linked);
                args.putBoolean("newloginverified", false);

                Fragment fragment = new Login();
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragment.setArguments(args);
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment)
                            .addToBackStack("Login")
                            .commit();
                }
            }
        });
        TextView TVLogin2 = (TextView) rootView.findViewById(R.id.settings_login_2);
        TVLogin2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TVLogin.callOnClick();
            }
        });

        // - - - - - - - - - - - - CONNECTION STATUS - - - - - - - - - - - - -

        final TextView TVConnection = (TextView) rootView.findViewById(R.id.settings_connectionstatus);
        TVConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("email", myAppState.CDS.email);
                args.putString("club", myAppState.CDS.clubID);
                Fragment fragment = new CloudStatus();

                fragment.setTargetFragment(thisFragment, FRAGMENT_CLOUDSTATUS_CODE);
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragment.setArguments(args);
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment)
                            .addToBackStack("CloudStatus")
                            .commit();
                }
            }
        });
        TextView TVConnection2 = (TextView) rootView.findViewById(R.id.settings_connectionstatus_2);
        TVConnection2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TVConnection.callOnClick();
            }
        });

        // - - - - - - - - - - - - ABOUT - - - - - - - - - - - - -

        final TextView TVAbout = (TextView) rootView.findViewById(R.id.settings_about);
        TVAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                String versionName = "";
                try {
                    versionName = myAppState.getPackageManager()
                            .getPackageInfo(myAppState.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                alert.setTitle("About CloudArchery");
                alert.setMessage("Version "+versionName+"\n\nThis CloudArchery app for android was created by \n Paul Williams and Dylan Smith. \n Email pangtuwi@gmail.com with comments and suggestions.");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    } //OnClick (dialog)
                }); //OnClickListener
                alert.show();
            }
        });
        TextView TVAbout2 = (TextView) rootView.findViewById(R.id.settings_about_2);
        TVAbout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TVAbout.callOnClick();
            }
        });

        // - - - - - - - - - UPDATE ROUND TYPES - - - - - - - - - -

        final TextView TVUpdate = (TextView) rootView.findViewById(R.id.settings_update_roundTypes);
        TVUpdate.setEnabled(myAppState.CDS.connected);
        TVUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    //ValueEventListener roundTypeUpdateCheckListener = myAppState.CDS.roundTypeUpdateAvailableListener();

                    myAppState.CDS.myRoundTypesUpdatedListener = new ClubFirebase.OnRoundTypesUpdatedListener() {
                        @Override
                        public void onRoundTypesUpdated() {
                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle("Round Type Update");
                            alert.setMessage("RoundType update has been found and is being downloaded");
                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                } //OnClick (dialog)
                            }); //OnClickListener
                            alert.show();
                        }
                    };
            myAppState.CDS.startRoundTypeUpdate();
            }
        });
        TextView TVUpdate2 = (TextView) rootView.findViewById(R.id.settings_update_roundTypes_2);
        //TVUpdate2.setEnabled(myAppState.CDS.connected);
        TVUpdate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TVUpdate.callOnClick();
            }
        });

        // - - - - - - - - - BACKUP DATA ONTO DEVICE - - - - - - - - - -


        final TextView TVBackup = (TextView) rootView.findViewById(R.id.settings_backup_download);
        TVBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Backup onto Device");
                alert.setMessage("Downlad a copy of your data onto your device?");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SQLiteRounds db = new SQLiteRounds(getActivity());
                        backupOK = db.backupLocal();
                    } //OnClick (dialog)
                }); //OnClickListener
                // Make a "Cancel" button that simply dismisses the alert
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                alert.show();
                if (!backupOK) {
                    AlertDialog.Builder alert2 = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Backup onto Device");
                    alert.setMessage("Sorry - CloudArchery could not find a directory to back up to...");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        } //OnClick (dialog)
                    }); //OnClickListener
                    alert.show();
                }
            }
        });
        TextView TVBackup2 = (TextView) rootView.findViewById(R.id.settings_backup_download_2);
        TVBackup2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TVBackup.callOnClick();
            }
        });



        return rootView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FRAGMENT_CLOUDSTATUS_CODE && resultCode == Activity.RESULT_OK) {
            if(data != null) {
                String value = data.getStringExtra("newloginverified");
                if(value != null) {
                    Log.v("CloudArchery", "Data passed from Child fragment = " + value);
                    getFragmentManager().popBackStackImmediate();
                    getFragmentManager().popBackStackImmediate();
                }
            }
        }
    } //onActivityResult
}
