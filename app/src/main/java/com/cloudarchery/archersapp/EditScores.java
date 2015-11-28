package com.cloudarchery.archersapp;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by paulwilliams on 22/01/15.
 */
public class EditScores extends Fragment {

    MyApp myAppState;
    ArrayList<TextView> alArrows = new ArrayList<TextView>();
    GradientDrawable gd;
    GradientDrawable gdSelected;
    int numArrowsPerEnd;
    int numEnds;
    
    String roundID;

    int currentArrow;
    int initialCurrentEnd;
    int thisArrow;
    int currentEnd;
    int endTotal = 0;
    int totalScore = 0;
    int totalArrows = 0;
    boolean complete = false;
    SQLiteRounds db;
    //JSONObject userJSON;
    JSONObject roundJSON;
    JSONObject userJSON;
    //JSONObject roundUserJSON;
    JSONArray arrowsArr;
    ClubFirebase CDS;
    //JSONObject scoresJSON;

    public EditScores(){}

    View.OnClickListener myClickListener = new View.OnClickListener() {

        public void onClick(View v) {        // v is the arrow that was clicked
            //Create a Block format style for Arrow scores
            final GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.WHITE);
            gd.setCornerRadius(7);
            gd.setStroke(2, 0xFF000000);

            //.. and a format for selected arrow
            final GradientDrawable gdSelected = new GradientDrawable();
            gdSelected.setColor(Color.WHITE);
            gdSelected.setCornerRadius(4);
            gdSelected.setStroke(6, 0xFF000000);

            alArrows.get(currentArrow).setBackground(gd);
            currentArrow = v.getId()-200;
            alArrows.get(currentArrow).setBackground(gdSelected);
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        
        // Tell the activity which XML layout is right
        View rootView = inflater.inflate(R.layout.edit_scores, container, false);

        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, getResources().getDisplayMetrics());

        myAppState = ((MyApp)getActivity().getApplicationContext());
        currentEnd = myAppState.getCurrentEnd();
        CDS = myAppState.CDS;

        initialCurrentEnd = currentEnd;
        currentArrow = myAppState.getCurrentArrow();

        Bundle args = getArguments();
        roundID = args.get("roundID").toString();
        db = new SQLiteRounds(getActivity());
        //userJSON = db.getUserJSON(roundID);
        roundJSON = db.getRoundJSON(roundID);

        try {
            //roundDetailJSON = roundJSON.getJSONObject("detail");
            //roundUsersJSON = roundJSON.getJSONObject("users");
            userJSON = roundJSON.getJSONObject("scores").getJSONObject("users").getJSONObject(CDS.getUserID());
            numArrowsPerEnd = roundJSON.getInt("numArrowsPerEnd");
            numEnds = roundJSON.getInt("numEnds");

            JSONArray dataArrJSON = userJSON.getJSONArray("data");
            JSONObject statusObjJSON = userJSON.getJSONObject("status");
            totalArrows = statusObjJSON.getInt("totalArrows");
            totalScore = statusObjJSON.getInt("totalScore");
            complete = statusObjJSON.getBoolean("complete");
            arrowsArr = dataArrJSON.getJSONArray(currentEnd);

        } catch (Throwable t) {
            t.printStackTrace();
            Log.d("MCCArchers", "error reading values from LDS JSON (EditScores.onCreateView)");
        }
        

        TextView tvHeader = (TextView) rootView.findViewById(R.id.editscores_tv_header);
        tvHeader.setText("Scoring End "+ (currentEnd+1) + " of "+numEnds);
        //Create a Block format style for Arrow scores
        final GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.WHITE);
        gd.setCornerRadius(7);
        gd.setStroke(2, 0xFF000000);

        //.. and a format for selected arrow
        final GradientDrawable gdSelected = new GradientDrawable();
        gdSelected.setColor(Color.WHITE);
        gdSelected.setCornerRadius(4);
        gdSelected.setStroke(6, 0xFF000000);

        //... and a Block format for End Totals
        GradientDrawable gdTotal = new GradientDrawable();
        gdTotal.setColor(Color.LTGRAY);
        gdTotal.setCornerRadius(7);
        gdTotal.setStroke(4, 0xFF000000);

        //create table in code (not in res file)
        TableLayout tl = (TableLayout) rootView.findViewById(R.id.edit_score_table);
        final TextView alEndTotal = new TextView(getActivity());
        //TextView alEndTotal = new TextView(rootView.getContext());
        TableRow alRow = new TableRow(getActivity());
        //TableRow alRow = new TableRow(rootView.getContext());

        alRow.setId(100);
        alRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        alRow.setPadding(5, 10, 5, 10);
        Resources res = getResources();
        int orange_1 = res.getColor(R.color.orange_1);
        alRow.setBackgroundColor(orange_1);

        //Todo : (RELEASE > 2) : Make the interface show the distance / target size in between the ends


        for (int i = 0; i < numArrowsPerEnd; i++) {
            //alArrows.add(new TextView(rootView.getContext()));
            alArrows.add(new TextView(getActivity()));
            alArrows.get(i).setId(200+i);
            alArrows.get(i).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            alArrows.get(i).setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            alArrows.get(i).setBackground(gd);

            try {
                thisArrow = arrowsArr.getInt(i);
                if (thisArrow == -1) {
                    alArrows.get(i).setText("-");
                } else {
                    endTotal += thisArrow;
                    alArrows.get(i).setText("" + thisArrow);
                }
            } catch (JSONException e) {
                alArrows.get(i).setText("E");
                e.printStackTrace();
            }

            alArrows.get(i).setPadding(2, 2, 5, 0);
            alArrows.get(i).setTextColor(Color.BLACK);
            alArrows.get(i).setWidth(px);

            alArrows.get(i).setOnClickListener(myClickListener);

            alRow.addView(alArrows.get(i));

            //TextView arrowInnerGap = new TextView(rootView.getContext());
            TextView arrowInnerGap = new TextView(getActivity());
            arrowInnerGap.setText(" ");
            arrowInnerGap.setWidth(5);
            alRow.addView(arrowInnerGap);
        }

        //TextView arrowGap = new TextView(rootView.getContext());
        TextView arrowGap = new TextView(getActivity());
        arrowGap.setId(300);
        arrowGap.setText(" ");
        arrowGap.setWidth(30);
        alRow.addView(arrowGap);

        alEndTotal.setId(400);
        alEndTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        alEndTotal.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        alEndTotal.setBackground(gdTotal);
        alEndTotal.setText("" + endTotal);
        alEndTotal.setPadding(2, 0, 5, 0);
        alEndTotal.setTextColor(Color.BLACK);
        alEndTotal.setWidth(px);
        alRow.addView(alEndTotal);

         // finally add this to the table row
         tl.addView(alRow, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

        // Enable the "Up" button for more navigation options
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        alArrows.get(currentArrow).setBackground(gdSelected);



        Button button10 = (Button) rootView.findViewById(R.id.editscores_button_10);
        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "TEN!!", Toast.LENGTH_LONG).show();
                alArrows.get(currentArrow).setText("10");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1) tempArrow = 0;
                    endTotal = endTotal - tempArrow + 10;
                    totalScore = totalScore - tempArrow + 10;
                    arrowsArr.put(currentArrow, 10);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {
                    currentArrow = 0;
                    currentEnd = initialCurrentEnd + 1;
                    saveAndExit();
                }
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText("" + endTotal);
            }
        });  //button10 OnClickListener


        Button button9 = (Button) rootView.findViewById(R.id.editscores_button_9);
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "TEN!!", Toast.LENGTH_LONG).show();
                alArrows.get(currentArrow).setText("9");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 9;
                    totalScore = totalScore - tempArrow + 9;
                    arrowsArr.put(currentArrow, 9);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {currentArrow = 0; currentEnd = initialCurrentEnd+1;saveAndExit();}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button9 OnClickListener

        Button button8 = (Button) rootView.findViewById(R.id.editscores_button_8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "TEN!!", Toast.LENGTH_LONG).show();
                alArrows.get(currentArrow).setText("8");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1) tempArrow = 0;
                    endTotal = endTotal - tempArrow + 8;
                    totalScore = totalScore - tempArrow + 8;
                    arrowsArr.put(currentArrow, 8);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {
                    currentArrow = 0;
                    currentEnd = initialCurrentEnd + 1;
                    saveAndExit();
                }
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText("" + endTotal);
            }
        });  //button8 OnClickListener

        Button button7 = (Button) rootView.findViewById(R.id.editscores_button_7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "TEN!!", Toast.LENGTH_LONG).show();
                alArrows.get(currentArrow).setText("7");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 7;
                    totalScore = totalScore - tempArrow + 7;
                    arrowsArr.put(currentArrow, 7);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {currentArrow = 0; currentEnd = initialCurrentEnd+1;saveAndExit();}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button7 OnClickListener

        Button button6 = (Button) rootView.findViewById(R.id.editscores_button_6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "TEN!!", Toast.LENGTH_LONG).show();
                alArrows.get(currentArrow).setText("6");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 6;
                    totalScore = totalScore - tempArrow + 6;
                    arrowsArr.put(currentArrow, 6);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {currentArrow = 0; currentEnd = initialCurrentEnd+1;saveAndExit();}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button6 OnClickListener

        Button button5 = (Button) rootView.findViewById(R.id.editscores_button_5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("5");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1) tempArrow = 0;
                    endTotal = endTotal - tempArrow + 5;
                    totalScore = totalScore - tempArrow + 5;
                    arrowsArr.put(currentArrow, 5);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {
                    currentArrow = 0;
                    currentEnd = initialCurrentEnd + 1;
                    saveAndExit();
                }
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText("" + endTotal);
            }
        });  //button5 OnClickListener

        Button button4 = (Button) rootView.findViewById(R.id.editscores_button_4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("4");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 4;
                    totalScore = totalScore - tempArrow + 4;
                    arrowsArr.put(currentArrow, 4);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {currentArrow = 0; currentEnd = initialCurrentEnd+1;saveAndExit();}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button4 OnClickListener

        Button button3 = (Button) rootView.findViewById(R.id.editscores_button_3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("3");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 3;
                    totalScore = totalScore - tempArrow + 3;
                    arrowsArr.put(currentArrow, 3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {currentArrow = 0; currentEnd = initialCurrentEnd+1;saveAndExit();}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button3 OnClickListener

        Button button2 = (Button) rootView.findViewById(R.id.editscores_button_2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("2");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 2;
                    totalScore = totalScore - tempArrow + 2;
                    arrowsArr.put(currentArrow, 2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {currentArrow = 0; currentEnd = initialCurrentEnd+1;saveAndExit();}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button2 OnClickListener

        Button button1 = (Button) rootView.findViewById(R.id.editscores_button_1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("1");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 1;
                    totalScore = totalScore - tempArrow + 1;
                    arrowsArr.put(currentArrow, 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {currentArrow = 0; currentEnd = initialCurrentEnd+1;saveAndExit();}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button1 OnClickListener

        Button button0 = (Button) rootView.findViewById(R.id.editscores_button_0);
        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("0");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((tempArrow == -1) && (totalArrows < (numArrowsPerEnd*numEnds))) totalArrows+=1;
                    if (tempArrow == -1) tempArrow = 0;
                    endTotal = endTotal - tempArrow + 0;
                    totalScore = totalScore - tempArrow + 1;
                    arrowsArr.put(currentArrow, 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == numArrowsPerEnd) {currentArrow = 0; currentEnd = initialCurrentEnd+1; saveAndExit();}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText("" + endTotal);
            }
        });  //button0 OnClickListener

        Button buttonDelete = (Button) rootView.findViewById(R.id.editscores_button_delete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if ((currentArrow > 0) && (tempArrow == -1)) {
                        currentArrow--;
                        tempArrow = arrowsArr.getInt(currentArrow);
                    }
                    if (tempArrow != -1) {
                        endTotal = endTotal - tempArrow;
                        totalScore = totalScore - tempArrow;
                        arrowsArr.put(currentArrow, -1);
                        totalArrows -= 1;
                        alArrows.get(currentArrow).setText("-");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText("" + endTotal);

            }
        });  //buttonDelete OnClickListener

        Button buttonClear = (Button) rootView.findViewById(R.id.editscores_button_clear);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setBackground(gd);
                currentArrow = 0;
                for (int i=0; i<numArrowsPerEnd; i++){
                    try {
                        int tempArrow = arrowsArr.getInt(i);
                        if (tempArrow != -1) {
                            endTotal = endTotal - tempArrow;
                            totalScore = totalScore - tempArrow;
                            totalArrows -= 1;
                        }
                        arrowsArr.put(i, -1);
                        alArrows.get(i).setText("-");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    alArrows.get(currentArrow).setBackground(gdSelected);
                    alEndTotal.setText("" + endTotal);
                }
            }
        });  //buttonClear OnClickListener

        Button buttonSave = (Button) rootView.findViewById(R.id.editscores_button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndExit();
            }
        });  //buttonSave OnClickListener




        return rootView;
    } //OnCreate

    public void saveAndExit () {
       try{
           JSONObject statusJSON = userJSON.getJSONObject("status");
            complete = (totalArrows == (numArrowsPerEnd*numEnds));
            statusJSON.put("complete", complete);
            statusJSON.put("currentEnd", currentEnd);
            statusJSON.put("currentArrow", currentArrow);
            statusJSON.put("totalArrows", totalArrows);
            statusJSON.put("totalScore", totalScore);
            //write unix timestamp into Scores JSON Object
            long updatedAt = System.currentTimeMillis();
            userJSON.put("updatedAt", updatedAt);
            myAppState.setCurrentArrow(currentArrow);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    Log.d("MCCArchers", "EditScores Saving End: "+ userJSON.toString());
    CDS.saveEnd(roundID, userJSON);
    getFragmentManager().popBackStackImmediate();

}



}
