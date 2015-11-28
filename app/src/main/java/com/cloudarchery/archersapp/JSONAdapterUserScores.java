package com.cloudarchery.archersapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by paulwilliams on 01/01/15.
 */

public class JSONAdapterUserScores extends BaseAdapter {

    // private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";
    //private static final String FIREBASE_URL_BASE = "https://vivid-torch-6393.firebaseio.com/";

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;
    int arrowCount = 0;
    int grandTotal = 0;
    float arrowAvg = 0.0f;

    public JSONAdapterUserScores(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mJsonArray = new JSONArray();
    }

    @Override
    public int getCount() {
        return mJsonArray.length();
    }

    @Override
    public JSONObject getItem(int position) {
        return mJsonArray.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // check if the view already exists: if so, no need to inflate and findViewById again!
        if (convertView == null) {

            // Inflate the custom row layout from your XML.
            convertView = mInflater.inflate(R.layout.row_userstatus, null);

            // create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.userNameTextView = (TextView) convertView.findViewById(R.id.text_userstatus_name);
            holder.userTotalTextView = (TextView) convertView.findViewById(R.id.text_userstatus_total);
            holder.userAverageTextView = (TextView) convertView.findViewById(R.id.text_userstatus_avg);
            holder.userArrowCountTextView = (TextView) convertView.findViewById(R.id.text_userstatus_arws);

            // hang onto this holder for future recyclage
            convertView.setTag(holder);
        } else {
            // skip all the expensive inflation/findViewById and just get the holder you already made
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data in JSON form
        JSONObject jsonObject = (JSONObject) getItem(position);
        JSONObject statusJSON = null;
        // Keeping above as not sure if need later
        //holder.thumbnailImageView.setImageResource(R.drawable.ic_launcher);

        // Grab the title and author from the JSON
        String userName = "no name read";
        String userTotal = "-";
        String userAvg = "-.-";
        String userArrows = "-";

        if ((jsonObject.has("name") && !jsonObject.optString("name").equals(""))) {
            userName = jsonObject.optString("name");
        } else {
            userName = "Me";
        }

        if (jsonObject.has("status")){
            try {
                statusJSON = jsonObject.getJSONObject("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (statusJSON.has("totalScore")) {
            grandTotal = statusJSON.optInt("totalScore");
            userTotal = ""+grandTotal;
        }

        if (statusJSON.has("totalArrows")) {
            arrowCount = statusJSON.optInt("totalArrows");
            userArrows = ""+arrowCount;
        }

        if (arrowCount == 0) {
            arrowAvg = 0;
        } else {
            arrowAvg = (float)grandTotal/arrowCount;
        }
        DecimalFormat df = new DecimalFormat("0.00");
        userAvg = ""+df.format(arrowAvg);


// Send these Strings to the TextViews for display
        holder.userNameTextView.setText(userName);
        holder.userTotalTextView.setText(userTotal);
        holder.userAverageTextView.setText(userAvg);
        holder.userArrowCountTextView.setText(userArrows);

        return convertView;
    }

    public void updateData(JSONArray jsonArray) {
        // update the adapter's dataset
        mJsonArray = jsonArray;
        notifyDataSetChanged();
    }

    // this is used so you only ever have to do inflation and finding by ID once ever per View
    private static class ViewHolder {
        // public ImageView thumbnailImageView;
        public TextView userNameTextView;
        public TextView userTotalTextView;
        public TextView userAverageTextView;
        public TextView userArrowCountTextView;
    }
}
