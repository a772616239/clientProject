package server.handler.activity.demonDescends;

import cfg.DemonDescendsConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimDemonDescendsRechargeReward;
import protocol.Activity.SC_ClaimDemonDescendsRechargeReward;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB.DB_DemonDescendsActivityInfo;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.10.08
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimDemonDescendsRechargeReward_VALUE)
public class ClaimDemonDescendsRechargeRewardHandler extends AbstractBaseHandler<CS_ClaimDemonDescendsRechargeReward> {
    @Override
    protected CS_ClaimDemonDescendsRechargeReward parse(byte[] bytes) throws Exception {
        return CS_ClaimDemonDescendsRechargeReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimDemonDescendsRechargeReward req, int i) {
        ServerActivity activity = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());

        SC_ClaimDemonDescendsRechargeReward.Builder resultBuilder = SC_ClaimDemonDescendsRechargeReward.newBuilder();
        if (activity == null || activity.getType() != ActivityTypeEnum.ATE_DemonDescends
                || GameUtil.outOfScope(activity.getBeginTime(), activity.getEndTime(), GlobalTick.getInstance().getCurrentTime())) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimDemonDescendsRechargeReward_VALUE, resultBuilder);
            return;
        }

        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimDemonDescendsRechargeReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            DB_DemonDescendsActivityInfo.Builder infoBuilder = entity.getDemonDescendsInfoBuilder(req.getActivityId());
            if (infoBuilder == null || infoBuilder.getRecharge().getCanClaimItemCount() <= 0) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_DemonDescends_RechargeItemAlreadyClaimed));
                gsChn.send(MsgIdEnum.SC_ClaimDemonDescendsRechargeReward_VALUE, resultBuilder);
                return;
            }

            Reward.Builder rewardBuilder = RewardUtil.parseRewardBuilder(DemonDescendsConfig.getById(GameConst.CONFIG_ID).getDrawuseitem());
            if (rewardBuilder == null) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimDemonDescendsRechargeReward_VALUE, resultBuilder);
                return;
            }

            int itemCount = infoBuilder.getRechargeBuilder().getCanClaimItemCount();
            rewardBuilder.setCount(itemCount);

            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DemonDescends_Recharge);
            RewardManager.getInstance().doReward(playerIdx, rewardBuilder.build(), reason, true);

            //清空累积道具
            infoBuilder.getRechargeBuilder().clearCanClaimItemCount();
            entity.putDemonDescendsInfoBuilder(infoBuilder);

            entity.refreshDemonDescendsActivityInfo(req.getActivityId());
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimDemonDescendsRechargeReward_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
