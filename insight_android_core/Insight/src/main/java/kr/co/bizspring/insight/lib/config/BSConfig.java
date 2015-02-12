package kr.co.bizspring.insight.lib.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.Object;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import kr.co.bizspring.insight.lib.util.Connectivity;
import kr.co.bizspring.insight.lib.util.BSDebugger;
import kr.co.bizspring.insight.lib.values.StaticValues;

/**
 * Created by caspar on 14. 9. 10.
 */
public class BSConfig {
    private static BSConfig instance = null;
    private boolean userRetention;
    private int timer;

    private int refererExpireDate;
    private Integer returnVisitDate = 0;

    private Context mContext = null;

    private boolean debug = false;
    private String ak = "";
    private int pfno = -1;

    private String uuid = "";
    private String advId = "";

    private SharedPreferences pref = null;
    private String installDate;

    private String apVr;
    private String os;
    private String phone;
    private String telCom;

    private int slotCount;

    private int dataSendMode;

    private String targetUri;
    private int maxDataLifeTime;

    private int maxDataSendLength;


    private String hashKey;



    public String getApVr() {
        String version = "1.0";
        try {
            PackageInfo i = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            version = i.versionName;

        } catch(PackageManager.NameNotFoundException e) {
            BSDebugger.log(e, this);
        }
        apVr = version;
        return apVr;
    }

    public String getOs() {
        // api level 코드를 전송해야 함.
        // os = Build.VERSION.RELEASE;
        os = String.valueOf(Build.VERSION.SDK_INT);
        return os;
    }

    public String getPhone() {
        // 모델명만 전송함.
        // phone = Build.MODEL+"/"+Build.PRODUCT;
        phone = Build.MODEL;
        return phone;
    }

    public String getTelCom() {
        telCom = tm.getNetworkOperatorName();
        return telCom;
    }

    public String getWifiTp() {
        wifiTp = Connectivity.getNetworkType(mContext);
        return wifiTp;
    }

    public String getSr() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int deviceDensityDIP = displayMetrics.densityDpi;
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        sr = String.valueOf(width)+"*"+String.valueOf(height)+"/"+String.valueOf(deviceDensityDIP);
        return sr;
    }

    public String getCntr() {
        //cntr = tm.getSimCountryIso();
        cntr = locale.getCountry();
        return cntr;
    }

    public String getLng() {
        lng = locale.getLanguage();
        return lng;
    }

    public double getInch() {
        DisplayMetrics matrix = new DisplayMetrics();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(matrix);

        int w = matrix.widthPixels;
        int h = matrix.heightPixels;

        float density = matrix.density;
        float xdpi = matrix.xdpi;
        float ydpi = matrix.ydpi;

        float x_inch = w/xdpi;
        float y_inch = h/ydpi;
        double display = Math.sqrt(x_inch*x_inch+y_inch*y_inch);
        inch = Math.round(display*10)/10;
        return inch;
    }

    public String getTz() {
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        tz = String.valueOf(TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS));
        return tz;
    }

    private String wifiTp;
    private String sr;
    private String cntr;
    private String lng;
    private double inch;
    private String tz;


    public static BSConfig getInstance(Context _Context){
        if(instance == null){
            instance = new BSConfig(_Context);
        }
        return instance;
    }
    TelephonyManager tm;
    ConnectivityManager cm;
    Locale locale;

    BSLocalConfig localConfig;

    private BSConfig(Context _Context){
        localConfig = BSLocalConfig.getInstance(_Context);
        mContext = _Context;
        tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        locale = mContext.getResources().getConfiguration().locale;

        pref = mContext.getSharedPreferences(StaticValues.SHARED_PREFRENCE_NAME,Context.MODE_PRIVATE);
        parseXml();
        try {

            advId = (String) getPrefValue(StaticValues.PARAM_ADVID,String.class);
            if(advId.equalsIgnoreCase("_")){
                AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        requestAdId();
                        return null;
                    }
                };
                task.execute();
            }
            uuid = (String) getPrefValue(StaticValues.PARAM_UUID,String.class);
            if(uuid.equalsIgnoreCase("_")) {
                requestUUID();
            }
            installDate = pref.getString(StaticValues.PREF_INSTALL_DATE,"_");
            if(installDate.equalsIgnoreCase("_")){
                String currentDateString = getCurrentDateString();
                installDate = currentDateString;
                putPref(StaticValues.PREF_INSTALL_DATE,installDate);
            }
        } catch (Exception e) {
            BSDebugger.log(e,this);
        }
    }

    private void parseXml() {
        ApplicationInfo applicationInfo = mContext.getApplicationInfo();
        String packageName = applicationInfo.packageName;
        int xmlId = 0;
        xmlId = mContext.getResources().getIdentifier(StaticValues.BS_CONFIG_FILE_NAME,"xml",packageName);
        if(xmlId==0){
            xmlId = mContext.getResources().getIdentifier(StaticValues.BS_CONFIG_FILE_NAME,"xml","kr.co.bizspring.insight");
        }
        XmlResourceParser localConfigXml = mContext.getResources().getXml(xmlId);
        try {
            localConfigXml.next();
            int eventType = localConfigXml.getEventType();
            String NodeValue;
            while (eventType != XmlPullParser.END_DOCUMENT)  //Keep going until end of xml document
            {
                if(eventType == XmlPullParser.START_DOCUMENT)
                {
                    //Start of XML, can check this with myxml.getName() in Log, see if your xml has read successfully
                }
                else if(eventType == XmlPullParser.START_TAG)
                {
                    NodeValue = localConfigXml.getName();//Start of a Node
                    if (NodeValue.equalsIgnoreCase("string"))
                    {
                        int attributeCount = localConfigXml.getAttributeCount();
                        for(int i = 0; i < attributeCount ; i++){
                            String attributeName = localConfigXml.getAttributeName(i);
                            if(attributeName.equalsIgnoreCase("name")){
                                String attributeValue = localConfigXml.getAttributeValue(i);
                                if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_TARGET_URI)) {
                                    String nextText = localConfigXml.nextText();
                                    targetUri = String.valueOf(nextText);
                                }else if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_CERT_CODE)){
                                    String nextText = localConfigXml.nextText();
                                    ak = String.valueOf(nextText);
                                }else if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_HASH_KEY)){
                                    String nextText = localConfigXml.nextText();
                                    hashKey = String.valueOf(nextText);
                                }
                            }
                        }
                        // use myxml.getAttributeValue(x); where x is the number
                        // of the attribute whose data you want to use for this node

                    }else if (NodeValue.equalsIgnoreCase("integer"))
                    {
                        int attributeCount = localConfigXml.getAttributeCount();
                        for(int i = 0; i < attributeCount ; i++){
                            String attributeName = localConfigXml.getAttributeName(i);
                            if(attributeName.equalsIgnoreCase("name")){
                                String attributeValue = localConfigXml.getAttributeValue(i);
                                if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_SERVICE_NO)){
                                    String nextText = localConfigXml.nextText();
                                    pfno = Integer.parseInt(nextText);
                                }else if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_SLOT_COUNT)){
                                    String nextText = localConfigXml.nextText();
                                    slotCount = Integer.parseInt(nextText);
                                }else if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_RETURN_VISIT_DATE)){
                                    String nextText = localConfigXml.nextText();
                                    returnVisitDate = Integer.parseInt(nextText);
                                }else if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_REFERER_EXPIRE_DATE)){
                                    String nextText = localConfigXml.nextText();
                                    refererExpireDate = Integer.parseInt(nextText);
                                }else if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_TIMER)){
                                    String nextText = localConfigXml.nextText();
                                    timer = Integer.parseInt(nextText);
                                }else if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_DATA_SEND_MODE)){
                                    String nextText = localConfigXml.nextText();
                                    dataSendMode = Integer.parseInt(nextText);
                                }else if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_MAX_DATA_LIFE_TIME)){
                                    String nextText = localConfigXml.nextText();
                                    maxDataLifeTime = Integer.parseInt(nextText);
                                }else if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_MAX_DATA_SEND_LENGTH)){
                                    String nextText = localConfigXml.nextText();
                                    maxDataSendLength = Integer.parseInt(nextText);
                                }
                            }
                        }
                        // use myxml.getAttributeValue(x); where x is the number
                        // of the attribute whose data you want to use for this node

                    }else if (NodeValue.equalsIgnoreCase("bool"))
                    {
                        int attributeCount = localConfigXml.getAttributeCount();
                        for(int i = 0; i < attributeCount ; i++){
                            String attributeName = localConfigXml.getAttributeName(i);
                            if(attributeName.equalsIgnoreCase("name")){
                                String attributeValue = localConfigXml.getAttributeValue(i);
                                if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_USE_RETENTION)) {
                                    String nextText = localConfigXml.nextText();
                                    userRetention = Boolean.parseBoolean(nextText);
                                }else if(attributeValue.equalsIgnoreCase(StaticValues.BS_CONFIG_DEBUG)) {
                                    String nextText = localConfigXml.nextText();
                                    debug = Boolean.parseBoolean(nextText);
                                }else{

                                }
                            }
                        }
                    }else{

                    }

                    if (NodeValue.equalsIgnoreCase("SecondNodeNameType"))
                    {
                        // use myxml.getAttributeValue(x); where x is the number
                        // of the attribute whose data you want to use for this node

                    }
                    //etc for each node name
                }
                else if(eventType == XmlPullParser.END_TAG)
                {
                    //End of document
                }
                else if(eventType == XmlPullParser.TEXT)
                {
                    //Any XML text
                }

                eventType = localConfigXml.next(); //Get next event from xml parser
            }

        } catch (XmlPullParserException e) {
            BSDebugger.log(e, this);
        } catch (IOException e) {
            BSDebugger.log(e,this);
        }
    }

    void requestUUID(){
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        uuid = deviceId;
        putPref(StaticValues.PARAM_UUID,uuid);
    }
    void requestAdId(){
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
            if(adInfo.isLimitAdTrackingEnabled()){
                //광고 추적 거부 시 임시 발금
                final String tmDevice, tmSerial, androidId;
                tmDevice = "" + tm.getDeviceId();
                tmSerial = "" + tm.getSimSerialNumber();
                androidId = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

                UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
                String deviceId = deviceUuid.toString();
                advId = deviceId;
            }else{
                advId = adInfo.getId();
            }
            putPref(StaticValues.PARAM_ADVID,advId);
        } catch (IOException e) {
            BSDebugger.log(e,this);
        } catch (GooglePlayServicesNotAvailableException e) {
            BSDebugger.log(e,this);
        } catch (GooglePlayServicesRepairableException e) {
            BSDebugger.log(e,this);
        }
    }

    public static String getCurrentDateString() {
        // 현재 시간을 msec으로 구한다.
        long now = System.currentTimeMillis();
// 현재 시간을 저장 한다.
        Date date = new Date(now);
// 시간 포맷으로 만든다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strNow = sdfNow.format(date);
        return strNow;
    }

    public boolean getDebug(){
        return debug;
    }
    public String getAk(){
        return ak;
    }
    public int getPfno(){
        return pfno;
    }
    public String getUuid(){
        return uuid;
    }
    public String getAdvId(){
        return advId;
    }

    public void putPref(String key,Object value){
        SharedPreferences.Editor editor = pref.edit();
        if(value instanceof String){
            editor.putString(key,(String)value);
        }else if(value instanceof Boolean){
            editor.putBoolean(key,(Boolean)value);
        }else if(value instanceof Float){
            editor.putFloat(key,(Float)value);
        }else if(value instanceof Integer){
            editor.putInt(key,(Integer)value);
        }else if(value instanceof Long){
            editor.putLong(key,(Long)value);
        }else if(value instanceof Set){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
                editor.putStringSet(key, (Set<String>) value);
            }
        }
        editor.commit();
    }

    public Object getPrefValue(String key,Class classObject){
        Object retValue = null;
        if(classObject.isInstance(new String())){
            retValue = pref.getString(key,"_");
        }else if(classObject.isInstance(new Integer(-1))){
            retValue = pref.getInt(key,-1);
        }else if(classObject.isInstance(new Boolean(false))){
            retValue = pref.getBoolean(key, false);
        }else if(classObject.isInstance(new Float(-1))){
            retValue = pref.getFloat(key,-1);
        }else if(classObject.isInstance(new Long(-1))){
            retValue = pref.getLong(key, -1);
        }else if(classObject.isInstance(new Set<String>() {
            @Override
            public boolean add(String s) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends String> strings) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> objects) {
                return false;
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @NonNull
            @Override
            public Iterator<String> iterator() {
                return null;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> objects) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> objects) {
                return false;
            }

            @Override
            public int size() {
                return 0;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(T[] ts) {
                return null;
            }
        })){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
                retValue = pref.getStringSet(key, new Set<String>() {
                    @Override
                    public boolean add(String s) {
                        return false;
                    }

                    @Override
                    public boolean addAll(Collection<? extends String> strings) {
                        return false;
                    }

                    @Override
                    public void clear() {

                    }

                    @Override
                    public boolean contains(Object o) {
                        return false;
                    }

                    @Override
                    public boolean containsAll(Collection<?> objects) {
                        return false;
                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @NonNull
                    @Override
                    public Iterator<String> iterator() {
                        return null;
                    }

                    @Override
                    public boolean remove(Object o) {
                        return false;
                    }

                    @Override
                    public boolean removeAll(Collection<?> objects) {
                        return false;
                    }

                    @Override
                    public boolean retainAll(Collection<?> objects) {
                        return false;
                    }

                    @Override
                    public int size() {
                        return 0;
                    }

                    @NonNull
                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }

                    @NonNull
                    @Override
                    public <T> T[] toArray(T[] ts) {
                        return null;
                    }
                });
            }else{
                return new Object();
            }
        }else{
            retValue = new Object();
        }
        return retValue;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public String getInstallDate() {
        return (String)getPrefValue(StaticValues.PREF_INSTALL_DATE,String.class);
    }

    public int getReturnVisitDate() {
        return returnVisitDate;
    }

    public String getTargetUri() {
        return (targetUri+"/InsightTrk/mobileForAd.json");
    }
    public String getAliveCheckUri(){ return (targetUri+"/InsightTrk/mobileForHc.json"); }
    public int getReportTime() {
//        int time = BSLocalConfig.getInstance(mContext).getReportTime();
        return timer*60;
    }
    public int getSessionTime(){
        int time = BSLocalConfig.getInstance(mContext).getSessionTime();
        return time;
    }

    public int getDataSendMode() {
        return this.dataSendMode;
    }

    public boolean getUseRetention() {
        return userRetention;
    }

    public int getMaxDataLifeTime(){
        return this.maxDataLifeTime;
    }
    public int getMaxDataSendLength() {
        return maxDataSendLength;
    }
    public String getHashKey() {
        return hashKey;
    }

}
