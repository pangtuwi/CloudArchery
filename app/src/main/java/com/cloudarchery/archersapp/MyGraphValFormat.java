package com.cloudarchery.archersapp;

import com.github.mikephil.charting.utils.ValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by paulwilliams on 16/04/15.
 */
public class MyGraphValFormat implements ValueFormatter {

    private DecimalFormat mFormat;

    public MyGraphValFormat() {
        mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
        //mFormat = new DecimalFormat("###,###,##0"); // use no decimal
    }

    @Override
    public String getFormattedValue(float value) {
        //return mFormat.format(value) + " $"; // append a dollar-sign
        return mFormat.format(value);
    }
}

