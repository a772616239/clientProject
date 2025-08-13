package server.handler.pet.mission;

import cfg.PetBaseProperties;
import cfg.PetFragmentConfig;
import cfg.PetFragmentConfigObject;
import common.AbstractBaseHandler;
import common.GameConst.EventType;
import common.SyncExecuteFunction;
import common.tick.GlobalTick;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.List;
import model.pet.dbCache.petCache;
import model.petmission.dbCache.petmissionCache;
import model.petmission.entity.petmissionEntity;
import org.springframework.util.CollectionUtils;
import platform.logs.LogService;
import platform.logs.entity.GamePlayLog;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PetMessage.AcceptedPetMission;
import protocol.PetMessage.CS_PetMissionAccept;
import protocol.PetMessage.Pet;
import protocol.PetMessage.PetMission;
import protocol.PetMessage.SC_PetMissionAccept;
import protocol.RetCodeId.RetCode;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import server.event.Event;
import server.event.EventManager;
import util.EventUtil;
import util.GameUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetMissionAccept_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetMissionAccept_VALUE;

/**
 * 处理客户端接收委托请求
 *
 * @author xiao_FL
 * @date 2019/6/24
 */
@MsgId(msgId = CS_PetMissionAccept_VALUE)
public class PetMissionAcceptHandler extends AbstractBaseHandler<CS_PetMissionAccept> {

    @Override
    protected CS_PetMissionAccept parse(byte[] bytes) throws Exception {
        return CS_PetMissionAccept.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetMissionAccept req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());

        SC_PetMissionAccept.Builder resultBuilder = SC_PetMissionAccept.newBuilder();
        petmissionEntity entity = petmissionCache.getInstance().getEntityByPlayerIdx(playerId);
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_PetMissionAccept_VALUE, resultBuilder);
            return;
        }

        PetMission mission = entity.getMissionById(req.getMissionId());

        List<Pet> petList = petCache.getInstance().getPetByIdList(playerId, req.getPetIdList());
        if (!missionLegal(mission, petList)) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Pet_MissionPetNotCorrect));
            gsChn.send(SC_PetMissionAccept_VALUE, resultBuilder);
            return;
        }

        AcceptedPetMission acceptedPetMission = acceptMission(mission, petList);
        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.removeMission(req.getMissionId());
            entity.addAcceptMission(acceptedPetMission);
            // 目标：累积完成x次x星委托
            EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_CumuFinishedPetEntrust, 1, acceptedPetMission.getMissionLvl());
        });

        resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
        resultBuilder.setMission(acceptedPetMission);
        gsChn.send(SC_PetMissionAccept_VALUE, resultBuilder);

        LogService.getInstance().submit(new GamePlayLog(playerId, EnumFunction.PetDelegate));

        //更新宠物状态
        Event event = Event.valueOf(EventType.ET_SetPetMissionStatus, GameUtil.getDefaultEventSource(),
                petCache.getInstance().getEntityByPlayer(playerId));
        event.pushParam(req.getPetIdList());
        EventManager.getInstance().dispatchEvent(event);
    }

    /**
     * 判断宠物委托是否合法
     *
     * @param mission 委托id
     * @param petList 宠物List，顺序为任务要求宠物品质，任务要求宠物职业，任务要求宠物等级
     * @return 判断结果
     */
    private boolean missionLegal(PetMission mission, List<Pet> petList) {
        if (mission == null || petList == null || petList.isEmpty()) {
            return false;
        }

        for (Pet pet : petList) {
            if (pet.getPetMissionStatus() == 1) {
                return false;
            }
        }
        // 标记位
        int i = 0;
        // 判断委托宠物稀有度
        if (mission.getRequiredPetRarity() != 0) {
            if (mission.getRequiredPetRarity() > petList.get(i).getPetRarity()) {
                return false;
            } else {
                i++;
            }
        }
        // 判断委托宠物职业
        if (mission.getRequiredPetType() != 0) {
            if (petList.size() <= i) {
                return false;
            }
            return mission.getRequiredPetType() == PetBaseProperties.getByPetid(petList.get(i).getPetBookId()).getPettype();
        }
        return true;

    }

    /**
     * 接受宠物委托
     *
     * @param mission 委托
     * @param petList 宠物list
     * @return 接受后的委托
     */
    private static AcceptedPetMission acceptMission(PetMission mission, List<Pet> petList) {
        AcceptedPetMission.Builder result = AcceptedPetMission.newBuilder();
        result.setMissionId(mission.getMissionId());
        result.setMissionDescription(mission.getMissionDescription());
        result.setMissionLvl(mission.getMissionLvl());
        result.setRequiredPetRarity(mission.getRequiredPetRarity());
        result.setRequiredPetType(mission.getRequiredPetType());
        result.setRequiredPetRarity(mission.getRequiredPetRarity());
        result.setTime(mission.getTime());
        for (Pet pet : petList) {
            result.addPetId(pet.getId());
        }
        result.setTimeStamp(GlobalTick.getInstance().getCurrentTime() + mission.getTime());
        result.setReward(parseMissionReward(mission, petList));
        return result.build();
    }

    private static protocol.Common.Reward parseMissionReward(PetMission mission, List<Pet> petList) {
        switch (mission.getMissionType()) {
            case PMT_Limit:
                return parseLimitMissionReward(mission, petList);
            default:
                return mission.getReward();
        }
    }

    private static Common.Reward parseLimitMissionReward(PetMission mission, List<Pet> petList) {
        if (CollectionUtils.isEmpty(petList)) {
            return null;
        }
        int missionLvl = mission.getMissionLvl();
        int petBookId = petList.get(0).getPetBookId();
        int rewardCount = cfg.PetMission.queryLimitMissionRewardCount(missionLvl);
        PetFragmentConfigObject fragment = PetFragmentConfig.getInstance().getPetDebrisRarityFragment(petBookId);
        if (fragment == null) {
            return null;
        }
        return Common.Reward.newBuilder().setRewardType(Common.RewardTypeEnum.RTE_PetFragment)
                .setId(fragment.getId()).setCount(rewardCount).build();
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetDelegate;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(MsgIdEnum.SC_PetMissionAccept_VALUE, SC_PetMissionAccept.newBuilder().setResult(retCode));
    }
}
