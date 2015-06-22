package kr.co.wisetracker.insight.receiver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Set;

import kr.co.wisetracker.insight.WiseTracker;
import kr.co.wisetracker.insight.lib.values.SignalIndex;
import kr.co.wisetracker.insight.lib.values.StaticValues;
import kr.co.wisetracker.insight.service.InsightService;

/**
 * Created by caspar on 14. 9. 8.
 */
public class InsightReceiver extends WakefulBroadcastReceiver {

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if(intent.getAction()==null){
            return;
        }
        String action = intent.getAction();
        if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "InsightReceiver : " + intent.getAction() ); }
        if(action.equalsIgnoreCase(SignalIndex.BOOT_COMPLEATE)){
            sendIntent(ctx,intent);
        }
        if(!action.equalsIgnoreCase(StaticValues.INSTALL_REFERRER)){
            return;
        }
        // deeplink
        String referrerString = "";
        if( intent != null ) {
            Uri uri = intent.getData();
            if (uri != null &&
                uri.toString().matches(".*"+StaticValues.WT_REFERRER_REGEXP+".*")
            ) {
                referrerString = uri.toString();
            } else {
                if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER","[InsightReceiver.uri is null ]" ); }
                // ###############################################################################################
                // EXTRA AREA
                Bundle extras = intent.getExtras();
                if( extras != null ) {
                    if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "[InsightReceiver.extras.keySet().toString()] " + extras.keySet().toString()); }
                    String extraItemKey = "";
                    Iterator<String> extrasItr = extras.keySet().iterator();
                    while (extrasItr.hasNext()) {
                        extraItemKey = extrasItr.next();
                        if( WiseTracker.FLAG_OF_PRINT_LOG ){  Log.i("DEBUG_WISETRACKER", "[InsightReceiver.extras." + extraItemKey + " : " +  String.valueOf(extras.get(extraItemKey)) ); }
                        if (extraItemKey.equals(StaticValues.WT_REFERRER_NAME)) {
                            referrerString += String.valueOf(extras.get(extraItemKey));
                        } else if (extraItemKey.equals(StaticValues.PARAM_PUSH_MESSAGE_KEY)) {
                            referrerString += "&ocmp=" + String.valueOf(extras.get(extraItemKey));
                        }
                    }
                    // ###############################################################################################
                    // decodeing
                    for (int cnt = 0; cnt < 2; cnt++) {
                        try {
                            if (referrerString != null && referrerString.indexOf("%") >= 0) {
                                referrerString = URLDecoder.decode(referrerString, "utf-8");
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "[InsightReceiver.referrerString." + referrerString ); }
                }else{
                    if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "[ InsightReceiver.extras.keySet().is empty ] "); }
                }
            }
            sendIntent(ctx,intent);
        }
        /***
        if(intent.getData()!=null){
            Log.d("data=",intent.getData().toString());
            if(intent.getData().getScheme()!=null){
                Log.d("scheme=",intent.getData().getScheme());
                if(intent.getData().getScheme().equalsIgnoreCase("package")){
                    String dom = intent.getData().toString();
                    if(dom != null){
                        Log.d("PACKAGE=",dom);
                        String packageName = ctx.getApplicationContext().getPackageName();
                        if(dom.endsWith(packageName)){
                            sendIntent(ctx,intent);
                        }else{
                            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", " dom.endsWith(packageName) NOT ELSE "); }
                        }
                    }else{
                        if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", " dom is null "); }
                    }
                }else{
                    if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", " intent.getData().getScheme() not equals "); }
                }
            }else{
                if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", " intent.getData().getScheme() is null "); }
            }
        }else{
           if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", " intent.getData() is null "); }
        }
        ****/
    }

    private void sendIntent(Context ctx, Intent intent) {
        Intent intent1 = new Intent(ctx, InsightService.class);
        intent1.setAction(intent.getAction());
        intent1.setData(intent.getData());
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            intent1.putExtras(bundle);
        }
        Set<String>category = intent.getCategories();
        if(category!=null) {
            for (String key : category) {
                intent1.addCategory(key);
            }
        }
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN){
            intent1.setClipData(intent.getClipData());
        }
        intent1.setData(intent.getData());
        intent1.setFlags(intent.getFlags());
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
            intent1.setSelector(intent.getSelector());
        }
        intent1.setSourceBounds(intent.getSourceBounds());
        intent1.setType(intent.getType());

        ctx.startService(intent1);
    }

}
