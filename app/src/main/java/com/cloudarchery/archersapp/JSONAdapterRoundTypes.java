package com.cloudarchery.archersapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by paulwilliams on 01/01/15.
 */

public class JSONAdapterRoundTypes extends BaseAdapter {

    // private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";
    //private static final String FIREBASE_URL_BASE = "https://vivid-torch-6393.firebaseio.com/";

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;

    public JSONAdapterRoundTypes(Context context, LayoutInflater inflater) {
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
            convertView = mInflater.inflate(R.layout.row_roundtypes, null);

            // create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.roundTypeNameTextView = (TextView) convertView.findViewById(R.id.text_roundtypename);
            holder.roundTypeDetailTextView = (TextView) convertView.findViewById(R.id.text_roundtypedetail);
            holder.roundTypeDescriptionTextView = (TextView) convertView.findViewById(R.id.text_roundtypedescription);

            // hang onto this holder for future recyclage
            convertView.setTag(holder);
        } else {
            // skip all the expensive inflation/findViewById and just get the holder you already made
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data in JSON form
        JSONObject jsonObject = (JSONObject) getItem(position);

        // Grab the title and author from the JSON
        String roundTypeName = "";
        String roundTypeDetail = "";
        String roundTypeDescription = "";

        if (jsonObject.has("name")) {
            roundTypeName = jsonObject.optString("name");
        }

        if (jsonObject.has("description")) {
            roundTypeDescription = jsonObject.optString("description");
        }

        if (jsonObject.has("numEnds") && (jsonObject.has("numArrowsPerEnd"))) {
            roundTypeDetail = "( " + jsonObject.optString("numEnds") + " x "+ jsonObject.optString("numArrowsPerEnd")+ " )";
        }

// Send these Strings to the TextViews for display
        holder.roundTypeNameTextView.setText(roundTypeName);
        holder.roundTypeDetailTextView.setText(roundTypeDetail);
        holder.roundTypeDescriptionTextView.setText(roundTypeDescription);

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
        public TextView roundTypeNameTextView;
        public TextView roundTypeDetailTextView;
        public TextView roundTypeDescriptionTextView;
    }
}
