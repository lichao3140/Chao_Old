package com.runvision.webcore.handler;

import com.runvision.core.Const;
import com.runvision.utils.JsonUtils;
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

public class QueryAppConfig implements RequestHandler {


    @RequestMapping(method = RequestMethod.POST)
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, Object> map = new HashMap<>();
        //1:N的阀值
        float oneVsMoreScore = SPUtil.getFloat(Const.KEY_ONEVSMORESCORE, Const.ONEVSMORE_SCORE);
        map.put(Const.KEY_ONEVSMORESCORE, oneVsMoreScore);
        //人证的阀值
        float cardScore = SPUtil.getFloat(Const.KEY_CARDSCORE, Const.ONEVSONE_SCORE);
        map.put(Const.KEY_CARDSCORE, cardScore);
        //是否开启活体检测
       // boolean isOpenLive = SPUtil.getBoolean(Const.KEY_ISOPENLIVE, Const.OPEN_LIVE);
       // map.put(Const.KEY_ISOPENLIVE, isOpenLive);
        //是否开启语音播报
       // boolean isOpenMusic = SPUtil.getBoolean(Const.KEY_ISOPENMUSIC, Const.OPEN_MUSIC);
       // map.put(Const.KEY_ISOPENMUSIC, isOpenMusic);
        //
        int backHome = SPUtil.getInt(Const.KEY_BACKHOME, Const.CLOSE_HOME_TIMEOUT);
        map.put(Const.KEY_BACKHOME, backHome);
        //开门延时时间
        int closeDoorTime = SPUtil.getInt(Const.KEY_OPENDOOR, Const.CLOSE_DOOR_TIME);
        map.put(Const.KEY_OPENDOOR, closeDoorTime);

        //保存数据天数
        int preservation_day = SPUtil.getInt(Const.KEY_PRESERVATION_DAY, 90);
        map.put(Const.KEY_PRESERVATION_DAY, preservation_day);

        //IP
        String ip = SPUtil.getString(Const.KEY_VMSIP, "");
        map.put(Const.KEY_VMSIP, ip);
        //PROT
        int prot = SPUtil.getInt(Const.KEY_VMSPROT, 0);
        map.put(Const.KEY_VMSPROT, prot);
        //USERNAME
        String userName = SPUtil.getString(Const.KEY_VMSUSERNAME, "");
        map.put(Const.KEY_VMSUSERNAME, userName);
        //PASSWORD
        String password = SPUtil.getString(Const.KEY_VMSPASSWORD, "");
        map.put(Const.KEY_VMSPASSWORD, password);

        response.setStatusCode(200);
        response.setEntity(new StringEntity(JsonUtils.toJson(map), "UTF-8"));

    }

}
