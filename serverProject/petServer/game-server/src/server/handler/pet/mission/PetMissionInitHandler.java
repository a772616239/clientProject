package server.handler.pet.mission;

import cfg.FunctionOpenLvConfig;
import common.AbstractBaseHandler;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import model.petmission.dbCache.petmissionCache;
import model.petmission.entity.petmissionEntity;
import model.player.util.PlayerUtil;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetDB;
import protocol.PetMessage.CS_PetMissionInit;
import protocol.PetMessage.SC_PetMissionInit;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetMissionInit_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetMissionInit_VALUE;

/**
 * 处理客户端查询委托请求
 *
 * @author xiao_FL
 * @date 2019/6/17
 */
@MsgId(msgId = CS_PetMissionInit_VALUE)
public class PetMissionInitHandler extends AbstractBaseHandler<CS_PetMissionInit> {

    @Override
    protected CS_PetMissionInit parse(byte[] bytes) throws Exception {
        return CS_PetMissionInit.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gameServerTcpChannel, CS_PetMissionInit csPetMissionInit, int i) {
        String playerId = String.valueOf(gameServerTcpChannel.getPlayerId1());
        SC_PetMissionInit.Builder resultBuilder = SC_PetMissionInit.newBuilder();
        if (PlayerUtil.queryFunctionLock(playerId, EnumFunction.PetDelegate)) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_LvNotEnough));
            gameServerTcpChannel.send(SC_PetMissionInit_VALUE, resultBuilder);
            return;
        }
        petmissionEntity entity = petmissionCache.getInstance().getEntityByPlayerIdx(playerId);
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gameServerTcpChannel.send(SC_PetMissionInit_VALUE, resultBuilder);
            return;
        }

        PetDB.SerializablePetMission.Builder missionListBuilder = entity.getMissionListBuilder();
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setNextRefreshConsume(missionListBuilder.getNextRefreshConsume());
        resultBuilder.addAllMission(missionListBuilder.getMissionsMap().values());
        resultBuilder.addAllAcceptedMission(entity.getAcceptedMissionListBuilder().getAcceptedMissionsMap().values());
        resultBuilder.setMissionLv(missionListBuilder.getMissionLv());
        resultBuilder.addAllUpMissionStar(missionListBuilder.getUpLvProMap().keySet());
        resultBuilder.addAllUpMissionPro(missionListBuilder.getUpLvProMap().values());
        gameServerTcpChannel.send(SC_PetMissionInit_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetDelegate;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetMissionInit_VALUE, SC_PetMissionInit.newBuilder().setResult(retCode));
    }
}
