package server.handler.pet;

import cfg.ItemPetAwakeExpConfig;
import cfg.ItemPetAwakeExpConfigObject;
import cfg.PetAwakenConfig;
import cfg.PetAwakenConfigObject;
import cfg.PetBaseProperties;
import common.AbstractBaseHandler;
import common.GameConst.WarPetUpdate;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import javafx.util.Pair;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.StringUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.PetAwakeUpLog;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.AwakeItem;
import protocol.PetMessage.CS_PetAwakeUp;
import protocol.PetMessage.Pet;
import protocol.PetMessage.PetAwake;
import protocol.PetMessage.PetAwake.Builder;
import protocol.PetMessage.PetProperty;
import protocol.PetMessage.SC_PetAwakeUp;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static protocol.RetCodeId.RetCodeEnum.RCE_LvNotEnough;
import static protocol.RetCodeId.RetCodeEnum.RCE_MatieralNotEnough;
import static protocol.RetCodeId.RetCodeEnum.RCE_Pet_NotMathCondition;
import static protocol.RetCodeId.RetCodeEnum.RSE_ConfigNotExist;

/**
 * @Description
 * @Author hanx
 * @Date2020/8/31 0031 10:30
 **/
@MsgId(msgId = MsgIdEnum.CS_PetAwakeUp_VALUE)
public class PetAwakeUpHandler extends AbstractBaseHandler<CS_PetAwakeUp> {

    @Override
    protected CS_PetAwakeUp parse(byte[] bytes) throws Exception {
        return CS_PetAwakeUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetAwakeUp req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        SC_PetAwakeUp.Builder resultBuilder = SC_PetAwakeUp.newBuilder();
        if (entity == null) {
            resultBuilder.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetAwakeUp_VALUE, resultBuilder);
            return;
        }
        int upType = req.getType();
        Pet pet = entity.getPetById(req.getPetId());
        int petOrientation = PetBaseProperties.getClass(pet.getPetBookId());
        RetCodeEnum codeEnum = beforeCheck(req, playerId, pet);
        if (RetCodeEnum.RCE_Success != codeEnum) {
            resultBuilder.setResult(GameUtil.buildRetCode(codeEnum));
            gsChn.send(MsgIdEnum.SC_PetAwakeUp_VALUE, resultBuilder);
            return;
        }


        List<AwakeItem> awakeMaterial = req.getMaterialList();

        Pair<RetCodeEnum, List<Consume>> parseConsume = parseConsume(upType, playerId, pet, awakeMaterial, petOrientation);
        if (RetCodeEnum.RCE_Success != parseConsume.getKey()) {
            resultBuilder.setResult(GameUtil.buildRetCode(codeEnum));
            gsChn.send(MsgIdEnum.SC_PetAwakeUp_VALUE, resultBuilder);
            return;
        }
        List<Consume> consumes = parseConsume.getValue();

        codeEnum = SyncExecuteFunction.executeFunction(entity, e -> {
            if (upType == PetProperty.AllMainProp_VALUE) {
                return doTotalLvUp(playerId, consumes, pet, petOrientation, entity);
            }
            return doItemAwakeUp(playerId, entity, upType, pet, petOrientation, awakeMaterial, consumes);
        });
        resultBuilder.setResult(GameUtil.buildRetCode(codeEnum));
        gsChn.send(MsgIdEnum.SC_PetAwakeUp_VALUE, resultBuilder);

        if (RetCodeEnum.RCE_Success != codeEnum) {
            return;
        }
        //目标系统埋点
        triggerTarget(req, playerId, entity, upType, pet);
        //通知战戈宠物更新
        EventUtil.triggerWarPetUpdate(entity.getPlayeridx(), pet.getId(), WarPetUpdate.MODIFY);
    }

    private RetCodeEnum doTotalLvUp(String playerId, List<Consume> consumes, Pet pet, int petOrientation, petEntity entity) {
        int newUpLv = pet.getPetUpLvl() + 1;
        int upType = PetProperty.AllMainProp_VALUE;

        PetAwakenConfigObject config = PetAwakenConfig.getByTypeAndLv(upType, newUpLv, petOrientation);
        if (config == null) {
            return RSE_ConfigNotExist;
        }
        if (config.getPetlvl() > 0 && config.getPetlvl() > pet.getPetLvl()) {
            return RCE_LvNotEnough;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetAwake);
        if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, consumes, reason)) {
            return RCE_MatieralNotEnough;
        }
        Pet.Builder petBuilder = pet.toBuilder().setPetUpLvl(newUpLv);

        entity.refreshPetPropertyAndPut(petBuilder, reason, false);
        // 埋点日志
        LogService.getInstance().submit(new PetAwakeUpLog(playerId, pet, upType, pet.getPetUpLvl(), petBuilder.getPetUpLvl(),
                newUpLv - 1, newUpLv, consumes, petBuilder.getAbility() - pet.getAbility()));
        //目标
        doTarget(playerId, petBuilder, null, upType);
        return RetCodeEnum.RCE_Success;
    }

    private RetCodeEnum doItemAwakeUp(String playerId, petEntity entity, int upType, Pet pet, int petOrientation, List<AwakeItem> awakeMaterial, List<Consume> consumes) {
        List<PetAwake> awakeList = pet.getAwakeList();
        Optional<PetAwake> awakeOptional = awakeList.stream().filter(awake -> upType == awake.getType()).findFirst();
        Builder awake = awakeOptional.isPresent() ? awakeOptional.get().toBuilder() : PetAwake.newBuilder().setType(upType);

        PetAwakenConfigObject config = PetAwakenConfig.getByTypeAndLv(upType, awake.getLevel(), petOrientation);
        if (config == null) {
            return RSE_ConfigNotExist;
        }
        if (config.getPetlvl() > pet.getPetLvl()) {
            return RCE_LvNotEnough;
        }
        if (awake.getLevel() >= pet.getPetUpLvl() + 1) {
            return RCE_Pet_NotMathCondition;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetAwake);
        if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, consumes, reason)) {
            return RCE_MatieralNotEnough;
        }
        int addExp = materialToExp(awakeMaterial);
        int propertyAddition = materialToProperty(awakeMaterial);
        int originalLv = awake.getLevel();
        awake.setCurExp(addExp + awake.getCurExp());
        awake.setPropertyAddition(awake.getPropertyAddition() + propertyAddition);
        Pet.Builder petBuilder = pet.toBuilder();
        refreshPetAwakeItem(awake, upType, petOrientation, pet.getPetLvl(), pet.getPetUpLvl());
        refreshPetAwake(petBuilder, awake);


        entity.refreshPetPropertyAndPut(petBuilder, reason, false);
        // 埋点日志
        LogService.getInstance().submit(new PetAwakeUpLog(playerId, pet, upType, pet.getPetUpLvl(), petBuilder.getPetUpLvl(),
                originalLv, awake.getLevel(), consumes, petBuilder.getAbility() - pet.getAbility()));
        //目标
        doTarget(playerId, petBuilder, awake, upType);
        return RetCodeEnum.RCE_Success;
    }

    private void triggerTarget(CS_PetAwakeUp req, String playerId, petEntity entity, int upType, Pet pet) {
        Pet newPet = entity.getPetById(req.getPetId());

        //目标：累积完成x次x品质宠物觉醒
        if (upType == PetProperty.AllMainProp_VALUE) {
            //目标：累积将x只宠物觉醒到x级
            EventUtil.triggerUpdateTargetProgress(entity.getPlayeridx(), TargetTypeEnum.TTE_CumuPetAwakeRech, 1, newPet.getPetUpLvl());
            EventUtil.triggerUpdateTargetProgress(entity.getPlayeridx(), TargetTypeEnum.TTE_Pet_SpecifyPetAwakeUpReach, newPet.getPetUpLvl(), newPet.getPetBookId());

            petCache.getInstance().statisticByPeAwakeUp(playerId, pet.getId(), pet.getPetUpLvl(), newPet.getPetUpLvl());
        }
        EventUtil.triggerUpdateTargetProgress(entity.getPlayeridx(), TargetTypeEnum.TTE_CumuAwakePet, 1, newPet.getPetRarity());
    }

    private Pair<RetCodeEnum, List<Consume>> parseConsume(int upType, String playerId, Pet pet, List<AwakeItem> awakeMaterial, int petOrientation) {
        List<Consume> consumes;
        if (upType == PetProperty.AllMainProp_VALUE) {
            PetAwakenConfigObject cfg = PetAwakenConfig.getByTypeAndLv(upType, pet.getPetUpLvl(), petOrientation);
            if (cfg == null) {
                return new Pair(RetCodeEnum.RCE_ErrorParam, null);
            }
            int[][] upConsume = cfg.getUpconsume();
            if (ArrayUtils.isEmpty(upConsume)) {
                return new Pair(RetCodeEnum.RCE_ErrorParam, null);
            }
            consumes = ConsumeUtil.parseToConsumeList(cfg.getUpconsume());
        } else {
            consumes = toConsumeList(awakeMaterial);
        }
        if (CollectionUtils.isEmpty(consumes)) {
            return new Pair(RetCodeEnum.RCE_ErrorParam, consumes);
        }
        if (!ConsumeManager.getInstance().materialIsEnoughByList(playerId, consumes)) {
            return new Pair(RCE_MatieralNotEnough, consumes);
        }
        return new Pair(RetCodeEnum.RCE_Success, consumes);
    }

    private RetCodeEnum beforeCheck(CS_PetAwakeUp req, String playerId, Pet pet) {
        if (pet == null) {
            return RetCodeEnum.RCE_Pet_PetNotExist;
        }
        if (req.getType() == PetProperty.AllMainProp_VALUE) {
            int minAwakeLv = pet.getAwakeList().stream().map(PetAwake::getLevel).min(Integer::compareTo).orElse(0);
            if (minAwakeLv <= pet.getPetUpLvl()) {
                return RCE_Pet_NotMathCondition;
            }
            return RetCodeEnum.RCE_Success;
        }
        itembagEntity item = itembagCache.getInstance().getItemBagByPlayerIdx(playerId);
        if (item == null) {
            return RetCodeEnum.RCE_UnknownError;
        }
        for (AwakeItem awakeItem : req.getMaterialList()) {
            ItemPetAwakeExpConfigObject config = ItemPetAwakeExpConfig.getByItemid(awakeItem.getItemId());
            if (config == null || config.getPropertytype() != req.getType()) {
                return RetCodeEnum.RCE_ErrorParam;
            }
        }
        return RetCodeEnum.RCE_Success;
    }

    /**
     * 目标
     */
    private void doTarget(String playerIdx, Pet.Builder petBuilder, Builder awake, int upType) {
        if (StringUtils.isEmpty(playerIdx) || petBuilder == null || (upType != PetProperty.AllMainProp_VALUE && awake == null)) {
            return;
        }
        if (upType == PetProperty.ATTACK_VALUE) {
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TEE_PetAwake_AttackReach, 1, awake.getLevel());
        } else if (upType == PetProperty.DEFENSIVE_VALUE) {
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TEE_PetAwake_DefenseReach, 1, awake.getLevel());
        } else if (upType == PetProperty.HEALTH_VALUE) {
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TEE_PetAwake_HpReach, 1, awake.getLevel());
        } else if (upType == PetProperty.AllMainProp_VALUE) {
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TEE_PetAwake_TotalReach, 1, petBuilder.getPetUpLvl());
        }

    }

    private void refreshPetAwake(Pet.Builder petBuilder, Builder awake) {
        if (petBuilder.getAwakeCount() <= 0) {
            petBuilder.addAwake(awake);
        }
        List<Builder> awakeList = petBuilder.getAwakeBuilderList();
        if (awakeList.stream().anyMatch(e -> e.getType() == awake.getType())) {
            awakeList.stream().filter(e -> e.getType() == awake.getType()).findFirst()
                    .ifPresent(awakeInDb -> awakeInDb.setPropertyAddition(awake.getPropertyAddition()).setCurExp(awake.getCurExp()).setLevel(awake.getLevel()));
            return;
        }
        petBuilder.addAwake(awake);
    }

    private int materialToProperty(List<AwakeItem> materialList) {
        return materialList.stream().mapToInt(this::itemToProperty).sum();
    }

    private int itemToProperty(AwakeItem e) {
        ItemPetAwakeExpConfigObject config = ItemPetAwakeExpConfig.getByItemid(e.getItemId());
        if (config == null || config.getProperty().length < 1) {
            return 0;
        }
        return config.getProperty()[1] * e.getItemNum();
    }

    private void refreshPetAwakeItem(Builder awake, int awakeType, int petOrientation, int petLvl, int totalUpLv) {
        int curExp = awake.getCurExp();
        int curLv = awake.getLevel();
        while (true) {
            PetAwakenConfigObject config = PetAwakenConfig.getByTypeAndLv(awakeType, curLv, petOrientation);
            if (config == null) {
                LogUtil.error("can`t find awakeConfig by awakeLv:{} ,orientation:{}, awakeType:{} ", awake.getLevel(), petOrientation, awakeType);
                break;
            }
            if (config.getPetlvl() > petLvl) {
                break;
            }
            if (curLv >= totalUpLv + 1) {
                break;
            }
            //到达最大等级
            if (config.getNeedexp() <= 0) {
                awake.setCurExp(curExp);
                awake.setLevel(curLv);
                if (config.getProperties().length > 0 && config.getProperties()[0].length > 1) {
                    awake.setPropertyAddition(config.getProperties()[0][1]);
                }
                return;
            }
            int needExp = config.getNeedexp();
            if (needExp > curExp) {
                break;
            }
            curLv++;
            curExp -= needExp;
        }
        awake.setCurExp(curExp);
        awake.setLevel(curLv);
    }

    private int materialToExp(List<AwakeItem> materialList) {
        return materialList.stream().mapToInt(this::itemToExp).sum();
    }

    private int itemToExp(AwakeItem e) {
        ItemPetAwakeExpConfigObject config = ItemPetAwakeExpConfig.getByItemid(e.getItemId());
        if (config == null) {
            return 0;
        }
        return config.getExp() * e.getItemNum();
    }

    private List<Consume> toConsumeList(List<AwakeItem> materialList) {
        if (CollectionUtils.isEmpty(materialList)) {
            return Collections.emptyList();
        }
        List<Consume> result = new ArrayList<>();
        for (AwakeItem awakeItem : materialList) {
            result.add(ConsumeUtil.parseConsume(RewardTypeEnum.RTE_Item_VALUE, awakeItem.getItemId(), awakeItem.getItemNum()));
        }
        return result;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.ET_PetAwakeUp;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetAwakeUp_VALUE, protocol.PetMessage.SC_PetAwakeUp.newBuilder().setResult(retCode));
    }


}
