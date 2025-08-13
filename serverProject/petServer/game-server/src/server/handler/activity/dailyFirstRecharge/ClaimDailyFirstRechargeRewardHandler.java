package server.handler.activity.dailyFirstRecharge;

import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import javafx.util.Pair;
import model.activity.ActivityManager;
import model.activity.DailyFirstRechargeManage;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.RetCodeId;
import protocol.Server;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MessageId.MsgIdEnum.CS_RandomDailyFirstRechargeReward_VALUE)
public class ClaimDailyFirstRechargeRewardHandler extends AbstractBaseHandler<Activity.CS_RandomDailyFirstRechargeReward> {
    @Override
    protected Activity.CS_RandomDailyFirstRechargeReward parse(byte[] bytes) throws Exception {
        return Activity.CS_RandomDailyFirstRechargeReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_RandomDailyFirstRechargeReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        LogUtil.info("player:{} ClaimDailyFirstRechargeReward ,req:{}", playerIdx, req);
        Activity.SC_RandomDailyFirstRechargeReward.Builder resultBuilder = Activity.SC_RandomDailyFirstRechargeReward.newBuilder();

        Server.ServerActivity activity = ActivityManager.getInstance().findOneRecentActivityByType(Activity.ActivityTypeEnum.ATE_DailyFirstRecharge);
        if (activity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MessageId.MsgIdEnum.SC_RandomDailyFirstRechargeReward_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_RandomDailyFirstRechargeReward_VALUE, resultBuilder);
            return;
        }
        if (!canExplore(entity)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_MissionCanNotClaim));
            gsChn.send(MessageId.MsgIdEnum.SC_RandomDailyFirstRechargeReward_VALUE, resultBuilder);
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, e -> {
            TargetSystemDB.DB_DailyFirstRecharge.Builder dailyFirstRecharge = entity.getDb_Builder().getSpecialInfoBuilder().getDailyFirstRechargeBuilder();
            useExploreTime(dailyFirstRecharge);
            Pair<Integer, List<Common.Reward>> randomResult = DailyFirstRechargeManage.getInstance().randomReward(dailyFirstRecharge.getEarnedRewardList());
            addEarnedRewards(dailyFirstRecharge, randomResult.getKey());
            doReward(playerIdx, randomResult);
            if (DailyFirstRechargeManage.getInstance().bigReward(randomResult.getKey())) {
                sendBigRewardMarquee(playerIdx,randomResult.getValue());
            }
            resultBuilder.setRewardIndex(randomResult.getKey());
        });
        entity.sendDailyFirstRechargeUpdate();
        resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_RandomDailyFirstRechargeReward_VALUE, resultBuilder);
    }

    private void sendBigRewardMarquee(String playerIdx, List<Common.Reward> rewardList) {
        if (CollectionUtils.isEmpty(rewardList)) {
            return;
        }
        GlobalData.getInstance().sendSpecialMarqueeToAllOnlinePlayer(DailyFirstRechargeManage.getBigRewardMarqueeId(),rewardList, PlayerUtil.queryPlayerName(playerIdx));
    }

    private void doReward(String playerIdx, Pair<Integer, List<Common.Reward>> randomResult) {
        ReasonManager.Reason reason = ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_DailyFirstPayExplore);
        RewardManager.getInstance().doRewardByList(playerIdx, randomResult.getValue(), reason, false);
    }

    private void addEarnedRewards(TargetSystemDB.DB_DailyFirstRecharge.Builder dailyFirstRecharge, int rewardIndex) {
        if (DailyFirstRechargeManage.getInstance().earnAllRewards(dailyFirstRecharge.getEarnedRewardCount() + 1)) {
            dailyFirstRecharge.clearEarnedReward();
            return;
        }
        dailyFirstRecharge.addEarnedReward(rewardIndex);
    }

    private void useExploreTime(TargetSystemDB.DB_DailyFirstRecharge.Builder dailyFirstRecharge) {
        dailyFirstRecharge.setExploreTimes(dailyFirstRecharge.getExploreTimes() - 1);
    }

    private boolean canExplore(targetsystemEntity entity) {
        return entity.getDb_Builder().getSpecialInfo().getDailyFirstRecharge().getExploreTimes() > 0;
    }


    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
