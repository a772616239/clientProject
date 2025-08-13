package model.gloryroad;

import cfg.FunctionOpenLvConfig;
import cfg.GloryRoadConfig;
import cfg.PlayerLevelConfig;
import cfg.PlayerLevelConfigObject;
import common.GameConst;
import common.GlobalData;
import common.tick.GlobalTick;
import io.netty.util.internal.ConcurrentSet;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.EnumFunction;
import protocol.Common.RandomReward;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.GameplayDB.DB_RedBag;
import protocol.GloryRoad.EnumGloryRoadSchedule;
import protocol.GloryRoad.SC_RedBagMessage;
import protocol.GloryRoad.SC_RedBagOpen;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;
import util.TimeUtil;

/**
 * @author huhan
 * @date 2021/3/16
 */
@Getter
@Setter
public class RedBag {

    private long startTime;
    private long endTime;

    private final Set<String> claimedPlayer = new ConcurrentSet<>();

    /**
     * 晋级信息
     */
    private List<String> promotionPlayerList;
    private EnumGloryRoadSchedule schedule;

    private RedBag() {
    }

    public RetCodeEnum canClaimRewards(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        if (this.claimedPlayer.contains(playerIdx)) {
            return RetCodeEnum.RCE_GloryRoad_RedBag_AlreadyClaimed;
        }

        if (GameUtil.outOfScope(this.startTime, this.endTime, GlobalTick.getInstance().getCurrentTime())) {
            return RetCodeEnum.RCE_GloryRoad_RedBag_OutOfTime;
        }
        return RetCodeEnum.RCE_Success;
    }

    public synchronized RetCodeEnum claimRewards(String playerIdx, int redBagCount) {
        RetCodeEnum canClaimRewards = canClaimRewards(playerIdx);
        if (canClaimRewards != RetCodeEnum.RCE_Success) {
            return canClaimRewards;
        }

        int playerLv = PlayerUtil.queryPlayerLv(playerIdx);
        PlayerLevelConfigObject levelConfig = PlayerLevelConfig.getByLevel(playerLv);
        if (levelConfig == null) {
            LogUtil.error("RedBag.claimRewards, level config is null,playerIdx:" + playerIdx + ", playerLv:" + playerLv);
            return RetCodeEnum.RCE_UnknownError;
        }

        List<RandomReward> randomRewards = RewardUtil.parseIntArrayToRandomRewardList(levelConfig.getRedbagrandomrewards());
        if (CollectionUtils.isEmpty(randomRewards)) {
            LogUtil.error("RedBag.claimRewards, level red bag random rewards is null, level:", playerLv);
            return RetCodeEnum.RCE_UnknownError;
        }

//        List<Reward> totalRewards = new ArrayList<>();

        int canGetRewardsCount = Math.min(GloryRoadConfig.getById(GameConst.CONFIG_ID).getRedbagmaxcount(), redBagCount);

//        int randomTimes = canGetRewardsCount / BASE_DIVISOR;
//        List<Reward> rewardList = RewardUtil.multiReward(RewardUtil.drawMustRandomReward(randomRewards, randomTimes), BASE_DIVISOR);
        List<Reward> rewardList = RewardUtil.drawMustRandomReward(randomRewards, canGetRewardsCount);
//        if (CollectionUtils.isNotEmpty(rewardList)) {
//            totalRewards.addAll(rewardList);
//        }

//        int remainTimes = canGetRewardsCount - randomTimes * BASE_DIVISOR;
//        Reward reward = RewardUtil.multiReward(RewardUtil.drawMustRandomReward(randomRewards), remainTimes);
//        if (reward != null) {
//            totalRewards.add(reward);
//        }

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RedBag);
        if (!RewardManager.getInstance().doRewardByList(playerIdx, rewardList, reason, true)) {
            return RetCodeEnum.RCE_UnknownError;
        }
        return this.claimedPlayer.add(playerIdx) ? RetCodeEnum.RCE_Success : RetCodeEnum.RCE_UnknownError;
    }

    private void sendOpenMsg() {
        Set<String> allOnlinePlayerIdx = GlobalData.getInstance().getAllOnlinePlayerIdx();
        if (CollectionUtils.isEmpty(allOnlinePlayerIdx)) {
            return;
        }

        SC_RedBagOpen.Builder openBuilder = SC_RedBagOpen.newBuilder().setEndTime(this.endTime);

        SC_RedBagMessage.Builder promotionMsg = SC_RedBagMessage.newBuilder().setType(1);
        if (this.schedule != null) {
            promotionMsg.setSchedule(this.schedule);
        }

        SC_RedBagMessage.Builder commonMsg = SC_RedBagMessage.newBuilder().setType(2);
        if (this.promotionPlayerList != null) {
            this.promotionPlayerList.forEach(e -> commonMsg.addPlayerName(PlayerUtil.queryPlayerName(e)));
        }

        for (String onlinePlayerIdx : allOnlinePlayerIdx) {
            if (PlayerUtil.queryFunctionLock(onlinePlayerIdx,EnumFunction.EF_GloryRoad)) {
                continue;
            }

            GlobalData.getInstance().sendMsg(onlinePlayerIdx, MsgIdEnum.SC_RedBagOpen_VALUE, openBuilder);

            if (this.promotionPlayerList != null && this.promotionPlayerList.contains(onlinePlayerIdx)) {
                GlobalData.getInstance().sendMsg(onlinePlayerIdx, MsgIdEnum.SC_RedBagMessage_VALUE, promotionMsg);
            } else {
                GlobalData.getInstance().sendMsg(onlinePlayerIdx, MsgIdEnum.SC_RedBagMessage_VALUE, commonMsg);
            }
        }
    }

    public void onPlayerLogin(String playerIdx) {
        if (PlayerUtil.queryFunctionLock(playerIdx, EnumFunction.EF_GloryRoad)
                || GameUtil.outOfScope(this.startTime, this.endTime, GlobalTick.getInstance().getCurrentTime())
                || this.claimedPlayer.contains(playerIdx)) {
            return;
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RedBagOpen_VALUE,
                SC_RedBagOpen.newBuilder().setEndTime(this.endTime));
    }

    public void addAllClaimedPlayer(Collection<String> claimedPlayer) {
        if (CollectionUtils.isEmpty(claimedPlayer)) {
            return;
        }
        this.claimedPlayer.addAll(claimedPlayer);
    }

    public DB_RedBag buildDbData() {
        DB_RedBag.Builder resultBuilder = DB_RedBag.newBuilder();
        resultBuilder.setStartTime(this.startTime);
        resultBuilder.setEntTime(this.endTime);
        resultBuilder.setSchedule(this.schedule);
        if (CollectionUtils.isNotEmpty(this.promotionPlayerList)) {
            resultBuilder.addAllPromotionPlayer(this.promotionPlayerList);
        }

        if (CollectionUtils.isNotEmpty(this.claimedPlayer)) {
            resultBuilder.addAllClaimedPlayer(this.claimedPlayer);
        }
        return resultBuilder.build();
    }

    public static RedBag createEntity(List<String> promotionPlayerList, EnumGloryRoadSchedule schedule, long startTime) {
        LogUtil.info("RedBag.createEntity, promotion:" + GameUtil.collectionToString(promotionPlayerList)
                + ", schedule:" + schedule);

        long endTime = startTime + GloryRoadConfig.getById(GameConst.CONFIG_ID).getRedbagduration() * TimeUtil.MS_IN_A_MIN;
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (GlobalTick.getInstance().getCurrentTime() >= endTime) {
            LogUtil.info("RedBag.createEntity, skip open red bag, currentTime:" + currentTime + " is max than end time :" + endTime);
            return null;
        }

        RedBag redBag = new RedBag();
        redBag.setStartTime(startTime);
        redBag.setEndTime(endTime);
        redBag.setPromotionPlayerList(promotionPlayerList);
        redBag.setSchedule(schedule);

        redBag.sendOpenMsg();
        return redBag;
    }

    public static RedBag createEntity(DB_RedBag dbRedBag) {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        if (dbRedBag == null || currentTime > dbRedBag.getEntTime()) {
            LogUtil.info("RedBag.createEntity, dbRedBag is null or currentTime:" + currentTime
                    + ", is max than endTime:" + (dbRedBag == null ? 0 : dbRedBag.getEntTime()));
            return null;
        }

        RedBag redBag = new RedBag();
        redBag.setStartTime(dbRedBag.getStartTime());
        redBag.setEndTime(dbRedBag.getEntTime());
        redBag.setSchedule(dbRedBag.getSchedule());
        redBag.setPromotionPlayerList(dbRedBag.getPromotionPlayerList());
        redBag.addAllClaimedPlayer(dbRedBag.getClaimedPlayerList());

        redBag.sendOpenMsg();
        return redBag;
    }
}
