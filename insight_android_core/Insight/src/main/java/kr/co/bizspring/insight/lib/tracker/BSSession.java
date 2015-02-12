package kr.co.bizspring.insight.lib.tracker;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import kr.co.bizspring.insight.lib.config.BSConfig;
import kr.co.bizspring.insight.lib.util.TinyDB;
import kr.co.bizspring.insight.lib.values.StaticValues;

/**
 * Created by mac on 2014. 9. 22..
 */
public class BSSession {

    private static final String LAST_VISIT_NAME = "lastTime";
    private static final String SESSIONS = "sessions";

    private static BSSession instance;

    private TinyDB sessionDB = null;
    private HashMap<String,TinyDB> sessionMap = null;
    private ArrayList<String> sessions;
    private BSConfig mConfig;

    Context mContext;

    private BSSession(Context context, BSConfig config){
        mContext = context;
        mConfig = config;
        sessionDB = new TinyDB(context,"BS_SESSION");
        sessionMap = new HashMap<String, TinyDB>();
        sessions = sessionDB.getList(SESSIONS);

        //세션의 수가 2보다 크면 2개가 남을 때 까지 세션 정보를 비운다.
        if(sessions.size()>2){
            while (sessions.size()>2){
                String sessionID = sessions.get(0);
                sessions.remove(0);
                new TinyDB(context,sessionID).clear();
            }
        }

        for(int i = 0 ; i < sessions.size();i++){
            String session = sessions.get(i);
            sessionMap.put(session,new TinyDB(context,session));
        }
        if(sessions.size()==0){
            createInitSession();
        }
    }

    public static BSSession getInstance(Context context,BSConfig config){
        if(instance==null){
            instance = new BSSession(context,config);
        }
        return instance;
    }

    public void createInitSession(){
        UUID uuid = UUID.randomUUID();
        sessionDB.putString("appId",uuid.toString());
        sessionDB.putString("lastSessionID","_");
        createSession();
    }

    public String getSid(boolean flagEvt){
        String sid = getSession(flagEvt);
        TinyDB lastSession = sessionMap.get(sid);
        Long lastTime = lastSession.getLong(LAST_VISIT_NAME);
//        return sid+":"+String.valueOf(lastTime);
        return sid;//+":"+String.valueOf(lastTime);
    }

    public String getSession(boolean flagEvt){
        String lastSid = sessions.get(sessions.size()-1);
        TinyDB lastSession = sessionMap.get(lastSid);
        Long lastTime = lastSession.getLong(LAST_VISIT_NAME);
        Long currentTime = System.currentTimeMillis()/1000;
        if(lastTime>currentTime){
            if(flagEvt){
                //이벤트로 들어오면 세션의 만료시간을 연장한다.
                Long nextTime = currentTime+BSConfig.getInstance(mContext).getSessionTime();
                lastSession.putLong(LAST_VISIT_NAME,nextTime);
            }
            return lastSid;
        }else{
            //세션이 만료되었으므로 새 세션을 생성한다.
            return createSession();
        }
    }

    public String createSession(){
        long now = System.currentTimeMillis()/1000;
        long after = now+BSConfig.getInstance(mContext).getSessionTime();
        UUID sessionUUID = UUID.randomUUID();
        String sid = sessionUUID.toString();
        TinyDB newSession = new TinyDB(mContext,sid);
        newSession.putLong(LAST_VISIT_NAME,after);
        String vtTz = mConfig.getCurrentDateString();
        newSession.putString(StaticValues.PARAM_VT_TZ,vtTz);
        int slotNo = mConfig.getSlotCount();
        int currentSlot = 0;
        if(sid.hashCode()<0){
            currentSlot = (-sid.hashCode())%slotNo;
        }else{
            currentSlot = sid.hashCode()%slotNo;
        }
        newSession.putInt(StaticValues.BS_CONFIG_SLOT_COUNT,currentSlot);
        sessions.add(sid);
        sessionMap.put(sid,newSession);
        sessionDB.putString("currentSessionID",sid);
        sessionDB.putList(SESSIONS,sessions);
        return sid;
    }

    public void updateLastSessionID(String sessionID){
        sessionDB.putString("lastSessionID",sessionID);
    }

    public String getLastSessionID(){
        return sessionDB.getString("lastSessionID");
    }

    //todo DocumentID 기반으로 세션 최신성 체크 - fin
    
    public String getVisitNew(String documentId){
        String lastSessionID = getLastSessionID();
        String currentSessionID = getSession(false);
        if(!lastSessionID.equalsIgnoreCase(currentSessionID)){
            TinyDB db = sessionMap.get(currentSessionID);
            String documentKeyId = db.getString(StaticValues.KEY_DOC_ID);
                //키가 되는 도큐먼트 아이디가 없으면 신규 세션이므로 Y
            if(documentKeyId.equalsIgnoreCase("")){
                db.putString(StaticValues.KEY_DOC_ID,documentId);
                return "Y";
                //신규 세션 정보를 저장해 두었다가 키 값과 매치가 되면 신규 세션에 해당하는 문서이므로 Y
            }else if(documentKeyId.equalsIgnoreCase(documentId)){
                return "Y";
            }else{
                //신규 세션이긴 하지만 도큐먼트 키값이 다르므로 시차의 오류가 있는 것.
                //이전의 도큐먼트가 정상 값이므로 N
                return "N";
            }
        }else{
            return "N";
        }
    }

    public BSConfig getConfig() {
        return this.mConfig;
    }

    public int getSlotNo(String sid) {
        TinyDB db = sessionMap.get(sid);
        return sessionMap.get(sid).getInt(StaticValues.BS_CONFIG_SLOT_COUNT);
    }

    public String getVtTz(String sid) {
        return sessionMap.get(sid).getString(StaticValues.PARAM_VT_TZ);
    }

    public void addCsPV() {
        int csPV = sessionMap.get(getSid(true)).getInt(StaticValues.PARAM_CS_P_V);
        csPV++;
        sessionMap.get(getSid(false)).putInt(StaticValues.PARAM_CS_P_V,csPV);
    }
    public int getCsPV(String sid){
        return sessionMap.get(sid).getInt(StaticValues.PARAM_CS_P_V);
    }
}
