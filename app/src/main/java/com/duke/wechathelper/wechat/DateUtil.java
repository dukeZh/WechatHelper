package com.duke.wechathelper.wechat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 */
public class DateUtil {
    public static final String NULL="null";
    /** 
     * 时间戳转换成日期格式字符串 
     * @param seconds 精确到秒的字符串
     * @return
     */  
    public static String timeStamp2Date(String seconds) {
        if(seconds == null || seconds.isEmpty() || seconds.equals(NULL)){
            return "";  
        }  

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        return sdf.format(new Date(Long.valueOf(seconds)));
    }  
    /** 
     * 日期转时间戳
     * @return
     */
    public static Long date2Timestamp(String time){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        try {
            Date date = simpleDateFormat.parse(time);
            long ts = date.getTime();
            return ts;
        } catch (ParseException e) {
            return Long.valueOf(0);
        }
    }
      
    /** 
     * 取得当前时间戳（精确到秒） 
     * @return 
     */  
    public static String getTimeStamp(){
        long time = System.currentTimeMillis();
        String t = String.valueOf(time);
        return t;  
    }

    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(data);
    }

}