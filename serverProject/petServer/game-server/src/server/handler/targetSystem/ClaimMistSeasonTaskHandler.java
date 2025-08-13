package server.handler.targetSystem;

import cfg.MistSeasonMission;
import cfg.MistSeasonMissionObject;
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
import protocol.TargetSystem.CS_ClaimMistSeasonMissionReward;
import protocol.TargetSystem.SC_ClaimMistSeasonMissionReward;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystemDB.DB_TargetSystem.Builder;
import util.GameUtil;
import util.LogUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimMistSeasonMissionReward_VALUE)
public class ClaimMistSeasonTaskHandler extends AbstractBaseHandler<CS_ClaimMistSeasonMissionReward> {
    @Override
    protected CS_ClaimMistSeasonMissionReward parse(byte[] bytes) throws Exception {
        return CS_ClaimMistSeasonMissionReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimMistSeasonMissionReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());

        SC_ClaimMistSeasonMissionReward.Builder resultBuilder = SC_ClaimMistSeasonMissionReward.newBuilder();
        int cfgId = req.getCfgId();
        MistSeasonMissionObject cfg = MistSeasonMission.getById(cfgId);
        if (cfg == null) {
            LogUtil.error("mistSeasonMission cfg is null, cfgId = " + cfgId);
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionCfgIdNotExist));
            gsChn.send(MsgIdEnum.SC_ClaimMistSeasonMissionReward_VALUE, resultBuilder);
            return;
        }

        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (target == null) {
            LogUtil.error("playerIdx[" + playerIdx + "] claimMistSeasonTask entity is null");
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionCfgIdNotExist));
            gsChn.send(MsgIdEnum.SC_ClaimMistSeasonMissionReward_VALUE, resultBuilder);
            return;
        }
        SyncExecuteFunction.executeConsumer(target, entity -> {
            Builder db_data = entity.getDb_Builder();
            if (db_data == null) {
                LogUtil.error("playerIdx[" + playerIdx + "] claimMistSeasonTask targetDbData is null");
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
                gsChn.send(MsgIdEnum.SC_ClaimMistSeasonMissionReward_VALUE, resultBuilder);
                return;
            }

            Map<Integer, TargetMission> mistSeasonTaskMap = db_data.getMistSeasonTaskMap();

            TargetMission targetMission = mistSeasonTaskMap.get(cfgId);
            if (targetMission == null || targetMission.getStatus() == MissionStatusEnum.MSE_UnFinished) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionUnfinished));
                gsChn.send(MsgIdEnum.SC_ClaimMistSeasonMissionReward_VALUE, resultBuilder);
                return;
            }

            if (targetMission.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionAlreadyClaim));
                gsChn.send(MsgIdEnum.SC_ClaimMistSeasonMissionReward_VALUE, resultBuilder);
                return;
            }

            List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(cfg.getFinishreward());
            RewardManager.getInstance().doRewardByList(playerIdx, rewards,
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForestSeasonTask, "首达奖励"), true);

            TargetMission.Builder builder = targetMission.toBuilder();
            builder.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);
            db_data.putMistSeasonTask(builder.getCfgId(), builder.build());

            target.sendMistSeasonTaskMsg(builder.build());

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimMistSeasonMissionReward_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimMistSeasonMissionReward_VALUE, SC_ClaimMistSeasonMissionReward.newBuilder().setRetCode(retCode));
    }
}
