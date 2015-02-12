package kr.co.bizspring.insight;

import android.content.Context;

import kr.co.bizspring.insight.lib.util.BSMap;

/**
 * Created by caspar on 2014. 11. 12..
 * @author caspar
 * @since 2014. 11. 12.
 * @version 1.0
 * @see kr.co.bizspring.insight.BSTracker
 */
public class Analytics extends BSTracker {

    /*
    * 입출력 명세서의 값으로 사용되는 필드
    * */
    public final static String PRODUCT_SUB_TYPE1 = "TYPE1"; // setProductSubTp(PRODUCT_SUB_TYPE1);
    public final static String PRODUCT_SUB_TYPE2 = "TYPE2";
    public final static String PRODUCT_SUB_TYPE3 = "TYPE3";
    public final static String PRODUCT_SUB_TYPE4 = "TYPE4";
    public final static String PRODUCT_SUB_TYPE5 = "TYPE5";
    public final static String PRODUCT_SUB_TYPE6 = "TYPE6";
    public final static String PRODUCT_SUB_TYPE7 = "TYPE7";
    public final static String PRODUCT_SUB_TYPE8 = "TYPE8";
    public final static String PRODUCT_SUB_TYPE9 = "TYPE9";


    public final static String MEMBER ="Y";
    public final static String NON_MEMBER = "N";


    public final static String GENDER_MALE ="M";
    public final static String GENDER_FEMALE ="F";
    public final static String GENDER_ETC ="U";

    public final static String AGE_0_TO_9	="A";
    public final static String AGE_10_TO_19	="B";
    public final static String AGE_20_TO_29	="C";
    public final static String AGE_30_TO_39	="D";
    public final static String AGE_40_TO_49	="E";
    public final static String AGE_50_TO_59	="F";
    public final static String AGE_60_OVER 	="G";

    private static Analytics instance = null;

    /**
    * 싱글톤 정의
     * 반드시 init 이후 메소드를 콜해야 함.
     * @return Analytics
    */
    public static Analytics getInstance(){
        if(instance==null){
            instance = new Analytics();
        }
        return instance;
    }

    /**
     * 프라이빗 생성자
     *
     */
    private Analytics() {
        super();
    }

    /**
     * 앱 실행 시 메인 콘텍스트를 에플리케이션 콘텍스트로 변환 호출
     * @see kr.co.bizspring.insight.BSTracker
     * @param mContext (Context/Activity/Service) 등등
     */
    public BSTracker init(Context mContext){
        return super.init(mContext);
    }
}
