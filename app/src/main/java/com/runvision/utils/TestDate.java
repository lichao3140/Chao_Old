package com.runvision.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestDate {





    /**
     * 得到几天前的时间
     * @param d
     * @param day
     * @return
     */
    public static String getDateBefore(Date d,int day){
        Calendar now =Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE,now.get(Calendar.DATE)-day);
        return DateToString(now.getTime());
    }


    public static String DateToString(Date date){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str=sdf.format(date);
        return str;
    }



    public static String getTime(String time){
        Calendar  calendar =Calendar. getInstance();
        calendar.setTimeInMillis(Long.valueOf(time));
        calendar.add( Calendar. DATE, -1); //向前走一天
        Date date= calendar.getTime();
        calendar.setTime(date);
        System.out.println("前一天时间为" +date .toString());
        return (date.getTime()+"").substring(0,10);
    }


    /**
     * 根据提供的年月日获取该月份的第一天
     * @Description: (这里用一句话描述这个方法的作用)
     * @Author: gyz
     * @Since: 2017-1-9下午2:26:57
     * @param date
     *
     * @return
     */
    public static String getSupportBeginDayofMonth(Date date) {
        date.getTime();
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(date);
        startDate.set(Calendar.MONTH, startDate.get(Calendar.MONTH));
        startDate.set(Calendar.DAY_OF_MONTH, 0);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        Date firstDate = startDate.getTime();
        return (firstDate.getTime()+"").substring(0,10);

    }

    /**
     * 根据提供的年月获取该月份的最后一天
     * @Description: (这里用一句话描述这个方法的作用)
     * @Author: gyz
     * @Since: 2017-1-9下午2:29:38
     * @param date
     * @return
     */
    public static String getSupportEndDayofMonth(Date date) {
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(date);
        startDate.set(Calendar.DAY_OF_MONTH, startDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        startDate.set(Calendar.HOUR_OF_DAY, 23);
        startDate.set(Calendar.MINUTE, 59);
        startDate.set(Calendar.SECOND, 59);
        startDate.set(Calendar.MILLISECOND, 999);
        Date firstDate = startDate.getTime();
        return (firstDate.getTime()+"").substring(0,10);
    }


        /* 获取当前时间时间戳*/
    public static String DGetSysTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss
        //获取当前时间Date date = new Date(System.currentTimeMillis());
        // return simpleDateFormat.format(new Date(System.currentTimeMillis()));
        Date date = null;
        try {
            date = simpleDateFormat.parse(simpleDateFormat.format(new Date(System.currentTimeMillis())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long ts = date.getTime();
        return String.valueOf(ts);
    }


    /* 获取当前时间时间戳*/
    public static String SGetSysTime() {

            long time=System.currentTimeMillis()/1000;//.currentimeMillis()/1000;//获取系统时间的10位的时间戳

            String  str=String.valueOf(time);

            return str;

        }



    public static String timetodate(String time) {
       // Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(Long.valueOf(time));
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这里的格式可换"yyyy年-MM月dd日-HH时mm分ss秒"等等格式

       // String date = sf.format(calendar.getTime());
        //return date;

        Date date = new Date(Long.valueOf(time)* 1000);
        return sf.format(date);
    }

    }
