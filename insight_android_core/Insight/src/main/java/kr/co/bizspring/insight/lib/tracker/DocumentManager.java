package kr.co.bizspring.insight.lib.tracker;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.squareup.tape.QueueFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import kr.co.bizspring.insight.lib.config.BSConfig;
import kr.co.bizspring.insight.lib.util.Connectivity;
import kr.co.bizspring.insight.lib.util.TinyDB;
import kr.co.bizspring.insight.lib.network.CallBackInterface;
import kr.co.bizspring.insight.lib.network.RestTask;
import kr.co.bizspring.insight.lib.util.BSDebugger;
import kr.co.bizspring.insight.lib.util.BSUtils;
import kr.co.bizspring.insight.lib.util.JsonStringCompress;
import kr.co.bizspring.insight.lib.values.StaticValues;
import kr.co.bizspring.insight.lib.values.TrackType;

/**
 * Created by mac on 2014. 9. 25..
 */
public class DocumentManager {

    public static final String DOCUMENT_DB_NAME = "DocumentDB";
    private static final String DOCUMENT_TARGET_NAME = "DocumentTarget";
    private static final String PAGE_DB_NAME = "PageDB";

    private static DocumentManager instance = null;

    private JSONObject currentDocument;
    private Context mContext;
    private BSSession sessionManager;

    TinyDB documentDB;
    TinyDB pageDB;
    ArrayList<String> documetNameList;
    Profiler profiler;

    /**
     * 생성자
     * @param context
     * @param session
     */
    private DocumentManager(Context context, BSSession session){
        mContext = context;
        sessionManager = session;
        documentDB = new TinyDB(mContext,DOCUMENT_DB_NAME);
        pageDB = new TinyDB(mContext,PAGE_DB_NAME);
        documetNameList = documentDB.getList(DOCUMENT_TARGET_NAME);
        profiler = Profiler.getInstance(mContext);
        if(documetNameList==null){
            documetNameList = new ArrayList<String>();
        }
        loadDocument();
    }

    /**
     * 가장 최신의 도큐먼트를 불러온다.
     * 없으면 새로 생성
     */
    private void loadDocument() {
        if(documetNameList.size()==0){
            currentDocument = createDocument();
        }else{
            String documentID = documetNameList.get(documetNameList.size() - 1);
            //새로운 세션이면 새 문서를 만든다.
            if(sessionManager.getVisitNew().equalsIgnoreCase("Y")){
                currentDocument = createDocument();
            }else{
                //새로운 세션이 아니면 기존의 문서를 불러온다.
                try {
                    FileInputStream inputStream = new FileInputStream(mContext.getFilesDir().getPath()+"/"+documentID);
                    String jsonString = "";
                    String buffer = "";
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    while ((buffer=bufferedReader.readLine())!=null){
                        jsonString += buffer;
                    }
                    currentDocument = new JSONObject(jsonString);
                    bufferedReader.close();
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    BSDebugger.log(e, this);
                } catch (JSONException e) {
                    BSDebugger.log(e, this);
                } catch (IOException e) {
                    BSDebugger.log(e,this);
                }catch (Exception e){
                    //어떤 에러라도 프로세스가 죽으면 안댐.
                    BSDebugger.log(e,this);
                }
            }
        }
    }

    /**
     * 도큐먼트 발송 메소드
     */
    boolean sendLock = false;
    public void sendDocumet(){
        if(sendLock){
            return;
        }
        //문서를 발송한다. 비동기로 순차적으로
        //네트워크 사용이 가능할 때
        if(Connectivity.isConnected(mContext)){
            if(documetNameList.size()>0){
                String target = sessionManager.getConfig().getTargetUri();
                String documentID = documetNameList.get(0);
                FileInputStream inputStream = null;
                try {
                    //로컬에서 최신의 도큐먼트를 불러온다.
                    inputStream = new FileInputStream(mContext.getFilesDir().getPath()+"/"+documentID);
                    String jsonString = "";
                    String buffer = "";
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    while ((buffer=bufferedReader.readLine())!=null){
                        jsonString += buffer;
                    }
                    if(jsonString.equalsIgnoreCase("")){
                        documetNameList.remove(0);
                        documentDB.putList(DOCUMENT_TARGET_NAME,documetNameList);
                        sendDocumet();
                        return;
                    }
                    final JSONObject document = new JSONObject(jsonString);

                    document.put(TrackType.TYPE_GOAL.toString(),getQueue(StaticValues.GOAL_FILE_PREFIX,documentID));
                    document.put(TrackType.TYPE_PAGES.toString(),mergePages(getQueue(StaticValues.PAGES_FILE_PREFIX, documentID)));

                    document.put(TrackType.TYPE_REVENUE.toString(),getQueue(StaticValues.REVENUE_FILE_PREFIX,documentID));
                    document.put(TrackType.TYPE_EVENT.toString(),getQueue(StaticValues.EVENT_FILE_PREFIX,documentID));
                    document.put(TrackType.TYPE_CAMPAIGN.toString(),getQueue(StaticValues.CAMPAIGN_FILE_PREFIX,documentID));
                    document.put(TrackType.TYPE_VIEW.toString(),getQueue(StaticValues.VIEW_FILE_PREFIX,documentID));
                    boolean sendFlag = false;
                    if(document.getJSONObject(TrackType.TYPE_SESSION.toString()).getString(StaticValues.PARAM_VISIT_NEW).equalsIgnoreCase("Y")){
                        sendFlag = true;
                    }else if(document.getJSONArray(TrackType.TYPE_GOAL.toString()).length()!=0||
                            document.getJSONArray(TrackType.TYPE_PAGES.toString()).length()!=0||
                            document.getJSONArray(TrackType.TYPE_REVENUE.toString()).length()!=0||
                            document.getJSONArray(TrackType.TYPE_EVENT.toString()).length()!=0||
                            document.getJSONArray(TrackType.TYPE_CAMPAIGN.toString()).length()!=0||
                            document.getJSONArray(TrackType.TYPE_VIEW.toString()).length()!=0
                            ){
                        sendFlag = true;
                    }
                    if(sendFlag){
                        RestTask task = new RestTask(target, JsonStringCompress.compress(document.toString()), RestTask.RestType.POST,new CallBackInterface() {
                            @Override
                            public void toDoInBackground(JSONObject o) throws JSONException {
                                sendLock = false;
                                try {
                                    if(documetNameList.isEmpty()){
                                        currentDocument = createDocument();
                                        return;
                                    }else{
                                        documetNameList.remove(0);
                                        documentDB.putList(DOCUMENT_TARGET_NAME,documetNameList);
                                        if(documetNameList.isEmpty()){
                                            currentDocument = createDocument();
                                        }else{
                                            sendDocumet();
                                        }
                                    }
                                }catch (Exception e){
                                    BSDebugger.log(e,this);
                                }
                            }

                            @Override
                            public void onErrorCodefind(int statusCode, String statusString) {
                                sendLock = false;
                                BSDebugger.log(statusString);
                            }
                        });
                        sendLock = true;
                        task.execute();
                    }else{
                        documetNameList.remove(0);
                        documentDB.putList(DOCUMENT_TARGET_NAME,documetNameList);
                        if(documetNameList.isEmpty()){
                            currentDocument = createDocument();
                        }else{
                            sendDocumet();
                        }
                    }

                } catch (Exception e) {
                    //읽는데 문제가 있으면 삭제하고 다시 시작.
                    documetNameList.remove(0);
                    documentDB.putList(DOCUMENT_TARGET_NAME,documetNameList);
                    sendDocumet();
                    BSDebugger.log(e,DocumentManager.this);
                }
            }else{
                currentDocument = createDocument();
            }
        }
    }

    private JSONArray mergePages(JSONArray queue) {
        JSONArray target = new JSONArray();
        String globalVtTz = documentDB.getString(StaticValues.PARAM_VT_TZ);
        if(globalVtTz.length()==0){
            globalVtTz = BSConfig.getCurrentDateString();
        }
        int globalVS = documentDB.getInt(StaticValues.PARAM_VS);
        int globalCsPv = documentDB.getInt(StaticValues.PARAM_CS_P_V);
        JSONObject globalPage = new JSONObject();
        try {
            globalPage.put(StaticValues.PARAM_VT_TZ,globalVtTz);
            globalPage.put(StaticValues.PARAM_VS,globalVS);
            globalPage.put(StaticValues.PARAM_CS_P_V,globalCsPv);
            for(int i = 0 ; i < queue.length();i++){
                JSONObject obj = (JSONObject) queue.get(i);
                if(obj.length()>4){
                    if(globalPage.getInt(StaticValues.PARAM_CS_P_V)!=0){
                        target.put(globalPage);
                    }
                    target.put(obj);
                    globalPage = new JSONObject();
                    globalPage.put(StaticValues.PARAM_VT_TZ,BSConfig.getCurrentDateString());
                    globalPage.put(StaticValues.PARAM_VS,0);
                    globalPage.put(StaticValues.PARAM_CS_P_V,0);
                }else{
                    globalPage.put(StaticValues.PARAM_VT_TZ,obj.getString(StaticValues.PARAM_VT_TZ));
                    globalPage.put(StaticValues.PARAM_VS,globalPage.getInt(StaticValues.PARAM_VS)+obj.getInt(StaticValues.PARAM_VS));
                    globalPage.put(StaticValues.PARAM_CS_P_V,globalPage.getInt(StaticValues.PARAM_CS_P_V)+obj.getInt(StaticValues.PARAM_CS_P_V));
                }
            }
            if(globalPage.getInt(StaticValues.PARAM_CS_P_V)!=0){
                target.put(globalPage);
            }
        } catch (JSONException e) {
            BSDebugger.log(e,this);
        }
        return target;
    }

    private JSONArray getQueue(String queueFileName, String documentID) {
        JSONArray targetArray = new JSONArray();
        QueueFile targetQueueFile = null;
        try {
            targetQueueFile = new QueueFile(new File(mContext.getFilesDir().getPath()+queueFileName+documentID));
            while (!targetQueueFile.isEmpty()){
                byte[] bytes = targetQueueFile.peek();
                targetQueueFile.remove();
                JSONObject goalRow = null;
                try {
                    String targetString = new String(bytes);
                    goalRow = new JSONObject(targetString);
                } catch (JSONException e) {
                    BSDebugger.log(e,this);
                    goalRow = new JSONObject();
                }
                targetArray.put(goalRow);
            }
        } catch (IOException e) {
            BSDebugger.log(e, this);
        }
        return targetArray;
    }

    private JSONObject getSessionConfigJson(){
        JSONObject sessionJson = new JSONObject();
        try {
            sessionJson.put(StaticValues.PARAM_DEBUG,sessionManager.getConfig().getDebug());
            sessionJson.put(StaticValues.PARAM_AK,sessionManager.getConfig().getAk());
            sessionJson.put(StaticValues.PARAM_PFNO,sessionManager.getConfig().getPfno());
            sessionJson.put(StaticValues.PARAM_UUID,sessionManager.getConfig().getUuid());
            sessionJson.put(StaticValues.PARAM_ADVID,sessionManager.getConfig().getAdvId());
            sessionJson.put(StaticValues.PARAM_INSTALL_DATE,sessionManager.getConfig().getInstallDate());
            sessionJson.put(StaticValues.PARAM_AP_VR,sessionManager.getConfig().getApVr());
            sessionJson.put(StaticValues.PARAM_OS,sessionManager.getConfig().getOs());
            sessionJson.put(StaticValues.PARAM_PHONE,sessionManager.getConfig().getPhone());
            sessionJson.put(StaticValues.PARAM_TEL_COM,sessionManager.getConfig().getTelCom());
            sessionJson.put(StaticValues.PARAM_WIFI_TP,sessionManager.getConfig().getWifiTp());
            sessionJson.put(StaticValues.PARAM_SR,sessionManager.getConfig().getSr());
            sessionJson.put(StaticValues.PARAM_CNTR,sessionManager.getConfig().getCntr());
            sessionJson.put(StaticValues.PARAM_LNG,sessionManager.getConfig().getLng());
            sessionJson.put(StaticValues.PARAM_INCH,sessionManager.getConfig().getInch());
            sessionJson.put(StaticValues.PARAM_TZ,sessionManager.getConfig().getTz());
        } catch (JSONException e) {
            BSDebugger.log(e,this);
        }
        return sessionJson;
    }
    private JSONObject putSessionDefault(JSONObject sessionJson){
        try {
            String visitNew =sessionManager.getVisitNew();
            int ltvt = documentDB.getInt(StaticValues.PARAM_LTVT);
            if(visitNew.equalsIgnoreCase("Y")){
                //신규 방문 시 이벤트 처리
                ltvt++;
                documentDB.putInt(StaticValues.PARAM_LTVT,ltvt);
//                long recentVisitPtm = documentDB.getLong(StaticValues.PARAM_RECENT_VISIT_PTM);
//                long currentTimeSec = System.currentTimeMillis()/1000;
                if(ltvt==1){
                    initRecentVisitPTime();
                    profiler.initOrderPTime();
                }
                updateLtvi();
                updateUdVt();
            }else{
            }

            sessionJson.put(StaticValues.PARAM_RESPONSE_TP,"json");
            sessionJson.put(StaticValues.PARAM_LTVT, ltvt);
            sessionJson.put(StaticValues.PARAM_SID,sessionManager.getSid(false));
            sessionJson.put(StaticValues.PARAM_SLOT_NO,sessionManager.getSlotNo(sessionManager.getSession(false)));
            sessionJson.put(StaticValues.PARAM_VISIT_NEW,visitNew);
            sessionJson.put(StaticValues.PARAM_VT_TZ,sessionManager.getVtTz(sessionManager.getSession(false)));
            sessionJson.put(StaticValues.PARAM_IS_UNI_VT,getUniVt(visitNew));
        } catch (JSONException e) {
            BSDebugger.log(e,this);
        }

        return sessionJson;
    }
    private JSONObject createDocument() {
        //문서의 명명 규칙 : 세션ID_timestamp.json
        //문서를 생성하고 저장한 후
        JSONObject toCreateJson = new JSONObject();
        try {
            JSONObject sessionJson = getSessionConfigJson();
            sessionJson = putSessionDefault(sessionJson);
            sessionJson = putSessionDBDefault(sessionJson);
            sessionJson = putSessionProfile(sessionJson);



            //광고 이벤트
            sessionJson.put(StaticValues.PARAM_MAT_SOURCE,documentDB.getString(StaticValues.PARAM_MAT_SOURCE));
            sessionJson.put(StaticValues.PARAM_MAT_MEDIUM,documentDB.getString(StaticValues.PARAM_MAT_MEDIUM));
            sessionJson.put(StaticValues.PARAM_MAT_KWD,documentDB.getString(StaticValues.PARAM_MAT_KWD));
            sessionJson.put(StaticValues.PARAM_MAT_CAMPAIGN,documentDB.getString(StaticValues.PARAM_MAT_CAMPAIGN));

            //광고 설치
            sessionJson.put(StaticValues.PARAM_IAT_SOURCE,documentDB.getString(StaticValues.PARAM_IAT_SOURCE));
            sessionJson.put(StaticValues.PARAM_IAT_MEDIUM,documentDB.getString(StaticValues.PARAM_IAT_MEDIUM));
            sessionJson.put(StaticValues.PARAM_IAT_KWD,documentDB.getString(StaticValues.PARAM_IAT_KWD));
            sessionJson.put(StaticValues.PARAM_IAT_CAMPAIGN,documentDB.getString(StaticValues.PARAM_IAT_CAMPAIGN));

            //todo 전환타입
            sessionJson.put(StaticValues.PARAM_CONV_TP,getConvTp());

            //todo facebook
            sessionJson.put(StaticValues.PARAM_UD_RVNC,profiler.getFbSource());

            //todo GDN 데이터
            sessionJson.put(StaticValues.PARAM_UTM_CAMPAIGN,documentDB.getString(StaticValues.PARAM_UTM_CAMPAIGN));
            sessionJson.put(StaticValues.PARAM_UTM_CONTENT,documentDB.getString(StaticValues.PARAM_UTM_CONTENT));
            sessionJson.put(StaticValues.PARAM_UTM_MEDIUM,documentDB.getString(StaticValues.PARAM_UTM_MEDIUM));
            sessionJson.put(StaticValues.PARAM_UTM_SOURCE,documentDB.getString(StaticValues.PARAM_UTM_SOURCE));
            sessionJson.put(StaticValues.PARAM_UTM_TERM,documentDB.getString(StaticValues.PARAM_UTM_TERM));
            sessionJson.put(StaticValues.PARAM_GCLID,documentDB.getString(StaticValues.PARAM_GCLID));

            toCreateJson.put(TrackType.TYPE_SESSION.toString(),sessionJson);

        } catch (JSONException e) {
            BSDebugger.log(e,this);
        }

        String documentID = sessionManager.getSession(false)+"_"+String.valueOf(System.currentTimeMillis());
        saveFile(documentID,toCreateJson);
        documetNameList.add(documentID);
        documentDB.putList(DOCUMENT_TARGET_NAME,documetNameList);
        //저장하면 현재 문서의 재방문 정보를 업데이트 해준다.
        sessionManager.updateLastSessionID(sessionManager.getSession(false));
        currentDocument =toCreateJson;
        return toCreateJson;
    }

    public static String getLastDocumentID(Context context){
        TinyDB db = new TinyDB(context,DOCUMENT_DB_NAME);
        ArrayList<String> nameList = db.getList(DOCUMENT_TARGET_NAME);
        if(nameList.isEmpty()){
            return "";//document is empty
        }
        return nameList.get(nameList.size()-1);
    }
    private JSONObject putSessionProfile(JSONObject sessionJson) {
        try {
            sessionJson.put(StaticValues.PARAM_MBR,profiler.getMbr());
            sessionJson.put(StaticValues.PARAM_GENDER,profiler.getGender());
            sessionJson.put(StaticValues.PARAM_AGE,profiler.getAge());
            sessionJson.put(StaticValues.PARAM_UVP1,profiler.getUvp1());
            sessionJson.put(StaticValues.PARAM_UVP2,profiler.getUvp2());
            sessionJson.put(StaticValues.PARAM_UVP3,profiler.getUvp3());
            sessionJson.put(StaticValues.PARAM_UVP4,profiler.getUvp4());
            sessionJson.put(StaticValues.PARAM_UVP5,profiler.getUvp5());

            //주문 처리 이벤트는 프로파일러가 해줌
            sessionJson.put(StaticValues.PARAM_LTRVNC,profiler.getLtrvnc());
            sessionJson.put(StaticValues.PARAM_LTRVNI,profiler.getLtrvni());
            sessionJson.put(StaticValues.PARAM_UD_RVNC,profiler.getUdRvnc());
        } catch (JSONException e) {
            BSDebugger.log(e,this);
        }

        return sessionJson;
    }

    private JSONObject putSessionDBDefault(JSONObject sessionJson) {

        try {
            sessionJson.put(StaticValues.PARAM_LTVI, documentDB.getInt(StaticValues.PARAM_LTVI));
            sessionJson.put(StaticValues.PARAM_UD_VT,documentDB.getInt(StaticValues.PARAM_UD_VT));
        } catch (JSONException e) {
            BSDebugger.log(e,this);
        }
        return sessionJson;
    }

    private void saveFile(final String _documentID,final JSONObject document) {
        if(mContext instanceof Activity){
            AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    requestSave(_documentID,document);
                    return null;
                }
            };
            task.execute();
        }else{
            requestSave(_documentID,document);
        }
    }

    void requestSave(final String _documentID,final JSONObject document){
        try {
            FileOutputStream outputStream = new FileOutputStream(mContext.getFilesDir().getPath()+"/"+_documentID);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            try {
                bufferedWriter.write(document.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStreamWriter.close();
                outputStream.close();
            } catch (IOException e) {
                BSDebugger.log(e,DocumentManager.this);
            }

        } catch (FileNotFoundException e) {
            BSDebugger.log(e,DocumentManager.this);
        }
    }
    private int getConvTp() {
        //todo 만들어야함.
        return 0;
    }

    private void updateUdVt() {
        int returnVisitDate = sessionManager.getConfig().getReturnVisitDate();
        if(returnVisitDate>0){
            long recentVisitPtm = documentDB.getLong(StaticValues.PARAM_RECENT_VISIT_PTM);
            long currentTimeSec = System.currentTimeMillis()/1000;
            int udVt = documentDB.getInt(StaticValues.PARAM_UD_VT);
            long interval = Math.round((currentTimeSec-recentVisitPtm)/60/60/24);
            if(interval>=returnVisitDate){
                udVt = 1;
            }else{
                udVt +=1;
            }
            documentDB.putInt(StaticValues.PARAM_UD_VT,udVt);
        }

    }

    private void updateLtvi() {
        long recentVisitPtm = documentDB.getLong(StaticValues.PARAM_RECENT_VISIT_PTM);
        long currentTimeSec = System.currentTimeMillis()/1000;

        //  앱을 최초 실행 하였을 경우 recentVisitPtm 값은 없으므로 ltvi가 잘못 계살될 수 있음.
        //  recentVisitPtm가 0보다 크면 interval 계산.
        int interval = 0;
        if( recentVisitPtm > 0l ){
            interval = Math.round((currentTimeSec-recentVisitPtm)/60/60/24);
        }

        int ltvi = documentDB.getInt(StaticValues.PARAM_LTVI);
        ltvi += interval;
        documentDB.putInt(StaticValues.PARAM_LTVI,ltvi);
        documentDB.putLong(StaticValues.PARAM_RECENT_VISIT_PTM, currentTimeSec);
    }

    private void initRecentVisitPTime(){
        long currentTimeSec = System.currentTimeMillis()/1000;
        documentDB.putLong(StaticValues.PARAM_RECENT_VISIT_PTM, currentTimeSec);
    }

    private int getUniVt(String visitNew) {

        boolean newVisitToday = false;
        String lastDateString = documentDB.getString(StaticValues.LAST_DATE_STRING);

        long expireTimeForToday = BSUtils.getExpireLongTimeForUniVt(lastDateString, BSUtils.DAILY_UNIQUE);
        boolean newVisitThisWeek = false;
        long expireTimeForWeek = BSUtils.getExpireLongTimeForUniVt(lastDateString,  BSUtils.WEEKLY_UNIQUE);
        boolean newVisitThisMonth = false;
        long expireTimeForMonth = BSUtils.getExpireLongTimeForUniVt(lastDateString, BSUtils.MONTHLY_UNIQUE);

        Calendar today = Calendar.getInstance();
        if( today.getTimeInMillis() > expireTimeForToday  ){
            newVisitToday = true;
        }
        if( today.getTimeInMillis() > expireTimeForWeek ){
            newVisitThisWeek = true;
        }
        if( today.getTimeInMillis() > expireTimeForMonth ){
            newVisitThisMonth = true;
        }

        /**
         전달되는 값의 의미는 다음과 같다.
         0 값 : 일,주,월 모두에 대하여 순수방문이 아님.
         1 값 : 일 순수방문만 해당
         2 값 : 일, 주 순수 방문에 해당
         3 값 : 일, 주, 월 순수 방문에 해당함.
         4 값 : 일, 월 순수 방문에 해당함
         * **/
        // 신규 세션이 발급된 시점이면,
        if(visitNew.equalsIgnoreCase("Y")){
            if( newVisitToday && !newVisitThisWeek  && !newVisitThisMonth){
                return 1;
            }else if( newVisitToday && newVisitThisWeek  && !newVisitThisMonth){
                return 2;
            }else if( newVisitToday && newVisitThisWeek && newVisitThisMonth ){
                return 3;
            }else if( newVisitToday && !newVisitThisWeek  && newVisitThisMonth){
                return 4;
            }else{
                return 0;
            }
        }else{
            return 0;
        }

        /***
        //현재시간
        Calendar mCalendar = new GregorianCalendar();
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH) +1;
        int date = mCalendar.get(Calendar.DATE);

        //현재날짜
        String dateString = String.format("%d-%d-%d",year,month,date);
        String lastDateString = documentDB.getString(StaticValues.LAST_DATE_STRING);

        if(!dateString.equalsIgnoreCase(lastDateString)){
            //일 순수 방문
            newVisitToday = true;
            documentDB.putString(StaticValues.LAST_DATE_STRING,dateString);
        }

        String monthString = String.format("%d-%d",year,month);
        String lastMonthString = documentDB.getString(StaticValues.LAST_MONTH_STRING);
        if(!monthString.equalsIgnoreCase(lastMonthString)) {
            //월 순수 방문
            newVisitThisMonth = true;
            documentDB.putString(StaticValues.LAST_MONTH_STRING,monthString);
        }

        //주 순수 방문
        int dayOfWeek = mCalendar.get(Calendar.DAY_OF_WEEK);
        int haveToAdd = 7-dayOfWeek;
        mCalendar.add(Calendar.DATE,haveToAdd);
        int weekOfYear = mCalendar.get(Calendar.WEEK_OF_YEAR);
        int lastWeekOfYear = documentDB.getInt(StaticValues.LAST_WEEK_INT);
        if(lastWeekOfYear!=weekOfYear){
            newVisitThisWeek = true;
        }
        if(newVisitToday){
            if(!newVisitThisWeek&&!newVisitThisMonth){
                return 1;
            }else if(newVisitThisWeek&&!newVisitThisMonth){
                return 2;
            }else if(newVisitThisWeek&&newVisitThisMonth){
                return 3;
            }else{
                return 4;
            }
        }else{
            return 0;
        }
        ***/

//        return 1;
    }

    public static DocumentManager getInstance(Context context, BSSession session){
        if(instance == null){
            instance = new DocumentManager(context,session);
        }
        return instance;
    }

    public void putInstallData(HashMap<String, String> map) {
        try {
            if(map.containsKey(StaticValues.PARAM_UTM_CAMPAIGN)) {
                //GDN 이벤트 등록
                documentDB.putString(StaticValues.PARAM_UTM_CAMPAIGN, map.get(StaticValues.PARAM_UTM_CAMPAIGN));
                documentDB.putString(StaticValues.PARAM_UTM_SOURCE, map.get(StaticValues.PARAM_UTM_SOURCE));
                documentDB.putString(StaticValues.PARAM_UTM_MEDIUM, map.get(StaticValues.PARAM_UTM_MEDIUM));
                documentDB.putString(StaticValues.PARAM_UTM_TERM, map.get(StaticValues.PARAM_UTM_TERM));
                documentDB.putString(StaticValues.PARAM_UTM_CONTENT, map.get(StaticValues.PARAM_UTM_CONTENT));
                documentDB.putString(StaticValues.PARAM_GCLID, map.get(StaticValues.PARAM_GCLID));

                //광고분석 데이터 등록
                documentDB.putString(StaticValues.PARAM_MAT_CAMPAIGN, map.get(StaticValues.PARAM_UTM_CAMPAIGN));
                documentDB.putString(StaticValues.PARAM_MAT_SOURCE, map.get(StaticValues.PARAM_UTM_SOURCE));
                documentDB.putString(StaticValues.PARAM_MAT_MEDIUM, map.get(StaticValues.PARAM_UTM_MEDIUM));
                documentDB.putString(StaticValues.PARAM_MAT_KWD, map.get(StaticValues.PARAM_UTM_TERM));

                //설치 광고 데이터 등록
                documentDB.putString(StaticValues.PARAM_IAT_CAMPAIGN, map.get(StaticValues.PARAM_UTM_CAMPAIGN));
                documentDB.putString(StaticValues.PARAM_IAT_SOURCE, map.get(StaticValues.PARAM_UTM_SOURCE));
                documentDB.putString(StaticValues.PARAM_IAT_MEDIUM, map.get(StaticValues.PARAM_UTM_MEDIUM));
                documentDB.putString(StaticValues.PARAM_IAT_KWD, map.get(StaticValues.PARAM_UTM_TERM));
            }
        } catch (Exception e) {
            BSDebugger.log(e,this);
        }

    }


    public void setGoal(int key, int value) {
        ArrayList<Integer> arr = new ArrayList<Integer>();
        arr.add(key);
        arr.add(value);
        documentDB.putListInt(StaticValues.PARAM_G,arr,mContext);
    }
    private String getLaistDocId(){
        return documetNameList.get(documetNameList.size()-1);
    }

    public void putGoal(){
        try {
            QueueFile goalQueue = new QueueFile(new File(mContext.getFilesDir().getPath()+StaticValues.GOAL_FILE_PREFIX+getLaistDocId()));
            JSONObject goalJson = new JSONObject();
            try {
                goalJson.put(StaticValues.PARAM_VT_TZ,sessionManager.getConfig().getCurrentDateString());
                goalJson.put(StaticValues.PARAM_G+String.valueOf(documentDB.getListInt(StaticValues.PARAM_G,mContext).get(0)),documentDB.getListInt(StaticValues.PARAM_G, mContext).get(1));
                goalJson.put(StaticValues.PARAM_PNC,documentDB.getString(StaticValues.PARAM_PNC));
                goalJson.put(StaticValues.PARAM_PNC_NM,documentDB.getString(StaticValues.PARAM_PNC_NM));
                goalJson.put(StaticValues.PARAM_PNG,documentDB.getString(StaticValues.PARAM_PNG));
                goalJson.put(StaticValues.PARAM_PNG_NM,documentDB.getString(StaticValues.PARAM_PNG_NM));
                goalQueue.add(goalJson.toString().getBytes());
                goalQueue.close();
            } catch (JSONException e) {
                BSDebugger.log(e, this);
            }
        } catch (IOException e) {
            BSDebugger.log(e, this);
        }
    }
    public void setProduct(String code,String name) {
        if(name==null){
            name = "";
        }
        documentDB.putString(StaticValues.PARAM_PNC,code);
        documentDB.putString(StaticValues.PARAM_PNC_NM,name);
    }

    public void setEvent(String type, String name, String value) {
        try {
            QueueFile targetQueue = new QueueFile(new File(mContext.getFilesDir().getPath()+StaticValues.EVENT_FILE_PREFIX+getLaistDocId()));
            JSONObject targetJson = new JSONObject();
            try {
                targetJson.put(StaticValues.PARAM_VT_TZ,sessionManager.getConfig().getCurrentDateString());
                targetJson.put(StaticValues.PARAM_TYPE,type);
                targetJson.put(StaticValues.PARAM_NAME,name);
                targetJson.put(StaticValues.PARAM_VALUE,value);
                targetQueue.add(targetJson.toString().getBytes());
                targetQueue.close();
            } catch (JSONException e) {
                BSDebugger.log(e, this);
            }
        } catch (IOException e) {
            BSDebugger.log(e, this);
        }
    }

    public void setCampaign(String type, String name, String value, String referer) {
        try {
            QueueFile targetQueue = new QueueFile(new File(mContext.getFilesDir().getPath()+StaticValues.CAMPAIGN_FILE_PREFIX+getLaistDocId()));
            JSONObject targetJson = new JSONObject();
            try {
                targetJson.put(StaticValues.PARAM_VT_TZ,sessionManager.getConfig().getCurrentDateString());
                targetJson.put(StaticValues.PARAM_TYPE,type);
                targetJson.put(StaticValues.PARAM_NAME,name);
                targetJson.put(StaticValues.PARAM_VALUE,value);
                targetJson.put(StaticValues.PARAM_REFERER,referer);
                targetQueue.add(targetJson.toString().getBytes());
                targetQueue.close();
            } catch (JSONException e) {
                BSDebugger.log(e, this);
            }
        } catch (IOException e) {
            BSDebugger.log(e, this);
        }
    }

    public void logView(final String view_command,final String name) {
        AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    QueueFile targetQueue = new QueueFile(new File(mContext.getFilesDir().getPath()+StaticValues.VIEW_FILE_PREFIX+getLaistDocId()));
                    JSONObject targetJson = new JSONObject();
                    try {
                        targetJson.put(StaticValues.PARAM_VT_TZ,sessionManager.getConfig().getCurrentDateString());
                        targetJson.put(StaticValues.PARAM_STATUS,view_command);
                        targetJson.put(StaticValues.PARAM_NAME,name);
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
    }

    public void startSession() {
        sessionManager.createSession();
        loadDocument();
    }

    public void setProductCategory(String code, String name) {
        if(name==null){
            name = "";
        }
        documentDB.putString(StaticValues.PARAM_PNG,code);
        documentDB.putString(StaticValues.PARAM_PNG_NM,name);
    }

    public void setOrderProduct(String[] code, String[] name) {
        if(name==null){
            name = new String[]{};
        }
        try {
            QueueFile targetQueue = new QueueFile(new File(mContext.getFilesDir().getPath()+StaticValues.REVENUE_FILE_PREFIX+getLaistDocId()));
            JSONObject targetJson = new JSONObject();
            try {
                targetJson.put(StaticValues.PARAM_VT_TZ,sessionManager.getConfig().getCurrentDateString());
                targetJson.put(StaticValues.PARAM_AMT, getOrderAmount());
                targetJson.put(StaticValues.PARAM_EA,getOrderQuantity());
                targetJson.put(StaticValues.PARAM_PNC,code);
                targetJson.put(StaticValues.PARAM_PNG,name);
                targetQueue.add(targetJson.toString().getBytes());
                targetQueue.close();
            } catch (JSONException e) {
                BSDebugger.log(e, this);
            }
        } catch (IOException e) {
            BSDebugger.log(e, this);
        }
    }

    private String getOrderAmount() {
        String ret = documentDB.getString(StaticValues.PARAM_AMT);
        documentDB.remove(StaticValues.PARAM_AMT);
        return ret;
    }

    public void setPageIdentity(String value) {
        String pageID = pageDB.getString(value);
        if(pageID.equalsIgnoreCase("")){
            pageDB.putString(value,value);
        }
        final int pageVS = pageDB.getInt(pageID+StaticValues.PARAM_VS);
//        pageVS++;//나중에 시간으로 변환해야함.
        pageDB.putInt(pageID+StaticValues.PARAM_VS,pageVS+1);
        final int csPV = pageDB.getInt(pageID+StaticValues.PARAM_CS_P_V);
//        csPV++; //이 부분도 로직을 좀 개선할 필요가 있음. 도큐먼트 보낼때 마다 계산해야 하나?
        pageDB.putInt(pageID+StaticValues.PARAM_CS_P_V,csPV+1);

        AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    QueueFile targetQueue = new QueueFile(new File(mContext.getFilesDir().getPath()+StaticValues.PAGES_FILE_PREFIX+getLaistDocId()));
                    JSONObject targetJson = new JSONObject();
                    try {
                        targetJson.put(StaticValues.PARAM_VT_TZ,sessionManager.getConfig().getCurrentDateString());
                        targetJson.put(StaticValues.PARAM_VS,pageVS);
                        targetJson.put(StaticValues.PARAM_CS_P_V,csPV);
                        targetJson.put(StaticValues.PARAM_CP,documentDB.getString(StaticValues.PARAM_CP));
                        targetJson.put(StaticValues.PARAM_PI,"");
                        targetJson.put(StaticValues.PARAM_PNC,documentDB.getString(StaticValues.PARAM_PNC));
                        targetJson.put(StaticValues.PARAM_PNG,documentDB.getString(StaticValues.PARAM_PNG));
                        targetJson.put(StaticValues.PARAM_PNG_NM,documentDB.getString(StaticValues.PARAM_PNG_NM));
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
    }

    public void setOrderAmount(int[] value) {
        documentDB.putString(StaticValues.PARAM_AMT, BSUtils.joinIntToString(value));
    }

    public void setOrderQuantity(int[] value) {
        documentDB.putString(StaticValues.PARAM_EA,BSUtils.joinIntToString(value));
    }

    private String getOrderQuantity(){
        String ret = documentDB.getString(StaticValues.PARAM_EA);
        documentDB.remove(StaticValues.PARAM_EA);
        return ret;
    }

    public void setOrderProductCategory(String[] code, String[] name) {
        documentDB.putString(StaticValues.PARAM_PNC,BSUtils.joinStringToString(code));
        documentDB.putString(StaticValues.PARAM_PNC_NM,BSUtils.joinStringToString(name));
    }

    public void setContents(String value) {
        documentDB.putString(StaticValues.PARAM_CP,value);
    }

    public void addCsPV() {
        sessionManager.addCsPV();
    }

    public int getCsPV() {
        return sessionManager.getCsPV(sessionManager.getSid(true));
    }
}
