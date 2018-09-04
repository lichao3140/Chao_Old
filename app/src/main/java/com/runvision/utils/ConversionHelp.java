package com.runvision.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ConversionHelp {
    // long类型转成byte数组
    public static byte[] longToByte(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Long(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    // byte数组转成long
    public static long byteToLong(byte[] b) {
        long s = 0;
        long s0 = b[0] & 0xff;// 最低位
        long s1 = b[1] & 0xff;
        long s2 = b[2] & 0xff;
        long s3 = b[3] & 0xff;
        long s4 = b[4] & 0xff;// 最低位
        long s5 = b[5] & 0xff;
        long s6 = b[6] & 0xff;
        long s7 = b[7] & 0xff;

        // s0不变
        s1 <<= 8;
        s2 <<= 16;
        s3 <<= 24;
        s4 <<= 8 * 4;
        s5 <<= 8 * 5;
        s6 <<= 8 * 6;
        s7 <<= 8 * 7;
        s = s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
        return s;
    }

    /**
     * 注释：int到字节数组的转换！
     *
     * @param number
     * @return
     */
    public static byte[] intToByte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return Bubble(b);
    }

    /**
     * 注释：字节数组到int的转换！
     *
     * @param b
     * @return
     */
    public static int byteToInt(byte[] b) {
        int s = 0;
        int s0 = b[0] & 0xff;// 最低位
        int s1 = b[1] & 0xff;
        int s2 = b[2] & 0xff;
        int s3 = b[3] & 0xff;
        s3 <<= 24;
        s2 <<= 16;
        s1 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }

    public static char byteToChar(byte[] b) {
        int s = 0;
        int s0 = b[0] & 0xff;// 最低位
        s = s0;
        return (char) s;
    }

    public static byte[] charToByte(char number) {
        int temp = number;
        byte[] b = new byte[1];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }

    /**
     * 注释：short到字节数组的转换！
     *
     * @param
     * @return
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return Bubble(b);
    }

    /**
     * 注释：字节数组到short的转换！
     *
     * @param b
     * @return
     */
    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * 字节 解码 （String---byte[]）
     */
    public static byte[] decodeHex(String nm) {
        int len = nm.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i++) {
            char c = nm.charAt(i);
            byte b = Byte.decode("0x" + c);
            c = nm.charAt(++i);
            result[i / 2] = (byte) (b << 4 | Byte.decode("0x" + c));
        }
        return result;
    }

    /**
     * 接收的数据（字节数组） 转 字符串
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static byte[] Bubble(byte[] a) {
        if (a.length == 4) {
            byte temp = a[0];
            a[0] = a[3];
            a[3] = temp;

            temp = a[2];
            a[2] = a[1];
            a[1] = temp;
        }
        if (a.length == 2) {
            byte temp = a[0];
            a[0] = a[1];
            a[1] = temp;
        }

        return a;
    }

    /**
     * 包序号，随机
     *
     * @return 产生的随机数
     */
    private static int sum = 0;

    public static int SeqNumber() {

        ++sum;
        return sum;
    }

    /**
     * 数据校验和的计算方式
     *
     * @param data 传入发送的字节数组
     * @param leng 总长度-包头（1）-包尾（1）-本身字节长度
     * @return
     */

    public static int getCheck(byte[] data, int leng) {
        int sum = 0;
        int i = 1;
        while (--leng > 0) {
            //System.out.println(data[i]+"和"+sum);

            sum += data[i] & 0xff;


            i++;
        }
        return sum;
    }

    /***
     * 解析返回的字节数组  转int
     * @param src
     * @param offset
     * @return
     */

    public static int bytesToInt2(byte[] src, int offset) {
        int value;
        value = (int) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16)
                | ((src[offset + 2] & 0xFF) << 8)
                | (src[offset + 3] & 0xFF));
        return value;

    }


    public static short bytesToInt3(byte[] src, int offset) {
        short value;
        value = (short) (((src[offset] & 0xFF) << 24)
                | ((src[offset + 1] & 0xFF) << 16));

        return value;

    }


    /***
     * 将返回的16进制字符串转字节数组
     * @param hex
     * @return
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static int toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 时间转化成4个长度的字节数组
     *
     * @param timeStr
     * @return
     */

    public static byte[] timeStringToBytes(String timeStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = sdf.parse(timeStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int time = (int) (calendar.getTimeInMillis() / 1000);
        byte[] bytes = new byte[4];
        for (int i = bytes.length - 1; i >= 0; i--) {
            bytes[i] = (byte) (time & 0xFF);
            time >>= 8;
        }
        return bytes;
    }

//    public void socketSaveImage(byte[] data, String name) {
//
//        String root="";
//
//        try {
//            File file = new File(path);
//            if (file.exists()) {
//                return;
//            }
//            FileOutputStream imageOutput = new FileOutputStream(file);
//            imageOutput.write(data, 0, data.length);
//            imageOutput.close();
//            System.out.println("Make Picture success,Please find image in "
//                    + path);
//        } catch (Exception ex) {
//            System.out.println("Exception: " + ex);
//            ex.printStackTrace();
//        }
//    }

    /**
     * 字节数组转化成时间
     *
     * @param bytes
     * @return
     */
    public static String bytesToTimeStr(byte[] bytes) {
        int time = (bytes[0] << 24) & 0xFF000000 |
                (bytes[1] << 16) & 0xFF0000 | (bytes[2] << 8) & 0xFF00 |
                (bytes[3]) & 0xFF;
        Calendar calendar = new GregorianCalendar();

        calendar.setTimeInMillis(time * 1000L);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeStr = sdf.format(calendar.getTime());

        return timeStr;
    }

    /**
     * 将图片转成字节流
     *
     * @param bmp
     * @return
     */
    public static byte[] bitmapToByte(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();
        return datas;
    }

    public static String hexStringRemove0(String hexStr) {
        boolean flag = true;
        do {
            if (hexStr.length() == 0) {
                return "";
            }

            if (hexStr.charAt(hexStr.length() - 1) == '0' && hexStr.charAt(hexStr.length() - 2) == '0') {
                hexStr = hexStr.substring(0, hexStr.length() - 2);
            } else {
                break;
            }
        } while (flag);

        return hexStr;
    }

    /**
     * 获取当前时间 变成字节流
     *
     * @return
     */
    public static byte[] getTimeByte() {
        int time = (int) (System.currentTimeMillis() / 1000);
        byte[] bytes = new byte[4];
        for (int i = bytes.length - 1; i >= 0; i--) {
            bytes[i] = (byte) (time & 0xFF);
            time >>= 8;
        }
        return bytes;
    }

    /**
     * 判断网络是否连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

}
