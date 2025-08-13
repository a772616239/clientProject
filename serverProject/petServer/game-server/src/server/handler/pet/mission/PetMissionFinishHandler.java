package server.handler.pet.mission;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.petmission.dbCache.petmissionCache;
import model.petmission.entity.petmissionEntity;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.AcceptedPetMission;
import protocol.PetMessage.CS_PetMissionFinish;
import protocol.PetMessage.SC_PetMissionFinish;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.TimeUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetMissionFinish_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetMissionFinish_VALUE;

/**
 * 处理客户端快速完成委托请求
 *
 * @author xiao_FL
 * @date 2019/6/25
 */
@MsgId(msgId = CS_PetMissionFinish_VALUE)
public class PetMissionFinishHandler extends AbstractBaseHandler<CS_PetMissionFinish> {

    @Override
    protected CS_PetMissionFinish parse(byte[] bytes) throws Exception {
        return CS_PetMissionFinish.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetMissionFinish req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petmissionEntity entity = petmissionCache.getInstance().getEntityByPlayerIdx(playerId);
        SC_PetMissionFinish.Builder resultBuilder = SC_PetMissionFinish.newBuilder();
        if (entity == null || entity.getAcceptMissionById(req.getMissionId()) == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_PetMissionFinish_VALUE, resultBuilder);
            return;
        }

        AcceptedPetMission acceptMission = entity.getAcceptMissionById(req.getMissionId());
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        // 消耗钻石
        int count = Math.toIntExact((acceptMission.getTimeStamp() - currentTime) % TimeUtil.MS_IN_A_HOUR == 0 ? 0 : 1
                + (acceptMission.getTimeStamp() - currentTime) / TimeUtil.MS_IN_A_HOUR);
        Consume consume = ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Diamond_VALUE, 0,
                GameConfig.getById(1).getPetmissionfinishconsume() * count);

        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetMission))) {

            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gsChn.send(SC_PetMissionFinish_VALUE, resultBuilder);
            return;
        }

        AcceptedPetMission build = acceptMission.toBuilder().setTimeStamp(currentTime).build();
        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.removeAcceptMissionById(req.getMissionId());
            entity.addAcceptMission(build);
        });

        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setAcceptedMission(build);
        gsChn.send(SC_PetMissionFinish_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetDelegate;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetMissionFinish_VALUE, SC_PetMissionFinish.newBuilder().setResult(retCode));
    }
}
