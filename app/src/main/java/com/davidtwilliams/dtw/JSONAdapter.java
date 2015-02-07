package com.davidtwilliams.dtw;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by paulwilliams on 01/01/15.
 */

public class JSONAdapter extends BaseAdapter {

   // private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";
    //private static final String FIREBASE_URL_BASE = "https://vivid-torch-6393.firebaseio.com/";

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;

    public JSONAdapter(Context context, LayoutInflater inflater) {
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
            convertView = mInflater.inflate(R.layout.row_round, null);

            // create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.img_thumbnail);
            holder.roundTypeTextView = (TextView) convertView.findViewById(R.id.text_roundtype);
            holder.roundDateTextView = (TextView) convertView.findViewById(R.id.text_rounddate);

            // hang onto this holder for future recyclage
            convertView.setTag(holder);
        } else {
            // skip all the expensive inflation/findViewById and just get the holder you already made
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data in JSON form
        JSONObject jsonObject = (JSONObject) getItem(position);
        // Keeping above as not sure if need later
         holder.thumbnailImageView.setImageResource(R.drawable.ic_launcher);

        // Grab the title and author from the JSON
        String roundType = "";
        String roundDate = "";

        if (jsonObject.has("roundType")) {
            roundType = jsonObject.optString("roundType");
        }

        if (jsonObject.has("date")) {
            //authorName = jsonObject.optJSONArray("author_name").optString(0);
            roundDate = jsonObject.optString("date");
        }

// Send these Strings to the TextViews for display
        holder.roundTypeTextView.setText(roundType);
        holder.roundDateTextView.setText(roundDate);

        return convertView;
    }

    public void updateData(JSONArray jsonArray) {
        // update the adapter's dataset
        mJsonArray = jsonArray;
        notifyDataSetChanged();
    }

    // this is used so you only ever have to do inflation and finding by ID once ever per View
    private static class ViewHolder {
        public ImageView thumbnailImageView;
        public TextView roundTypeTextView;
        public TextView roundDateTextView;
    }
}