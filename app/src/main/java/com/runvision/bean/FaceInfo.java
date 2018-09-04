package com.runvision.bean;

import com.arcsoft.facedetection.AFD_FSDKFace;

/**
 * Created by Administrator on 2018/8/3.
 */

public class FaceInfo {
    private byte[] des;

    private AFD_FSDKFace face;

    public FaceInfo(byte[] des, AFD_FSDKFace face) {
        this.des = des;
        this.face = face;
    }

    public byte[] getDes() {
        return des;
    }

    public void setDes(byte[] des) {
        this.des = des;
    }

    public AFD_FSDKFace getFace() {
        return face;
    }

    public void setFace(AFD_FSDKFace face) {
        this.face = face;
    }
}
