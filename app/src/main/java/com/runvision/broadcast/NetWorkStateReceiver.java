package com.runvision.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.runvision.utils.ConversionHelp;


/**
 * Created by Administrator on 2018/6/22.
 */

public class NetWorkStateReceiver extends BroadcastReceiver {
    private boolean is_conn=true;

    public void setIs_conn(boolean is_conn) {
        this.is_conn = is_conn;
    }

    private INetStatusListener mINetStatusListener;
    public void setmINetStatusListener(INetStatusListener mINetStatusListener) {
        this.mINetStatusListener = mINetStatusListener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
        } else {
            boolean isConnection= ConversionHelp.isNetworkConnected(context);
            if(isConnection && !is_conn){
                System.out.println("网络连接成功");
                mINetStatusListener.getNetState(0);
            }else if(!isConnection && is_conn){
                System.out.println("网络断开");
                mINetStatusListener.getNetState(-1);
            }
            is_conn=isConnection;
        }
    }

    public interface INetStatusListener {
        public void getNetState(int state);
    }
}
