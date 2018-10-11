package com.runvision.thread;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.arcsoft.ageestimation.ASAE_FSDKAge;
import com.arcsoft.ageestimation.ASAE_FSDKEngine;
import com.arcsoft.ageestimation.ASAE_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.genderestimation.ASGE_FSDKEngine;
import com.arcsoft.genderestimation.ASGE_FSDKFace;
import com.arcsoft.genderestimation.ASGE_FSDKGender;
import com.runvision.bean.AppData;
import com.runvision.bean.FaceInfo;
import com.runvision.bean.FaceLibCore;
import com.runvision.bean.ImageStack;
import com.runvision.core.Const;
import com.runvision.g68a_sn.MainActivity;
import com.runvision.g68a_sn.MyApplication;
import com.runvision.myview.MyCameraSuf;
import com.runvision.utils.CameraHelp;
import com.runvision.utils.SPUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2018/5/29.
 */

public class FaceFramTask extends AsyncTask<Void, Rect, Void> {

    private String TAG = "FaceFramTask";
    private Handler handler;
    public boolean isRuning = true;
    private ImageStack imageStack;
    private MyCameraSuf mCameraView;
    List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>();
    List<ASAE_FSDKAge> result_Age = new ArrayList<ASAE_FSDKAge>();
    List<ASAE_FSDKFace> input_Age = new ArrayList<ASAE_FSDKFace>();
    List<ASGE_FSDKGender> result_Sex = new ArrayList<ASGE_FSDKGender>();
    List<ASGE_FSDKFace> input_Sex = new ArrayList<ASGE_FSDKFace>();

    byte[] des;
    private boolean flag=false;
    public static boolean faceflag=false;

    public void setRuning(boolean runing) {
        isRuning = runing;
    }

    public FaceFramTask(Handler handler, MyCameraSuf mCameraView) {
        this.handler = handler;
        this.mCameraView = mCameraView;
        imageStack = mCameraView.getImgStack();
    }

    public FaceFramTask(MyCameraSuf mCameraView) {
        flag=true;
        this.mCameraView = mCameraView;
        imageStack = mCameraView.getImgStack();
    }

    @Override
    protected Void doInBackground(Void... params) {
        while (isRuning) {
            des= CameraHelp.rotateCamera(imageStack.pullImageInfo().getData(), 640, 480, 90);
            //人脸
            MyApplication.mFaceLibCore.FaceDetection(des, 480, 640, result);
            if (result.size() != 0) { //有人脸
                /**
                //年龄:人脸框和角度
                input_Age.add(new ASAE_FSDKFace(new Rect(result.get(0).getRect().left,
                        result.get(0).getRect().top,
                        result.get(0).getRect().right,
                        result.get(0).getRect().bottom), ASAE_FSDKEngine.ASAE_FOC_0));
                MyApplication.mFaceLibCore.FaceAge(des, 480, 640, input_Age, result_Age);
                for (ASAE_FSDKAge age : result_Age) {
                    Log.i("lichao", "Age:" + age.getAge());
                }

                //性别
                input_Sex.add(new ASGE_FSDKFace(new Rect(result.get(0).getRect().left,
                        result.get(0).getRect().top,
                        result.get(0).getRect().right,
                        result.get(0).getRect().bottom), ASGE_FSDKEngine.ASGE_FOC_0));
                MyApplication.mFaceLibCore.FaceSex(des, 480, 640, input_Sex, result_Sex);
                for (ASGE_FSDKGender gender : result_Sex) {
                    switch(gender.getGender()) {
                        case ASGE_FSDKGender.FEMALE : Log.i("lichao", "gender: FEMALE" ); break;
                        case ASGE_FSDKGender.MALE: Log.i("lichao", "gender: MALE" ); break;
                        case ASGE_FSDKGender.UNKNOWN: Log.i("lichao", "gender: UNKNOWN" ); break;
                        default: break;
                    }
                }
                 **/

                publishProgress(result.get(0).getRect());
                faceflag = true;

                //活体
                List<com.arcsoft.liveness.FaceInfo> faceInfos = new ArrayList<>();
                com.arcsoft.liveness.FaceInfo faceInfo = new com.arcsoft.liveness.FaceInfo(result.get(0).getRect(), result.get(0).getDegree());
                faceInfos.add(faceInfo);
                // detect活体验证
                if (SPUtil.getBoolean(Const.KEY_ISOPENLIVE, Const.OPEN_LIVE) == true) {
                    if(!flag && MyApplication.mFaceLibCore.detect(des, 480, 640, faceInfos)) {
                        FaceInfo info = new FaceInfo(des, result.get(0));
                        Message msg = new Message();
                        msg.obj = info;
                        msg.what = Const.MSG_FACE;
                        handler.sendMessage(msg);
                    }
                } else {
                    if(!flag) {
                        FaceInfo info = new FaceInfo(des, result.get(0));
                        Message msg = new Message();
                        msg.obj = info;
                        msg.what = Const.MSG_FACE;
                        handler.sendMessage(msg);
                    }
                }
            } else {
                Log.i("lichao", "无人脸");
                faceflag=false;
                publishProgress(new Rect(0, 0, 0, 0));
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Rect... values) {
        super.onProgressUpdate(values);
        mCameraView.setFacePamaer(values[0]);
        if (mCameraView.getCamerType() == 1 && result != null && Const.is_regFace && flag) {
           int degree = result.get(0).getDegree();
          //  if (degree==1 || degree==12 || degree==6) {
            Log.i("Gavin","degree::"+degree);
               // if(degree==1)
              // {
                Bitmap map = CameraHelp.getBitMap(des);
                AppData.getAppData().setFaceBmp(CameraHelp.getFaceImgByInfraredJpg(values[0].left, values[0].top, values[0].right, values[0].bottom, map));
               // AppData.getAppData().setFaceBmp(map);
                AppData.getAppData().setFlag(Const.REG_FACE);
                Const.is_regFace=false;
           // }
        }

    }
}
