package kr.co.bizspring.insight;

import android.content.Context;

/**
 * Created by caspar on 2014. 11. 12..
 * @author caspar
 * @since 2014. 11. 12.
 * @version 1.0
 * @see kr.co.bizspring.insight.BSTracker
 */
public class Analytics extends BSTracker {

    public static int GOAL_1 = 1;
    public static int GOAL_2 = 2;
    public static int GOAL_3 = 3;
    public static int GOAL_4 = 4;
    public static int GOAL_5 = 5;
    public static int GOAL_6 = 6;
    public static int GOAL_7 = 7;
    public static int GOAL_8 = 8;
    public static int GOAL_9 = 9;
    public static int GOAL_10 = 10;

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
    public void init(Context mContext){
        super.init(mContext);
    }
}
