package kr.co.wisetracker.insight.lib.util;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

import kr.co.wisetracker.insight.lib.config.BSConfig;
import kr.co.wisetracker.insight.lib.values.SignalIndex;
import kr.co.wisetracker.insight.lib.values.StaticValues;

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
    public static boolean log(Exception e){
        if(!isDebug()){
            StackTraceElement[] ste = e.getStackTrace();
            Log.e("WiseTracker", "BS_Error Start");
            Log.e("WiseTracker", e.getClass().getName());
            Log.e("WiseTracker", e.getLocalizedMessage());
            for(int i = 0 ; i < ste.length ; i++){
                Log.e("WiseTracker", ste[i].getFileName());
                Log.e("WiseTracker", ste[i].getClassName());
                Log.e("WiseTracker", ste[i].getMethodName());
                Log.e("WiseTracker", String.valueOf(ste[i].getLineNumber()));
                Log.e("WiseTracker", ste[i].toString());
            }
            Log.e("WiseTracker", "BS_Error End");
        }else{
            e.printStackTrace();
        }
        return isDebug();
    }

    public static boolean log(Exception e, Object c){
//        Log.e("WiseTracker", "BS_Error Start");
        if(!isDebug()){
//			StackTraceElement[] ste = e.getStackTrace();
//            Log.e(c.getClass().getName(), "BS_Error Start");
//			Log.e(c.getClass().getName(), e.getClass().getName());
//			Log.e(c.getClass().getName(), e.getLocalizedMessage());
//			for(int i = 0 ; i < ste.length ; i++){
//				Log.e(c.getClass().getName(), ste[i].getFileName());
//				Log.e(c.getClass().getName(), ste[i].getClassName());
//				Log.e(c.getClass().getName(), ste[i].getMethodName());
//				Log.e(c.getClass().getName(), String.valueOf(ste[i].getLineNumber()));
//				Log.e(c.getClass().getName(), ste[i].toString());
//			}
//            Log.e(c.getClass().getName(), "BS_Error End");
        }else{
            e.printStackTrace();
        }
//        Log.e("WiseTracker", "BS_Error End");
        return isDebug();
    }

    public static void logAsync(final Context mContext, final String message) {
        AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                BSConfig config = BSConfig.getInstance(mContext);
                Boolean debugFlag = (Boolean) config.getPrefValue(StaticValues.DEBUG_FLAG,Boolean.class);
                if(debugFlag){
                    SimpleDateFormat sdf=new SimpleDateFormat("[yyyy-MM-dd / HH:mm:ss ] ", Locale.KOREA);
                    Long tempTime = System.currentTimeMillis();
                    sdf.format(tempTime);
                    Intent intent = new Intent();
                    intent.setAction(SignalIndex.LOG_DEBUG);
                    intent.putExtra("message",sdf.format(tempTime)+message);
                    mContext.sendBroadcast(intent);
                }
                return null;
            }
        };
        task.execute();
    }
}
