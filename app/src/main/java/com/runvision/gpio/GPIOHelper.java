package com.runvision.gpio;

/**
 * Created by Administrator on 2018/3/23.
 */

public class GPIOHelper {
    static {
        System.loadLibrary("gpio-lib");
    }

    public static native void openDoor(boolean no);

    public static native int readStatus();

    public static native void init();

}
