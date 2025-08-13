package server.handler.mistforest;

import cfg.Mission;
import cfg.MissionObject;
import cfg.MistNewbieTaskConfig;
import cfg.MistNewbieTaskConfigObject;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.warpServer.crossServer.CrossServerManager;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_ClaimMistNewbieTaskReward;
import protocol.TargetSystem.SC_ClaimMistNewbieTaskReward;
import protocol.TargetSystem.TargetMission;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClaimMistNewbieTaskReward_VALUE)
public class ClaimMistNewbieTaskRewardHandler extends AbstractBaseHandler<CS_ClaimMistNewbieTaskReward> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistForest;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {

    }

    @Override
    protected CS_ClaimMistNewbieTaskReward parse(byte[] bytes) throws Exception {
        return CS_ClaimMistNewbieTaskReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClaimMistNewbieTaskReward req, int i) {
        String playerId = GameUtil.longToString(gsChn.getPlayerId1(), "");
        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return;
        }
        targetsystemEntity targetSystem = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerId);
        if (targetSystem == null) {
            return;
        }
        if (CrossServerManager.getInstance().getMistForestPlayerServerIndex(playerId) <= 0) {
            return;
        }
        SyncExecuteFunction.executeConsumer(targetSystem, entity->{
            SC_ClaimMistNewbieTaskReward.Builder builder = SC_ClaimMistNewbieTaskReward.newBuilder();
            TargetMission.Builder targetBuilder = entity.getDb_Builder().getMistTaskDataBuilder().getCurNewbieTaskBuilder();
            if (targetBuilder.getCfgId() <= 0) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionNotAccept));
                gsChn.send(MsgIdEnum.SC_ClaimMistNewbieTaskReward_VALUE, builder);
                return;
            }
            MistNewbieTaskConfigObject cfg = MistNewbieTaskConfig.getById(targetBuilder.getCfgId());
            if (cfg == null) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionCfgIdNotExist));
                gsChn.send(MsgIdEnum.SC_ClaimMistNewbieTaskReward_VALUE, builder);
                return;
            }
            MissionObject missionCfg = Mission.getById(cfg.getMissionid());
            if (missionCfg == null) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionCfgIdNotExist));
                gsChn.send(MsgIdEnum.SC_ClaimMistNewbieTaskReward_VALUE, builder);
                return;
            }
            if (targetBuilder.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionAlreadyClaim));
                gsChn.send(MsgIdEnum.SC_ClaimMistNewbieTaskReward_VALUE, builder);
                return;
            } else if (targetBuilder.getStatus() != MissionStatusEnum.MSE_Finished) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionUnfinished));
                gsChn.send(MsgIdEnum.SC_ClaimMistNewbieTaskReward_VALUE, builder);
                return;
            }
            MistNewbieTaskConfigObject nextTaskCfg = MistNewbieTaskConfig.getById(targetBuilder.getCfgId() + 1); // 按顺序
            if (nextTaskCfg != null) {
                builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionUnfinished));
                gsChn.send(MsgIdEnum.SC_ClaimMistNewbieTaskReward_VALUE, builder);
                return;
            }
            targetBuilder.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);
            List<Reward> rewards = RewardUtil.parseRewardIntArrayToRewardList(missionCfg.getFinishreward());
            if (!CollectionUtils.isEmpty(rewards)) {
                RewardManager.getInstance().doRewardByList(playerId, rewards, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistForest), true);
            }
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_ClaimMistNewbieTaskReward_VALUE, builder);
        });
    }
}
