package com.runvision.thread;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.runvision.bean.Type;
import com.runvision.core.Const;
import com.runvision.core.LogToFile;
import com.runvision.utils.ConversionHelp;
import com.runvision.utils.FileUtils;
import com.runvision.utils.SPUtil;
import com.runvision.utils.SendData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2018/6/11.
 */

public class SocketThread extends Thread {
    private String ip;
    private int port;
    private String TAG = this.getClass().getSimpleName();
    private int timeout = 10000;
    private Socket socket = null;

    private DataOutputStream dos = null;
    private DataInputStream dis = null;
    private boolean isRun = true;
    private byte[] mData;
    private char top = 0x7E;
    private Handler mHandler;


    public SocketThread(String ip, int port, Handler mHandler) {
        this.ip = ip;
        this.port = port;
        this.mHandler = mHandler;
    }


    public void conn() {
        //可以通过缓存获取IP和端口
        try {
            Log.d(TAG, "socket连接中。。。");
            Log.e(TAG, "conn: " + ip + "," + port);
            socket = new Socket(ip, port);
            socket.setSoTimeout(timeout);
            Log.d(TAG, "socket连接成功");
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            SendData.login(this);
            isRun = true;
        } catch (UnknownHostException e) {
            LogToFile.i(TAG, "连接错误UnknownHostException 重新获取");
            Log.e(TAG, "连接错误UnknownHostException 重新获取");
            //如果输入的IP和端口不符合正常性  进入这里
            isRun = false;
            Message msg = new Message();
            msg.what = Const.SOCKET_TIMEOUT;
            msg.obj = "请检查IP或者端口的合法性";
            mHandler.sendMessage(msg);
        } catch (IOException e) {
            //如果连接的IP 不正确就会进入这里（连接超时）
            LogToFile.i(TAG, "连接服务器io错误");
            Log.e(TAG, "连接服务器io错误");
            isRun = false;
            Message msg = new Message();
            msg.what = Const.SOCKET_TIMEOUT;
            msg.obj = "网络连接超时";
            mHandler.sendMessage(msg);
        }
    }

    StringBuffer sb = new StringBuffer();

    @Override
    public void run() {
        conn();
        one:
        while (isRun) {
            try {
                //清空sb
                sb.delete(0, sb.length());
                int length = 0;
                two:
                do {
                    //读取dis里面的字节流长度
                    int ioSize = dis.available();
                    if (ioSize == 0) {
                        continue one;
                    }
                    byte[] data = new byte[ioSize];
                    length += dis.read(data);
                    sb.append(ConversionHelp.bytesToHexString(data));
                } while (dis.available() != 0);
                int msgType = ConversionHelp.bytesToInt2(ConversionHelp.decodeHex(sb.toString().substring(9 * 2, 13 * 2)), 0);
                switch (msgType) {
                    case Const.NMSG_CNT_DEVLOGIN:
                        mDataParsingToLogin(sb.toString());
                        break;
                    case Const.NMSG_DCHNL_SET:
                        updateConfig(sb.toString());
                        break;
                    case Const.NMSG_FLIB_ADD:
                        MyThread th = new MyThread(sb.toString());
                        th.start();
                        break;
                    default:
                        break;
                }
            } catch (IOException e) {

            }
        }
    }


    class MyThread extends Thread {
        private String str;

        public MyThread(String str) {
            this.str = str;
        }

        @Override
        public void run() {
            super.run();
            try {
                FileUtils.cleanDir("SocketImage");
                analysisTempterStr(str);
            } catch (UnsupportedEncodingException e) {
                System.out.println("--------------------");
            }
        }
    }

    /**
     * 解析下发过来的模版（主要解决多个模版粘包）;
     *
     * @param str
     */
    private void analysisTempterStr(String str) throws UnsupportedEncodingException {
        boolean flag = false;
        do {
            int length = ConversionHelp.bytesToInt2(ConversionHelp.decodeHex(str.substring(15 * 2, 19 * 2)), 0);
            String a = str.substring(0, (length + 22) * 2);
            analysisTempterData(a);
            if ((length + 22) * 2 < str.length()) {
                str = str.substring((length + 22) * 2);
                flag = true;
            } else {
                flag = false;
            }
        } while (flag);
        mHandler.sendEmptyMessage(Const.SOCKRT_SENDIMAGE);
    }


    /**
     * 解析一个模版对象详细信息
     *
     * @param str
     */
    private void analysisTempterData(String str) throws UnsupportedEncodingException {

        String name = new String(ConversionHelp.decodeHex(ConversionHelp.hexStringRemove0(str.substring(27 * 2, 91 * 2))), "gbk");
        int type = Integer.parseInt(str.substring(91 * 2, 93 * 2), 16);
        int sex = Integer.parseInt(str.substring(95 * 2, 96 * 2), 16);
        String sexStr = "";
        if (sex == 0) {
            sexStr = "未知";
        }
        if (sex == 1) {
            sexStr = "男";
        }
        if (sex == 2) {
            sexStr = "女";
        }
        int age = Integer.parseInt(str.substring(96 * 2, 97 * 2), 16);
        String workNoHex = ConversionHelp.hexStringRemove0(str.substring(433 * 2, 449 * 2));
        String workNo;
        if (workNoHex.equals("")) {
            workNo = "";
        } else {
            workNo = new String(ConversionHelp.decodeHex(workNoHex), "GBK");
        }
        String cardNoHex = ConversionHelp.hexStringRemove0(str.substring(489 * 2, 521 * 2));
        String cardNo;
        if (cardNoHex.equals("")) {
            cardNo = "";
        } else {
            cardNo = new String(ConversionHelp.decodeHex(ConversionHelp.hexStringRemove0(str.substring(489 * 2, 521 * 2))), "GBK");
        }
        //解析子模版部分
        str = str.substring(553 * 2, str.length() - 6);
        int mImageSize = ConversionHelp.bytesToInt2(ConversionHelp.decodeHex(str.substring(344 * 2, 348 * 2)), 0);
        String imageName = name + "&" + Type.getType(type).getDesc() + "&" + sexStr + "&" + age + "&" + workNo + "&" + cardNo;
        System.out.println("imageName=" + imageName);
        FileUtils.socketSaveImage(ConversionHelp.decodeHex(str.substring(348 * 2, (348 + mImageSize) * 2)), imageName);
    }


    private void updateConfig(String str) {
        try {
            analysisConfigData(str);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "json error");
        } catch (JSONException e) {
            Log.e(TAG, "json error");
        }
    }

    private void mDataParsingToLogin(String str) {
        Log.i(TAG, "mDataParsing: " + str.substring(13 * 2, 15 * 2));
        if ("0002".equals(str.substring(13 * 2, 15 * 2))) {

            String name = null;
            try {
                name = new String(ConversionHelp.decodeHex(ConversionHelp.hexStringRemove0(str.substring(87 * 2, 119 * 2))), "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            SPUtil.putString("name", name);

            Message msg = new Message();
            msg.what = Const.SOCKET_LOGIN;
            msg.obj = true;
            mHandler.sendMessage(msg);
        } else {
            Message msg = new Message();
            msg.what = Const.SOCKET_LOGIN;
            msg.obj = false;
            mHandler.sendMessage(msg);
        }

    }


    /**
     * 解析 配置信息
     *
     * @param str
     * @throws UnsupportedEncodingException
     * @throws JSONException
     */
    private void analysisConfigData(String str) throws UnsupportedEncodingException, JSONException {
        //设备通道标识ID
        int UUID = ConversionHelp.bytesToInt2(ConversionHelp.decodeHex(str.substring(19 * 2, 23 * 2)), 0);
        Log.e(TAG, "UUID:" + UUID);
        SPUtil.putInt("UUID", UUID);

        //设备通道标识编号
        String number = new String(ConversionHelp.decodeHex(ConversionHelp.hexStringRemove0(str.substring(23 * 2, 87 * 2))), "GBK");
        Log.e(TAG, "number:" + number);
        SPUtil.putString(Const.KEY_VMSUSERNAME, number);
        //设备通道名称
        String name = new String(ConversionHelp.decodeHex(ConversionHelp.hexStringRemove0(str.substring(87 * 2, 119 * 2))), "GBK");
        SPUtil.putString("name", name);
        Log.e(TAG, "name:" + name);
        //设备通道类型
        String type = str.substring(119 * 2, 121 * 2);

        Log.e(TAG, "Type:" + type);
        SPUtil.putString("type", type);
        //设备口令
        String password = new String(ConversionHelp.decodeHex(ConversionHelp.hexStringRemove0(str.substring(121 * 2, 153 * 2))), "GBK");
        Log.e(TAG, "password:" + password);
        SPUtil.putString(Const.KEY_VMSPASSWORD, password);
        //设备通道目录ID
        int CatalogID = ConversionHelp.bytesToInt2(ConversionHelp.decodeHex(str.substring(413 * 2, 417 * 2)), 0);
        Log.e(TAG, "CatalogID:" + CatalogID);
        SPUtil.putInt("CatalogID", CatalogID);
        //配置参数
        String configJson = new String(ConversionHelp.decodeHex(ConversionHelp.hexStringRemove0(str.substring(473 * 2, 2521 * 2))), "GBK");
        Log.e(TAG, "configJson:" + configJson);
        JSONObject config = new JSONObject(new JSONObject(configJson).getString("FVRYCFG"));
        String open_time = config.getString("open_time");
        SPUtil.putInt("open_time", Integer.parseInt(open_time));
        String score_type = config.getString("score_type");
        String self_value1 = config.getString("self_value1");

        if (score_type.equals("0")) {
            SPUtil.addFloat(Const.KEY_ONEVSMORESCORE, Const.ONEVSMORE_SCORE);
        } else {
            SPUtil.putInt(Const.KEY_ONEVSMORESCORE, Integer.parseInt(self_value1));
        }
        String timeout_time = config.getString("timeout_time");
//        SPUtil.putInt("comper_num", Integer.parseInt(timeout_time) * 2);
        String voice_type = config.getString("voice_type");
        SPUtil.putBoolean(Const.KEY_ISOPENMUSIC, Integer.parseInt(voice_type) == 1 ? true : false);
        String wait_time = config.getString("wait_time");
        Log.e(TAG, "open_time:" + open_time + ",score_type:" + score_type + ",self_value1:" + self_value1 + ",timeout_time:" + timeout_time + ",voice_type:" + voice_type + ",wait_time:" + wait_time);
        SPUtil.putInt(Const.KEY_BACKHOME, Integer.parseInt(wait_time));

    }

    /**
     * 发送数据
     *
     * @param data
     */

    public void mSend(byte[] data) {

        try {
            if (socket != null && socket.isConnected() && isRun == true) {
                Log.i("Gavin","Socket:0");
                dos.write(data);
            } else {
                LogToFile.e(TAG, "连接不存在，无法发送数据");
                Log.e(TAG, "连接不存在，无法发送数据");
                //如果服务器断开 或则socket连接断开就会进入这里
                //conn();
                mHandler.sendEmptyMessage(Const.SOCKET_DIDCONNECT);
                isRun = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "send error");
            e.printStackTrace();
            mHandler.sendEmptyMessage(Const.SOCKET_DIDCONNECT);
            isRun = false;
        } finally {
            Log.e(TAG, "发送完毕");
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        isRun = false;
        try {
            if (socket != null) {
                dos.close();
                dos = null;
                dis.close();
                dis = null;
                socket.close();
                socket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
