package model.activityboss;

import cfg.ActivityBossConfig;
import cfg.ActivityBossConfigObject;
import cfg.ActivityBossRankingReward;
import cfg.ActivityBossRankingRewardObject;
import common.GameConst;
import common.tick.GlobalTick;
import common.tick.Tickable;
import model.activity.ActivityUtil.LocalActivityId;
import model.activityboss.entity.ActivityBossEnterResult;
import model.player.util.PlayerUtil;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.RankingReward;
import protocol.Common.Reward;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server;
import protocol.Server.ServerActivity;
import util.LogUtil;
import util.TimeUtil;

import java.util.List;

public class ActivityBossManager implements Tickable {
    private static final ActivityBossManager activityBossManager = new ActivityBossManager();

    public static ActivityBossManager getInstance() {
        return activityBossManager;
    }


    private long thisCycleBeginTime;

    private long thisCycleEndTime;

    private long thisCycleBeginDisplay;
    private long thisCycleEndDisplay;

    private long nextTickTime;


    @Override
    public void onTick() {
        if (nextTickTime > GlobalTick.getInstance().getCurrentTime()) {
            return;
        }
        ActivityBossConfigObject config = ActivityBossConfig.getById(GameConst.CONFIG_ID);
        if (config == null) {
            LogUtil.error("ActivityBossConfigObject is null by configId:{}", GameConst.CONFIG_ID);
            nextTickTime = Long.MAX_VALUE;
            return;
        }
        long beginTime = TimeUtil.parseActivityTime(config.getBegibtime());
        long endTime = TimeUtil.parseActivityTime(config.getEndtime());
        int openDays = config.getOpendays();
        if (openDays <= 0) {
            LogUtil.error("ActivityBossConfig openDays less than zero");
            nextTickTime = Long.MAX_VALUE;
            return;
        }
        int openCycle = config.getOpencecyle();
        if (openCycle <= 0) {
            LogUtil.error("ActivityBossConfig openCycle less than zero");
            nextTickTime = Long.MAX_VALUE;
            return;
        }
        long now = GlobalTick.getInstance().getCurrentTime();
        if (now > beginTime) {
            thisCycleBeginTime = now - (now - beginTime) % (openCycle * TimeUtil.MS_IN_A_DAY);
        } else {
            thisCycleBeginTime = beginTime;
        }

        thisCycleEndTime = Math.min(endTime, thisCycleBeginTime + openDays * TimeUtil.MS_IN_A_DAY);

        thisCycleBeginDisplay = thisCycleBeginTime - config.getDisplayaheadtime() * TimeUtil.MS_IN_A_MIN;

        thisCycleEndDisplay = thisCycleEndTime + config.getDisplaylagtime() * TimeUtil.MS_IN_A_MIN;

        nextTickTime = thisCycleBeginTime + openCycle * TimeUtil.MS_IN_A_DAY - config.getDisplayaheadtime() * TimeUtil.MS_IN_A_MIN;
    }

    public boolean isOpened() {
        long now = GlobalTick.getInstance().getCurrentTime();
        return now > thisCycleBeginTime && now <= thisCycleEndTime;
    }

    public boolean isDisplayed() {
        long now = GlobalTick.getInstance().getCurrentTime();
        return now > thisCycleBeginDisplay && now <= thisCycleEndDisplay;
    }


    public ActivityBossEnterResult getFightMakeId(String playerId) {
        ActivityBossEnterResult result = new ActivityBossEnterResult();
        if (!isOpened()) {
            result.setCode(RetCodeEnum.RCE_ActivityBoss_Closed);
            return result;
        }
        int unLockLevel = ActivityBossConfig.getById(GameConst.CONFIG_ID).getUnlocklevel();
        if (PlayerUtil.queryPlayerLv(playerId) < unLockLevel) {
            result.setCode(RetCodeEnum.RCE_LvNotEnough);
            return result;
        }
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (entity == null) {
            result.setCode(RetCodeEnum.RCE_UnknownError);
            return result;
        }
        if (entity.getDb_Builder().getSpecialInfo().getActivityBoss().getTimes() >= ActivityBossConfig.getById(1).getTimes()) {
            result.setCode(RetCodeEnum.RCE_ActivityBoss_UseUpTime);
            return result;
        }
        result.setEnemyBuffId(ActivityBossConfig.getById(1).getBuffid());
        result.setFightMakeId(ActivityBossConfig.getById(1).getFightmakeid());
        result.setSuccess(true);
        return result;
    }

    public int getEachDayTimes() {
        if (isDisplayed()) {
            return ActivityBossConfig.getById(1).getTimes();
        }
        return 0;
    }

    public ServerActivity buildBossActivity() {
        if (!isDisplayed()) {
            return null;
        }
        Server.ServerActivity.Builder builder = Server.ServerActivity.newBuilder();
        ActivityBossConfigObject bossConfig = ActivityBossConfig.getById(GameConst.CONFIG_ID);
        if (bossConfig == null) {
            LogUtil.warn("ActivityBossConfig is not config");
            return null;
        }
        builder.setType(ActivityTypeEnum.ATE_BossBattle);
        builder.setActivityId(LocalActivityId.BossBattle);
        builder.setStartDisTime(thisCycleBeginDisplay);
        builder.setOverDisTime(thisCycleEndDisplay);

        builder.setBeginTime(thisCycleBeginTime);
        builder.setEndTime(thisCycleEndTime);
        initRankReward(builder);

        return builder.build();
    }

    private void initRankReward(Server.ServerActivity.Builder builder) {
        for (ActivityBossRankingRewardObject config : ActivityBossRankingReward._ix_id.values()) {
            RankingReward.Builder rankReward = RankingReward.newBuilder();
            rankReward.setStartRanking(config.getStartranking());
            rankReward.setEndRanking(config.getEndranking());
            List<Reward> rewards = RewardUtil.getRewardsByRewardId(config.getDailyrewards());
            if (!CollectionUtils.isEmpty(rewards)) {
                rankReward.addAllRewards(rewards);
            }
            builder.addRankingReward(rankReward.build());
        }
    }

}
