package com.runvision.webcore.handler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.runvision.bean.WebDataResultJson;
import com.runvision.core.Const;
import com.runvision.db.User;
import com.runvision.g68a_sn.MyApplication;
import com.runvision.utils.FileUtils;
import com.runvision.utils.JsonUtils;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.RequestMethod;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.Map;

/**
 * 读取图片  Base64返回
 */
public class QueryTemplateImage implements RequestHandler {

    @RequestMapping(method = RequestMethod.POST)
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        int id = Integer.parseInt(params.get("id"));
        Log.i("HTTP", "QueryTemplateImage:id= " + id);
        User user = MyApplication.faceProvider.getUserByUserId(id);
        if (user != null) {
            String template = user.getTemplateImageID();
            String root = Environment.getExternalStorageDirectory() + "/FaceAndroid/";

            Bitmap tempBmp = BitmapFactory.decodeFile(root + Const.TEMP_DIR + "/" + template + ".jpg", FileUtils.getBitmapOption(2));

            int code = 0;
            try {
                String cardBase64 = FileUtils.bitmaptoString(tempBmp);
                user.setTemplateImageID(cardBase64);
                code = 200;
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("HTTP", "to Base64 error:" + e.getMessage());
                code = 404;
            } finally {

                WebDataResultJson dataResultJson = new WebDataResultJson<>(code, user);
                response.setStatusCode(code);
                response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));
            }

        }
    }
}
