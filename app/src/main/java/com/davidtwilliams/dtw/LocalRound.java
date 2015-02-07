package com.davidtwilliams.dtw;

/**
 * Created by paulwilliams on 02/02/15.
 */
public class LocalRound {

    private int id;
    private String roundtype;
    private String date;
    private int ends;
    private int arrowsperend;

    public LocalRound(){}

    public LocalRound(String roundtype, String date, int ends, int arrowsperend) {
        super();
        this.roundtype = roundtype;
        this.date = date;
        this.ends = ends;
        this.arrowsperend = arrowsperend;
    }

    //getters & setters

    @Override
    public String toString() {
        return "Round [id=" + id + ", Round Type:" + roundtype + ", " + date +  "("+ends+"x"+arrowsperend+")]";
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getRoundtype() {
        return roundtype;
    }
    
    public int getEnds() {
        return ends;
    }
    
    public int getArrowsperend() {
        return arrowsperend;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRoundtype(String roundtype) {
        this.roundtype = roundtype;
    }

    public void setEnds(int ends) {
        this.ends = ends;
    }

    public void setArrowsperend(int arrowsperend) {
        this.arrowsperend = arrowsperend;
    }
}
