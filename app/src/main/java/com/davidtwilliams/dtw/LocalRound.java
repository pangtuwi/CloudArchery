package com.davidtwilliams.dtw;

import org.json.JSONObject;

/**
 * Created by paulwilliams on 02/02/15.
 */
public class LocalRound {

    private String UUID;                     //ID
    private JSONObject detailJSON;
    private JSONObject scoreJSON;

    public LocalRound() {
    }

    public LocalRound(String UUID, JSONObject scoreJSON, JSONObject detailJSON) {
        super();
        this.UUID = UUID;
        this.detailJSON = detailJSON;
        this.scoreJSON = scoreJSON;
     }


    @Override
    public String toString(){
        return detailJSON.toString();
    }


    //getters

    public String getUUID() {
        return UUID;
    }

    public JSONObject getDetailJSON() {
        return detailJSON;
    }

    public JSONObject getScoreJSON() {
        return scoreJSON;
    }


    //& setters

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public void setDetailJSON(JSONObject detailJSON) {
        this.detailJSON = detailJSON;
    }

    public void setScoreJSON(JSONObject scoreJSON) {
        this.scoreJSON = scoreJSON;
    }


}
    /*
    private int id;                     //ID in local SQLite DB
    private String cloudID;                //ID of linked Cloud (Firebase) round
    private int roundTypeID;            //ID of roundType in RoundType DB : reference
    private String roundType;           //RoundType Name i.e. "Portsmouth"   redundant data
    private String roundDescriptor;     //some more info, i.e. Friday night competition
    private String date;                //date of round
    private int numEnds;                //how many ends to be shot
    private int numArrowsPerEnd;        //how many arrows per end to be shot (i.e. 6)
    private int currentEnd;             //which end is the user busy with (if 1 and 2 are complete, this will be 3
    private int currentArrow;           //Current arrow user is busy with (if ends 1 and 2 are complete, this will be = 13 for a 6 arrow end
    private int arrowCount;             //How many arrows have been shot.  Normally same as currentArrow, but not if user is re-scoring
    private int roundTotal;             //running total score
    private boolean roundComplete;      //has the user finished scoring
    private long updateTimeDateStamp;    //when last updated
    private String arrowData;           //JSON array of scores (=-1 if not scored)

    public LocalRound(){}

    public LocalRound(String cloudID, int roundTypeID, String roundType, String roundDescriptor, String date,
                      int numEnds, int numArrowsPerEnd,
                      int currentEnd, int currentArrow, int arrowCount, int roundTotal, boolean roundComplete,
                      long updateTimeDateStamp, String arrowData) {
        super();
        //this.id = id; Used in Round (OnlineVersion)
        this.cloudID = cloudID;
        this.roundTypeID = roundTypeID;
        this.roundType = roundType;
        this.roundDescriptor = roundDescriptor;
        this.date = date;
        this.numEnds = numEnds;
        this.numArrowsPerEnd = numArrowsPerEnd;
        this.currentEnd = currentEnd;
        this.currentArrow = currentArrow;
        this.arrowCount = arrowCount;
        this.roundTotal = roundTotal;
        this.roundComplete = roundComplete;
        this.updateTimeDateStamp = updateTimeDateStamp;
        this.arrowData = arrowData;
    }


    @Override

    public String toString(){
        return "{\"id\":"+id + "," +
                " \"roundType\":\""+roundType + "\"," +
                " \"date\":\""+date + "\"," +
                " \"numEnds\":\""+numEnds + "\"," +
                " \"numArrowsPerEnd\":\""+numArrowsPerEnd + "\"," +
                " \"roundTotal\":\""+roundTotal + "\"," +
                " \"date\":\"" + date +"\"} ";
    }

    //getters

    public int getId() {
        return id;
    }

    public String getCloudID() {
        return cloudID;
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

    public int getNumEnds() {
        return numEnds;
    }

    public int getNumArrowsPerEnd() {
        return numArrowsPerEnd;
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

    public int getRoundTotal() {
        return roundTotal;
    }

    public boolean getRoundComplete() {
        return roundComplete;
    }

    public long getUpdateTimeDateStamp() {
        return updateTimeDateStamp;
    }

    public String getArrowData() {
        return arrowData;
    }

    //& setters

    public void setId(int id) {
        this.id = id;
    }

    public void setCloudID(String cloudID) {
        this.cloudID = cloudID;
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

    public void setNumEnds(int numEnds) {
        this.numEnds = numEnds;
    }

    public void setNumArrowsPerEnd(int arrowsPerEnd) {
        this.numArrowsPerEnd = numArrowsPerEnd;
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

    public void setRoundTotal(int roundTotal) {
        this.roundTotal = roundTotal;
    }

    public void setRoundComplete(boolean roundComplete) {
        this.roundComplete = roundComplete;
    }

    public void setArrowData(String arrowData) {
        this.arrowData = arrowData;
    }

    public void setUpdateTimeDateStamp(long updateTimeDateStamp) {
        this.updateTimeDateStamp = updateTimeDateStamp;
    }

    public void setUpdateTimeDateStampNow() {
        long unixTime = System.currentTimeMillis() / 1000L;
        this.updateTimeDateStamp = unixTime;
    }
    */



