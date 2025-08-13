package server.handler.pet.mission;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.consume.ConsumeManager;
import model.mainLine.dbCache.mainlineCache;
import model.petmission.dbCache.petmissionCache;
import model.petmission.entity.PetMissionHelper;
import model.petmission.entity.petmissionEntity;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetDB;
import protocol.PetMessage.CS_PetMissionRefresh;
import protocol.PetMessage.PetMission;
import protocol.PetMessage.SC_PetMissionRefresh;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.RandomUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetMissionRefresh_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetMissionRefresh_VALUE;

/**
 * 处理客户端刷新委托请求, 刷新所有未接受的任务
 *
 * @author xiao_FL
 * @date 2019/6/21
 */
@MsgId(msgId = CS_PetMissionRefresh_VALUE)
public class PetMissionRefreshHandler extends AbstractBaseHandler<CS_PetMissionRefresh> {

    @Override
    protected CS_PetMissionRefresh parse(byte[] bytes) throws Exception {
        return CS_PetMissionRefresh.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetMissionRefresh req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petmissionEntity entity = petmissionCache.getInstance().getEntityByPlayerIdx(playerId);
        SC_PetMissionRefresh.Builder resultBuilder = SC_PetMissionRefresh.newBuilder();
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_PetMissionRefresh_VALUE, resultBuilder);
            return;
        }

        int needRefreshCount = entity.getMissionListBuilder().getMissionsCount();
        if (needRefreshCount <= 0) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(SC_PetMissionRefresh_VALUE, resultBuilder);
            return;
        }

        Consume consumeItem = entity.getMissionListBuilder().getNextRefreshConsume();

        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consumeItem,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetMission, "刷新"))) {

            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(SC_PetMissionRefresh_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            PetDB.SerializablePetMission.Builder missionListBuilder = entity.getMissionListBuilder();
            int curRefreshCount = missionListBuilder.getRefreshCount() + 1;
            Consume nextRefreshConsume = PetMissionHelper.calculateRefreshNeed(playerId, curRefreshCount + 1);
            List<PetMission> petMissions = entity.randomPetMission(mainlineCache.getInstance().getCurOnHookNode(playerId),
                    entity.getMissionListBuilder().getMissionLv(), needRefreshCount);

            missionListBuilder.clearMissions();
            entity.addNewMissionList(petMissions);
            missionListBuilder.setRefreshCount(curRefreshCount);
            missionListBuilder.setNextRefreshConsume(nextRefreshConsume);

            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            resultBuilder.addAllMission(petMissions);
            resultBuilder.setNextRefreshConsume(nextRefreshConsume);
        });
        gsChn.send(SC_PetMissionRefresh_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetDelegate;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetMissionRefresh_VALUE, SC_PetMissionRefresh.newBuilder().setResult(retCode));
    }
}
