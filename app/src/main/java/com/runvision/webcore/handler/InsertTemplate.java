package com.runvision.webcore.handler;

import android.graphics.Bitmap;
import android.os.Environment;

import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.runvision.bean.Sex;
import com.runvision.bean.Type;
import com.runvision.bean.WebDataResultJson;
import com.runvision.core.Const;
import com.runvision.db.User;
import com.runvision.g68a_sn.MyApplication;
import com.runvision.utils.CameraHelp;
import com.runvision.utils.DateTimeUtils;
import com.runvision.utils.FileUtils;
import com.runvision.utils.IDUtils;
import com.runvision.utils.JsonUtils;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import com.face.sv.FaceInfo;

public class InsertTemplate implements RequestHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        String name = params.get("name");
        Const.BATCH_IMPORT_TEMPLATE=true;
        Const.BATCH_FLAG=1;
        int type = Integer.parseInt(params.get("type"));
        int sex = Integer.parseInt(params.get("sex"));
        int age = Integer.parseInt(params.get("age"));
        String workNo = params.get("workNo");
        String cardNo = params.get("cardNo");
        String imageBase64 = params.get("img");
        Bitmap bmp = FileUtils.stringtoBitmap(imageBase64);
        User user = new User(name, Type.getType(type).getDesc(), Sex.getSex(sex).getDesc(), age, workNo, cardNo, "", DateTimeUtils.getTime());
        String msg = insertTemplate(bmp, user);
        WebDataResultJson dataResultJson;
        if (msg.equals("success")) {
            dataResultJson = new WebDataResultJson(200, msg, null);
        } else {
            dataResultJson = new WebDataResultJson(404, msg, null);
        }
        response.setStatusCode(200);
        response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));
    }

    private String insertTemplate(Bitmap bmp, User user) {
        //转RGB
        //  byte[] mBGR = FileUtils.bitmapToBGR24(bmp);
        //生成随机图片ID

        String path = Environment.getExternalStorageDirectory() + "/FaceAndroid/Template/";
        String ImagePath = Environment.getExternalStorageDirectory() + "/FaceAndroid/FaceTemplate/";
        int w = bmp.getWidth() % 2 == 0 ? bmp.getWidth() : bmp.getWidth() - 1;
        int h = bmp.getHeight() % 2 == 0 ? bmp.getHeight() : bmp.getHeight() - 1;

        byte[] nv21 = CameraHelp.getNV21(w, h, bmp);

        String imageID = IDUtils.genImageName();
        user.setTemplateImageID(imageID);

        //插入数据库
        int id = MyApplication.faceProvider.addUserOutId(user);

        if (nv21 == null) {
            MyApplication.faceProvider.deleteUserById(id);
            return "图片转码错误";
        }

        List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>();
        MyApplication.mFaceLibCore.FaceDetection(nv21, w, h, result);
        if (result.size() != 0) {
            AFR_FSDKFace face = new AFR_FSDKFace();
            int ret = MyApplication.mFaceLibCore.FaceFeature(nv21, w, h, result.get(0).getRect(), result.get(0).getDegree(), face);
            if (ret == 0) {
                CameraHelp.saveFile(path, imageID + ".data", face.getFeatureData());
                CameraHelp.saveImgToDisk(ImagePath, imageID + ".jpg", bmp);
                FileUtils.saveFile(bmp, imageID, "FaceTemplate");
                // Template t = new Template(mfilename, face);
                MyApplication.mList.put(imageID, face.getFeatureData());
                //   generateTemplate = true;
                System.out.println("存入模板库");
                return "success";
            } else {
                MyApplication.faceProvider.deleteUserById(id);
                // generateTemplate=false;
                System.out.println("提取模版失败");
                return "注册人脸异常,错误码:" + ret;
            }
        } else {
            MyApplication.faceProvider.deleteUserById(id);
            //  generateTemplate=false;
            System.out.println("无人脸");
            return "检测不到人脸";
        }

    }

}
