package com.runvision.db;

/**
 * Created by Administrator on 2018/7/23.
 */

public class Record {
    private String score;
    private String compertResult;
    private String snapImageID;
    private String comperType;

    public Record() {
    }

    public Record(String score, String compertResult, String snapImageID, String comperType) {
        this.score = score;
        this.compertResult = compertResult;
        this.snapImageID = snapImageID;
        this.comperType = comperType;
    }

    public String getCompertResult() {
        return compertResult;
    }

    public void setCompertResult(String compertResult) {
        this.compertResult = compertResult;
    }

    public String getSnapImageID() {
        return snapImageID;
    }

    public void setSnapImageID(String snapImageID) {
        this.snapImageID = snapImageID;
    }

    public String getComperType() {
        return comperType;
    }

    public void setComperType(String comperType) {
        this.comperType = comperType;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}
