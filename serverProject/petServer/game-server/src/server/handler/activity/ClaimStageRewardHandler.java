package server.handler.activity;

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
import protocol.Activity.CS_ClaimStageReward;
import protocol.Activity.SC_ClaimStageReward;
import protocol.Activity.StageRewards;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.Server.ServerActivity;
import protocol.TargetSystemDB;
import util.GameUtil;
import util.LogUtil;

/**
 *
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimStageReward_VALUE)
public class ClaimStageRewardHandler extends AbstractBaseHandler<CS_ClaimStageReward> {
    @Override
    protected CS_ClaimStageReward parse(byte[] bytes) throws Exception {
        return CS_ClaimStageReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimStageReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        int claimIndex = req.getRewardIndex();
        long activityId = req.getActivityId();
        ServerActivity activityCfg = ActivityManager.getInstance().getActivityCfgById(activityId);
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_ClaimStageReward.Builder resultBuilder = SC_ClaimStageReward.newBuilder();
        if (entity == null
                || !ActivityUtil.activityInOpen(activityCfg)
                || activityCfg.getStageRewardsMap().get(claimIndex) == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimStageReward_VALUE, resultBuilder);
            return;
        }

        LogUtil.info("playerIdx:{} ClaimStageReward req:{},activityType:{}", playerIdx, req, activityCfg.getType());
        StageRewards stageRewards = activityCfg.getStageRewardsMap().get(claimIndex);
        RetCodeEnum retCode = SyncExecuteFunction.executeFunction(entity, e -> {

            if (!canClaim(claimIndex, stageRewards, activityId, playerIdx, entity)) {
                return RetCodeEnum.RCE_Activity_MissionCanNotClaim;
            }

            updateClaimRecord(req, entity);

            Reason reason = getRewardReason(activityCfg);
            RewardManager.getInstance().doRewardByList(playerIdx, stageRewards.getRewardsList(), reason, false);

            LogUtil.info("playerIdx:{} ClaimStageReward  finish req:{}", playerIdx, req);

            return RetCodeEnum.RCE_Success;
        });

        if (RetCodeEnum.RCE_Success == retCode) {
            entity.sendUpdateStageRewardInfo(activityId);
        }

        resultBuilder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_ClaimStageReward_VALUE, resultBuilder);
    }

    private Reason getRewardReason(ServerActivity activityCfg) {
        if (ActivityTypeEnum.ATE_RichMan == activityCfg.getType()) {
            return ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_RichMan);
        }
        return ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Activity);
    }

    private boolean canClaim(int claimIndex, StageRewards stageRewards, long activityId, String playerIdx, targetsystemEntity entity) {
        TargetSystemDB.DB_StageRewardClaimInfo record = peekOneDbStageReward(activityId, entity);

        LogUtil.info("playerIdx:{} ClaimStageReward record in DB:{}", playerIdx, record);

        return record != null && record.getCurTarget() >= stageRewards.getNeedDrawTimes()
                && !record.getClaimedIndexList().contains(claimIndex);
    }

    private void updateClaimRecord(CS_ClaimStageReward req, targetsystemEntity entity) {
        TargetSystemDB.DB_StageRewardClaimInfo.Builder newRecord = entity.pullOneDbStageReward(req.getActivityId());
        newRecord.addClaimedIndex(req.getRewardIndex());
        entity.getDb_Builder().addStageRewardClaimInfo(newRecord);
        LogUtil.info("playerIdx:{} ClaimStageReward saveClaimRecord :{}", newRecord);
    }


    private TargetSystemDB.DB_StageRewardClaimInfo peekOneDbStageReward(long activityId, targetsystemEntity entity) {
        for (TargetSystemDB.DB_StageRewardClaimInfo record : entity.getDb_Builder().getStageRewardClaimInfoList()) {
            if (record.getActivityId() == activityId) {
                return record;
            }
        }
        return null;
    }

    @Override
    public EnumFunction belongFunction() {
        return null;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }
}
