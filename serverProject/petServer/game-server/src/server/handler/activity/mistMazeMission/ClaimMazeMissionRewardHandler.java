package server.handler.activity.mistMazeMission;

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
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_ClamMazeMissionReward;
import protocol.TargetSystem.SC_ClamMazeMissionReward;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystemDB.DB_MistMazeActivity;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_ClamMazeMissionReward_VALUE)
public class ClaimMazeMissionRewardHandler extends AbstractBaseHandler<CS_ClamMazeMissionReward> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.MistMaze;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        SC_ClamMazeMissionReward.Builder builder = SC_ClamMazeMissionReward.newBuilder();
        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance));
    }

    @Override
    protected CS_ClamMazeMissionReward parse(byte[] bytes) throws Exception {
        return CS_ClamMazeMissionReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_ClamMazeMissionReward req, int i) {
        String playerIdx = String.valueOf(gsChn.getPlayerId1());
        SC_ClamMazeMissionReward.Builder retBuilder = SC_ClamMazeMissionReward.newBuilder();
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_MistMazeActivityMission_VALUE, retBuilder);
            return;
        }
        MissionObject mission = Mission.getById(req.getMissionId());
        if (mission == null) {
            retBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Target_MissionCfgIdNotExist));
            gsChn.send(MsgIdEnum.SC_MistMazeActivityMission_VALUE, retBuilder);
            return;
        }

        RetCodeEnum retCodeEnum = SyncExecuteFunction.executeFunction(entity, targetSystem->{
            DB_MistMazeActivity.Builder builder = entity.getDb_Builder().getSpecialInfoBuilder().getMazeActivityMissionBuilder();
            for (int index = 0; index < builder.getMissionProCount(); index++) {
                TargetMission.Builder target = builder.getMissionProBuilder(index);
                if (target.getCfgId() == req.getMissionId()) {
                    if (target.getStatus() == MissionStatusEnum.MSE_Finished) {
                        target.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);
                        return RetCodeEnum.RCE_Success;
                    } else if (target.getStatus() == MissionStatusEnum.MSE_UnFinished) {
                        return RetCodeEnum.RCE_Target_MissionUnfinished;
                    } else if (target.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
                        return RetCodeEnum.RCE_Target_MissionAlreadyClaim;
                    }
                    break;
                }
            }
            return RetCodeEnum.RCE_Target_MissionUnfinished;
        });
        if (retCodeEnum == RetCodeEnum.RCE_Success) {
            List<Reward> rewardList = RewardUtil.parseRewardIntArrayToRewardList(mission.getFinishreward());
            if (CollectionUtils.isEmpty(rewardList)) {
                return;
            }
            RewardManager.getInstance().doRewardByList(entity.getLinkplayeridx(), rewardList, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MistMaze), true);
        }
        retBuilder.setRetCode(GameUtil.buildRetCode(retCodeEnum));
        gsChn.send(MsgIdEnum.SC_ClamMazeMissionReward_VALUE, retBuilder);
    }
}
