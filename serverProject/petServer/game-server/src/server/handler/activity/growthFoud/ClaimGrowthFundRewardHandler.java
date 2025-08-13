package server.handler.activity.growthFoud;

import cfg.GrowthFundConfig;
import cfg.GrowthFundConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityUtil;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Activity;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId;
import protocol.RetCodeId;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;

import java.util.List;

/**
 * @Description
 * @Author hanx
 * @Date2020/6/3 0003 15:42
 **/
@MsgId(msgId = MessageId.MsgIdEnum.CS_ClaimGrowthFundReward_VALUE)
public class ClaimGrowthFundRewardHandler extends AbstractBaseHandler<Activity.CS_ClaimGrowthFundReward> {
    @Override
    protected Activity.CS_ClaimGrowthFundReward parse(byte[] bytes) throws Exception {
        return Activity.CS_ClaimGrowthFundReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, Activity.CS_ClaimGrowthFundReward req, int i) {
        Activity.SC_ClaimGrowthFundReward.Builder result = Activity.SC_ClaimGrowthFundReward.newBuilder();
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        LogUtil.info("receive playerIdx:{} claimGrowthFundReward , req:{}", playerIdx, req);
        targetsystemEntity targetsystemEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (targetsystemEntity == null || player == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimGrowthFundReward_VALUE, result);
            return;
        }

        if (player.alreadyClaimed(ActivityUtil.LocalActivityId.GrowthFund, req.getGrowthFundId())) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_RewardAlreadyClaim));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimGrowthFundReward_VALUE, result);
            return;
        }

        int growthFundId = req.getGrowthFundId();
        TargetSystemDB.DB_GrowthFund growthFund = targetsystemEntity.getDb_Builder().getSpecialInfo().getGrowthFund();
        //是否购买
        if (!growthFund.getBuy()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_GrowthFund_NotBuy));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimGrowthFundReward_VALUE, result);
            return;
        }

        GrowthFundConfigObject config = GrowthFundConfig.getById(growthFundId);
        if (config == null) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_UnknownError));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimGrowthFundReward_VALUE, result);
            return;
        }
        //玩家条件不满足
        if (player.getLevel() < config.getTargetplayerlv()) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_DissatisfyAddition));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimGrowthFundReward_VALUE, result);
            return;
        }
        if (growthFund.getClaimedIdListList().contains(growthFundId)) {
            result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Activity_RewardAlreadyClaim));
            gsChn.send(MessageId.MsgIdEnum.SC_ClaimGrowthFundReward_VALUE, result);
            return;
        }
        updateGrowthFondAndDoReward(playerIdx, targetsystemEntity, growthFundId, config);

        player.increasePlayerRewardRecord(ActivityUtil.LocalActivityId.GrowthFund, growthFundId);

        LogUtil.info("playerIdx:{} claimGrowthFundReward , req:{} success");
        result.setRetCode(GameUtil.buildRetCode(RetCodeId.RetCodeEnum.RCE_Success));
        gsChn.send(MessageId.MsgIdEnum.SC_ClaimGrowthFundReward_VALUE, result);

    }

    private void updateGrowthFondAndDoReward(String playerIdx, targetsystemEntity targetsystemEntity, int growthFundId, GrowthFundConfigObject config) {
        LogUtil.info(" playerIdx:{} claimGrowthFundReward  updateGrowthFondAndDoReward, before growthFoundIds:{}",
                playerIdx, targetsystemEntity.getDb_Builder().getSpecialInfo().getGrowthFund().getClaimedIdListList());

        SyncExecuteFunction.executeConsumer(targetsystemEntity, entity -> {
            targetsystemEntity.getDb_Builder().getSpecialInfoBuilder().getGrowthFundBuilder().addClaimedIdList(growthFundId);

            List<Common.Reward> rewards = RewardUtil.getRewardsByRewardId(config.getReward());
            //发放奖励
            RewardManager.getInstance().doRewardByList(playerIdx, rewards,
                    ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_GrowthFund), true);
        });
        LogUtil.info(" playerIdx:{} claimGrowthFundReward  updateGrowthFondAndDoReward, now growthFoundIds:{}",
                playerIdx, targetsystemEntity.getDb_Builder().getSpecialInfo().getGrowthFund().getClaimedIdListList());
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
