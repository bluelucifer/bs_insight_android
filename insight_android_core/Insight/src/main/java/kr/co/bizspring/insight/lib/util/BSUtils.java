package kr.co.bizspring.insight.lib.util;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by mac on 2014. 11. 13..
 */
public class BSUtils {
    public static String joinIntToString(int[] values){
        ArrayList<String> arr = new ArrayList<String>();
        String buffer = "";
        for(int i = 0 ; i < values.length;i++){
            arr.add(String.valueOf(values[i]));
        }
        if(arr.size()>1){
            buffer +=arr.get(0);
            for(int i = 1 ; i < arr.size();i++){
                buffer += ";"+arr.get(i);
            }
            return buffer;
        }else if(arr.size()==1){
            return arr.get(0);//유일값.
        }else if(arr.size()<1){
            return "";//값없음.
        }
        return "";
    }

    public static String joinStringToString(String[] values) {
        ArrayList<String> arr = new ArrayList<String>();
        String buffer = "";
        for(int i = 0 ; i < values.length;i++){
            arr.add(String.valueOf(values[i]));
        }
        if(arr.size()>1){
            buffer +=arr.get(0);
            for(int i = 1 ; i < arr.size();i++){
                buffer += ";"+arr.get(i);
            }
            return buffer;
        }else if(arr.size()==1){
            return arr.get(0);//유일값.
        }else if(arr.size()<1){
            return "";//값없음.
        }
        return "";
    }

    /**
     * 순수방문자 ExpireTime을 계산하여 리턴함.
     * */
    public static final int DAILY_UNIQUE = 1;
    public static final int WEEKLY_UNIQUE = 2;
    public static final int MONTHLY_UNIQUE = 3;
    public static long getExpireLongTimeForUniVt(String inputDate, int type){
        long returnExpireTiem = 0l;
        Calendar now = Calendar.getInstance();
        if( inputDate != null && !inputDate.equals("")){
            String[] tp = inputDate.substring(0, 10).split("-");
            now.set(Calendar.YEAR, Integer.parseInt(tp[0]));
            now.set(Calendar.MONTH, Integer.parseInt(tp[1])-1);
            now.set(Calendar.DAY_OF_MONTH, Integer.parseInt(tp[2]));
        }
        switch (type){
            case DAILY_UNIQUE:
                now.set(Calendar.HOUR_OF_DAY,23);
                now.set(Calendar.MINUTE,59);
                now.set(Calendar.SECOND,59);
                now.set(Calendar.MILLISECOND,999);
                break;
            case WEEKLY_UNIQUE:
                now.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                now.set(Calendar.HOUR_OF_DAY,23);
                now.set(Calendar.MINUTE,59);
                now.set(Calendar.SECOND,59);
                now.set(Calendar.MILLISECOND,999);
                break;
            case MONTHLY_UNIQUE:
                now.set(Calendar.DAY_OF_MONTH,now.getActualMaximum(Calendar.DAY_OF_MONTH));
                now.set(Calendar.HOUR_OF_DAY,23);
                now.set(Calendar.MINUTE,59);
                now.set(Calendar.SECOND,59);
                now.set(Calendar.MILLISECOND,999);
                break;
        }
        returnExpireTiem = now.getTimeInMillis();
        return returnExpireTiem;
    }
}
