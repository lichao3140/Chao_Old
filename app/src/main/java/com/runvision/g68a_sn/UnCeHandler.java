package com.runvision.g68a_sn;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import com.runvision.utils.LogToFile;


/**
 * Created by Administrator on 2016/10/26.
 *
 * @auther madreain
 */

public class UnCeHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    public static final String TAG = "MyApplication";
    MyApplication application;

    public UnCeHandler(MyApplication application) {

        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.application = application;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {

            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }

            handleException(ex);

            Intent intent = new Intent(application.getApplicationContext(), MainActivity.class);
            PendingIntent restartIntent = PendingIntent.getActivity(
                    application.getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);

            AlarmManager mgr = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                    restartIntent);
            application.finishActivity();
        }
    }


    private boolean handleException(final Throwable ex) {


        if (ex == null) {
            return false;
        }

        ex.printStackTrace();

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
//                Toast.makeText(application.getApplicationContext(), "程序即将关闭,将会自动重启,请等待..."+ex.getMessage(),
//                        Toast.LENGTH_SHORT).show();
                LogToFile.e("奔溃信息",ex.getMessage());
                Looper.loop();
            }
        }.start();
        return true;
    }
}