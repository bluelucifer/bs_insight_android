package kr.co.bizspring.insight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.squareup.tape.QueueFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.bizspring.insight.lib.config.BSConfig;
import kr.co.bizspring.insight.lib.tracker.BSSession;
import kr.co.bizspring.insight.lib.util.BSDebugger;
import kr.co.bizspring.insight.lib.tracker.DocumentManager;
import kr.co.bizspring.insight.lib.tracker.Profiler;
import kr.co.bizspring.insight.lib.util.BSMap;
import kr.co.bizspring.insight.lib.util.BSUtils;
import kr.co.bizspring.insight.lib.values.SignalIndex;
import kr.co.bizspring.insight.lib.values.StaticValues;
import kr.co.bizspring.insight.lib.values.TrackType;
import kr.co.bizspring.insight.service.InsightService;

/**
 * Created by mac on 2014. 10. 20..
 */
public class BSTracker {

    public static BSTracker instance;
    Context mContext;
    Map<String,BSMap> pageMap;
    Map<String,WebView> webViewMap;

    BSConfig bsConfig;

    int trkCounter;

    int dataSendMode;

    int maxDataLength;

    int dataLengthCounter;

    @JavascriptInterface
    public static BSTracker getInstance(){
        if(instance==null){
            instance = new BSTracker();
        }
        return instance;
    }
    protected BSTracker(){
        pageMap = new HashMap<String, BSMap>();
        webViewMap = new HashMap<>();
        trkCounter = 0;
        dataLengthCounter = 0;
    }

    @JavascriptInterface
    public BSTracker init(Context _context){
        this.mContext = _context;
        this.clearInitData();
        this.checkReferrer();
        this.startPage(this.mContext);
        bsConfig = BSConfig.getInstance(mContext);
        dataSendMode = bsConfig.getDataSendMode();
        maxDataLength = bsConfig.getMaxDataSendLength();
        BSDebugger.logAsync(this.mContext,"init");
        return this;
    }

    private void scheduleSendMode() {
        Intent intent = new Intent(mContext, InsightService.class);
        intent.setAction(SignalIndex.SEND_MODE_SCHEDULE);
        intent.putExtra(StaticValues.ST_SEND_TIME,System.currentTimeMillis());
        mContext.startService(intent);

    }

    @JavascriptInterface
    private void clearInitData(){
        // Multi Session으로 값이 연결되면 안되는 항목의 경우 여기에서 초기화 시킨다.
        Profiler.getInstance(mContext).putSessionData(StaticValues.PARAM_MVT1, "");
        Profiler.getInstance(mContext).putSessionData(StaticValues.PARAM_MVT2, "");
        Profiler.getInstance(mContext).putSessionData(StaticValues.PARAM_MVT3, "");
    }

    public void checkReferrer(){
        String referrerString = "";
        Uri uri = null;
        Intent intent = ((Activity)this.mContext).getIntent();
        if( intent != null && intent.getData() != null ) {
            uri = intent.getData();
            referrerString =  uri.toString();
        }
        this.putSessionReferrer(referrerString);
    }

    @JavascriptInterface
    public BSTracker putInitData(String key, String value){
        BSMap currentMap = pageMap.get(Integer.toHexString(System.identityHashCode(this.mContext)));
        currentMap.putInitData(key, value);
        return this;
    }

    @JavascriptInterface
    public void initEnd(){
        Intent intent1 = new Intent(this.mContext, InsightService.class);
        intent1.setAction(SignalIndex.INIT);
        this.mContext.startService(intent1);
        if(dataSendMode==2){
            scheduleSendMode();
        }
    }

    long lastSendTime = 0;

    @JavascriptInterface
    public boolean sendTransaction(){
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
    }

    @JavascriptInterface
    public boolean updateDocument(){
        Intent intent = new Intent(mContext, InsightService.class);
        intent.setAction(SignalIndex.UPDATE_DOCUMENT);
        intent.putExtra(StaticValues.ST_SEND_TIME,System.currentTimeMillis());
        mContext.startService(intent);
        return true;
    }




    public BSMap startPage(Object obj){
        return this.startPage(Integer.toHexString(System.identityHashCode(obj)));
    }

    //현재 페이지에서 사용하는 page데이터 저장. startPage에서 초기화 됨.
    private BSMap currentPageMap = null;

    @JavascriptInterface
    public BSMap startPage(String pageCode){
        BSDebugger.logAsync(this.mContext,"startPage");
        if(pageMap.containsKey(pageCode)){
            this.currentPageMap = pageMap.get(pageCode);
        }else{
            this.currentPageMap = new BSMap(pageCode,this);
        }
        this.currentPageMap.putPageData(StaticValues.START_TIME,System.currentTimeMillis()/1000);
        pageMap.put(pageCode, this.currentPageMap);
        return this.currentPageMap;
    }

//    @JavascriptInterface
    public void endPage(Object obj){
        this.endPage(Integer.toHexString(System.identityHashCode(obj)));
    }

    //todo map size check + counter + create new document
    @JavascriptInterface
    public void endPage(String pageCode){
        BSDebugger.logAsync(this.mContext,"endPage");
        BSMap currentMap = pageMap.get(pageCode);
        Long stTime = (Long)currentMap.getPageData(StaticValues.START_TIME);
        Long edTime = System.currentTimeMillis()/1000;
        Long vs = edTime - stTime;
        currentMap.putPageData(StaticValues.PARAM_VS,vs);
        currentMap.putPageData(StaticValues.PARAM_CS_P_V,Integer.valueOf(1));
        BSDebugger.log(currentMap.getPageDataMap().toString());
        putMap(currentMap.getPageDataMap(),TrackType.TYPE_PAGES);
        currentMap.send();
    }

    /**
     * 환경변수 설정 변수 부분.
     **/
    // #################################################################################
    // GOAL

    @JavascriptInterface
    public void setGoal(String key, int value){
        this.currentPageMap.putGoalData(key,value);
    }

    @JavascriptInterface
    public boolean trkGoal(String key, int value){
        setGoal(key, value);
        return sendTransaction();
    }

    @JavascriptInterface
    public void setAcceptPushReceived( boolean value ){
        if( value ){ // value 가 true 일때, push 메시지 수신동의 한것으로 처리함. g29
               setGoal(StaticValues.PARAM_GOAL_ACCEPT_PUSH,1);
        }
    }

    @JavascriptInterface
    public boolean trkAcceptPushReceived( boolean value ){
        setAcceptPushReceived(value);
        return sendTransaction();
    }

    @JavascriptInterface
    public void setGoalProduct( String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        this.currentPageMap.putGoalData(StaticValues.PARAM_PNC,code);
    }

    @JavascriptInterface
    public void setGoalProductArray( String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        this.currentPageMap.putGoalDataArray(StaticValues.PARAM_PNC,code);
    }

    @JavascriptInterface
    public void setGoalProductType(String type){
        // setGoalProduct함수를 사용해서 이미 pnc데이터가 설정된 경우에만 동작하도록 체크 해야함.
        // 상품코드가 먼저 정의되었을 때에만 전송되도록 조건 추가.
        // ["productType1","productType2"]   =>  productType1;productType2
        if( this.currentPageMap.containsGoalData(StaticValues.PARAM_PNC)){
            this.currentPageMap.putGoalData(StaticValues.PARAM_PNC_TP,type);
        }
    }

    @JavascriptInterface
    public void setGoalProductTypeArray(String[] type){
        // setGoalProduct함수를 사용해서 이미 pnc데이터가 설정된 경우에만 동작하도록 체크 해야함.
        // 상품코드가 먼저 정의되었을 때에만 전송되도록 조건 추가.
        // ["productType1","productType2"]   =>  productType1;productType2
        if( this.currentPageMap.containsGoalData(StaticValues.PARAM_PNC)){
            this.currentPageMap.putGoalDataArray(StaticValues.PARAM_PNC_TP,type);
        }
    }

    @JavascriptInterface
    public void setGoalProductCategory( String code ){
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        this.currentPageMap.putGoalData(StaticValues.PARAM_PNG, code);
    }

    @JavascriptInterface
    public void setGoalProductCategoryArray( String[] code ){
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        this.currentPageMap.putGoalDataArray(StaticValues.PARAM_PNG, code);
    }
    // #################################################################################
    // PRODUCT 함수

    public void setProduct( String code ){  setProduct(code, "");    }

    @JavascriptInterface
    public void setProduct( String code, String name ){
        this.currentPageMap.putPageData(StaticValues.PARAM_PNC,code);
        this.currentPageMap.putPageData(StaticValues.PARAM_PNC_NM, name);
    }

    @Deprecated
    public boolean trkProduct( String code ){   return trkProduct(code,""); }

    @Deprecated
    public boolean trkProduct( String code, String name ){
        setProduct(code, name);
        return sendTransaction();
    }

    @JavascriptInterface
    public void setProductType( String type ){
        if(this.currentPageMap.containsPageData(StaticValues.PARAM_PNC)){
            this.currentPageMap.putPageData(StaticValues.PARAM_PNC_TP,type);
        }
    }


    // #################################################################################
    // PRODUCT CATEGORY 함수

    public void setProductCategory( String code ){
        setProductCategory(code,"");
    }
    @JavascriptInterface
    public void setProductCategory( String code, String name ){
        this.currentPageMap.putPageData(StaticValues.PARAM_PNG,code);
        this.currentPageMap.putPageData(StaticValues.PARAM_PNG_NM,name);
    }
    @Deprecated
    public boolean trkProductCategory( String code ){   return trkProductCategory(code,"");    }
    @Deprecated
    public boolean trkProductCategory( String code, String name ){
        setProductCategory(code, name);
        return sendTransaction();
    }
    // #################################################################################
    // PRODUCT(주문완료) 함수
    @JavascriptInterface
    public void setOrderProduct( String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        this.currentPageMap.putRevenueData(StaticValues.PARAM_PNC,code);
    }
    @JavascriptInterface
    public void setOrderProductArray( String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        this.currentPageMap.putRevenueDataArray(StaticValues.PARAM_PNC,code);
    }

    @JavascriptInterface
    public void setOrderProductType( String type ) {
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productType1","productType2"]   =>  productType1;productType2
        if(this.currentPageMap.containsRevenueData(StaticValues.PARAM_PNC)){
            this.currentPageMap.putRevenueData(StaticValues.PARAM_PNC_TP,type);
        }
    }
    @JavascriptInterface
    public void setOrderProductTypeArray( String[] type ) {
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productType1","productType2"]   =>  productType1;productType2
        if(this.currentPageMap.containsRevenueData(StaticValues.PARAM_PNC)){
            this.currentPageMap.putRevenueDataArray(StaticValues.PARAM_PNC_TP,type);
        }
    }

    @JavascriptInterface
    public void setOrderProductCategory( String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        this.currentPageMap.putRevenueData(StaticValues.PARAM_PNG,code);
    }

    @JavascriptInterface
    public void setOrderProductCategoryArray( String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        this.currentPageMap.putRevenueDataArray(StaticValues.PARAM_PNG,code);
    }

    @JavascriptInterface
    public void setOrderAmount( int value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["10000","20000"]   =>  10000;20000
        this.currentPageMap.putRevenueData(StaticValues.PARAM_AMT,value);
    }
    @JavascriptInterface
    public void setOrderAmountArray( int[] value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["10000","20000"]   =>  10000;20000
        this.currentPageMap.putRevenueDataArray(StaticValues.PARAM_AMT,value);
    }

    @JavascriptInterface
    public void setOrderQuantity( int value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["1","2"]   =>  1;2
        this.currentPageMap.putRevenueData(StaticValues.PARAM_EA,value);
    }

    @JavascriptInterface
    public void setOrderQuantityArray( int[] value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["1","2"]   =>  1;2
        this.currentPageMap.putRevenueDataArray(StaticValues.PARAM_EA,value);
    }
    // #################################################################################
    // 컨텐츠 분석 함수
    @JavascriptInterface
    public void setContents( String value ){
        this.currentPageMap.putPageData(StaticValues.PARAM_CP,value);
    }
    public boolean trkContents( String value ){
        setContents(value);
        return sendTransaction();
    }

    @JavascriptInterface
    public void setPageIdentity( String value ){
        this.currentPageMap.putPageData(StaticValues.PARAM_PI, value);
    }
    public boolean trkPageIdentity( String value ){
        setPageIdentity(value);
        return sendTransaction();
    }
    // #################################################################################
    // 방문자 속성 분석 함수
    @JavascriptInterface
    public void setMember( String isMember ){
        // Profiler.getInstance(mContext).putIsMember(isMember);
        this.putSessionData(StaticValues.PARAM_MBR, isMember);
    }
    @Deprecated
    public boolean trkMember( String isMember ){
        setMember(isMember);
        return sendTransaction();
    }

    @JavascriptInterface
    public void setGender( String gender ){
        // Profiler.getInstance(mContext).putGender(gender);
        this.putSessionData(StaticValues.PARAM_GENDER,gender);
    }
    @Deprecated
    public boolean trkGender( String gender ){
        setGender(gender);
        return sendTransaction();
    }

    @JavascriptInterface
    public void setAge( String age ){
        // Profiler.getInstance(mContext).putAge(age);
        this.putSessionData(StaticValues.PARAM_AGE,age);
    }
    @Deprecated
    public boolean trkAge( String age ){
        setAge(age);
        return sendTransaction();
    }

    @JavascriptInterface
    public void setUserAttribute( String key, String attribute ){
        switch(key){
            case StaticValues.PARAM_UVP1 :   this.putSessionData(StaticValues.PARAM_UVP1, attribute); break;
            case StaticValues.PARAM_UVP2 :   this.putSessionData(StaticValues.PARAM_UVP2, attribute); break;
            case StaticValues.PARAM_UVP3 :   this.putSessionData(StaticValues.PARAM_UVP3, attribute); break;
            case StaticValues.PARAM_UVP4 :   this.putSessionData(StaticValues.PARAM_UVP4, attribute); break;
            case StaticValues.PARAM_UVP5 :   this.putSessionData(StaticValues.PARAM_UVP5, attribute); break;
        }
    }
    @Deprecated
    public boolean trkUserAttribute( String key, String attribute ){
        setUserAttribute(key, attribute);
        return sendTransaction();
    }

    @JavascriptInterface
    public void putSessionData(String key, String value){
        Profiler.getInstance(mContext).putSessionData(key, value);
        updateDocument();
    }

    @JavascriptInterface
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
            }
            if( interval > bsConfig.getReturnVisitDate() ){ // ReturnVisitDate 지난 경우에는 초기화 시킴.
                profiler.putSessionData(itrKey,"");
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
                    if( itrKey.matches("(mat_source|mat_medium|mat_kwd|mat_campaign)") ){
                        profiler.putSessionLongData(StaticValues.PARAM_MAT_UPDATE_TIME, now.getTimeInMillis() );
                        profiler.putSessionData(StaticValues.PARAM_MAT_UPDATE_SID,bsSession.getSession(false)); // 현재의 sid를 저장해둠.
                    }else if( itrKey.matches("(fb_source)") ){
                        profiler.putSessionLongData(StaticValues.PARAM_FB_UPDATE_TIME,  now.getTimeInMillis() );
                        profiler.putSessionData(StaticValues.PARAM_FB_UPDATE_SID,bsSession.getSession(false)); // 현재의 sid를 저장해둠.
                    }else if( itrKey.matches("(utm_source|utm_medium|utm_campaign|utm_term|utm_content)") ){
                        profiler.putSessionLongData(StaticValues.PARAM_UTM_UPDATE_TIME,  now.getTimeInMillis() );
                        profiler.putSessionData(StaticValues.PARAM_UTM_UPDATE_SID,bsSession.getSession(false)); // 현재의 sid를 저장해둠.
                    }
                }
            }
        }
        // ##################################################################################
        // 3. 전환 타입 ( convTp ) 처리.
        String curSid = bsSession.getSession(false);
        String matSid = profiler.getSessionStringData(StaticValues.PARAM_MAT_UPDATE_SID);
        if( profiler.hasReferrerData_MAT() &&
            referrerInfo.size() == 0 &&
            (referrerInfo.containsKey(StaticValues.PARAM_MAT_SOURCE) || referrerInfo.containsKey(StaticValues.PARAM_MAT_MEDIUM) || referrerInfo.containsKey(StaticValues.PARAM_MAT_KWD) || referrerInfo.containsKey(StaticValues.PARAM_MAT_CAMPAIGN)) &&
            !curSid.equals(matSid) ){
            // 간접전환.
            profiler.putSessionIntData(StaticValues.PARAM_CONV_TP, StaticValues.MAT_CONVERSION_TP_NON_DIRECT);
        }else{
            // 직접전환.
            profiler.putSessionIntData(StaticValues.PARAM_CONV_TP, StaticValues.MAT_CONVERSION_TP_DIRECT);
        }
        return this;
    }
    // #################################################################################
    // 사용자 정의 분석 함수
    @JavascriptInterface
    public void setCustomMvtTag( String key, String mvtValue ){
        this.currentPageMap.putPageData(key, mvtValue);
    }

    /**
     * Map 방식 환경변수 설정 부분.
     **/
//    public BSMap putData(String key, Object value){
//        BSMap bsmap = new BSMap();
//        return bsmap.putData(key, value);
//    }
    public BSTracker putMap(final HashMap<String,Object> map, final TrackType type){
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
        AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    QueueFile targetQueue = new QueueFile(new File(mContext.getFilesDir().getPath()+type.filePrefix()+DocumentManager.getLastDocumentID(mContext)));
                    JSONObject targetJson = new JSONObject();
                    try {
                        Iterator<String> itr = map.keySet().iterator();
                        targetJson.put(StaticValues.PARAM_VT_TZ, BSConfig.getCurrentDateString());
                        while (itr.hasNext()){
                            String key = itr.next();
                            targetJson.put(key,map.get(key));
                        }
                        targetQueue.add(targetJson.toString().getBytes());
                        targetQueue.close();
                        updateDocument();
                    } catch (JSONException e) {
                        BSDebugger.log(e, this);
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
        task.execute();
        return this;
    }

    private void sendNewDocSignal() {
        Intent intent = new Intent(mContext, InsightService.class);
        intent.setAction(SignalIndex.CREATE_NEW_DOCUMENT);
        intent.putExtra(StaticValues.ST_SEND_TIME,System.currentTimeMillis());
        mContext.startService(intent);
        dataLengthCounter = 0;
    }

    @JavascriptInterface
    public void putPageCP(Object obj,String cpString){
        putPageParam(obj, StaticValues.PARAM_CP, cpString);
    }
    @JavascriptInterface
    public void putPagePI(Object obj,String piString){
        putPageParam(obj, StaticValues.PARAM_PI, piString);
    }
    @JavascriptInterface
    public void putPagePncTp(Object obj,String targetString){
        putPageParam(obj, StaticValues.PARAM_PNC_TP, targetString);
    }
    @JavascriptInterface
    public void putPagePnc(Object obj,String targetString){
        putPageParam(obj,StaticValues.PARAM_PNC,targetString);
    }
    @JavascriptInterface
    public void putPagePncName(Object obj,String targetString){
        putPageParam(obj,StaticValues.PARAM_PNC_NM,targetString);
    }
    @JavascriptInterface
    public void putPagePng(Object obj,String targetString){
        putPageParam(obj,StaticValues.PARAM_PNG,targetString);
    }
    @JavascriptInterface
    public void putPagePngName(Object obj,String targetString){
        putPageParam(obj,StaticValues.PARAM_PNG_NM,targetString);
    }
    @JavascriptInterface
    public void putPageMTV(Object obj,int id,String targetString){
        putPageParam(obj,StaticValues.PARAM_MTV+String.valueOf(id),targetString);
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

    @JavascriptInterface
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
        targetWebview.addJavascriptInterface(BSTracker.this,StaticValues.BS_WEB_TRACKER);

    }
}
