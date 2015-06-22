package kr.co.wisetracker.insight.lib.util;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import kr.co.wisetracker.insight.WiseTracker;
import kr.co.wisetracker.insight.lib.values.StaticValues;

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
    public static long getExpireLongTimeForUniVt(long recentVisitPtm, int type){
        long returnExpireTiem = 0l;
        Calendar now = Calendar.getInstance();
        if( recentVisitPtm > 0 ){
            now.setTimeInMillis(recentVisitPtm*1000);
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

    /**
     * 두개의 시간을 입력받아 그 차이를 일수(int)로 return하는 메소드
     * @param diffDate1 비교시간1
     * @param diffDate2 비교시간2
     * @return 두 시간의 차(일수)
     */
    public static int getCalDayDiff(long diffDate1, long diffDate2){
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis( diffDate1 );
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis( diffDate2 );
        int returnValue = 0;
        if( calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) ){
            returnValue = calendar1.get(Calendar.DAY_OF_YEAR) - calendar2.get(Calendar.DAY_OF_YEAR);
        }else{
            int yearTerm = calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR);
            returnValue = (yearTerm*365) + (calendar1.get(Calendar.DAY_OF_YEAR) - calendar2.get(Calendar.DAY_OF_YEAR)) + 1;
        }
        return returnValue;
    }
    public static long stringToYyyyMmDdHhMmSsLong(String string){
        Calendar c = Calendar.getInstance();
        try {
            SimpleDateFormat sdfYyyyMmDdHhMmSs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            c.setTime(sdfYyyyMmDdHhMmSs.parse(string));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return c.getTimeInMillis();
    }


    /**
     * 입력받은 Referrer 문자열을 분석한다.
     * @param referrer Referrer 문자열
     * @return 분석된 입출력 항목정보가 담긴 Map
     * **/
    public static Map<String,String> parseReferrer(String referrer){
        Map<String,String> referrerInfo = new HashMap<String,String>();
        try {
            if( referrer != null && !referrer.equals("")){
                // http://www.yardin.com?mat_source=AS0001&mat_medium=AT0001&mat_kwd=키워드&mat_campaign=C0000001&fb_source=&utm_source=&utm_medium=&utm_campaign=
                // referrer 값에 마지막 ?를 찾아서, 이후로 잘라내서 사용함.
                if( referrer.indexOf("?") >= 0 ){
                    referrer = referrer.substring(referrer.lastIndexOf("?")+1,referrer.length() );
                }
                String[] split = referrer.trim().split("&");
                for(String spItem : split ){
                    if( spItem != null && !(spItem.trim()).equals("")){
                        spItem = spItem.trim();
                        if( spItem != null && spItem.indexOf("=") >= 0 ){
                            if( (spItem.substring(0, spItem.indexOf("="))).matches(StaticValues.WT_REFERRER_REGEXP)  ){
                                referrerInfo.put(spItem.substring(0,spItem.indexOf("=")), spItem.substring(spItem.indexOf("=")+1) );
                            }
                        }else{
                          referrerInfo.put(spItem,spItem);
                        }
                    }
                }
            }
        } catch (Exception e) {
            BSDebugger.log(e);
        }
        if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER","[BSUtil.parseReferrer.referrerInfo] : "+ referrerInfo.toString()); }
        return referrerInfo;
    }

    public static String getSimpleDateFormat(long time){
        Calendar tmp = Calendar.getInstance();
        tmp.setTimeInMillis(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        return sdf.format(tmp.getTime());
    }

    public static String decompress(String zipText){
        byte[] compressed = Base64.decode(zipText,Base64.DEFAULT);
        if (compressed.length > 4)
        {
            GZIPInputStream gzipInputStream = null;
            try {
                gzipInputStream = new GZIPInputStream(
                        new ByteArrayInputStream(compressed, 4,
                                compressed.length - 4));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (int value = 0; value != -1;) {
                    value = gzipInputStream.read();
                    if (value != -1) {
                        baos.write(value);
                    }
                }
                gzipInputStream.close();
                baos.close();
                String sReturn = new String(baos.toByteArray(), "UTF-8");
                return sReturn;
            } catch (IOException e) {
                BSDebugger.log(e);
                return "";
            }
        }
        else
        {
            return "";
        }
    }
}
