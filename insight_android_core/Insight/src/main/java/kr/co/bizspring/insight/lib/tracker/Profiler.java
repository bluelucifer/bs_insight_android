package kr.co.bizspring.insight.lib.tracker;

import android.content.Context;

import kr.co.bizspring.insight.lib.config.BSConfig;
import kr.co.bizspring.insight.lib.util.TinyDB;
import kr.co.bizspring.insight.lib.values.StaticValues;

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

        ltrvni += currentOrderPtm - recentOrderPtm;
        profileDB.putLong(StaticValues.PARAM_LTRVNI,ltrvni);
        profileDB.putLong(StaticValues.RECENT_ORDER_PTM,currentOrderPtm);
        ltrvnc++;
        profileDB.putInt(StaticValues.PARAM_LTRVNC,ltrvnc);
        notifyUdRvnc();
    }

    public int getUdRvnc() {
        return profileDB.getInt(StaticValues.PARAM_UD_RVNC);
    }

    private void notifyUdRvnc(){
        int returnVisitDate = BSConfig.getInstance(mContext).getReturnVisitDate();
        if(returnVisitDate>0){
            TinyDB documentDB = new TinyDB(mContext,DocumentManager.DOCUMENT_DB_NAME);
            long recentVisitPtm = documentDB.getLong(StaticValues.PARAM_RECENT_VISIT_PTM);
            long currentTimeSec = System.currentTimeMillis()/1000;
            int udRvnc = profileDB.getInt(StaticValues.PARAM_UD_RVNC);
            long interval = Math.round((currentTimeSec-recentVisitPtm)/60/60/24);
            if(interval>=returnVisitDate){
                udRvnc = 1;
            }else{
                udRvnc +=1;
            }
            profileDB.putInt(StaticValues.PARAM_UD_RVNC,udRvnc);
        }
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
}
