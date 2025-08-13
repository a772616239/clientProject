package server.handler.arena;

import cfg.Mission;
import cfg.MissionObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Arena.CS_ClaimArenaMissionReward;
import protocol.Arena.SC_ClaimArenaMissionReward;
import protocol.Arena.SC_ClaimArenaMissionReward.Builder;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetMission;
import util.GameUtil;

/**
 * @author huhan
 * @date 2020.09.02
 */
@MsgId(msgId = MsgIdEnum.CS_ClaimArenaMissionReward_VALUE)
public class ClaimArenaMissionRewardHandler extends AbstractBaseHandler<CS_ClaimArenaMissionReward> {
    @Override
    protected CS_ClaimArenaMissionReward parse(byte[] bytes) throws Exception {
        return CS_ClaimArenaMissionReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimArenaMissionReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        Builder resultBuilder = SC_ClaimArenaMissionReward.newBuilder();
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_ClaimArenaMissionReward_VALUE, resultBuilder);
            return;
        }

        MissionObject missionCfg = Mission.getById(req.getIndex());
        if (missionCfg == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_ClaimArenaMissionReward_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            TargetMission missionPro = entity.getArenaMission(req.getIndex());
            if (missionPro == null || missionPro.getStatus() == MissionStatusEnum.MSE_UnFinished) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionUnfinished));
                gsChn.send(MsgIdEnum.SC_ClaimArenaMissionReward_VALUE, resultBuilder);
                return;
            }

            if (missionPro.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
                resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionAlreadyClaim));
                gsChn.send(MsgIdEnum.SC_ClaimArenaMissionReward_VALUE, resultBuilder);
                return;
            }

            List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_ArenaMission);
            RewardManager.getInstance().doRewardByList(playerIdx, rewards, reason, true);

            entity.putArenaMission(missionPro.toBuilder().setStatus(MissionStatusEnum.MSE_FinishedAndClaim).build());

            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimArenaMissionReward_VALUE, resultBuilder);
        });
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Arena;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimArenaMissionReward_VALUE, SC_ClaimArenaMissionReward.newBuilder().setRetCode(retCode));
    }
}
