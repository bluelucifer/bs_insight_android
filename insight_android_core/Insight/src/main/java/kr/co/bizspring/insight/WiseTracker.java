package kr.co.bizspring.insight;

import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import kr.co.bizspring.insight.lib.util.BSMap;
import kr.co.bizspring.insight.lib.values.StaticValues;

/**
 * Created by caspar on 2014. 11. 12..
 * @author caspar
 * @since 2014. 11. 12.
 * @version 1.0
 * @see kr.co.bizspring.insight.BSTracker
 */
public class WiseTracker {

    private static BSTracker instance;
    /**
    * 싱글톤 정의
     * 반드시 init 이후 메소드를 콜해야 함.
     * @return Analytics
    */
    @JavascriptInterface
    public static BSTracker getInstance(){
        if(instance==null){
            instance = new BSTracker();
        }
        return instance;
    }
    @JavascriptInterface
    public static BSTracker init(Context _context){
        return WiseTracker.getInstance().init(_context);
    }

    @JavascriptInterface
    public static void putInitData(String key, String value){
        WiseTracker.getInstance().getCurrentPageMap().putInitData(key,value);
    }

    public static void checkReferrer(){
        WiseTracker.getInstance().checkReferrer();
    }
    public static void checkReferrer(Intent intent){
        WiseTracker.getInstance().checkReferrer(intent);
    }

    @JavascriptInterface
    public static void initEnd(){
        WiseTracker.getInstance().initEnd();
    }

    @JavascriptInterface
    public static boolean sendTransaction(){
        return WiseTracker.getInstance().sendTransaction();
    }
    public static BSMap startPage(Object obj){
        return WiseTracker.getInstance().startPage(obj);
    }
    @JavascriptInterface
    public static BSMap startPage(String obj){
        return WiseTracker.getInstance().startPage(obj);
    }
    public static void endPage(Object obj){
        WiseTracker.getInstance().endPage(obj);
    }
    @JavascriptInterface
    public static void endPage(String pageCode){
        WiseTracker.getInstance().endPage(pageCode);
    }
    public static BSMap builder(Object obj){
        return WiseTracker.getInstance().builder(obj);
    }
    @JavascriptInterface
    public BSMap builder(String obj) {
        return WiseTracker.getInstance().builder(obj);
    }
    /***
     * 환경변수 설정 함수  ( 고급 )
     * */
    @JavascriptInterface
    public static boolean containsGoalData(String key){
        BSMap currentPageMap = WiseTracker.getInstance().getCurrentPageMap();
        return currentPageMap.containsGoalData(key);
    }
    public static BSMap putGoalData(String key, Object value){
        return WiseTracker.getInstance().getCurrentPageMap().putGoalData(key,value);
    }
    @JavascriptInterface
    public static BSMap putGoalData(String key, String value){
        return WiseTracker.getInstance().getCurrentPageMap().putGoalData(key,value);
    }
    @JavascriptInterface
    public static BSMap putGoalDataArray(String key, String[] value){
        return WiseTracker.getInstance().getCurrentPageMap().putGoalDataArray(key,value);
    }
    @JavascriptInterface
    public static boolean containsPageData(String key){
        BSMap currentPageMap = WiseTracker.getInstance().getCurrentPageMap();
        return currentPageMap.containsPageData(key);
    }
    @JavascriptInterface
    public static BSMap putPageData(String key, String value){
        return WiseTracker.getInstance().getCurrentPageMap().putPageData(key,value);
    }
    public static BSMap putPageData(String key, Object value){
        return WiseTracker.getInstance().getCurrentPageMap().putPageData(key,value);
    }

    @JavascriptInterface
    public static Object getPageData(String key){
        return WiseTracker.getInstance().getCurrentPageMap().getPageData(key);
    }
    @JavascriptInterface
    public String getPageDataString(String key){
        return WiseTracker.getInstance().getCurrentPageMap().getPageDataString(key);
    }
    @JavascriptInterface
    public static boolean containsRevenueData(String key){
        BSMap currentPageMap = WiseTracker.getInstance().getCurrentPageMap();
        return currentPageMap.containsRevenueData(key);
    }
    @JavascriptInterface
    public static BSMap putRevenueData(String key, String value){
        return WiseTracker.getInstance().getCurrentPageMap().putRevenueData(key,value);
    }
    public static BSMap putRevenueData(String key, Object value){
        return WiseTracker.getInstance().getCurrentPageMap().putRevenueData(key,value);
    }
    @JavascriptInterface
    public static BSMap putRevenueDataArray(String key, String[] value){
        return WiseTracker.getInstance().getCurrentPageMap().putRevenueDataArray(key,value);
    }
    public static BSMap putRevenueDataArray(String key, int[] value){
        return WiseTracker.getInstance().getCurrentPageMap().putRevenueDataArray(key,value);
    }
    @JavascriptInterface
    public static void putSessionData(String key, String value){
        WiseTracker.getInstance().getCurrentPageMap().putSessionData(key,value);
        WiseTracker.getInstance().updateDocument();
    }
    @JavascriptInterface
    public static BSMap putSessionReferrer(String referrer){
        return WiseTracker.getInstance().getCurrentPageMap().putSessionReferrer(referrer);
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
    public static void setAcceptPushReceived( boolean value ){
        if( value ){ // value 가 true 일때, push 메시지 수신동의 한것으로 처리함. g29
            WiseTracker.putGoalData(StaticValues.PARAM_GOAL_ACCEPT_PUSH,1);
        }
    }
    @JavascriptInterface
    public static void setGoalProduct( String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putGoalData(StaticValues.PARAM_PNC,code);
    }
    @JavascriptInterface
    public static void setGoalProductArray( String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putGoalDataArray(StaticValues.PARAM_PNC,code);
    }
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
    public static void setGoalProductTypeArray(String[] type){
        // setGoalProduct함수를 사용해서 이미 pnc데이터가 설정된 경우에만 동작하도록 체크 해야함.
        // 상품코드가 먼저 정의되었을 때에만 전송되도록 조건 추가.
        // ["productType1","productType2"]   =>  productType1;productType2
        if( WiseTracker.containsGoalData(StaticValues.PARAM_PNC)){
            WiseTracker.putGoalDataArray(StaticValues.PARAM_PNC_TP,type);
        }
    }
    @JavascriptInterface
    public static void setGoalProductCategory( String code ){
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putGoalData(StaticValues.PARAM_PNG, code);
    }
    @JavascriptInterface
    public static void setGoalProductCategoryArray( String[] code ){
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putGoalDataArray(StaticValues.PARAM_PNG, code);
    }


    // #################################################################################
    // PRODUCT 함수
    public static void setProduct( String code ){
        WiseTracker.setProduct(code, "");
    }
    @JavascriptInterface
    public static void setProduct( String code, String name ){
        WiseTracker.putPageData(StaticValues.PARAM_PNC,code);
        WiseTracker.putPageData(StaticValues.PARAM_PNC_NM, name);
    }
    @JavascriptInterface
    public static void setProductType( String type ){
        if(WiseTracker.containsPageData(StaticValues.PARAM_PNC)){
            WiseTracker.putPageData(StaticValues.PARAM_PNC_TP,type);
        }
    }
    // #################################################################################
    // PRODUCT CATEGORY 함수
    public static void setProductCategory( String code ){
        WiseTracker.setProductCategory(code,"");
    }
    @JavascriptInterface
    public static void setProductCategory( String code, String name ){
        WiseTracker.putPageData(StaticValues.PARAM_PNG,code);
        WiseTracker.putPageData(StaticValues.PARAM_PNG_NM,name);
    }
    // #################################################################################
    // PRODUCT(주문완료) 함수
    @JavascriptInterface
    public static void setOrderProduct( String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putRevenueData(StaticValues.PARAM_PNC,code);
    }
    @JavascriptInterface
    public static void setOrderProductArray( String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["product1","product2"]   =>  product1;product2
        WiseTracker.putRevenueDataArray(StaticValues.PARAM_PNC,code);
    }
    @JavascriptInterface
    public static void setOrderProductType( String type ) {
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productType1","productType2"]   =>  productType1;productType2
        if(WiseTracker.containsRevenueData(StaticValues.PARAM_PNC)){
            WiseTracker.putRevenueData(StaticValues.PARAM_PNC_TP,type);
        }
    }
    @JavascriptInterface
    public static void setOrderProductTypeArray( String[] type ) {
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productType1","productType2"]   =>  productType1;productType2
        if(WiseTracker.containsRevenueData(StaticValues.PARAM_PNC)){
            WiseTracker.putRevenueDataArray(StaticValues.PARAM_PNC_TP,type);
        }
    }
    @JavascriptInterface
    public static void setOrderProductCategory( String code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putRevenueData(StaticValues.PARAM_PNG,code);
    }

    @JavascriptInterface
    public static void setOrderProductCategoryArray( String[] code ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["productCategory1","productCategory2"]   =>  productCategory1;productCategory2
        WiseTracker.putRevenueDataArray(StaticValues.PARAM_PNG,code);
    }

    @JavascriptInterface
    public static void setOrderAmount( int value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["10000","20000"]   =>  10000;20000
        WiseTracker.putRevenueData(StaticValues.PARAM_AMT,value);
    }
    @JavascriptInterface
    public static void setOrderAmountArray( int[] value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["10000","20000"]   =>  10000;20000
        WiseTracker.putRevenueDataArray(StaticValues.PARAM_AMT,value);
    }

    @JavascriptInterface
    public static void setOrderQuantity( int value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["1","2"]   =>  1;2
        WiseTracker.putRevenueData(StaticValues.PARAM_EA,value);
    }

    @JavascriptInterface
    public static void setOrderQuantityArray( int[] value ){
        // 배열의 형태를 파라미터로 받아서, 서버에 전송하는 json 문자열은 ; 문자를 delimiter로하여 join 결과를 전송한다.
        // ["1","2"]   =>  1;2
        WiseTracker.putRevenueDataArray(StaticValues.PARAM_EA,value);
    }
    // #################################################################################
    // 컨텐츠 분석 함수
    @JavascriptInterface
    public static void setContents( String value ){
        WiseTracker.putPageData(StaticValues.PARAM_CP,value);
    }
    @JavascriptInterface
    public static boolean trkContents( String value ){
        WiseTracker.setContents(value);
        return WiseTracker.getInstance().sendTransaction();
    }
    @JavascriptInterface
    public static void setPageIdentity( String value ){
        WiseTracker.putPageData(StaticValues.PARAM_PI, value);
    }
    public static boolean trkPageIdentity( String value ){
        WiseTracker.setPageIdentity(value);
        return WiseTracker.getInstance().sendTransaction();
    }
    @JavascriptInterface
    public static void setSearchKeyword(String keyword){
        WiseTracker.putPageData(StaticValues.PARAM_IKWD,keyword);
    }
    @JavascriptInterface
    public static void setSearchKeywordResult(int count){
        WiseTracker.putPageData(StaticValues.PARAM_IKWD_RS, count);
    }
    @JavascriptInterface
    public static void setSearchKeywordCategory(String category){
        WiseTracker.putPageData(StaticValues.PARAM_IKWD_GRP,category);
    }
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
    @JavascriptInterface
    public void setCustomMvtTag( String key, String mvtValue ){
        WiseTracker.putPageData(key, mvtValue);
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
    public static final String PARAM_GOAL_1   = "g1";
    public static final String PARAM_GOAL_2   = "g2";
    public static final String PARAM_GOAL_3   = "g3";
    public static final String PARAM_GOAL_4   = "g4";
    public static final String PARAM_GOAL_5   = "g5";
    public static final String PARAM_GOAL_6   = "g6";
    public static final String PARAM_GOAL_7   = "g7";
    public static final String PARAM_GOAL_8   = "g8";
    public static final String PARAM_GOAL_9   = "g9";
    public static final String PARAM_GOAL_10  = "g10";
    public static final String PARAM_GOAL_APP_INSTALL = "g29";

}
