package com.runvision.webcore.handler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.runvision.bean.WebDataResultJson;
import com.runvision.core.Const;
import com.runvision.db.User;
import com.runvision.g68a_sn.MyApplication;
import com.runvision.utils.FileUtils;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.RequestMethod;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取图片  Base64返回
 */
public class QueryRecordImage implements RequestHandler {

    @RequestMapping(method = RequestMethod.POST)
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        int id = Integer.parseInt(params.get("id"));
        int type = Integer.parseInt(params.get("type"));
        Log.i("HTTP", "QueryCardRecordImage:id= " + id);
        List<User> mList = MyApplication.faceProvider.queryRecord("select * from tRecord where id =" + id);
        if (mList.size() == 1) {
            String cardID = mList.get(0).getTemplateImageID();
            String snapImageID = mList.get(0).getRecord().getSnapImageID();
            String root = Environment.getExternalStorageDirectory() + "/FaceAndroid/";

            Bitmap snapBmp = BitmapFactory.decodeFile(snapImageID + ".jpg", FileUtils.getBitmapOption(2));
            Bitmap cardBmp = null;
            if (type == 0) {
                cardBmp = BitmapFactory.decodeFile(cardID + ".jpg", FileUtils.getBitmapOption(2));
            }
            if (type == 1) {
                cardBmp = BitmapFactory.decodeFile(root + Const.TEMP_DIR + "/" + cardID + ".jpg", FileUtils.getBitmapOption(2));
            }
            int code = 0;
            Map<String, String> map = new HashMap<>();
            try {

                String cardBase64 = FileUtils.bitmaptoString(cardBmp);
                String snapBase64 = FileUtils.bitmaptoString(snapBmp);
                map.put("libImage", cardBase64);
                map.put("capImage", snapBase64);
                code = 200;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("HTTP", "to Base64 error:" + e.getMessage());
                code = 404;
            } finally {
                WebDataResultJson dataResultJson = new WebDataResultJson<>(code, map);
                Gson gson = new Gson();
                String json = gson.toJson(dataResultJson);
                Log.i("HTTP", "QueryCardRecordImage:json= " + json);

                response.setStatusCode(code);
                response.setEntity(new StringEntity(json, "UTF-8"));
            }

        }
    }
}
