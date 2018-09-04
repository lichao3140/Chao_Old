package com.runvision.webcore.handler;

import com.runvision.bean.AppData;
import com.runvision.core.Const;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.Map;

public class UpdateDeviceIP implements RequestHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
       // Context mContext= (Context) context;
        Map<String, String> params = HttpRequestParser.parseParams(request);
        String updatedeviceip = params.get("updatedeviceip");
         AppData.getAppData().setUpdatedeviceip(updatedeviceip);
         Const.UPDATE_IP=true;
    }



}
