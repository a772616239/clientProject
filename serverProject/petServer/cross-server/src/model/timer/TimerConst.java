package model.timer;

/**
 * @author huhan
 * @date 2020/1/10
 */
public class TimerConst {
    public static class TimerIdx {
        public static final String TI_RESET_DAILY_DATE = "1";
        public static final String TI_RESET_WEEK_DATE = "2";
        public static final String TI_SETTLE_ARENA_DAN = "3";     //竞技场段位结算
    }

    public static class TimerExpireType {
        public static final int ET_EXPIRE_BY_TIME = 1;
        public static final int ET_EXPIRE_BY_TRIGGER_TIMES = 2;
    }

    public static class TimerTargetType {
        /**重置每日数据**/
        public static final int TT_RESET_DAILY_DATA = 1;
        /**重置每周数据**/
        public static final int TT_RESET_WEEK_DATA = 2;
        /**GamePlayer存储**/
        public static final int TT_UPDATE_GAME_PLAY = 3;
        public static final int TT_SETTLE_ARENA_DAN = 4;

        /**段位结算通知*/
        public static final int TT_ARENA_DAN_SETTLE_ADVICE = 5;
    }
}
