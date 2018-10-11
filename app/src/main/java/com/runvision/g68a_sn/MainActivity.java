package com.runvision.g68a_sn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.runvision.bean.AppData;
import com.runvision.bean.FaceInfo;
import com.runvision.bean.ImageStack;
import com.runvision.broadcast.NetWorkStateReceiver;
import com.runvision.core.Const;
import com.runvision.db.Record;
import com.runvision.db.User;
import com.runvision.frament.DeviceSetFrament;
import com.runvision.gpio.GPIOHelper;
import com.runvision.gpio.SlecProtocol;
import com.runvision.myview.MyCameraSuf;
import com.runvision.thread.BatchImport;
import com.runvision.thread.FaceFramTask;
import com.runvision.thread.HeartBeatThread;
import com.runvision.thread.SocketThread;
import com.runvision.utils.CameraHelp;
import com.runvision.utils.ConversionHelp;
import com.runvision.utils.DateTimeUtils;
import com.runvision.utils.FileUtils;
import com.runvision.utils.IDUtils;
import com.runvision.utils.LogToFile;
import com.runvision.utils.SPUtil;
import com.runvision.utils.SendData;
import com.runvision.utils.TestDate;
import com.runvision.webcore.ServerManager;
import com.wits.serialport.SerialPortManager;
import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.module.idcard.IDCardReader;
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory;
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android_serialport_api.SerialPort;

public class MainActivity extends Activity implements NetWorkStateReceiver.INetStatusListener, View.OnClickListener {
    private static String TAG = MainActivity.class.getSimpleName();

    private Context mContext;
    //private ComperThread mComperThread;//1:n比对线程
    private MyRedThread mMyRedThread;//红外线程
    private UIThread uithread;//UI线程
    private SLecDeviceThread sLecDeviceThread;

    //////////////////////////////////////////////////视图控件
    public MyCameraSuf mCameraSurfView;
    private RelativeLayout home_layout;

    private View promptshow_xml;//提示框
    private TextView loadprompt;

    private View oneVsMoreView;  //1:N
    private ImageView oneVsMore_face, oneVsMore_temper;
    private TextView oneVsMore_userName, oneVsMore_userID, oneVsMore_userType;

    private View alert; //1:1
    private ImageView faceBmp_view, cardBmp_view, idcard_Bmp, isSuccessComper;
    private TextView card_name, card_sex, card_nation, name, year, month, day, addr, cardNumber, version;

    private ImageView home_set;

    private View pro_xml;//刷卡标记

    public int logshowflag = 0;

    private MediaPlayer mPlayer;//音频

    private boolean TipsFlag = false;

    private FaceFramTask faceDetectTask = null;

    private boolean bStop = false;

    private boolean oneVsMoreThreadStauts = false;
    private boolean isOpenOneVsMore = true;
    private boolean Infra_red = true;
    private ImageStack imageStack;

    public boolean comparisonEnd = false;
    private int timingnum = 0;

    private MyApplication application;
    // -----------------------------------读卡器参数-------------------------------------------
    private static final int VID = 1024; // IDR VID
    private static final int PID = 50010; // IDR PID
    private IDCardReader idCardReader = null;
    private boolean ReaderCardFlag = true;

    // ------------------------------这个按钮是设置或以开关的----------------------------------
    //这个按钮是设置或以开关的
    private NetWorkStateReceiver receiver;
    private TextView socket_status;
    //
    private SocketThread socketThread;
    private HeartBeatThread heartBeatThread;
    private TextView showHttpUrl;
    private ServerManager serverManager;
    private int socketErrorNum = 0;

    private Dialog dialog = null;
    private int templatenum = 0;
    private int template = 0;
    private Toast mToast;

    private Boolean SysTimeflag = true;

    List<User> mList;

    // private boolean vms_Import_template=false;
    /**
     * 消息响应
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Const.UPDATE_UI://更新UI
                    if (!isWirePluggedIn()) {
                        showHttpUrl.setText("");
                        //Log.e("lichao", "无网线");
                    }

                    if (Const.DELETETEMPLATE == true) {
                        isOpenOneVsMore = false;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Const.DELETETEMPLATE = false;
                                isOpenOneVsMore = true;
                            }
                        }, 2000);
                    }

                    /*更新VMS连接*/
                    if (Const.WEB_UPDATE == true) {
                        Const.WEB_UPDATE = false;
                        if (!SPUtil.getString(Const.KEY_VMSIP, "").equals("") && SPUtil.getInt(Const.KEY_VMSPROT, 0) != 0 && !SPUtil.getString(Const.KEY_VMSUSERNAME, "").equals("") && !SPUtil.getString(Const.KEY_VMSPASSWORD, "").equals("")) {
                            //开启socket线程
                            socketReconnect(SPUtil.getString(Const.KEY_VMSIP, ""), SPUtil.getInt(Const.KEY_VMSPROT, 0));
                        }
                    }

                    /*每天重启操作*/
                    DateFormat df = new SimpleDateFormat("HH:mm:ss");
                    if (df.format(new Date()).equals("02:00:00")) {
                        Log.i("zhuhuilong", "data" + df.format(new Date()));
                        rebootSU();
                    }

                    /*设置删除数据操作*/
                    String time1 = TestDate.SGetSysTime();
                    // String time2=TestDate.getSupportEndDayofMonth(new Date());
                    if ((df.format(new Date()).equals("00:00:00")) && SysTimeflag == true) {
                        // TestDate.getTime();
                        SysTimeflag = false;
                        Log.i("zhuhuilong", "data" + TestDate.getSupportBeginDayofMonth(new Date()));
                        Log.i("zhuhuilong", "data" + TestDate.getDateBefore(new Date(), SPUtil.getInt(Const.KEY_PRESERVATION_DAY, 90)));
                        Log.i("zhuhuilong", "data" + TestDate.getTime(time1));
                        Log.i("zhuhuilong", "data" + TestDate.timetodate(TestDate.getTime(time1)));

                        String time11 = TestDate.timetodate(TestDate.getTime(time1));
                        //String time22 = TestDate.timetodate(TestDate.getSupportBeginDayofMonth(new Date()));
                        String time22 = TestDate.getDateBefore(new Date(), SPUtil.getInt(Const.KEY_PRESERVATION_DAY, 90));

                        if (MyApplication.faceProvider.quaryUserTableRowCount("select count(id) from tUser") != 0) {
                            Log.i("zhuhuilong", "quaryUserTableRowCount:不为0");
                            mList = MyApplication.faceProvider.getAllPoints();
                            Log.i("zhuhuilong", "mList:" + mList.size());
                            Log.i("zhuhuilong", "mList:" + mList.toArray());
                            for (int i = 0; i < mList.size(); i++) {
                                Log.i("zhuhuilong", "mList.get(i).getTime():" + mList.get(i).getTime());
                                Log.i("zhuhuilong", "mList.get(i).getTime():" + mList.get(i).getId());
                                if (TimeCompare(time11, time22, TestDate.timetodate(String.valueOf(mList.get(i).getTime()))))//String.valueOf(mList.get(i).getTime())))
                                {
                                    List<User> mList1 = MyApplication.faceProvider.queryRecord("select * from tRecord where id=" + (mList.get(i).getId()));
                                    FileUtils.deleteTempter(mList1.get(0).getTemplateImageID());
                                    FileUtils.deleteTempter(mList1.get(0).getRecord().getSnapImageID());
                                    MyApplication.faceProvider.deleteRecord(mList.get(i).getId());

                                    Log.i("zhuhuilong", "mList.get(i).getTime():" + mList1.get(0).getTemplateImageID());
                                    Log.i("zhuhuilong", "mList.get(i).getTime():" + mList1.get(0).getRecord().getSnapImageID());
                                    Log.i("zhuhuilong", "true");

                                }
                            }
                        }
                    }

                    /*显示逻辑*/
                    if (promptshow_xml.getVisibility() == View.VISIBLE) {
                        oneVsMoreView.setVisibility(View.GONE);
                        pro_xml.setVisibility(View.GONE);
                        // home_layout.setVisibility(View.GONE);
                    }
                    if (alert.getVisibility() == View.VISIBLE) {
                        // AppData.getAppData().setCompareScore(0);
                        home_layout.setVisibility(View.GONE);
                        oneVsMoreView.setVisibility(View.GONE);
                        pro_xml.setVisibility(View.GONE);
                    }
                    if (home_layout.getVisibility() == View.VISIBLE) {
                        oneVsMoreView.setVisibility(View.GONE);
                        // promptshow_xml.setVisibility(View.GONE);
                        alert.setVisibility(View.GONE);
                        pro_xml.setVisibility(View.GONE);
                        Infra_red = false;
                    }
                    if (isOpenOneVsMore == false) {
                        mHandler.removeMessages(Const.COMPER_END);
                        mHandler.removeMessages(Const.MSG_FACE);
                    }
                    if (faceDetectTask != null) {
                        if (faceDetectTask.faceflag == true)//检测到有人脸
                        {
                            logshowflag = 0;
                            if (SerialPort.Fill_in_light == false) {
                                SerialPort.openLED();
                            }
                        }
                    }
                    if (SerialPort.Fill_in_light == true) {   //补光灯
                        timingnum++;
                        if (timingnum >= 100) {
                            Log.i("zhuhuilong", "Fill_in_light:" + SerialPort.Fill_in_light);
                            SerialPort.Fill_in_light = false;
                            timingnum = 0;
                        }
                    }

                    /*导入模板显示*/
                    if ((Const.BATCH_IMPORT_TEMPLATE == true) && (Const.BATCH_FLAG == 1)) {
                        if (faceDetectTask != null) {
                            faceDetectTask.isRuning = false;
                        }
                        isOpenOneVsMore = false;
                        Infra_red = false;
                        if (mMyRedThread != null) {
                            mMyRedThread.closeredThread();
                        }
                        // home_layout.setVisibility(View.VISIBLE);
                        templatenum = 0;
                        template++;
                        ReaderCardFlag = false;
                        Const.BATCH_IMPORT_TEMPLATE = false;
                        Const.BATCH_FLAG = 2;
                        showToast("正在导入模板,停止比对！");
                        // Toast.makeText(mContext, "正在导入模板", Toast.LENGTH_SHORT).show();
                    }
                    if ((template >= 5) || (Const.VMS_BATCH_IMPORT_TEMPLATE == true)) {
                        if (faceDetectTask != null) {
                            faceDetectTask.isRuning = false;
                        }
                        isOpenOneVsMore = false;
                        Infra_red = false;
                        if (mMyRedThread != null) {
                            mMyRedThread.closeredThread();
                        }
                        // home_layout.setVisibility(View.VISIBLE);
                        // templatenum=0;
                        // template++;
                        ReaderCardFlag = false;
                        oneVsMoreView.setVisibility(View.GONE);
                        alert.setVisibility(View.GONE);
                        home_layout.setVisibility(View.VISIBLE);
                        ShowPromptMessage("模板批量导入中！", 2);
                        cancelToast();
                    }

                    if ((Const.BATCH_IMPORT_TEMPLATE == false) && (Const.BATCH_FLAG == 2)) {
                        templatenum++;
                    }

                    if ((templatenum == 20) || (Const.VMS_TEMPLATE == true)) {
                        Const.VMS_TEMPLATE = false;
                        Log.i("Gavin_debug", "templatenum==20");
                        promptshow_xml.setVisibility(View.GONE);
                        cancelToast();
                        ReaderCardFlag = true;//1:1
                        isOpenOneVsMore = true;//1:n
                        if (faceDetectTask != null) {
                            faceDetectTask.isRuning = true;//人脸框
                        }
                        Infra_red = true;
                        if (mMyRedThread != null) {
                            mMyRedThread.startredThread();
                        }
                        template = 0;
                        // home_layout.setVisibility(View.GONE);
                    }

                    /*更新IP后的web重启*/
                    if (Const.UPDATE_IP == true) {
                        int returndate = DeviceSetFrament.updateSetting(AppData.getAppData().getUpdatedeviceip(), mContext);
                        if (returndate == 3) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    openHttpServer();
                                }

                            }, 3000);
                        }
                        Const.UPDATE_IP = false;
                    }
                    break;

                case Const.MSG_FACE://开启一比n处理
                    FaceInfo info = (FaceInfo) msg.obj;
                    openOneVsMoreThread(info);
                    break;
                case Const.READ_CARD://收到读卡器的信息
                    mHandler.removeMessages(Const.COMPER_FINIASH);
                    mHandler.removeMessages(Const.READ_CARD);
                    mHandler.removeMessages(Const.COMPER_END);
                    oneVsMoreView.setVisibility(View.GONE);
                    home_layout.setVisibility(View.GONE);
                    pro_xml.setVisibility(View.VISIBLE);
                    IDCardInfo Idinfo = (IDCardInfo) msg.obj;
                    toComperFace(Idinfo);
                    break;
                case Const.COMPER_END://1:n比对显示
                    showAlert();
                    break;
                case Const.COMPER_FINIASH://身份证比对完显示
                    mHandler.removeMessages(Const.COMPER_FINIASH);
                    mHandler.removeMessages(Const.COMPER_END);
                    oneVsMoreView.setVisibility(View.GONE);
                    pro_xml.setVisibility(View.GONE);
                    int count2 = (Integer) msg.obj;
                    if (count2 > 4) {
                        pro_xml.setVisibility(View.GONE);
                        showAlertDialog();
                        Message msg3 = obtainMessage();
                        msg3.what = Const.COMPER_FINIASH;
                        msg3.obj = count2 - 1;
                        sendMessageDelayed(msg3, 1000);
                    }
                    if (count2 > 0) {
                        Message msg3 = obtainMessage();
                        msg3.what = Const.COMPER_FINIASH;
                        msg3.obj = count2 - 1;
                        sendMessageDelayed(msg3, 1000);
                    }
                    if (count2 == 0) {
                        isOpenOneVsMore = true;
                    }
                    break;
                case Const.TEST_INFRA_RED://红外处理
                    int count1 = (Integer) msg.obj;
                    if (count1 > 0) {
                        Message msg3 = obtainMessage();
                        msg3.what = Const.TEST_INFRA_RED;
                        msg3.obj = count1 - 1;
                        sendMessageDelayed(msg3, 1000);
                    }
                    if (count1 == 0) {
                        home_layout.setVisibility(View.GONE);
                        // 开启人脸比对线程
                        stratThread();
                        Infra_red = true;
                        bStop = false;

                        if (uithread == null) {
                            uithread = new UIThread();
                            uithread.start();
                        }
                    }
                    break;
                case Const.FLAG_SHOW_LOG://待机处理
                    int count4 = (Integer) msg.obj;
                    oneVsMoreView.setVisibility(View.GONE);
                    promptshow_xml.setVisibility(View.GONE);
                    alert.setVisibility(View.GONE);
                    pro_xml.setVisibility(View.GONE);
                    Infra_red = false;
                    if (count4 > 0) {
                        Message msgb = obtainMessage();
                        msgb.what = Const.FLAG_SHOW_LOG;
                        msgb.obj = count4 - 1;
                        sendMessageDelayed(msgb, 1000);
                    }
                    if (count4 == 0) {
                        home_layout.setVisibility(View.VISIBLE);
                        mCameraSurfView.releaseCamera();
                    }
                    break;
                case Const.SOCKET_LOGIN: /*socket设备登陆*/
                    boolean isSuccess = (boolean) msg.obj;
                    if (isSuccess) {
                        Toast.makeText(mContext, "socket登录成功", Toast.LENGTH_SHORT).show();
                        LogToFile.i("MainActivity", "socket登录成功");
                        socket_status.setBackgroundResource(R.drawable.socket_true);
                        //开启心跳
                        if (heartBeatThread != null) {
                            heartBeatThread.HeartBeatThread_flag = false;
                            heartBeatThread = null;
                        }
                        heartBeatThread = new HeartBeatThread(socketThread);
                        heartBeatThread.start();
                    } else {
                        socket_status.setBackgroundResource(R.drawable.socket_false);
                        LogToFile.i("MainActivity", "socket登录失败");
                        Toast.makeText(mContext, "socket登录失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Const.SOCKET_TIMEOUT:/*socket连接超时*/
                    socket_status.setBackgroundResource(R.drawable.socket_false);
                    String prompt = (String) msg.obj;
                    LogToFile.i("MainActivity", prompt);
                    Toast.makeText(mContext, prompt, Toast.LENGTH_SHORT).show();
                    break;
                case Const.SOCKET_DIDCONNECT:/*socket断开连接*/
                    socket_status.setBackgroundResource(R.drawable.socket_false);
                    closeSocket();
                    break;
                case Const.SOCKRT_SENDIMAGE:/*VMS批量导入操作*/
                    batchImport();
                    break;
                case 101:/*VMS批量导入结束操作*/
                    int success1 = (int) msg.obj;
                    bacthOk1 = success1;
                    double a = (double) success1 / (double) dataList1.size();
                    int b = (int) (a * 100);
                    // progesssValue1.setText(success1 + "/" + dataList1.size());
                    //  progesss1.setProgress(b);
                    if (bacthOk1 + bacthOk2 + bacthOk3 == mSum) {
                        // batchDialog.dismiss();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Const.VMS_TEMPLATE = true;
                                Const.VMS_BATCH_IMPORT_TEMPLATE = false;
                            }
                        }, 2000);

                    }
                    break;
                case 102:/*VMS批量导入结束操作*/
                    int success2 = (int) msg.obj;
                    bacthOk2 = success2;
                    double a2 = (double) success2 / (double) dataList2.size();
                    int b2 = (int) (a2 * 100);
                    // progesssValue2.setText(success2 + "/" + dataList2.size());
                    // progesss2.setProgress(b2);
                    if (bacthOk1 + bacthOk2 + bacthOk3 == mSum) {
                        // batchDialog.dismiss();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Const.VMS_TEMPLATE = true;
                                Const.VMS_BATCH_IMPORT_TEMPLATE = false;
                            }
                        }, 2000);

                    }
                    break;
                case 103:/*VMS批量导入结束操作*/
                    int success3 = (int) msg.obj;
                    bacthOk3 = success3;
                    double a3 = (double) success3 / (double) dataList3.size();
                    int b3 = (int) (a3 * 100);
                    // progesssValue3.setText(success3 + "/" + dataList3.size());
                    //  progesss3.setProgress(b3);
                    if (bacthOk1 + bacthOk2 + bacthOk3 == mSum) {
                        // batchDialog.dismiss();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Const.VMS_TEMPLATE = true;
                                Const.VMS_BATCH_IMPORT_TEMPLATE = false;
                            }
                        }, 2000);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * ACTIVITY周期
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 全屏代码
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        hideBottomUIMenu();
        initView();
        initRelay();
        mContext = this;

        application = (MyApplication) getApplication();
        application.init();
        application.addActivity(this);

        openNetStatusReceiver();
        openSocket();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideBottomUIMenu();
        // stratThread();
        // bStop = false;
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, usbDeviceStateFilter);
        startIDCardReader();

        if (uithread == null) {
            uithread = new UIThread();
            uithread.start();
        }

        if (mMyRedThread == null) {
            mMyRedThread = new MyRedThread();  //红外
            mMyRedThread.start();
        }
        mMyRedThread.startredThread();
        isOpenOneVsMore = true;
        // Log.i("Gavin","mList:"+MyApplication.mList.size());
    }

    @Override
    protected void onPause() {
        super.onPause();
        //关闭相机线程
        Infra_red = false;
        mCameraSurfView.releaseCamera();
        //关闭红外
        mMyRedThread.closeredThread();
        sLecDeviceThread.interrupt();
        if (mMyRedThread != null) {
            mMyRedThread.interrupt();
            mMyRedThread = null;
        }
        //关闭人脸框线程
        if (faceDetectTask != null) {
            faceDetectTask.setRuning(false);
            faceDetectTask.cancel(false);
            faceDetectTask = null;
        }
        //关闭未播报完语音
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.release();
                mPlayer = null;
            }
        }
        isOpenOneVsMore = false;
        bStop = true;
        try {
            idCardReader.close(0);
        } catch (IDCardReaderException e) {
            Log.i(TAG, "关闭失败");
        }
        IDCardReaderFactory.destroy(idCardReader);
        unregisterReceiver(mUsbReceiver);
    }

    @Override
    protected void onDestroy() {
        MyApplication.mFaceLibCore.UninitialAllEngine();
        mSerialPortManager.closeSerialPort4();
        super.onDestroy();
    }

    /**
     * 初始化视图控件
     */
    private void initView() {
        mCameraSurfView = (MyCameraSuf) findViewById(R.id.myCameraView);
        imageStack = mCameraSurfView.getImgStack();
        home_layout = (RelativeLayout) findViewById(R.id.home_layout);//待机界面

        // 提示框
        promptshow_xml = findViewById(R.id.promptshow_xml);
        loadprompt = (TextView) promptshow_xml.findViewById(R.id.loadprompt);

        //1:N
        oneVsMoreView = findViewById(R.id.onevsmore);
        oneVsMore_face = oneVsMoreView.findViewById(R.id.onevsmore_face);
        oneVsMore_temper = oneVsMoreView.findViewById(R.id.onevsmore_temper);
        oneVsMore_userName = oneVsMoreView.findViewById(R.id.onevsmore_userName);
        oneVsMore_userID = oneVsMoreView.findViewById(R.id.onevsmore_userID);
        oneVsMore_userType = oneVsMoreView.findViewById(R.id.onevsmore_userType);

        //1:1
        alert = findViewById(R.id.alert_xml);
        faceBmp_view = (ImageView) alert.findViewById(R.id.comperFacebm);
        cardBmp_view = (ImageView) alert.findViewById(R.id.comperCardbm);
        idcard_Bmp = (ImageView) alert.findViewById(R.id.cardImage);
        card_name = (TextView) alert.findViewById(R.id.name_1);
        name = (TextView) alert.findViewById(R.id.userName);
        card_sex = (TextView) alert.findViewById(R.id.sex);
        card_nation = (TextView) alert.findViewById(R.id.nation);
        year = (TextView) alert.findViewById(R.id.year);
        day = (TextView) alert.findViewById(R.id.day);
        month = (TextView) alert.findViewById(R.id.month);
        addr = (TextView) alert.findViewById(R.id.addr);
        cardNumber = (TextView) alert.findViewById(R.id.cardNumber);
        isSuccessComper = (ImageView) alert.findViewById(R.id.isSuccessComper);

        //刷卡标记
        pro_xml = findViewById(R.id.pro);

        socket_status = findViewById(R.id.socket_status);
        showHttpUrl = findViewById(R.id.showHttpUrl);

        home_set = (ImageView) findViewById(R.id.home_set);
        home_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmPsdDialog();
//                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }

    private void initRelay() {
        mSerialPortManager = new SerialPortManager();
        sLecDeviceThread = new SLecDeviceThread();
        sLecDeviceThread.start();
    }

    /**
     * 开启画人脸框线程
     */
    private void stratThread() {
        if (faceDetectTask != null) {
            faceDetectTask.setRuning(false);
            faceDetectTask = null;
        }
        faceDetectTask = new FaceFramTask(mHandler, mCameraSurfView);
        faceDetectTask.setRuning(true);
        faceDetectTask.execute();
    }

    /**
     * 开启一个1：N的线程
     */
    private void openOneVsMoreThread(FaceInfo info) {
        if (!oneVsMoreThreadStauts && isOpenOneVsMore && Infra_red) {
            oneVsMoreThreadStauts = true;
            OneVsMoreThread thread = new OneVsMoreThread(info);
            thread.start();
        }
    }

    /**
     * 身份证读取
     */
    private void toComperFace(final IDCardInfo idCardInfo) {
        if (idCardInfo.getPhotolength() > 0) {
            byte[] buf = new byte[WLTService.imgLength];
            if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                final Bitmap cardBmp = IDPhotoHelper.Bgr2Bitmap(buf);
                if (cardBmp != null) {
                    synchronized (this) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                long start = System.currentTimeMillis();
                                faceComperFrame(cardBmp);
                                // System.out.println("人证比对时间:" + (System.currentTimeMillis() - start));
                                AppData.getAppData().setName(idCardInfo.getName());
                                AppData.getAppData().setSex(idCardInfo.getSex());
                                AppData.getAppData().setNation(idCardInfo.getNation());
                                AppData.getAppData().setBirthday(idCardInfo.getBirth());
                                AppData.getAppData().setAddress(idCardInfo.getAddress());
                                AppData.getAppData().setCardNo(idCardInfo.getId());
                                AppData.getAppData().setCardBmp(cardBmp);
                                Message msg = new Message();
                                msg.obj = 5;
                                msg.what = Const.COMPER_FINIASH;
                                mHandler.sendMessage(msg);
                            }
                        }).start();
                    }
                } else {
                    Log.i(TAG, "读卡器解码得到的图片为空");
                }
            } else {
                Log.i(TAG, "图片解码 error");
                showToast("身份证图片解码失败");
            }
        } else {
            Log.i(TAG, "图片数据长度为0");
            showToast("图片数据长度为0" + idCardInfo.getName());
        }
    }


    /*1：1比对操作*/
    public void faceComperFrame(Bitmap bmp) {
        //提取人脸
        List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>();
        byte[] des = CameraHelp.rotateCamera(imageStack.pullImageInfo().getData(), 640, 480, 270);
        MyApplication.mFaceLibCore.FaceDetection(des, 480, 640, result);
        if (result.size() == 0) {
            return;
        }

        AppData.getAppData().setOneFaceBmp(CameraHelp.getFaceImgByInfraredJpg(result.get(0).getRect().left, result.get(0).getRect().top, result.get(0).getRect().right, result.get(0).getRect().bottom, CameraHelp.getBitMap(des)));
        AFR_FSDKFace face = new AFR_FSDKFace();
        int ret = MyApplication.mFaceLibCore.FaceFeature(des, 480, 640, result.get(0).getRect(), result.get(0).getDegree(), face);
        if (ret != 0) {
            return;
        }
        //提取身份证
        int w = bmp.getWidth() % 2 == 0 ? bmp.getWidth() : bmp.getWidth() - 1;
        int h = bmp.getHeight() % 2 == 0 ? bmp.getHeight() : bmp.getHeight() - 1;
        byte[] cardDes = CameraHelp.getNV21(w, h, bmp);
        List<AFD_FSDKFace> result_card = new ArrayList<AFD_FSDKFace>();
        MyApplication.mFaceLibCore.FaceDetection(cardDes, w, h, result_card);
        if (result_card.size() == 0) {
            return;
        }
        AFR_FSDKFace card = new AFR_FSDKFace();
        ret = MyApplication.mFaceLibCore.FaceFeature(cardDes, w, h, result_card.get(0).getRect(), result_card.get(0).getDegree(), card);
        if (ret != 0) {
            return;
        }

        AFR_FSDKMatching score = new AFR_FSDKMatching();
        ret = MyApplication.mFaceLibCore.FacePairMatching(face, card, score);
        if (ret != 0) {
            return;
        }
        // System.out.println("人证分数:" + score.getScore());
        AppData.getAppData().setoneCompareScore(score.getScore());
        //  Log.i("Gavin","oneCompareScore:"+AppData.getAppData().getoneCompareScore());
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                Log.e(TAG, "拔出usb了");
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    Log.e(TAG, "设备的ProductId值为：" + device.getProductId());
                    Log.e(TAG, "设备的VendorId值为：" + device.getVendorId());
                    if (device.getProductId() == PID && device.getVendorId() == VID) {
                        bStop = true;
                        try {
                            idCardReader.close(0);
                        } catch (IDCardReaderException e) {
                            // TODO Auto-generated catch block
                            Log.i(TAG, "关闭失败");
                        }
                        IDCardReaderFactory.destroy(idCardReader);
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.e(TAG, "插入usb了");
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device.getProductId() == PID && device.getVendorId() == VID) {
                    // 读卡器
                    startIDCardReader();
                }
            }
        }
    };

    /**
     * 读卡器初始化
     */
    private void startIDCardReader() {
        LogHelper.setLevel(Log.ASSERT);
        Map idrparams = new HashMap();
        idrparams.put(ParameterHelper.PARAM_KEY_VID, VID);
        idrparams.put(ParameterHelper.PARAM_KEY_PID, PID);
        idCardReader = IDCardReaderFactory.createIDCardReader(this,
                TransportType.USB, idrparams);
        readCard();

    }


    private void readCard() {
        try {
            idCardReader.open(0);
            bStop = false;
            Log.i(TAG, "设备连接成功");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!bStop) {
                        long begin = System.currentTimeMillis();
                        IDCardInfo idCardInfo = new IDCardInfo();
                        boolean ret = false;
                        try {
                            idCardReader.findCard(0);
                            idCardReader.selectCard(0);
                        } catch (IDCardReaderException e) {
                            continue;
                        }
                        if (ReaderCardFlag == true) {
                            try {
                                ret = idCardReader.readCard(0, 0, idCardInfo);
                            } catch (IDCardReaderException e) {
                                Log.i(TAG, "读卡失败，错误信息：" + e.getMessage());
                            }
                            if (ret) {
                                Const.ONE_VS_MORE_TIMEOUT_NUM = 0;
                                isOpenOneVsMore = false;
                                ReaderCardFlag = false;
                                final long nTickUsed = (System.currentTimeMillis() - begin);
                                Log.i(TAG, "success>>>" + nTickUsed + ",name:" + idCardInfo.getName() + "," + idCardInfo.getValidityTime() + "，" + idCardInfo.getDepart());
                                Message msg = new Message();
                                msg.what = Const.READ_CARD;
                                msg.obj = idCardInfo;
                                mHandler.sendMessage(msg);

                            }
                        }
                    }
                }
            }).start();
        } catch (IDCardReaderException e) {
            Log.i(TAG, "连接设备失败");
            Log.i(TAG, "开始读卡失败，错误码：" + e.getErrorCode() + "\n错误信息："
                    + e.getMessage() + "\n内部代码="
                    + e.getInternalErrorCode());
            showToast("连接读卡器失败:" + e.getMessage());
        }
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    @SuppressLint("NewApi")
    protected void hideBottomUIMenu() {
        // 隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            // for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 提示显示框
     */
    private void ShowPromptMessage(String showmessage, int audionum) {
        if (audionum == 1) {
            playMusic(R.raw.burlcard);
        }
        if (audionum == 3) {
            playMusic(R.raw.blacklist);
        }
        loadprompt.setText(showmessage);
        promptshow_xml.setVisibility(View.VISIBLE);
        if (audionum != 2) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    promptshow_xml.setVisibility(View.GONE);
                }
            }, 1500);
        }
    }

    /**
     * 1vsn显示对比后成功是否窗口
     */
    private void showAlert() {
        if ((isOpenOneVsMore != false) || (Const.DELETETEMPLATE == false)) {
            if (AppData.getAppData().getCompareScore() <= SPUtil.getFloat(Const.KEY_ONEVSMORESCORE, Const.ONEVSMORE_SCORE) && Const.ONE_VS_MORE_TIMEOUT_NUM >= Const.ONE_VS_MORE_TIMEOUT_MAXNUM) {
                if (promptshow_xml.getVisibility() != View.VISIBLE) {
                    Const.ONE_VS_MORE_TIMEOUT_NUM = 0;
                    ShowPromptMessage("请刷身份证", 1);
                }
            } else if (AppData.getAppData().getCompareScore() > SPUtil.getFloat(Const.KEY_ONEVSMORESCORE, Const.ONEVSMORE_SCORE) && AppData.getAppData().getNFaceBmp() != null) {
                String sdCardDir = null;
                Const.ONE_VS_MORE_TIMEOUT_NUM = 0;
                String snapImageID = IDUtils.genImageName();
                oneVsMore_face.setImageBitmap(AppData.getAppData().getNFaceBmp());
                //保存抓拍照片，用来显示对比结果
                FileUtils.saveFile(AppData.getAppData().getNFaceBmp(), snapImageID, TestDate.DGetSysTime() + "_Face");
                User user = MyApplication.faceProvider.getUserByUserId(AppData.getAppData().getUser().getId());
                AppData.getAppData().setUser(user);
                if (user.getTemplateImageID() != null) {
                    sdCardDir = Environment.getExternalStorageDirectory() + "/FaceAndroid/FaceTemplate/" + user.getTemplateImageID() + ".jpg";
                }
                try {
                    if (sdCardDir != null) {
                        Bitmap bmp = BitmapFactory.decodeFile(sdCardDir);
                        AppData.getAppData().setCardBmp(bmp);
                        oneVsMore_temper.setImageBitmap(bmp);
                    }
                } catch (Exception e) {
                    oneVsMore_temper.setImageResource(R.mipmap.ic_launcher);
                }
                if (user.getType().equals("黑名单")) {
                    ShowPromptMessage("黑名单", 3);
                    return;
                }
                GPIOHelper.openDoor(true);
                openRelay();

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        GPIOHelper.openDoor(false);
                    }
                }, SPUtil.getInt(Const.KEY_OPENDOOR, Const.CLOSE_DOOR_TIME) * 1000);

                oneVsMore_userName.setText(user.getName());
                oneVsMore_userType.setText(user.getType());
                oneVsMore_userID.setText(user.getWordNo());
                com.runvision.core.LogToFile.e("1:N", "1:N成功: 姓名：" + user.getName() + ",分数：" + AppData.getAppData().getCompareScore());
                user.setTime(DateTimeUtils.getTime());
                Record record = new Record(AppData.getAppData().getCompareScore() + "", "成功", Environment.getExternalStorageDirectory() + "/FaceAndroid/" + TestDate.DGetSysTime() + "_Face" + "/" + snapImageID, "1:N");
                user.setRecord(record);
                MyApplication.faceProvider.addRecord(user);

                oneVsMoreView.setVisibility(View.VISIBLE);
                playMusic(R.raw.success);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        oneVsMoreView.setVisibility(View.GONE);
                    }
                }, 1000);


                if (socketThread != null) {
                    SendData.sendComperMsgInfo(socketThread, true, Const.TYPE_ONEVSMORE);
                } else {
                    AppData.getAppData().clean();
                }
            } else if (AppData.getAppData().getCompareScore() != 0) {
                Const.ONE_VS_MORE_TIMEOUT_NUM++;
            }
        }
    }

    private void showConfirmPsdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        final View view = View.inflate(this, R.layout.dialog_confirm_psd, null);
        //让对话框显示一个自己定义的对话框界面效果
        dialog.setView(view);
        dialog.show();
        Button bt_submit = view.findViewById(R.id.bt_submit);
        Button bt_cancel = view.findViewById(R.id.bt_cancel);

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_confirm_psd = view.findViewById(R.id.et_confirm_psd);
                String confirmPsd = et_confirm_psd.getText().toString();
                String psd = Const.MOBILE_SAFE_PSD;
                if(!TextUtils.isEmpty(confirmPsd)){
                    if(psd.equals(confirmPsd)) {
                        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    } else {
                        showToast("输入密码错误");
                    }
                }else{
                    showToast("请输入密码");
                }
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 1v1显示对比后成功是否窗口
     */
    private void showAlertDialog() {
        String str = "";
        cardBmp_view.setImageBitmap(AppData.getAppData().getCardBmp());
        idcard_Bmp.setImageBitmap(AppData.getAppData().getCardBmp());
        card_name.setText(AppData.getAppData().getName());
        card_sex.setText(AppData.getAppData().getSex());
        name.setText(AppData.getAppData().getName());
        year.setText(AppData.getAppData().getBirthday().substring(0, 4));
        month.setText(AppData.getAppData().getBirthday().substring(5, 7));
        day.setText(AppData.getAppData().getBirthday().substring(8, 10));
        addr.setText(AppData.getAppData().getAddress());
        cardNumber.setText(AppData.getAppData().getCardNo().substring(0, 4)
                + "************"
                + AppData.getAppData().getCardNo().substring(16, 18));
        card_nation.setText(AppData.getAppData().getNation());
        faceBmp_view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (AppData.getAppData().getoneCompareScore() == 0) {
            str = "失败";
            isSuccessComper.setImageResource(R.mipmap.icon_sb);
            if (AppData.getAppData().getOneFaceBmp() == null) {
                faceBmp_view.setImageResource(R.mipmap.tx);
                faceBmp_view.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                faceBmp_view.setImageBitmap(AppData.getAppData().getOneFaceBmp());
                //保存抓拍图片
                String snapImageID = IDUtils.genImageName();
                FileUtils.saveFile(AppData.getAppData().getOneFaceBmp(), snapImageID, TestDate.DGetSysTime() + "_Face");
                //保存身份证图片
                String cardImageID = snapImageID + "_card";
                FileUtils.saveFile(AppData.getAppData().getCardBmp(), cardImageID, TestDate.DGetSysTime() + "_Card");

                Record record = new Record(AppData.getAppData().getoneCompareScore() + "", str, Environment.getExternalStorageDirectory() + "/FaceAndroid/" + TestDate.DGetSysTime() + "_Face" + "/" + snapImageID, "人证");
                User user = new User(AppData.getAppData().getName(), "无", AppData.getAppData().getSex(), 0, "无", AppData.getAppData().getCardNo(), Environment.getExternalStorageDirectory() + "/FaceAndroid/" + TestDate.DGetSysTime() + "_Card" + "/" + cardImageID, DateTimeUtils.getTime());
                user.setRecord(record);
                MyApplication.faceProvider.addRecord(user);
            }
            playMusic(R.raw.error);

            oneVsMoreView.setVisibility(View.GONE);
            alert.setVisibility(View.VISIBLE);

        } else if (AppData.getAppData().getoneCompareScore() < SPUtil.getFloat(Const.KEY_CARDSCORE, Const.ONEVSONE_SCORE) && AppData.getAppData().getOneFaceBmp() != null) {
            str = "失败";
            isSuccessComper.setImageResource(R.mipmap.icon_sb);
            playMusic(R.raw.error);
            faceBmp_view.setImageBitmap(AppData.getAppData().getOneFaceBmp());
            //保存抓拍图片
            String snapImageID = IDUtils.genImageName();
            FileUtils.saveFile(AppData.getAppData().getOneFaceBmp(), snapImageID, TestDate.DGetSysTime() + "_Face");
            //保存身份证图片
            String cardImageID = snapImageID + "_card";
            FileUtils.saveFile(AppData.getAppData().getCardBmp(), cardImageID, TestDate.DGetSysTime() + "_Card");

            Record record = new Record(AppData.getAppData().getoneCompareScore() + "", str, Environment.getExternalStorageDirectory() + "/FaceAndroid/" + TestDate.DGetSysTime() + "_Face" + "/" + snapImageID, "人证");
            User user = new User(AppData.getAppData().getName(), "无", AppData.getAppData().getSex(), 0, "无", AppData.getAppData().getCardNo(), Environment.getExternalStorageDirectory() + "/FaceAndroid/" + TestDate.DGetSysTime() + "_Card" + "/" + cardImageID, DateTimeUtils.getTime());
            user.setRecord(record);
            MyApplication.faceProvider.addRecord(user);

            oneVsMoreView.setVisibility(View.GONE);
            alert.setVisibility(View.VISIBLE);
        } else if (AppData.getAppData().getOneFaceBmp() != null && AppData.getAppData().getoneCompareScore() >= SPUtil.getFloat(Const.KEY_CARDSCORE, Const.ONEVSONE_SCORE)) {
            str = "成功";
            playMusic(R.raw.success);
            isSuccessComper.setImageResource(R.mipmap.icon_tg);
            faceBmp_view.setImageBitmap(AppData.getAppData().getOneFaceBmp());
            GPIOHelper.openDoor(true);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    GPIOHelper.openDoor(false);
                }
            }, SPUtil.getInt(Const.KEY_OPENDOOR, Const.CLOSE_DOOR_TIME) * 1000);

            //保存抓拍图片
            String snapImageID = IDUtils.genImageName();
            if (AppData.getAppData().getOneFaceBmp() != null) {
                FileUtils.saveFile(AppData.getAppData().getOneFaceBmp(), snapImageID, TestDate.DGetSysTime() + "_Face");
            }
            //保存身份证图片
            String cardImageID = snapImageID + "_card";
            if (AppData.getAppData().getCardBmp() != null) {
                FileUtils.saveFile(AppData.getAppData().getCardBmp(), cardImageID, TestDate.DGetSysTime() + "_Card");
            }

            Record record = new Record(AppData.getAppData().getoneCompareScore() + "", str, Environment.getExternalStorageDirectory() + "/FaceAndroid/" + TestDate.DGetSysTime() + "_Face" + "/" + snapImageID, "人证");
            User user = new User(AppData.getAppData().getName(), "无", AppData.getAppData().getSex(), 0, "无", AppData.getAppData().getCardNo(), Environment.getExternalStorageDirectory() + "/FaceAndroid/" + TestDate.DGetSysTime() + "_Card" + "/" + cardImageID, DateTimeUtils.getTime());
            user.setRecord(record);
            MyApplication.faceProvider.addRecord(user);


            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    GPIOHelper.openDoor(false);
                }
            }, 1000);
            oneVsMoreView.setVisibility(View.GONE);
            alert.setVisibility(View.VISIBLE);

        } else {
            oneVsMoreView.setVisibility(View.GONE);
            alert.setVisibility(View.GONE);
        }

        if (AppData.getAppData().getoneCompareScore() < SPUtil.getFloat(Const.KEY_CARDSCORE, Const.ONEVSONE_SCORE) && AppData.getAppData().getOneFaceBmp() != null) {
            //    Log.i("Gavin","人证失败："+socketThread.toString());
            if (socketThread != null) {
                SendData.sendComperMsgInfo(socketThread, false, Const.TYPE_CARD);
            } else {
                AppData.getAppData().clean();
            }
        }
        if (AppData.getAppData().getoneCompareScore() >= SPUtil.getFloat(Const.KEY_CARDSCORE, Const.ONEVSONE_SCORE) && AppData.getAppData().getOneFaceBmp() != null) {
//            Log.i("Gavin","人证成功："+socketThread.toString());
            if (socketThread != null) {
                SendData.sendComperMsgInfo(socketThread, true, Const.TYPE_CARD);
            } else {
                AppData.getAppData().clean();
            }
        }

        AppData.getAppData().setoneCompareScore(0);
        ReaderCardFlag = true;
        //ReaderCardFlag = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // AppData.getAppData().setOneFaceBmp(null);
                oneVsMoreView.setVisibility(View.GONE);
                alert.setVisibility(View.GONE);
                // isOpenOneVsMore = true;
            }
        }, 2000);
    }


    /**
     * 播放语音
     */
    public void playMusic(int msuicID) {
        if (!SPUtil.getBoolean(Const.KEY_ISOPENMUSIC, Const.OPEN_MUSIC)) {
            return;
        }
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.release();
            }
        }
        mPlayer = MediaPlayer.create(mContext, msuicID);
        mPlayer.start();
    }

    /**
     * 1：N比对操作线程
     */
    class OneVsMoreThread extends Thread {
        private FaceInfo info;
        AFR_FSDKFace face;
        AFR_FSDKMatching score;
        User user;

        public OneVsMoreThread(FaceInfo info) {
            this.info = info;
        }

        @Override
        public void run() {
            if (isOpenOneVsMore != false) {
                if (face == null) {
                    face = new AFR_FSDKFace();
                }
                int ret = MyApplication.mFaceLibCore.FaceFeature(info.getDes(), 480, 640, info.getFace().getRect(), info.getFace().getDegree(), face);
                if (ret == 0) {
                    AppData.getAppData().SetNFaceBmp(CameraHelp.getFaceImgByInfraredJpg(info.getFace().getRect().left, info.getFace().getRect().top, info.getFace().getRect().right, info.getFace().getRect().bottom, CameraHelp.getBitMap(info.getDes())));
                    float fenshu = SPUtil.getFloat(Const.KEY_ONEVSMORESCORE, Const.ONEVSMORE_SCORE);
                    if (score == null) {
                        score = new AFR_FSDKMatching();
                    }
                    Log.i("GavinTest", "for前" + System.currentTimeMillis());
                    if (MyApplication.mList.size() > 0) {

                        Log.i("Gavin0903", "for");

                      /*  Iterator iter = MyApplication.mList.entrySet().iterator();
                        while (iter.hasNext()) {
                            if((isOpenOneVsMore==false)||(Const.BATCH_IMPORT_TEMPLATE==true))
                            {
                                //  AppData.getAppData().setCompareScore(0);
                                continue;
                            }
                            Map.Entry entry = (Map.Entry) iter.next();
                            String fileName = (String) entry.getKey();
                            byte[] mTemplate = (byte[]) entry.getValue();
                            AFR_FSDKFace face3 = new AFR_FSDKFace(mTemplate);
                            ret = MyApplication.mFaceLibCore.FacePairMatching(face3, face, score);
                            if (score.getScore() >= fenshu) {
                                if(user==null) {
                                    user = new User();
                                }
                                if (MyApplication.faceProvider.quaryUserTableRowCount("select count(id) from tUser") != 0) {
                                    user.setId(MyApplication.faceProvider.getUserByUserpath(fileName).getId());
                                    AppData.getAppData().setUser(user);
                                }
                                fenshu = score.getScore();
                                continue;
                            }
                        }*/


                        for (Map.Entry<String, byte[]> entry : MyApplication.mList.entrySet()) {
                            if ((isOpenOneVsMore == false) || (Const.BATCH_IMPORT_TEMPLATE == true) || (Const.DELETETEMPLATE == true)) {
                                //  AppData.getAppData().setCompareScore(0);
                                continue;
                            }
                            String fileName = (String) entry.getKey();
                            byte[] mTemplate = (byte[]) entry.getValue();
                            AFR_FSDKFace face3 = new AFR_FSDKFace(mTemplate);
                            ret = MyApplication.mFaceLibCore.FacePairMatching(face3, face, score);
                            if (score.getScore() >= fenshu) {
                                if (user == null) {
                                    user = new User();
                                }
                                if (MyApplication.faceProvider.quaryUserTableRowCount("select count(id) from tUser") != 0) {
                                    if ((MyApplication.faceProvider.getUserByUserpath(fileName)) != null) {
                                        user.setId(MyApplication.faceProvider.getUserByUserpath(fileName).getId());
                                        AppData.getAppData().setUser(user);
                                    } else {

                                    }
                                }
                                fenshu = score.getScore();
                                continue;
                            }
                        }
                        Log.i("GavinTest", "for后" + System.currentTimeMillis());
                        Log.i("GavinTest", "fenshu:" + fenshu);
                        // AppData.getAppData().setCompareScore(fenshu);
                        AppData.getAppData().setCompareScore(fenshu);
                    }
                } else {
                    AppData.getAppData().setCompareScore(0);
                }
                if (isOpenOneVsMore != false) {
                    // Log.i("Gavin", "发送消息:");
                    Message msg = new Message();
                    msg.what = Const.COMPER_END;
                    mHandler.sendMessage(msg);
                }

                if (MyApplication.mList.size() < 1000) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    oneVsMoreThreadStauts = false;
                } else if (MyApplication.mList.size() < 3000) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    oneVsMoreThreadStauts = false;
                } else {
                    oneVsMoreThreadStauts = false;
                }
            }
        }

    }

    /**
     * 红外线程
     */
    private class MyRedThread extends Thread {

        public boolean redflag = false;

        @Override
        public void run() {
            super.run();
            while (true) {
                //Log.i("Gavin","redflag:" +redflag);
                // G68A设备红外接口不一样
                int status = GPIOHelper.readStatus();
                status = 1;
                if (redflag == true) {
                    try {
                        Thread.sleep(1500);
                        if (status == 1) {
                            mCameraSurfView.openCamera();
                            Message msg4 = new Message();
                            msg4.what = Const.TEST_INFRA_RED;
                            msg4.obj = 1;
                            mHandler.sendMessage(msg4);
                            logshowflag = 0;
                        }
                        if (status == 0) {
                            logshowflag++;
                            if (logshowflag == ((SPUtil.getInt(Const.KEY_BACKHOME, Const.CLOSE_HOME_TIMEOUT)) / 1.5)) {
                                logshowflag = 0;
                                Message msg4 = new Message();
                                msg4.what = Const.FLAG_SHOW_LOG;
                                msg4.obj = 2;
                                mHandler.sendMessage(msg4);

                            }
                            //handler.removeMessages(0);
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }

        public void closeredThread() {
            this.redflag = false;
        }

        public void startredThread() {
            this.redflag = true;
        }

    }

    /**
     * 更新UI标志线程
     */
    private class UIThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    Thread.sleep(250);
                    Message msg = new Message();
                    msg.what = Const.UPDATE_UI;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 开启HTTP服务时显示IP
     *
     * @param ip
     */
    public void httpStrat(String ip) {
        if (!TextUtils.isEmpty(ip)) {
            showHttpUrl.setText(ip + ":8088");
        } else {
            showHttpUrl.setText("");
        }
    }

    /**
     * HTTP服务开启异常时
     *
     * @param msg
     */
    public void httpError(String msg) {
        showHttpUrl.setText(msg + "---请重启软件，获取IP地址");
    }

    /**
     * 关闭服务
     */
    public void httpStop() {
        showHttpUrl.setText("The HTTP Server is stopped---请重启软件，获取IP地址");
    }

    /**
     * 注册网络监听广播
     */
    private void openNetStatusReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetWorkStateReceiver();
        receiver.setmINetStatusListener(this);
        registerReceiver(receiver, filter);
    }

    /**
     * 网络状态改变  接口回调的数据     *
     *
     * @param state
     */
    @Override
    public void getNetState(int state) {
        if (state == 0) {
            System.out.println("conn");
            openSocket();
            openHttpServer();
        } else {
            System.out.println("dis conn");
            closeSocket();
            closeHttpServer();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }

    /**
     * 打开socket连接
     */
    private void openSocket() {
        boolean conn = ConversionHelp.isNetworkConnected(mContext);
        receiver.setIs_conn(conn);
        if (!conn) {
            Toast.makeText(mContext, "没有网络,不开启socket连接", Toast.LENGTH_SHORT).show();
            com.runvision.core.LogToFile.i("MainActivity", "没有网络,不开启socket连接");
            return;
        }
        openHttpServer();

        if (!SPUtil.getString(Const.KEY_VMSIP, "").equals("") && SPUtil.getInt(Const.KEY_VMSPROT, 0) != 0 && !SPUtil.getString(Const.KEY_VMSUSERNAME, "").equals("") && !SPUtil.getString(Const.KEY_VMSPASSWORD, "").equals("")) {
            //开启socket线程
            socketReconnect(SPUtil.getString(Const.KEY_VMSIP, ""), SPUtil.getInt(Const.KEY_VMSPROT, 0));
        }
    }

    /**
     * socket重连接
     *
     * @param ip
     * @param port
     */
    private void socketReconnect(String ip, int port) {
        if (socketThread == null) {
            socketThread = new SocketThread(ip, port, mHandler);
        } else {
            socketThread.close();
            if (heartBeatThread != null) {
                heartBeatThread.HeartBeatThread_flag = false;
                heartBeatThread = null;
            }
            socketThread = new SocketThread(ip, port, mHandler);
        }
        socketThread.start();
    }

    /**
     * 结束socket
     *
     * @param
     */
    public void closeSocket() {
        if (heartBeatThread != null) {
            //取消心跳
            heartBeatThread.HeartBeatThread_flag = false;
            heartBeatThread = null;
        }
        //结束socket
        if (socketThread != null) {
            socketThread.close();
            socketThread = null;
        }

    }

    //上传的所有数据长度大小
    private int mSum = 0;
    //切割后的数据
    private List<File> dataList1 = null;
    private List<File> dataList2 = null;
    private List<File> dataList3 = null;
    //三个线程消息传递对应的标志为
    private int[] loadFlag = {101, 102, 103};
    private int bacthOk1, bacthOk2, bacthOk3 = 0;
    private int parts = 0;
    private ProgressBar progesss1, progesss2, progesss3;
    private TextView progesssValue1, progesssValue2, progesssValue3;
    private Dialog batchDialog;

    private List<File> getImagePathFile() {
        String strPath = Environment.getExternalStorageDirectory() + "/SocketImage/";
        File file = new File(strPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File[] mListFile = file.listFiles();
        if (mListFile.length == 0) {
            //Toast.makeText(mContext, "SocketImage文件夹下面没有图片文件", Toast.LENGTH_SHORT).show();
            return null;
        }
        List<File> mImportFile = new ArrayList<>();
        for (File file1 : mListFile) {
            if (checkIsImageFile(file1.getName())) {
                mImportFile.add(file1);
            }
        }
        //得到图片文件
        if ((mSum = mImportFile.size()) == 0) {
            //Toast.makeText(mContext, "image文件夹下面没有图片文件", Toast.LENGTH_SHORT).show();
            return null;
        }
        return mImportFile;
    }

    private void batchImport() {

        List<File> mImportFile = getImagePathFile();
        if (mImportFile == null) {
            return;
        }

        //vms_Import_template=true;

        Const.VMS_BATCH_IMPORT_TEMPLATE = true;
        // Const.BATCH_FLAG=1;

        System.out.println("一共：" + mSum);
        //将文件数据分成三个集合
        cuttingList(mImportFile);

        if (parts == 1) {
            BatchImport impory = new BatchImport(dataList1, mHandler, loadFlag[0]);
            Thread thread = new Thread(impory);
            thread.start();
        } else if (parts == 3) {
            BatchImport impory1 = new BatchImport(dataList1, mHandler, loadFlag[0]);
            Thread thread1 = new Thread(impory1);
            thread1.start();

            BatchImport impory2 = new BatchImport(dataList2, mHandler, loadFlag[1]);
            Thread thread2 = new Thread(impory2);
            thread2.start();

            BatchImport impory3 = new BatchImport(dataList3, mHandler, loadFlag[2]);
            Thread thread3 = new Thread(impory3);
            thread3.start();
        }
    }

    /**
     * 检查扩展名，得到图片格式的文件
     *
     * @param fName 文件名
     * @return
     */
    private boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg") || FileEnd.equals("bmp")) {
            isImageFile = true;
        } else {
            isImageFile = false;
        }
        return isImageFile;
    }

    private void cuttingList(List<File> list) {
        //我们数据之分三批
        int part = 3;
        int dataList = list.size();
        int minBatchImprot = 10;
        int pointsDataLimit = dataList % part == 0 ? dataList / part : (dataList / part) + 1;
        if (dataList > minBatchImprot) {
            parts = 3;
            System.out.println("开启三个线程");
            dataList1 = list.subList(0, pointsDataLimit);
            dataList2 = list.subList(pointsDataLimit, pointsDataLimit * 2);
            if (!list.isEmpty()) {
                dataList3 = list.subList(pointsDataLimit * 2, list.size());
            }
        } else {
            parts = 1;
            //只开启一个线程
            System.out.println("只开启一个线程");
            dataList1 = list;
        }
    }

    /**
     * 打开HTTP服务器
     */
    public void openHttpServer() {
        //开启HTTP服务
        if (serverManager != null) {
            closeHttpServer();
        }
        serverManager = new ServerManager(this);
        serverManager.register();
        serverManager.startService();

    }

    public void closeHttpServer() {
        if (serverManager != null) {
            serverManager.unRegister();
            serverManager.stopService();
            serverManager = null;
        }

    }

    public void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    public void onBackPressed() {
        cancelToast();
        super.onBackPressed();
    }


    public void rebootSU() {
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        OutputStreamWriter osw = null;
        StringBuilder sbstdOut = new StringBuilder();
        StringBuilder sbstdErr = new StringBuilder();

        String command = "/system/bin/reboot";

        try { // Run Script
            proc = runtime.exec("su");
            osw = new OutputStreamWriter(proc.getOutputStream());
            osw.write(command);
            osw.flush();
            osw.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            if (proc != null)
                proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sbstdOut.append(new BufferedReader(new InputStreamReader(proc
                .getInputStream())));
        sbstdErr.append(new BufferedReader(new InputStreamReader(proc
                .getErrorStream())));
        if (proc.exitValue() != 0) {
        }
    }

    //获取当前系统时间
    private Date currentTime = null;//currentTime就是系统当前时间
    //定义时间的格式
    private DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Date strbeginDate = null;//起始时间
    private Date strendDate = null;//结束时间
    private boolean range = false;

    public Boolean TimeCompare(String strbeginTime, String strendTime, String currentTime1) {
        try {
            strbeginDate = fmt.parse(strbeginTime);//将时间转化成相同格式的Date类型
            strendDate = fmt.parse(strendTime);
            currentTime = fmt.parse(currentTime1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if ((currentTime.getTime() - strbeginDate.getTime()) > 0 && (strendDate.getTime() - currentTime.getTime()) > 0) {//使用.getTime方法把时间转化成毫秒数,然后进行比较
            range = true;
        } else {
            range = false;
        }
        return range;
    }

    private SerialPortManager mSerialPortManager;
    private InputStream mInputStream4;
    private OutputStream mOutputStream4;
    private com.wits.serialport.SerialPort serialPort4;
    private String icCard = "";

    public void openRelay() {
        if (mOutputStream4 == null) {
            showToast("请先打开串口");
            return;
        }
        try {
            byte[] bytes1 = SlecProtocol.hexStringToBytes(new String[]{
                            "55555555",  //用户id,8个字符，缺少的前面补0
                            "12345678",//用户卡号,8个字符，缺少的的前面补0
                            "0001"}//开门间隔,4个字符，缺少的的前面补0
                    , true);
            byte[] bytes = SlecProtocol.commandAndDataToAscii(
                    ((byte) 0x01),
                    bytes1
            );
            mOutputStream4.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在java代码中执行adb命令
     * @param command
     * @return
     */
    public String execCommand(String command) {
        Runtime runtime;
        Process proc = null;
        StringBuffer stringBuffer = null;
        try {
            runtime = Runtime.getRuntime();
            proc = runtime.exec(command);
            stringBuffer = new StringBuffer();
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line + " ");
            }

        } catch (Exception e) {
            System.err.println(e);
        } finally {
            try {
                proc.destroy();
            } catch (Exception e2) {
            }
        }
        return stringBuffer.toString();
    }

    //判断网线拔插状态
    //通过命令cat /sys/class/net/eth0/carrier，如果插有网线的话，读取到的值是1，否则为0
    public boolean isWirePluggedIn(){
        String state= execCommand("cat /sys/class/net/eth0/carrier");
        if(state.trim().equals("1")){  //有网线插入时返回1，拔出时返回0
            return true;
        }
        return false;
    }

    private class SLecDeviceThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                //串口4，继电器控制
                serialPort4 = mSerialPortManager.getSerialPort4();
                mInputStream4 = serialPort4.getInputStream();
                mOutputStream4 = serialPort4.getOutputStream();
                sleep(500);
                while (true) {
                    try {
                        sleep(50);
                        byte[] buffer = new byte[64];
                        if (mInputStream4 == null) {
                            continue;
                        }
                        int size = mInputStream4.read(buffer);

                        if (size < 1) {
                            continue;
                        }

                        int len = icCard.length();
                        Log.e("gzy", "run: " + size + "--" + icCard);
                        if (len == 0) {
                            //第一条数据
                            icCard = SlecProtocol.bytesToHexString2(buffer, size);
                        } else {
                            //之前已经有数据
                            icCard = icCard + SlecProtocol.bytesToHexString2(buffer, size);
                        }
                        mHandler.removeCallbacks(cancelCardRunnable);
                        //200ms没有新的数据就发送
                        mHandler.postDelayed(cancelCardRunnable, 200);

                    } catch (SecurityException e) {
                        Log.e("SerialPort", "-----------------SecurityException");
                    } catch (IOException e) {
                        Log.e("SerialPort", "-----------------IOException" + e.toString());
                    } catch (InvalidParameterException e) {
                        Log.e("SerialPort", "-----------------InvalidParameterException");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 退出读卡状态
     */
    private Runnable cancelCardRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                //处理icCard
                Log.e("gzy", "接收到的串口数据为: " + icCard);

                final byte[] bytes = SlecProtocol.asciiToHex(SlecProtocol.hexToByteArray(icCard));
                if (bytes.length > 5) {
                    Log.e("gzy", "接收转换: " + SlecProtocol.bytesToHexString2(bytes, bytes.length) +
                            "--命令：" + bytes[3] +
                            "--数据长度：" + bytes[5] +
                            "--数据：" + (bytes[5] == 0 ? "没有数据" : bytes[6])
                    );
                    switch (bytes[3]) {
                        case 1:
                            if (bytes[6] == 0) {
                                Log.e("gzy", "run: 发送开门指令成功");
                            } else {
                                Log.e("gzy", "run: 发送开门指令失败");
                            }
                            break;
                        case 2:
                            //刷卡
                            //6-9是用户id，10-13是卡号
                            if (bytes.length > 13) {
                                final byte[] card = new byte[4];
                                for (int i = 0; i < card.length; i++) {
                                    card[i] = bytes[10 + i];
                                }
                            }
                            break;
                        default:
                    }
                }
                icCard = "";
            } catch (Exception e2) {
                e2.printStackTrace();
                icCard = "";
            }
            icCard = "";
        }
    };

}
