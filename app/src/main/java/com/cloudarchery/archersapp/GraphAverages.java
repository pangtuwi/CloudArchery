package com.cloudarchery.archersapp;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GraphAverages extends Fragment {

    MyApp myAppState;
    Resources res;
    String userID;
    SQLiteRounds db;
    JSONArray roundsJSON;
    JSONObject thisRoundJSON;
    JSONObject thisScoresJSON;
    JSONObject thisUsersJSON;
    JSONObject thisUserJSON;
    JSONObject thisUserStatusJSON;
	
	public GraphAverages(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.graph_averages, container, false);
        res = getResources();
        int orange_1 = res.getColor(R.color.orange_1);
        int orange_2 = res.getColor(R.color.orange_2);
        int grey_1 = res.getColor(R.color.grey_1);

        myAppState = ((MyApp)getActivity().getApplicationContext());
        userID = myAppState.CDS.getUserID();

        LineChart mLineChart = (LineChart) rootView.findViewById(R.id.chart);
        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();

        db = new SQLiteRounds(getActivity());
        roundsJSON = db.getRoundsJSONArray();
        String roundDate = "";

        int numEntries = roundsJSON.length();
        if (numEntries != 0) {
            for (int i = 0; i < numEntries; i++) {
                try {
                    thisRoundJSON = roundsJSON.getJSONObject(i);
                    if (thisRoundJSON.has("createdAt")) {
                        Long createdAtLong = thisRoundJSON.optLong("createdAt");
                        Date createdAtDate = new Date(createdAtLong);
                        String DATE_FORMAT_NOW = "dd-MMM";
                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                        roundDate = sdf.format(createdAtDate);
                    }

                    thisScoresJSON = thisRoundJSON.getJSONObject("scores");
                    thisUsersJSON = thisScoresJSON.getJSONObject("users");
                    thisUserJSON = thisUsersJSON.getJSONObject(userID);
                    thisUserStatusJSON = thisUserJSON.getJSONObject("status");
                    int totalScore = thisUserStatusJSON.getInt("totalScore");
                    int totalArrows = thisUserStatusJSON.getInt("totalArrows");
                    boolean complete = thisUserStatusJSON.getBoolean("complete");
                    if (complete) {
                        float arrowAvg = (float) totalScore / totalArrows;
                        xVals.add(roundDate);
                        Entry c1e1 = new Entry(arrowAvg, i);
                        valsComp1.add(c1e1);
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    Log.d("MCCArchers", "error reading values from LDS JSON (ScoreHistory.onCreateView)");
                }
            }


            LineDataSet setComp1 = new LineDataSet(valsComp1, "");

            setComp1.setColor(orange_1);
            setComp1.setCircleColor(orange_1);
            setComp1.setCircleSize(10);
            setComp1.setCircleColorHole(orange_2);
            setComp1.setValueTextSize(12);
            setComp1.setLineWidth(3);

            YAxis rightAxis = mLineChart.getAxisRight();
            rightAxis.setEnabled(false);

            YAxis leftAxis = mLineChart.getAxisLeft();
            leftAxis.setTextSize(16);
            leftAxis.setValueFormatter(new MyGraphValFormat());
            leftAxis.setAxisLineColor(grey_1);
            leftAxis.enableGridDashedLine(10, 10, 1);

            XAxis xAxis = mLineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.enableGridDashedLine(10, 10, 1);
            xAxis.setTextSize(12);
            xAxis.setAdjustXLabels(true);
            xAxis.setSpaceBetweenLabels(1);


            dataSets.add(setComp1);
            LineData data = new LineData(xVals, dataSets);
            mLineChart.setData(data);

            mLineChart.setDescription("");
            mLineChart.setDescriptionTextSize(16);
            mLineChart.invalidate(); // refresh
        }
        return rootView;
    }
}
