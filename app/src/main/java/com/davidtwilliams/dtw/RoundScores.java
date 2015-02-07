package com.davidtwilliams.dtw;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by paulwilliams on 10/01/15.
 */
public class RoundScores extends Activity {

    TextView HeaderText;
    //TableLayout ScoresTable;
    Firebase myFirebaseRef;

    int nArrows = 6;
    int nEnds = 10;
    int currentEnd = 0;
    int endTotal = 0;
    int grandTotal = 0;
    int currentArrow = 0;
    int arrowCount = 0;
    float arrowAvg;
    JSONObject roundScores;
    JSONArray endsArr;
    boolean okToEdit = false;
    String roundID;
    String userID;
    int roundIDInt;
    String roundDate;
    String roundType;


    private static final int REQUEST_CODE = 10;   //Arbitrary number? http://www.vogella.com/tutorials/AndroidIntent/article.html

    //Create Array Lists for the Textviews
    ArrayList<TableRow> alRows = new ArrayList<TableRow>();
    ArrayList<TextView> alEndTotals = new ArrayList<TextView>();
    ArrayList<TextView> alRunningTotals = new ArrayList<TextView>();

    TextView arrowTextView;

    Resources res;
    int orange_1;


    View.OnClickListener myClickListener = new View.OnClickListener() {

        public void onClick(View v) {
            // v is the TableRow that was clicked
            if (!okToEdit) {
                Toast.makeText(getApplicationContext(), "No user data for round : Editing disabled", Toast.LENGTH_LONG).show();
                return;
            };

            //v.setBackgroundColor(orange_1);  //set new selected row to red

            currentEnd = v.getId()-100;  //save ID of new selected row for next time.
            alRows.get(currentEnd).setBackgroundColor(Color.LTGRAY);  //change remembered row to light Grey

            Intent detailIntent = new Intent(v.getContext(), EditScores.class);
            detailIntent.putExtra("currentEnd", currentEnd);
            detailIntent.putExtra("currentArrow", currentArrow);
            detailIntent.putExtra("roundID", roundID);
            detailIntent.putExtra("userID", userID);
            detailIntent.putExtra("roundIDInt", roundIDInt);
            detailIntent.putExtra("grandTotal", grandTotal);

            try {
                JSONArray arrowsArr = endsArr.getJSONArray(currentEnd);
                String arrowsArrJSONString = arrowsArr.toString();
                detailIntent.putExtra("arrowsArr", arrowsArrJSONString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivityForResult(detailIntent, REQUEST_CODE);
        }
    };

    protected void loadData() {
        //Loads data when required by Activity
        Firebase.setAndroidContext(this);

        //Set basic information in the header (verbose for debug)
        HeaderText = (TextView) findViewById(R.id.round_scores_text);
        HeaderText.setText(roundDate + " " + roundType + " (" + roundID + ")");

        //Set Callback to get data from DB
        myFirebaseRef = new Firebase(getString(R.string.firebase_url));
        myFirebaseRef.child("scores/"+userID+"/"+roundID).addValueEventListener(new ValueEventListener() {

            @Override public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String myJSONString = snapshot.getValue().toString();
                    //Log.d("My App", myJSONString);
                    try {
                        endTotal = 0;
                        grandTotal = 0;
                        int tempArrow = 0;
                        arrowCount = 0;
                        try {
                            roundScores = new JSONObject(myJSONString);
                        } catch (Throwable t) {
                            Log.d("roundScores = new JSONObject(myJSONString)", myJSONString);
                        }
                        try {
                            endsArr = roundScores.getJSONArray("data");
                        } catch (Throwable t) {
                            Log.d("endsArr = roundScores.getJSONArray(\"data\");", myJSONString);
                        }

                        for (int i=0; i < endsArr.length(); i++) {

                            endTotal = 0;
                            JSONArray arrowsArr = endsArr.getJSONArray(i);

                            Log.d("MCCArchers", "Setting color : row is "+i+", current end is "+currentEnd);
                            if(i==currentEnd)  {
                                alRows.get(i).setBackgroundColor(orange_1);
                                Log.d("MCCArchers", "Setting color : SUCCESS");
                            }


                            for (int j=0; j < arrowsArr.length(); j++) {
                                //Log.d("doing arrow: "+i+", "+j, "top");

                                tempArrow = arrowsArr.getInt(j);

                                if (tempArrow == -1) {
                                    arrowTextView = (TextView) findViewById(200+(i*nArrows)+j);
                                    arrowTextView.setText("-");
                                } else {
                                    //Log.d("doing arrow: "+i+", "+j, "top of sums, arrow is:"+currentArrow);

                                    arrowCount++;
                                    //Log.d("doing arrow: "+i+", "+j, "top of sums, arrowCount is:"+arrowCount);

                                    endTotal = endTotal + tempArrow;
                                    //Log.d("doing arrow: "+i+", "+j, "top of sums, endTotal is:"+endTotal);

                                    grandTotal = grandTotal + tempArrow;
                                    //Log.d("doing arrow: "+i+", "+j, "top of sums, grandTotal is:"+grandTotal);


                                    arrowTextView = (TextView) findViewById(200+(i*nArrows)+j);
                                    if (arrowTextView != null) {
                                        arrowTextView.setText("" + tempArrow);
                                    } else {

                                        Log.d("did not write arrow: "+i+", "+j, ""+200+(i*nArrows)+j);
                                    }
                                }
                            }
                            TextView endTextView = (TextView) findViewById(400+i);
                            endTextView.setText(""+endTotal);
                            TextView totalTextView = (TextView) findViewById(600+i);
                            totalTextView.setText(""+grandTotal);
                        }

                        TextView grandTotalTextView = (TextView) findViewById(R.id.round_total);
                        grandTotalTextView.setText("Total="+grandTotal);

                        if (arrowCount == 0) {
                            arrowAvg = 0;
                        } else {
                            arrowAvg = (float)grandTotal/arrowCount;
                        }
                        DecimalFormat df = new DecimalFormat("0.00");
                        TextView arrowAvgTextView = (TextView) findViewById(R.id.round_avg);
                        arrowAvgTextView.setText("Average="+df.format(arrowAvg));

                        TextView arrowCountTextView = (TextView) findViewById(R.id.round_arrows);
                        arrowCountTextView.setText("Arrows="+arrowCount);

                        int endsCompleted = arrowCount/nArrows;
                        TextView endsTextView = (TextView) findViewById(R.id.round_ends);
                        endsTextView.setText("Ends="+endsCompleted);

                        okToEdit = true;

                    } catch (Throwable t) {
                        t.printStackTrace();
                        okToEdit = false;
                        Log.e("MCCArchers", "RoundScores load incomplete: Error in data or could not parse malformed JSON ");
                        Toast.makeText(getApplicationContext(), "Error loading round data", Toast.LENGTH_LONG).show();
                    }
                } else {
                    okToEdit = false;
                    Log.d("MCCArchers", "No Data Record found");
                    Toast.makeText(getApplicationContext(), "No data record found for this round", Toast.LENGTH_LONG).show();
                }
            } //onDataChange

            @Override public void onCancelled(FirebaseError error) { }

        }); //addValueEventListener


    } //loadData

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Toast.makeText(getApplicationContext(), "Loading Data", Toast.LENGTH_LONG).show();
        super.onCreate(savedInstanceState);

        Log.d("MCCArchers", "Running RoundScores Oncreate");

        //Get info from intent
        try {
            roundID = this.getIntent().getExtras().getString("roundID");
            userID = this.getIntent().getExtras().getString("userID");
            currentEnd = this.getIntent().getExtras().getInt("currentEnd");
            currentArrow = this.getIntent().getExtras().getInt("currentArrow");
            roundIDInt = this.getIntent().getExtras().getInt("roundIDInt");
            roundDate = this.getIntent().getExtras().getString("roundDate");
            roundType = this.getIntent().getExtras().getString("roundType");
            Log.d("MCCArchers", "RoundScores OnCreate... CurrentEnd retrieved from Intent as "+currentEnd);
        } catch (Throwable t) {
            Log.e("MCCArchers", "No Intent data found...");
        };


        //Initiate required variables
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, getResources().getDisplayMetrics());

        // Tell the activity which XML layout is right
        setContentView(R.layout.round_scores);

        //create table programatically
        TableLayout tl = (TableLayout) findViewById(R.id.dynamic_scores_table);

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

        //iterate through the number of ends, creating a row for each.
        for (int j = 0; j < nEnds; j++) {
            // Create the table row
            alRows.add(new TableRow(this));

            alRows.get(j).setId(100+j);

            alRows.get(j).setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            alRows.get(j).setPadding(5, 10, 5, 10);

            alEndTotals.add(new TextView(this));
            alRunningTotals.add(new TextView(this));

            ArrayList<TextView> alArrows = new ArrayList<TextView>();

            for (int i = 0; i < nArrows; i++) {
                alArrows.add(new TextView(this));

                alArrows.get(i).setId(200+(j*nArrows)+i);
                alArrows.get(i).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                alArrows.get(i).setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                alArrows.get(i).setBackground(gd);
                //alArrows.get(i).setText(""+j+","+i); Debugging
                alArrows.get(i).setText("");
                alArrows.get(i).setPadding(2, 0, 5, 0);
                alArrows.get(i).setTextColor(Color.BLACK);
                alArrows.get(i).setWidth(px);
                alRows.get(j).addView(alArrows.get(i));

                TextView arrowInnerGap = new TextView(this);
                //arrowInnerGap.setId(300+j);
                arrowInnerGap.setText(" ");
                arrowInnerGap.setWidth(5);
                alRows.get(j).addView(arrowInnerGap);
            }

            TextView arrowGap = new TextView(this);
            arrowGap.setId(300+j);
            arrowGap.setText(" ");
            arrowGap.setWidth(20);
            alRows.get(j).addView(arrowGap);

            alEndTotals.get(j).setId(400+j);
            alEndTotals.get(j).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            alEndTotals.get(j).setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            alEndTotals.get(j).setBackground(gdTotal);
            alEndTotals.get(j).setText("0");
            alEndTotals.get(j).setPadding(2, 0, 5, 0);
            alEndTotals.get(j).setTextColor(Color.BLACK);
            alEndTotals.get(j).setWidth(px);
            alRows.get(j).addView(alEndTotals.get(j));

            TextView totalGap = new TextView(this);
            totalGap.setId(500+j);
            totalGap.setText(" ");
            totalGap.setWidth(10);
            alRows.get(j).addView(totalGap);

            alRunningTotals.get(j).setId(600+j);
            alRunningTotals.get(j).setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            alRunningTotals.get(j).setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            alRunningTotals.get(j).setBackground(gdRTotal);
            alRunningTotals.get(j).setText("0");
            alRunningTotals.get(j).setPadding(2, 0, 5, 0);
            alRunningTotals.get(j).setTextColor(Color.WHITE);
            alRunningTotals.get(j).setWidth(px);
            alRows.get(j).addView(alRunningTotals.get(j));

            alRows.get(j).setOnClickListener(myClickListener);

            if(j==currentEnd)  {
                alRows.get(j).setBackgroundColor(orange_1);
                Log.d("MCCArchers", "Setting initial color in onCreate : SUCCESS");
            }

            // finally add this to the table row
            tl.addView(alRows.get(j), new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
        }

        // Enable the "Up" button for more navigation options
        getActionBar().setDisplayHomeAsUpEnabled(true);


    }  // Oncreate

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }//onStart

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnData) {
        super.onActivityResult(requestCode, resultCode, returnData);
        Toast.makeText(this, "saved...", Toast.LENGTH_SHORT).show();
        Log.e("MCCArchers", "In onActivityResult (request/result...("+requestCode+ "/ "+resultCode+") should match ("+REQUEST_CODE+"/"+RESULT_OK+")");
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {

            //Get info from intent
            try {
                //roundID = returnData.getExtras().getString("roundID");
                //userID = returnData.getExtras().getString("userID");
                currentEnd = returnData.getExtras().getInt("currentEnd");
                currentArrow = returnData.getExtras().getInt("currentArrow");
                Log.d("MCCArchers", "RoundScores OnActivityresult... CurrentEnd retrieved from Intent as "+currentEnd);
                loadData();
            } catch (Throwable t) {
                Log.e("MCCArchers", "No Intent data found...");
            };

            if (returnData.hasExtra("returnkey")) {
                String result = returnData.getExtras().getString("returnkey");
                if (result != null && result.length() > 0) {
                    //Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                }
            }
        }
    } //onActivityResult

}
