package kr.co.wisetracker.insight.lib.values;

/**
 * Created by caspar on 14. 11. 21.
 */
public enum  TrackType {
    TYPE_EVENT(){
        @Override
        public String toString(){
            return "EVENT";
        }
    },TYPE_CAMPAIGN(){
        @Override
        public String toString(){
            return "CAMPAIGN";
        }
    },TYPE_VIEW(){
        @Override
        public String toString(){
            return "VIEW";
        }
    },TYPE_SESSION(){
        @Override
        public String toString(){
            return "SESSION";
        }
    },TYPE_PAGES(){
        @Override
        public String toString(){
            return "PAGES";
        }
    },TYPE_GOAL(){
        @Override
        public String toString(){
            return "GOAL";
        }
    },TYPE_REVENUE(){
        @Override
        public String toString(){
            return "REVENUE";
        }
    }, Type_PUSH(){
        @Override
        public String toString(){
            return "PUSH";
        }
    };

    public String filePrefix(){
        switch (this){
            case TYPE_EVENT :
                return StaticValues.EVENT_FILE_PREFIX;
            case TYPE_CAMPAIGN:
                return StaticValues.CAMPAIGN_FILE_PREFIX;
            case TYPE_GOAL:
                return StaticValues.GOAL_FILE_PREFIX;
            case TYPE_PAGES:
                return StaticValues.PAGES_FILE_PREFIX;
            case TYPE_REVENUE:
                return StaticValues.REVENUE_FILE_PREFIX;
            case TYPE_SESSION:
                return StaticValues.SESSION_FILE_PREFIX;
            case TYPE_VIEW:
                return StaticValues.VIEW_FILE_PREFIX;
            case Type_PUSH:
                return StaticValues.PUSH_FILE_PREFIX;
            default:
                return StaticValues.PAGES_FILE_PREFIX;
        }
    }
}
