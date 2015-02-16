package kr.co.bizspring.insight.lib.util;

import android.webkit.JavascriptInterface;

import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.bizspring.insight.BSTracker;
import kr.co.bizspring.insight.lib.values.StaticValues;
import kr.co.bizspring.insight.lib.values.TrackType;

/**
 * Created by mrcm on 2014-12-01.
 */
public class BSMap{

    private HashMap<String,Object> revenueMap = null;
    private HashMap<String,Object> goalMap = null;
    private HashMap<String,Object> pageMap = null;

    private String identity;
    private BSTracker targetTracker;
    public BSMap(){
        init();
    }
    public BSMap(String _identity,BSTracker tracker){
        this.identity = _identity;
        this.targetTracker = tracker;
        init();
    }

    // 초기화 메소드
    private void init(){
        initPageMap();
        initRevenueMap();
        initGoalMap();
    }
    private void initPageMap(){
        if( pageMap == null ) {    pageMap = new HashMap<String,Object>();
        }else{                      pageMap.clear();  }
    }
    private void initGoalMap() {
        if( goalMap == null ) {      goalMap = new HashMap<>();
        }else{                         goalMap.clear(); }
    }
    private void initRevenueMap() {
        if( revenueMap == null ){   revenueMap = new HashMap<String,Object>();
        }else{                        revenueMap.clear();    }
    }

    // 주문 데이터 Map 처리 메소드
    public BSMap putRevenueData(String key, Object value){
        if( key != null && value != null ){
            this.revenueMap.put(key, value);
            this.syncPageData(key, value);
        }
        return this;
    }

    public BSMap putRevenueDataArray(String key, String[] value){
        if( key != null && value != null && value.length > 0  ){
            this.putRevenueData(key, BSUtils.joinStringToString( value ));
        }
        return this;
    }
    public BSMap putRevenueData(String key, String value){
        this.revenueMap.put(key,value);
        this.syncPageData(key, value);
        return this;
    }

    public BSMap putRevenueDataArray(String key, int[] value){
        if( key != null && value != null && value.length > 0 ){
            this.putRevenueData(key, BSUtils.joinIntToString( value ));
        }
        return this;
    }

    public boolean containsRevenueData(String key){
        return this.revenueMap.containsKey(key);
    }
    // GOAL 데이터 Map 처리 메소드

    public BSMap putGoalData(String key, Object value){
        if( key != null && value != null ){
            this.goalMap.put(key, value);
            this.syncPageData(key, value);
        }
        return this;
    }
    public BSMap putGoalDataArray(String key, String[] value){
        if( key != null && value != null && value.length > 0  ){
            this.putGoalData(key, BSUtils.joinStringToString(value));
        }
        return this;
    }
    public BSMap putGoalData(String key, String value){
        if( key != null && value != null ){
            this.goalMap.put(key, value);
            this.syncPageData(key, value);
        }
        return this;
    }

    public BSMap putGoalDataArray(String key, int[] value){
        if( key != null && value != null && value.length > 0  ){
            this.putGoalData(key, BSUtils.joinIntToString( value ));
        }
        return this;
    }
    public boolean containsGoalData(String key){
        return this.goalMap.containsKey(key);
    }

    // PAGE MAP 데이터 메소드
    public BSMap putPageData(String key, Object value){
        if( key != null && value != null ){
            this.pageMap.put(key, value);
        }
        return this;
    }
    public BSMap putPageData(String key, String value){
        if( key != null && value != null ){
            this.pageMap.put(key, value);
        }
        return this;
    }
    public Object getPageData(String key){
        if( this.pageMap != null ){
            return this.pageMap.get(key);
        }else{
          return null;
        }
    }
    public String getPageDataString(String key){
        if( this.pageMap != null ){
            return this.pageMap.get(key).toString();
        }else{
            return null;
        }
    }
    public boolean containsPageData(String key){
        return this.pageMap.containsKey(key);
    }

    private void syncPageData(String key, Object value){
        // PAGE DATA 와 동기화 필요한 입출력 항목 처리.
        if( key.equalsIgnoreCase(StaticValues.PARAM_MVT1) || key.equalsIgnoreCase(StaticValues.PARAM_MVT2)  || key.equalsIgnoreCase(StaticValues.PARAM_MVT3) ||  key.equalsIgnoreCase(StaticValues.PARAM_PI) ){
            this.putPageData(key,String.valueOf(value));
        }
    }

    // ###############################################
    // session Data 메소드
    public BSMap putSessionData(String key, String value){
        this.targetTracker.putSessionData(key,value); // profileDB 에 저장함.
        this.syncPageData(key, value);
        return this;
    }
    public BSMap putSessionReferrer(String referrer){
        this.targetTracker.putSessionReferrer(referrer); // profileDB 에 저장함.
        this.targetTracker.updateDocument();
        return this;
    }

    // ###############################################
    // init Data 메소드
    public void putInitData(String key, String value){
        this.targetTracker.putSessionData(key,value); // profileDB 에 저장함.
        this.syncPageData(key, value);
    }

    public HashMap<String,Object> getPageDataMap(){
        return this.pageMap;
    }

    // GOAL, REVENUE 데이터 전송 메소드
    public BSMap send(){
        boolean isSendOk = false;
        if( revenueMap != null && revenueMap.size() > 0 ){
            targetTracker.putMap(revenueMap, TrackType.TYPE_REVENUE);
            this.revenueMap = new HashMap<String,Object>();
            isSendOk = true;
        }
        if( goalMap != null && goalMap.size() > 0 ){
            targetTracker.putMap(goalMap,TrackType.TYPE_GOAL);
            this.goalMap = new HashMap<String,Object>();
            isSendOk = true;
        }
        if( isSendOk ){
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    targetTracker.sendTransaction();
                }
            };
            Timer timer = new Timer();
            timer.schedule(task,3000);
        }
        return this;
    }


}
