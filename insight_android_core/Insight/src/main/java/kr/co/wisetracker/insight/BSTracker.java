package kr.co.wisetracker.insight;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import kr.co.wisetracker.insight.lib.squareup.tape.QueueFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.wisetracker.insight.lib.config.BSConfig;
import kr.co.wisetracker.insight.lib.tracker.BSSession;
import kr.co.wisetracker.insight.lib.util.BSDebugger;
import kr.co.wisetracker.insight.lib.tracker.DocumentManager;
import kr.co.wisetracker.insight.lib.tracker.Profiler;
import kr.co.wisetracker.insight.lib.util.BSMap;
import kr.co.wisetracker.insight.lib.util.BSUtils;
import kr.co.wisetracker.insight.lib.values.SignalIndex;
import kr.co.wisetracker.insight.lib.values.StaticValues;
import kr.co.wisetracker.insight.lib.values.TrackType;
import kr.co.wisetracker.insight.service.InsightService;

/**
 * Created by mac on 2014. 10. 20..
 */
public class BSTracker {

    Context mContext;
    Activity mActivity;
    Map<String,BSMap> pageMap;
    Map<String,WebView> webViewMap;

    Map<String,String> pushFilterSet = new HashMap<>();

    BSConfig bsConfig;

    int trkCounter;
    int dataSendMode;
    int maxDataLength;
    int dataLengthCounter;

    boolean flagInit = false;
    public boolean getFlagInit(){
        return this.flagInit;
    }
    private static BSTracker instance;

    @JavascriptInterface
    public static BSTracker getInstance(){
        if(instance==null){
            instance = new BSTracker();
        }
        return instance;
    }



//    private static BSTracker instance = new BSTracker();
//
//    public static BSTracker getInstance(){
//        if(instance == null){
//            instance = new BSTracker();
//        }
//        return instance;
//    }

    private BSTracker(){
        pageMap = new HashMap<String, BSMap>();
        webViewMap = new HashMap<>();
        trkCounter = 0;
        dataLengthCounter = 0;
    }
    // ##############################################################################
    // 내부 메소스
    private void scheduleSendMode() {
        Intent intent = new Intent(mContext, InsightService.class);
        intent.setAction(SignalIndex.SEND_MODE_SCHEDULE);
        intent.putExtra(StaticValues.ST_SEND_TIME, System.currentTimeMillis());
        mContext.startService(intent);
    }
    private void clearInitData(){
        // Multi Session으로 값이 연결되면 안되는 항목의 경우 여기에서 초기화 시킨다.
        Profiler.getInstance(mContext).putSessionData(StaticValues.PARAM_MVT1, "");
        Profiler.getInstance(mContext).putSessionData(StaticValues.PARAM_MVT2, "");
        Profiler.getInstance(mContext).putSessionData(StaticValues.PARAM_MVT3, "");
    }

    public void checkReferrer(Intent intent) {
        String referrerString = "";
        // deeplink
        if( intent != null ){
            Uri uri = intent.getData();
            if( uri != null &&
                uri.toString().matches(".*"+StaticValues.WT_REFERRER_REGEXP+".*")
            ){
                referrerString = uri.toString();
            }else{
                // ###############################################################################################
                // EXTRA AREA
                Bundle extras = intent.getExtras();
                if( extras != null ){
                    String extraItemKey = "";
                    Iterator<String> extrasItr = extras.keySet().iterator();
                    while( extrasItr.hasNext() ){
                        extraItemKey = extrasItr.next();
                        if( WiseTracker.FLAG_OF_PRINT_LOG ){  Log.i("DEBUG_WISETRACKER", "[BSTracker.extras."+extraItemKey+" : " + String.valueOf(extras.get(extraItemKey)) ); }
                        if( extraItemKey.equals(StaticValues.WT_REFERRER_NAME) ){
                            referrerString += String.valueOf(extras.get(extraItemKey));
                        }else if( extraItemKey.equals(StaticValues.PARAM_PUSH_MESSAGE_KEY) ){
                            referrerString += "&ocmp="+String.valueOf(extras.get(extraItemKey));
                        }
                    }
                    if(intent.hasExtra(StaticValues.PARAM_PUSH_MESSAGE_KEY)){
                        String ocmp = intent.getStringExtra(StaticValues.PARAM_PUSH_MESSAGE_KEY);
                        referrerString += "&ocmp="+ocmp;
                    }
                }else{
                    if( WiseTracker.FLAG_OF_PRINT_LOG ){  Log.i("DEBUG_WISETRACKER", "[BSTracker.extras.keySet().is empty ] "); }
                }
            }
        }else{
            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER","[BSTracker.intent null]" ); }
        }
        // ###############################################################################################
        // decodeing
        for( int cnt = 0; cnt < 2; cnt++){
            try {
                if( referrerString != null && referrerString.indexOf("%") >= 0 ){
                    referrerString = URLDecoder.decode(referrerString,"utf-8");
                }
            }catch (UnsupportedEncodingException e){e.printStackTrace();}
        }
        if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "[ BSTracker.referrerString ] at checkReferrer() : " + referrerString); }
        this.putSessionReferrer(referrerString);
    }
    public void checkReferrer(){
        Intent intent = this.mActivity.getIntent();
        this.checkReferrer(intent);
    }

    public boolean updateDocument(){
        Intent intent = new Intent(mContext, InsightService.class);
        intent.setAction(SignalIndex.UPDATE_DOCUMENT);
        intent.putExtra(StaticValues.ST_SEND_TIME, System.currentTimeMillis());
        mContext.startService(intent);
        return true;
    }
    // ##############################################################################
    // WiseTracker 매핑 메소스
    public BSTracker initStart(Context _context){
        this.mContext = _context.getApplicationContext();
        this.mActivity = (Activity)_context;
        if(flagInit == false){
            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "BSTracker.initStart() is calling "); }
            this.clearInitData();
            bsConfig = BSConfig.getInstance(mContext);
            dataSendMode = bsConfig.getDataSendMode();
            maxDataLength = bsConfig.getMaxDataSendLength();
            BSDebugger.logAsync(this.mContext, "init");
        }else{
            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "BSTracker.initStart() had already finished. "); }
        }
        this.checkReferrer();
        this.startPage(this.mActivity);
        return this;
    }
    public BSTracker init(Context _context){
        initStart(_context);
        initEnd();
        return this;
    }

    public BSTracker putInitData(String key, String value){
        putSessionData(key,value);
//        BSMap currentMap = pageMap.get(Integer.toHexString(System.identityHashCode(this.mContext)));
//        currentMap.putInitData(key, value);
        return this;
    }
    public void initEnd(){
        if( !flagInit ){
            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "BSTracker.initEnd() is calling "); }
            Intent intent1 = new Intent(this.mContext, InsightService.class);
            intent1.setAction(SignalIndex.INIT);
            this.mContext.startService(intent1);
            if(dataSendMode==2){
                scheduleSendMode();
            }
            Intent intent = this.mActivity.getIntent();
            updatePush(intent);
            flagInit = true;
        }else{
            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "BSTracker.initEnd() had already finished "); }
        }
    }
    long lastSendTime = 0;
    public boolean sendTransaction(){
        if( mContext != null ){
            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "[sendTransaction called] : " + this.currentPageMap.getDataString() ); }
            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "[sendTransaction flagInit ] : " + this.flagInit ); }
            long currentTime = System.currentTimeMillis();
            if(currentTime-lastSendTime<3000){
                lastSendTime = currentTime;
                return false;
            }
            lastSendTime = currentTime;
            Intent intent = new Intent(mContext, InsightService.class);
            intent.setAction(SignalIndex.SEND_TRANSACTION);
            mContext.startService(intent);
            return true;
        }else{
            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "BSTracker.sendTransaction mContext is Null !! "); }
            return false;
        }
    }

    //현재 페이지에서 사용하는 page데이터 저장. startPage에서 초기화 됨.
    private BSMap currentPageMap = null;
    public BSMap getCurrentPageMap(){
        return this.currentPageMap;
    }
    public BSMap getCurrentPageMapById(Object id){
        return this.pageMap.get(id);
    }
    public BSMap startPage(Object obj){
        return this.startPage(Integer.toHexString(System.identityHashCode(obj)));
    }
    public BSMap startPage(String pageCode){
        if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER","StartPage : " + pageCode );  BSDebugger.logAsync(this.mContext,"startPage"); }
        if(pageMap.containsKey(pageCode)){
            this.currentPageMap = pageMap.get(pageCode);
        }else{
            this.currentPageMap = new BSMap(pageCode,this);
        }
        this.currentPageMap.putPageData(StaticValues.START_TIME,System.currentTimeMillis()/1000);
        pageMap.put(pageCode, this.currentPageMap);
        return this.currentPageMap;
    }

    public void updatePush(Intent intent) {
        pushFilterSet.put(StaticValues.PARAM_PUSH_MESSAGE_KEY,StaticValues.PARAM_PUSH_NO);
        HashMap<String,Object> pushMap = new HashMap<>();
        //Intent가 들어오면 점검
        Bundle bundle = intent.getExtras();
        if(bundle == null){
            //bundle이 없는 경우
            return;
        }
        if(!bundle.containsKey(StaticValues.PARAM_PUSH_MESSAGE_KEY)){
            //ocmp코드가 없음.
            return;
        }
        Set keySet = bundle.keySet();
        Iterator<String> itr = keySet.iterator();
        while (itr.hasNext()){
            String key = itr.next();
            Object value = bundle.get(key);
            if(pushFilterSet.containsKey(key)){
                key = pushFilterSet.get(key);
            }
            pushMap.put(key,value);
        }
        pushMap.put(StaticValues.PUSH_PERIOD,bsConfig.getReturnVisitDate());
        putMap(pushMap,TrackType.Type_PUSH);
    }

    public void endPage(Object obj){
        this.endPage(Integer.toHexString(System.identityHashCode(obj)));
    }
    //todo map size check + counter + create new document
    public void endPage(String pageCode){
        if( WiseTracker.FLAG_OF_PRINT_LOG ){  Log.i("DEBUG_WISETRACKER","endPage : " + pageCode ); }
        BSDebugger.logAsync(this.mContext,"endPage");
        if( pageMap != null && pageMap.containsKey(pageCode)){
            BSMap currentMap = pageMap.get(pageCode);
            Long stTime = (Long)currentMap.getPageData(StaticValues.START_TIME);
            Long edTime = System.currentTimeMillis()/1000;
            Long vs = edTime - stTime;
            currentMap.putPageData(StaticValues.PARAM_VS,vs);
            currentMap.putPageData(StaticValues.PARAM_CS_P_V,Integer.valueOf(1));
            BSDebugger.log(currentMap.getPageDataMap().toString());
            putMap(currentMap.getPageDataMap(),TrackType.TYPE_PAGES);
            currentMap.send();
        }else{
            if( WiseTracker.FLAG_OF_PRINT_LOG ){  Log.i("DEBUG_WISETRACKER","endPage : pageMap Not Found Key : " + pageCode ); }
        }
    }
    public void putSessionData(String key, String value){
        Profiler.getInstance(mContext).putSessionData(key, value);
        updateDocument();
    }
    public BSTracker putSessionReferrer(String referrer){
        /****
         * 1. referrer가 전송되어 오면 값을 업데이트 한다.
         * 2. 만약 업데이트되어질 값이 없는 경우에는 config.xml파일에 정의된 14일 만료 일자를 체크한다.
         * 3. 전환 타입 분석을 위해서 mat가 업데이트 된 시점의 sid를 저장해둘 필요가 있다.
         */
        // ############################################################################
        // 1. 기존에 저장된 리퍼러의 만료 시간을 체크하여, 해당 값을 초기화 시킨다.
        Map<String,Object> latestReferrerMap = Profiler.getInstance(mContext).getLatestReferrerInfo();
        Iterator<String> itr = latestReferrerMap.keySet().iterator();
        String itrKey = "";
        Calendar now = Calendar.getInstance();
        Profiler profiler = Profiler.getInstance(mContext);
        BSSession bsSession = BSSession.getInstance(mContext,BSConfig.getInstance(mContext));
        BSConfig bsConfig = BSConfig.getInstance(mContext);
        while( itr.hasNext() ){
            itrKey = itr.next();
            // 전달된 파라미터가 없는 경우에는 만료일자 체크로직을 수행.
            int interval = 0;
            if( itrKey.matches("(mat_source|mat_medium|mat_kwd|mat_campaign)") && profiler.getSessionLongData(StaticValues.PARAM_MAT_UPDATE_TIME) > 0 ){
                interval = BSUtils.getCalDayDiff(now.getTimeInMillis(),  profiler.getSessionLongData(StaticValues.PARAM_MAT_UPDATE_TIME) );

            }else if( itrKey.matches("(fb_source)") && profiler.getSessionLongData(StaticValues.PARAM_FB_UPDATE_TIME) > 0  ){
                interval = BSUtils.getCalDayDiff(now.getTimeInMillis(), profiler.getSessionLongData(StaticValues.PARAM_FB_UPDATE_TIME) );

            }else if( itrKey.matches("(utm_source|utm_medium|utm_campaign|utm_term|utm_content)") && profiler.getSessionLongData(StaticValues.PARAM_UTM_UPDATE_TIME) > 0 ){
                interval = BSUtils.getCalDayDiff(now.getTimeInMillis(),  profiler.getSessionLongData(StaticValues.PARAM_UTM_UPDATE_TIME) );

            }else if( itrKey.matches("(ocmp)") && profiler.getSessionLongData(StaticValues.PARAM_PUSH_MESSAGE_UPDATE_TIME) > 0  ){
                interval = BSUtils.getCalDayDiff(now.getTimeInMillis(),  profiler.getSessionLongData(StaticValues.PARAM_PUSH_MESSAGE_UPDATE_TIME) );
            }
            if( interval > bsConfig.getReturnVisitDate() ){ // ReturnVisitDate 지난 경우에는 초기화 시킴.
                profiler.putSessionData(itrKey,"");
                // #####################################################
                // trace data 초기화
                if( itrKey.equals(StaticValues.PARAM_MAT_SOURCE)){
                    profiler.clearSessionTrace(StaticValues.PARAM_MAT_SOURCE_TRACE);
                }
                else if( itrKey.equals(StaticValues.PARAM_MAT_CAMPAIGN)){
                    profiler.clearSessionTrace(StaticValues.PARAM_MAT_CAMPAIGN_TRACE);
                }
            }
        }
        // ###################################################################################
        // 2.현재 리퍼러를 통해서 전달된 값이 있을 경우, 값을 업데이트 한다.
        Map<String,String> referrerInfo = BSUtils.parseReferrer(referrer);
        if( referrerInfo != null && referrerInfo.size() > 0 ){
            itr = latestReferrerMap.keySet().iterator();
            while( itr.hasNext() ){
                itrKey = itr.next();
                // 전달된 업데이트 정보가 있다면, 갱신을 수행한다
                if( referrerInfo.containsKey(itrKey) && !((String)referrerInfo.get(itrKey)).equals("") ){
                    profiler.putSessionData(itrKey, (String)referrerInfo.get(itrKey) );
                    // ###########################################################################################################################
                    // 1.전달된 캠페인 정보 셋팅
                    if( itrKey.matches("(mat_source|mat_medium|mat_kwd|mat_campaign)") ){
                        profiler.putSessionLongData(StaticValues.PARAM_MAT_UPDATE_TIME, now.getTimeInMillis() );
                        profiler.putSessionData(StaticValues.PARAM_MAT_UPDATE_SID,bsSession.getSession(false)); // 현재의 sid를 저장해둠.
                    }else if( itrKey.matches("(fb_source)") ){
                        profiler.putSessionLongData(StaticValues.PARAM_FB_UPDATE_TIME,  now.getTimeInMillis() );
                        profiler.putSessionData(StaticValues.PARAM_FB_UPDATE_SID,bsSession.getSession(false)); // 현재의 sid를 저장해둠.
                    }else if( itrKey.matches("(utm_source|utm_medium|utm_campaign|utm_term|utm_content)") ){
                        profiler.putSessionLongData(StaticValues.PARAM_UTM_UPDATE_TIME,  now.getTimeInMillis() );
                        profiler.putSessionData(StaticValues.PARAM_UTM_UPDATE_SID,bsSession.getSession(false)); // 현재의 sid를 저장해둠.
                    }else if( itrKey.matches("(ocmp)") ){
                        profiler.putSessionLongData(StaticValues.PARAM_PUSH_MESSAGE_UPDATE_TIME,  now.getTimeInMillis() );
                        profiler.putSessionData(StaticValues.PARAM_PUSH_MESSAGE_UPDATE_SID,bsSession.getSession(false)); // 현재의 sid를 저장해둠.
                    }
                    // ###########################################################################################################################
                    // 2.전달된 캠페인 정보 셋팅, trace정보를 추가 저장함.
                    if( itrKey.matches("(mat_source|mat_campaign)")){
                        if( itrKey.equals(StaticValues.PARAM_MAT_SOURCE)){
                            profiler.addSessionTrace(StaticValues.PARAM_MAT_SOURCE_TRACE,(String)referrerInfo.get(itrKey));
                        }else if( itrKey.equals(StaticValues.PARAM_MAT_CAMPAIGN)){
                            profiler.addSessionTrace(StaticValues.PARAM_MAT_CAMPAIGN_TRACE,(String)referrerInfo.get(itrKey));
                        }
                    }
                }
            }
        }
        // ##################################################################################
        // 3. 전환 타입 ( convTp ) 처리.
        String curSid = bsSession.getSession(false);
        String matSid = profiler.getSessionStringData(StaticValues.PARAM_MAT_UPDATE_SID);
        if( profiler.hasReferrerData_MAT() && referrerInfo.size() == 0 && !curSid.equals(matSid) ){
            // 간접전환.
            profiler.putSessionIntData(StaticValues.PARAM_CONV_TP, StaticValues.MAT_CONVERSION_TP_NON_DIRECT);
        }else{
            // 직접전환.
            profiler.putSessionIntData(StaticValues.PARAM_CONV_TP, StaticValues.MAT_CONVERSION_TP_DIRECT);
        }
        return this;
    }



    /**
     * Map 방식 환경변수 설정 부분.
     **/
//    public BSMap putData(String key, Object value){
//        BSMap bsmap = new BSMap();
//        return bsmap.putData(key, value);
//    }
    public BSTracker putMap(final HashMap<String,Object> map, final TrackType type){
        try{
            if(dataLengthCounter>bsConfig.getMaxDataSendLength()){
                sendNewDocSignal();
            }else{
                if(type!=TrackType.TYPE_PAGES){
                    dataLengthCounter++;
                }else{
                    if(map.keySet().size()>4){
                        dataLengthCounter++;
                    }
                }
            }
            BSDebugger.logAsync(mContext,"putMap");
            if(type == TrackType.TYPE_REVENUE){
                Profiler.getInstance(mContext).notifyLtrvnc();
            }
            BSDebugger.logValue(type);
            BSSession.getInstance(mContext,BSConfig.getInstance(mContext)).getSession(true);
            AsyncTask<JSONObject,Void,Void> task = new AsyncTask<JSONObject, Void, Void>() {
                @Override
                protected Void doInBackground(JSONObject... targetJson) {
                    try {
                            QueueFile targetQueue = new QueueFile(new File(mContext.getFilesDir().getPath()+"/wisetracker/"+type.filePrefix()+DocumentManager.getLastDocumentID(mContext)+ ".WiseTracker"));
                            /**
                             JSONObject targetJson = new JSONObject();
                            Iterator<String> itr = map.keySet().iterator();
                            targetJson.put(StaticValues.PARAM_VT_TZ, BSConfig.getCurrentDateString());
                            while (itr.hasNext()){
                                String key = itr.next();
                                targetJson.put(key,map.get(key));
                            }
                            **/
                            for( JSONObject obj : targetJson ){
                                targetQueue.add(obj.toString().getBytes());
                            }
                            targetQueue.close();
                            updateDocument();
                            if(type == TrackType.TYPE_REVENUE){
                                Profiler.getInstance(mContext).markRevenue();
                            }

                    } catch (IOException e) {
                        BSDebugger.log(e, this);
                    }
                    if(dataSendMode==1||trkCounter<10){
                        trkCounter++;
                        sendTransaction();
                    }
                    return null;
                }
            };
            // #############################################################################
            // create JSONObject
            JSONObject targetJson = new JSONObject();
            Iterator<String> itr = map.keySet().iterator();
            targetJson.put(StaticValues.PARAM_VT_TZ, BSConfig.getCurrentDateString());
            while (itr.hasNext()){
                String key = itr.next();
                targetJson.put(key,map.get(key));
            }
            task.execute(targetJson);
            // #############################################################################
        }catch(Exception e){
            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER_EXCEPTION","BSTracker.putMap : " + e.toString()); }
        }
        return this;
    }

    private void sendNewDocSignal() {
        Intent intent = new Intent(mContext, InsightService.class);
        intent.setAction(SignalIndex.CREATE_NEW_DOCUMENT);
        intent.putExtra(StaticValues.ST_SEND_TIME,System.currentTimeMillis());
        mContext.startService(intent);
        dataLengthCounter = 0;
    }

    public void putPageParam(Object obj, String ParamName,Object paramValue){
        String pageCode;
        if(obj instanceof String){
            pageCode = (String)obj;
        }else{
            pageCode =Integer.toHexString(System.identityHashCode(obj));
        }
        BSMap currentMap = pageMap.get(pageCode);
        currentMap.putPageData(ParamName,paramValue);
        currentMap.putPageData(StaticValues.PARAM_EXTRA, Boolean.TRUE);
    }

    public BSMap builder(Object obj){
        String pageCode;
        if(obj instanceof String){
            pageCode = (String)obj;
        }else{
            pageCode =Integer.toHexString(System.identityHashCode(obj));
        }
        BSMap map;
        if(pageMap.containsKey(pageCode)){
            map = pageMap.get(pageCode);
        }else{
            map = new BSMap(pageCode,this);
            pageMap.put(pageCode,map);
        }
        return map;
    }
    public BSMap builder(String obj) {
        String pageCode;
        pageCode = obj;
        BSMap map;
        if(pageMap.containsKey(pageCode)){
            map = pageMap.get(pageCode);
        }else{
            map = new BSMap(pageCode,this);
            pageMap.put(pageCode,map);
        }
        return map;
    }



    @SuppressLint("NewApi")
    public void setWebView(WebView webView) {
        String webViewCode = Integer.toHexString(System.identityHashCode(webView));
        WebView targetWebview = null;
        if(!webViewMap.containsKey(webViewCode)){
            webViewMap.put(webViewCode,webView);
        }

        targetWebview = webViewMap.get(webViewCode);
        targetWebview.getSettings().setJavaScriptEnabled(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            targetWebview.setWebContentsDebuggingEnabled(true);
        }
        targetWebview.addJavascriptInterface(WiseTracker.getTracker(),StaticValues.BS_WEB_TRACKER);
    }
    String webUrl = null;
    public void injectFinished(WebView webView) {
        String url = webView.getUrl();
        // String javascriptString = "javascript:(function(){var builder;var doc;try{doc=window.location.href;console.log(doc);builder=WiseTracker.startPage(doc);window.onunload=function(){try{WiseTracker.endPage(doc);console.log('end'+doc);}catch(err2){console.log('err2');}}}catch(err){console.log('err1')}}())";
        String overideString =
                "javascript:(function(){" +
                        "window.WiseTracker.PRODUCT_SUB_TYPE1=\""+WiseTracker.PRODUCT_SUB_TYPE1+"\";" +
                        "window.WiseTracker.PRODUCT_SUB_TYPE2=\""+WiseTracker.PRODUCT_SUB_TYPE2+"\";" +
                        "window.WiseTracker.PRODUCT_SUB_TYPE3=\""+WiseTracker.PRODUCT_SUB_TYPE3+"\";" +
                        "window.WiseTracker.PRODUCT_SUB_TYPE4=\""+WiseTracker.PRODUCT_SUB_TYPE4+"\";" +
                        "window.WiseTracker.PRODUCT_SUB_TYPE5=\""+WiseTracker.PRODUCT_SUB_TYPE5+"\";" +
                        "window.WiseTracker.PRODUCT_SUB_TYPE6=\""+WiseTracker.PRODUCT_SUB_TYPE6+"\";" +
                        "window.WiseTracker.PRODUCT_SUB_TYPE7=\""+WiseTracker.PRODUCT_SUB_TYPE7+"\";" +
                        "window.WiseTracker.PRODUCT_SUB_TYPE8=\""+WiseTracker.PRODUCT_SUB_TYPE8+"\";" +
                        "window.WiseTracker.PRODUCT_SUB_TYPE9=\""+WiseTracker.PRODUCT_SUB_TYPE9+"\";" +
                        "window.WiseTracker.MEMBER=\""+WiseTracker.MEMBER+"\";" +
                        "window.WiseTracker.NON_MEMBER=\""+WiseTracker.NON_MEMBER+"\";" +
                        "window.WiseTracker.GENDER_MALE=\""+WiseTracker.GENDER_MALE+"\";" +
                        "window.WiseTracker.GENDER_FEMALE=\""+WiseTracker.GENDER_FEMALE+"\";" +
                        "window.WiseTracker.GENDER_ETC=\""+WiseTracker.GENDER_ETC+"\";" +
                        "window.WiseTracker.AGE_0_TO_9=\""+WiseTracker.AGE_0_TO_9+"\";" +
                        "window.WiseTracker.AGE_10_TO_19=\""+WiseTracker.AGE_10_TO_19+"\";" +
                        "window.WiseTracker.AGE_20_TO_29=\""+WiseTracker.AGE_20_TO_29+"\";" +
                        "window.WiseTracker.AGE_30_TO_39=\""+WiseTracker.AGE_30_TO_39+"\";" +
                        "window.WiseTracker.AGE_40_TO_49=\""+WiseTracker.AGE_40_TO_49+"\";" +
                        "window.WiseTracker.AGE_50_TO_59=\""+WiseTracker.AGE_50_TO_59+"\";" +
                        "window.WiseTracker.AGE_60_OVER=\""+WiseTracker.AGE_60_OVER+"\";" +
                        "window.WiseTracker.GOAL_1=\""+WiseTracker.GOAL_1+"\";" +
                        "window.WiseTracker.GOAL_2=\""+WiseTracker.GOAL_2+"\";" +
                        "window.WiseTracker.GOAL_3=\""+WiseTracker.GOAL_3+"\";" +
                        "window.WiseTracker.GOAL_4=\""+WiseTracker.GOAL_4+"\";" +
                        "window.WiseTracker.GOAL_5=\""+WiseTracker.GOAL_5+"\";" +
                        "window.WiseTracker.GOAL_6=\""+WiseTracker.GOAL_6+"\";" +
                        "window.WiseTracker.GOAL_7=\""+WiseTracker.GOAL_7+"\";" +
                        "window.WiseTracker.GOAL_8=\""+WiseTracker.GOAL_8+"\";" +
                        "window.WiseTracker.GOAL_9=\""+WiseTracker.GOAL_9+"\";" +
                        "window.WiseTracker.GOAL_10=\""+WiseTracker.GOAL_10+"\";" +
                        "window.WiseTracker.CUSTOM_MVT_TAG_1=\""+WiseTracker.CUSTOM_MVT_TAG_1+"\";" +
                        "window.WiseTracker.CUSTOM_MVT_TAG_2=\""+WiseTracker.CUSTOM_MVT_TAG_2+"\";" +
                        "window.WiseTracker.CUSTOM_MVT_TAG_3=\""+WiseTracker.CUSTOM_MVT_TAG_3+"\";" +
                        "window.WiseTracker.USER_ATTRIBUTE_1=\""+WiseTracker.USER_ATTRIBUTE_1+"\";" +
                        "window.WiseTracker.USER_ATTRIBUTE_2=\""+WiseTracker.USER_ATTRIBUTE_2+"\";" +
                        "window.WiseTracker.USER_ATTRIBUTE_3=\""+WiseTracker.USER_ATTRIBUTE_3+"\";" +
                        "window.WiseTracker.USER_ATTRIBUTE_4=\""+WiseTracker.USER_ATTRIBUTE_4+"\";" +
                        "window.WiseTracker.USER_ATTRIBUTE_5=\""+WiseTracker.USER_ATTRIBUTE_5+"\";" +
                 "}())";
        webView.loadUrl(overideString);
        if(webUrl!=null){
            endPage(webUrl);
        }
        startPage(url);
        webUrl = url;
//        String javascriptString = "javascript:(function(){var WiseTracker;var wisetrackerDoc;try{WiseTracker=window.WiseTracker;wisetrackerDoc=window.location.href;console.log(wisetrackerDoc);WiseTracker.startPage(wisetrackerDoc);window.onunload=function(){try{WiseTracker.endPage(wisetrackerDoc);console.log('end'+wisetrackerDoc);}catch(err2){console.log('err2');}}}catch(err){console.log('err1');}}())";
//        webView.loadUrl(javascriptString);
        String evalScript = "javascript:(function(){eval(document.getElementById(\"wiseTracker\").innerHTML)}())";
        webView.loadUrl(evalScript);

    }

    public void broadcastInstall(Context mContext,Intent intent) {
        intent.setClass(mContext,InsightService.class);
        mContext.startService(intent);
    }

    public BSTracker initPushSet(String key, String value) {
        //value에 맞는 key 를 역으로 뽑아서 써야함.
        pushFilterSet.put(value,key);
        return this;
    }

    public void injectExtra(Intent orgIntent, Intent targetIntent) {
        Bundle extras = orgIntent.getExtras();
        if(extras == null){
            return;
        }
        targetIntent.putExtras(extras);
    }
    public String getFacebookIdMeta() {
        String metaString = "";
        try {
            ApplicationInfo ai = null;
            ai = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if( bundle.containsKey(StaticValues.META_FACEBOOK_APP_ID) ){
                metaString = bundle.getString(StaticValues.META_FACEBOOK_APP_ID);
            }
        } catch (Exception e) {
            BSDebugger.log(e);
        }
        return metaString;
    }

    public boolean isInstallChecked(){
        boolean installCheck = false; // 인스톨 처리 아직 안되었음.
        try{
            installCheck = (boolean)this.bsConfig.getPrefValue(StaticValues.INSTALL_CHECKED,Boolean.class);
        }catch(Exception e){
        }
        return installCheck;
    }



}
