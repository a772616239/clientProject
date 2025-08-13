/*CREATED BY TOOL*/

package model.pet.dbCache;

import annotation.annationInit;
import cfg.*;
import common.GameConst;
import common.GameConst.Discharge;
import common.GameConst.WarPetUpdate;
import common.GlobalData;
import common.IdGenerator;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.util.Pair;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.bravechallenge.dbCache.bravechallengeCache;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.foreignInvasion.dbCache.foreigninvasionCache;
import model.inscription.dbCache.petinscriptionCache;
import model.inscription.petinscriptionEntity;
import model.patrol.dbCache.service.PatrolServiceImpl;
import model.pet.HelpPetManager;
import model.pet.PetManager;
import model.pet.cache.petUpdateCache;
import model.pet.entity.FightPowerCalculate;
import model.pet.entity.GemAdditionDto;
import model.pet.entity.PetComposeHelper;
import model.pet.entity.ResonancePet;
import model.pet.entity.petEntity;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import model.petrune.dbCache.petruneCache;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.player.util.PlayerUtil;
import model.ranking.RankingManager;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.team.dbCache.teamCache;
import model.team.entity.Team;
import model.team.entity.teamEntity;
import model.team.util.TeamsUtil;
import model.training.TrainingManager;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.DailyDateLog;
import platform.logs.entity.PetDischargeLog;
import platform.logs.statistics.GemStatistics;
import platform.logs.statistics.PetStatistics;
import protocol.Activity;
import protocol.Battle.BattlePetData;
import protocol.Battle.BattleSubTypeEnum;
import protocol.Battle.PetPropertyDict;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage;
import protocol.PetMessage.Gem;
import protocol.PetMessage.Pet;
import protocol.PetMessage.Pet.Builder;
import protocol.PetMessage.PetAwake;
import protocol.PetMessage.PetDisplayInfo;
import protocol.PetMessage.PetProperties;
import protocol.PetMessage.PetProperty;
import protocol.PetMessage.PetPropertyEntity;
import protocol.PetMessage.Rune;
import protocol.PetMessage.RunePropertieyEntity;
import protocol.PetMessage.SC_AddPets;
import protocol.PetMessage.SC_PetAbilityUpdate;
import protocol.PlayerInfo.SC_RefreshTotalAbility;
import protocol.PrepareWar.TeamNumEnum;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.ArrayUtil;
import util.CollectionUtil;
import util.EventUtil;
import util.GameUtil;
import util.LogUtil;
import util.MapUtil;

import static common.GameConst.emptyGemAdditionDto;
import static model.reward.RewardUtil.gemToReward;
import static model.reward.RewardUtil.runeToRuneReward;
import static protocol.PetMessage.PetProperty.ATTACK_VALUE;
import static protocol.PetMessage.PetProperty.DEFENSIVE_VALUE;
import static protocol.PetMessage.PetProperty.ExtendAttackRate_VALUE;
import static protocol.PetMessage.PetProperty.ExtendDefenceRate_VALUE;
import static protocol.PetMessage.PetProperty.ExtendHealthRate_VALUE;
import static protocol.PetMessage.PetProperty.HEALTH_VALUE;
import static util.MapUtil.add2IntMapValue;
import static util.MapUtil.mergeIntMaps;

@annationInit(value = "petCache", methodname = "load")
public class petCache extends baseCache<petCache> implements IbaseCache {
    /******************* MUST HAVE ********************************/


    /**
     * 放生宠物需要计算宠物资源返还的功能(不包括觉醒)
     */
    private static final List<Integer> dischargeRewardsType = Arrays.asList(
            GameConst.PetUpType.Level, GameConst.PetUpType.Rarity, GameConst.PetUpType.Evolve);

    private static petCache instance = null;

    public static petCache getInstance() {
        if (instance == null) {
            instance = new petCache();
        }
        return instance;
    }

    @Override
    public String getDaoName() {
        return "petDAO";
    }

    @Override
    public BaseDAO getDao() {
        return AppContext.getBean("petDAO");
    }

    @Override
    public void load(baseCache o) {
        if (instance == null) {
            instance = (petCache) o;
        }
        super.loadAllFromDb();
        for (BaseEntity entity : _ix_id.values()) {
            ((petEntity) entity).init();
        }
    }

    public static void put(petEntity v) {
        getInstance().putBaseEntity(v);

    }

    public static petEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx)) {
            return null;
        }
        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null) {
            return null;
        }
        return (petEntity) v;
    }

    @Override
    public Map<String, BaseEntity> getAll() {
        return getInstance()._ix_id;
    }

    @Override
    public baseUpdateCache getUpdateCache() {
        return petUpdateCache.getInstance();
    }

    public static void remove(String idx) {
        getInstance().BaseRemove(idx);
    }

    @Override
    public void putToMem(BaseEntity v) {
        petEntity entity = (petEntity) v;
        if (null != entity) {
            this.entityMap.put(entity.getPlayeridx(), entity);
        }
    }

    /***************************分割**********************************/

    private final Map<String, petEntity> entityMap = new ConcurrentHashMap<>();

    public petEntity getEntityByPlayer(String playerIdx) {
        if (StringHelper.isNull(playerIdx)) {
            return null;
        }

        petEntity entity = entityMap.get(playerIdx);
        if (entity == null && PlayerUtil.playerIsExist(playerIdx)) {
            entity = new petEntity(playerIdx);
        }

        return entity;
    }

    public Pet getPetById(String playerIdx, String petId) {
        petEntity entity = getEntityByPlayer(playerIdx);
        if (null != entity) {
            return entity.getPetById(petId);
        }
        return null;
    }

    public List<Pet> getPetByIdList(String playerId, Collection<String> idList) {
        petEntity entity = getEntityByPlayer(playerId);
        if (entity == null || GameUtil.collectionIsEmpty(idList)) {
            return Collections.emptyList();
        }
        return entity.distinctGetPetByIdList(idList);
    }

    /**
     * 计算宠物属性 更新 并发送到客户端
     *
     * @param playerId
     * @param petId
     * @param reason
     */
    public void refreshPetProperty(String playerId, String petId, Reason reason, boolean showAbilityChange) {
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null) {
            return;
        }
        SyncExecuteFunction.executeConsumer(cache, cacheTemp ->
                cacheTemp.refreshPetPropertyAndPut(cache.getPetById(petId), reason, showAbilityChange));
    }

    public void updatePetMissionStatus(String playerIdx, List<String> petIdx, boolean inOrOut) {
        petEntity entity = getEntityByPlayer(playerIdx);
        if (GameUtil.collectionIsEmpty(petIdx) || entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            entity.updatePetMissionStatus(petIdx, inOrOut);
        });
    }

    public boolean capacityEnough(String playerId, int occupancy) {
        return getRemainCapacity(playerId) >= occupancy;
    }

    public int getRemainCapacity(String playerId) {
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        if (entity != null) {
            return Math.max(0, entity.getCapacity() - entity.getOccupancy());
        }
        return 0;
    }

    /**
     * 移除宠物 并返回移除成功的宠物数据
     *
     * @param playerIdx
     * @param petIdList
     * @param reason
     * @return 返回移除成功的玩家, null 移除失败,
     */
    public List<Pet> removeByPetIdList(String playerIdx, List<String> petIdList, Reason reason) {
        petEntity entity = getEntityByPlayer(playerIdx);
        if (GameUtil.collectionIsEmpty(petIdList) || entity == null) {
            return null;
        }

        return SyncExecuteFunction.executeFunction(entity, e -> entity.removePets(petIdList, reason));
    }

    /**
     * vip 背包扩容
     *
     * @param playerId
     * @param vipLevel
     * @return
     */
    public boolean vipBagEnlarge(String playerId, int vipLevel) {
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null) {
            return false;
        }

        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            VIPConfigObject vipCfg = VIPConfig.getById(vipLevel);
            if (vipCfg == null) {
                LogUtil.error("model.pet.dbCache.service.PetServiceImpl.bagEnlarge, VIPConfigObject is not exist, vipLv = " + vipLevel);
                return;
            }
            cacheTemp.setCapacity(cacheTemp.getCapacity() + vipCfg.getPetbagimprove());
            cache.sendPetBagRefresh();
        });
        return true;
    }

    /**
     * 恢复pet,仅适用于consume消耗失败时,返回宠物时调用
     *
     * @param playerIdx
     * @param pets
     */
    public void restorePet(String playerIdx, List<Pet> pets) {
        if (GameUtil.collectionIsEmpty(pets)) {
            return;
        }

        petEntity petByPlayer = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (petByPlayer == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(petByPlayer, pet -> {
            petByPlayer.putAllPet(pets);
        });
    }


    /**
     * 0是放生宠物，1是重生宠物
     *
     * @param playerId
     * @param petIdList
     * @param discharge
     * @param result
     * @param reason
     * @return
     */
    public RetCodeEnum dischargePet(String playerId, List<String> petIdList, int discharge, PetMessage.SC_PetDisCharge.Builder result, Reason reason) {

        petEntity cache = petCache.getInstance().getEntityByPlayer(playerId);

        List<Pet> disChargePetList = new ArrayList<>();

        RetCodeEnum checkRet = checkPetDischarge(petIdList, cache, disChargePetList, discharge, reason);

        if (RetCodeEnum.RCE_Success != checkRet) {
            result.setResult(GameUtil.buildRetCode(checkRet));
            return checkRet;
        }
        reason = reason != null ? reason : ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetDischarge);
        //放生奖励
        List<Reward> rewardList = calculateDisChargeReward(disChargePetList, discharge);

        Common.SC_DisplayMultiRewards.Builder showRewardsMsg = buildDischargePetReward(playerId, petIdList, rewardList, reason);
        if (Discharge.free == discharge) {
            List<Pet> removeByPetIdList = petCache.getInstance().removeByPetIdList(playerId, petIdList, reason);

            if (GameUtil.collectionIsEmpty(removeByPetIdList)) {
                return RetCodeEnum.RCE_Failure;
            }

        } else {
            RetCodeEnum retCodeEnum = doPetReborn(petIdList, playerId, cache, discharge);
            if (retCodeEnum != RetCodeEnum.RCE_Success) {
                return retCodeEnum;
            }
        }

        petCache.getInstance().refreshAndSendTotalAbility(playerId);

        RewardManager.getInstance().doRewardByList(playerId, rewardList, reason, false);

        sendAutoFreeReward(playerId, reason, showRewardsMsg);


        if (discharge == Discharge.free) {
            petEntity.sendPetRemove(playerId, petIdList, cache);
            //目标：累积放生宠物x次
            EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_PetDischarge, petIdList.size(), 0);
        } else {
            // 重生功能缺日志

            //目标：累积重订契约x次
            EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_PetRestore, petIdList.size(), 0);
        }
        EventUtil.triggerCoupTeamUpdate(playerId);
        EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_Pet_DischargeOrRestore, petIdList.size(), 0);
        LogUtil.info("player:{} discharge pet:{}", playerId, disChargePetList);
        // 埋点日志
        LogService.getInstance().submit(new PetDischargeLog(playerId, disChargePetList, rewardList, discharge));
        checkExpirePetLink(cache, disChargePetList, reason);
        result.addAllRewardList(showRewardsMsg.getRewardListList());
        return RetCodeEnum.RCE_Success;
    }

    private void sendAutoFreeReward(String playerId, Reason reason, Common.SC_DisplayMultiRewards.Builder showRewardsMsg) {
        if (RewardSourceEnum.RSE_Pet_AutoFree == reason.getSourceEnum()) {
            GlobalData.getInstance().sendMsg(playerId, MsgIdEnum.SC_DisplayMultiRewards_VALUE, showRewardsMsg);
        }
    }

    private void checkExpirePetLink(petEntity petEntity, List<Pet> disChargePetList, Reason reason) {
        Collection<Pet> pets = petEntity.peekAllPetByUnModify();
        List<Integer> expireLink = new ArrayList<>();
        disChargePetList.stream().filter(pet -> pet.getActiveLinkCount() > 0 && noEvolvePet(pets, pet.getPetBookId()))
                .forEach(pet -> {
                    expireLink.addAll(pet.getActiveLinkList());
                });

        if (CollectionUtils.isEmpty(expireLink)) {
            return;
        }
        Map<String, Pet.Builder> modifyPets = new HashMap<>();
        for (Integer linkId : expireLink) {
            LinkConfigObject cfg = LinkConfig.getById(linkId);
            if (cfg == null) {
                continue;
            }
            pets.stream().filter(pet -> pet.getActiveLinkList().contains(linkId)).forEach(
                    pet -> {
                        Builder builder1 = modifyPets.get(pet.getId());
                        List<Integer> activeLinkList = pet.getActiveLinkList();
                        Builder builder = builder1 != null ? builder1 : pet.toBuilder().clearActiveLink();
                        activeLinkList.stream().filter(e -> !e.equals(linkId)).forEach(builder::addActiveLink);
                        int petIndex = LinkConfig.findPetIndex(cfg, pet.getPetBookId());
                        if (petIndex != -1) {
                            List<Integer> buffIdsList = pet.getBuffIdsList();
                            buffIdsList.stream().filter(e -> !e.equals(cfg.getBufflist()[petIndex])).forEach(builder::addBuffIds);
                        }
                        modifyPets.put(builder.getId(), builder);

                    }
            );
        }
        if (MapUtils.isNotEmpty(modifyPets)) {
            petEntity.refreshAllPetPropertyAndPut(modifyPets.values(), reason, false, false, false);
        }


    }

    private boolean noEvolvePet(Collection<Pet> pets, int petBookId) {
        return pets.stream().noneMatch(pet -> pet.getPetBookId() == petBookId && pet.getEvolveLv() > 0);

    }

    private Common.SC_DisplayMultiRewards.Builder buildDischargePetReward(String playerId, List<String> petIdList, List<Reward> rewardList, Reason reason) {
        Common.SC_DisplayMultiRewards.Builder showRewardsMsg = Common.SC_DisplayMultiRewards.newBuilder();
        showRewardsMsg.addAllRewardList(rewardList);
        combineRuneAndGemRewardInPets(showRewardsMsg, playerId, petIdList);
        showRewardsMsg.setRewardSource(reason.getSourceEnum());
        return showRewardsMsg;

    }

    private RetCodeEnum checkPetDischarge(List<String> petIdList, petEntity cache, List<Pet> queryResult, int discharge, Reason reason) {
        if (GameUtil.collectionIsEmpty(petIdList) || cache == null) {
            return RetCodeEnum.RCE_Pet_PetNotExist;
        }

        return SyncExecuteFunction.executeFunction(cache, c -> {
            List<Pet> pets = cache.distinctGetPetByIdList(petIdList);
            if (GameUtil.collectionIsEmpty(pets) || pets.size() != petIdList.size()) {
                return RetCodeEnum.RCE_ErrorParam;
            }
            queryResult.addAll(cache.distinctGetPetByIdList(petIdList));

            //检查宠物状态
            for (Pet pet : queryResult) {
                RetCodeEnum retCodeEnum = petStatusCheck(pet);
                if (retCodeEnum != RetCodeEnum.RCE_Success) {
                    return retCodeEnum;
                }
            }
            if (reason != null && reason.getSourceEnum() == RewardSourceEnum.RSE_Pet_AutoFree) {
                return RetCodeEnum.RCE_Success;
            }
            return checkCoupTeamStatus(petIdList, discharge, cache);
        });
    }

    /**
     * 放生重订后检查魔晶编队状态
     *
     * @param petIdList
     * @param discharge
     * @param cache
     * @return
     */
    private RetCodeEnum checkCoupTeamStatus(List<String> petIdList, int discharge, petEntity cache) {
        if (discharge == Discharge.free) {
            return TeamsUtil.checkCoupTeamByPetRemove(cache.getPlayeridx(), petIdList);
        }
        return RetCodeEnum.RCE_Success;
    }


    public RetCodeEnum doPetReborn(List<String> petIdList, String playerId, petEntity petEntity, int discharge) {

        List<Pet> pets = petEntity.distinctGetPetByIdList(petIdList);

        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetReBorn);
        RetCodeEnum consume = consumePetReborn(playerId, pets, reason);
        if (RetCodeEnum.RCE_Success != consume) {
            return consume;
        }

        SyncExecuteFunction.executeConsumer(petEntity, c -> {
            EventUtil.resetRuneStatus(playerId, petIdList);
            List<String> gemIds = new ArrayList<>();
            List<Pet.Builder> after = new ArrayList<>();
            pets.forEach(pet -> {
                int rarity = getPetDischargeRarity(discharge, pet);
                after.add(getPetBuilder(pet.getPetBookId(), pet.getSource()).setPetRarity(rarity).setId(pet.getId()));
                EventUtil.triggerWarPetUpdate(playerId, pet.getId(), WarPetUpdate.MODIFY);
                if (!StringUtils.isEmpty(pet.getGemId())) {
                    gemIds.add(pet.getGemId());
                }
            });
            EventUtil.resetPetGemStatus(playerId, gemIds);
            petEntity.refreshAllPetPropertyAndPut(after, reason, false, true, true);
        });
        return RetCodeEnum.RCE_Success;
    }

    private int getPetDischargeRarity(int discharge, Pet pet) {
        if (discharge != Discharge.rarityReset) {
            return pet.getPetRarity();
        }
        if (pet.getPetRarity() < GameConfig.getById(GameConst.CONFIG_ID).getRarityresetminrarity()) {
            return pet.getPetRarity();
        }
        return GameConfig.getById(GameConst.CONFIG_ID).getAfterrarityresetrarity();
    }

    private RetCodeEnum consumePetReborn(String playerId, List<Pet> pets, Reason reason) {
        int rebornNeedConsumeNum = getRebornNeedConsumeNum(pets);
        if (rebornNeedConsumeNum <= 0) {
            return RetCodeEnum.RCE_Success;
        }
        Consume consume = ConsumeUtil.parseAndMulti(GameConfig.getById(GameConst.CONFIG_ID).getRebornpet(), rebornNeedConsumeNum);
        if (!ConsumeManager.getInstance().consumeMaterial(playerId, consume, reason)) {
            return RetCodeEnum.RCE_Player_DiamondNotEnought;
        }
        return RetCodeEnum.RCE_Success;
    }

    private int getRebornNeedConsumeNum(List<Pet> pets) {
        int rebornPetLv = GameConfig.getById(GameConst.CONFIG_ID).getRebornpetlv();

        return (int) pets.stream().filter(pet -> pet.getPetLvl() >= rebornPetLv).count();
    }


    public void combineRuneAndGemRewardInPets(Common.SC_DisplayMultiRewards.Builder showRewards, String playerId, List<String> petIdList) {
        List<Rune> runes = petruneCache.getInstance().getRuneListByPets(playerId, petIdList);
        List<Common.RuneReward> runeRewards = runeToRuneReward(runes);
        showRewards.addAllRuneReward(runeRewards);
        List<Gem> gems = petgemCache.getInstance().getGemListByPets(playerId, petIdList);

        showRewards.addAllRewardList(gemToReward(gems));
    }


    /**
     * 计算宠物放生后的消耗
     *
     * @return
     */
    public List<Reward> calculateDisChargeReward(List<Pet> pets, int dischargeType) {
        if (GameUtil.collectionIsEmpty(pets)) {
            return Collections.emptyList();
        }
        List<Reward> result = new ArrayList<>();
        pets.forEach(pet -> {
            for (Integer rewardType : dischargeRewardsType) {
                combineDisChargeRewardByType(rewardType, dischargeType, result, pet);
            }
            combineDisChargeAwakeReward(result, pet);
            if (dischargeType == Discharge.rarityReset) {
                result.addAll(PetBackConfig.getRewardByPet(pet));
            }
        });
        return RewardUtil.mergeReward(result);
    }

    private void combineDisChargeAwakeReward(List<Reward> result, Pet pet) {
        if (CollectionUtils.isEmpty(pet.getAwakeList())) {
            return;
        }
        int petOrientation = PetBaseProperties.getClass(pet.getPetBookId());
        List<Reward> awakeSourceReturn = PetAwakenConfig.getSourceReturn(pet.getAwakeList(), petOrientation);
        result.addAll(awakeSourceReturn);
    }

    private void combineDisChargeRewardByType(int rewardType, int dischargeType, List<Reward> result, Pet pet) {
        PetFreeConfigObject cfg = PetFreeConfig.getByTypeAndLvl(rewardType, getPetFreeLvl(rewardType, pet));
        if (cfg == null) {
            return;
        }
        if (dischargeType == Discharge.free) {
            result.addAll(RewardUtil.parseRewardIntArrayToRewardList(cfg.getRewardbylvl()));
        } else {
            result.addAll(RewardUtil.parseRewardIntArrayToRewardList(cfg.getReorderbylvl()));
        }

        if (PetBaseProperties.isCorePet(pet.getPetBookId())) {
            if (dischargeType == Discharge.free) {
                result.addAll(RewardUtil.parseRewardIntArrayToRewardList(cfg.getPetcorereward()));
            } else {
                result.addAll(RewardUtil.parseRewardIntArrayToRewardList(cfg.getPetcorereorder()));
            }

        }

    }

    private int getPetFreeLvl(int rewardType, Pet pet) {
        switch (rewardType) {
            case GameConst.PetUpType.Level:
                return pet.getPetLvl();
            case GameConst.PetUpType.Rarity:
                return pet.getPetRarity();
            case GameConst.PetUpType.Evolve:
                return pet.getEvolveLv();
        }
        return 0;
    }


    /**
     * @param playerIdx
     * @param petAddMap< >
     * @param source
     */
    public void playerObtainPets(String playerIdx, Map<Integer, Integer> petAddMap, Reason source) {
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (cache == null) {
            return;
        }

        List<Integer> bookIds = new ArrayList<>();
        List<Pet> addPets = new ArrayList<>();
        List<Integer> newBookIds = new ArrayList<>();
        Map<Integer, Set<Integer>> rarityBookIdsMap = new HashMap<>();
        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            int bookId;
            int amount;
            int rarity;
            for (Entry<Integer, Integer> entry : petAddMap.entrySet()) {
                bookId = PetComposeHelper.getBookIdByComposeId(entry.getKey());
                rarity = PetComposeHelper.getPetRarityByComposeId(entry.getKey());
                amount = entry.getValue();
                //获得前的拥有量
                int beforeAddOwned = cache.getPetOwnedCount(bookId);
                // addPets.addAll(getSpecialPetBuilder(bookId, rarity, amount, source == null ? 0 : source.getSourceNum()));
                addPets.addAll(PetManager.getInstance().buildLevel1NewPetList(bookId, rarity, amount, source == null ? 0 : source.getSourceNum()));

                bookIds.add(bookId);

                //统计宠物变化日志
                LogService.getInstance().submit(new DailyDateLog(playerIdx, false, RewardTypeEnum.RTE_Pet, bookId,
                        beforeAddOwned, amount, beforeAddOwned + amount, source));
                Set<Integer> rarityBookIds = rarityBookIdsMap.computeIfAbsent(rarity, a -> new HashSet<>());
                rarityBookIds.add(bookId);
            }
            cache.putAllPet(addPets);
            newBookIds.addAll(cache.collectionAllPet(bookIds, source, addPets));

        });

        petAddMap.forEach((key, value) -> {
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_Pet_CumuGainSpecifyPet, value, PetComposeHelper.getBookIdByComposeId(key));
            EventUtil.triggerUpdateTargetProgress(playerIdx, TargetTypeEnum.TTE_CumuGainPet, value, PetComposeHelper.getPetRarityByComposeId(key));
        });

        sendAddPet(playerIdx, addPets);
        petCache.getInstance().refreshAndSendTotalAbility(playerIdx);
        statisticByAddPet(addPets, newBookIds);
        sendObtainPetSystemChat(playerIdx, addPets, source);
        autoFreePet(playerIdx, addPets);
        EventUtil.triggerCoupTeamUpdate(playerIdx);
    }

    public RetCodeEnum playerObtainPet(String playerId, int petBookId, int amount, Reason sourceValue, boolean sendTotalAbility) {
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }

        int rarity = PetComposeHelper.getPetRarityByComposeId(petBookId);
        int realBookId = PetComposeHelper.getBookIdByComposeId(petBookId);


        List<Pet> temp = PetManager.getInstance().buildLevel1NewPetList(petBookId, rarity, amount, sourceValue == null ? 0 : sourceValue.getSourceNum());
        List<Integer> newBookIds = new ArrayList<>();
        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            //获得前的拥有量
            int beforeAddOwned = cache.getPetOwnedCount(realBookId);
            newBookIds.addAll(cache.collectionPet(temp.get(0), sourceValue));

            //统计宠物变化日志
            LogService.getInstance().submit(new DailyDateLog(playerId, false, RewardTypeEnum.RTE_Pet, realBookId,
                    beforeAddOwned, amount, beforeAddOwned + amount, sourceValue));
            cache.putAllPet(temp);
        });
        sendAddPet(playerId, temp);
        //更新战力
        if (sendTotalAbility) {
            refreshAndSendTotalAbility(playerId);
        }
        EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_CumuGainPet, amount, rarity);
        EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_Pet_CumuGainSpecifyPet, amount, petBookId);
        statisticByAddPet(temp, newBookIds);
        sendObtainPetSystemChat(playerId, temp, sourceValue);
        autoFreePet(playerId, temp);
        EventUtil.triggerCoupTeamUpdate(playerId);
        return RetCodeEnum.RCE_Success;
    }


    /**
     * 发送获得宠物系统聊天(暂时不要)
     *
     * @param playerId
     * @param temp
     * @param sourceValue
     */
    private void sendObtainPetSystemChat(String playerId, List<Pet> temp, Reason sourceValue) {
       /* String playerName = PlayerUtil.queryPlayerName(playerId);
        switch (sourceValue.getSourceEnum()) {
            //玩家通过契约法阵获得超凡品质以上魔灵
            case RSE_DrawCard_Common:
                filterPetCoreStream(temp, GameConst.PetCoreType.PCT_CF).forEach(e ->
                        GlobalData.getInstance().sendSystemChatToAllOnlinePlayer(SCE_ObtainPetByDrawCommonCard,
                                new SystemChatCreator().addPlayerIdx(playerId).addPlayerName(playerName).addPetCfgId(e.getPetRarity()).addPetRarity(e.getPetBookId())));

                //通过远古祭坛获得上古魔灵
            case RSE_AncientCall:
                filterPetCoreStream(temp, GameConst.PetCoreType.PCT_SG).forEach(e ->
                        GlobalData.getInstance().sendSystemChatToAllOnlinePlayer(SCE_ObtainPetByAncient,
                                new SystemChatCreator().addPlayerIdx(playerId).addPlayerName(playerName).addPetCfgId(e.getPetRarity())));
                //玩家通过许愿池获得超凡品质以上魔灵
            case RSE_WishingWell:
                filterPetCoreStream(temp, GameConst.PetCoreType.PCT_CF).forEach(e ->
                        GlobalData.getInstance().sendSystemChatToAllOnlinePlayer(SCE_ObtainPetByWishWell,
                                new SystemChatCreator().addPlayerIdx(playerId).addPlayerName(playerName).addPetCfgId(e.getPetRarity()).addPetRarity(e.getPetBookId())));

                //玩家通过碎片合成上古品质以上魔灵
            case RSE_PetFragment:
                filterPetCoreStream(temp, GameConst.PetCoreType.PCT_SG).forEach(e ->
                        GlobalData.getInstance().sendSystemChatToAllOnlinePlayer(SCE_obtainPetByFragment,
                                new SystemChatCreator().addPlayerIdx(playerId).addPlayerName(playerName).addPetCfgId(e.getPetRarity())));

        }*/
    }

    private Stream<Pet> filterPetCoreStream(List<Pet> temp, int needCore) {
        return temp.stream().filter(e -> PetBaseProperties.queryPetCore(e.getPetBookId()) >= needCore);
    }

    public void autoFreePet(String playerId, List<Pet> temp) {
        if (!playerSettingAutoFree(playerId)) {
            return;
        }

        List<String> freePetIds = temp.stream().filter(pet -> pet.getPetRarity() <= GameConfig.getById(GameConst.CONFIG_ID).getAutofreerarity()).map(Pet::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(freePetIds)) {
            return;
        }

        petCache.getInstance().dischargePet(playerId, freePetIds, 0, PetMessage.SC_PetDisCharge.newBuilder(), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_Pet_AutoFree));
    }

    public boolean playerSettingAutoFree(String playerId) {
        petEntity petEntity = getEntityByPlayer(playerId);
        if (petEntity == null) {
            return false;
        }
        return petEntity.getDbPetsBuilder().getAutoFree();
    }


    private void statisticByAddPet(List<Pet> pets, List<Integer> newBookIds) {
        if (CollectionUtils.isEmpty(pets)) {
            return;
        }
        Map<Integer, Long> rarityUpdate = new HashMap<>();
        Map<Integer, Long> bookIdUpdate = new HashMap<>();
        for (Pet pet : pets) {
            if (pet.getPetRarity() >= PetStatistics.petRarityLimit) {
                MapUtil.add2LongMapValue(rarityUpdate, pet.getPetRarity(), 1L);
            }

        }
        for (Integer newBookId : newBookIds) {
            if (PetBaseProperties.getQualityByPetId(newBookId) >= PetStatistics.petRarityLimit) {
                bookIdUpdate.put(newBookId, 1L);
            }
        }
        PetStatistics.getInstance().updateOwnPetBookIdMap(bookIdUpdate);
    }


    private void sendAddPet(String playerIdx, List<Pet> addPets) {
        if (CollectionUtils.isEmpty(addPets)) {
            return;
        }
        SC_AddPets.Builder msg = SC_AddPets.newBuilder();
        for (Pet pet : addPets) {
            msg.addPetId(pet.getId());
            msg.addBookId(pet.getPetBookId());
            msg.addRarity(pet.getPetRarity());
        }
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_AddPets_VALUE, msg);

    }


    /**
     * 默认无属性提升
     *
     * @param playerId
     * @param petIdList
     * @param subType
     * @return
     */
    public List<BattlePetData> getPetBattleData(String playerId, List<String> petIdList, BattleSubTypeEnum subType) {

        if (CollectionUtils.isEmpty(petIdList)) {
            return null;
        }
        List<Pet> petList = new ArrayList<>();
        for (String petIdx : petIdList) {
            Pet pet = petCache.getInstance().getPetById(playerId, petIdx);
            if (pet == null && BattleSubTypeEnum.BSTE_Patrol == subType) {
                pet = PatrolServiceImpl.getInstance().getVirtualPet(playerId, petIdx);
            } else if (pet == null && BattleSubTypeEnum.BSTE_Training == subType) {
                pet = TrainingManager.getInstance().getVirtualPet(playerId, petIdx);
            } else if (pet == null && BattleSubTypeEnum.BSTE_EpisodeSpecial == subType) {
                pet = HelpPetManager.getHelpPet(petIdx);
            }

            if (pet != null) {
                petList.add(pet);
            }
        }
        addHelpPet(petList, playerId, subType);
        return buildPlayerPetBattleData(playerId, petList, subType);
    }

    public Pet buildPet(int petBookId, int rarity, int level) {
        Builder petBuilder = getPetBuilder(petBookId, 0);
        if (petBuilder == null) {
            return null;
        }
        petBuilder.setPetRarity(rarity).setPetLvl(level);
        return refreshPetData(petBuilder, null).build();
    }

    private void addHelpPet(List<Pet> petIdList, String playerId, BattleSubTypeEnum subType) {
        if (BattleSubTypeEnum.BSTE_MainLineCheckPoint == subType) {
            petEntity petEntity = petCache.getInstance().getEntityByPlayer(playerId);
            if (petEntity == null) {
                return;
            }
            PetMessage.HelpPetBagItem helpPetBagItem = petEntity.getDbPetsBuilder().getHelpPetMap().get(Common.EnumFunction.MainLine_VALUE);
            if (helpPetBagItem == null || CollectionUtils.isEmpty(helpPetBagItem.getPetList())) {
                return;
            }
            for (Pet pet : helpPetBagItem.getPetList()) {
                petIdList.add(0, pet);
            }
        }

    }


    /**
     * @param playerId
     * @param petList
     * @param subType  pve build敌方战斗数据时必须传入
     * @return
     */
    public List<BattlePetData> buildPlayerPetBattleData(String playerId, List<Pet> petList, BattleSubTypeEnum subType) {
        return buildPetBattleData(playerId, petList, subType, true);
    }

    /**
     * @param playerId
     * @param petList
     * @param subType  pve build敌方战斗数据时必须传入
     * @param isPlayer 是否玩家宠物
     * @return
     */
    public List<BattlePetData> buildPetBattleData(String playerId, List<Pet> petList, BattleSubTypeEnum subType, boolean isPlayer) {

        List<BattlePetData> result = new ArrayList<>();
        if (petList == null) {
            return result;
        }
        playerEntity player = playerCache.getByIdx(playerId);
        List<Integer> bookIds = petList.stream().map(Pet::getPetBookId).collect(Collectors.toList());
        List<Integer> bonusBuff = getTeamBattleBonusBuff(subType, bookIds, isPlayer);
        long petAbilityAddition = player == null ? 0 : player.getDb_data().getPetAbilityAddition();
        Map<Integer, Integer> playerAddition = player == null ? Collections.emptyMap() : player.getDb_data().getPetPropertyAdditionMap();
        boolean functionUnLock = player != null && player.functionUnLock(Common.EnumFunction.PetResonance);
        List<ResonancePet> resonancePetList = toResonanceList(petList);
        for (Pet pet : petList) {
            //宠物共鸣buff
            List<Integer> resonanceBuffs = getPetResonance(functionUnLock, new ResonancePet(pet.getPetBookId(),
                    pet.getPetRarity()), resonancePetList, bookIds);

            List<Integer> allExPetBuff = CollectionUtil.safeMerge(resonanceBuffs, bonusBuff);
            //当前宠物属性加成
            BattlePetData.Builder battlePet = BattlePetData.newBuilder();
            battlePet.setPetId(pet.getId());
            battlePet.setEvolveLv(pet.getEvolveLv());
            battlePet.setPetLevel(pet.getPetLvl());
            battlePet.setPetRarity(pet.getPetRarity());
            //羁绊buff不需要放入
            battlePet.addAllBuffList(CollectionUtil.safeMerge(pet.getBuffIdsList(), resonanceBuffs));
            battlePet.setPetCfgId(pet.getPetBookId());
            battlePet.setAwake(pet.getPetUpLvl());
            battlePet.setPropDict(refreshPetBattlePropertyDictWithAddition(playerId, subType, pet, playerAddition, isPlayer));
            battlePet.setAbility(calculateBattlePetAbility(allExPetBuff, pet.getPetBookId(), getBattlePetBaseAbility(petAbilityAddition, pet), petList.indexOf(pet), pet.getSource()));
            if (pet.getSource() == Common.RewardSourceEnum.RSE_HelpPet_VALUE) {
                battlePet.setPetType(1);
            }
            result.add(battlePet.build());
        }

        return result;
    }

    /**
     * 玩家需要计算羁绊 ,其他的按照BattleSubTypeConfig来判断
     *
     * @param subType
     * @param petBookIds
     * @param player
     * @return
     */
    private List<Integer> getTeamBattleBonusBuff(BattleSubTypeEnum subType, List<Integer> petBookIds, boolean player) {
        if (CollectionUtils.isEmpty(petBookIds) || noBonusBuff(subType, player)) {
            return Collections.emptyList();
        }
        //<种族id,个数>
        Map<Integer, Long> bonusNumMap = petBookIds.stream().collect(Collectors.groupingBy(bookId -> {
            if (bookId == null) {
                return 0;
            }
            PetBasePropertiesObject baseProp = PetBaseProperties.getByPetid(bookId);
            return baseProp != null ? baseProp.getPettype() : 0;
        }, Collectors.counting()));

        return PetBondConfig.getInstance().queryBonusBuffs(bonusNumMap, player);
    }

    private boolean noBonusBuff(BattleSubTypeEnum subType, boolean player) {
        if (player) {
            return false;
        }
        if (subType == null) {
            LogUtil.warn("petCache getTeamBattleBonusBuff subType is null ");
            return true;
        }

        BattleSubTypeConfigObject cfg = BattleSubTypeConfig.getById(subType.getNumber());
        if (cfg == null) {
            LogUtil.error("getTeamBattleBonusBuff error BattleSubTypeConfig is null by battleSubType:{}", subType);
            return true;
        }
        if (!cfg.getHasmonsterbondbuff()) {
            return true;
        }
        return false;
    }

    private long calculateBattlePetAbility(List<Integer> allExPetBuff, int petBookId, long battlePetBaseAbility, int index, int source) {
        long abilityInTeam = FightPowerCalculate.getAbilityInTeam(petBookId, battlePetBaseAbility, allExPetBuff, index);
        if (source == RewardSourceEnum.RSE_BraveChallenge_VALUE) {
            return abilityInTeam * GameConfig.getById(GameConst.CONFIG_ID).getFightshowparameter() / GameConst.commonMagnification;
        }
        return abilityInTeam;

    }

    public long calculateTeamAbility(String playerId, Collection<String> petIds) {
        long teamAbility = 0L;
        if (StringUtils.isEmpty(playerId) || CollectionUtils.isEmpty(petIds)) {
            return teamAbility;
        }
        petEntity petEntity = petCache.getInstance().getEntityByPlayer(playerId);
        if (petEntity == null) {
            return teamAbility;
        }
        List<Pet> petList = petEntity.distinctGetPetByIdList(petIds);
        List<BattlePetData> battlePetData = buildPlayerPetBattleData(playerId, petList, null);
        for (BattlePetData petData : battlePetData) {
            teamAbility += petData.getAbility();
        }
        return teamAbility;
    }


    private long getBattlePetBaseAbility(long petAbilityAddition, Pet pet) {
        return RewardSourceEnum.RSE_Patrol_VALUE == pet.getSource() ? pet.getAbility() : pet.getAbility() + petAbilityAddition;
    }


    public PetProperties refreshProperty(PetProperties petProperty, Map<Integer, Integer> additionMap) {
        if (petProperty == null || CollectionUtils.isEmpty(additionMap)) {
            return petProperty;
        }
        PetProperties.Builder result = PetProperties.newBuilder();
        Integer value;

        for (PetPropertyEntity property : petProperty.getPropertyList()) {
            value = additionMap.get(property.getPropertyType());
            if (value != null) {
                result.addProperty(property.toBuilder().setPropertyValue(property.getPropertyValue() + value));
            } else {
                result.addProperty(property);
            }

        }

        return result.build();
    }

    private PetPropertyDict.Builder refreshPetBattlePropertyDictWithAddition(String playerId, BattleSubTypeEnum subType, Pet pet, Map<Integer, Integer> curPetAddition, boolean isPlayer) {
        int value;
        Integer addition;
        PetPropertyDict.Builder property = PetPropertyDict.newBuilder();
        List<Integer> addedPropertyType = new ArrayList<>();

        for (PetPropertyEntity propertyEntity : pet.getPetProperty().getPropertyList()) {
            property.addKeys(PetProperty.forNumber(propertyEntity.getPropertyType()));
            addition = RewardSourceEnum.RSE_Patrol_VALUE == pet.getSource() ? null : curPetAddition.get(propertyEntity.getPropertyType());
            if (addition != null) {
                addedPropertyType.add(propertyEntity.getPropertyType());
            }
            value = addition == null ? propertyEntity.getPropertyValue() : propertyEntity.getPropertyValue() + addition;
            property.addValues(value);
            if (propertyEntity.getPropertyType() == PetProperty.HEALTH_VALUE) {
                property.addKeys(PetProperty.Current_Health);
                property.addValues(getPetRemainHp(playerId, pet.getId(), subType, isPlayer));
            }
        }
        for (Entry<Integer, Integer> entry : curPetAddition.entrySet()) {
            if (!addedPropertyType.contains(entry.getKey())) {
                property.addKeys(PetProperty.forNumber(entry.getKey()));
                property.addValues(entry.getValue());
            }
        }
        return property;
    }


    /**
     * 获取共鸣buff 这一版不同宠物品质触发不同共鸣等级
     *
     * @param functionUnLock
     * @param curPet
     * @param petInTeam
     * @param teamBookId
     * @return
     */
/*    private List<Integer> getPetResonance(boolean functionUnLock, ResonancePet curPet
            , List<ResonancePet> petInTeam, List<Integer> teamBookId) {
        if (functionUnLock) {
            return Collections.emptyList();
        }
        List<Integer> result = null;
        for (List<PetResonanceCfgObject> list : PetResonanceCfg.resonanceMap.values()) {
            if (!resonanceContainsPetBookId(list.get(0).getGroupid(), curPet.getPetBookId(), teamBookId)) {
                continue;
            }
            for (PetResonanceCfgObject config : list) {
                if (matchResonance(config, curPet, petInTeam)) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    for (int buffId : config.getBufflist()) {
                        result.add(buffId);
                    }
                    break;
                }
            }
        }
        return result;
    }*/
    private List<Integer> getPetResonance(boolean functionUnLock, ResonancePet curPet
            , List<ResonancePet> petInTeam, List<Integer> teamBookId) {
        if (!functionUnLock) {
            return Collections.emptyList();
        }
        List<Integer> result = null;
        List<PetVibrationCfgObject> petVibrationCfg = PetVibrationCfg.getPetVibrationCfg(curPet.getPetBookId());
        if (CollectionUtils.isEmpty(petVibrationCfg)) {
            return Collections.emptyList();
        }
        for (PetVibrationCfgObject cfg : petVibrationCfg) {
            if (!teamBookId.containsAll(cfg.getNeedPetList())) {
                continue;
            }
            if (matchPetVibration(petInTeam, cfg)) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                int buffId = PetVibrationCfg.queryPetBuff(cfg, curPet.getPetBookId());
                if (buffId > 0) {
                    result.add(PetVibrationCfg.queryPetBuff(cfg, curPet.getPetBookId()));
                }
            }
        }
        return result;
    }

    private boolean matchPetVibration(List<ResonancePet> petInTeam, PetVibrationCfgObject cfg) {
        for (int i = 0; i < cfg.getNeedPetList().size(); i++) {
            int petBookId = cfg.getNeedpet()[i][0];
            int rarity = cfg.getNeedpet()[i][1];
            if (petInTeam.stream().noneMatch(e -> e.getPetBookId() == petBookId
                    && e.getPetRarity() >= rarity)) {
                return false;
            }

        }
        return true;

    }


    private int getPetRemainHp(String playerId, String petId, BattleSubTypeEnum battleType, boolean isPlayer) {
        if (battleType == BattleSubTypeEnum.BSTE_BreaveChallenge) {
            return bravechallengeCache.getInstance().getPetRemainHpRate(playerId, petId);
        } else if (battleType == BattleSubTypeEnum.BSTE_NewForeignInvasion && isPlayer) {
            return foreigninvasionCache.getInstance().getPlayerPetRemainHp(playerId, petId);
        }
        return GameConst.PetMaxHpRate;
    }

    private boolean resonanceContainsPetBookId(int groupId, int petBookId, List<Integer> teamBookId) {
        List<Integer> needBookIds = PetResonanceCfg.resonanceGroupMap.get(groupId);
        if (CollectionUtils.isEmpty(needBookIds)) {
            return false;
        }
        if (!needBookIds.contains(petBookId)) {
            return false;
        }

        return teamBookId.containsAll(needBookIds);
    }

    private boolean matchResonance(PetResonanceCfgObject config, ResonancePet curPet, List<ResonancePet> petInTeam) {
        int[][] needPet = config.getNeedpet();

        if (Arrays.stream(needPet).noneMatch(needConfig -> existResonance(curPet, needConfig))) {
            return false;
        }

        for (int[] needConfig : needPet) {
            Optional<ResonancePet> check = petInTeam.stream().filter(pet -> existResonance(pet, needConfig)).findFirst();

            if (!check.isPresent()) {
                return false;
            }
        }
        return true;

    }

    private boolean existResonance(ResonancePet curPet, int[] needConfig) {
        return needConfig.length >= 2 && needConfig[0] == curPet.getPetBookId() && needConfig[1] <= curPet.getPetRarity();
    }

    public long getBasicPetBonusRate(BattleSubTypeEnum subType, List<Integer> petBookIdList, boolean player) {
        List<Integer> teamBonusBuff = getTeamBattleBonusBuff(subType, petBookIdList, player);
        if (CollectionUtils.isEmpty(teamBonusBuff)) {
            return 0L;
        }
        long result = 0;
        for (Integer buffId : teamBonusBuff) {
            BuffConfigObject config = BuffConfig.getById(buffId);
            if (config != null) {
                result += config.getAbilityaddtion();
            }
        }
        return result;
    }


    public Pet.Builder refreshPetData(Pet.Builder pet, List<Rune> runeList) {
        return refreshPetData(pet, runeList, null, false);
    }

    public Pet.Builder refreshPetData(Pet.Builder pet, List<Rune> runeList, String playerId, boolean showAbilityChange) {
        return refreshPetData(pet, runeList, playerId, showAbilityChange, null, null, false);
    }

    /**
     * 刷新宠物属性
     *
     * @param pet        原宠物
     * @param runeList   穿戴符文信息
     * @param exProperty 额外属性加成(如勇气试炼给宠物的额外属性)
     * @param isMonster  是否怪物
     */
    public Pet.Builder refreshPetData(Builder pet, List<Rune> runeList, String playerId, boolean showAbilityChange
            , Map<Integer, Integer> exProperty, List<Integer> exBuffIds, boolean isMonster) {

        if (pet == null) {
            return null;
        }
        int propertyType;
        int value;

        //符文加成
        Map<Integer, Integer> runeAddition = calculateRuneAddition(runeList);
        //觉醒加成
        Map<Integer, Integer> awakeAddition = calculateAwakeAddition(pet);
        //宝石加成
        GemAdditionDto gemAdditionDto = calculateGemAddition(playerId, pet.getGemId());
        Map<Integer, Integer> gemAddition = gemAdditionDto.getPropertyAddition();
        //宠物品质加成
        Map<Integer, Integer> rarityAddition = PetRarityConfig.getRarityAddition(pet.getPetRarity(), pet.getPetBookId());


        PetBasePropertiesObject petBaseProperties = PetBaseProperties.getByPetid(pet.getPetBookId());
        if (petBaseProperties==null){
            LogUtil.error("refreshPetData error can`t find PetBaseProperties cfg by petId:{}",pet.getPetBookId());
            return pet;
        }
        PetLvlGroupUpConfigObject lvCfg = PetLvlGroupUpConfig.findByPetPropertyModelAndLv(petBaseProperties.getPropertymodel(), pet.getPetLvl());

        List<PetPropertyEntity> proList = new ArrayList<>(pet.getPetProperty().getPropertyList());
        for (PetPropertyEntity propertyEntity : pet.getPetProperty().getPropertyList()) {
            propertyType = propertyEntity.getPropertyType();
            value = getBasePetPropertyByConfig(pet, petBaseProperties, lvCfg, propertyType, isMonster);
            value = additionProperty(propertyType, value, awakeAddition, runeAddition, rarityAddition, gemAddition, exProperty);
            refreshProp(value, proList, propertyType);
        }

        propUpScaleUp(proList, awakeAddition, runeAddition);

        pet.clearBuffIds().addAllBuffIds(getSuitBuff(runeList));
        if (!CollectionUtils.isEmpty(exBuffIds)) {
            pet.addAllBuffIds(exBuffIds);
        }
        if (!CollectionUtils.isEmpty(gemAdditionDto.getBuffIds())) {
            pet.addAllBuffIds(gemAdditionDto.getBuffIds());
        }
        if (!CollectionUtils.isEmpty(pet.getActiveLinkList())) {
            pet.addAllBuffIds(LinkConfig.getPetLinkBuffs(pet.getPetBookId(), pet.getActiveLinkList()));
        }

        pet.getPetPropertyBuilder().clear().addAllProperty(proList);

        //战力计算
        long ability = new FightPowerCalculate(pet, runeList).calculateAbilityL();
        if (showAbilityChange) {
            sendAbilityChange(playerId, pet.getAbility(), ability);
        }

        pet.setAbility(ability);
        return pet;
    }


    private static Map<Integer, Integer> scaleUpProperty = new HashMap<>();

    static {
        scaleUpProperty.put(ExtendAttackRate_VALUE, ATTACK_VALUE);
        scaleUpProperty.put(ExtendDefenceRate_VALUE, DEFENSIVE_VALUE);
        scaleUpProperty.put(ExtendHealthRate_VALUE, HEALTH_VALUE);
        scaleUpProperty = MapUtils.unmodifiableMap(scaleUpProperty);
    }

    private PetPropertyEntity findPropertyByType(List<PetPropertyEntity> proList, int propetyType) {
        return proList.stream().filter(e -> propetyType == e.getPropertyType()).findFirst().orElse(null);
    }

    /**
     * 处理宠物属性中的千分比 攻防血
     *
     * @param proList
     * @param additionMaps
     */
    private void propUpScaleUp(List<PetPropertyEntity> proList, Map<Integer, Integer>... additionMaps) {
        for (Map<Integer, Integer> additionMap : additionMaps) {
            for (Entry<Integer, Integer> property : additionMap.entrySet()) {
                Integer upProperty = scaleUpProperty.get(property.getKey());
                if (upProperty == null) {
                    continue;
                }
                PetPropertyEntity property1 = findPropertyByType(proList, upProperty);
                if (property1 != null) {
                    int value = (int) (property1.getPropertyValue() + property1.getPropertyValue() * (property.getValue() / 1000.0));
                    refreshProp(value, proList, upProperty);
                }
            }
        }
    }


    private GemAdditionDto calculateGemAddition(String playerId, String gemId) {
        if (StringUtils.isEmpty(gemId)) {
            return emptyGemAdditionDto;
        }
        if (virtualGem(gemId)) {
            return new GemAdditionDto(Collections.emptyList(), PetGemConfig.getGemAdditionByGemCfgId(Integer.parseInt(gemId)));
        }
        petgemEntity petgemEntity = petgemCache.getInstance().getEntityByPlayer(playerId);
        if (petgemEntity == null) {
            return emptyGemAdditionDto;
        }
        Gem gem = petgemEntity.getGemById(gemId);
        if (gem == null) {
            return emptyGemAdditionDto;
        }
        Map<Integer, Integer> gemAddition = PetGemConfig.getGemAdditionByGemCfgId(gem.getGemConfigId());
        Pair<List<Integer>, Map<Integer, Integer>> inscriptionAddition = calculateInscriptionAddition(playerId, gem);
        if (inscriptionAddition != null) {
            Map<Integer, Integer> finalAddition = new HashMap<>(gemAddition);
            MapUtil.mergeIntMaps(finalAddition, inscriptionAddition.getValue());
            return new GemAdditionDto(inscriptionAddition.getKey(), finalAddition);
        }
        return new GemAdditionDto(Collections.emptyList(), gemAddition);
    }


    private Pair<List<Integer>, Map<Integer, Integer>> calculateInscriptionAddition(String playerId, Gem gem) {
        List<String> inscriptionIdList = gem.getInscriptionIdList();
        if (CollectionUtils.isEmpty(inscriptionIdList)) {
            return null;
        }
        petinscriptionEntity inscriptionEntity = petinscriptionCache.getInstance().getEntityByPlayer(playerId);
        if (inscriptionEntity == null) {
            return null;
        }
        List<Integer> inscriptionCfgIds = inscriptionEntity.getInscriptionCfgIds(inscriptionIdList);
        Map<Integer, Integer> addition = InscriptionCfg.getInstance().calculteAddition(inscriptionCfgIds);
        List<Integer> additionBuff = InscriptionCfg.getAdditionBuff(inscriptionCfgIds);
        return new Pair<>(additionBuff, addition);
    }


    private boolean virtualGem(String gemId) {
        //巡逻队中助阵宠物宝石用的宝石配置id做的id,该宝石不属于玩家也不存在于背包
        return gemId.length() < 10;
    }


    @SafeVarargs
    private final int additionProperty(int propertyType, int value, Map<Integer, Integer>... additionMaps) {
        if (additionMaps.length <= 0) {
            return value;
        }
        for (Map<Integer, Integer> additionMap : additionMaps) {
            if (MapUtils.isNotEmpty(additionMap)) {
                Integer addition = additionMap.get(propertyType);
                value = addition == null ? value : addition + value;
            }
        }
        return value;
    }

    private Map<Integer, Integer> calculateAwakeAddition(Builder pet) {
        if (CollectionUtils.isEmpty(pet.getAwakeList())) {
            return Collections.emptyMap();
        }
        Map<Integer, Integer> awakeAddition = new HashMap<>();
        //分支觉醒等级加成
        for (PetAwake petAwake : pet.getAwakeList()) {
            add2IntMapValue(awakeAddition, petAwake.getType(), petAwake.getPropertyAddition());
        }
        //总觉醒等级加成
        //refreshAwakeLv(pet);
        if (pet.getPetUpLvl() <= 0) {
            return awakeAddition;
        }
        int[][] awakePropertyAddition = PetAwakenConfig.getAwakePropertyAddition(pet, PetProperty.AllMainProp_VALUE);
        if (ArrayUtils.isEmpty(awakePropertyAddition)) {
            return awakeAddition;
        }
        for (int[] ints : awakePropertyAddition) {
            if (ints.length <= 1) {
                continue;
            }
            add2IntMapValue(awakeAddition, ints[0], ints[1]);

        }
        return awakeAddition;
    }

    private void sendAbilityChange(String playerIdx, long before, long after) {
        SC_PetAbilityUpdate.Builder builder = SC_PetAbilityUpdate.newBuilder();
        builder.setAbilityUpdate(after - before);
        builder.setBeforeAbility(before);
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_PetAbilityUpdate_VALUE, builder);
    }

    private void refreshAwakeLv(Builder pet) {
        List<PetAwake> awakeList = pet.getAwakeList();
        if (awakeList.size() < 3) {
            return;
        }
        int awakeLv = awakeList.stream().mapToInt(PetAwake::getLevel).min().orElse(0);
        pet.setPetUpLvl(awakeLv);
    }


    private List<Integer> getSuitBuff(List<Rune> runeList) {
        if (CollectionUtils.isEmpty(runeList) || runeList.size() <= 1) {
            return Collections.emptyList();
        }
        List<Integer> buffIds = new ArrayList<>();
        Map<Integer, List<Rune>> runeMap = runeList.stream().
                collect(Collectors.groupingBy(rune -> PetRuneProperties.getByRuneid(rune.getRuneBookId()).getRunesuit()));
        // 统计计算套装加成，KV:key-propertyType,value-propertyValue
        runeMap.keySet().forEach(suitId -> {
            for (int[] buffConfig : PetRuneSuitProperties.getBySuitid(suitId).getBuffid()) {
                // buffConfig[需要符文数量,加成buff,(0:展示buff,1战斗buff)]
                // 检查到套装属性要求小于已穿戴的套装数，触发套装效果
                if (buffConfig.length > 2 && buffConfig[0] <= runeMap.get(suitId).size() && buffConfig[2] == 1) {
                    buffIds.add(buffConfig[1]);
                }
            }
        });
        return buffIds;
    }

    private void refreshProp(int value, List<PetPropertyEntity> proList, int propertyType) {
        if (CollectionUtils.isEmpty(proList)) {
            return;
        }
        Optional<PetPropertyEntity> proInCache = proList.stream().
                filter(pro -> propertyType == pro.getPropertyType() && value != pro.getPropertyValue()).findAny();

        if (proInCache.isPresent()) {
            proList.remove(proInCache.get());
            proList.add(PetPropertyEntity.newBuilder(proInCache.get()).setPropertyValue(value).build());
        }
    }


    private Map<Integer, Integer> calculateRuneAddition(List<Rune> runeList) {
        if (CollectionUtils.isEmpty(runeList)) {
            return Collections.emptyMap();
        }

        //<套装id,符文集合>
        Map<Integer, List<Rune>> runeMap = runeList.stream().
                collect(Collectors.groupingBy(this::getRuneSuitId));

        Map<Integer, Integer> runePropertyAddition = new HashMap<>();
        for (Entry<Integer, List<Rune>> entry : runeMap.entrySet()) {
            if (entry.getKey()<=0){
                continue;
            }
            int[][] suitProperties = PetRuneSuitProperties.getBySuitid(entry.getKey()).getSuitproperties();
            for (Rune rune : entry.getValue()) {
                //基础属性
                for (RunePropertieyEntity runeBaseProperty : rune.getRuneBaseProperty().getPropertyList()) {
                    add2IntMapValue(runePropertyAddition, runeBaseProperty.getPropertyType(), runeBaseProperty.getPropertyValue());
                }
                //附加属性
                for (RunePropertieyEntity runeExProperty : rune.getRuneExProperty().getPropertyList()) {
                    add2IntMapValue(runePropertyAddition, runeExProperty.getPropertyType(), runeExProperty.getPropertyValue());
                }
                //祝福属性
                mergeIntMaps(runePropertyAddition, PetRuneBlessRatingCfg.getInstance().queryRuneAddition(rune));

            }
            List<int[]> suitCfg = Arrays.stream(suitProperties).filter(item -> item.length > 2 && item[2] <= entry.getValue().size()).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(suitCfg)) {
                for (int[] suitConfig : suitCfg) {
                    add2IntMapValue(runePropertyAddition, suitConfig[0], suitConfig[1]);
                }
            }
        }
        return runePropertyAddition;
    }

    private int getRuneSuitId(Rune rune) {
        PetRunePropertiesObject cfg = PetRuneProperties.getByRuneid(rune.getRuneBookId());
        if (cfg == null) {
            return 0;
        }
        return cfg.getRunesuit();
    }

    /**
     * 当前宠物的属性(包含等级,不计算装备)
     *
     * @param pet
     * @param petBaseProperties
     * @param lvCfg
     * @param propertyType
     * @return
     */
    private int getBasePetPropertyByConfig(Builder pet, PetBasePropertiesObject petBaseProperties,
                                           PetLvlGroupUpConfigObject lvCfg, int propertyType, boolean monster) {

        if (petBaseProperties == null || lvCfg == null) {
            return 0;
        }

        //基础属性
        int petBaseValue = getBasicPropertyValueByCfg(petBaseProperties.getPetproperties(), propertyType);

        int factor;
        int[][] factorCfg = monster ? lvCfg.getMonsterfactor() : lvCfg.getFactor();
        int[][] otherFactorCfg = monster ? lvCfg.getMonsterotherfactors() : lvCfg.getOtherfactors();

        //基础属性计算
        if (PetProperty.ATTACK_VALUE == propertyType || PetProperty.DEFENSIVE_VALUE == propertyType
                || PetProperty.HEALTH_VALUE == propertyType) {
            // 卡牌品质加成
            PetRarityConfigObject rarityConfig = PetRarityConfig.getByRarityAndPropertyModel(pet.getPetRarity(), petBaseProperties.getPropertymodel());
            if (rarityConfig == null) {
                LogUtil.warn("cant`t find rarityConfig by rarity:{},propertyModel:{}", pet.getPetRarity(), petBaseProperties.getPropertymodel());
                return petBaseValue;
            }
            factor = ArrayUtil.getValueFromKeyValueIntArray(factorCfg, propertyType);
            //攻防血 = 基础值*品质系数*等级系数
            return (int) Math.floor(petBaseValue * ((double) rarityConfig.getPetfactor() / 1000) * ((double) factor / 1000));
        }
        //暴击爆伤,抗暴击抗暴伤....攻防血以外其他属性= 基础值*品质系+等级系数
        factor = ArrayUtil.getValueFromKeyValueIntArray(otherFactorCfg, propertyType);
        return petBaseValue + factor;

    }


    public List<Pet.Builder> getSpecialPetBuilder(int petBookId, int rarity, int count, int sourceValue) {
        List<Pet.Builder> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Pet.Builder newPet = getPetBuilder(petBookId, sourceValue);
            if (null != newPet) {
                newPet.setPetRarity(rarity);
                result.add(newPet);
            }
        }
        return result;
    }

    /**
     * 按枚举类型取出配置属性值
     *
     * @param source 配置数据，二维数组：
     *               [属性类型][属性值]
     *               [属性类型][属性值]
     * @param type   属性类型
     * @return 属性值
     */
    private int getBasicPropertyValueByCfg(int[][] source, int type) {
        if (source != null) {
            for (int[] property : source) {
                if (property[0] == type) {
                    return property[1];
                }
            }
        }
        LogUtil.error("error in PetCache,method getPropertyValueByCfg(),source: ");
        if (source != null) {
            for (int[] ints : source) {
                LogUtil.error("" + Arrays.toString(ints) + "");
            }
        } else {
            LogUtil.error("source is null");
        }
        LogUtil.error(",type: " + type + "\n");
        return 0;
    }

    public Pet.Builder getPetBuilder(int petBookId, int sourceValue) {
        PetBasePropertiesObject petInit = PetBaseProperties.getByPetid(petBookId);
        if (petInit == null) {
            LogUtil.error("model.pet.dbCache.service.PetServiceImpl.getPetEntity, pet bool id is not exist, book id =" + petBookId);
            return null;
        }
        return getPetBuilder(petInit, sourceValue);
    }

    /**
     * 通过配置初始化宠物
     *
     * @return 初始化宠物实体
     */
    public Pet.Builder getPetBuilder(PetBasePropertiesObject pet, int sourceValue) {
        if (pet == null) {
            LogUtil.error("error in PetServiceImpl,method getPetEntity():pet cfg is null" + "\n");
            return null;
        }
        // 配置表属性部分数据已*1000，注意

        Pet.Builder result = Pet.newBuilder();
        // 基础属性
        result.setId(IdGenerator.getInstance().generateId());
        result.setPetBookId(pet.getPetid());
        result.setPetLvl(1);
        result.setPetRarity(pet.getStartrarity());

        result.setPetAliveStatus(1);
        // 属性初始化
        PetProperties.Builder properties = PetProperties.newBuilder();
        for (int[] propertyCfg : pet.getPetproperties()) {
            PetPropertyEntity.Builder property = PetPropertyEntity.newBuilder();
            property.setPropertyType(propertyCfg[0]);
            property.setPropertyValue(propertyCfg[1]);
            properties.addProperty(property);
        }
        result.setPetProperty(properties);
        result.setSource(sourceValue);
        return result;
    }

    public long getBasicPetFightPower(int bookId, int level, int raity) {
        PetBasePropertiesObject basicPetProp = PetBaseProperties.getByPetid(bookId);
        if (basicPetProp == null) {
            return 0;
        }
        Pet.Builder result = Pet.newBuilder();
        result.setPetBookId(bookId);
        result.setPetLvl(level);
        result.setPetRarity(raity);


        PetProperties.Builder properties = PetProperties.newBuilder();
        for (int i = PetProperty.ATTACK_VALUE; i <= PetProperty.ATTACK_SPEED_VALUE; i++) {
            PetPropertyEntity.Builder property = PetPropertyEntity.newBuilder();
            property.setPropertyType(i);
            property.setPropertyValue(getBasicPropertyValueByCfg(basicPetProp.getPetproperties(), i));
            properties.addProperty(property);
        }
        result.setPetProperty(properties);
        refreshPetData(result, null);
        return result.getAbility();
    }

    public boolean petTransfer(String playerIdx, String petIdx, int targetBookId) {
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (PetBaseProperties.getByPetid(targetBookId) == null || cache == null) {
            return false;
        }

        return SyncExecuteFunction.executeFunction(cache, cacheTemp -> {
            Pet pet = cache.getPetById(petIdx);
            if (pet == null) {
                return false;
            }

            int srcBookId = pet.getPetBookId();
            Pet.Builder petBuilder = pet.toBuilder();
            petBuilder.setPetBookId(targetBookId).setSource(RewardSourceEnum.RSE_PetTransfer_VALUE);
            //重新计算属性并推送
            cache.refreshPetPropertyAndPut(petBuilder, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetTransfer), false);

            LogUtil.info("playerIdx：" + playerIdx + ",petTransfer, change petIdx [" + pet.getId() + "]src bookId = "
                    + srcBookId + ", to =" + targetBookId);

            statisticsByPetTransfer(targetBookId, srcBookId);
            EventUtil.triggerCollectPets(playerIdx, Collections.singletonList(petBuilder.build()));
            return true;
        });
    }

    private void statisticsByPetTransfer(int targetBookId, int srcBookId) {
        Map<Integer, Long> map = new HashMap<>();
        if (PetBaseProperties.getQualityByPetId(srcBookId) >= PetStatistics.petRarityLimit) {
            map.put(srcBookId, -1L);
        }
        if (PetBaseProperties.getQualityByPetId(targetBookId) >= PetStatistics.petRarityLimit) {
            map.put(targetBookId, 1L);
        }
        PetStatistics.getInstance().updateOwnPetBookIdMap(map);
    }

    public boolean petChange(String playerId, String petId, boolean status) {
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null) {
            return false;
        }

        return SyncExecuteFunction.executeFunction(cache, cacheTemp -> {
            Pet pet = cache.getPetById(petId);
            if (null == pet) {
                LogUtil.error("error in PetServiceImpl,method petChange(): no such pet,playerId: " + playerId + " petId:" + petId + "\n");
                return false;
            }

            // 转换中为1
            pet = pet.toBuilder().setPetChangeStatus(status ? 1 : 0).build();
            cache.putPet(pet);

            // 解除转换后可能宠物的bookId 已经变化了
            if (!status) {
                cache.collectionPet(pet, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetTransfer));
            }

            cache.sendPetChangeStatus(petId);
            return true;
        });
    }


    /**
     * 检查玩家宠物状态是否合法
     *
     * @param pet 宠物
     * @return 是否合法
     */
    public static RetCodeEnum petStatusCheck(Pet pet) {
        // 检查宠物状态
        if (pet.getPetLockStatus() != 0) {
            return RetCodeEnum.RCE_Pet_PetStatusLock;
        }
        //编队中允许放生
//        if (pet.getPetMineStatus() != 0) {
//            return RetCodeEnum.RCE_Pet_PetStatusMineLock;
//        }
//        if (pet.getPetTeamStatus() != 0) {
//            return RetCodeEnum.RCE_Pet_PetStatusTeamLock;
//        }
        if (pet.getPetChangeStatus() != 0) {
            return RetCodeEnum.RCE_Pet_PetStatusChangeLock;
        }
        return RetCodeEnum.RCE_Success;
    }

    public List<PetDisplayInfo> displayPetMsgList(String playerId, List<String> petIdList) {
        petEntity entity = petCache.getInstance().getEntityByPlayer(playerId);
        if (null == entity || GameUtil.collectionIsEmpty(petIdList)) {
            return null;
        }

        playerEntity player = playerCache.getByIdx(playerId);
        if (player == null) {
            return null;
        }
        long petAbilityAddition = player.getDb_data().getPetAbilityAddition();
        return petIdList.stream()
                .map(e -> buildPetDisInfo(entity.getPetById(e), petAbilityAddition))
                .collect(Collectors.toList());
    }

    public PetDisplayInfo buildPetDisInfo(Pet pet, long petAbilityAddition) {
        if (null == pet) {
            return null;
        }
        PetDisplayInfo.Builder result = PetDisplayInfo.newBuilder();
        return result.setPetIdx(pet.getId())
                .setPetBookId(pet.getPetBookId())
                .setPetLevel(pet.getPetLvl())
                .setPetRarity(pet.getPetRarity())
                .setPetAbility(pet.getAbility() + petAbilityAddition)
                .setPetUpLvl(pet.getPetUpLvl())
                .setPetEvolveLv(pet.getEvolveLv())
                .build();
    }


    public void petSpecify(String playerId, int bookId, int lvl, int petRarity, int upLvl, int amount) {
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerId);
        if (null == cache) {
            return;
        }
        int maxUpLv = PetAwakenConfig._ix_id.values().stream().mapToInt(PetAwakenConfigObject::getUplvl).max().orElse(0);
        upLvl = Math.min(maxUpLv, upLvl);
        PetBasePropertiesObject petConfig = PetBaseProperties.getByPetid(bookId);
        if (petConfig == null || petConfig.getStartrarity() > petRarity || petConfig.getMaxrarity() < petRarity) {
            return;
        }

        int finalUpLvl = upLvl;
        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            for (int i = 0; i < amount; i++) {
                Pet.Builder pet = getPetBuilder(bookId, RewardSourceEnum.RSE_GM_VALUE);
                if (pet == null) {
                    continue;
                }
                // 修改等级
                pet.setPetLvl(lvl).setPetRarity(petRarity).setPetUpLvl(finalUpLvl);
                if (finalUpLvl > 0) {
                    for (int j = 1; j <= 3; j++) {
                        pet.addAwake(PetAwake.newBuilder().setLevel(finalUpLvl).setType(j).build());
                    }
                }

                // 刷新属性
                cache.refreshPetPropertyAndPut(pet, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM), false);
                cache.collectionPet(pet.build(), ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_GM));
            }

        });
    }

    public synchronized long totalAbility(String playerId) {
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null) {
            return 0;
        }
        long result = 0;
        for (Pet pet : cache.peekAllPetByUnModify()) {
            result += pet.getAbility();
        }
        playerEntity player = playerCache.getByIdx(playerId);
        if (player != null) {
            result += player.getDb_data().getPetAbilityAddition() * cache.getOccupancy();
        }
        cache.setTotalAbility(result);
        return result;
    }

    public void refreshAndSendTotalAbility(String playerIdx) {
        long ability = totalAbility(playerIdx);
        RankingManager.getInstance().updatePlayerRankingScore(playerIdx, Activity.EnumRankingType.ERT_Ability, ability);
        sendTotalAbility(playerIdx);
    }

    public void sendTotalAbility(String playerIdx) {
        SC_RefreshTotalAbility.Builder builder = SC_RefreshTotalAbility.newBuilder();
        builder.setTotalAbility(queryTotalAbility(playerIdx));
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_RefreshTotalAbility_VALUE, builder);
    }

    private long queryTotalAbility(String playerIdx) {
        petEntity cache = petCache.getInstance().getEntityByPlayer(playerIdx);
        if (cache == null) {
            return 0;
        }
        return cache.getTotalAbility();
    }


    /**
     * 服务器启动时重新计算所有宠物的属性
     */
    public void reCalculateAllPetProperties() {
        for (petEntity value : this.entityMap.values()) {
            SyncExecuteFunction.executeConsumer(value, entity -> {
                Collection<Pet> allPet = value.peekAllPetByUnModify();
                List<Pet.Builder> allPetsBuilder = allPet.stream().map(Pet::toBuilder).collect(Collectors.toList());
                value.refreshAllPetPropertyAndPut(allPetsBuilder, null, false, false, true);
            });
        }
    }

    /**
     * 获取最高战力宠物
     *
     * @param playerIdx
     * @return
     */
    public Pet getMaxAbilityPet(String playerIdx) {
        if (StringUtils.isEmpty(playerIdx)) {
            return null;
        }
        petEntity entity = getEntityByPlayer(playerIdx);
        if (entity == null) {
            return null;
        }
        return entity.getMaxAbilityPet();
    }

    public long getPetMaxAbility(String playerIdx) {
        Pet maxAbilityPet = getMaxAbilityPet(playerIdx);
        if (maxAbilityPet == null) {
            return 0;
        }
        return maxAbilityPet.getAbility();
    }

    public int getPexMaxLv(int rarity) {
        Integer maxLv = PetRarityConfig.getMaxLv(rarity);
        return maxLv == null ? 1 : maxLv;
    }

    public int getPetMaxRarity(int petBookId) {
        PetBasePropertiesObject config = PetBaseProperties.getByPetid(petBookId);
        return config == null ? 0 : config.getMaxrarity();
    }

    // 获得修正等级宠物
    public Pet buildReviseLevelPet(String playerIdx, String petIdx, int level) {
        Pet pet = petCache.getInstance().getPetById(playerIdx, petIdx);
        if (pet == null) {
            return null;
        }
        Builder petBuilder = pet.toBuilder().setPetLvl(level);
        List<Rune> petRune = petruneCache.getInstance().getPetRune(playerIdx, petIdx);
        return refreshPetData(petBuilder, petRune).build();
    }

    // 原迷雾森林修正等级宠物,需求已修改，代码暂保留
    public List<BattlePetData> buildReviseLevelPetList(String playerIdx, List<String> petIdxList,
                                                       int level, BattleSubTypeEnum subType) {
        List<Pet> sourcePetList = getPetByIdList(playerIdx, petIdxList);
        if (CollectionUtils.isEmpty(sourcePetList)) {
            return null;
        }
        List<Pet> revisePetList = new ArrayList<>();
        Pet newPet;
        for (Pet pet : sourcePetList) {
            Builder petBuilder = pet.toBuilder().setPetLvl(level);
            List<Rune> petRune = petruneCache.getInstance().getPetRune(playerIdx, pet.getId());
            newPet = refreshPetData(petBuilder, petRune).build();
            revisePetList.add(newPet);
        }
        return buildPlayerPetBattleData(playerIdx, revisePetList, subType);
    }

    public static void settlePetUpdate(String playerId, String petId, Reason reason) {
        if (StringUtils.isEmpty(playerId) || StringUtils.isEmpty(petId)) {
            return;
        }
        petEntity petEntity = getInstance().getEntityByPlayer(playerId);
        if (petEntity == null) {
            return;
        }

        EventUtil.triggerRefreshPetData(playerId, petId, reason);
        //通知战戈宠物更新
        EventUtil.triggerWarPetUpdate(playerId, petId, WarPetUpdate.MODIFY);
    }

    public void sendAllPetAbilityUpdate(String playerIdx, long abilityUpdate, int type) {
        SC_PetAbilityUpdate.Builder builder = SC_PetAbilityUpdate.newBuilder();
        builder.setAbilityUpdate(abilityUpdate);
        builder.setUpdateType(type);
        GlobalData.getInstance().sendMsg(playerIdx, MsgIdEnum.SC_PetAbilityUpdate_VALUE, builder);
    }


    public void equipGemStatistic(String playerId, Gem unEquipGem, Gem equipGem) {
        int equipEnhanceLvUpdate = 0;
        int totalEquipNumUpdate = 0;
        Map<Integer, Long> rarityUpdate = new HashMap<>();
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerId);
        if (unEquipGem != null && gemInFightPet(teamEntity, unEquipGem)) {
            PetGemConfigObject unEquipConfig = PetGemConfig.getById(unEquipGem.getGemConfigId());
            if (unEquipConfig != null) {
                equipEnhanceLvUpdate -= unEquipConfig.getLv();
                MapUtil.add2LongMapValue(rarityUpdate, unEquipConfig.getRarity(), -1L);
                totalEquipNumUpdate--;
            }
        }

        if (equipGem != null) {
            PetGemConfigObject equipGemConfig = PetGemConfig.getById(equipGem.getGemConfigId());
            if (equipGemConfig != null && gemInFightPet(teamEntity, equipGem)) {
                equipEnhanceLvUpdate += equipGemConfig.getLv();
                MapUtil.add2LongMapValue(rarityUpdate, equipGemConfig.getRarity(), 1L);
                totalEquipNumUpdate++;
            }
        }


        GemStatistics.getInstance().updateEquipGemRarityMap(rarityUpdate);
        GemStatistics.getInstance().updateEquipEnhanceLv(equipEnhanceLvUpdate);
        GemStatistics.getInstance().updateTotalEquipNum(totalEquipNumUpdate);


    }

    public void statisticTeamUpdate(String playerIdx, Collection<String> beforeInTeam, Collection<String> nowInTeam) {
        List<Pet> nowInTeamPets = petCache.getInstance().getPetByIdList(playerIdx, nowInTeam);
        List<Pet> beforeTeamPets = petCache.getInstance().getPetByIdList(playerIdx, beforeInTeam);

        statisticsFightRarityUpdate(nowInTeamPets, beforeTeamPets);

        statisticsFightPetAwakeMap(nowInTeamPets, beforeTeamPets);

        statisticsFightAwakePersonMap(nowInTeamPets, beforeTeamPets);

        statisticsFightBookIdPersonMap(nowInTeamPets, beforeTeamPets);

        statisticsFightGemMap(playerIdx, nowInTeamPets, beforeTeamPets);
    }

    private void statisticsFightGemMap(String playerIdx, List<Pet> nowInTeamPets, List<Pet> beforeTeamPets) {

        petgemEntity petgemEntity = petgemCache.getInstance().getEntityByPlayer(playerIdx);
        if (petgemEntity == null) {
            return;
        }
        List<Gem> nowGems = nowInTeamPets.stream().map(pet -> petgemEntity.getGemById(pet.getGemId())).filter(Objects::nonNull).collect(Collectors.toList());
        List<Gem> beforeGems = beforeTeamPets.stream().map(pet -> petgemEntity.getGemById(pet.getGemId())).filter(Objects::nonNull).collect(Collectors.toList());

        Map<Integer, Long> nowGemRarity = getFightGemInPetRarityUpdate(nowGems, beforeGems);
        GemStatistics.getInstance().updateEquipGemRarityMap(nowGemRarity);

        int nowEnhanceLv = nowGems.stream().mapToInt(gem -> PetGemConfig.queryEnhanceLv(gem.getGemConfigId())).sum();

        int beforeEnhanceLv = beforeGems.stream().mapToInt(gem -> PetGemConfig.queryEnhanceLv(gem.getGemConfigId())).sum();

        int equipNumUpdate = nowGems.size() - beforeGems.size();

        GemStatistics.getInstance().updateEquipEnhanceLv(nowEnhanceLv - beforeEnhanceLv);
        GemStatistics.getInstance().updateTotalEquipNum(equipNumUpdate);


    }

    private Map<Integer, Long> getFightGemInPetRarityUpdate(List<Gem> nowGems, List<Gem> beforeGems) {
        Map<Integer, Long> nowGemRarity = nowGems.stream().map(Gem::getGemConfigId).map(PetGemConfig::queryRarity).filter(rarity -> rarity > 0)
                .collect(Collectors.groupingBy(a -> a, Collectors.counting()));

        Map<Integer, Long> beforeGemRarity = beforeGems.stream().map(Gem::getGemConfigId).map(PetGemConfig::queryRarity).filter(rarity -> rarity > 0)
                .collect(Collectors.groupingBy(a -> a, Collectors.counting()));

        MapUtil.subtractValue(nowGemRarity, beforeGemRarity);
        return nowGemRarity;
    }

    private void statisticsFightRarityUpdate(List<Pet> nowInTeamPets, List<Pet> beforeTeamPets) {
        Map<Integer, Long> nowRarity = nowInTeamPets.stream().filter(pet -> pet.getPetRarity() >= PetStatistics.petRarityLimit)
                .collect(Collectors.groupingBy(Pet::getPetRarity, Collectors.counting()));

        Map<Integer, Long> beforeRarity = beforeTeamPets.stream().filter(pet -> pet.getPetRarity() >= PetStatistics.petRarityLimit)
                .collect(Collectors.groupingBy(Pet::getPetRarity, Collectors.counting()));

        MapUtil.subtractValue(nowRarity, beforeRarity);

        PetStatistics.getInstance().updateFightPetRarityMap(nowRarity);
    }

    private void statisticsFightPetAwakeMap(List<Pet> nowInTeamPets, List<Pet> beforeTeamPets) {
        Map<Integer, Long> nowAwakePets = nowInTeamPets.stream().filter(pet -> pet.getPetUpLvl() > 0)
                .collect(Collectors.groupingBy(Pet::getPetUpLvl, Collectors.counting()));

        Map<Integer, Long> beforeAwakePets = beforeTeamPets.stream().filter(pet -> pet.getPetUpLvl() > 0)
                .collect(Collectors.groupingBy(Pet::getPetUpLvl, Collectors.counting()));

        MapUtil.subtractValue(nowAwakePets, beforeAwakePets);

        PetStatistics.getInstance().updateFightPetAwakeMap(nowAwakePets);
    }

    private void statisticsFightAwakePersonMap(List<Pet> nowInTeamPets, List<Pet> beforeTeamPets) {
        Map<Integer, Long> nowAwakePersonMap = nowInTeamPets.stream().map(Pet::getPetUpLvl).filter(lv -> lv > 0).distinct().collect(Collectors.toMap(a -> a, a -> 1L));

        Map<Integer, Long> beforeAwakePersonMap = beforeTeamPets.stream().map(Pet::getPetUpLvl).filter(lv -> lv > 0).distinct().collect(Collectors.toMap(a -> a, a -> 1L));

        MapUtil.subtractValue(nowAwakePersonMap, beforeAwakePersonMap);

        PetStatistics.getInstance().updateFightAwakePersonMap(nowAwakePersonMap);
    }

    private void statisticsFightBookIdPersonMap(List<Pet> nowInTeamPets, List<Pet> beforeTeamPets) {
        Map<Integer, Long> nowPetBookIdPersonMap = nowInTeamPets.stream().map(Pet::getPetBookId)
                .filter(petBookId -> PetBaseProperties.getQualityByPetId(petBookId) >= PetStatistics.petRarityLimit).distinct().collect(Collectors.toMap(a -> a, a -> 1L));

        Map<Integer, Long> beforePetBookIdPersonMap = beforeTeamPets.stream().map(Pet::getPetBookId)
                .filter(petBookId -> PetBaseProperties.getQualityByPetId(petBookId) >= PetStatistics.petRarityLimit).distinct().collect(Collectors.toMap(a -> a, a -> 1L));

        MapUtil.subtractValue(nowPetBookIdPersonMap, beforePetBookIdPersonMap);

        PetStatistics.getInstance().updateFightPetBookIdMap(nowPetBookIdPersonMap);
    }

    public boolean gemInFightPet(teamEntity teamEntity, Gem gem) {
        if (teamEntity == null || gem == null || org.apache.commons.lang.StringUtils.isEmpty(gem.getGemPet())) {
            return false;
        }
        Team dbTeam = teamEntity.getDBTeam(TeamNumEnum.TNE_Team_1);
        if (dbTeam == null) {
            return false;
        }
        return dbTeam.getLinkPetMap().containsValue(gem.getGemPet());
    }


    public void statisticByPetRarityUp(String playerId, String petId, int lastRarity, int nowRarity) {
        if (lastRarity == nowRarity || StringUtils.isEmpty(petId)) {
            return;
        }
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerId);
        if (teamEntity == null) {
            return;
        }

        Map<Integer, Long> rarityUpdate = new HashMap<>();
        if (!teamEntity.petExistInTeam(TeamNumEnum.TNE_Team_1, petId)) {
            return;
        }
        if (lastRarity >= PetStatistics.petRarityLimit) {
            rarityUpdate.put(lastRarity, -1L);
        }
        if (nowRarity >= PetStatistics.petRarityLimit) {
            rarityUpdate.put(nowRarity, 1L);
        }
        PetStatistics.getInstance().updateFightPetRarityMap(rarityUpdate);
    }


    public void statisticByPeAwakeUp(String playerId, String petId, int lastAwakeLv, int nowAwakeLv) {
        if (lastAwakeLv == nowAwakeLv || StringUtils.isEmpty(petId)) {
            return;
        }
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerId);
        if (teamEntity == null) {
            return;
        }
        Map<Integer, Long> awakeUpdate = new HashMap<>();
        if (lastAwakeLv > 0) {
            awakeUpdate.put(lastAwakeLv, -1L);
        }
        awakeUpdate.put(nowAwakeLv, 1L);

        PetStatistics.getInstance().updateOwnPetAwakeMap(awakeUpdate);


        List<String> teamPetIdxList = teamEntity.getTeamPetIdxList(TeamNumEnum.TNE_Team_1);
        if (CollectionUtils.isEmpty(teamPetIdxList)) {
            return;
        }
        if (teamPetIdxList.contains(petId)) {
            PetStatistics.getInstance().updateFightPetAwakeMap(awakeUpdate);

            List<Pet> petList = petCache.getInstance().getPetByIdList(playerId, teamPetIdxList);
            Map<Integer, Long> ownPersonMap = new HashMap<>();
            if (petList.stream().noneMatch(other -> other.getPetUpLvl() == lastAwakeLv && !other.getId().equals(petId))) {
                ownPersonMap.put(lastAwakeLv, -1L);
            }
            if (petList.stream().noneMatch(other -> other.getPetUpLvl() == nowAwakeLv && !other.getId().equals(petId))) {
                ownPersonMap.put(nowAwakeLv, 1L);
            }
            PetStatistics.getInstance().updateFightAwakePersonMap(ownPersonMap);

        }

    }

    /**
     * 得到一个新的battlePetDataList(增益团队属性):
     * 共鸣和羁绊buff放入battlePetData
     * 共鸣和羁绊,额外exBuffs 增加的战力加到每个宠物身上
     *
     * @param playerLv
     * @param petList
     * @param exBuffs  额外buff
     * @return
     */
    public List<BattlePetData> getNewBattlePetDataListWithTeamAddition(int playerLv, List<
            BattlePetData> petList, List<Integer> exBuffs) {
        if (CollectionUtils.isEmpty(petList)) {
            return Collections.emptyList();
        }
        List<Integer> petBookIds = petList.stream().map(BattlePetData::getPetCfgId).collect(Collectors.toList());
        List<Integer> bonusBuff = getTeamBattleBonusBuff(BattleSubTypeEnum.BSTE_TheWar, petBookIds, playerLv > 0);
        List<ResonancePet> resonancePetList = toResonancePetList(petList);
        List<BattlePetData> result = new ArrayList<>();
        for (BattlePetData battlePet : petList) {
            //宠物共鸣buff
            //todo 功能解锁需要判断宠物共鸣是否生效
            List<Integer> resonanceBuffs = getPetResonance(true, new ResonancePet(battlePet.getPetCfgId(), battlePet.getPetRarity())
                    , resonancePetList, petBookIds);

            List<Integer> needCalculateAbilityBuff = combineBuffList(exBuffs, bonusBuff, resonanceBuffs);

            if (CollectionUtils.isEmpty(needCalculateAbilityBuff)) {
                result.add(battlePet);
                continue;
            }
            BattlePetData.Builder battlePetBuilder = battlePet.toBuilder();
            //羁绊,阵营buff不需要放入
            if (!CollectionUtils.isEmpty(resonanceBuffs)) {
                battlePetBuilder.addAllBuffList(resonanceBuffs);
            }
            battlePetBuilder.setAbility(FightPowerCalculate.getAbilityInTeam(battlePet.getPetCfgId(), battlePet.getAbility()
                    , needCalculateAbilityBuff, petList.indexOf(battlePet)));
            result.add(battlePetBuilder.build());
        }

        return result;
    }

    private List<Integer> combineBuffList
            (List<Integer> exBuffs, List<Integer> bonusBuff, List<Integer> resonanceBuffs) {
        List<Integer> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(exBuffs)) {
            result.addAll(exBuffs);
        }
        if (!CollectionUtils.isEmpty(bonusBuff)) {
            result.addAll(bonusBuff);
        }
        if (!CollectionUtils.isEmpty(resonanceBuffs)) {
            result.addAll(resonanceBuffs);
        }
        return result;
    }

    private List<ResonancePet> toResonancePetList(List<BattlePetData> petList) {
        if (CollectionUtils.isEmpty(petList)) {
            return Collections.emptyList();
        }
        return petList.stream().map(pet -> new ResonancePet(pet.getPetCfgId(), pet.getPetRarity())).collect(Collectors.toList());
    }

    private List<ResonancePet> toResonanceList(List<Pet> petList) {
        if (CollectionUtils.isEmpty(petList)) {
            return Collections.emptyList();
        }
        return petList.stream().map(pet -> new ResonancePet(pet.getPetBookId(), pet.getPetRarity())).collect(Collectors.toList());
    }

    public static int queryCollectionCount(String playerIdx) {
        petEntity petEntity = getInstance().getEntityByPlayer(playerIdx);
        if (petEntity == null) {
            return 0;
        }
        return petEntity.getPetCollectionBuilder().getCfgIdCount();
    }

    public boolean playerRarityRest(String playerIdx) {
        petEntity petEntity = getInstance().getEntityByPlayer(playerIdx);
        if (petEntity == null) {
            return false;
        }
        return petEntity.getDbPetsBuilder().getRarityReset();
    }

    /**
     * 玩家是否拥有某个宠物
     * @param playerIdx
     * @param petCfgId
     * @return
     */
    public boolean isPlayerHavePetByCfgId(String playerIdx, int petCfgId) {
        petEntity petEntity = getInstance().getEntityByPlayer(playerIdx);
        if (petEntity==null){
            return false;
        }
        return petEntity.peekAllPetByUnModify().stream().anyMatch(e->e.getPetBookId()==petCfgId);
    }
}
