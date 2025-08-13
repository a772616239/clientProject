package server.handler.pet.mission;

import common.AbstractBaseHandler;
import common.GameConst.EventType;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.pet.dbCache.petCache;
import model.petmission.dbCache.petmissionCache;
import model.petmission.entity.petmissionEntity;
import model.reward.RewardManager;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.AcceptedPetMission;
import protocol.PetMessage.CS_PetMissionAllComplete;
import protocol.PetMessage.SC_PetMissionAllComplete;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import server.event.Event;
import server.event.EventManager;
import util.EventUtil;
import util.GameUtil;
import util.MapUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetMissionAllComplete_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetMissionAllComplete_VALUE;


/**
 * 一键完成委托任务
 */
@MsgId(msgId = CS_PetMissionAllComplete_VALUE)
public class OneClickPetMissionCompleteHandler extends AbstractBaseHandler<CS_PetMissionAllComplete> {

    @Override
    protected CS_PetMissionAllComplete parse(byte[] bytes) throws Exception {
        return CS_PetMissionAllComplete.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetMissionAllComplete req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petmissionEntity entity = petmissionCache.getInstance().getEntityByPlayerIdx(playerId);

        SC_PetMissionAllComplete.Builder resultBuilder = SC_PetMissionAllComplete.newBuilder();
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_PetMissionAllComplete_VALUE, resultBuilder);
            return;
        }

        List<AcceptedPetMission> canCompleteMissions = findCanCompleteMissions(entity);

        if (CollectionUtils.isEmpty(canCompleteMissions)) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_NoCanCompleteMission));
            gsChn.send(SC_PetMissionAllComplete_VALUE, resultBuilder);
            return;
        }
        SyncExecuteFunction.executeConsumer(entity, e -> {
            for (AcceptedPetMission mission : canCompleteMissions) {
                entity.removeAcceptMissionById(mission.getMissionId());
            }
        });

        List<Common.Reward> rewards = new ArrayList<>();

        canCompleteMissions.stream().map(AcceptedPetMission::getReward).forEach(rewards::add);
        RewardManager.getInstance().doRewardByList(playerId, rewards,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetMission), true);


        //重设宠物状态
        Event event = Event.valueOf(EventType.ET_ResetPetMissionStatus, GameUtil.getDefaultEventSource(),
                petCache.getInstance().getEntityByPlayer(playerId));
        event.pushParam(getMissionPetIds(canCompleteMissions));
        EventManager.getInstance().dispatchEvent(event);
        EventUtil.triggerUpdatePetMissionLvUpPro(playerId, getMissionPro(canCompleteMissions));

        for (AcceptedPetMission canCompleteMission : canCompleteMissions) {
            resultBuilder.addMissionIds(canCompleteMission.getMissionId());
        }
        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        gsChn.send(SC_PetMissionAllComplete_VALUE, resultBuilder);
    }

    private Map<Integer,Integer> getMissionPro(List<AcceptedPetMission> canCompleteMissions) {
        Map<Integer, Integer> proMap = new HashMap<>();
        for (AcceptedPetMission canCompleteMission : canCompleteMissions) {
            MapUtil.add2IntMapValue(proMap,canCompleteMission.getMissionLvl(),1);
        }
        return proMap;
    }

    private List<String> getMissionPetIds(List<AcceptedPetMission> canCompleteMissions) {
        List<String> petIds = new ArrayList<>();
        for (AcceptedPetMission canCompleteMission : canCompleteMissions) {
            petIds.addAll(canCompleteMission.getPetIdList());
        }
        return petIds;
    }

    private List<AcceptedPetMission> findCanCompleteMissions(petmissionEntity entity) {
        return entity.getAcceptedMissionListBuilder().getAcceptedMissionsMap()
                .values().stream().filter(mission -> mission.getTimeStamp() <= GlobalTick.getInstance().getCurrentTime())
                .collect(Collectors.toList());
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetDelegate;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetMissionAllComplete_VALUE, SC_PetMissionAllComplete.newBuilder().setResult(retCode));
    }
}
