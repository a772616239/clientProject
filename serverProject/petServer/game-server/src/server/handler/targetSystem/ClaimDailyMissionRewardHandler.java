package server.handler.targetSystem;

import cfg.DailyMission;
import cfg.DailyMissionObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import java.util.Map;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_ClaimDailyMissionReward;
import protocol.TargetSystem.SC_ClaimDailyMissionReward;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystemDB.DB_TargetSystem.Builder;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimDailyMissionReward_VALUE)
public class ClaimDailyMissionRewardHandler extends AbstractBaseHandler<CS_ClaimDailyMissionReward> {
    @Override
    protected CS_ClaimDailyMissionReward parse(byte[] bytes) throws Exception {
        return CS_ClaimDailyMissionReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimDailyMissionReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimDailyMissionReward.Builder resultBuilder = SC_ClaimDailyMissionReward.newBuilder();
        int cfgId = req.getCfgId();
        DailyMissionObject cfg = DailyMission.getById(cfgId);
        if (cfg == null) {
            LogUtil.error("dailyMission cfg is null, cfgId = " + cfgId);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionCfgIdNotExist));
            gsChn.send(MsgIdEnum.SC_ClaimDailyMissionReward_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionCfgIdNotExist));
            gsChn.send(MsgIdEnum.SC_ClaimDailyMissionReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(target, entity -> {
            Builder db_data = entity.getDb_Builder();
            if (db_data == null) {
                LogUtil.error("playerIdx[" + playerIdx + "] targetDbData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimDailyMissionReward_VALUE, resultBuilder);
                return;
            }

            Map<Integer, TargetMission> dailyMissionMap = db_data.getDailyMissionMap();

            TargetMission targetMission = dailyMissionMap.get(cfgId);
            if (targetMission == null || targetMission.getStatus() == MissionStatusEnum.MSE_UnFinished) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionUnfinished));
                gsChn.send(MsgIdEnum.SC_ClaimDailyMissionReward_VALUE, resultBuilder);
                return;
            }

            if (targetMission.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionAlreadyClaim));
                gsChn.send(MsgIdEnum.SC_ClaimDailyMissionReward_VALUE, resultBuilder);
                return;
            }

            List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(cfg.getFinishreward());
            RewardManager.getInstance().doRewardByList(playerIdx, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_DailyMission), true);

            TargetMission.Builder builder = targetMission.toBuilder();
            builder.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);
            db_data.putDailyMission(builder.getCfgId(), builder.build());

            target.sendRefreshDailyMissionMsg(builder.build());

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimDailyMissionReward_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.DailyMission;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimDailyMissionReward_VALUE, SC_ClaimDailyMissionReward.newBuilder().setRetCode(retCode));
    }
}
