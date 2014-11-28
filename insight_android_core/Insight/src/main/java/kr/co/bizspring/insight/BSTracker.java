package kr.co.bizspring.insight;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.squareup.tape.QueueFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import kr.co.bizspring.insight.lib.config.BSConfig;
import kr.co.bizspring.insight.lib.tracker.BSSession;
import kr.co.bizspring.insight.lib.util.BSDebugger;
import kr.co.bizspring.insight.lib.tracker.DocumentManager;
import kr.co.bizspring.insight.lib.tracker.Profiler;
import kr.co.bizspring.insight.lib.values.SiginalIndex;
import kr.co.bizspring.insight.lib.values.StaticValues;
import kr.co.bizspring.insight.lib.values.TrackType;
import kr.co.bizspring.insight.service.InsightService;

/**
 * Created by mac on 2014. 10. 20..
 */
public class BSTracker {

    public static BSTracker instance;
    Context mContext;
    Map<String,HashMap<String,Object>> pageMap;
    public static BSTracker getInstance(){
        if(instance==null){
            instance = new BSTracker();
        }
        return instance;
    }
    protected BSTracker(){
        pageMap = new HashMap<String, HashMap<String, Object>>();
    }
    public void init(Context _context){
        this.mContext = _context;
        Intent intent1 = new Intent(_context, InsightService.class);
        intent1.setAction(SiginalIndex.INIT);
        _context.startService(intent1);
    }

    public boolean sendTransaction(){
        //이건 바뀌어야함!!
        Intent intent = new Intent(mContext, InsightService.class);
        intent.setAction(SiginalIndex.SEND_TRANSACTION);
        mContext.startService(intent);
        return true;
    }

    public void setGoal(String key, String value){

    }
    public boolean trkGoal(String key, String value){
        setGoal(key,value);
        return sendTransaction();
    }

    //todo 전송 규약에서 아직 찾지 못함.
    public void setAcceptPushReceived( boolean value ){

    }
    //todo 전송 규약에서 아직 찾지 못함.
    public boolean trkAcceptPushReceived( boolean value ){
        setAcceptPushReceived(value);
        return sendTransaction();
    }
    public void setProduct( String code ){
//        documentManager.setProduct(code,null);
    }
    public boolean trkProduct( String code ){
        setProduct(code);
//        documentManager.putGoal();
        return sendTransaction();
    }
    public void setProduct( String code, String name ){
//        documentManager.setProduct(code,name);
    }
    public boolean trkProduct( String code, String name ){
        setProduct(code,name);
//        documentManager.putGoal();
        return sendTransaction();
    }
    public void setProductCategory( String code ){
        setProductCategory(code,null);
    }
    public boolean trkProductCategory( String code ){
        setProductCategory(code);
//        documentManager.putGoal();
        return sendTransaction();
    }
    public void setProductCategory( String code, String name ){
//        documentManager.setProductCategory(code,name);
    }
    public boolean trkProductCategory( String code, String name ){
        setProductCategory(code,name);
//        documentManager.putGoal();
        return sendTransaction();
    }
    public void setOrderProduct( String[] code ){
        setOrderProduct(code,null);
    }
    public void setOrderProduct( String[] code, String[] name ){
//        documentManager.setOrderProduct(code,name);
    }
    public void setOrderProductCategory( String[] code ){
        setOrderProductCategory(code,new String[]{});
    }
    public void setOrderProductCategory( String[] code, String[] name ){
//        documentManager.setOrderProductCategory(code,name);
    }
    public void setOrderAmount( int[] value ){
//        documentManager.setOrderAmount(value);
    }
    public void setOrderQuantity( int[] value ){
//        documentManager.setOrderQuantity(value);
    }

    public void setContents( String value ){
//        documentManager.setContents(value);
    }
    public boolean trkContents( String value ){
        setContents(value);
        return sendTransaction();
    }
    public void setPageIdentity( String value ){
//        documentManager.setPageIdentity(value);
    }
    public boolean trkPageIdentity( String value ){
        setPageIdentity(value);
        return sendTransaction();
    }
    public void setMember( String isMember ){
        Profiler.getInstance(mContext).putIsMember(isMember);
    }
    public boolean trkMember( String isMember ){
        setMember(isMember);
        return sendTransaction();
    }
    public void setGender( String gender ){
        Profiler.getInstance(mContext).putGender(gender);
    }
    public boolean trkGender( String gender ){
        setGender(gender);
        return sendTransaction();
    }
    public void setAge( String age ){
        Profiler.getInstance(mContext).putAge(age);
    }
    public boolean trkAge( String age ){
        setAge(age);
        return sendTransaction();
    }
    public void setUserAttribute( int key, String attribute ){

    }
    public boolean trkUserAttribute( int key, String attribute ){
        setUserAttribute(key,attribute);
        return sendTransaction();
    }

    public void setEvent(String type, String name, String value){
//        documentManager.setEvent(type,name,value);
    }
    public void setCampaign(String type, String name, String value,String referer){
//        documentManager.setCampaign(type, name, value, referer);
    }
    public void startCurrentView(Object obj){
//        documentManager.logView(StaticValues.START_VIEW,obj.getClass().getName());
    }
    public void endCurrentView(Object obj){
//        documentManager.logView(StaticValues.END_VIEW,obj.getClass().getName());
    }
    public void pauseCurrentView(Object obj){
//        documentManager.logView(StaticValues.PAUSE_VIEW,obj.getClass().getName());
    }
    public void resumeCurrentView(Object obj){
//        documentManager.logView(StaticValues.RESUME_VIEW,obj.getClass().getName());
    }

    public void startPage(Object obj){
        this.startPage(Integer.toHexString(System.identityHashCode(obj)));
    }
    public void startPage(String pageCode){
//        pageTimerMap.put(pageCode,System.currentTimeMillis()/1000);
        HashMap<String,Object> currentPageMap = new HashMap<String, Object>();
        currentPageMap.put(StaticValues.START_TIME,System.currentTimeMillis()/1000);
        pageMap.put(pageCode,currentPageMap);
//        documentManager.addCsPV();
    }
    public void endPage(Object obj){
        this.endPage(Integer.toHexString(System.identityHashCode(obj)));
    }

    public void putPageCP(Object obj,String cpString){
        putPageParam(obj, StaticValues.PARAM_CP, cpString);
    }

    public void putPagePI(Object obj,String piString){
        putPageParam(obj, StaticValues.PARAM_PI, piString);
    }

    public void putPagePncTp(Object obj,String targetString){
        putPageParam(obj,StaticValues.PARAM_PNC_TP,targetString);
    }

    public void putPagePnc(Object obj,String targetString){
        putPageParam(obj,StaticValues.PARAM_PNC,targetString);
    }

    public void putPagePncName(Object obj,String targetString){
        putPageParam(obj,StaticValues.PARAM_PNC_NM,targetString);
    }

    public void putPagePng(Object obj,String targetString){
        putPageParam(obj,StaticValues.PARAM_PNG,targetString);
    }

    public void putPagePngName(Object obj,String targetString){
        putPageParam(obj,StaticValues.PARAM_PNG_NM,targetString);
    }

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
        HashMap<String,Object> currentMap = pageMap.get(pageCode);
        currentMap.put(ParamName,paramValue);
        currentMap.put(StaticValues.PARAM_EXTRA,Boolean.TRUE);
    }

    public void endPage(String pageCode){
        HashMap<String,Object> currentMap = pageMap.get(pageCode);

        Long stTime = (Long)currentMap.get(StaticValues.START_TIME);
        Long edTime = System.currentTimeMillis()/1000;
        Long vs = edTime - stTime;
        currentMap.put(StaticValues.PARAM_VS,vs);
        currentMap.put(StaticValues.PARAM_CS_P_V,Integer.valueOf(1));
        putMap(currentMap,TrackType.TYPE_PAGES);
    }

    public BSTracker putMap(final HashMap<String,Object> map, final TrackType type){
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
                    } catch (JSONException e) {
                        BSDebugger.log(e, this);
                    }
                } catch (IOException e) {
                    BSDebugger.log(e, this);
                }
                return null;
            }
        };
        task.execute();
        return this;
    }
}
