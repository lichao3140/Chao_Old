package com.runvision.utils;


import android.util.Log;

import com.runvision.bean.AppData;
import com.runvision.bean.Type;
import com.runvision.core.Const;
import com.runvision.thread.SocketThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

public class SendData {

    /**
     * 默认设备登录
     */
    public static void login(SocketThread socketThread) {
        byte[] loginData = new byte[4 + 64 + 32];
        byte[] temp = null;

        temp = ConversionHelp.intToByte(Const.SOCKET_VERSION);
        Log.d("send", "login: " + temp.length);
        System.arraycopy(temp, 0, loginData, 0, temp.length);
        temp = SPUtil.getString(Const.KEY_VMSUSERNAME, getSerialNumber()).getBytes();
        System.arraycopy(temp, 0, loginData, 4, temp.length);
        temp = SPUtil.getString(Const.KEY_VMSPASSWORD, "123456").getBytes();
        System.arraycopy(temp, 0, loginData, 68, temp.length);
        socketThread.mSend(getSendData(Const.NMSG_CNT_DEVLOGIN, loginData));

    }


    /**
     * 心跳
     *
     * @param socketThread
     */
    public static void heartbeat(SocketThread socketThread) {
        byte[] heartbeatData = new byte[36];
        byte[] temp = null;

        temp = ConversionHelp.intToByte(SPUtil.getInt("UUID", 0));
        System.arraycopy(temp, 0, heartbeatData, 0, temp.length);
        temp = ConversionHelp.intToByte(0x0000001);
        System.arraycopy(temp, 0, heartbeatData, 4, temp.length);
        int time = (int) (System.currentTimeMillis() / 1000);
        byte[] bytes = new byte[4];
        for (int i = bytes.length - 1; i >= 0; i--) {
            bytes[i] = (byte) (time & 0xFF);
            time >>= 8;
        }
        System.arraycopy(bytes, 0, heartbeatData, 8, bytes.length);

        socketThread.mSend(getSendData(Const.NMSG_DCHNL_STATUS, heartbeatData));
    }

    /**
     * 比对数据发送
     *
     * @param socketThread
     * @param flag
     * @param type
     */
    public static void sendComperMsgInfo(SocketThread socketThread, boolean flag, char type) {
        byte[] cardByte = new byte[0];
        byte[] faceByte = new byte[0];
        if(type == Const.TYPE_ONEVSMORE) {
            if(AppData.getAppData().getCardBmp()!=null&&AppData.getAppData().getNFaceBmp()!=null) {
                cardByte = ConversionHelp.bitmapToByte(AppData.getAppData().getCardBmp());
                faceByte = ConversionHelp.bitmapToByte(AppData.getAppData().getNFaceBmp());
            }
        }
        else
        {
            if(AppData.getAppData().getCardBmp()!=null&&AppData.getAppData().getOneFaceBmp()!=null) {
                cardByte = ConversionHelp.bitmapToByte(AppData.getAppData().getCardBmp());
                faceByte = ConversionHelp.bitmapToByte(AppData.getAppData().getOneFaceBmp());
            }
        }

        byte[] sendComperMsgData = new byte[1306 + cardByte.length + faceByte.length];
        byte[] temp = null;
        temp = ConversionHelp.intToByte(SPUtil.getInt("UUID", 0));
        System.arraycopy(temp, 0, sendComperMsgData, 0, temp.length);
        temp = ConversionHelp.charToByte(type);
        System.arraycopy(temp, 0, sendComperMsgData, 4, temp.length);
        temp = SPUtil.getString(Const.KEY_VMSUSERNAME, getSerialNumber()).getBytes();
        System.arraycopy(temp, 0, sendComperMsgData, 5, temp.length);
        try {
            temp = SPUtil.getString("name", "").getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.arraycopy(temp, 0, sendComperMsgData, 69, temp.length);
        if (flag) {
            temp = ConversionHelp.charToByte((char) 0x01);
        } else {
            temp = ConversionHelp.charToByte((char) 0x02);
        }
        System.arraycopy(temp, 0, sendComperMsgData, 101, temp.length);



        if(type == Const.TYPE_ONEVSMORE) {
            Log.i("Gavin", "Score:" + AppData.getAppData().getCompareScore());
            temp = ConversionHelp.intToByte((int) (AppData.getAppData().getCompareScore() * 100) * 1000000);
            System.arraycopy(temp, 0, sendComperMsgData, 102, temp.length);
        }else
        {
            temp = ConversionHelp.intToByte((int) (AppData.getAppData().getoneCompareScore() * 100) * 1000000);
            System.arraycopy(temp, 0, sendComperMsgData, 102, temp.length);
        }

        temp = ConversionHelp.getTimeByte();
        System.arraycopy(temp, 0, sendComperMsgData, 106, temp.length);
        //人脸子模版类型
        if (type == Const.TYPE_ONEVSMORE) {
            System.out.println("Type:" + Type.valueOf(AppData.getAppData().getUser().getType()).getCode());
            temp = ConversionHelp.shortToByte((short) Type.valueOf(AppData.getAppData().getUser().getType()).getCode());
            Log.i("Gavin","Type:"+Type.valueOf(AppData.getAppData().getUser().getType()).getCode());
            System.arraycopy(temp, 0, sendComperMsgData, 110, temp.length);
        }
        //2个字节跳过
        //
        if (type == Const.TYPE_ONEVSMORE) {
            //temp = "123456789".getBytes();
            temp=AppData.getAppData().getUser().getCardNo().getBytes();
            System.arraycopy(temp, 0, sendComperMsgData, 112, temp.length);
        } else {
            temp = AppData.getAppData().getCardNo().getBytes();
            System.arraycopy(temp, 0, sendComperMsgData, 112, temp.length);
        }


        Log.e("SocketThread", "name:" + AppData.getAppData().getName());
        try {
            if (type == Const.TYPE_ONEVSMORE) {
                temp = AppData.getAppData().getUser().getName().getBytes("GBK");
            } else {
                temp = AppData.getAppData().getName().getBytes("GBK");
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("SocketThread", "name to gbk error");
        }
        System.arraycopy(temp, 0, sendComperMsgData, 144, temp.length);
        if (type == Const.TYPE_CARD) {
            if (AppData.getAppData().getSex().equals("男")) {
                temp = ConversionHelp.charToByte((char) 0x01);
            } else {
                temp = ConversionHelp.charToByte((char) 0x02);
            }
            System.arraycopy(temp, 0, sendComperMsgData, 208, temp.length);
        }
        else
        {
            if(AppData.getAppData().getUser().getSex().equals("男"))
            {
                temp = ConversionHelp.charToByte((char) 0x01);
            }
            else if(AppData.getAppData().getUser().getSex().equals("女"))
            {
                temp = ConversionHelp.charToByte((char) 0x02);
            }
            else
            {
                temp = ConversionHelp.charToByte((char) 0x00);
            }
            System.arraycopy(temp, 0, sendComperMsgData, 208, temp.length);
        }
        if (type == Const.TYPE_CARD) {
        }
        else {
            temp = ConversionHelp.charToByte((char) AppData.getAppData().getUser().getAge());
            System.arraycopy(temp, 0, sendComperMsgData, 209, temp.length);
        }
        //其他字段json
        JSONObject obj = new JSONObject();
        try {
            obj.put("addr", AppData.getAppData().getAddress());
            obj.put("birthday", AppData.getAppData().getBirthday());
            obj.put("nation", AppData.getAppData().getNation());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String json = obj.toString();
        try {
            temp = json.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.arraycopy(temp, 0, sendComperMsgData, 210, temp.length);
        temp = "".getBytes();
        System.arraycopy(temp, 0, sendComperMsgData, 1234, temp.length);

        temp = ConversionHelp.intToByte(cardByte.length);
        System.arraycopy(temp, 0, sendComperMsgData, 1298, temp.length);
        //添加card图片
        System.arraycopy(cardByte, 0, sendComperMsgData, 1302, cardByte.length);

        temp = ConversionHelp.intToByte(faceByte.length);
        System.arraycopy(temp, 0, sendComperMsgData, 1302 + cardByte.length, temp.length);
        //添加face图片
        System.arraycopy(faceByte, 0, sendComperMsgData, 1302 + cardByte.length + 4, faceByte.length);

        socketThread.mSend(getSendData(Const.NMSG_FACE_CMPRESULT, sendComperMsgData));

        AppData.getAppData().clean();

    }

    /**
     * 获取SN序列号
     *
     * @return
     */
    private static String getSerialNumber() {
        String serial = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");

            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");

        } catch (Exception e) {
            Log.i("error", e.getMessage());
        }
        return serial;
    }

    /**
     * 发送无数据请求
     *
     * @param msgType 发送消息的类型
     * @return
     */
    public static byte[] getSendData(int msgType) {
        char a = 0x7E;
        byte[] b = new byte[19];
        byte[] temp = null;
        temp = ConversionHelp.charToByte(a);
        System.arraycopy(temp, 0, b, 0, temp.length);
        temp = ConversionHelp.intToByte(ConversionHelp.SeqNumber());
        System.arraycopy(temp, 0, b, 1, temp.length);
        temp = ConversionHelp.shortToByte((short) 0x0000);
        System.arraycopy(temp, 0, b, 5, temp.length);
        temp = ConversionHelp.shortToByte((short) 0xF101);
        System.arraycopy(temp, 0, b, 7, temp.length);

        temp = ConversionHelp.intToByte(msgType);
        System.arraycopy(temp, 0, b, 9, temp.length);

        temp = ConversionHelp.shortToByte((short) 0x0001);
        System.arraycopy(temp, 0, b, 13, temp.length);
        temp = ConversionHelp.intToByte(0);
        System.arraycopy(temp, 0, b, 15, temp.length);

        byte[] resultData = null;

        resultData = new byte[b.length + 3];
        System.arraycopy(b, 0, resultData, 0, b.length);

        temp = ConversionHelp.shortToByte((short) ConversionHelp.getCheck(resultData,
                resultData.length - 4));
        System.arraycopy(temp, 0, resultData, b.length, temp.length);

        temp = ConversionHelp.charToByte((char) 0x7F);
        System.arraycopy(temp, 0, resultData, resultData.length - 1,
                temp.length);
        return resultData;
    }

    /**
     * 发送有参数的请求
     *
     * @param msgType
     * @param data
     * @return
     */
    public static byte[] getSendData(int msgType, byte[] data) {
        char a = 0x7E;
        byte[] b = new byte[19];
        byte[] temp = null;
        temp = ConversionHelp.charToByte(a);
        System.arraycopy(temp, 0, b, 0, temp.length);
        temp = ConversionHelp.intToByte(ConversionHelp.SeqNumber());
        System.arraycopy(temp, 0, b, 1, temp.length);
        temp = ConversionHelp.shortToByte((short) 0x0000);
        System.arraycopy(temp, 0, b, 5, temp.length);
        temp = ConversionHelp.shortToByte((short) 0xF101);
        System.arraycopy(temp, 0, b, 7, temp.length);

        temp = ConversionHelp.intToByte(msgType);
        System.arraycopy(temp, 0, b, 9, temp.length);

        temp = ConversionHelp.shortToByte((short) 0x0001);
        System.arraycopy(temp, 0, b, 13, temp.length);
        temp = ConversionHelp.intToByte(data.length);
        System.arraycopy(temp, 0, b, 15, temp.length);

        byte[] resultData = null;

        resultData = new byte[b.length + data.length + 3];
        System.arraycopy(b, 0, resultData, 0, b.length);

        System.arraycopy(data, 0, resultData, 19, data.length);


        temp = ConversionHelp.shortToByte((short) ConversionHelp.getCheck(resultData, resultData.length - 3));

        System.arraycopy(temp, 0, resultData, 19 + data.length, temp.length);

        temp = ConversionHelp.charToByte((char) 0x7F);
        System.arraycopy(temp, 0, resultData, resultData.length - 1,
                temp.length);

        return resultData;

    }

}
