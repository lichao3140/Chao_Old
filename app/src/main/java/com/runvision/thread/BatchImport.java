package com.runvision.thread;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.runvision.core.Const;
import com.runvision.db.User;
import com.runvision.g68a_sn.MyApplication;
import com.runvision.utils.CameraHelp;
import com.runvision.utils.DateTimeUtils;
import com.runvision.utils.FileUtils;
import com.runvision.utils.IDUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import com.face.sv.FaceInfo;

/**
 * Created by Administrator on 2018/7/23.
 */

public class BatchImport implements Runnable {

    private List<File> mList;

    private Handler handler;

    private int messageID;

    private int num;


    public BatchImport(List<File> mList, Handler handler, int messageID) {
        this.mList = mList;
        this.handler = handler;
        this.messageID = messageID;
        this.num = mList.size();
    }

    @Override
    public void run() {
        for (int i = 1; i <= num; i++) {

            File file = mList.get(i - 1);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), FileUtils.getBitmapOption(2));
          //  byte[] mBGR = FileUtils.bitmapToBGR24(bitmap);
            int w = bitmap.getWidth() % 2 == 0 ? bitmap.getWidth() : bitmap.getWidth() - 1;
            int h = bitmap.getHeight() % 2 == 0 ? bitmap.getHeight() : bitmap.getHeight() - 1;
            byte[] nv21 = CameraHelp.getNV21(w, h, bitmap);
            String time = DateTimeUtils.parseDataTimeToFormatString(new Date());


            String userName = file.getName().substring(0, file.getName().indexOf("."));
            String[] strs = userName.split("&");
            if (strs.length != 6) {
                continue;
            }
            //保存图片
            //生成随机图片ID
            String imageID = IDUtils.genImageName();
            FileUtils.saveFile(bitmap, imageID, "FaceTemplate");
            User user = new User(strs[0], strs[1], strs[2], Integer.parseInt(strs[3]), strs[4], strs[5], imageID,DateTimeUtils.getTime());
            int id = MyApplication.faceProvider.addUserOutId(user);

            if (nv21 == null) {
                MyApplication.faceProvider.deleteUserById(id);
                //FileUtils.saveFile(bitmap, userName, "errorImage");
                FileUtils.deleteTempter(imageID, "FaceTemplate");
                sendMsg(i);
//                publishProgress();
                System.out.println("RBG==NULL");
                continue;
            }

            List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>();
            MyApplication.mFaceLibCore.FaceDetection(nv21, w, h, result);
            if (result.size() != 0) {
                AFR_FSDKFace face = new AFR_FSDKFace();
                int ret = MyApplication.mFaceLibCore.FaceFeature(nv21, w, h, result.get(0).getRect(), result.get(0).getDegree(), face);
                if (ret == 0) {
                    CameraHelp.saveFile(Const.Templatepath, imageID + ".data", face.getFeatureData());
                    CameraHelp.saveImgToDisk(Const.ImagePath, imageID + ".jpg", bitmap);
                    FileUtils.saveFile(bitmap, imageID, "FaceTemplate");
                    // Template t = new Template(mfilename, face);
                    MyApplication.mList.put(imageID, face.getFeatureData());
                    //   generateTemplate = true;
                    System.out.println("存入模板库");
                    Log.i("Gavin", "存入模板库:");

                    file.delete();
                    sendMsg(i);
                } else {
                    System.out.println("注册模版error");
                    Log.i("Gavin", "注册模版error:" );
                    MyApplication.faceProvider.deleteUserById(id);
                    // error++;
                    FileUtils.deleteTempter(imageID, "FaceTemplate");
                    //FileUtils.saveFile(bitmap, userName, "errorImage");
                    /// publishProgress();
                    sendMsg(i);
                    continue;
                }
            } else {
                MyApplication.faceProvider.deleteUserById(id);
                //error++;
                FileUtils.deleteTempter(imageID, "FaceTemplate");
                //FileUtils.saveFile(bitmap, userName, "errorImage");
                // publishProgress();
                System.out.println("人脸定位失败");
                Log.i("Gavin", "人脸定位失败:" );
                sendMsg(i);
                continue;
            }

        }

    }

    private void sendMsg(int i) {

        Message msg = new Message();
        msg.what = messageID;
        msg.obj = i;
        handler.sendMessage(msg);
    }
}
