package server.handler.training;

import cfg.Mission;
import cfg.MissionObject;
import cfg.TrainingMap;
import cfg.TrainingMapObject;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import model.training.dbCache.trainingCache;
import model.training.entity.trainingEntity;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.CS_TrainUpdateTask;
import protocol.TargetSystem.SC_TrainUpdateTask;
import protocol.TargetSystem.TargetMission;
import protocol.TargetSystemDB.TrainingMapTaskData;
import util.GameUtil;

@MsgId(msgId = MsgIdEnum.CS_TrainUpdateTask_VALUE)
public class UpdateTraingTaskHandler extends AbstractBaseHandler<CS_TrainUpdateTask> {
    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.Training;
    }

    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        gsChn.send(MsgIdEnum.SC_TrainUpdateTask_VALUE,
                SC_TrainUpdateTask.newBuilder().setRetCode(GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance)));
    }

    @Override
    protected CS_TrainUpdateTask parse(byte[] bytes) throws Exception {
        return CS_TrainUpdateTask.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_TrainUpdateTask req, int i) {
        String playerIdx = GameUtil.longToString(gsChn.getPlayerId1(), "");
        targetsystemEntity targetsystemEntity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (targetsystemEntity == null) {
            return;
        }
        SC_TrainUpdateTask.Builder builder = SC_TrainUpdateTask.newBuilder();
        trainingEntity trainingEntity = trainingCache.getInstance().getCacheByPlayer(playerIdx);
        if (trainingEntity == null || trainingEntity.getInfoDB().containsEndMap(req.getMapId())) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_TRAIN_NOTOPEN));
            gsChn.send(MsgIdEnum.SC_TrainUpdateTask_VALUE, builder);
            return;
        }
        TrainingMapObject mapCfg = TrainingMap.getByMapid(req.getMapId());
        if (mapCfg == null) {
            builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_TrainUpdateTask_VALUE, builder);
            return;
        }
        if (mapCfg.getScoretasklist() != null && mapCfg.getScoretasklist().length > 0) {
            TrainingMapTaskData mapTaskDataData = null;
            for (TrainingMapTaskData tmpTaskMap : targetsystemEntity.getDb_Builder().getTrainingTaskData().getMapTaskDataList()) {
                if (tmpTaskMap.getMapId() == req.getMapId()) {
                    mapTaskDataData = tmpTaskMap;
                    break;
                }
            }
            MissionObject mission;
            for (int missionId : mapCfg.getScoretasklist()) {
                mission = Mission.getById(missionId);
                if (mission == null) {
                    continue;
                }
                boolean existFlag = false;
                if (mapTaskDataData != null) {
                    for (TargetMission taskData : mapTaskDataData.getTrainTaskList()) {
                        if (missionId == taskData.getCfgId()) {
                            existFlag = true;
                            builder.addTasks(taskData);
                            break;
                        }
                    }
                }
                if (!existFlag) {
                    builder.addTasks(TargetMission.newBuilder().setCfgId(missionId));
                }
            }
        }

        builder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(MsgIdEnum.SC_TrainUpdateTask_VALUE, builder);
    }
}
