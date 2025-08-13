package server.handler.training;

import cfg.Mission;
import cfg.MissionObject;
import cfg.TrainingMap;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.training.dbCache.trainingCache;
import model.training.entity.trainingEntity;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.MissionStatusEnum;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_TrainGainTaskReward;
import protocol.TargetSystem.SC_TrainGainTaskReward;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystemDB.TrainingMapTaskData.Builder;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainGainTaskReward_VALUE)
public class GainTrainTaskHandler extends AbstractBaseHandler<CS_TrainGainTaskReward> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Training;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_TrainGainTaskReward_VALUE,
                SC_TrainGainTaskReward.newBuilder().setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance)));
    }

    @Override
    protected CS_TrainGainTaskReward parse(byte[] bytes) throws Exception {
        return CS_TrainGainTaskReward.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_TrainGainTaskReward req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity targetsystemEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (targetsystemEntity == null) {
            return;
        }
        SC_TrainGainTaskReward.Builder builder = SC_TrainGainTaskReward.newBuilder();
        trainingEntity trainingEntity = trainingCache.getInstance().getCacheByPlayer(playerIdx);
        if (trainingEntity == null || trainingEntity.getInfoDB().containsEndMap(req.getMapId())) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN));
            gsChn.send(MsgIdEnum.SC_TrainGainTaskReward_VALUE, builder);
            return;
        }
        if (TrainingMap.getByMapid(req.getMapId()) == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_TrainGainTaskReward_VALUE, builder);
            return;
        }
        MissionObject mission = Mission.getById(req.getTaskId());
        if (mission == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_TrainGainTaskReward_VALUE, builder);
            return;
        }
        RetCodeEnum retCode = SyncExecuteFunction.executeFunction(targetsystemEntity, targetEntity -> {
            for (Builder mapData : targetsystemEntity.getDb_Builder().getTrainingTaskDataBuilder().getMapTaskDataBuilderList()) {
                if (mapData.getMapId() == req.getMapId()) {
                    for (TargetMission.Builder trainTask : mapData.getTrainTaskBuilderList()) {
                        if (trainTask.getCfgId() == req.getTaskId()) {
                            if (trainTask.getStatus() == MissionStatusEnum.MSE_Finished) {
                                trainTask.setStatus(MissionStatusEnum.MSE_FinishedAndClaim);
                                return RetCodeEnum.RCE_Success;
                            } else if (trainTask.getStatus() == MissionStatusEnum.MSE_UnFinished) {
                                return RetCodeEnum.RCE_Target_MissionUnfinished;
                            } else if (trainTask.getStatus() == MissionStatusEnum.MSE_FinishedAndClaim) {
                                return RetCodeEnum.RCE_Target_MissionAlreadyClaim;
                            }
                            return RetCodeEnum.RCE_Target_MissionUnfinished;
                        }
                    }
                    break;
                }
            }
            return RetCodeEnum.RCE_Target_MissionUnfinished;
        });
        if (retCode == RetCodeEnum.RCE_Success) {
            List<Reward> rewardList = RewardUtil.parseRewardIntArrayToRewardList(mission.getFinishreward());
            RewardManager.getInstance().doRewardByList(playerIdx, rewardList, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Train_Task), true);
        }
        builder.setRetCode(GameUtil.buildRetCode(retCode));
        gsChn.send(MsgIdEnum.SC_TrainGainTaskReward_VALUE, builder);
    }
}
