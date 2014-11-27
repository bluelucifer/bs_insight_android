package kr.co.bizspring.insight.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.HashMap;
import java.util.Timer;

import kr.co.bizspring.insight.lib.config.BSConfig;
import kr.co.bizspring.insight.lib.tracker.BSSession;
import kr.co.bizspring.insight.lib.tracker.DocumentManager;
import kr.co.bizspring.insight.lib.tracker.Profiler;
import kr.co.bizspring.insight.lib.values.SiginalIndex;
import kr.co.bizspring.insight.lib.values.StaticValues;

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


    /**
     * 프라이빗 메소드
     */
    private void commandProcesser(final Intent intent, final int flags, final int startId){

        Log.i("BS_INSIGHT","commandProcesser");
        if(intent!=null){
            String action = intent.getAction();
            //Activity활동
            if(action.equalsIgnoreCase(SiginalIndex.ON_START_SESSION)){
                //앱이 시작됨. 세션 처리를 하자.
//                documentManager.startSession();
            }else if(action.equalsIgnoreCase(SiginalIndex.ON_END_SESSION)){
                //세션이 종료되면 도큐먼트를 전송한다.
//                documentManager.sendDocumet();
                //타이머를 두고 종료할까?
            }else if(action.equalsIgnoreCase(SiginalIndex.SEND_TRANSACTION)){
                //시그널이 들어오면 서버에 데이터를 즉시 전송한다.
                documentManager.sendDocumet();
            }else if(action.equalsIgnoreCase(SiginalIndex.INIT)){
                //앱이 시작되면 세션을 만들어서 전송한다.
                documentManager.sendDocumet();
            }else if(action.equalsIgnoreCase(StaticValues.INSTALL_REFERRER)){
                //앱 설치 레퍼러 정보가 들어온다.
                Bundle extras = intent.getExtras();
                String referrerString = extras.getString("referrer");
                String[] paramArray = referrerString.split("&");
                HashMap<String,String> map = new HashMap<String, String>();
                for(String pair : paramArray){
                    String[] keyValue = pair.split("=");
                    map.put(keyValue[0],keyValue[1]);
                }
                documentManager.putInstallData(map);
                if(extras.containsKey(StaticValues.PARAM_FB_SOURCE)){
                    String fbSource = extras.getString(StaticValues.PARAM_FB_SOURCE);
                    Profiler.getInstance(this).putFbSource(fbSource);
                }
            }
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
