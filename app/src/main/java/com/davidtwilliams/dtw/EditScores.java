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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.firebase.client.Firebase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paulwilliams on 22/01/15.
 */
public class EditScores extends Activity {

    int nArrows = 6;
    TextView HeaderText;
    JSONArray arrowsArr;
    String roundID;
    String userID;
    int roundIDInt;
    int currentArrow;
    int thisArrow;
    int currentEnd;
    int endTotal = 0;
    int grandTotal = 0;
    int firstEmptyArrow = 0;

    Firebase myFirebaseRef;

    ArrayList<TextView> alArrows = new ArrayList<TextView>();
    TextView alEndTotal;
    GradientDrawable gd;
    GradientDrawable gdSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MCCArchers", "Running EditScores.onCreate");

        // Tell the activity which XML layout is right
        setContentView(R.layout.edit_scores);
        //Toast.makeText(getApplicationContext(), "Loading Data", Toast.LENGTH_LONG).show();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, getResources().getDisplayMetrics());

        //Get the data for the Activity from the Intent
        String arrowsArrJSONString = this.getIntent().getExtras().getString("arrowsArr");
        currentEnd = this.getIntent().getExtras().getInt("currentEnd");
        currentArrow = this.getIntent().getExtras().getInt("currentArrow");
        userID = this.getIntent().getExtras().getString("userID");
        roundID = this.getIntent().getExtras().getString("roundID");
        roundIDInt = this.getIntent().getExtras().getInt("roundIDInt");
        grandTotal = this.getIntent().getExtras().getInt("grandTotal");
        //Log.d("MCCArchers", "currentEnd arrived in EditScores.java as "+currentEnd);

        //Convert JSON String to an Array
        try {
            arrowsArr = new JSONArray(arrowsArrJSONString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        TableLayout tl = (TableLayout) findViewById(R.id.edit_score_table);
        final TextView alEndTotal = new TextView(this);
        TableRow alRow = new TableRow(this);

        alRow.setId(100);
        alRow.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        alRow.setPadding(5, 10, 5, 10);
        Resources res = getResources();
        int orange_1 = res.getColor(R.color.orange_1);
        alRow.setBackgroundColor(orange_1);

        for (int i = 0; i < nArrows; i++) {
            alArrows.add(new TextView(this));

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
            alRow.addView(alArrows.get(i));

            TextView arrowInnerGap = new TextView(this);
            arrowInnerGap.setText(" ");
            arrowInnerGap.setWidth(5);
            alRow.addView(arrowInnerGap);
        }

        TextView arrowGap = new TextView(this);
        arrowGap.setId(300);
        arrowGap.setText(" ");
        arrowGap.setWidth(30);
        alRow.addView(arrowGap);

        alEndTotal.setId(400);
        alEndTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        alEndTotal.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        alEndTotal.setBackground(gdTotal);
        alEndTotal.setText(""+endTotal);
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

        //currentArrow =0;
        alArrows.get(currentArrow).setBackground(gdSelected);

        Button button10 = (Button) findViewById(R.id.button_10);
        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "TEN!!", Toast.LENGTH_LONG).show();
                alArrows.get(currentArrow).setText("10");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 10;
                    grandTotal = grandTotal - tempArrow + 10;
                    arrowsArr.put(currentArrow, 10);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button10 OnClickListener

        //TODO There has to be a better way to do this than having 10 basically identical functions
        Button button9 = (Button) findViewById(R.id.button_9);
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "TEN!!", Toast.LENGTH_LONG).show();
                alArrows.get(currentArrow).setText("9");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 9;
                    grandTotal = grandTotal - tempArrow + 9;
                    arrowsArr.put(currentArrow, 9);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button9 OnClickListener

        Button button8 = (Button) findViewById(R.id.button_8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "TEN!!", Toast.LENGTH_LONG).show();
                alArrows.get(currentArrow).setText("8");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 8;
                    grandTotal = grandTotal - tempArrow + 8;
                    arrowsArr.put(currentArrow, 8);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button8 OnClickListener

        Button button7 = (Button) findViewById(R.id.button_7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "TEN!!", Toast.LENGTH_LONG).show();
                alArrows.get(currentArrow).setText("7");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 7;
                    grandTotal = grandTotal - tempArrow + 7;
                    arrowsArr.put(currentArrow, 7);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button7 OnClickListener

        Button button6 = (Button) findViewById(R.id.button_6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "TEN!!", Toast.LENGTH_LONG).show();
                alArrows.get(currentArrow).setText("6");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 6;
                    grandTotal = grandTotal - tempArrow + 6;
                    arrowsArr.put(currentArrow, 6);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button6 OnClickListener

        Button button5 = (Button) findViewById(R.id.button_5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("5");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 5;
                    grandTotal = grandTotal - tempArrow + 5;
                    arrowsArr.put(currentArrow, 5);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button5 OnClickListener

        Button button4 = (Button) findViewById(R.id.button_4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("4");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 4;
                    grandTotal = grandTotal - tempArrow + 4;
                    arrowsArr.put(currentArrow, 4);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button4 OnClickListener

        Button button3 = (Button) findViewById(R.id.button_3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("3");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 3;
                    grandTotal = grandTotal - tempArrow + 3;
                    arrowsArr.put(currentArrow, 3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button3 OnClickListener

        Button button2 = (Button) findViewById(R.id.button_2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("2");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 2;
                    grandTotal = grandTotal - tempArrow + 2;
                    arrowsArr.put(currentArrow, 2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button2 OnClickListener

        Button button1 = (Button) findViewById(R.id.button_1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("1");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    int tempArrow = arrowsArr.getInt(currentArrow);
                    if (tempArrow == -1)tempArrow = 0;
                    endTotal = endTotal - tempArrow + 1;
                    grandTotal = grandTotal - tempArrow + 1;
                    arrowsArr.put(currentArrow, 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button1 OnClickListener

        Button button0 = (Button) findViewById(R.id.button_0);
        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alArrows.get(currentArrow).setText("0");
                alArrows.get(currentArrow).setBackground(gd);

                try {
                    //int tempArrow = arrowsArr.getInt(currentArrow);
                    //endTotal = endTotal - tempArrow + 0; not necessary
                    arrowsArr.put(currentArrow, 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                currentArrow++;
                if (currentArrow == nArrows) {currentArrow = 0;}
                alArrows.get(currentArrow).setBackground(gdSelected);
                alEndTotal.setText(""+endTotal);
            }
        });  //button0 OnClickListener

        Button buttonSave = (Button) findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myFirebaseRef = new Firebase(getString(R.string.firebase_url));
                String arrowsArrJSONString = arrowsArr.toString();
                //Log.d("MCCArchers", "Save Clicked in EditScores, SAVING DATA TO DB : "+arrowsArrJSONString);
                //Map dataMap = new HashMap();
                //dataMap.put(""+currentEnd, "{"+arrowsArrJSONString+"}");
                try {
                    for (int i = 0; i < nArrows; i++) {
                        myFirebaseRef.child("scores/"+userID+"/" + roundID + "/data/" + currentEnd + "/" + i).setValue(arrowsArr.get(i));
                    }
                    myFirebaseRef.child("scores/"+userID+"/" + roundID + "/score/").setValue(grandTotal);
                    myFirebaseRef.child("rounds/" + roundIDInt + "/scores/"+userID+"/").setValue(grandTotal);
                    myFirebaseRef.child("users/"+userID+"/scores/" + roundID).setValue(grandTotal);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //TODO Implement Listener to notify if saved

                Intent returnIntent = new Intent();
                arrowsArrJSONString = arrowsArr.toString();
                returnIntent.putExtra("arrowsArr", arrowsArrJSONString);
                returnIntent.putExtra("currentEnd", currentEnd);
                returnIntent.putExtra("currentArrow", currentArrow);
                returnIntent.putExtra("roundID", roundID);
                returnIntent.putExtra("userID", userID);
                returnIntent.putExtra("roundIDInt", roundIDInt);

                //Log.d("MCCArchers", "currentEnd set on Editscores OnClick as "+currentEnd +" with result "+RESULT_OK);

                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });  //buttonSave OnClickListener
    } //OnCreate


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MCCArchers", "Running EditScores.onStart");
    } //onStart

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // I think this is no longer required - fixed issue by setting launchmode = singletop in AndroidManifest.xml
        Log.d("MCCArchers", "Key up = " + keyCode);
        if(keyCode == KeyEvent.KEYCODE_PAGE_UP){
            return true;
        }else{
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
       // Log.d("MCCArchers", "Running onBackPressed in EditScores");
        //finish();

    }

    @Override
    public void finish() {
        super.finish();


    }

    @Override
    protected void onPause() {
        // Another activity is taking focus (this activity is about to be "paused").
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.d("MCCArchers", "Running onStop in EditScores");
        // The activity is no longer visible (it is now "stopped")
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.d("MCCArchers", "Running onDestroy in EditScores");
        // The activity is about to be destroyed.
    }

}
