package server.handler.pet.mission;

import cfg.GameConfig;
import common.AbstractBaseHandler;
import common.GameConst;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.mainLine.dbCache.mainlineCache;
import model.petmission.dbCache.petmissionCache;
import model.petmission.entity.petmissionEntity;
import platform.logs.ReasonManager;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetMissionAdd;
import protocol.PetMessage.PetMission;
import protocol.PetMessage.SC_PetMissionAdd;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.RandomUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetMissionAdd_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetMissionAdd_VALUE;

/**
 * 处理客户端添加委托请求
 *
 * @author xiao_FL
 * @date 2019/6/24
 */
@MsgId(msgId = CS_PetMissionAdd_VALUE)
public class PetMissionAddHandler extends AbstractBaseHandler<CS_PetMissionAdd> {

    @Override
    protected CS_PetMissionAdd parse(byte[] bytes) throws Exception {
        return CS_PetMissionAdd.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PetMissionAdd csPetMissionAdd, int i) {
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());

        SC_PetMissionAdd.Builder resultBuilder = SC_PetMissionAdd.newBuilder();

        petmissionEntity entity = petmissionCache.getInstance().getEntityByPlayerIdx(playerId);
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gameServerTcpChannel.send(SC_PetMissionAdd_VALUE, resultBuilder);
            return;
        }
        PetMission petMission = entity.randomOnePetMission(mainlineCache.getInstance().getCurOnHookNode(playerId), entity.getMissionListBuilder().getMissionLv());
        if (petMission == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gameServerTcpChannel.send(SC_PetMissionAdd_VALUE, resultBuilder);
            return;
        }


        Consume consume = ConsumeUtil.parseConsume(GameConfig.getById(GameConst.CONFIG_ID).getPetmissionaddconsume());
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetMission, "添加"))) {

            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_MatieralNotEnough));
            gameServerTcpChannel.send(SC_PetMissionAdd_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.addNewMission(petMission);
        });

        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setMission(petMission);
        gameServerTcpChannel.send(SC_PetMissionAdd_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetDelegate;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetMissionAdd_VALUE, SC_PetMissionAdd.newBuilder().setResult(retCode));
    }
}
