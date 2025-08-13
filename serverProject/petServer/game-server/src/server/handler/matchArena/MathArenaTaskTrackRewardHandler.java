package server.handler.matchArena;

import cfg.Mission;
import cfg.MissionObject;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem;
import protocol.TargetSystem.CS_MatchArenaTaskMissionReward;
import protocol.TargetSystem.SC_MatchArenaTaskMissionReward;
import protocol.TargetSystem.TargetMission;
import util.GameUtil;

import java.util.List;

@MsgId(msgId = MsgIdEnum.CS_MatchArenaTaskMissionReward_VALUE)
public class MathArenaTaskTrackRewardHandler extends AbstractBaseHandler<CS_MatchArenaTaskMissionReward> {
    @Override
    protected CS_MatchArenaTaskMissionReward parse(byte[] bytes) throws Exception {
        return CS_MatchArenaTaskMissionReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_MatchArenaTaskMissionReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        SC_MatchArenaTaskMissionReward.Builder resultBuilder = SC_MatchArenaTaskMissionReward.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_MatchArenaTaskMissionReward_VALUE, resultBuilder);
            return;
        }

        MissionObject missionCfg = Mission.getById(req.getIndex());
        if (missionCfg == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_MatchArenaTaskMissionReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            TargetMission missionPro = entity.getDb_Builder().getMatchArenaInfoMap().get(req.getIndex());
            if (missionPro == null || missionPro.getStatus() == MissionStatusEnum.MSE_UnFinished) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionUnfinished));
                gsChn.send(MsgIdEnum.SC_MatchArenaTaskMissionReward_VALUE, resultBuilder);
                return;
            }

            if (missionPro.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionAlreadyClaim));
                gsChn.send(MsgIdEnum.SC_MatchArenaTaskMissionReward_VALUE, resultBuilder);
                return;
            }

            List<Common.Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MatchArenaleitai);
            RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);

            TargetMission newdata = missionPro.toBuilder().setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build();
            entity.getDb_Builder().putMatchArenaInfo(missionPro.getCfgId(), newdata);

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_MatchArenaTaskMissionReward_VALUE, resultBuilder);

            TargetSystem.SC_RefMatchArenaTaskMission.Builder refreshBuilder = TargetSystem.SC_RefMatchArenaTaskMission.newBuilder();
            refreshBuilder.addMission(newdata);
            GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RefMatchArenaTaskMission_VALUE, refreshBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_MatchArena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_MatchArenaTaskMissionReward_VALUE, SC_MatchArenaTaskMissionReward.newBuilder().setRetCode(retCode));
    }
}
