package com.davidtwilliams.dtw;

/**
 * Created by paulwilliams on 02/02/15.
 */
public class Round {

    private String id;
    private String roundType;
    private String date;
    private int ends;
    private int arrowsperend;

    public Round(){}

    public Round(String id, String roundType, String date, int ends, int arrowsperend) {
        super();
        this.id = id;
        this.roundType = roundType;
        this.date = date;
        this.ends = ends;
        this.arrowsperend = arrowsperend;
    }

    //getters & setters


    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getRoundType() {
        return roundType;
    }

    public int getEnds() {
        return ends;
    }

    public int getArrowsperend() {
        return arrowsperend;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRoundType(String roundType) {
        this.roundType = roundType;
    }

    public void setEnds(int ends) {
        this.ends = ends;
    }

    public void setArrowsperend(int arrowsperend) {
        this.arrowsperend = arrowsperend;
    }
}


