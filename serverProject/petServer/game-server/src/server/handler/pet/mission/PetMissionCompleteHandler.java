package server.handler.pet.mission;

import common.AbstractBaseHandler;
import common.GameConst.EventType;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.Collections;
import model.pet.dbCache.petCache;
import model.petmission.dbCache.petmissionCache;
import model.petmission.entity.petmissionEntity;
import model.reward.RewardManager;
import platform.logs.ReasonManager;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.AcceptedPetMission;
import protocol.PetMessage.CS_PetMissionComplete;
import protocol.PetMessage.SC_PetMissionComplete;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.EventUtil;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetMissionComplete_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetMissionComplete_VALUE;


/**
 * 处理客户端完成委托请求
 *
 * @author xiao_FL
 * @date 2019/6/25
 */
@MsgId(msgId = CS_PetMissionComplete_VALUE)
public class PetMissionCompleteHandler extends AbstractBaseHandler<CS_PetMissionComplete> {

    @Override
    protected CS_PetMissionComplete parse(byte[] bytes) throws Exception {
        return CS_PetMissionComplete.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetMissionComplete req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petmissionEntity entity = petmissionCache.getInstance().getEntityByPlayerIdx(playerId);

        SC_PetMissionComplete.Builder resultBuilder = SC_PetMissionComplete.newBuilder();
        if (entity == null || entity.getAcceptMissionById(req.getMissionId()) == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(SC_PetMissionComplete_VALUE, resultBuilder);
            return;
        }

        AcceptedPetMission acceptMission = entity.getAcceptMissionById(req.getMissionId());
        if (acceptMission.getTimeStamp() > GlobalTick.getInstance().getCurrentTime()) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_MissionNotComplete));
            gsChn.send(SC_PetMissionComplete_VALUE, resultBuilder);
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.removeAcceptMissionById(req.getMissionId());
        });


        RewardManager.getInstance().doReward(playerId, acceptMission.getReward(),
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetMission), true);


        //重设宠物状态
        Event event = Event.valueOf(EventType.ET_ResetPetMissionStatus, GameUtil.getDefaultEventSource(),
                petCache.getInstance().getEntityByPlayer(playerId));
        event.pushParam(acceptMission.getPetIdList());
        EventManager.getInstance().dispatchEvent(event);
        EventUtil.triggerUpdatePetMissionLvUpPro(playerId, Collections.singletonMap(acceptMission.getMissionLvl(), 1));


        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_PetMissionComplete_VALUE, resultBuilder);
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetDelegate;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetMissionComplete_VALUE, SC_PetMissionComplete.newBuilder().setResult(retCode));
    }
}
