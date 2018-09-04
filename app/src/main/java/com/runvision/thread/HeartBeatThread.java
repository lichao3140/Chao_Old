package com.runvision.thread;

import android.util.Log;

import com.runvision.core.LogToFile;
import com.runvision.utils.SendData;


/**
 * Created by Administrator on 2018/6/20.
 */

public class HeartBeatThread extends Thread {
    private String TAG = this.getClass().getSimpleName();
    private SocketThread thread;

    public HeartBeatThread(SocketThread thread) {
        this.thread = thread;
    }

    public boolean HeartBeatThread_flag = true;

    @Override
    public void run() {
        super.run();

        while (HeartBeatThread_flag) {
            SendData.heartbeat(thread);
            LogToFile.e(TAG, "心跳发送成功");
            Log.e(TAG, "心跳发送成功");
            try {
                sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
