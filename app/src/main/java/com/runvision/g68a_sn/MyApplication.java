package com.runvision.g68a_sn;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.runvision.bean.FaceLibCore;
import com.runvision.db.Admin;
import com.runvision.db.FaceProvider;
import com.runvision.gpio.GPIOHelper;
import com.runvision.utils.CameraHelp;
import com.runvision.utils.LogToFile;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/6/5.
 */
public class MyApplication extends Application {
    private String TAG = "MyApplication";

    private static Context context;
    ArrayList<Activity> list = new ArrayList<Activity>();

    private static MyApplication myApplication;
    public static FaceProvider faceProvider;
    public static FaceLibCore mFaceLibCore = new FaceLibCore();

    public static Map<String,byte[]> mList = new HashMap<String,byte[]>();

    Uri mImage;

    public static MyApplication getInstance() {
        return myApplication;
    }


    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        context = getApplicationContext();
        GPIOHelper.init();
       // initLeakCanary();
        //初始化日志打印
        LogToFile.init(this);
        String serlia = getSerialNumber();
        // G68A设备没有序列号
//       if (serlia.equals("") || serlia.length() < 4 || !serlia.substring(0, 4).equals("R50A")) {
//            LogToFile.e(TAG,"该设备没有序列号");
//            finishActivity();
//        }

        int ret = mFaceLibCore.initLib(context);
        if (ret == 0) {
            Toast.makeText(this, "算法初始化成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "算法初始化失败" + ret, Toast.LENGTH_SHORT).show();
        }
        faceProvider=new FaceProvider(this);
        if (faceProvider.querAdminSize() == 0) {
            faceProvider.addAdmin(new Admin("admin", "123456"));
        }

        loadTemper();
    }


    private void loadTemper() {
        String path = Environment.getExternalStorageDirectory() + "/FaceAndroid/Template/";
        File mFile = new File(path);
        if (!mFile.exists()) {
            return;
        }
        File[] files = mFile.listFiles();
        Log.i("Gavin", "files:" + files.length);
        for (File file : files) {
            byte[] temp = CameraHelp.readFile(file);
            String userName = file.getName().substring(0, file.getName().indexOf("."));
            // byteslist.add(temp);
            mList.put(userName, temp);
        }

    }

    public String getSerialNumber() {
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

   // private void initLeakCanary() {

    //    if (LeakCanary.isInAnalyzerProcess(this)) {
    //        return;
   //     }
   //     LeakCanary.install(this);
   // }

    public void init() {
        // 设置该CrashHandler为程序的默认处理器
        UnCeHandler catchExcep = new UnCeHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(catchExcep);
    }

    /**
     * Activity关闭时，删除Activity列表中的Activity对象
     */
    public void removeActivity(Activity a) {
        list.remove(a);
    }

    /**
     * 向Activity列表中添加Activity对象
     */
    public void addActivity(Activity a) {
        list.add(a);
    }

    /**
     * 关闭Activity列表中的所有Activity
     */
    public void finishActivity() {
        for (Activity activity : list) {
            if (null != activity) {
                activity.finish();
            }
        }
        // 杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * @param path
     * @return
     */
    public static Bitmap decodeImage(String path) {
        Bitmap res;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = 1;
            op.inJustDecodeBounds = false;
            //op.inMutable = true;
            res = BitmapFactory.decodeFile(path, op);
            //rotate and scale.
            Matrix matrix = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }

            Bitmap temp = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
            Log.d("com.arcsoft", "check target Image:" + temp.getWidth() + "X" + temp.getHeight());

            if (!temp.equals(res)) {
                res.recycle();
            }
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void setCaptureImage(Uri uri) {
        mImage = uri;
    }

    public Uri getCaptureImage() {
        return mImage;
    }
}
