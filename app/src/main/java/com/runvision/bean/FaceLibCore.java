package com.runvision.bean;

import android.graphics.Rect;
import android.util.Log;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.runvision.core.Const;

import java.util.List;

/**
 * Created by Administrator on 2018/8/3.
 */
public class FaceLibCore {
    private String TAG = this.getClass().getSimpleName();
    private boolean initLib = false;
    private byte[] a = new byte[1];
    private byte[] b = new byte[1];
    private AFD_FSDKEngine engine_AFD = null;
    private AFR_FSDKEngine engine_AFR = null;

    public int initLib() {
        //初始化FD的引擎
        engine_AFD = new AFD_FSDKEngine();
        AFD_FSDKError error = engine_AFD.AFD_FSDK_InitialFaceEngine(Const.APP_ID, Const.APP_KEY_FD, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 1);
        if (error.getCode() != AFD_FSDKError.MOK) {
            Log.e(TAG, "初始化init_AFD失败,错误码:" + error.getCode());
            engine_AFD = null;
            return error.getCode();
        }
        //FR的引擎
        engine_AFR = new AFR_FSDKEngine();
        AFR_FSDKError err = engine_AFR.AFR_FSDK_InitialEngine(Const.APP_ID, Const.APP_KEY_FR);
        if (err.getCode() != AFR_FSDKError.MOK) {
            Log.e(TAG, "初始化init_AFR失败,错误码" + error.getCode());
            engine_AFD = null;
            return err.getCode();
        }
        return 0;
    }


    /**
     * 人脸定位接口
     *
     * @param des    nv21的相机流  需要流的方向为0度
     * @param w      流的宽度
     * @param h      流的高度
     * @param result 返回人脸的坐标和其他信息
     * @return 0表示成功  其他表示失败
     */
    public int FaceDetection(byte[] des, int w, int h, List<AFD_FSDKFace> result) {
        int ret = -1;
        if (engine_AFD == null) {
            return ret;
        }
        synchronized (a) {
            AFD_FSDKError err = engine_AFD.AFD_FSDK_StillImageFaceDetection(des, w, h, AFD_FSDKEngine.CP_PAF_NV21, result);
            ret = err.getCode();
        }
        return ret;
    }

    /**
     * 特征提取
     *
     * @param des
     * @param w
     * @param h
     * @param rect
     * @param degree
     * @param face
     * @return
     */
    public int FaceFeature(byte[] des, int w, int h, Rect rect, int degree, AFR_FSDKFace face) {
        int ret = -1;
        if (engine_AFR == null) {
            return ret;
        }
        synchronized (b) {
            AFR_FSDKError error = engine_AFR.AFR_FSDK_ExtractFRFeature(des, w, h, AFR_FSDKEngine.CP_PAF_NV21, rect, degree, face);
            ret = error.getCode();
        }
        return ret;
    }

    /**
     * 分数比对
     *
     * @param face1
     * @param face2
     * @param score
     * @return
     */
    public int FacePairMatching(AFR_FSDKFace face1, AFR_FSDKFace face2, AFR_FSDKMatching score) {
        int ret = -1;
        if (engine_AFR == null) {
            return ret;
        }

        synchronized (b) {
            AFR_FSDKError error = engine_AFR.AFR_FSDK_FacePairMatching(face1, face2, score);
            ret = error.getCode();
        }
        return ret;
    }

    /**
     * 销毁人脸引擎
     */
    public void AFR_FSDK_UninitialEngine() {
        synchronized (a) {
            if (engine_AFD != null) {
                engine_AFD.AFD_FSDK_UninitialFaceEngine();
                engine_AFD = null;
            }
            if (engine_AFR != null) {
                engine_AFR.AFR_FSDK_UninitialEngine();
                engine_AFR = null;
            }

        }
    }


}
