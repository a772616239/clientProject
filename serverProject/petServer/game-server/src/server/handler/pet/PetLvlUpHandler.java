package server.handler.pet;

import cfg.GameConfig;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetUpConsume;
import cfg.PetUpConsumeObject;
import common.AbstractBaseHandler;
import common.GameConst;
import common.GameConst.PetUpType;
import common.GameConst.WarPetUpdate;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.team.util.TeamsUtil;
import org.apache.commons.lang.ArrayUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.PetLvlUpLog;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetLvlUp;
import protocol.PetMessage.Pet;
import protocol.PetMessage.SC_PetLvlUp;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import static protocol.MessageId.MsgIdEnum.CS_PetLvlUp_VALUE;

/**
 * 处理宠物升级相关业务
 *
 * @author xiao_FL
 * @date 2019/5/16
 */
@MsgId(msgId = CS_PetLvlUp_VALUE)
public class PetLvlUpHandler extends AbstractBaseHandler<CS_PetLvlUp> {

    @Override
    protected CS_PetLvlUp parse(byte[] bytes) throws Exception {
        return CS_PetLvlUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetLvlUp req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_PetLvlUp.Builder resultBuilder = SC_PetLvlUp.newBuilder();
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        Pet pet;
        if (entity == null || (pet = entity.getPetById(req.getPetId())) == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetLvlUp_VALUE, resultBuilder);
            return;
        }
        PetBasePropertiesObject petCfg = PetBaseProperties.getByPetid(pet.getPetBookId());
        if (petCfg == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetLvlUp_VALUE, resultBuilder);
            return;
        }

        RetCodeEnum ret;
        if (req.getUpType() == UP_TYPE_LV_UP) {
            ret = petLvUp(entity, pet, req.getUpLevel());
        }/* else if (req.getUpType() == UP_TYPE_AWAKE_UP) {
            ret = petAwakeUp(entity, pet, petCfg);
        } */ else {
            ret = RetCodeEnum.RCE_ErrorParam;
        }

        resultBuilder.setUpType(req.getUpType());
        resultBuilder.setPetId(req.getPetId());
        resultBuilder.setPetUpResult(ret);
        //觉醒失败返回成功
        if (ret == RetCodeEnum.RCE_Pet_UpLvlFailure) {
            ret = RetCodeEnum.RCE_Success;
        }
        resultBuilder.setResult(GameUtil.buildRetCode(ret));
        gsChn.send(MsgIdEnum.SC_PetLvlUp_VALUE, resultBuilder);
    }


    /**
     * 升级类型
     */
    private static final int UP_TYPE_LV_UP = 0;


    private RetCodeEnum petLvUpBeforeCheck(playerEntity player, Pet pet, int upLevel, petEntity petEntity) {
        if (pet.getPetLvl() >= petCache.getInstance().getPexMaxLv(pet.getPetRarity())) {
            return RetCodeEnum.RCE_Pet_PetLvlMaxErroe;
        }

        if (upLevel <= 0) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        return coupTeamLimit(petEntity, pet.getId(), pet.getPetLvl() + upLevel);
    }

    private RetCodeEnum coupTeamLimit(petEntity petEntity, String petId, int afterUpPetLv) {
        Map<String, Integer> petLvMap = petEntity.getAllPetLvMap();
        List<Integer> sorLv = petLvMap.values().stream().sorted(Integer::compareTo).collect(Collectors.toList());
        int maxPetLv = sorLv.get(sorLv.size() - 1);
        if (afterUpPetLv <= maxPetLv) {
            return RetCodeEnum.RCE_Success;
        }
        petLvMap.put(petId, afterUpPetLv);
        int teamLv = TeamsUtil.queryCoupTeamLv(petEntity.getPlayeridx());
        int checkLv = GameConfig.getById(GameConst.CONFIG_ID).getCoupteamminlv();
        if (afterUpPetLv <= checkLv && teamLv <= checkLv) {
            return RetCodeEnum.RCE_Success;
        }
        int teamNeedNum = TeamsUtil.queryMainLineTeamMaxPetNum(petEntity.getPlayeridx());
        teamNeedNum = Math.min(teamNeedNum, sorLv.size());

        int minPetLv = sorLv.get(sorLv.size() - teamNeedNum);
        if (maxPetLv > minPetLv + GameConfig.getById(GameConst.CONFIG_ID).getCoupteampetlvdif()) {
            return RetCodeEnum.RCE_Pet_CoupTeamPetLvDifferGreatly;
        }
        return RetCodeEnum.RCE_Success;
    }

    /**
     * 升级
     *
     * @return
     */
    private RetCodeEnum petLvUp(petEntity entity, Pet pet, int upLevel) {
        playerEntity player = playerCache.getByIdx(entity.getPlayeridx());
        if (player == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        upLevel = getUpLevel(pet, upLevel);
        RetCodeEnum codeEnum = petLvUpBeforeCheck(player, pet, upLevel, entity);
        if (RetCodeEnum.RCE_Success != codeEnum) {
            return codeEnum;
        }
        int lvUpSuccessCount = 0;
        List<Consume> consumes = new ArrayList<>();
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetLvUp);
        int petLv = pet.getPetLvl();
        boolean corePet = PetBaseProperties.getByPetid(pet.getPetBookId()).getPetcore()
                ==GameConfig.getById(GameConst.CONFIG_ID).getBestpetcoretype();
        for (int i = 0; i < upLevel; i++) {
            PetUpConsumeObject level = PetUpConsume.getByTypeAndLvl(PetUpType.Level, petLv);
            if (level == null) {
                LogUtil.error("cant`t find pet level up config by level[" + pet.getPetLvl() + "]");
                codeEnum = RetCodeEnum.RCE_UnknownError;
                break;
            }
            List<Consume> consumeList = ConsumeUtil.parseToConsumeList(level.getUpconsume());

            if (!ArrayUtils.isEmpty(level.getExtraconsume()) && corePet) {
                consumes = ConsumeUtil.mergeConsumeByTypeAndId(consumes, ConsumeUtil.parseToConsumeList(level.getExtraconsume()));
            }
            consumes = ConsumeUtil.mergeConsumeByTypeAndId(consumes, consumeList);
            if (!ConsumeManager.getInstance().materialIsEnoughByList(entity.getPlayeridx(), consumes)) {
                EventUtil.triggerUpdateTargetProgress(player.getIdx(), TargetTypeEnum.TTE_PopMission_MaterialNotEnough,1,1);
                codeEnum = RetCodeEnum.RCE_MatieralNotEnough;
                break;
            }
            petLv++;
            lvUpSuccessCount++;
        }
        //升级消耗
        if (!ConsumeManager.getInstance().consumeMaterialByList(entity.getPlayeridx(), consumes, reason)) {
            EventUtil.triggerUpdateTargetProgress(player.getIdx(), TargetTypeEnum.TTE_PopMission_MaterialNotEnough,1,1);
            return RetCodeEnum.RCE_MatieralNotEnough;
        }
        petLv = pet.getPetLvl() + lvUpSuccessCount;
        if (lvUpSuccessCount > 0) {
            int finalPetLv = petLv;
            List<Consume> finalConsumes = consumes;
            Pet.Builder builder = pet.toBuilder();
            SyncExecuteFunction.executeConsumer(entity, e -> {
                builder.setPetLvl(finalPetLv);
                entity.refreshPetPropertyAndPut(builder, reason, false);
                entity.tryUpdatePetMaxLvHis(finalPetLv);
                //提升日志
                LogService.getInstance().submit(new PetLvlUpLog(entity.getPlayeridx(), pet.getPetLvl(), builder, finalConsumes));
            });
            //目标：累积完成x次宠物升级
            EventUtil.triggerUpdateTargetProgress(entity.getPlayeridx(), TargetTypeEnum.TTE_CumuLevelUpPet, lvUpSuccessCount, 0);
            //目标：累积x只宠物升到x级,由于存在一键升级需要遍历对每个等级抛出事件,以宠物的下一个等级开始
            for (int i = (pet.getPetLvl() + 1); i <= petLv; i++) {
                EventUtil.triggerUpdateTargetProgress(entity.getPlayeridx(), TargetTypeEnum.TTE_CumuPetLevelReach, 1, i);
            }
            //通知战戈宠物更新
            EventUtil.triggerWarPetUpdate(entity.getPlayeridx(), pet.getId(), WarPetUpdate.MODIFY);
            //更新魔晶宠物编队
            EventUtil.triggerCoupTeamUpdate(entity.getPlayeridx());
            //只有升级成功一次,给前端返回的就是成功
            codeEnum = RetCodeEnum.RCE_Success;
        }
        return codeEnum;
    }

    private int getUpLevel(Pet pet, int upLevel) {
        int maxLv = petCache.getInstance().getPexMaxLv(pet.getPetRarity());
        //升级不超过最大等级
        if (pet.getPetLvl() + upLevel >= maxLv) {
            upLevel = maxLv - pet.getPetLvl();
        }
     /*   if (pet.getPetLvl() + upLevel > playerLv) {
            upLevel = playerLv - pet.getPetLvl();
        }*/
        return upLevel;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetLvUp;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetLvlUp_VALUE, protocol.PetMessage.SC_PetLvlUp.newBuilder().setResult(retCode));

    }


}
