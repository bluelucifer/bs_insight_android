package kr.co.bizspring.insight.lib.values;

import java.util.HashMap;

import kr.co.bizspring.insight.lib.tracker.Profiler;

/**
 * Created by caspar on 14. 9. 10.
 */
public class StaticValues {

    public static final String BS_CONFIG_TRK_DOC = "/InsightTrk/mobileForAd.json";
    public static final String BS_CONFIG_DEBUG = "debugMsg";
    public static final String BS_CONFIG_CERT_CODE = "appKey";
    public static final String BS_CONFIG_SERVICE_NO = "appServiceNo";
    public static final String BS_CONFIG_SLOT_COUNT = "appSlotNo";
    public static final String BS_CONFIG_RETURN_VISIT_DATE = "returnVisitDate";
    public static final String BS_CONFIG_TARGET_URI = "trkHost";
    public static final String BS_CONFIG_REFERER_EXPIRE_DATE = "referrerExpireDate";
    public static final String BS_CONFIG_TIMER = "timer";
    public static final String BS_CONFIG_USE_RETENTION ="useRetention";

    public static final String PROFILE_DB_NAME = "profileDB";
    public static final String ANALYTICS_DB = "ANALYTICS_DB";

    public static final String PARAM_G = "g";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_VALUE = "value";
    public static final String PARAM_REFERER = "referer";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_RESPONSE_TP = "responseTp";

    public static final String PAGES_FILE_PREFIX = "/PAGES_FILE";
    public static final String REVENUE_FILE_PREFIX = "/REVENUE_FILE";
    public static final String CAMPAIGN_FILE_PREFIX = "/CAMPAIGN_FILE";
    public static final String VIEW_FILE_PREFIX = "/VIEW_FILE";
    public static final String EVENT_FILE_PREFIX = "/EVENT_FILE";
    public static final String GOAL_FILE_PREFIX = "/GOAL_FILE";

    public static final String START_VIEW = "start_view";
    public static final String PAUSE_VIEW = "pause_view";
    public static final String RESUME_VIEW = "resume_view";
    public static final String END_VIEW = "end_view";
    public static final String RECENT_ORDER_PTM = "recentOrderPtm";
    public static final String SESSION_FILE_PREFIX = "/SESSION_FILE";
    public static final String PARAM_PNC_TP = "pncSubTp";
    public static final String PARAM_MTV = "mtv";
    public static final String START_TIME = "st_time";
    public static final String END_TIME = "ed_time";
    public static final String PARAM_EXTRA = "extra";

    /*
    public static final String LAST_DATE_STRING = "lastDateString";
    public static final String LAST_MONTH_STRING = "lastMonthString";
    public static final String LAST_WEEK_INT = "lastWeekInt";
    */
    public static final String EXPIRE_TIME_DAILY = "expireTimeDaily";
    public static final String EXPIRE_TIME_WEEKLY = "expireTimeWeekly";
    public static final String EXPIRE_TIME_MONTHLY = "expireTimeMonthly";

    public static final String PARAM_REF = "Ref";
    public static final String RETRY_COUNT = "retryCount";
    public static final String BS_WEB_TRACKER = "BSTracker";
    public static final String CREATE_TIME = "createTime";
    public static final String DEBUG_FLAG = "debugFlag";
    public static final String BS_CONFIG_DATA_SEND_MODE = "dataSendMode";
    public static final String BS_CONFIG_MAX_DATA_LIFE_TIME = "maxDataLifeTime";
    public static final String BS_CONFIG_MAX_DATA_SEND_LENGTH = "maxDataSendLength";
    public static final String BS_CONFIG_HASH_KEY = "hashKey";
    public static final String ST_SEND_TIME = "ST_SEND_TIME";
    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RES001 = "RES001";
    public static final String RES002 = "RES002";
    public static final String RES003 = "RES003";
    public static final String RES004 = "RES004";
    public static final String LOCK_CODE = "LOCK_CODE";
    public static final String KEY_DOC_ID = "DOCUMENT_ID";

    public static Boolean BS_MODE_DEBUG = false;

    public static String BS_CONFIG_FILE_NAME = "bs_config";

    public static String BS_LOCAL_CONFIG_FILE_NAME = "bs_local_config";

    public static String DEBUG_TAG = "BS_INSIGHT_LOGGER";

    public static String PKG_CLASS_NAME = "kr.co.bizspring.insight.lib.values.StaticValues";

    /* 서비스 엑션별 목록  시작*/
    public static final String INSTALL_REFERRER = "com.android.vending.INSTALL_REFERRER";
    /* 서비스 엑션별 목록  종료*/

    public static String SHARED_PREFRENCE_NAME = "BS_INSIGHT_PREFRENCE";

    /* 파라미터 목록  시작*/
    public static final String PARAM_DEBUG = "debug";
    public static final String PARAM_AK = "ak";
    public static final String PARAM_PDTK = "pdtk";
    public static final String PARAM_PFNO = "pfno";
    public static final String PARAM_UUID = "uuid";
    public static final String PARAM_ADVID = "advtId";
    public static final String PARAM_SID = "sid";
    public static final String PARAM_INSTALL_DATE = "installDate";
    public static final String PARAM_SLOT_NO = "slotNo";
    public static final String PARAM_VISIT_NEW = "visitNew";
    public static final String PARAM_VT_TZ = "vtTz";
    public static final String PARAM_IS_UNI_VT = "isUniVt";
    public static final String PARAM_AP_VR = "apVr";
    public static final String PARAM_OS = "os";
    public static final String PARAM_PHONE = "phone";
    public static final String PARAM_TEL_COM = "telCom";
    public static final String PARAM_WIFI_TP = "wifiTp";
    public static final String PARAM_SR = "sr";
    public static final String PARAM_CNTR = "cntr";
    public static final String PARAM_LNG = "lng";
    public static final String PARAM_INCH = "inch";
    public static final String PARAM_TZ = "tz";
    public static final String PARAM_LTVT = "ltvt";
    public static final String PARAM_LTVI = "ltvi";
    public static final String PARAM_UD_VT = "udVt";
    public static final String PARAM_LTRVNC = "ltrvnc";
    public static final String PARAM_LTRVNI = "ltrvni";
    public static final String PARAM_UD_RVNC = "udRvnc";
    public static final String PARAM_MBR = "mbr";
    public static final String PARAM_GENDER = "sex";
    public static final String PARAM_AGE = "age";
    public static final String PARAM_UVP1 = "uvp1";
    public static final String PARAM_UVP2 = "uvp2";
    public static final String PARAM_UVP3 = "uvp3";
    public static final String PARAM_UVP4 = "uvp4";
    public static final String PARAM_UVP5 = "uvp5";

    public static final String PARAM_MAT_SOURCE = "mat_source";
    public static final String PARAM_MAT_MEDIUM = "mat_medium";
    public static final String PARAM_MAT_KWD = "mat_kwd";
    public static final String PARAM_MAT_CAMPAIGN = "mat_campaign";
    public static final String PARAM_MAT_UPDATE_TIME = "mat_uptime";
    public static final String PARAM_MAT_UPDATE_SID = "mat_upsid";
    public static final String PARAM_CONV_TP = "convTp";
    public static final int MAT_CONVERSION_TP_DIRECT = 0;
    public static final int MAT_CONVERSION_TP_NON_DIRECT = 1;


    public static final String PARAM_IAT_SOURCE = "iat_source";
    public static final String PARAM_IAT_MEDIUM = "iat_medium";
    public static final String PARAM_IAT_KWD = "iat_kwd";
    public static final String PARAM_IAT_CAMPAIGN = "iat_campaign";




    public static final String PARAM_FB_SOURCE = "fb_source";
    public static final String PARAM_FB_UPDATE_TIME = "fb_uptime";
    public static final String PARAM_FB_UPDATE_SID = "fb_upsid";


    public static final String PARAM_UTM_SOURCE = "utm_source";
    public static final String PARAM_UTM_MEDIUM = "utm_medium";
    public static final String PARAM_UTM_CAMPAIGN = "utm_campaign";
    public static final String PARAM_UTM_TERM = "utm_term";
    public static final String PARAM_UTM_CONTENT = "utm_content";
    public static final String PARAM_UTM_UPDATE_TIME = "utm_uptime";
    public static final String PARAM_UTM_UPDATE_SID = "utm_upsid";

    public static final String PARAM_GCLID = "gclid";



    public static final String PARAM_AMT = "amt";
    public static final String PARAM_EA = "ea";
    public static final String PARAM_PNC = "pnc";
    public static final String PARAM_PNG = "png";
    public static final String PARAM_VS = "vs";
    public static final String PARAM_CS_P_V = "csPV";
    public static final String PARAM_CP = "cp";
    public static final String PARAM_PI = "pi";
    public static final String PARAM_PNC_NM = "pncNm";
    public static final String PARAM_PNG_NM = "pngNm";
    public static final String PARAM_RECENT_VISIT_PTM = "recentVisitPtm";
    /* 파라미터 목록  종료*/

    public static String PREF_INSTALL_DATE = "installDate";

    /*
    * 2014.11.29 추가 파라미터
    * **/
    public static final String PARAM_MVT1 = "mvt1";
    public static final String PARAM_MVT2 = "mvt2";
    public static final String PARAM_MVT3 = "mvt3";
    public static final String PARAM_GOAL_ACCEPT_PUSH ="g30";


    /*
    * 2015.02.16 추가 파라미터
    * **/
    public static final String PARAM_IKWD = "ikwd";
    public static final String PARAM_IKWD_RS = "ikwdRs";
    public static final String PARAM_IKWD_GRP = "ikwdGrp";

    public static final String PARAM_PUSH_MESSAGE_KEY = "ocmp";
    public static final String PARAM_PUSH_MESSAGE_UPDATE_TIME = "ocmp_uptime";
    public static final String PARAM_PUSH_MESSAGE_UPDATE_SID = "ocmp_upsid";

  }
