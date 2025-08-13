package server.handler.activity.runeTreasure;

import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.activity.ActivityManager;
import model.activity.ActivityUtil;
import model.reward.RewardManager;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Activity.ActivityTypeEnum;
import protocol.Activity.CS_ClaimRuneTreasureStageReward;
import protocol.Activity.SC_ClaimRuneTreasureStageReward;
import protocol.Activity.StageRewards;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB.DB_RuneTreasureInfo;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020/11/27
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimRuneTreasureStageReward_VALUE)
public class ClaimRuneTreasureStageRewardHandler extends AbstractBaseHandler<CS_ClaimRuneTreasureStageReward> {
    @Override
    protected CS_ClaimRuneTreasureStageReward parse(byte[] bytes) throws Exception {
        return CS_ClaimRuneTreasureStageReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimRuneTreasureStageReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(req.getActivityId());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_ClaimRuneTreasureStageReward.Builder resultBuilder = SC_ClaimRuneTreasureStageReward.newBuilder();
        if (!ActivityUtil.activityInOpen(activityCfg)) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Activity_NotOpen));
            gsChn.send(MsgIdEnum.SC_DrawRuneTreasure_VALUE, resultBuilder);
            return;
        }
        if (entity == null
                || activityCfg.getType() != ActivityTypeEnum.ATE_RuneTreasure
                || activityCfg.getStageRewardsMap().get(req.getIndex()) == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimRuneTreasureStageReward_VALUE, resultBuilder);
            return;
        }

        StageRewards stageRewards = activityCfg.getStageRewardsMap().get(req.getIndex());
        RetCodeEnum retCode = SyncExecuteFunction.executeFunction(entity, e -> {
            DB_RuneTreasureInfo.Builder infoBuilder = entity.getDbRuneTreasureInfoBuilder(req.getActivityId());
            if (infoBuilder.getDrawTimes() < stageRewards.getNeedDrawTimes()
                    || infoBuilder.getClaimedProgressList().contains(req.getIndex())) {
                return RetCodeEnum.RCE_Activity_MissionCanNotClaim;
            }
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RuneTreasure);
            RewardManager.getInstance().doRewardByList(playerIdx, stageRewards.getRewardsList(), reason, false);

            infoBuilder.addClaimedProgress(req.getIndex());

            entity.putRuneTreasureInfoBuilder(infoBuilder);
            return RetCodeEnum.RCE_Success;
        });

        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_ClaimRuneTreasureStageReward_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
