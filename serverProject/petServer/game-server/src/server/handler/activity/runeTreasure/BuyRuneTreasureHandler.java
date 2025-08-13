package server.handler.activity.runeTreasure;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_BuyRuneTreasure;
import protocol.Activity.SC_BuyRuneTreasure;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.Server.ServerBuyMission;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystemDB.DB_RuneTreasureInfo;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/11/26
 */
@MsgId(msgId = MsgIdEnum.CS_BuyRuneTreasure_VALUE)
public class BuyRuneTreasureHandler extends AbstractBaseHandler<CS_BuyRuneTreasure> {
    @Override
    protected CS_BuyRuneTreasure parse(byte[] bytes) throws Exception {
        return CS_BuyRuneTreasure.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_BuyRuneTreasure req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_BuyRuneTreasure.Builder resultBuilder = SC_BuyRuneTreasure.newBuilder();
        ServerBuyMission buyMissionCfg;
        if (entity == null
                || !ActivityUtil.activityInOpen(activityCfg)
                || activityCfg.getType() != ActivityTypeEnum.ATE_RuneTreasure
                || (buyMissionCfg = activityCfg.getBuyMissionMap().get(req.getGoodsIndex())) == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_BuyRuneTreasure_VALUE, resultBuilder);
            return;
        }

        if (buyMissionCfg.getEndTimestamp() != -1 && GlobalTick.getInstance().getCurrentTime() > buyMissionCfg.getEndTimestamp()) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_MissionOutOfTime));
            gsChn.send(MsgIdEnum.SC_BuyRuneTreasure_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum retCode = SyncExecuteFunction.executeFunction(entity, e -> {
            DB_RuneTreasureInfo.Builder runeTreasureInfoBuilder = entity.getDbRuneTreasureInfoBuilder(req.getActivityId());
            TargetMission targetMission = runeTreasureInfoBuilder.getBuyMissionProMap().get(req.getGoodsIndex());
            TargetMission.Builder buyPro = targetMission == null ? TargetMission.newBuilder().setCfgId(buyMissionCfg.getIndex()) : targetMission.toBuilder();
            if ((buyPro.getProgress() + req.getBuyTimes()) > buyMissionCfg.getLimitBuy()) {
                return RetCodeEnum.RCE_Activity_RewardAlreadyClaim;
            }

            Consume consume = ConsumeUtil.multiConsume(buyMissionCfg.getPrice(), req.getBuyTimes());
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RuneTreasure);
            if (!ConsumeManager.getInstance().asyncConsumeMaterial(playerIdx, consume, reason)) {
                return RetCodeEnum.RCE_MatieralNotEnough;
            }

            List<Reward> rewards = RewardUtil.multiReward(buyMissionCfg.getRewardsList(), req.getBuyTimes());
            RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);

            buyPro.setProgress(buyPro.getProgress() + req.getBuyTimes());
            runeTreasureInfoBuilder.putBuyMissionPro(buyPro.getCfgId(), buyPro.build());
            entity.putRuneTreasureInfoBuilder(runeTreasureInfoBuilder);

            resultBuilder.setNewBuyProgress(runeTreasureInfoBuilder.getBuyMissionProMap().get(req.getGoodsIndex()));
            return RetCodeEnum.RCE_Success;
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_BuyRuneTreasure_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
