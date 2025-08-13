package server.handler.pet;

import cfg.*;
import com.google.protobuf.ProtocolStringList;
import common.AbstractBaseHandler;
import common.GameConst.Discharge;
import common.GameConst.PetUpType;
import common.GameConst.WarPetUpdate;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.pet.entity.petEntity;
import model.petgem.dbCache.petgemCache;
import model.petrune.dbCache.petruneCache;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.team.util.TeamsUtil;
import org.apache.commons.collections4.CollectionUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.PetStarUpLog;
import protocol.Common;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PetMessage.CS_PetRarityUp;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Pet.Builder;
import protocol.PetMessage.PetRarityUp;
import protocol.PetMessage.SC_PetRarityUp;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;

import static model.reward.RewardUtil.gemToReward;
import static model.reward.RewardUtil.runeToRuneReward;
import static protocol.MessageId.MsgIdEnum.CS_PetRarityUp_VALUE;
import static protocol.RetCodeId.RetCodeEnum.RCE_Pet_PetStatusChangeLock;

/**
 * 处理宠物品质升级相关业务
 */
@MsgId(msgId = CS_PetRarityUp_VALUE)
public class PetRarityUpHandler extends AbstractBaseHandler<CS_PetRarityUp> {
    /**
     * 升星消耗表配置类型：指定宠物
     */
    private static final int UP_STAR_SPECIFY = 100;

    /**
     * 升星消耗表配置类型：任意宠物
     */
    private static final int UP_STAR_RANDOM = 101;


    @Override
    protected CS_PetRarityUp parse(byte[] bytes) throws Exception {
        return CS_PetRarityUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetRarityUp req, int i) {
        String playerId = String.valueOf(gsChn.getPlayerId1());
        SC_PetRarityUp.Builder resultBuilder = PetMessage.SC_PetRarityUp.newBuilder();
        LogUtil.info("receive player:{} pet rarity up,req:{}", playerId, req);
        if (CollectionUtils.isEmpty(req.getUpListList())) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_ErrorParam));
            gsChn.send(MsgIdEnum.SC_PetRarityUp_VALUE, resultBuilder);
            return;
        }
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        if (entity == null) {
            resultBuilder.setRetCode(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(MsgIdEnum.SC_PetRarityUp_VALUE, resultBuilder);
            return;
        }
        RetCodeEnum codeEnum = RetCodeEnum.RCE_Success;

        List<Reward> realReturnRewards = new ArrayList<>();

        for (PetRarityUp petRarityUp : req.getUpListList()) {
            RetCodeEnum curCodeEnum = petRarityUp(playerId, entity, petRarityUp, resultBuilder, realReturnRewards);
            if (curCodeEnum != RetCodeEnum.RCE_Success) {
                codeEnum = curCodeEnum;
            }
        }
        if (CollectionUtils.isNotEmpty(realReturnRewards)) {
            RewardManager.getInstance().doRewardByList(playerId, RewardUtil.mergeReward(realReturnRewards),
                    ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Pet_RarityUp), false);
        }
        if (CollectionUtils.isNotEmpty(resultBuilder.getSourceReturnList())) {
            List<Reward> rewards = RewardUtil.mergeReward(resultBuilder.getSourceReturnList());
            resultBuilder.clearSourceReturn().addAllSourceReturn(rewards);
        }
        EventUtil.triggerCoupTeamUpdate(entity.getPlayeridx());
        resultBuilder.setRetCode(GameUtil.buildRetCode(codeEnum));
        gsChn.send(MsgIdEnum.SC_PetRarityUp_VALUE, resultBuilder);
        LogUtil.info(" player:{} pet rarity up,result:{}", playerId, codeEnum);
    }

    private RetCodeEnum petRarityUp(String playerId, petEntity entity, PetRarityUp petRarityUp
            , SC_PetRarityUp.Builder result, List<Reward> realReturnRewards) {
        ProtocolStringList materialPetsList = petRarityUp.getMaterialPetsList();
        Pet pet = petCache.getInstance().getPetById(playerId, petRarityUp.getPetId());
        if (pet == null) {
            return RetCodeEnum.RCE_Pet_PetNotExist;
        }
        if (pet.getPetRarity() >= petCache.getInstance().getPetMaxRarity(pet.getPetBookId())) {
            //返回错误码已经到最大品质
            return RetCodeEnum.RCE_Pet_RarityMaxLvLimit;
        }
        if (isPetTransfer(pet)) {
            return RCE_Pet_PetStatusChangeLock;
        }
        List<Common.Consume> itemsConsume = materialItemToConsumeItem(petRarityUp.getMaterialItemsList());


        //消耗
        PetUpConsumeObject consume = PetUpConsume.getByTypeAndLvl(PetUpType.Rarity, pet.getPetRarity(), pet.getPetBookId());
        if (consume == null) {
            LogUtil.error("cant`t find pet rarity up config by petBookId:{} rarity:{} ", pet.getPetBookId(), pet.getPetRarity());
            return RetCodeEnum.RCE_UnknownError;
        }
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Pet_RarityUp);
        RetCodeEnum checkCoupTeam = TeamsUtil.checkCoupTeamByPetRemove(playerId, materialPetsList);
        if (RetCodeEnum.RCE_Success != checkCoupTeam) {
            return checkCoupTeam;
        }
        if (CollectionUtils.isEmpty(materialPetsList) && CollectionUtils.isEmpty(itemsConsume)) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        List<Pet> consumePet = entity.distinctGetPetByIdList(materialPetsList);

        if (isPetsTransfer(consumePet)) {
            return RCE_Pet_PetStatusChangeLock;
        }

        if (!checkStarUpPetIsSatisfy(consume.getUpconsume(), consumePet, petRarityUp.getMaterialItemsList())) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        if (!CollectionUtils.isEmpty(itemsConsume) && !ConsumeManager.getInstance().consumeMaterialByList(playerId, itemsConsume,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Pet_RarityUp))) {
            return RetCodeEnum.RCE_MatieralNotEnough;
        }

        List<Reward> rewards = petCache.getInstance().calculateDisChargeReward(consumePet, Discharge.reset);
        realReturnRewards.addAll(rewards);
        result.addAllSourceReturn(rewards);
        List<PetMessage.Rune> runes = petruneCache.getInstance().getRuneListByPets(playerId, materialPetsList);
        result.addAllRuneReward(runeToRuneReward(runes));
        List<PetMessage.Gem> gems = petgemCache.getInstance().getGemListByPets(playerId, materialPetsList);
        result.addAllSourceReturn(gemToReward(gems));

        //重新计算属性
        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.removePets(materialPetsList, reason);
            Builder builder = pet.toBuilder();
            builder.setPetRarity(pet.getPetRarity() + 1);
            entity.refreshPetPropertyAndPut(builder, reason, true);
            // 埋点日志
            LogService.getInstance().submit(new PetStarUpLog(playerId, builder, pet.getPetRarity(), null, consumePet));
            EventUtil.triggerUpdateTargetProgress(entity.getPlayeridx(), TargetSystem.TargetTypeEnum.TTE_CumuUpPetStar, 1, 0);
            EventUtil.triggerUpdateTargetProgress(entity.getPlayeridx(), TargetSystem.TargetTypeEnum.TTE_Pet_CumuPetStarReach, 1, builder.getPetRarity());
            EventUtil.triggerUpdateTargetProgress(entity.getPlayeridx(), TargetSystem.TargetTypeEnum.TTE_Pet_SpecifyPetStarUpReach, builder.getPetRarity(), pet.getPetBookId());
            EventUtil.triggerUpdateTargetProgress(entity.getPlayeridx(), TargetSystem.TargetTypeEnum.TTE_CumuGainPet, 1, builder.getPetRarity());
        });
        //通知战戈宠物更新
        EventUtil.triggerWarPetUpdate(entity.getPlayeridx(), pet.getId(), WarPetUpdate.MODIFY);
        petCache.getInstance().statisticByPetRarityUp(playerId, pet.getId(), pet.getPetRarity(), pet.getPetRarity() + 1);
        EventUtil.triggerCollectPets(playerId, Collections.singletonList(entity.getPetById(pet.getId())));
        //返回成功
        return RetCodeEnum.RCE_Success;
    }

    private boolean isPetsTransfer(List<Pet> consumePet) {
        return CollectionUtils.isNotEmpty(consumePet) && consumePet.stream().anyMatch(this::isPetTransfer);
    }

    //宠物是否转换中
    private boolean isPetTransfer(Pet pet) {
        return pet.getPetChangeStatus() == 1;
    }


    private List<Common.Consume> materialItemToConsumeItem(List<Integer> materialItemsList) {
        if (CollectionUtils.isEmpty(materialItemsList)) {
            return Collections.emptyList();
        }
        List<Common.Consume> result = new ArrayList<>();
        for (Integer item : materialItemsList) {
            result.add(ConsumeUtil.parseConsume(Common.RewardTypeEnum.RTE_Item_VALUE, item, 1));
        }
        return result;
    }

    private boolean checkStarUpPetIsSatisfy(int[][] petConsumeCfg, List<Pet> material, List<Integer> materialItemsList) {
        if (petConsumeCfg == null) {
            return false;
        }
        int checkNum = getPetNumNeedByConfig(petConsumeCfg);
        if (checkNum != getMaterilSize(material,materialItemsList)) {
            return false;
        }
        // 和前端约定：检查材料按照配置中顺序
        int cfgIndex = 0;
        int checkIndex = 0;
        int petIndex = 0;
        int useItemIndex = 0;
        while (checkNum <= 0) {
            int[] upCfg = petConsumeCfg[cfgIndex];
            boolean upSpecial = upCfg[0] == UP_STAR_SPECIFY;
            int numNeed = upSpecial ? upCfg[3] : upCfg[2];
            for (int j = 0; j < numNeed; j++) {
                if (!checkCurPet(upCfg, material, petIndex, upSpecial)) {
                    if (!checkItemReplace(petConsumeCfg, checkIndex, upSpecial, materialItemsList, useItemIndex)) {
                        return false;
                    }
                    useItemIndex++;
                } else {
                    petIndex++;
                }
                checkNum--;
            }
            cfgIndex++;
        }
        return true;
    }

    private int getMaterilSize(List<Pet> material, List<Integer> materialItemsList) {
        int size = 0;
        size = material == null ? size : size + material.size();
        size = materialItemsList == null ? size : size + materialItemsList.size();
        return size;
    }

    private boolean checkCurPet(int[] upCfg, List<Pet> material, int petIndex, boolean upSpecial) {
        if (material == null || material.size() <= petIndex) {
            return false;
        }
        Pet pet = material.get(petIndex);
        // 指定宠物
        if (upSpecial) {
            // 第一个要求是宠物类型，第二个要求是宠物星级
            if (pet.getPetBookId() != upCfg[1] || pet.getPetRarity() < upCfg[2]) {
                return false;
            }
        }
        if (upCfg[0] == UP_STAR_RANDOM) {
            // 第一个要求是宠物稀有度，第二个要求是宠物星级，第三个要求是宠物类型（种族）/0则为全种族
            return pet.getPetRarity() >= upCfg[1] &&
                    (upCfg[3] == 0 || PetBaseProperties.getTypeById(pet.getPetBookId()) == upCfg[3]);
        }
        return true;
    }

    private boolean checkItemReplace(int[][] petConsumeCfg, int i, boolean upSpecial, List<Integer> materialItemsList, int useItemIndex) {
        // 指定宠物
        if (upSpecial) {
            return false;
        }
        if (materialItemsList.size() <= useItemIndex) {
            return false;
        }
        AdvancedSoulConfigObject cfg = AdvancedSoulConfig.getByItemid(materialItemsList.get(useItemIndex));
        if (cfg == null) {
            LogUtil.error("petRarity up checkItemReplace not find AdvancedSoulConfig by id :{}", materialItemsList.get(useItemIndex));
            return false;
        }
        if (petConsumeCfg[i][0] == UP_STAR_RANDOM) {
            // 第一个要求是宠物稀有度，第二个要求是宠物星级，第三个要求是宠物类型（种族）/0则为全种族
            return cfg.getRarity() >= petConsumeCfg[i][1] &&
                    (petConsumeCfg[i][3] == 0 || cfg.getPetclass() == 0 || cfg.getPetclass() == petConsumeCfg[i][3]);
        }
        return true;
    }

    private int getPetNumNeedByConfig(int[][] petConsumeCfg) {
        int petNumNeed = 0;
        for (int[] ints : petConsumeCfg) {
            if (ints.length < 4) {
                LogUtil.error("error petUpConsume config by petConsumeCfg:{}", petConsumeCfg);
                return Integer.MAX_VALUE;
            }
            petNumNeed += ints[0] == UP_STAR_SPECIFY ? ints[3] : ints[2];
        }
        return petNumNeed;
    }


    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.EF_PetRarityUp;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetRarityUp_VALUE, SC_PetRarityUp.newBuilder().setRetCode(retCode));
    }


}
