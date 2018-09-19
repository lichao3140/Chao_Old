package com.runvision.webcore.handler;

import com.runvision.bean.Sex;
import com.runvision.bean.Type;
import com.runvision.bean.WebDataResultJson;
import com.runvision.db.User;
import com.runvision.g68a_sn.MyApplication;
import com.runvision.utils.JsonUtils;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.Map;

public class UpdateTemplate implements RequestHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        String name = params.get("name");
        int type = Integer.parseInt(params.get("type"));
        int sex = Integer.parseInt(params.get("sex"));
        int age = Integer.parseInt(params.get("age"));
        String workNo = params.get("workNo");
        String cardNo = params.get("cardNo");
        String imageBase64 = params.get("img");
        int id = Integer.parseInt(params.get("id"));

        String templateName = MyApplication.faceProvider.getUserByUserId(id).getTemplateImageID();
        User user = new User(id, name, Type.getType(type).getDesc(), Sex.getSex(sex).getDesc(), age, workNo, cardNo, templateName);
        WebDataResultJson dataResultJson;
        int ret = MyApplication.faceProvider.updateUserById(user);
        if(ret==0){
            dataResultJson = new WebDataResultJson(200, "success", null);
        }else{
            dataResultJson = new WebDataResultJson(404, "error", null);
        }
        response.setStatusCode(200);
        response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));
    }

}
