package com.davidtwilliams.dtw;

/**
 * Created by paulwilliams on 02/02/15.
 * Matches version 2 of the SQL Database
 */
public class Round {

    private String id;
    private int roundTypeID;
    private String roundType;
    private String roundDescriptor;
    private String date;
    private int ends;
    private int arrowsPerEnd;
    private int currentEnd;
    private int currentArrow;
    private int arrowCount;
    private String arrowData;

    public Round(){}

    public Round(String id, int roundTypeID, String roundType, String roundDescriptor, String date, int ends, int arrowsPerEnd,
                 int currentEnd, int currentArrow, int arrowCount, String arrowData) {
        super();
        this.id = id;
        this.roundTypeID = roundTypeID;
        this.roundType = roundType;
        this.roundDescriptor = roundDescriptor;
        this.date = date;
        this.ends = ends;
        this.arrowsPerEnd = arrowsPerEnd;
        this.currentEnd = currentEnd;
        this.currentArrow = currentArrow;
        this.arrowCount = arrowCount;
        this.arrowData = arrowData;
    }

    //getters

    public String getId() {
        return id;
    }

    public int getRoundTypeID() {
       return roundTypeID;
    }

    public String getRoundType() {
        return roundType;
    }

    public String getRoundDescriptor() {
        return roundDescriptor;
    }

    public String getDate() {
        return date;
    }

    public int getEnds() {
        return ends;
    }

    public int getArrowsPerEnd() {
        return arrowsPerEnd;
    }

    public int getCurrentEnd() {
        return currentEnd;
    }

    public int getCurrentArrow() {
        return currentArrow;
    }

    public int getArrowCount() {
        return arrowCount;
    }

    public String getArrowData() {
        return arrowData;
    }

    //& setters

    public void setId(String id) {
        this.id = id;
    }

    public void setRoundTypeID(int roundTypeID) {
        this.roundTypeID = roundTypeID;
    }

    public void setRoundType(String roundType) {
        this.roundType = roundType;
    }

    public void setRoundDescriptor(String roundDescriptor) {
        this.roundDescriptor = roundDescriptor;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setEnds(int ends) {
        this.ends = ends;
    }

    public void setArrowsPerEnd(int arrowsPerEnd) {
        this.arrowsPerEnd = arrowsPerEnd;
    }

    public void setCurrentEnd(int currentEnd) {
        this.currentEnd = currentEnd;
    }

    public void setCurrentArrow(int curentArrow) {
        this.currentArrow = curentArrow;
    }

    public void setArrowCount(int arrowCount) {
        this.arrowCount = arrowCount;
    }

    public void setArrowData(String arrowData) {
        this.arrowData = arrowData;
    }
}


