package com.runvision.webcore.handler;

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
import java.util.List;
import java.util.Map;


public class DeleteRecord implements RequestHandler {

    @RequestMapping(method = RequestMethod.POST)
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        int id = Integer.parseInt(params.get("id"));
        //查找对应的图片ID
        List<User> mList= MyApplication.faceProvider.queryRecord("select * from tRecord where id=" + id);
        FileUtils.deleteTempter(mList.get(0).getTemplateImageID());
        FileUtils.deleteTempter(mList.get(0).getRecord().getSnapImageID());
        MyApplication.faceProvider.deleteRecord(id);
        response.setStatusCode(200);

    }
}
