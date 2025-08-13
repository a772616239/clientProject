package server.handler.pet.gem;

import cfg.*;
import common.AbstractBaseHandler;
import common.GlobalData;
import common.SyncExecuteFunction;
import hyzNet.GameServerTcpChannel;
import hyzNet.message.MsgId;
import lombok.Getter;
import lombok.Setter;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.petgem.dbCache.petgemCache;
import model.petgem.entity.petgemEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.team.dbCache.teamCache;
import model.team.entity.teamEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.petGem.PetGemStarUpLog;
import platform.logs.statistics.GemStatistics;
import protocol.Common.Consume;
import protocol.Common.EnumFunction;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.MessageId.MsgIdEnum;
import protocol.PetMessage.CS_PetGemStarUp;
import protocol.PetMessage.Gem;
import protocol.PetMessage.GemStarUp;
import protocol.PetMessage.SC_PetGemStarUp;
import protocol.RetCodeId.RetCodeEnum;
import util.GameUtil;
import util.LogUtil;
import util.MapUtil;

import java.util.*;
import java.util.stream.Collectors;

import static protocol.MessageId.MsgIdEnum.CS_PetGemStarUp_VALUE;
import static protocol.MessageId.MsgIdEnum.SC_PetRuneUnEquip_VALUE;

@MsgId(msgId = CS_PetGemStarUp_VALUE)
public class PetGemStarUpHandler extends AbstractBaseHandler<CS_PetGemStarUp> {

    @Override
    protected CS_PetGemStarUp parse(byte[] bytes) throws Exception {
        return CS_PetGemStarUp.parseFrom(bytes);
    }

    @Override
    protected void execute(GameServerTcpChannel gsChn, CS_PetGemStarUp req, int i) {
        SC_PetGemStarUp.Builder result = SC_PetGemStarUp.newBuilder();
        // 获取当前channel对应playerId
        String playerId = String.valueOf(gsChn.getPlayerId1());
        petgemEntity petGem = petgemCache.getInstance().getEntityByPlayer(playerId);
        if (petGem == null) {
            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_UnknownError));
            gsChn.send(SC_PetRuneUnEquip_VALUE, result);
            return;
        }
        List<GemStarUp> gemList = req.getGemList();
        Set<String> needUpdatePets = new HashSet<>();
        List<String> allMaterials = new ArrayList<>();
        List<Gem> updateGems = new ArrayList<>();
        Map<Integer, Long> rarityUpdate = new HashMap<>();
        Map<Integer, Long> equipRarityUpdate = new HashMap<>();
        List<Consume> resourceConsume = new ArrayList<>();
        List<String> upGemIds = new ArrayList<>();
        List<Reward> returnSource = new ArrayList<>();
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetGemStarUp);
        SyncExecuteFunction.executeConsumer(petGem, e -> {
            RetCodeEnum errorCodeEnum = doGemsStarUp(playerId, petGem, gemList, needUpdatePets, allMaterials, updateGems, rarityUpdate, equipRarityUpdate, resourceConsume, upGemIds, returnSource);

            if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, resourceConsume, reason)) {
                errorCodeEnum = RetCodeEnum.RCE_Player_CurrencysNotEnought;
            }
            if (errorCodeEnum != RetCodeEnum.RCE_Success) {
                result.setResult(GameUtil.buildRetCode(errorCodeEnum));
                gsChn.send(MsgIdEnum.SC_PetGemStarUp_VALUE, result);
                return;
            }

            returnSource.addAll(petGem.gemInscription2Rewards(allMaterials));
            //移除材料
            petGem.removeGemByIdList(allMaterials);

            //刷新宝石
            petGem.putAllGem(updateGems);
            petGem.sendGemUpdate(updateGems);

            List<Reward> rewards = RewardUtil.mergeReward(returnSource);

            if (!CollectionUtils.isEmpty(rewards)) {
                RewardManager.getInstance().doRewardByList(playerId, rewards, reason, false);
            }

            //展示新宝石
            rewards.addAll(RewardUtil.gemToReward(updateGems));
            GlobalData.getInstance().sendDisRewardMsg(playerId, rewards, reason.getSourceEnum());

            result.setResult(GameUtil.buildRetCode(RetCodeEnum.RCE_Success));
            gsChn.send(MsgIdEnum.SC_PetGemStarUp_VALUE, result);

        });

        statisticsPlayerOwnGem(rarityUpdate, equipRarityUpdate);

        // 重新计算宠物属性，通知
        triggerUpdatePet(playerId, needUpdatePets, reason);

    }

    private void triggerUpdatePet(String playerId, Set<String> needUpdatePets, Reason reason) {
        if (!CollectionUtils.isEmpty(needUpdatePets)) {
            needUpdatePets.forEach(petId -> petCache.settlePetUpdate(playerId, petId, reason));
        }
    }

    private void statisticsPlayerOwnGem(Map<Integer, Long> rarityUpdate, Map<Integer, Long> equipRarityUpdate) {
        if (!CollectionUtils.isEmpty(rarityUpdate)) {
            GemStatistics.getInstance().updateOwnGemRarityMap(rarityUpdate);
        }
        if (!CollectionUtils.isEmpty(equipRarityUpdate)) {
            GemStatistics.getInstance().updateEquipGemRarityMap(equipRarityUpdate);
        }
    }

    private RetCodeEnum doGemsStarUp(String playerId, petgemEntity petGem, List<GemStarUp> gemList, Set<String> needUpdatePets,
                                     List<String> allMaterials, List<Gem> updateGems,
                                     Map<Integer, Long> rarityUpdate, Map<Integer, Long> equipRarityUpdate,
                                     List<Consume> resourceConsume, List<String> upGemIds,
                                     List<Reward> returnSource) {
        RetCodeEnum errorCodeEnum = null;
        boolean success = false;
        for (GemStarUp gemStarUp : gemList) {
            Gem gem = petGem.getGemById(gemStarUp.getUpGemId());
            if (gem == null) {
                errorCodeEnum = RetCodeEnum.RCE_Pet_GemNotExist;
                continue;
            }

            int curId = gem.getGemConfigId();
            PetGemConfigObject curConfig = PetGemConfig.getById(curId);

            List<String> materials = gemStarUp.getMaterialGemList().stream().distinct().collect(Collectors.toList());
            List<Gem> materialGems = gemIdsToEntity(petGem, materials);

            PetGemConfigObject nextStarConfig = PetGemConfig.getNextUpStarId(curId);

            CheckResult checkResult = beforeCheck(playerId, petGem, materialGems, gem, curId, curConfig,
                    nextStarConfig, allMaterials, resourceConsume, upGemIds);

            RetCodeEnum retCodeEnum = checkResult.getCodeEnum();
            if (RetCodeEnum.RCE_Success != retCodeEnum) {
                errorCodeEnum = retCodeEnum;
                continue;
            }
            if (!CollectionUtils.isEmpty(checkResult.getSourceReturn())) {
                returnSource.addAll(checkResult.sourceReturn);
            }
            if (!StringUtils.isEmpty(gem.getGemPet())) {
                needUpdatePets.add(gem.getGemPet());
            }
            //刷新宝石
            Gem newGem = gem.toBuilder().setGemConfigId(nextStarConfig.getId()).build();
            updateGems.add(newGem);
            upGemIds.add(newGem.getId());

            statisticsPlayerGemUpdate(playerId, rarityUpdate, equipRarityUpdate, gem, curConfig, nextStarConfig);

            // 埋点日志
            LogService.getInstance().submit(new PetGemStarUpLog(playerId, gem, materialGems, newGem, checkResult.getSourceReturn()));
            success = true;
        }
        return success ? RetCodeEnum.RCE_Success : errorCodeEnum;
    }

    private void statisticsPlayerGemUpdate(String playerId, Map<Integer, Long> rarityUpdate, Map<Integer, Long> equipRarityUpdate, Gem gem, PetGemConfigObject curConfig, PetGemConfigObject nextStarConfig) {
        if (nextStarConfig.getRarity() > curConfig.getRarity()) {
            MapUtil.add2LongMapValue(rarityUpdate, nextStarConfig.getRarity(), 1L);
            MapUtil.add2LongMapValue(rarityUpdate, curConfig.getRarity(), -1L);
            if (needStatistics(playerId, gem)) {
                MapUtil.add2LongMapValue(equipRarityUpdate, nextStarConfig.getRarity(), 1L);
                MapUtil.add2LongMapValue(equipRarityUpdate, curConfig.getRarity(), -1L);
            }
        }
    }

    private boolean needStatistics(String playerId, Gem gem) {
        if (StringUtils.isEmpty(gem.getGemPet())) {
            return false;
        }
        teamEntity teamEntity = teamCache.getInstance().getTeamEntityByPlayerId(playerId);
        if (teamEntity == null) {
            return false;
        }

        return petCache.getInstance().gemInFightPet(teamEntity, gem);
    }

    private List<Gem> gemIdsToEntity(petgemEntity petGem, List<String> materials) {
        if (CollectionUtils.isEmpty(materials)) {
            return Collections.emptyList();
        }
        return materials.stream().map(petGem::getGemById).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private CheckResult beforeCheck(String playerId, petgemEntity petGem, List<Gem> materials, Gem gem, int curId,
                                    PetGemConfigObject curConfig, PetGemConfigObject nextStarConfig,
                                    List<String> allMaterials, List<Consume> resourceConsume, List<String> updateGems) {

        CheckResult checkResult = new CheckResult();
        if (curConfig == null || nextStarConfig == null) {
            LogUtil.warn("PetGemStarUpHandler beforeCheck failed case by cant`t find star up config,star up gemCfgId:{}", gem.getGemConfigId());
            checkResult.setCodeEnum(RetCodeEnum.RSE_ConfigNotExist);
            return checkResult;
        }

        if (curConfig.getAdvanceneedplayerlv() > PlayerUtil.queryPlayerLv(playerId)) {
            checkResult.setCodeEnum(RetCodeEnum.RCE_PlayerLvNotEnough);
            return checkResult;
        }

        PetGemConfigAdvanceObject advanceConfig = PetGemConfigAdvance.getByGemConfigId(curId);
        if (advanceConfig == null) {
            LogUtil.warn("PetGemStarUpHandler getByGemConfigId is null by gemConfigId:{}", curId);
            checkResult.setCodeEnum(RetCodeEnum.RSE_ConfigNotExist);
            return checkResult;
        }
        if (updateGems.contains(gem.getId())) {
            LogUtil.warn("PetGemStarUpHandler beforeCheck failed case by materials is empty " +
                    "or upGem repeated appear,materials Size:{},upGemId:{}", materials.size(), gem.getId());
            checkResult.setCodeEnum(RetCodeEnum.RCE_ErrorParam);
            return checkResult;
        }

        int[][] advanceGemConsume = advanceConfig.getAdvancegemconsume();
        if (materials.size() != advanceGemConsume.length) {
            LogUtil.warn("PetGemStarUpHandler beforeCheck failed case by materialSize not Match");
            checkResult.setCodeEnum(RetCodeEnum.RCE_ErrorParam);
            return checkResult;
        }

        List<Reward> rewards = new ArrayList<>();

        for (int index = 0; index < advanceGemConsume.length; index++) {
            Gem curMaterial = materials.get(index);
            String curMaterialId = curMaterial.getId();
            if (curMaterial.getGemLockStatus() == 1) {
                checkResult.setCodeEnum(RetCodeEnum.RCE_Pet_PetGemLock);
                return checkResult;
            }
            if (allMaterials.contains(curMaterialId) || updateGems.contains(curMaterialId) || gem.getId().equals(curMaterialId)) {
                LogUtil.warn("PetGemStarUpHandler beforeCheck failed case by materialId repeated appear materialId:{}", curMaterialId);
                checkResult.setCodeEnum(RetCodeEnum.RCE_ErrorParam);
                return checkResult;
            }
            allMaterials.add(curMaterialId);
            Gem material = petGem.getGemById(curMaterialId);
            if (material == null) {
                LogUtil.warn("PetGemStarUpHandler beforeCheck failed case by material not exists");
                checkResult.setCodeEnum(RetCodeEnum.RCE_ErrorParam);
                return checkResult;
            }
            if (material.getGemLockStatus() == 1 || !StringUtils.isEmpty(material.getGemPet())) {
                LogUtil.warn("PetGemStarUpHandler beforeCheck failed case by material status lockStatus:{},equip:{}", index, material.getGemLockStatus(), !StringUtils.isEmpty(material.getGemPet()));
                checkResult.setCodeEnum(RetCodeEnum.RCE_ErrorParam);
                return checkResult;
            }
            PetGemConfigObject materialConfig = PetGemConfig.getById(material.getGemConfigId());
            if (materialConfig == null) {
                checkResult.setCodeEnum(RetCodeEnum.RSE_ConfigNotExist);
                return checkResult;
            }
            if (materialConfig.getRarity() != advanceGemConsume[index][0] || materialConfig.getStar() != advanceGemConsume[index][1]) {
                LogUtil.warn("PetGemStarUpHandler beforeCheck failed case by material rarity or star notMatch,materialIndex:{}", index, materialConfig);
                checkResult.setCodeEnum(RetCodeEnum.RCE_ErrorParam);
                return checkResult;
            }
            PetGemConfigLeveObject lvConfig = PetGemConfigLeve.getByLv(materialConfig.getLv());
            if (lvConfig != null) {
                rewards.addAll(RewardUtil.parseRewardIntArrayToRewardList(lvConfig.getGemsale()));
            }
        }

        //资源最后merge判断是否足够 防止当前进阶不成功导致多余消耗
        List<Consume> consumes = ConsumeUtil.parseToConsumeList(advanceConfig.getAdvancesourceconsume());
        List<Consume> mergeConsume = ConsumeUtil.mergeConsumeByTypeAndId(resourceConsume, consumes);
        resourceConsume.clear();
        resourceConsume.addAll(mergeConsume);
        if (!ConsumeManager.getInstance().materialIsEnoughByList(playerId, resourceConsume)) {
            checkResult.setCodeEnum(RetCodeEnum.RCE_MatieralNotEnough);
            return checkResult;
        }
        checkResult.setSourceReturn(rewards);
        checkResult.setCodeEnum(RetCodeEnum.RCE_Success);
        return checkResult;
    }

    @Setter
    @Getter
    private class CheckResult {
        private RetCodeEnum codeEnum;
        private List<Reward> sourceReturn;
    }

    @Override
    public EnumFunction belongFunction() {
        return EnumFunction.PetGemRefine;
    }


    @Override
    public void doClosedActive(GameServerTcpChannel gsChn, int codeNum) {
        protocol.RetCodeId.RetCode.Builder retCode = GameUtil.buildRetCode(RetCodeEnum.RSE_Function_AbnormalMaintenance);
        gsChn.send(protocol.MessageId.MsgIdEnum.SC_PetGemStarUp_VALUE, SC_PetGemStarUp.newBuilder().setResult(retCode));
    }


}
