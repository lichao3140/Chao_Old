package com.runvision.utils;

import java.lang.reflect.Method;

public class JniUtil {

    static {
        System.loadLibrary("native-lib");
    }

    /**
     * 判断序列号是否授权
     *
     * @param sn
     * @return
     */
    public native static boolean AuthorizeSN(String sn);

    public native static String getAPPID();

    public native static String getSDKKEY();

    private static Method systemProperties_get = null;

    public static String initSDK() {
        String mysn = null;

        String[] propertys = {"ro.boot.serialno", "ro.serialno"};
        for (String key : propertys){
            mysn= JniUtil.getAndroidOsSystemProperties(key);
        }

        if (AuthorizeSN(mysn)) {
            //IdCardVerifyManager.getInstance().init(getAPPID(), getSDKKEY());
            return "initSDK success";
        } else {

            return "initSDK falut";
        }
    }

    public static String getAndroidOsSystemProperties(String key) {
        String ret;
        try {
            systemProperties_get = Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class);
            if ((ret = (String) systemProperties_get.invoke(null, key)) != null)
                return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return "";
    }
}
