package kr.co.wisetracker.insight.lib.tracker;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import kr.co.wisetracker.insight.WiseTracker;
import kr.co.wisetracker.insight.lib.config.BSConfig;
import kr.co.wisetracker.insight.lib.util.BSDebugger;
import kr.co.wisetracker.insight.lib.util.BSUtils;
import kr.co.wisetracker.insight.lib.util.TinyDB;
import kr.co.wisetracker.insight.lib.values.StaticValues;

/**
 * Created by mac on 2014. 9. 29..
 */
public class Profiler {

    Context mContext;
    TinyDB profileDB;
    private static Profiler instance = null;

    public static Profiler getInstance(Context context){
        if(instance==null){
            instance = new Profiler(context);
        }
        return instance;
    }
    private Profiler(Context context){
        mContext = context;
        profileDB = new TinyDB(mContext,StaticValues.PROFILE_DB_NAME);
    }

    public void addSessionTrace(String key, String value){
        int TRACE_SIZE_MAX = 10;
        ArrayList<String> values = new ArrayList<>(profileDB.getList(key));
        Iterator<String> itr = values.iterator();
        int counter = 0;
        int flagFindIdx = -1;
        while (itr.hasNext()){
            String next = itr.next();
            if(next.equalsIgnoreCase(value)){
                // 과거에 봤던 캠페인이라면 과거 index를 초기화 시키고 제일 마지막에 추가 시킴.
                flagFindIdx = values.indexOf(next);
            }
            counter++;
        }
        if(flagFindIdx >=0){
            values.remove(flagFindIdx);
            values.add(value);
        }else{
            values.add(value);
            counter++;
        }
        if(counter>TRACE_SIZE_MAX){
            for(int i = 0 ; i < (counter-TRACE_SIZE_MAX) ; i++){
                values.remove(0);
            }
        }
        profileDB.putList(key,values);
        if( WiseTracker.FLAG_OF_PRINT_LOG) {
            Log.i("DEBUG_WISETRACKER", " addSessionTrace Update : " + key +" / "+ value );
        }
        /* old source
        if( value != null && !value.equals("")){
//          String c = profileDB.getString(key);
          // 과거에 봤던 캠페인이라면 과거 index를 초기화 시키고 제일 마지막에 추가 시킴.
          if( c.indexOf(("["+value+"]")) >= 0 ){
              c = c.replaceAll("\\["+value+"\\]","");
          }
          c += ("["+value+"]");
          profileDB.putString(key,c);
      }
      */
    }
    public void clearSessionTrace(String key){
//        profileDB.putString(key,"");
        profileDB.putList(key,new ArrayList<String>());
        if( WiseTracker.FLAG_OF_PRINT_LOG ){  Log.i("DEBUG_WISETRACKER", " clearSessionTrace Update : " + key + " / Empty."); }
    }
    public void putSessionData(String key, String value){   profileDB.putString(key, value);    }
    public void putSessionLongData( String key, long value ){   profileDB.putLong(key, value);     }
    public void putSessionIntData(String key, int value){ profileDB.putInt(key, value);  }

    public String getSessionStringData(String key){ return profileDB.getString(key); }
    public int getSessionIntegerData(String key){ return profileDB.getInt(key); }
    public long getSessionLongData(String key){ return profileDB.getLong(key); }

    public void putIsMember(String isMember) {
        profileDB.putString(StaticValues.PARAM_MBR,isMember);
    }

    public String getMbr(){
        return profileDB.getString(StaticValues.PARAM_MBR);
    }

    public void putGender(String gender) {
        profileDB.putString(StaticValues.PARAM_GENDER,gender);
    }

    public String getGender(){
        return profileDB.getString(StaticValues.PARAM_GENDER);
    }
    public void putAge(String age) {
        profileDB.putString(StaticValues.PARAM_AGE,age);
    }

    public String getAge(){
        return profileDB.getString(StaticValues.PARAM_AGE);
    }

    public void putUvp1(String uvp1) {
        profileDB.putString(StaticValues.PARAM_UVP1,uvp1);
    }
    public void putUvp2(String uvp2) {
        profileDB.putString(StaticValues.PARAM_UVP2,uvp2);
    }
    public void putUvp3(String uvp3) {
        profileDB.putString(StaticValues.PARAM_UVP3,uvp3);
    }
    public void putUvp4(String uvp4) {
        profileDB.putString(StaticValues.PARAM_UVP4,uvp4);
    }
    public void putUvp5(String uvp5) {
        profileDB.putString(StaticValues.PARAM_UVP5,uvp5);
    }

    public String getUvp1(){
        return profileDB.getString(StaticValues.PARAM_UVP1);
    }
    public String getUvp2(){
        return profileDB.getString(StaticValues.PARAM_UVP2);
    }
    public String getUvp3(){
        return profileDB.getString(StaticValues.PARAM_UVP3);
    }
    public String getUvp4(){
        return profileDB.getString(StaticValues.PARAM_UVP4);
    }
    public String getUvp5(){
        return profileDB.getString(StaticValues.PARAM_UVP5);
    }
    public int getLtrvnc() {
        return profileDB.getInt(StaticValues.PARAM_LTRVNC);
    }
    public long getLtrvni() {
        return profileDB.getLong(StaticValues.PARAM_LTRVNI);
    }

    public void notifyLtrvnc(){
        int ltrvnc = profileDB.getInt(StaticValues.PARAM_LTRVNC);
        long ltrvni = profileDB.getLong(StaticValues.PARAM_LTRVNI);
        long recentOrderPtm = profileDB.getLong(StaticValues.RECENT_ORDER_PTM);
        long currentOrderPtm = System.currentTimeMillis()/1000;

        // ltrvni값은 day(일)로 계산된 값을 가져야함. longtime 아님.
        // ltrvni += currentOrderPtm - recentOrderPtm;
        ltrvni += Math.round((currentOrderPtm-recentOrderPtm)/60/60/24);


        profileDB.putLong(StaticValues.PARAM_LTRVNI,ltrvni);
        ltrvnc++;
        profileDB.putInt(StaticValues.PARAM_LTRVNC,ltrvnc);
        notifyUdRvnc();
    }

    public int getUdRvnc() {
        return profileDB.getInt(StaticValues.PARAM_UD_RVNC);
    }

    private void notifyUdRvnc(){
        int returnVisitDate = BSConfig.getInstance(mContext).getReturnVisitDate();
        long currentOrderPtm = System.currentTimeMillis()/1000;
        if(returnVisitDate>0){
            TinyDB documentDB = new TinyDB(mContext,DocumentManager.DOCUMENT_DB_NAME);
            long recentOrderPtm = profileDB.getLong(StaticValues.RECENT_ORDER_PTM);
            int udRvnc = profileDB.getInt(StaticValues.PARAM_UD_RVNC);
            long interval = Math.round((currentOrderPtm-recentOrderPtm)/60/60/24);
            if(interval>=returnVisitDate){
                udRvnc = 1;
            }else{
                udRvnc +=1;
            }
            profileDB.putInt(StaticValues.PARAM_UD_RVNC,udRvnc);
        }
        profileDB.putLong(StaticValues.RECENT_ORDER_PTM,currentOrderPtm);
    }

    public String getFbSource() {
        return profileDB.getString(StaticValues.PARAM_FB_SOURCE);
    }
    public void putFbSource(String target){
        String refText = "";
        String[] targetArr = target.split("&");
        for(int i = 0 ; i < targetArr.length ; i++){
            String keys = targetArr[i];
            String[] keyValue = keys.split("=");
            if(keyValue[0].equalsIgnoreCase(StaticValues.PARAM_FB_SOURCE)){
                refText =keyValue[1];
            }else if(keyValue[0].equalsIgnoreCase(StaticValues.PARAM_REF)){
                refText =keyValue[1];
            }
        }
        profileDB.putString(StaticValues.PARAM_FB_SOURCE,refText);
    }

    public void initOrderPTime() {
        long currentOrderPtm = System.currentTimeMillis()/1000;
        profileDB.putLong(StaticValues.RECENT_ORDER_PTM, currentOrderPtm);
    }

    public void setInstallReferrer(){
        profileDB.putString(StaticValues.PARAM_IAT_SOURCE, profileDB.getString(StaticValues.PARAM_MAT_SOURCE));
        profileDB.putString(StaticValues.PARAM_IAT_MEDIUM, profileDB.getString(StaticValues.PARAM_MAT_MEDIUM));
        profileDB.putString(StaticValues.PARAM_IAT_KWD, profileDB.getString(StaticValues.PARAM_MAT_KWD));
        profileDB.putString(StaticValues.PARAM_IAT_CAMPAIGN, profileDB.getString(StaticValues.PARAM_MAT_CAMPAIGN));
    }
    public boolean hasReferrerData_MAT(){
        boolean checkFlag = false;
        if( !(profileDB.getString(StaticValues.PARAM_MAT_SOURCE)).equals("") ||
                !(profileDB.getString(StaticValues.PARAM_MAT_MEDIUM)).equals("") ||
                !(profileDB.getString(StaticValues.PARAM_MAT_KWD)).equals("") ||
                !(profileDB.getString(StaticValues.PARAM_MAT_CAMPAIGN)).equals("") ) {
            checkFlag = true;
        }
        return checkFlag;
    }
    public HashMap<String,Object> getLatestReferrerInfo(){
        HashMap<String,Object> referrerSet = new HashMap<String,Object>();

        // mat
        referrerSet.put(StaticValues.PARAM_MAT_SOURCE,profileDB.getString(StaticValues.PARAM_MAT_SOURCE));
        referrerSet.put(StaticValues.PARAM_MAT_MEDIUM,profileDB.getString(StaticValues.PARAM_MAT_MEDIUM));
        referrerSet.put(StaticValues.PARAM_MAT_KWD,profileDB.getString(StaticValues.PARAM_MAT_KWD));
        referrerSet.put(StaticValues.PARAM_MAT_CAMPAIGN,profileDB.getString(StaticValues.PARAM_MAT_CAMPAIGN));
        referrerSet.put(StaticValues.PARAM_MAT_UPDATE_TIME, profileDB.getLong(StaticValues.PARAM_MAT_UPDATE_TIME)); // 업데이트 된 시간 저장 후 만료시간 체크
        referrerSet.put(StaticValues.PARAM_MAT_UPDATE_SID,profileDB.getString(StaticValues.PARAM_MAT_UPDATE_SID)); // 전환타입 분석을 위한 sid저장후 체크

        // facebook
        referrerSet.put(StaticValues.PARAM_FB_SOURCE,profileDB.getString(StaticValues.PARAM_FB_SOURCE));
        referrerSet.put(StaticValues.PARAM_FB_UPDATE_TIME, profileDB.getLong(StaticValues.PARAM_FB_UPDATE_TIME)); // 업데이트 된 시간 저장 후 만료시간 체크
        referrerSet.put(StaticValues.PARAM_FB_UPDATE_SID,profileDB.getString(StaticValues.PARAM_FB_UPDATE_SID)); // 전환타입 분석을 위한 sid저장후 체크

        // utm
        referrerSet.put(StaticValues.PARAM_UTM_SOURCE,profileDB.getString(StaticValues.PARAM_UTM_SOURCE));
        referrerSet.put(StaticValues.PARAM_UTM_MEDIUM,profileDB.getString(StaticValues.PARAM_UTM_MEDIUM));
        referrerSet.put(StaticValues.PARAM_UTM_CAMPAIGN,profileDB.getString(StaticValues.PARAM_UTM_CAMPAIGN));
        referrerSet.put(StaticValues.PARAM_UTM_TERM,profileDB.getString(StaticValues.PARAM_UTM_TERM));
        referrerSet.put(StaticValues.PARAM_UTM_CONTENT,profileDB.getString(StaticValues.PARAM_UTM_CONTENT));
        referrerSet.put(StaticValues.PARAM_UTM_UPDATE_TIME, profileDB.getLong(StaticValues.PARAM_UTM_UPDATE_TIME)); // 업데이트 된 시간 저장 후 만료시간 체크
        referrerSet.put(StaticValues.PARAM_UTM_UPDATE_SID,profileDB.getString(StaticValues.PARAM_UTM_UPDATE_SID)); // 전환타입 분석을 위한 sid저장후 체크

        referrerSet.put(StaticValues.PARAM_PUSH_MESSAGE_KEY,profileDB.getString(StaticValues.PARAM_PUSH_MESSAGE_KEY)); // 전환타입 분석을 위한 sid저장후 체크
        referrerSet.put(StaticValues.PARAM_PUSH_MESSAGE_UPDATE_TIME, profileDB.getLong(StaticValues.PARAM_PUSH_MESSAGE_UPDATE_TIME)); // 업데이트 된 시간 저장 후 만료시간 체크
        referrerSet.put(StaticValues.PARAM_PUSH_MESSAGE_UPDATE_SID,profileDB.getString(StaticValues.PARAM_PUSH_MESSAGE_UPDATE_SID)); // 전환타입 분석을 위한 sid저장후 체크

        return referrerSet;
    }

    public boolean updateResponse(JSONObject o) {
        try {
            String rCode = o.getString(StaticValues.RESPONSE_CODE);
            BSConfig.getInstance(mContext).putPref(StaticValues.RESPONSE_CODE,rCode);
            if(rCode.equalsIgnoreCase(StaticValues.RES004)){
                BSConfig.getInstance(mContext).putPref(StaticValues.LOCK_CODE,BSConfig.getInstance(mContext).getHashKey());
            }

            //일시적 장애 처리
            if(rCode.equalsIgnoreCase(StaticValues.RES003)){
                return true;
            }else{
                return false;
            }
        } catch (JSONException e) {
            BSDebugger.log(e, this);
        }
        return false;
    }

    public boolean shouldSend(boolean isDocumnet) {
        String rCode = (String) BSConfig.getInstance(mContext).getPrefValue(StaticValues.RESPONSE_CODE,String.class);
        if(rCode.equalsIgnoreCase(StaticValues.RES001)){
            return true;
        }else if(rCode.equalsIgnoreCase(StaticValues.RES002)){
            return !isDocumnet;
        }else if(rCode.equalsIgnoreCase(StaticValues.RES003)){
            return true;
        }else if(rCode.equalsIgnoreCase(StaticValues.RES004)){
            String lockCode = (String) BSConfig.getInstance(mContext).getPrefValue(StaticValues.LOCK_CODE,String.class);
            String hashCode = BSConfig.getInstance(mContext).getHashKey();
            return !lockCode.equalsIgnoreCase(hashCode);
        }else {
            return true;
        }
    }

    public String getSessionStringListData(String key) {
        List<String> values = profileDB.getList(key);
        Iterator<String> itr = values.iterator();
        String targetRrn = new String();
        while (itr.hasNext()){
            String next = itr.next();
            targetRrn += "["+next+"]";
        }
        return targetRrn;
    }

    public void markRevenue() {
        profileDB.putBoolean(StaticValues.MARK_REVENUE,true);
    }

    public boolean isMarkRevenue() {
        return profileDB.getBoolean(StaticValues.MARK_REVENUE);
    }

    public void unMarkRevenue() {
        profileDB.putBoolean(StaticValues.MARK_REVENUE,false);
    }
}
