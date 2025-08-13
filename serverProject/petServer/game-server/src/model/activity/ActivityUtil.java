package model.activity;

import common.tick.GlobalTick;
import lombok.Getter;
import model.ranking.settle.RankingRewards;
import model.ranking.settle.RankingRewardsImpl;
import org.springframework.util.CollectionUtils;
import protocol.Activity.ActivityTypeEnum;
import protocol.Server.ServerActivity;
import util.GameUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ActivityUtil {

    public static class LocalActivityId {
        public static final int PET_EXCHANGE = 1;
        //远古召唤
        public static final int ALIEN_REQUEST = 2;

        //成长基金
        public static final int GrowthFund = 5;

        //限时闯关
        public static final int TIME_LIMIT_MAIN_LINE = 6;
        //限时无尽尖塔
        public static final int TIME_LIMIT_ENDLESS_SPIRE = 7;
        //限时竞技场
        public static final int TIME_LIMIT_ARENA = 8;
        //七日签到
        public static final int SEVEN_DAYS_SIGN_IN = 9;

        //boss战
        public static final int BossBattle = ActivityTypeEnum.ATE_BossBattle_VALUE;

        //许愿池
        public static final int WishingWell = 14;
        //日礼包
        public static final int DailyGift = 16;
        //周礼包
        public static final int WeeklyGift = 17;
        //月礼包
        public static final int MonthlyGift = 18;
        //总在线活动
        public static final int CumuOnline = 20;
        //每日在线活动
        public static final int DailyOnline = 21;
        //累计充值
        public static final int CumuRecharge = 22;
        //新手礼包
        public static final int NewBeeGift = 24;
        //极品兑换
        public static final int BestExchange = 25;

        //广告
        public static final int Ads = 26;

        //零元购
        public static final int ZeroCostPurchase = 31;

        //每日首充
        public static final int DailyFirstRecharge = 35;

        //首充
        public static final int FirstRecharge = 36;

        //七日目标
        public static final int SevenDayTarget = 37;


        private static List<Integer> cycleClearActivityIds;

        public static List<Integer> getCycleClearActivityIds() {
            if (CollectionUtils.isEmpty(cycleClearActivityIds)) {
                setCycleClearActivityIds();
            }
            return cycleClearActivityIds;
        }

        private static void setCycleClearActivityIds() {
            cycleClearActivityIds = Arrays.asList(LocalActivityId.MonthlyGift, LocalActivityId.DailyGift, LocalActivityId.WeeklyGift);
        }

        @Getter
        private static final List<Integer> dailyResetActivityIds = Arrays.asList(DailyGift, DailyOnline, Ads);
    }


    /**
     * 活动是否在展示时间内
     */
    public static boolean activityNeedDis(long startDisTime, long overDisTime) {
        if (startDisTime == -1 && overDisTime == -1) {
            return true;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (startDisTime == -1 && overDisTime > currentTime) {
            return true;
        }

        if (overDisTime == -1 && startDisTime < currentTime) {
            return true;
        }
        return GameUtil.inScope(startDisTime, overDisTime, currentTime);
    }

    public static boolean activityNeedDis(ServerActivity serverActivity) {
        if (serverActivity == null) {
            return false;
        }
        return activityNeedDis(serverActivity.getStartDisTime(), serverActivity.getOverDisTime());
    }

    /**
     * 活动开放中
     */
    public static boolean activityInOpen(ServerActivity serverActivity) {
        if (serverActivity == null) {
            return false;
        }
        return activityInOpen(serverActivity.getBeginTime(), serverActivity.getEndTime());
    }

    public static boolean activityInOpen(long beginTime, long endTime) {
        if (beginTime == -1 && endTime == -1) {
            return true;
        }

        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (beginTime == -1 && endTime > currentTime) {
            return true;
        }

        if (endTime == -1 && currentTime > beginTime) {
            return true;
        }

        return GameUtil.inScope(beginTime, endTime, currentTime);
    }

    /**
     * 用于计算活动是否已经结束
     *
     * @param activity
     * @return
     */
    public static boolean activityIsEnd(ServerActivity activity) {
        if (activity == null) {
            return true;
        }

        if (-1 != activity.getEndTime()
                && GlobalTick.getInstance().getCurrentTime() > activity.getEndTime()) {
            return true;
        }
        return false;
    }


    /**
     * 判断一个活动是否已经展示结束
     *
     * @param activity
     * @return
     */
    public static boolean activityIsOverDisplay(ServerActivity activity) {
        if (activity == null) {
            return true;
        }

        if (-1 != activity.getOverDisTime()
                && GlobalTick.getInstance().getCurrentTime() > activity.getOverDisTime()) {
            return true;
        }
        return false;
    }

    public static List<RankingRewards> getRankingRewards(ServerActivity activity) {
        if (activity == null) {
            return null;
        }
        return activity.getRankingRewardList().stream()
                .map(e -> new RankingRewardsImpl(e.getStartRanking(), e.getEndRanking(), e.getRewardsList()))
                .collect(Collectors.toList());
    }
}
