package com.cloudarchery.archersapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by paulwilliams on 10/01/15.
 */
public class RoundScores extends Fragment {

    MyApp myAppState;
    //Display Variables

    Resources res;

    SQLiteRounds db;
    //Scores Table Views
    TextView arrowTextView;
    TextView headerText1;
    TextView headerText2;
    TextView headerText3;

    ArrayList<TableRow> alRows;

    //USer Table Views
    ArrayList<TableRow> alUserRows;
    ListView userListView;
    JSONAdapterUserScores mJSONAdapter;

    int numArrowsPerEnd = 0;
    int numEnds = 0;
    int currentEnd = 0;
    int endTotal = 0;
    int grandTotal = 0;
    int numUsers = 0;
    int arrowCount = 0;
    float arrowAvg;
    JSONArray endsArr;

    String roundID;
    String roundTypeName;
    String roundTypeDescription;
    String roundOwner;
    String roundDate;
    String creatorID;
    JSONObject userJSON;
    JSONObject roundJSON;

    //ToDo: BUG : My arrow Table not updated after scoring a round (looks like it happened when queue was full)
    //ToDo : Does not retrigger a list update if new rounds loaded after visible

    public RoundScores(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // ToDo : RISK : Add OnPause to fragments in case user does not come back
        myAppState = ((MyApp)getActivity().getApplicationContext());
        currentEnd = myAppState.getCurrentEnd();

        Bundle args = getArguments();
        roundID = args.get("roundID").toString();
        db = new SQLiteRounds(getActivity());
        roundJSON = db.getRoundJSON(roundID);

        try {
            if (roundJSON.has("roundType")) {
                roundTypeName = roundJSON.getJSONObject("roundType").getString("name");
                //roundTypeDescription = roundJSON.getJSONObject("roundType").getString("description");
                roundOwner = roundJSON.getJSONObject("creator").getString("name");

                if (roundJSON.has("createdAt")) {
                    //authorName = jsonObject.optJSONArray("author_name").optString(0);
                    Long createdAtLong = roundJSON.optLong("createdAt");
                    Date createdAtDate = new Date(createdAtLong);
                    //String DATE_FORMAT_NOW = "yyyy-MM-dd";
                    String DATE_FORMAT_NOW = "EEEE dd MMMM yyyy";
                    //Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                    roundDate = sdf.format(createdAtDate);
                    //roundDate = createdAtDate.toString();
                }

            }

            if (roundJSON.has("numArrowsPerEnd")) numArrowsPerEnd = roundJSON.getInt("numArrowsPerEnd");
            if (roundJSON.has("numEnds"))numEnds = roundJSON.getInt("numEnds");
            if (roundJSON.has("scores")) {
                JSONObject roundUserJSON = roundJSON.getJSONObject("scores").getJSONObject("users");
                numUsers = roundUserJSON.length();
            }
            creatorID = roundJSON.getJSONObject("creator").getString("id");
            userJSON = roundJSON.getJSONObject("scores").getJSONObject("users").getJSONObject(myAppState.CDS.getUserID());
        } catch (Throwable t) {
            t.printStackTrace();
            Log.d("MCCArchers", "error reading values from LDS JSON (RoundScores.onCreateView)");
        }

        View rootView = inflater.inflate(R.layout.round_scores, container, false);
        getActivity().getActionBar().setTitle("Cloud Archery : Scores");

        userListView = (ListView) rootView.findViewById(R.id.round_scores_user_list_view);
        mJSONAdapter = new JSONAdapterUserScores(getActivity(), getActivity().getLayoutInflater());
        userListView.setAdapter(mJSONAdapter);

        try {
            SQLiteRounds db = new SQLiteRounds(getActivity());
            JSONArray myRoundUsersArray = db.getRoundUsersJSONArray(roundID);
            mJSONAdapter.updateData(myRoundUsersArray);
        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("MCCArchers", "Could not get user status data (RoundScores.onCreateView");
        }

        myAppState.CDS.myScoreListener = new ClubFirebase.OnUserScoreUpdatedListener() {
            @Override
            public void onScoreUpdated(JSONArray newScoresJSON) {
                mJSONAdapter.updateData(newScoresJSON);
            }
        };

        

        //Initiate required variables
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 36, getResources().getDisplayMetrics());

        headerText1 = (TextView) rootView.findViewById(R.id.round_scores_round_type_name);
        headerText1.setText(roundTypeName);
        headerText2 = (TextView) rootView.findViewById(R.id.round_scores_round_type_description);
        headerText2.setText(roundTypeDescription);
        headerText3 = (TextView) rootView.findViewById(R.id.round_scores_round_owner);
        //headerText3.setText("created by " +roundOwner + " on " + roundDate);
        headerText3.setText("" + roundDate);

        //create scores table programatically

        res = getResources();
        int orange_1 = res.getColor(R.color.orange_1);

        //Block format for Arrow scores
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.WHITE);
        gd.setCornerRadius(7);
        gd.setStroke(2, 0xFF000000);

        //Block format for End Totals
        GradientDrawable gdTotal = new GradientDrawable();
        gdTotal.setColor(Color.LTGRAY);
        gdTotal.setCornerRadius(7);
        gdTotal.setStroke(4, 0xFF000000);

        //Block format for Running Totals
        GradientDrawable gdRTotal = new GradientDrawable();
        gdRTotal.setColor(Color.DKGRAY);
        gdRTotal.setCornerRadius(7);
        gdRTotal.setStroke(4, 0xFF000000);

        alRows = new ArrayList<TableRow>();
        ArrayList<TextView> alEndTotals = new ArrayList<TextView>();
        ArrayList<TextView> alRunningTotals = new ArrayList<TextView>();

        //iterate through the number of ends, creating a row for each.
        try {
            //clean up existing views in the table if they exist
            int alRowsSize = alRows.size();
            for (int j = 0; j < alRowsSize; j++) {
                if (alRows.get(j).getParent() != null) {
                    ((ViewGroup)alRows.get(j).getParent()).removeView(alRows.get(j));}
                if (alEndTotals.get(j).getParent() != null){
                    ((ViewGroup) alEndTotals.get(j).getParent()).removeView(alEndTotals.get(j));}
                if (alRunningTotals.get(j).getParent() != null){
                    ((ViewGroup) alRunningTotals.get(j).getParent()).removeView(alRunningTotals.get(j));}
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        TableLayout tl = (TableLayout) rootView.findViewById(R.id.dynamic_scores_table);
        tl.removeAllViews();

        for (int j = 0; j < numEnds; j++) {
            // Create the table row
            alRows.add(new TableRow(getActivity()));
            //replaced rootView.getContext  with getActivity
            alRows.get(j).setId(1000+j);
            alRows.get(j).setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
            alRows.get(j).setPadding(5, 5, 5, 5);
            alEndTotals.add(new TextView(getActivity()));
            alRunningTotals.add(new TextView(getActivity()));

            ArrayList<TextView> alArrows = new ArrayList<TextView>();
            try {
                int alArrowsSize = alArrows.size();
                for (int i = 0; i < alArrowsSize; i++) {
                    if (alArrows.get(i).getParent() != null) {
                        ((ViewGroup) alArrows.get(j).getParent()).removeView(alArrows.get(j));
                    }
                }
            }catch (Throwable e) {
                e.printStackTrace();
            }

            for (int i = 0; i < numArrowsPerEnd; i++) {

                alArrows.add(new TextView(getActivity()));

                alArrows.get(i).setId(2000+(j*numArrowsPerEnd)+i);
                alArrows.get(i).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                alArrows.get(i).setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                alArrows.get(i).setBackground(gd);
                //alArrows.get(i).setText(""+j+","+i); Debugging
                alArrows.get(i).setText("");
                alArrows.get(i).setPadding(2, 0, 5, 0);
                alArrows.get(i).setTextColor(Color.BLACK);
                alArrows.get(i).setWidth(px);

                alRows.get(j).addView(alArrows.get(i));

                TextView arrowInnerGap = new TextView(getActivity());
                //arrowInnerGap.setId(300+j);
                arrowInnerGap.setText(" ");
                arrowInnerGap.setWidth(5);
                alRows.get(j).addView(arrowInnerGap);
            }

            TextView arrowGap = new TextView(getActivity());
            arrowGap.setId(3000 + j);
            arrowGap.setText(" ");
            arrowGap.setWidth(20);
            alRows.get(j).addView(arrowGap);

            alEndTotals.get(j).setId(4000 + j);
            alEndTotals.get(j).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            alEndTotals.get(j).setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            alEndTotals.get(j).setBackground(gdTotal);
            alEndTotals.get(j).setText("0");
            alEndTotals.get(j).setPadding(2, 0, 5, 0);
            alEndTotals.get(j).setTextColor(Color.BLACK);
            alEndTotals.get(j).setWidth(px);

            alRows.get(j).addView(alEndTotals.get(j));

            TextView totalGap = new TextView(getActivity());
            totalGap.setId(5000 + j);
            totalGap.setText(" ");
            totalGap.setWidth(10);
            alRows.get(j).addView(totalGap);

            alRunningTotals.get(j).setId(6000 + j);
            alRunningTotals.get(j).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            alRunningTotals.get(j).setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            alRunningTotals.get(j).setBackground(gdRTotal);
            alRunningTotals.get(j).setText("0");
            alRunningTotals.get(j).setPadding(2, 0, 5, 0);
            alRunningTotals.get(j).setTextColor(Color.WHITE);
            alRunningTotals.get(j).setWidth(px);

            alRows.get(j).removeView(alRunningTotals.get(j));
            alRows.get(j).addView(alRunningTotals.get(j));

            alRows.get(j).setOnClickListener(myClickListener);
            alRows.get(j).getParent();
            tl.addView(alRows.get(j), new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

        }
        alRows.get(currentEnd).setBackgroundColor(res.getColor(R.color.orange_1));

        //Write scores to table
        try {
            endTotal = 0;
            grandTotal = 0;
            int tempArrow = 0;
            arrowCount = 0;
            try {
                endsArr = userJSON.getJSONArray("data");
            } catch (Throwable t) {
                Log.e("MCCArchers", "error getting data from scoresJSON (RoundScores.displayData");
                t.printStackTrace();
            }

            for (int i=0; i < endsArr.length(); i++) {

                endTotal = 0;
                JSONArray arrowsArr = endsArr.getJSONArray(i);

                for (int j=0; j < arrowsArr.length(); j++) {
                    tempArrow = arrowsArr.getInt(j);
                    if (tempArrow == -1) {
                        arrowTextView = (TextView) rootView.findViewById(2000+(i*numArrowsPerEnd)+j);
                        arrowTextView.setText("-");
                    } else {
                        arrowCount++;
                        endTotal = endTotal + tempArrow;
                        grandTotal = grandTotal + tempArrow;
                        arrowTextView = (TextView) rootView.findViewById(2000 + (i * numArrowsPerEnd) + j);
                        if (arrowTextView != null) {
                            arrowTextView.setText("" + tempArrow);
                        } else {
                            Log.d("MCCArchers", "could not write arrow: "+i+", "+j+ "   "+2000+(i*numArrowsPerEnd)+j);
                        }
                    }
                }
                TextView endTextView = (TextView) rootView.findViewById(4000 + i);
                endTextView.setText(""+endTotal);
                TextView totalTextView = (TextView) rootView.findViewById(6000 + i);
                totalTextView.setText(""+grandTotal);
                alRows.get(currentEnd).setBackgroundColor(res.getColor(R.color.orange_1));
            }


        } catch (Throwable t) {
            t.printStackTrace();
            Log.e("MCCArchers", "RoundScores load incomplete: Error in data or could not parse malformed JSON ");
        }


        myAppState.CDS.startLeaderboardListener(roundID);
        setHasOptionsMenu(true);

        return rootView;

    }  // OnCreateView

    @Override
    public void onDestroyView (){
        super.onDestroyView();
        //Log.d ("CloudArchery", "onDestroyView has been called");
        myAppState.CDS.stopLeaderboardListener();
    } //onDestroyView

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void EditSelectedRound (){
        if (creatorID.equals(myAppState.CDS.getUserID())){
            Bundle args = new Bundle();
            args.putString("roundID", roundID);
            Fragment fragment = new EditRound();
            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                fragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .addToBackStack("")
                        .commit();
            }

        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Edit Round");
            alert.setMessage("You may not edit this round as you are not the creator of the round.");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                } //OnClick (dialog)
            }); //OnClickListener
            alert.show();
        }
    }//EditSelectedRound

    public void DeleteSelectedRound (){
        if (creatorID.equals(myAppState.CDS.getUserID())){
            try {
                JSONObject roundUsersJSON = roundJSON.getJSONObject("scores").getJSONObject("users");
                if (roundUsersJSON.length() > 1) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Delete Round");
                    alert.setMessage("Cannot delete this round as there are other users participating.");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        } //OnClick (dialog)
                    }); //OnClickListener
                    alert.show();
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Delete Round");
                    alert.setMessage("Are you sure you want to delete this round?");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            myAppState.CDS.deleteRound(roundID);
                            getFragmentManager().popBackStackImmediate();
                        } //OnClick (dialog)
                    }); //OnClickListener
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
                    alert.show();
                }
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("CloudArchery", "error reading round JSON from "+roundID+ " (RoundScores.DeleteSelectedRound)");
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Delete Round");
            alert.setMessage("You may not delete this round as you are not the creator of the round.");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                } //OnClick (dialog)
            }); //OnClickListener
            alert.show();
        }
    } //DeleteSelectedRound

    public void MergeSeletedRound (){
        //Criteria for Merge
        // 1. I own this round
        // 2. I am connected to Cloud Database (must be Cloud Round to merge to)
        // 3. Selected rounds not same (cannot merge round with itself)
        // 4. Round to Merge to is Public Cloud Round (not local)
        // 5. Both rounds have same RoundTypeID
        // 6. Nobody has scores in both rounds.
        if (creatorID.equals(myAppState.CDS.getUserID())) { // Criteria 1. I own this Round
            if (myAppState.CDS.authenticated){  //Criteria 2 : connected to Cloud Datanase
                //Criteria 3, 4, 5, 6 done in MergeRound Fragment
                Bundle args = new Bundle();
                args.putString("roundID", roundID);
                Fragment fragment = new MergeRound();
                if (fragment != null) {
                    FragmentManager fragmentManager = getFragmentManager();
                    fragment.setArguments(args);
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment)
                            .addToBackStack("")
                            .commit();
                }
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Merge Round");
                alert.setMessage("You can only merge rounds if you are connected to a Cloud Database.");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    } //OnClick (dialog)
                }); //OnClickListener
                alert.show();
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle("Merge Round");
            alert.setMessage("You may not merge this round as you are not the creator of the round.");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                } //OnClick (dialog)
            }); //OnClickListener
            alert.show();
        }
    } //MergeSelectedRound


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.round_menu_action_edit:
                //Toast.makeText(getActivity().getApplicationContext(), "Edit RoundSelected", Toast.LENGTH_LONG).show();
                EditSelectedRound();
                return true;
            case R.id.round_menu_action_delete:
                //Toast.makeText(getActivity().getApplicationContext(),"Delete Round Selected",Toast.LENGTH_LONG).show();
                DeleteSelectedRound();
                return true;
            case R.id.round_menu_action_merge:
                //Toast.makeText(getActivity().getApplicationContext(),"Merge Round Selected",Toast.LENGTH_LONG).show();
                MergeSeletedRound ();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    View.OnClickListener myClickListener = new View.OnClickListener() {

        public void onClick(View v) {

            alRows.get(currentEnd).setBackgroundColor(Color.WHITE);
            currentEnd = v.getId()-1000;  //save ID of new selected row for next time.
            myAppState.setCurrentEnd(currentEnd);
            db.setCurrentEnd (roundID, myAppState.CDS.getUserID(), currentEnd);
            alRows.get(currentEnd).setBackgroundColor(res.getColor(R.color.orange_1));


            Bundle args = new Bundle();
            args.putString("roundID", roundID);
            Fragment fragment = new EditScores();
            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                fragment.setArguments(args);
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, fragment)
                        .addToBackStack("")
                        .commit();
            }
    }
    };



}
