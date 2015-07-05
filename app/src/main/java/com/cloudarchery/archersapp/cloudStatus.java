package com.cloudarchery.archersapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class cloudStatus extends Fragment {

    String userID = "";
    String roundID = "";

    MyApp myAppState;

    ImageView IVNetwork;
    ImageView IVConnected;
    ImageView IVAuthenticated;
    ImageView IVLinked;
    TextView TVStatus;
    Boolean network = false;
    Boolean linked = false;
    Boolean connected = false;
    Boolean authenticated = false;


    public cloudStatus() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        String email = args.getString("email");
        String club = args.getString("club");
        myAppState = ((MyApp) getActivity().getApplicationContext());
        //ToDO : BUG : UserID is not the same as Email - check for consistency

        final View rootView = inflater.inflate(R.layout.cloud_status, container, false);
        getActivity().getActionBar().setTitle("Checking Connection Status");

        TextView TVUsername = (TextView) rootView.findViewById(R.id.cloudStatus_textView_username);
        TVUsername.setText("email : " + email);
        TextView TVClub = (TextView) rootView.findViewById(R.id.cloudStatus_textView_club);
        TVClub.setText("Club : " + club);
        final TextView TVStatus = (TextView) rootView.findViewById(R.id.cloudstatus_TextView_Status);
        TVClub.setText("Club : " + club);

        IVNetwork = (ImageView) rootView.findViewById(R.id.cloudStatus_imageView_Network);
        IVConnected = (ImageView) rootView.findViewById(R.id.cloudStatus_imageView_Connected);
        IVAuthenticated = (ImageView) rootView.findViewById(R.id.cloudStatus_imageView_Authenticated);
        IVLinked = (ImageView) rootView.findViewById(R.id.cloudStatus_imageView_Linked);

        IVNetwork.setImageResource(R.drawable.ic_action_wait);
        IVConnected.setImageResource(R.drawable.ic_action_wait);
        IVAuthenticated.setImageResource(R.drawable.ic_action_wait);
        IVLinked.setImageResource(R.drawable.ic_action_wait);
        TVStatus.setText("checking status...");

        myAppState.CDS.myConnectionListener = new ClubFirebase.OnConnectionListener() {
            @Override
            public void onConnectionUpdated(Boolean Network, Boolean CDSConnected, Boolean Authenticated, Boolean Linked, String ErrorMessage) {
                TVStatus.setText(ErrorMessage);
                if (Network) {
                    IVNetwork.setImageResource(R.drawable.ic_action_ok);
                } else {
                    IVNetwork.setImageResource(R.drawable.ic_action_nok);
                }
                if (CDSConnected == null) {
                    IVConnected.setImageResource(R.drawable.ic_action_wait);
                } else if (CDSConnected) {
                    IVConnected.setImageResource(R.drawable.ic_action_ok);
                } else {
                    IVConnected.setImageResource(R.drawable.ic_action_nok);
                }
                if (Authenticated == null) {
                    IVAuthenticated.setImageResource(R.drawable.ic_action_wait);
                } else if (Authenticated) {
                    IVAuthenticated.setImageResource(R.drawable.ic_action_ok);
                } else {
                    IVAuthenticated.setImageResource(R.drawable.ic_action_nok);
                }
                if (Linked == null) {
                    IVLinked.setImageResource(R.drawable.ic_action_wait);
                } else if (Linked) {
                    IVLinked.setImageResource(R.drawable.ic_action_ok);
                } else {
                    IVLinked.setImageResource(R.drawable.ic_action_nok);
                }
                linked = Linked;
                if ((linked != null) && linked) TVStatus.setText("completed - all OK.");
                connected = CDSConnected;
                authenticated = Authenticated;
                network = Network;
            }
        };
        myAppState.CDS.initialise();

        //Set a handler to trigger a timeout
        final Handler timeoutHandler = new Handler();
        timeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (linked == null) linked = false;
                if (authenticated == null) authenticated = false;
                if (connected == null) connected = false;
                if (network == null) network = false;
                if (!linked) {
                    if (!authenticated) {
                        if (!connected) {
                            if (!network) {
                                TVStatus.setText("status check timed out - no internet connection.");
                                IVNetwork.setImageResource(R.drawable.ic_action_nok);
                            } else {
                                TVStatus.setText("completed - no connection to CloudArchery database.");
                                IVConnected.setImageResource(R.drawable.ic_action_nok);
                                IVAuthenticated.setImageResource(R.drawable.ic_action_nok);
                                IVLinked.setImageResource(R.drawable.ic_action_nok);
                            }
                        } else {
                            TVStatus.setText("completed - cannot authenticate with supplied email and password - contact your club administrator.");
                            IVAuthenticated.setImageResource(R.drawable.ic_action_nok);
                            IVLinked.setImageResource(R.drawable.ic_action_nok);
                        }
                    } else {
                        TVStatus.setText("completed - cannot find a data record for this user - contact your club administrator.");
                        IVLinked.setImageResource(R.drawable.ic_action_nok);
                    }
                } else {
                    TVStatus.setText("check completed - all OK.");
                }
            }
        }, 30000);

        //ToDo : What happens if not OK?

        Button buttonOk = (Button) rootView.findViewById(R.id.cloudStatus_button_Ok);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("newloginverified", "Ok");
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
//                getFragmentManager().popBackStack();
            }

            ;
        }); //buttonSave.setOnClickListener


        return rootView;
    } //onCreateView


}
