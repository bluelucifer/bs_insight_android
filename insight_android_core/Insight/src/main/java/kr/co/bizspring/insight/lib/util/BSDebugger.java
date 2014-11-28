package kr.co.bizspring.insight.lib.util;

import android.util.Log;

import kr.co.bizspring.insight.lib.values.StaticValues;

/**
 * Created by caspar on 14. 9. 10.
 */
public class BSDebugger {

    public static boolean isDebug(){
        return StaticValues.BS_MODE_DEBUG;
    }

    public static boolean log(String Tag, String message){
        if(isDebug()) {
            Log.e(Tag, message);
        }
        return isDebug();
    }

    public static boolean logValue(Object value){
        if(isDebug()){
            String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
            String valueName = value.getClass().getName();
            Log.d(StaticValues.DEBUG_TAG,methodName+":"+valueName+":"+value.toString());
        }
        return isDebug();
    }

    public static boolean log(String message){
        try {
            Log.d(StaticValues.DEBUG_TAG,message);
            return isDebug();
        }catch (Exception e){
            return false;
        }
    }
    public static boolean log(Exception e, Object c){
        if(!isDebug()){
			StackTraceElement[] ste = e.getStackTrace();
            Log.e(c.getClass().getName(), "BS_Error Start");
			Log.e(c.getClass().getName(), e.getClass().getName());
			Log.e(c.getClass().getName(), e.getLocalizedMessage());
			for(int i = 0 ; i < ste.length ; i++){
				Log.e(c.getClass().getName(), ste[i].getFileName());
				Log.e(c.getClass().getName(), ste[i].getClassName());
				Log.e(c.getClass().getName(), ste[i].getMethodName());
				Log.e(c.getClass().getName(), String.valueOf(ste[i].getLineNumber()));
				Log.e(c.getClass().getName(), ste[i].toString());
			}
            Log.e(c.getClass().getName(), "BS_Error End");
        }else{
            e.printStackTrace();
        }
        return isDebug();
    }

}
