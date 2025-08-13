package server.handler.mistforest;

import cfg.Mission;
import cfg.MissionObject;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_ClaimMistTargetMissionReward;
import protocol.TargetSystem.SC_ClaimMistTargetMissionReward;
import protocol.TargetSystem.TargetMission.Builder;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimMistTargetMissionReward_VALUE)
public class ClaimMistTargetMissionRewardHandler extends AbstractBaseHandler<CS_ClaimMistTargetMissionReward> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_ClaimMistTargetMissionReward_VALUE, SC_ClaimMistTargetMissionReward.newBuilder().setRetCode(retCode));
    }

    @Override
    protected CS_ClaimMistTargetMissionReward parse(byte[] bytes) throws Exception {
        return CS_ClaimMistTargetMissionReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimMistTargetMissionReward req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity target = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (target == null) {
            return;
        }
        SC_ClaimMistTargetMissionReward.Builder builder = SC_ClaimMistTargetMissionReward.newBuilder();
        RetCodeEnum retCodeEnum = RetCodeEnum.RCE_Target_MissionCfgIdNotExist;
        MissionObject missionCfg = Mission.getById(req.getMissionId());
        if (missionCfg != null) {
            for (Builder targetBuilder : target.getDb_Builder().getMistTaskDataBuilder().getTargetMissionDbBuilder().getTargetMissionBuilderList()) {
                if (targetBuilder.getCfgId() == req.getMissionId()) {
                    if (targetBuilder.getStatus() == MissionStatusEnum.MSE_Finished) {
                        List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
                        if (!CollectionUtils.isEmpty(rewards)) {
                            Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForest);
                            RewardManager.getInstance().doRewardByList(target.getLinkplayeridx(), rewards, reason, true);
                        }
                        targetBuilder.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);
                        retCodeEnum = RetCodeEnum.RCE_Success;
                    } else if (targetBuilder.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
                        retCodeEnum = RetCodeEnum.RCE_Target_MissionAlreadyClaim;
                    } else {
                        retCodeEnum = RetCodeEnum.RCE_Target_MissionUnfinished;
                    }
                    break;
                }
            }
        }
        builder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_ClaimMistTargetMissionReward_VALUE, builder);
    }
}
