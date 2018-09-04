package com.runvision.webcore.handler;

import com.runvision.bean.WebDataResultJson;
import com.runvision.core.Const;
import com.runvision.utils.JsonUtils;
import com.runvision.utils.SPUtil;
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
import java.util.Map;

public class UpdateAppConfig implements RequestHandler {

    @RequestMapping(method = RequestMethod.POST)
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        float cardScore = Float.parseFloat(params.get("onenum"));//.parseInt(params.get("onenum"));
        SPUtil.addFloat(Const.KEY_CARDSCORE, cardScore);
        float oneVsMoreScore = Float.parseFloat(params.get("nnum"));
        SPUtil.addFloat(Const.KEY_ONEVSMORESCORE, oneVsMoreScore);
        int backHome = Integer.parseInt(params.get("rettimeout"));
        SPUtil.putInt(Const.KEY_BACKHOME, backHome);
        int closeDoor = Integer.parseInt(params.get("ondeltime"));
        SPUtil.putInt(Const.KEY_OPENDOOR, closeDoor);
        //int isOpenLive = Integer.parseInt(params.get("httest"));
       // SPUtil.putBoolean(Const.KEY_ISOPENLIVE, isOpenLive == 1 ? true : false);
       // int isOpenMusic = Integer.parseInt(params.get("voice"));
       // SPUtil.putBoolean(Const.KEY_ISOPENMUSIC, isOpenMusic == 1 ? true : false);

        int preservation_day = Integer.parseInt(params.get("preservation_day"));
        SPUtil.putInt(Const.KEY_PRESERVATION_DAY, preservation_day);

        String ip = params.get("vmsip");
        SPUtil.putString(Const.KEY_VMSIP, ip);
        int prot = Integer.parseInt(params.get("vmsport"));
        SPUtil.putInt(Const.KEY_VMSPROT, prot);
        String userName = params.get("vmsuser");
        SPUtil.putString(Const.KEY_VMSUSERNAME, userName);
        String password = params.get("vmspwd");
        SPUtil.putString(Const.KEY_VMSPASSWORD, password);

        Const.WEB_UPDATE=true;

        WebDataResultJson webDataResultJson = new WebDataResultJson(200, "success", null);
        response.setStatusCode(200);
        response.setEntity(new StringEntity(JsonUtils.toJson(webDataResultJson), "UTF-8"));
    }
}
