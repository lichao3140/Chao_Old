package com.runvision.core;

import android.os.Environment;

/**
 * Created by Administrator on 2018/6/5.
 */

public class Const {
    /**
     * 实际相机显示的大小
     */
    public static int Panel_width = 0;
    /**
     * 1：N当前失败次数
     */
    public static int ONE_VS_MORE_TIMEOUT_NUM = 0;
    /**
     * 1:N允许最大失败次数
     */
    public static int ONE_VS_MORE_TIMEOUT_MAXNUM = 5;

    /**
     * 人脸模版所有 在第一次加载到这里面来
     */
    // public static Map<String, byte[]> mTemplateMap = new HashMap<String, byte[]>();
    public static int CARD_WIDTH = 102;//身份证图片的宽度

    public static int CARD_HEIGTH = 126;//身份证图片的高度

    public static int COMPER_NUM = 10;

    public static float ONEVSMORE_SCORE = 0.70f;//1：N阈值

    public static float ONEVSONE_SCORE = 0.45f;//1：1阈值

    /**
     * 是否注册人脸标志位
     */
    public static boolean is_regFace = false;

    //注册抓拍人脸
    public static final int REG_FACE = 16;
    // 人证比对完成
    public final static int FLAG_CLEAN = 220;

    /**
     * 相机流的高
     */
    public static final int PRE_HEIGTH = 480;

    /**
     * 相机流的宽
     */
    public static final int PRE_WIDTH = 640;

    /**
     * G68A设备屏幕高
     */
    public static final int G68A_HEIGHT = 976;

    /**
     * G68A设备屏幕宽
     */
    public static final int G68A_WIDTH = 600;

    public static final String APP_ID = "J3Yscp63XC1M1ut6Fk6DguT9pEGgKYkXZVcfUQmsFDZC";
    public static final String APP_KEY_FD = "4wY3J6N2MQvjY1wAtg51niqvMheQ57mU2VdZ9e6gP2kB";
    public static final String APP_KEY_FR = "4wY3J6N2MQvjY1wAtg51nir3X6uX5LAVPFCXTXVeXRD7";

    // 活体检测
    public static final String FREESDKAPPID = "J3Yscp63XC1M1ut6Fk6DguTmdEaYzya4tb3teTZZZbAp";
    public static final String FDSDKKEY = "5VkbWAVASfvjVKGnnkdzu5n6xQgpjRKT2Acr8dzgsqzE";
    public static final String FTSDKKEY = "5VkbWAVASfvjVKGnnkdzu5myo1RheXt6ToyELTBqvvgu";
    public static final String LIVENESSAPPID = "J3Yscp63XC1M1ut6Fk6DguUGGqdGZocc2c8QK2GfnzTt";
    public static final String LIVENESSSDKKEY = "9LQgVXRmKTXtph3pbzibo9mCErzEC5Gg8yjX3JyU79FP";

    //年龄检测
    public static final String APP_ID_AGE = "J3Yscp63XC1M1ut6Fk6DguTeTqKPKJZRZ3BukyHnuE5E";
    public static final String APP_KEY_AGE = "DMco6iZeeVhdeXJKQUENi4zPw7wTu11zGmriBq5e3ang";

    //记录人脸定位算法是否成功
    public static boolean afdInit = false;

    public static final int PROMPT = 1000;
    public static final int COMPER_END = 1001;
    public static final int TEST_INFRA_RED = 1002;
    public static final int FLAG_SHOW_LOG = 1003;
    public static final int READ_CARD = 1004;
    public static final int UPDATE_UI = 1005;
    public static final int COMPER_FINIASH = 1006;
    public static final int MSG_FACE = 1007;

    //socket连接超时
    public static final int SOCKET_TIMEOUT = 10;
    public static final int SOCKET_DIDCONNECT = 11;

    //socket接收模版完成
    public static final int SOCKRT_SENDIMAGE = 12;

    // public static final int WEB_UPDATE = 13;

    //socket协议发送消息类型
    //通信协议版本号
    public final static int SOCKET_VERSION = 0x02000000;
    //设备登录
    public final static int NMSG_CNT_DEVLOGIN = 0x00000101;
    //心跳
    public final static int NMSG_DCHNL_STATUS = 0x10020502;
    //上传数据
    public final static int NMSG_FACE_CMPRESULT = 0x00020300;
    //修改设置参数
    public final static int NMSG_DCHNL_SET = 0x10020200;
    //收到模版
    public final static int NMSG_FLIB_ADD = 0x00010100;

    public final static char TYPE_CARD = 0x01;
    public final static char TYPE_ONEVSMORE = 0x02;

    public static final int SOCKET_LOGIN = 9;

    /**
     * 抓拍图片保存未知
     */
    public static final String SNAP_DIR = "Snap";
    /**
     * 身份证照片保存位置
     */
    public static final String CARD_DIR = "Card";

    public static final String TEMP_DIR = "FaceTemplate";

    /**
     * SharedPreferences 的 KEY
     */
    public static final String KEY_ONEVSMORESCORE = "oneVsMoreScore";
    public static final String KEY_CARDSCORE = "cardScore";
    public static final String KEY_ISOPENLIVE = "isOpenLive";
    public static final String KEY_ISOPENMUSIC = "isOpenMusic";
    public static final String KEY_BACKHOME = "backHome";
    public static final String KEY_OPENDOOR = "openDoor";
    public static final String KEY_VMSIP = "vmsIP";
    public static final String KEY_VMSPROT = "vmsProt";
    public static final String KEY_VMSUSERNAME = "vmsUserName";
    public static final String KEY_VMSPASSWORD = "vmsPassWord";

    public static final String ATD_UP_STARTIME = "atd_up_startime";
    public static final String ATD_UP_ENDTIME = "atd_up_endtime";
    public static final String ATD_DOWN_STARTIME = "atd_down_startime";
    public static final String ATD_DOWN_ENDTIME = "atd_down_endtime";

    public static final String KEY_PRESERVATION_DAY = "preservation_day";

    /**
     * 默认开启活体
     */
    public static final boolean OPEN_LIVE = true;

    /**
     * 默认开启语音
     */
    public static final boolean OPEN_MUSIC = true;

    /**
     * 30S无操作  返回待机页面  相机休眠
     */
    public static final int CLOSE_HOME_TIMEOUT = 30;

    public static final int CLOSE_DOOR_TIME = 1;

    public static Boolean BATCH_IMPORT_TEMPLATE = false;
    public static Boolean VMS_BATCH_IMPORT_TEMPLATE = false;
    public static Boolean VMS_TEMPLATE = false;

    public static int BATCH_FLAG = 0;

    public static boolean UPDATE_IP = false;

    //public static Boolean BATCH_IMPORT_TEMPLATE=false;

    /**
     * 存储人脸图片转Data数据模板路径
     */
    public static String Templatepath = Environment.getExternalStorageDirectory() + "/FaceAndroid/Template/";

    /**
     * 存储人脸图片模板路径
     */
    public static String ImagePath = Environment.getExternalStorageDirectory() + "/FaceAndroid/FaceTemplate/";

    public static boolean WEB_UPDATE = false;
    public static boolean DELETETEMPLATE = false;


}
