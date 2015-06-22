package kr.co.wisetracker.insight.lib.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import kr.co.wisetracker.insight.lib.util.BSUtils;
import kr.co.wisetracker.insight.lib.util.Connectivity;
import kr.co.wisetracker.insight.lib.util.BSDebugger;
import kr.co.wisetracker.insight.lib.values.StaticValues;

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

    private int slotCount = 4;

    private int dataSendMode;

    private String targetUri;
    private int maxDataLifeTime;

    private int maxDataSendLength;


    private String hashKey;

    private String venId;



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
        if(tm != null){
            telCom = tm.getNetworkOperatorName();
        }else{
            telCom = "noTelcomDev";
        }
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
        sr = String.valueOf(width)+"*"+String.valueOf(height);
        if( !(String.valueOf(deviceDensityDIP)).equals("")){
            sr += ("/"+String.valueOf(deviceDensityDIP));
        }
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

//    BSLocalConfig localConfig;

    private BSConfig(Context _Context){
//        localConfig = BSLocalConfig.getInstance(_Context);
        mContext = _Context;
        File file = new File(mContext.getFilesDir().getPath()+"/wisetracker/");
        file.mkdirs();
        if(Context.TELEPHONY_SERVICE != null){
            tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        }else{
            tm = null;
        }
        if(Context.CONNECTIVITY_SERVICE != null){
            cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }else{
            cm = null;
        }
        locale = mContext.getResources().getConfiguration().locale;

        pref = mContext.getSharedPreferences(StaticValues.SHARED_PREFRENCE_NAME,Context.MODE_PRIVATE);
        parseXml();
        try {

            advId = (String) getPrefValue(StaticValues.PARAM_ADVID,String.class);
            venId = (String) getPrefValue(StaticValues.PARAM_VENID,String.class);

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

        String metaDataString = getAppKeyMeta();
        String decodeMeta = BSUtils.decompress(metaDataString);
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            InputStream is;
            try {

                is = new ByteArrayInputStream(decodeMeta.getBytes("UTF-8"));
                Document dom = builder.parse(is);
                String name;
                String value;
                NodeList stringList = dom.getElementsByTagName(StaticValues.TAG_STRING);
                for(int i = 0 ; i < stringList.getLength();i++){
                    Element element = (Element) stringList.item(i);
                    name = element.getAttribute("name");
                    value = element.getTextContent();
                    if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_TARGET_URI)){
                        targetUri = String.valueOf(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_CERT_CODE)){
                        ak = String.valueOf(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_HASH_KEY)){
                        hashKey = String.valueOf(value);
                    }
                }
                NodeList integerList = dom.getElementsByTagName(StaticValues.TAG_INTEGER);
                for(int i = 0 ; i < integerList.getLength();i++){
                    Element element = (Element) integerList.item(i);
                    name = element.getAttribute("name");
                    value = element.getTextContent();
                    if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_TARGET_URI)){
                        targetUri = String.valueOf(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_CERT_CODE)){
                        ak = String.valueOf(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_HASH_KEY)){
                        hashKey = String.valueOf(value);
                    }
                    if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_SERVICE_NO)){
                        pfno = Integer.parseInt(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_SLOT_COUNT)){
                        slotCount = Integer.parseInt(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_RETURN_VISIT_DATE)){
                        returnVisitDate = Integer.parseInt(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_REFERER_EXPIRE_DATE)){
                        refererExpireDate = Integer.parseInt(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_TIMER)){
                        timer = Integer.parseInt(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_DATA_SEND_MODE)){
                        dataSendMode = Integer.parseInt(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_MAX_DATA_LIFE_TIME)){
                        maxDataLifeTime = Integer.parseInt(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_MAX_DATA_SEND_LENGTH)){
                        maxDataSendLength = Integer.parseInt(value);
                    }
                }
                NodeList boolList = dom.getElementsByTagName(StaticValues.TAG_BOOL);
                for(int i = 0 ; i < boolList.getLength();i++){
                    Element element = (Element) boolList.item(i);
                    name = element.getAttribute("name");
                    value = element.getTextContent();
                    if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_USE_RETENTION)) {
                        userRetention = Boolean.parseBoolean(value);
                    }else if(name.equalsIgnoreCase(StaticValues.BS_CONFIG_DEBUG)) {
                        debug = Boolean.parseBoolean(value);
                    }else{

                    }
                }

            } catch (UnsupportedEncodingException e) {
                BSDebugger.log(e);
                return;
            } catch (SAXException e) {
                BSDebugger.log(e);
                return;
            } catch (IOException e) {
                BSDebugger.log(e);
                return;
            }
        } catch (ParserConfigurationException e) {
            BSDebugger.log(e);
            return;
        }
    }

    private String getAppKeyMeta() {
        ApplicationInfo ai = null;
        try {
            ai = mContext.getApplicationContext().getPackageManager().getApplicationInfo(mContext.getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String metaString = bundle.getString(StaticValues.META_WISETRACKER_KEY);
            return metaString;
        } catch (PackageManager.NameNotFoundException e) {
            BSDebugger.log(e);
            return "";
        }
    }

    void requestUUID(){
        final String tmDevice, tmSerial, androidId;
        if(tm != null){
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
        }else{
            tmDevice = "";
            tmSerial = "";
        }

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
                if(tm!=null){
                    tmDevice = "" + tm.getDeviceId();
                    tmSerial = "" + tm.getSimSerialNumber();
                }else{
                    tmDevice = "";
                    tmSerial = "";
                }
                androidId = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

                UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
                String deviceId = deviceUuid.toString();
                advId = deviceId;
            }else{
                advId = adInfo.getId();
            }
            putPref(StaticValues.PARAM_ADVID,advId);
            putPref(StaticValues.PARAM_VENID,advId+mContext.getApplicationContext().getPackageName());
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
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.KOREA);
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
    public String getVenId(){
        return venId;
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
        int time = 1800;//BSLocalConfig.getInstance(mContext).getSessionTime();
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

    public int getRetryTime() {
        return 5*60;
    }
    public long getAlarmScheduleTime() {
        return 60*60*24;
    }

}
