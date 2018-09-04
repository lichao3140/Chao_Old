package com.runvision.bean;

import android.graphics.Bitmap;

import com.runvision.db.User;

/**
 * Created by Administrator on 2018/5/31.
 */

public class AppData {


    private String CardNo;// 证件号
    private String Name;// 姓名
    private String sex;// 性别
    private String Nation;// 名族代码

    private String Birthday;// 生日
    private String Address;// 地址
    private float CompareScore;// 人证比对的比分
    private float OneCompareScore;//1:1比分

    private Bitmap faceBmp;
    private Bitmap cardBmp;
    private Bitmap onefacebmp;
    private Bitmap nfaceBmp=null;

/////////////////////////////////////////////////////////////////////////////////////

    private String facepicName;

    private User user;

    private String updatedeviceip;

///////////////////////////////////////////////////////////////////
    private int flag=0;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    private static AppData appData=new AppData();

    public static AppData getAppData(){
        return appData;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

///////////////////////////////////////////////////////////////////////////////////////

    public Bitmap getNFaceBmp() {
        return nfaceBmp;
    }

    public void SetNFaceBmp(Bitmap nfaceBmp) {
        this.nfaceBmp=nfaceBmp;
    }

    public Bitmap getOneFaceBmp() {
        return onefacebmp;
    }

    public void setOneFaceBmp(Bitmap onefacebmp) {
        this.onefacebmp = onefacebmp;
    }

///////////////////////////////////////////////////////////////////////////////////////
    public Bitmap getFaceBmp() {
        return faceBmp;
    }

    public void setFaceBmp(Bitmap faceBmp) {
        this.faceBmp = faceBmp;
    }

    public String getCardNo() {
        return CardNo;
    }

    public void setCardNo(String cardNo) {
        CardNo = cardNo;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNation() {
        return Nation;
    }

    public void setNation(String nation) {
        Nation = nation;
    }

    public String getBirthday() {
        return Birthday;
    }

    public void setBirthday(String birthday) {
        Birthday = birthday;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public float getCompareScore() {
        return CompareScore;
    }

    public void setCompareScore(float compareScore) {
        CompareScore = compareScore;
    }

    public float getoneCompareScore() {
        return OneCompareScore;
    }

    public void setoneCompareScore(float onecompareScore) {
        OneCompareScore = onecompareScore;
    }

    public Bitmap getCardBmp() {
        return cardBmp;
    }

    public void setCardBmp(Bitmap cardBmp) {
        this.cardBmp = cardBmp;
    }

    public void clean() {
        appData = new AppData();
    }

    public String getUpdatedeviceip() {
        return updatedeviceip;
    }

    public void setUpdatedeviceip(String updatedeviceip) {
        this.updatedeviceip = updatedeviceip;
    }


}


