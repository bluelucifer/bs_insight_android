package kr.co.wisetracker.insight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;
import kr.co.wisetracker.insight.lib.config.BSConfig;
import kr.co.wisetracker.insight.lib.util.BSMap;
import kr.co.wisetracker.insight.lib.values.StaticValues;
import kr.co.wisetracker.insight.service.InsightService;

/**
 * Created by caspar on 2014. 11. 12..
 * @author caspar
 * @since 2014. 11. 12.
 * @version 1.0
 * @see BSTracker
 */
public class WiseTracker {

    public static boolean FLAG_OF_PRINT_LOG = false;

    private static WiseTracker tracker;
    /**
    * 싱글톤 정의
     * 반드시 init 이후 메소드를 콜해야 함.
     * @return Analytics
    */
    public static WiseTracker getTracker(){
        if(tracker==null){
            tracker = new WiseTracker();
        }
        return tracker;
    }

    public static BSTracker getInstance() {
        return BSTracker.getInstance();
    }


    private WiseTracker(){

    }

    private static void getPrintLogMetaValue(Context _context){
        // Log.i 출력 여부 가져오기.
        try {
            ApplicationInfo ai = _context.getPackageManager().getApplicationInfo(_context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            if( bundle.containsKey("WiseTrackerLogState")){
                boolean wiseTrackerLogState = bundle.getBoolean("WiseTrackerLogState");
                if( wiseTrackerLogState ){
                    FLAG_OF_PRINT_LOG = wiseTrackerLogState;
                }
            }
        }catch(Exception e){
        }
    }
    @JavascriptInterface
    public static BSTracker init(Context _context){
        getPrintLogMetaValue(_context);
        // init, initEnd 까지 같이 호출 시키는 함수.
        BSTracker bstracker = WiseTracker.initStart(_context);
        bstracker.initEnd();
        return bstracker;
    }
    @JavascriptInterface
    public static BSTracker initStart(Context _context){
        getPrintLogMetaValue(_context);
        BSTracker bstracker = BSTracker.getInstance().initStart(_context);
        // #########################################################################
        // Facebook sdkInitialize
        /*if( _context instanceof Activity && !bstracker.getFacebookIdMeta().equals("") && !bstracker.getFlagInit() ){
            FaceBookWrapper.Initialize(_context);
            FaceBookWrapper.initFacebookSdkReferrer( _context.getApplicationContext(), bstracker);
        }*/
        // #########################################################################
        return bstracker;
    }
    @JavascriptInterface
    public static BSTracker initPushSet(String key, String value) {
        return BSTracker.getInstance().initPushSet(key, value);
    }

    @JavascriptInterface
    public static void putInitData(String key, String value){
        BSTracker.getInstance().putInitData(key,value);
    }

    public static void checkReferrer(){
        BSTracker.getInstance().checkReferrer();
    }
    public static void checkReferrer(Intent intent){
        BSTracker.getInstance().checkReferrer(intent);
    }

    @JavascriptInterface
    public static void initEnd(){
        BSTracker.getInstance().initEnd();
    }

    @JavascriptInterface
    public static boolean sendTransaction(){
        return BSTracker.getInstance().sendTransaction();
    }
    public static BSMap startPage(Object obj){
        // #########################################################################
        // Facebook sdkInitialize
        //if( obj instanceof Activity ){ FaceBookWrapper.startPage(obj);  }
        // #########################################################################
        return BSTracker.getInstance().startPage(obj);
    }
    @JavascriptInterface
    public static BSMap startPage(String obj){
        return BSTracker.getInstance().startPage(obj);
    }
    public static void endPage(Object obj){
        // #########################################################################
        // Facebook sdkInitialize
        //if( obj instanceof Activity ){ FaceBookWrapper.endPage(obj);  }
        // #########################################################################
        BSTracker.getInstance().endPage(obj);
    }
    @JavascriptInterface
    public static void endPage(String pageCode){
        BSTracker.getInstance().endPage(pageCode);
    }
    public static BSMap builder(Object obj){
        return BSTracker.getInstance().builder(obj);
    }

//    @JavascriptInterface
    public BSMap builder(String obj) {
        return BSTracker.getInstance().builder(obj);
    }
    /***
     * 환경변수 설정 함수  ( 고급 )
     * */
    // ---------------------------------------------------------------------------------------
     @JavascriptInterface
    public static boolean containsGoalData(String key){
        BSMap currentPageMap = BSTracker.getInstance().getCurrentPageMap();
        return currentPageMap.containsGoalData(key);
    }
    @JavascriptInterface
    public static boolean containsGoalDataById(String id, String key){
        return BSTracker.getInstance().getCurrentPageMapById(id).containsGoalData(key);
    }
    // ---------------------------------------------------------------------------------------
    public static BSMap putGoalData(String key, Object value){
        return BSTracker.getInstance().getCurrentPageMap().putGoalData(key,value);
    }
    public static BSMap putGoalDataById(String id, String key, Object value){
        return BSTracker.getInstance().getCurrentPageMapById(id).putGoalData(key,value);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static BSMap putGoalData(String key, String value){
        return BSTracker.getInstance().getCurrentPageMap().putGoalData(key,value);
    }
    @JavascriptInterface
    public static BSMap putGoalDataById(String id, String key, String value){
        return BSTracker.getInstance().getCurrentPageMapById(id).putGoalData(key,value);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static BSMap putGoalDataArray(String key, String[] value){
        return BSTracker.getInstance().getCurrentPageMap().putGoalDataArray(key,value);
    }
    @JavascriptInterface
    public static BSMap putGoalDataArrayById(String id, String key, String[] value){
        return BSTracker.getInstance().getCurrentPageMapById(id).putGoalDataArray(key,value);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static boolean containsPageData(String key){
        BSMap currentPageMap = BSTracker.getInstance().getCurrentPageMap();
        return currentPageMap.containsPageData(key);
    }
    @JavascriptInterface
    public static boolean containsPageDataById(String id, String key){
        return BSTracker.getInstance().getCurrentPageMapById(id).containsPageData(key);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static BSMap putPageData(String key, String value){
        return BSTracker.getInstance().getCurrentPageMap().putPageData(key,value);
    }
    @JavascriptInterface
    public static BSMap putPageDataById(String id, String key, String value){
        return BSTracker.getInstance().getCurrentPageMapById(id).putPageData(key,value);
    }
    // ---------------------------------------------------------------------------------------
    public static BSMap putPageData(String key, Object value){
        return BSTracker.getInstance().getCurrentPageMap().putPageData(key,value);
    }
    public static BSMap putPageDataById(String id, String key, Object value){
        return BSTracker.getInstance().getCurrentPageMapById(id).putPageData(key,value);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static Object getPageData(String key){
        return BSTracker.getInstance().getCurrentPageMap().getPageData(key);
    }
    @JavascriptInterface
    public static Object getPageDataById(String id, String key){
        return BSTracker.getInstance().getCurrentPageMapById(id).getPageData(key);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static String getPageDataString(String key){
        return BSTracker.getInstance().getCurrentPageMap().getPageDataString(key);
    }
    @JavascriptInterface
    public static String getPageDataStringById(String id, String key){
        return BSTracker.getInstance().getCurrentPageMapById(id).getPageDataString(key);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static boolean containsRevenueData(String key){
        BSMap currentPageMap = BSTracker.getInstance().getCurrentPageMap();
        return currentPageMap.containsRevenueData(key);
    }
    @JavascriptInterface
    public static boolean containsRevenueDataById(String id, String key){
        return BSTracker.getInstance().getCurrentPageMapById(id).containsRevenueData(key);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static BSMap putRevenueData(String key, String value){
        return BSTracker.getInstance().getCurrentPageMap().putRevenueData(key,value);
    }
    @JavascriptInterface
    public static BSMap putRevenueDataById(String id, String key, String value){
        return BSTracker.getInstance().getCurrentPageMapById(id).putRevenueData(key,value);
    }
    // ---------------------------------------------------------------------------------------
    public static BSMap putRevenueData(String key, Object value){
        return BSTracker.getInstance().getCurrentPageMap().putRevenueData(key,value);
    }
    public static BSMap putRevenueDataById(String id, String key, Object value){
        return BSTracker.getInstance().getCurrentPageMapById(id).putRevenueData(key,value);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static BSMap putRevenueDataArray(String key, String[] value){
        return BSTracker.getInstance().getCurrentPageMap().putRevenueDataArray(key,value);
    }
    @JavascriptInterface
    public static BSMap putRevenueDataArrayById(String id, String key, String[] value){
        return BSTracker.getInstance().getCurrentPageMapById(id).putRevenueDataArray(key,value);
    }
    // ---------------------------------------------------------------------------------------
    public static BSMap putRevenueDataArray(String key, int[] value){
        return BSTracker.getInstance().getCurrentPageMap().putRevenueDataArray(key,value);
    }
    public static BSMap putRevenueDataArrayById(String id, String key, int[] value){
        return BSTracker.getInstance().getCurrentPageMapById(id).putRevenueDataArray(key,value);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void putSessionData(String key, String value){
        BSTracker.getInstance().getCurrentPageMap().putSessionData(key,value);
        BSTracker.getInstance().updateDocument();
    }
    @JavascriptInterface
    public static BSMap putSessionReferrer(String referrer){
        return BSTracker.getInstance().getCurrentPageMap().putSessionReferrer(referrer);
    }
    /**
     * 환경변수 설정 함수  ( 고급 )
     **/
    // #################################################################################
    // GOAL
    @JavascriptInterface
    public static void setGoal(String key, int value){
        WiseTracker.putGoalData(key,value);
    }
    @JavascriptInterface
    public static void setGoalById(String id, String key, int value){
        WiseTracker.putGoalDataById(id, key,value);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setAcceptPushReceived( boolean value ){
        if( value ){ // value 가 true 일때, push 메시지 수신동의 한것으로 처리함. g29
            WiseTracker.putGoalData(StaticValues.PARAM_GOAL_ACCEPT_PUSH,1);
        }
    }
    @JavascriptInterface
    public static void setAcceptPushReceivedById( String id, boolean value ){
        if( value ){ // value 가 true 일때, push 메시지 수신동의 한것으로 처리함. g29
            WiseTracker.putGoalDataById(id,StaticValues.PARAM_GOAL_ACCEPT_PUSH,1);
        }
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setGoalCustomMvtTag( String key, String mvtValue ){
        WiseTracker.putGoalData(key, mvtValue);
    }
    @JavascriptInterface
    public static void setGoalCustomMvtTagById(String id, String key, String mvtValue ){
        WiseTracker.putGoalDataById(id, key, mvtValue);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setGoalProduct( String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putGoalData(StaticValues.PARAM_PNC,code);
    }
    @JavascriptInterface
    public static void setGoalProductById( String id, String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putGoalDataById(id, StaticValues.PARAM_PNC,code);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setGoalProductArray( String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putGoalDataArray(StaticValues.PARAM_PNC,code);
    }
    @JavascriptInterface
    public static void setGoalProductArrayById( String id, String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putGoalDataArrayById( id, StaticValues.PARAM_PNC,code);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setGoalProductType(String type){
        // setGoalProduct함수를 사용해서 이미 pnc데이터가 설정된 경우에만 동작하도록 체크 해야함.
        // 상품코드가 먼저 정의되었을 때에만 전송되도록 조건 추가.
        // ["productType1","productType2"]   =>  productType1;productType2
        if( WiseTracker.containsGoalData(StaticValues.PARAM_PNC)){
            WiseTracker.putGoalData(StaticValues.PARAM_PNC_TP,type);
        }
    }
    @JavascriptInterface
    public static void setGoalProductTypeById(String id, String type){
        // setGoalProduct함수를 사용해서 이미 pnc데이터가 설정된 경우에만 동작하도록 체크 해야함.
        // 상품코드가 먼저 정의되었을 때에만 전송되도록 조건 추가.
        // ["productType1","productType2"]   =>  productType1;productType2
        if( WiseTracker.containsGoalDataById(id, StaticValues.PARAM_PNC)){
            WiseTracker.putGoalDataById(id, StaticValues.PARAM_PNC_TP,type);
        }
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setGoalProductTypeArray(String[] type){
        // setGoalProduct함수를 사용해서 이미 pnc데이터가 설정된 경우에만 동작하도록 체크 해야함.
        // 상품코드가 먼저 정의되었을 때에만 전송되도록 조건 추가.
        // ["productType1","productType2"]   =>  productType1;productType2
        if( WiseTracker.containsGoalData(StaticValues.PARAM_PNC)){
            WiseTracker.putGoalDataArray(StaticValues.PARAM_PNC_TP,type);
        }
    }
    @JavascriptInterface
    public static void setGoalProductTypeArrayById(String id, String[] type){
        // setGoalProduct함수를 사용해서 이미 pnc데이터가 설정된 경우에만 동작하도록 체크 해야함.
        // 상품코드가 먼저 정의되었을 때에만 전송되도록 조건 추가.
        // ["productType1","productType2"]   =>  productType1;productType2
        if( WiseTracker.containsGoalData(StaticValues.PARAM_PNC)){
            WiseTracker.putGoalDataArray(StaticValues.PARAM_PNC_TP,type);
        }
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setGoalProductCategory( String code ){
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putGoalData(StaticValues.PARAM_PNG, code);
    }
    @JavascriptInterface
    public static void setGoalProductCategoryById( String id, String code ){
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putGoalDataById(id, StaticValues.PARAM_PNG, code);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setGoalProductCategoryArray( String[] code ){
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putGoalDataArray(StaticValues.PARAM_PNG, code);
    }
    @JavascriptInterface
    public static void setGoalProductCategoryArrayById( String id,  String[] code ){
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putGoalDataArrayById( id, StaticValues.PARAM_PNG, code);
    }
    // ---------------------------------------------------------------------------------------

    // #################################################################################
    // PRODUCT 함수
    public static void setProduct( String code ){
        WiseTracker.setProduct(code, "");
    }
    public static void setProductById( String id, String code ){
        WiseTracker.setProductById( id, code, "");
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setProduct( String code, String name ){
        if(name==null){
            name = "";
        }
        if(name.equalsIgnoreCase("undefined")){
            name="";
        }
        WiseTracker.putPageData(StaticValues.PARAM_PNC,code);
        WiseTracker.putPageData(StaticValues.PARAM_PNC_NM, name);
    }
    @JavascriptInterface
    public static void setProductById( String id,  String code, String name ){
        if(name==null){
            name = "";
        }
        if(name.equalsIgnoreCase("undefined")){
            name="";
        }
        WiseTracker.putPageDataById(id, StaticValues.PARAM_PNC,code);
        WiseTracker.putPageDataById(id, StaticValues.PARAM_PNC_NM, name);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setProductType( String type ){
        if(WiseTracker.containsPageData(StaticValues.PARAM_PNC)){
            WiseTracker.putPageData(StaticValues.PARAM_PNC_TP,type);
        }
    }
    @JavascriptInterface
    public static void setProductTypeById( String id,  String type ){
        if(WiseTracker.containsPageDataById(id, StaticValues.PARAM_PNC)){
            WiseTracker.putPageDataById(id, StaticValues.PARAM_PNC_TP,type);
        }
    }
    // ---------------------------------------------------------------------------------------
    // #################################################################################
    // PRODUCT CATEGORY 함수
    public static void setProductCategory( String code ){
        WiseTracker.setProductCategory(code,"");
    }
    public static void setProductCategoryById( String id,  String code ){
        WiseTracker.setProductCategoryById(id, code,"");
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setProductCategory( String code, String name ){
        if(name==null){
            name = "";
        }
        if(name.equalsIgnoreCase("undefined")){
            name="";
        }
        WiseTracker.putPageData(StaticValues.PARAM_PNG,code);
        WiseTracker.putPageData(StaticValues.PARAM_PNG_NM,name);
    }
    @JavascriptInterface
    public static void setProductCategoryById( String id,  String code, String name ){
        if(name==null){
            name = "";
        }
        if(name.equalsIgnoreCase("undefined")){
            name="";
        }
        WiseTracker.putPageDataById(id, StaticValues.PARAM_PNG,code);
        WiseTracker.putPageDataById(id, StaticValues.PARAM_PNG_NM,name);
    }
    // ---------------------------------------------------------------------------------------
    // #################################################################################
    // PRODUCT(주문완료) 함수
    @JavascriptInterface
    public static void setOrderCustomMvtTag( String key, String mvtValue ){
        WiseTracker.putRevenueData(key, mvtValue);
    }
    @JavascriptInterface
    public static void setOrderCustomMvtTagById( String id,  String key, String mvtValue ){
        WiseTracker.putRevenueDataById(id, key, mvtValue);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setOrderProduct( String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putRevenueData(StaticValues.PARAM_PNC,code);
    }
    @JavascriptInterface
    public static void setOrderProductById( String id, String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putRevenueDataById(id, StaticValues.PARAM_PNC,code);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setOrderProductArray( String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putRevenueDataArray(StaticValues.PARAM_PNC,code);
    }
    @JavascriptInterface
    public static void setOrderProductArrayById( String id, String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putRevenueDataArrayById( id, StaticValues.PARAM_PNC,code);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setOrderProductType( String type ) {
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productType1","productType2"]   =>  productType1;productType2
        if(WiseTracker.containsRevenueData(StaticValues.PARAM_PNC)){
            WiseTracker.putRevenueData(StaticValues.PARAM_PNC_TP,type);
        }
    }
    @JavascriptInterface
    public static void setOrderProductTypeById( String id, String type ) {
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productType1","productType2"]   =>  productType1;productType2
        if(WiseTracker.containsRevenueDataById( id, StaticValues.PARAM_PNC)){
            WiseTracker.putRevenueDataById( id, StaticValues.PARAM_PNC_TP,type);
        }
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setOrderProductTypeArray( String[] type ) {
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productType1","productType2"]   =>  productType1;productType2
        if(WiseTracker.containsRevenueData(StaticValues.PARAM_PNC)){
            WiseTracker.putRevenueDataArray(StaticValues.PARAM_PNC_TP,type);
        }
    }
    @JavascriptInterface
    public static void setOrderProductTypeArrayById( String id, String[] type ) {
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productType1","productType2"]   =>  productType1;productType2
        if(WiseTracker.containsRevenueDataById(id, StaticValues.PARAM_PNC)){
            WiseTracker.putRevenueDataArrayById(id, StaticValues.PARAM_PNC_TP,type);
        }
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setOrderProductCategory( String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putRevenueData(StaticValues.PARAM_PNG,code);
    }
    @JavascriptInterface
    public static void setOrderProductCategoryById( String id, String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putRevenueDataById( id, StaticValues.PARAM_PNG,code);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setOrderProductCategoryArray( String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putRevenueDataArray(StaticValues.PARAM_PNG,code);
    }
    @JavascriptInterface
    public static void setOrderProductCategoryArrayById( String id, String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putRevenueDataArrayById( id, StaticValues.PARAM_PNG,code);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setOrderAmount( int value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["10000","20000"]   =>  10000;20000
        WiseTracker.putRevenueData(StaticValues.PARAM_AMT,value);
    }
    @JavascriptInterface
    public static void setOrderAmountById( String id, int value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["10000","20000"]   =>  10000;20000
        WiseTracker.putRevenueDataById(id, StaticValues.PARAM_AMT,value);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setOrderAmountArray( int[] value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["10000","20000"]   =>  10000;20000
        WiseTracker.putRevenueDataArray(StaticValues.PARAM_AMT,value);
    }
    @JavascriptInterface
    public static void setOrderAmountArrayById( String id, int[] value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["10000","20000"]   =>  10000;20000
        WiseTracker.putRevenueDataArrayById(id, StaticValues.PARAM_AMT,value);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setOrderQuantity( int value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["1","2"]   =>  1;2
        WiseTracker.putRevenueData(StaticValues.PARAM_EA,value);
    }
    @JavascriptInterface
    public static void setOrderQuantityById( String id, int value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["1","2"]   =>  1;2
        WiseTracker.putRevenueDataById(id, StaticValues.PARAM_EA,value);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setOrderQuantityArray( int[] value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["1","2"]   =>  1;2
        WiseTracker.putRevenueDataArray(StaticValues.PARAM_EA,value);
    }
    @JavascriptInterface
    public static void setOrderQuantityArrayById( String id, int[] value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["1","2"]   =>  1;2
        WiseTracker.putRevenueDataArrayById(id, StaticValues.PARAM_EA,value);
    }
    // ---------------------------------------------------------------------------------------
    // #################################################################################
    // 컨텐츠 분석 함수
    @JavascriptInterface
    public static void setContents( String value ){
        WiseTracker.putPageData(StaticValues.PARAM_CP,value);
    }
    @JavascriptInterface
    public static void setContentsById( String id, String value ){
        WiseTracker.putPageDataById(id,StaticValues.PARAM_CP,value);
    }
    // ---------------------------------------------------------------------------------------
    public static boolean trkContents( String value ){
        WiseTracker.setContents(value);
        return BSTracker.getInstance().sendTransaction();
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setPageIdentity( String value ){
        WiseTracker.putPageData(StaticValues.PARAM_PI, value);
    }
    @JavascriptInterface
    public static void setPageIdentityById( String id, String value ){
        WiseTracker.putPageDataById(id, StaticValues.PARAM_PI, value);
    }
    // ---------------------------------------------------------------------------------------
    public static boolean trkPageIdentity( String value ){
        WiseTracker.setPageIdentity(value);
        return BSTracker.getInstance().sendTransaction();
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setSearchKeyword(String keyword){
        WiseTracker.putPageData(StaticValues.PARAM_IKWD,keyword);
    }
    @JavascriptInterface
    public static void setSearchKeywordById(String id,String keyword){
        WiseTracker.putPageDataById(id,StaticValues.PARAM_IKWD,keyword);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setSearchKeywordResult(int count){
        WiseTracker.putPageData(StaticValues.PARAM_IKWD_RS, count);
    }
    @JavascriptInterface
    public static void setSearchKeywordResultById(String id,int count){
        WiseTracker.putPageDataById(id, StaticValues.PARAM_IKWD_RS, count);
    }
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setSearchKeywordCategory(String category){
        WiseTracker.putPageData(StaticValues.PARAM_IKWD_GRP,category);
    }
    @JavascriptInterface
    public static void setSearchKeywordCategoryById(String id,String category){
        WiseTracker.putPageDataById(id,StaticValues.PARAM_IKWD_GRP,category);
    }
    // ---------------------------------------------------------------------------------------
    // #################################################################################
    // 방문자 속성 분석 함수
    @JavascriptInterface
    public static void setMember( String isMember ){
        // Profiler.getInstance(mContext).putIsMember(isMember);
        WiseTracker.putSessionData(StaticValues.PARAM_MBR, isMember);
    }
    @JavascriptInterface
    public static void setGender( String gender ){
        // Profiler.getInstance(mContext).putGender(gender);
        WiseTracker.putSessionData(StaticValues.PARAM_GENDER,gender);
    }
    @JavascriptInterface
    public static void setAge( String age ){
        // Profiler.getInstance(mContext).putAge(age);
        WiseTracker.putSessionData(StaticValues.PARAM_AGE,age);
    }
    @JavascriptInterface
    public static void setUserAttribute( String key, String attribute ){
        switch(key){
            case StaticValues.PARAM_UVP1 :   WiseTracker.putSessionData(StaticValues.PARAM_UVP1, attribute); break;
            case StaticValues.PARAM_UVP2 :   WiseTracker.putSessionData(StaticValues.PARAM_UVP2, attribute); break;
            case StaticValues.PARAM_UVP3 :   WiseTracker.putSessionData(StaticValues.PARAM_UVP3, attribute); break;
            case StaticValues.PARAM_UVP4 :   WiseTracker.putSessionData(StaticValues.PARAM_UVP4, attribute); break;
            case StaticValues.PARAM_UVP5 :   WiseTracker.putSessionData(StaticValues.PARAM_UVP5, attribute); break;
        }
    }
    // #################################################################################
    // 사용자 정의 분석 함수
    // ---------------------------------------------------------------------------------------
    @JavascriptInterface
    public static void setCustomMvtTag( String key, String mvtValue ){
        WiseTracker.putPageData(key, mvtValue);
    }
    @JavascriptInterface
    public static void setCustomMvtTagById( String id, String key, String mvtValue ){
        WiseTracker.putPageDataById(id, key, mvtValue);
    }
    // ---------------------------------------------------------------------------------------
    public static void setWebView(WebView webView) {
        BSTracker.getInstance().setWebView(webView);
    }

    public static void injectFinished(WebView webView){
        BSTracker.getInstance().injectFinished(webView);
    }

    /*
    * 입출력 명세서의 값으로 사용되는 필드
    * */

    public final static String PRODUCT_SUB_TYPE1 = "TYPE1"; // setProductSubTp(PRODUCT_SUB_TYPE1);
    public final static String PRODUCT_SUB_TYPE2 = "TYPE2";
    public final static String PRODUCT_SUB_TYPE3 = "TYPE3";
    public final static String PRODUCT_SUB_TYPE4 = "TYPE4";
    public final static String PRODUCT_SUB_TYPE5 = "TYPE5";
    public final static String PRODUCT_SUB_TYPE6 = "TYPE6";
    public final static String PRODUCT_SUB_TYPE7 = "TYPE7";
    public final static String PRODUCT_SUB_TYPE8 = "TYPE8";
    public final static String PRODUCT_SUB_TYPE9 = "TYPE9";
    public final static String MEMBER ="Y";
    public final static String NON_MEMBER = "N";
    public final static String GENDER_MALE ="M";
    public final static String GENDER_FEMALE ="F";
    public final static String GENDER_ETC ="U";
    public final static String AGE_0_TO_9	="A";
    public final static String AGE_10_TO_19	="B";
    public final static String AGE_20_TO_29	="C";
    public final static String AGE_30_TO_39	="D";
    public final static String AGE_40_TO_49	="E";
    public final static String AGE_50_TO_59	="F";
    public final static String AGE_60_OVER 	="G";

    public static final String GOAL_1   = "g1";
    public static final String GOAL_2   = "g2";
    public static final String GOAL_3   = "g3";
    public static final String GOAL_4   = "g4";
    public static final String GOAL_5   = "g5";
    public static final String GOAL_6   = "g6";
    public static final String GOAL_7   = "g7";
    public static final String GOAL_8   = "g8";
    public static final String GOAL_9   = "g9";
    public static final String GOAL_10  = "g10";

    public static final String CUSTOM_MVT_TAG_1 = "mvt1";
    public static final String CUSTOM_MVT_TAG_2 = "mvt2";
    public static final String CUSTOM_MVT_TAG_3 = "mvt3";

    public static final String USER_ATTRIBUTE_1 = "uvp1";
    public static final String USER_ATTRIBUTE_2 = "uvp2";
    public static final String USER_ATTRIBUTE_3 = "uvp3";
    public static final String USER_ATTRIBUTE_4 = "uvp4";
    public static final String USER_ATTRIBUTE_5 = "uvp5";

    public static void broadcastInstall(Context mContext,Intent intent) {
        BSTracker.getInstance().broadcastInstall(mContext,intent);
    }

    public static void updatePush(Intent intent) {
        BSTracker.getInstance().updatePush(intent);
    }

    public static void injectExtra(Intent orgIntent, Intent targetIntent) {
        BSTracker.getInstance().injectExtra(orgIntent,targetIntent);
    }
}
