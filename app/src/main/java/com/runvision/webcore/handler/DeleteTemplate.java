package com.runvision.webcore.handler;

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
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.Map;

public class DeleteTemplate implements RequestHandler {

    @RequestMapping(method = RequestMethod.POST)
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        int id = Integer.parseInt(params.get("id"));

        User user = MyApplication.faceProvider.getUserByUserId(id);

        FileUtils.deleteTempter(user.getTemplateImageID(),  Const.TEMP_DIR);
        FileUtils.deleteFaceTempter(user.getTemplateImageID(),"Template");
        MyApplication.mList.remove(user.getTemplateImageID());

        MyApplication.faceProvider.deleteUserById(id);

        Const.DELETETEMPLATE=true;

        response.setStatusCode(200);

    }
}
