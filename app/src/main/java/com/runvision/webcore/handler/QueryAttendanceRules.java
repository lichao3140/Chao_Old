package com.runvision.webcore.handler;

import android.util.Log;

import com.google.gson.Gson;
import com.runvision.bean.WebDataResultJson;
import com.runvision.core.Const;
import com.runvision.utils.SPUtil;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.RequestMethod;
import com.yanzhenjie.andserver.annotation.RequestMapping;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QueryAttendanceRules implements RequestHandler {
    @RequestMapping(method = RequestMethod.POST)
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> map = new HashMap<>();

        String atd_up_startime = SPUtil.getString(Const.ATD_UP_STARTIME,"");
        map.put(Const.ATD_UP_STARTIME,atd_up_startime);

        String atd_up_endtime = SPUtil.getString(Const.ATD_UP_ENDTIME, "");
        map.put(Const.ATD_UP_ENDTIME,atd_up_endtime);

        String atd_down_startime = SPUtil.getString(Const.ATD_DOWN_STARTIME,"");
        map.put(Const.ATD_DOWN_STARTIME,atd_down_startime);

        String atd_down_endtime = SPUtil.getString(Const.ATD_DOWN_ENDTIME, "");
        map.put(Const.ATD_DOWN_ENDTIME,atd_down_endtime);



        WebDataResultJson<Map<String, String>> web = new WebDataResultJson<>(200, map);

        Gson gson = new Gson();
        String json = gson.toJson(web);
        Log.i("HTTP", "QueryCardRecordCount:json= " + json);

        response.setEntity(new StringEntity(json, "UTF-8"));
        response.setStatusCode(200);
    }
}
