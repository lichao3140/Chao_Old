package com.runvision.webcore.handler;

import com.runvision.bean.WebDataResultJson;
import com.runvision.core.Const;
import com.runvision.utils.JsonUtils;
import com.runvision.utils.SPUtil;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.Map;

public class UpdateAttendanceRules implements RequestHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);

        String atd_up_startime = params.get("atd_up_startime");
        SPUtil.putString(Const.ATD_UP_STARTIME,atd_up_startime);
        String atd_up_endtime = params.get("atd_up_endtime");
        SPUtil.putString(Const.ATD_UP_ENDTIME,atd_up_endtime);
        String atd_down_startime = params.get("atd_down_startime");
        SPUtil.putString(Const.ATD_DOWN_STARTIME,atd_down_startime);
        String atd_down_endtime = params.get("atd_down_endtime");
        SPUtil.putString(Const.ATD_DOWN_ENDTIME,atd_down_endtime);

        WebDataResultJson dataResultJson=new WebDataResultJson(200, "success", null);
        response.setStatusCode(200);
        response.setEntity(new StringEntity(JsonUtils.toJson(dataResultJson), "UTF-8"));
    }
}
