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

public class Statistics extends Fragment {

    MyApp myAppState;
    Fragment thisFragment;


    public Statistics(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.statistics, container, false);
        myAppState = ((MyApp)getActivity().getApplicationContext());
        thisFragment = this;

        // - - - - - - - - - - - - ALL TIME STATS  - - - - - - - - - - - - -

        final TextView TVAllTimeStats = (TextView) rootView.findViewById(R.id.statistics_alltime);
        TVAllTimeStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment fragment = new MyStats();
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    //fragment.setArguments(args);
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment)
                            .addToBackStack("AllTimeStats")
                            .commit();
                }
            }
        });
        TextView TVAllTimeStats2 = (TextView) rootView.findViewById(R.id.statistics_alltime_2);
        TVAllTimeStats2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TVAllTimeStats.callOnClick();
            }
        });

        // - - - - - - - - - - - - GRAPH - - - - - - - - - - - - -

        final TextView TVGraph = (TextView) rootView.findViewById(R.id.statistics_graph);
        TVGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new GraphAverages();

               // fragment.setTargetFragment(thisFragment, FRAGMENT_CLOUDSTATUS_CODE);
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    //fragment.setArguments(args);
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment)
                            .addToBackStack("GraphAverages")
                            .commit();
                }
            }
        });
        TextView TVGraph2 = (TextView) rootView.findViewById(R.id.statistics_graph_2);
        TVGraph2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TVGraph.callOnClick();
            }
        });

        return rootView;
    }

}
