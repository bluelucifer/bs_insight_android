package kr.co.wisetracker.insight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
/**
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.applinks.AppLinkData;
import bolts.AppLinks;
**/
import kr.co.wisetracker.insight.lib.values.StaticValues;
import kr.co.wisetracker.insight.service.InsightService;

/**
 * Created by mrcm on 2015-05-27.
 */
public class FaceBookWrapper {

    /****
    // Facebook init
    public static void Initialize( final Context _context ){
        try {
            if (WiseTracker.FLAG_OF_PRINT_LOG) {    Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK", "FacebookSdk.sdkInitialize is calling.");   }
            FacebookSdk.sdkInitialize(_context);
        }catch (Exception e){
        }
    }
    public static void createForActivity(final Activity m){
        try {
            Uri uri = AppLinks.getTargetUrl(m.getIntent());
            AppLinkData appLinkData = AppLinkData.createFromActivity(m);
            if (WiseTracker.FLAG_OF_PRINT_LOG) {
                Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK", appLinkData.toString());
            }
        }catch (Exception e){}
    }
    // Facebook Install Referrer 처리.
    public static void initFacebookSdkReferrer(final Context _context, final BSTracker bstracker ){
        try {
            if( !bstracker.isInstallChecked() ){

                if (WiseTracker.FLAG_OF_PRINT_LOG) {    Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK", "FACEBOOK_ID : " + bstracker.getFacebookIdMeta() ); }
                // Fetch data immediately.
                AppLinkData.fetchDeferredAppLinkData(_context, bstracker.getFacebookIdMeta(), new AppLinkData.CompletionHandler() {
                    @Override
                    public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
                        if (appLinkData != null) {
                            Bundle bundle = appLinkData.getArgumentBundle();
                            if (bundle != null && (bundle.containsKey("target_url") || bundle.containsKey("com.facebook.platform.APPLINK_NATIVE_URL"))) {
                                String t = bundle.getString("target_url");
                                if (t == null || t.equals("")) {
                                    t = bundle.getString("com.facebook.platform.APPLINK_NATIVE_URL");
                                }
                                if (WiseTracker.FLAG_OF_PRINT_LOG) {
                                    Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK", bundle.toString());
                                    Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK_Selected", t);
                                }
                                if (t.matches(".*" + StaticValues.WT_REFERRER_REGEXP + ".*")) {
                                    Intent intent1 = new Intent(_context.getApplicationContext(), InsightService.class);
                                    intent1.setAction(StaticValues.INSTALL_REFERRER);
                                    intent1.setData(Uri.parse(t));
                                    _context.startService(intent1);
                                    if (WiseTracker.FLAG_OF_PRINT_LOG) {
                                        Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK->InsightService.class", t);
                                    }
                                }
                            }
                        } else {
                            if (WiseTracker.FLAG_OF_PRINT_LOG) {
                                Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK", "App Link Data is Null");
                            }
                        }
                    }
                });
            }else{
                if( WiseTracker.FLAG_OF_PRINT_LOG ) {
                    Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK", "App Launching is not first time.");
                }
            }
        }catch(Exception e){
            if( WiseTracker.FLAG_OF_PRINT_LOG ) {
                e.printStackTrace();
            }
        }
    }

    // Facebook startPage
    public static void startPage( final Object _context ){
        try {
            BSTracker bstracker = BSTracker.getInstance();
            if( _context instanceof Activity && !bstracker.getFacebookIdMeta().equals("") ){
                if (WiseTracker.FLAG_OF_PRINT_LOG) {    Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK", "AppEventsLogger.activateApp is calling.");   }
                AppEventsLogger.activateApp((Activity)_context);
            }
        }catch (Exception e) {
        }
    }

    //  Facebook startPage
    public static void endPage( final Object _context ){
        try {
            BSTracker bstracker = BSTracker.getInstance();
            if( _context instanceof Activity && !bstracker.getFacebookIdMeta().equals("") ){
                if (WiseTracker.FLAG_OF_PRINT_LOG) {    Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK", "AppEventsLogger.deactivateApp is calling.");   }
                AppEventsLogger.deactivateApp((Activity)_context);
            }
        }catch (Exception e) {
        }
    }***/
    public static void setFacebookAppLinkData(Context _context, Bundle bundle){
        try {
            if (bundle != null && (bundle.containsKey("target_url") || bundle.containsKey("com.facebook.platform.APPLINK_NATIVE_URL"))) {
                String t = bundle.getString("target_url");
                if (t == null || t.equals("")) {
                    t = bundle.getString("com.facebook.platform.APPLINK_NATIVE_URL");
                }
                if (WiseTracker.FLAG_OF_PRINT_LOG) {
                    Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK", bundle.toString());
                    Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK_Selected", t);
                }
                if (t.matches(".*" + StaticValues.WT_REFERRER_REGEXP + ".*")) {
                    Intent intent1 = new Intent(_context.getApplicationContext(), InsightService.class);
                    intent1.setAction(StaticValues.INSTALL_REFERRER);
                    intent1.setData(Uri.parse(t));
                    _context.startService(intent1);
                    if (WiseTracker.FLAG_OF_PRINT_LOG) {
                        Log.i("DEBUG_WISETRACKER_FACEBOOK_SDK->InsightService.class", t);
                    }
                }
            }
        }catch (Exception e){}
    }
}
