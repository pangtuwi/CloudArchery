package com.cloudarchery.archersapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by paulwilliams on 01/01/15.
 */

public class JSONAdapterJoinRoundList extends BaseAdapter {

    // private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";
    //private static final String FIREBASE_URL_BASE = "https://vivid-torch-6393.firebaseio.com/";

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;

    public JSONAdapterJoinRoundList(Context context, LayoutInflater inflater) {
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
        /* ### can use this to go back and get more info.  see http://www.raywenderlich.com/78578/android-tutorial-for-beginners-part-3*/
        /* ### can use this to go back and get more info.  see http://www.raywenderlich.com/78578/android-tutorial-for-beginners-part-3*/
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // check if the view already exists: if so, no need to inflate and findViewById again!
        if (convertView == null) {

            // Inflate the custom row layout from your XML.
            convertView = mInflater.inflate(R.layout.row_joinround, null);

            // create a new "Holder" with subviews
            holder = new ViewHolder();
            // holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.joinround_img_thumbnail);
            holder.roundTypeTextView = (TextView) convertView.findViewById(R.id.joinround_roundtype);
            holder.roundDateTextView = (TextView) convertView.findViewById(R.id.joinround_rounddate);
            holder.createdByTextView = (TextView) convertView.findViewById(R.id.joinround_createdby);

            // hang onto this holder for future recyclage
            convertView.setTag(holder);
        } else {
            // skip all the expensive inflation/findViewById and just get the holder you already made
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data in JSON form
        JSONObject roundJSON = (JSONObject) getItem(position);

        // Keeping above as not sure if need later
        // holder.thumbnailImageView.setImageResource(R.drawable.ic_launcher);

        // Grab the title and author from the JSON
        String roundType = "";
        String roundDate = "";
        String createdBy = "";


        try {
            if (roundJSON.has("roundType")) {
                roundType = roundJSON.getJSONObject("roundType").optString("name");
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }

        if (roundJSON.has("createdAt")) {
            Long createdAtLong = roundJSON.optLong("createdAt");
            Date createdAtDate = new Date(createdAtLong);
            //String DATE_FORMAT_NOW = "yyyy-MM-dd";
            String DATE_FORMAT_NOW = "EEEE dd MMMM yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            roundDate = sdf.format(createdAtDate);
        }

        if (roundJSON.has("creator")) {
            try {
                createdBy = "created by : " + roundJSON.getJSONObject("creator").getString("name");
            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("MCCArchers", "Could not get Creator from JSONObject (JSONAdapterJoinRoundList.getView)");
            }
        }


// Send these Strings to the TextViews for display
        holder.roundTypeTextView.setText(roundType);
        holder.roundDateTextView.setText(roundDate);
        holder.createdByTextView.setText(createdBy);

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
        public TextView roundTypeTextView;
        public TextView roundDateTextView;
        public TextView createdByTextView;
    }
}