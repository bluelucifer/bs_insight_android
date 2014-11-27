package kr.co.bizspring.insight.lib.util;

import java.util.ArrayList;

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
}
