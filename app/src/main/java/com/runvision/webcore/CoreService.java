/*
 * Copyright © 2017 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.runvision.webcore;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.runvision.webcore.handler.DeleteRecord;
import com.runvision.webcore.handler.DeleteTemplate;
import com.runvision.webcore.handler.InsertTemplate;
import com.runvision.webcore.handler.LoginHandler;
import com.runvision.webcore.handler.QueryAppConfig;
import com.runvision.webcore.handler.QueryAttendanceRules;
import com.runvision.webcore.handler.QueryRecord;
import com.runvision.webcore.handler.QueryRecordCount;
import com.runvision.webcore.handler.QueryRecordImage;
import com.runvision.webcore.handler.QueryTemplate;
import com.runvision.webcore.handler.QueryTemplateCount;
import com.runvision.webcore.handler.QueryTemplateImage;
import com.runvision.webcore.handler.UpdateAppConfig;
import com.runvision.webcore.handler.UpdateAttendanceRules;
import com.runvision.webcore.handler.UpdateDeviceIP;
import com.runvision.webcore.handler.UpdateTemplate;
import com.runvision.webcore.util.NetUtils;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;
import com.yanzhenjie.andserver.filter.HttpCacheFilter;
import com.yanzhenjie.andserver.website.AssetsWebsite;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * <p>Server service.</p>
 * Created by Yan Zhenjie on 2017/3/16.
 */
public class CoreService extends Service {

    /**
     * AndServer.
     */
    private Server mServer;

    @Override
    public void onCreate() {
        // More usage documentation: http://yanzhenjie.github.io/AndServer
        mServer = AndServer.serverBuilder()
                .inetAddress(NetUtils.getLocalIPAddress()) // Bind IP address.
                .port(8088)

                .timeout(10, TimeUnit.SECONDS)
                .website(new AssetsWebsite(getAssets(), "web"))
                .registerHandler("/adminLogin", new LoginHandler())
                .registerHandler("/queryRecord", new QueryRecord())
                .registerHandler("/queryRecordCount", new QueryRecordCount())
                .registerHandler("/queryRecordImage", new QueryRecordImage())
                .registerHandler("/deleteRecord",new DeleteRecord())
                .registerHandler("/queryTemplateCount",new QueryTemplateCount())
                .registerHandler("/queryTemplate",new QueryTemplate())
                .registerHandler("/InsertTemplate",new InsertTemplate())
                .registerHandler("/queryTemplateImage", new QueryTemplateImage())
                .registerHandler("/updateTemplate",new UpdateTemplate())
                .registerHandler("/deleteTemplate",new DeleteTemplate())
                .registerHandler("/queryAppConfig",new QueryAppConfig())
                .registerHandler("/updateAppConfig",new UpdateAppConfig())
                .registerHandler("/updatedeviceip", new UpdateDeviceIP())
                .registerHandler("/GetAtdConfig", new QueryAttendanceRules())
                .registerHandler("/SetAtdConfig", new UpdateAttendanceRules())
                .filter(new HttpCacheFilter())
                .listener(mListener)
                .build();
    }

    /**
     * Server listener.
     */
    private Server.ServerListener mListener = new Server.ServerListener() {
        @Override
        public void onStarted() {
//            String hostAddress = mServer.getInetAddress().getHostAddress();
            String hostAddress = getHostIP();
            ServerManager.serverStart(CoreService.this, hostAddress);
        }

        @Override
        public void onStopped() {
            ServerManager.serverStop(CoreService.this);
        }

        @Override
        public void onError(Exception e) {
            ServerManager.serverError(CoreService.this, e.getMessage());
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopServer(); // Stop server.
    }

    /**
     * Start server.
     */
    private void startServer() {
        if (mServer != null) {
            if (mServer.isRunning()) {
//                String hostAddress = mServer.getInetAddress().getHostAddress();
                String hostAddress = getHostIP();
                ServerManager.serverStart(CoreService.this, hostAddress);
            } else {
                mServer.startup();
            }
        }
    }

    /**
     * Stop server.
     */
    private void stopServer() {
        if (mServer != null && mServer.isRunning()) {
            mServer.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 获取ip地址
     * @return
     */
    public String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;

    }

}
