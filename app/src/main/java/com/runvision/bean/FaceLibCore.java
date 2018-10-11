package com.runvision.bean;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

import com.arcsoft.ageestimation.ASAE_FSDKAge;
import com.arcsoft.ageestimation.ASAE_FSDKEngine;
import com.arcsoft.ageestimation.ASAE_FSDKError;
import com.arcsoft.ageestimation.ASAE_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.genderestimation.ASGE_FSDKEngine;
import com.arcsoft.genderestimation.ASGE_FSDKError;
import com.arcsoft.genderestimation.ASGE_FSDKFace;
import com.arcsoft.genderestimation.ASGE_FSDKGender;
import com.arcsoft.liveness.ErrorInfo;
import com.arcsoft.liveness.LivenessEngine;
import com.arcsoft.liveness.LivenessInfo;
import com.runvision.core.Const;
import com.runvision.utils.JniUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/8/3.
 */
public class FaceLibCore {
    private String TAG = this.getClass().getSimpleName();
    private boolean initLib = false;
    private byte[] a = new byte[1];
    private byte[] b = new byte[1];

    /**
     * 人脸检测
     */
    private AFD_FSDKEngine engine_AFD = null;

    /**
     * 人脸识别
     */
    private AFR_FSDKEngine engine_AFR = null;

    /**
     * 人脸追踪
     */
    private AFT_FSDKEngine engine_AFT = null;

    /**
     * 活体检测
     */
    private LivenessEngine engine_Live = null;

    /**
     * 年龄
     */
    private ASAE_FSDKEngine engine_Age = null;

    /**
     * 性别
     */
    private ASGE_FSDKEngine engine_Sex = null;

    public int initLib(Context context) {
        //初始化FD的引擎
        engine_AFD = new AFD_FSDKEngine();
        AFD_FSDKError error = engine_AFD.AFD_FSDK_InitialFaceEngine(Const.APP_ID, Const.APP_KEY_FD, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 1);
        if (error.getCode() != AFD_FSDKError.MOK) {
            Log.e(TAG, "初始化init_AFD失败,错误码:" + error.getCode());
            return error.getCode();
        }

        //初始化FR的引擎
        engine_AFR = new AFR_FSDKEngine();
        AFR_FSDKError err = engine_AFR.AFR_FSDK_InitialEngine(Const.APP_ID, Const.APP_KEY_FR);
        if (err.getCode() != AFR_FSDKError.MOK) {
            Log.e(TAG, "初始化init_AFR失败,错误码:" + err.getCode());
            return err.getCode();
        }

        //初始化FT的引擎
        engine_AFT = new AFT_FSDKEngine();
        AFT_FSDKError aft_fsdkError = engine_AFT.AFT_FSDK_InitialFaceEngine(Const.APP_ID, Const.APP_KEY_FT, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 5);
        if(aft_fsdkError.getCode() != AFT_FSDKError.MOK) {
            Log.e(TAG, "初始化init_AFT失败,错误码：" + aft_fsdkError.getCode());
            return aft_fsdkError.getCode();
        }

        //活体引擎激活
        engine_Live = new LivenessEngine();
        ErrorInfo activeCode = engine_Live.activeEngine(context, Const.LIVENESSAPPID, Const.LIVENESSSDKKEY);
        if (activeCode.getCode() == ErrorInfo.MOK || activeCode.getCode() == ErrorInfo.MERR_AL_BASE_ALREADY_ACTIVATED) {
            //初始化Live的引擎
            ErrorInfo errorInfo = engine_Live.initEngine(context, LivenessEngine.AL_DETECT_MODE_VIDEO);
            if(errorInfo.getCode() != ErrorInfo.MOK) {
                Log.e(TAG, "活体初始化失败,错误码：" + errorInfo.getCode());
                return (int) errorInfo.getCode();
            }
        } else {
            Log.e(TAG, "活体引擎激活失败,错误码：" + activeCode.getCode());
            return (int) activeCode.getCode();
        }

        /**
        //年龄引擎激活
        engine_Age = new ASAE_FSDKEngine();
        //初始化人脸检测引擎，使用时请替换申请的APPID和SDKKEY
        ASAE_FSDKError errAge = engine_Age.ASAE_FSDK_InitAgeEngine(Const.APP_ID, Const.APP_KEY_AGE);
        if (errAge.getCode() != ASAE_FSDKError.MOK) {
            Log.e(TAG, "初始化init_ASAE失败,错误码：" + errAge.getCode());
            return errAge.getCode();
        }

        engine_Sex = new ASGE_FSDKEngine();
        //初始化人脸检测引擎，使用时请替换申请的APPID和SDKKEY
        ASGE_FSDKError errSex = engine_Sex.ASGE_FSDK_InitgGenderEngine(Const.APP_ID,Const.APP_KEY_GEN);
        if (errSex.getCode() != ASGE_FSDKError.MOK) {
            Log.e(TAG, "初始化init_ASGE失败,错误码：" + errAge.getCode());
            return errSex.getCode();
        }
        **/

//        Log.e(TAG, "====JNI===" + JniUtil.getAPPID());
//        Log.e(TAG, "====JNI===" + JniUtil.getSDKKEY());
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
     * 活体检测
     * @param data  相机流
     * @param mWidth
     * @param mHeight
     * @param faceInfos
     * @return
     */
    public boolean detect(final byte[] data, int mWidth, int mHeight, List<com.arcsoft.liveness.FaceInfo> faceInfos) {
        //活体检测(目前只支持单人脸，且无论有无人脸都需调用)
        List<LivenessInfo> livenessInfos = new ArrayList<>();
        synchronized (b) {
            ErrorInfo livenessError = engine_Live.startLivenessDetect(data, mWidth, mHeight,
                    LivenessEngine.CP_PAF_NV21, faceInfos, livenessInfos);
            //Log.i("lichao", "startLiveness: errorcode " + livenessError.getCode());
            if (livenessError.getCode() == ErrorInfo.MOK) {
                if (livenessInfos.size() == 0) {
                    Log.e("lichao", "无人脸");
                    return false;
                }
                final int liveness = livenessInfos.get(0).getLiveness();
                //Log.i("lichao", "getLivenessScore: liveness " + liveness);
                if (liveness == LivenessInfo.NOT_LIVE) {
                    Log.e("lichao", "非活体");
                    return false;
                } else if (liveness == LivenessInfo.LIVE) {
                    Log.e("lichao", "活体");
                    return true;
                } else if (liveness == LivenessInfo.MORE_THAN_ONE_FACE) {
                    Log.e("lichao", "非单人脸信息");
                    return false;
                } else {
                    Log.e("lichao", "未知");
                    return false;
                }
            }
        }
        return false;
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
     * 年龄判断
     * @param des
     * @param w
     * @param h
     * @param input
     * @param result
     * @return
     */
    public int FaceAge(byte[] des, int w, int h, List<ASAE_FSDKFace> input, List<ASAE_FSDKAge> result) {
        int ret = -1;
        if (engine_Age == null) {
            return ret;
        }
        synchronized (b) {
            ASAE_FSDKError err = engine_Age.ASAE_FSDK_AgeEstimation_Image(des, w, h, ASAE_FSDKEngine.CP_PAF_NV21, input, result);
            ret = err.getCode();
        }
        return ret;
    }

    /**
     * 性别判断
     * @param des
     * @param w
     * @param h
     * @param input
     * @param result
     * @return
     */
    public int FaceSex(byte[] des, int w, int h, List<ASGE_FSDKFace> input, List<ASGE_FSDKGender> result) {
        int ret = -1;
        if (engine_Sex == null) {
            return ret;
        }
        synchronized (b) {
            ASGE_FSDKError err = engine_Sex.ASGE_FSDK_GenderEstimation_Image(des, w, h, ASGE_FSDKEngine.CP_PAF_NV21, input, result);
            ret =err.getCode();
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
     * 销毁所有引擎
     */
    public void UninitialAllEngine() {
        synchronized (a) {
            if (engine_AFD != null) {
                engine_AFD.AFD_FSDK_UninitialFaceEngine();
                engine_AFD = null;
            }
            if (engine_AFR != null) {
                engine_AFR.AFR_FSDK_UninitialEngine();
                engine_AFR = null;
            }
            if (engine_AFT != null) {
                engine_AFT.AFT_FSDK_UninitialFaceEngine();
                engine_AFT = null;
            }
            if (engine_Live != null) {
                engine_Live.unInitEngine();
                engine_Live = null;
            }
            if (engine_Age != null) {
                engine_Age.ASAE_FSDK_UninitAgeEngine();
                engine_Age = null;
            }
            if (engine_Sex != null) {
                engine_Sex.ASGE_FSDK_UninitGenderEngine();
                engine_Sex = null;
            }
        }
    }


}
