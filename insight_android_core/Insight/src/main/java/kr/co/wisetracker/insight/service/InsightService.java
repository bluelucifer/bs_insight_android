package kr.co.wisetracker.insight.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.wisetracker.insight.WiseTracker;
import kr.co.wisetracker.insight.lib.config.BSConfig;
//import kr.co.wisetracker.insight.lib.config.BSLocalConfig;
import kr.co.wisetracker.insight.lib.network.CallBackInterface;
import kr.co.wisetracker.insight.lib.network.RestTask;
import kr.co.wisetracker.insight.lib.tracker.BSSession;
import kr.co.wisetracker.insight.lib.tracker.DocumentManager;
import kr.co.wisetracker.insight.lib.tracker.Profiler;
import kr.co.wisetracker.insight.lib.util.BSDebugger;
import kr.co.wisetracker.insight.lib.util.BSUtils;
import kr.co.wisetracker.insight.lib.util.JsonStringCompress;
import kr.co.wisetracker.insight.lib.values.SignalIndex;
import kr.co.wisetracker.insight.lib.values.StaticValues;
import kr.co.wisetracker.insight.lib.values.TrackType;


/**
 * Created by caspar on 14. 9. 8.
 * @version 1.0
 * @since 14. 9. 8.
 */
public class InsightService extends Service {

    /**
     * 변수 필드
     */
    private Context mContext;

    private BSConfig config = null;

    private BSSession sessionManager = null;

    private DocumentManager documentManager = null;

    private int retryCount = 0;

    Timer commandTiemr = null;

    Timer sessionTimer = null;
    private long st_send_time;

    /**
     * 외부 호출 메소드
     */

    public void onCreate(){

        retryCount = 0;
        mContext = this;
        config = BSConfig.getInstance(mContext);
        sessionManager = BSSession.getInstance(mContext,config);
        documentManager = DocumentManager.getInstance(mContext,sessionManager);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        retryCount++;
        final Intent fIntent = intent;
        final int fFlags = flags;
        final int fStartId = startId;
        commandProcesser(fIntent,fFlags,fStartId);
        return START_STICKY;
    }


    Timer sendTimer;
    TimerTask sendTimertask;
    /**
     * 프라이빗 메소드
     */
    private void commandProcesser(final Intent intent, final int flags, final int startId){
        try{
            if(intent!=null){
                String action = intent.getAction();
                if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER","InsightService.commandProcesser  action Name : "+ action ); }
                //Activity활동
                if(action.equalsIgnoreCase(SignalIndex.BOOT_COMPLEATE)){
                    //알람이 들어오면
                    //리텐션을 보내고
                    sendRetention();
                    //알람을 다시 등록한다.
                    installAlarmManager();
                }else if(action.equalsIgnoreCase(SignalIndex.ON_START_SESSION)){
                    //앱이 시작됨. 세션 처리를 하자.
//                documentManager.startSession();
                }else if(action.equalsIgnoreCase(SignalIndex.ON_END_SESSION)){
                    //세션이 종료되면 도큐먼트를 전송한다.
//                documentManager.sendDocumet();
                    //타이머를 두고 종료할까?
                }else if(action.equalsIgnoreCase(SignalIndex.SEND_TRANSACTION)){
                    //시그널이 들어오면 서버에 데이터를 즉시 전송한다.
                    documentManager.createDocument();
                    sendTimertask = new TimerTask() {
                        @Override
                        public void run() {
                            documentManager.sendDocumet();
                        }
                    };
                    if(sendTimer == null){
                        sendTimer  = new Timer();
                    }else{
                        sendTimer.cancel();
                        sendTimer  = new Timer();
                    }
                    sendTimer.schedule(sendTimertask,3000);

                }else if(action.equalsIgnoreCase(SignalIndex.SEND_MODE_SCHEDULE)){
                    //세션 시작 시간을 기록
                    st_send_time = intent.getLongExtra(StaticValues.ST_SEND_TIME,-1);
                    scheduleSend();
                }else if(action.equalsIgnoreCase(SignalIndex.SEND_ALARM)){
                    documentManager.createDocument();
                    documentManager.sendDocumet();
                    long currentTimeMillis = System.currentTimeMillis();
                    long diff = currentTimeMillis - st_send_time;

                    //세션 마지막 작업이 세션 타임아웃을 지나지 않으면 스케쥴러를 다시 돌린다.
                    if(diff<=(BSConfig.getInstance(this).getSessionTime()*1000)) {
                        scheduleSend();
                    }
                }else if(action.equalsIgnoreCase(SignalIndex.INIT)){
                    //앱이 시작되면 세션을 만들어서 전송한다.
                    //도큐먼트를 전송하기 전에 새 문서를 발행해서 이전 문서에 비동기로 큐가 생기는 것을 방지함.
                    documentManager.createDocument();
//                documentManager.sendDocumet();
                }else if(action.equalsIgnoreCase(SignalIndex.UPDATE_DOCUMENT)){
                    //앱이 시작되면 세션을 만들어서 전송한다.
                    documentManager.updateDocument();
                    //세션 마지막 작업 시간 기록
                    st_send_time = intent.getLongExtra(StaticValues.ST_SEND_TIME,-1);
                }else if(action.equalsIgnoreCase(SignalIndex.CREATE_NEW_DOCUMENT)){
                    //세션 마지막 작업 시간 기록
                    st_send_time = intent.getLongExtra(StaticValues.ST_SEND_TIME,-1);
                    documentManager.createDocument();
                }else if(action.equalsIgnoreCase(SignalIndex.ACTIVATE_DEBUG)){
                    //앱이 시작되면 세션을 만들어서 전송한다.
                    boolean debugFlag = intent.getBooleanExtra(StaticValues.DEBUG_FLAG,false);
                    config.putPref(StaticValues.DEBUG_FLAG, debugFlag);
                }else if(action.equalsIgnoreCase(SignalIndex.ACTIVATE_ALARM)){
                    //알람이 들어오면
                    //리텐션을 보내고
                    sendRetention();
                    //알람을 다시 등록한다.
                    installAlarmManager();
                }else if(action.equalsIgnoreCase(StaticValues.INSTALL_REFERRER)){
                    //앱 설치 레퍼러 정보가 들어온다.
                    Map<String,String> map = new HashMap<String,String>();
                    String referrerString = "";
                    // deeplink
                    if( intent != null ) {
                        Uri uri = intent.getData();
                        if (uri != null &&
                                uri.toString().matches(".*"+StaticValues.WT_REFERRER_REGEXP+".*")
                                ) {
                            referrerString = uri.toString();
                            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER","[InsightService.uri ::" + uri.toString() ); }
                        } else {
                            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER","[InsightService.uri is null ]" ); }
                            // ###############################################################################################
                            // EXTRA AREA
                            Bundle extras = intent.getExtras();
                            if( extras != null ) {
                                if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "[InsightService.extras.keySet().toString()] " + extras.keySet().toString()); }
                                String extraItemKey = "";
                                Iterator<String> extrasItr = extras.keySet().iterator();
                                while( extrasItr.hasNext() ){
                                    extraItemKey = extrasItr.next();
                                    if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "[InsightService.extras."+extraItemKey+" : " + String.valueOf(extras.get(extraItemKey)) ); }
                                    if( extraItemKey.equals(StaticValues.WT_REFERRER_NAME) ){
                                        referrerString += String.valueOf(extras.get(extraItemKey));
                                    }else if( extraItemKey.equals(StaticValues.PARAM_PUSH_MESSAGE_KEY) ){
                                        referrerString += "&ocmp="+String.valueOf(extras.get(extraItemKey));
                                    }
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
                                if(extras.containsKey(StaticValues.PARAM_FB_SOURCE)){
                                    String fbSource = extras.getString(StaticValues.PARAM_FB_SOURCE);
                                    Profiler.getInstance(this).putFbSource(fbSource);
                                }
                            }else{
                                if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER", "[ InsightService.extras.keySet().is empty ] "); }
                            }
                        }
                    }
                    if( referrerString != null ){
                        map = BSUtils.parseReferrer(referrerString);
                        documentManager.putInstallData(map);
                        if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER",  "[InsightService.referrerString] : "+ referrerString ); }
                        if( map != null ){
                            if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER","[InsightService.map.toString()] "+ map.toString()  ); }
                        }
                    }else{
                        if( WiseTracker.FLAG_OF_PRINT_LOG ){ Log.i("DEBUG_WISETRACKER",  "[InsightService.referrerString] is null "); }
                    }

                    documentManager.putInstallData(map);
                    installAlarmManager();
                    documentManager.updateDocument();
                    documentManager.updateInstallDocument();
                    config.putPref(StaticValues.INSTALL_CHECKED,true);
                    //브로드케스트 전달
                    try {
                        ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
                        Bundle bundle = ai.metaData;
                        String targetClass = bundle.getString("TrackHandOver");
                        try {
                            intent.setClass(mContext, Class.forName(targetClass));
                            sendBroadcast(intent);
                        } catch (ClassNotFoundException e) {
                        }

                    } catch (PackageManager.NameNotFoundException e) {
//                    Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
                    } catch (NullPointerException e) {
//                    Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
                    }
                }else if(action.equalsIgnoreCase(SignalIndex.PACKAGE_ADDED)
                        ||action.equalsIgnoreCase(SignalIndex.PACKAGE_CHANGED)
                        ||action.equalsIgnoreCase(SignalIndex.MY_PACKAGE_REPLACED)
                        ||action.equalsIgnoreCase(SignalIndex.PACKAGE_REPLACED)){
                    installAlarmManager();
                }
            }
        }catch(Exception ex){
            BSDebugger.log(ex);
        }
    }
    private void scheduleSend() {
        int dataSendMode = BSConfig.getInstance(this).getDataSendMode();
        if(dataSendMode==2){
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(SignalIndex.SEND_ALARM);
            intent.putExtra(StaticValues.ST_SEND_TIME,st_send_time);
            PendingIntent pIntent = PendingIntent.getService(this, 0, intent, 0);
            alarmManager.cancel(pIntent);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + BSConfig.getInstance(this).getReportTime() * 1000, pIntent);
//            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() +  1000, pIntent);
        }
    }

    private void sendRetention() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        String currentDateString = formatter.format(currentDate);
        long currentTimestamp = 0;
        try {
            currentTimestamp = formatter.parse(currentDateString).getTime();
        } catch (ParseException e) {
            BSDebugger.log(e);
        }
        Long lastTimestamp = (Long) config.getPrefValue(StaticValues.RETENTION_SEND_DATE,Long.class);
        if(currentTimestamp <= lastTimestamp){
           return;
        }
        final long finalCurrentTimestamp = currentTimestamp;
        if(!Profiler.getInstance(this).shouldSend(false)){
            return;
        }
        JSONObject retentionObject = new JSONObject();
        JSONObject sessionJson = new JSONObject();
        try {
            BSConfig bsConfig = BSConfig.getInstance(this);
            Profiler profiler = Profiler.getInstance(this);

            // ##########################################################
            // 설치후 35일 이내 인지 체크
            Calendar now = Calendar.getInstance();
            int interval = BSUtils.getCalDayDiff(
                    now.getTimeInMillis(),
                    BSUtils.stringToYyyyMmDdHhMmSsLong((String)bsConfig.getPrefValue(StaticValues.PREF_INSTALL_DATE,String.class))
            );
            if( interval >= 35 ){
                return;
            }
            // ##########################################################
            sessionJson.put(StaticValues.PARAM_RESPONSE_TP,"json"); // Retention 전송시 응답 타입 정의.
            sessionJson.put(StaticValues.PARAM_AK,bsConfig.getAk());
            sessionJson.put(StaticValues.PARAM_PFNO,bsConfig.getPfno());
            sessionJson.put(StaticValues.PARAM_INSTALL_DATE,bsConfig.getInstallDate());
            // #############################################################################
            // sessionJson.put(StaticValues.PARAM_VT_TZ,bsConfig.getCurrentDateString());
            String retentionVtTz = "";
            long t = (Long)config.getPrefValue(StaticValues.RETENTION_SEND_DATE,Long.class);
            if( ((Long)config.getPrefValue(StaticValues.RETENTION_SEND_DATE,Long.class)).longValue() != -1L ){
                retentionVtTz = BSUtils.getSimpleDateFormat((Long)config.getPrefValue(StaticValues.RETENTION_SEND_DATE, Long.class));
            }else{
                retentionVtTz = bsConfig.getInstallDate();
            }
            sessionJson.put(StaticValues.PARAM_VT_TZ, retentionVtTz );
            // #############################################################################
            sessionJson.put(StaticValues.PARAM_IAT_SOURCE,profiler.getSessionStringData(StaticValues.PARAM_IAT_SOURCE));
            sessionJson.put(StaticValues.PARAM_IAT_MEDIUM,profiler.getSessionStringData(StaticValues.PARAM_IAT_MEDIUM));
            sessionJson.put(StaticValues.PARAM_IAT_KWD,profiler.getSessionStringData(StaticValues.PARAM_IAT_KWD));
            sessionJson.put(StaticValues.PARAM_IAT_CAMPAIGN,profiler.getSessionStringData(StaticValues.PARAM_IAT_CAMPAIGN));

            retentionObject.put(TrackType.TYPE_SESSION.toString(),sessionJson);

            RestTask task = null;
            try {
                task = new RestTask(bsConfig.getAliveCheckUri(), JsonStringCompress.compress(retentionObject.toString()), RestTask.RestType.POST,new CallBackInterface() {
                    @Override
                    public void toDoInBackground(JSONObject o) throws JSONException {
                        boolean failCheck = Profiler.getInstance(mContext).updateResponse(o);
                        if(failCheck){
                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    sendRetention();
                                }
                            };
                            Timer timer = new Timer();
                            timer.schedule(timerTask, BSConfig.getInstance(mContext).getRetryTime()*1000);
                            return;
                        }else{
                            config.putPref(StaticValues.RETENTION_SEND_DATE,new Long(finalCurrentTimestamp));
                        }
                    }

                    @Override
                    public void onErrorCodefind(int statusCode, String statusString) {
                        BSDebugger.log(statusString);
                    }
                });
            } catch (IOException e) {
                BSDebugger.log(e,this);
            }
            task.execute();
        } catch (JSONException e) {
            BSDebugger.log(e,this);
        }
    }

    private void installAlarmManager() {
        if(BSConfig.getInstance(this).getUseRetention()) {
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            Intent Intent = new Intent(SignalIndex.ACTIVATE_ALARM);
            PendingIntent pIntent = PendingIntent.getService(this, 0, Intent, 0);
            alarmManager.cancel(pIntent);

            long alarmTime = System.currentTimeMillis() + BSConfig.getInstance(this).getAlarmScheduleTime() * 1000;
            alarmManager.set(AlarmManager.RTC, alarmTime, pIntent);
            if( WiseTracker.FLAG_OF_PRINT_LOG ){
                Log.i("DEBUG_WISETRACKER","AlarmTime : " + BSUtils.getSimpleDateFormat(alarmTime)  );
            }
        }else{
        }
    }

    /** 바인드 시퀀스 시작
     *
     */

    private final IBinder mBinder = new InsightBinder();

    /**
     * 인사이트 서비스 바인더
     * 서비스를 바인드 하면 호출됨
     *
     */
    public class InsightBinder extends Binder {
        InsightService getService() {
            return InsightService.this;
        }
    }

    /**
     * 서비스를 바인드한 경우 리턴해줌
     * intent 클래스의 파라미터를 통해 파라미터를 전달함.
     *
     * @param intent 추가적 파라미터가 바인드를 통해 들어오는 경우
     *               현재까지는 startService 에서 바인딩 처리
     * @return
     */

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * 바인드 시퀀스 종료
     */



}
