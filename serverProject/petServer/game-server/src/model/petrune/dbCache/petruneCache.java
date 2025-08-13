/*CREATED BY TOOL*/

package model.petrune.dbCache;

import annotation.annationInit;
import cfg.GameConfig;
import cfg.PetRuneExp;
import cfg.PetRuneExpObject;
import cfg.PetRuneKindCfg;
import cfg.PetRuneKindCfgObject;
import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import cfg.PetRuneRairtyUpCfg;
import cfg.PetRuneRairtyUpCfgObject;
import cfg.PetRuneUpConsume;
import cfg.PetRuneUpConsumeObject;
import cfg.PetRuneWorth;
import cfg.PetRuneWorthObject;
import cfg.VIPConfig;
import common.GameConst;
import common.GameConst.WarPetUpdate;
import common.SyncExecuteFunction;
import datatool.StringHelper;
import db.config.AppContext;
import db.core.BaseDAO;
import db.entity.BaseEntity;
import model.base.cache.IbaseCache;
import model.base.cache.baseCache;
import model.base.cache.baseUpdateCache;
import model.consume.ConsumeManager;
import model.consume.ConsumeUtil;
import model.pet.dbCache.petCache;
import model.petrune.cache.petruneUpdateCache;
import model.petrune.entity.PetRunePropertyRandom;
import model.petrune.entity.PetRuneUpResult;
import model.petrune.entity.petruneEntity;
import model.player.util.PlayerUtil;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import platform.logs.LogService;
import platform.logs.ReasonManager;
import platform.logs.ReasonManager.Reason;
import platform.logs.entity.DailyDateLog;
import platform.logs.entity.PetRuneLvlLog;
import platform.logs.entity.PetRuneMakeLog;
import platform.logs.statistics.RuneStatistics;
import protocol.Common;
import protocol.Common.Consume;
import protocol.Common.Reward;
import protocol.Common.RewardSourceEnum;
import protocol.Common.RewardTypeEnum;
import protocol.PetMessage;
import protocol.PetMessage.Rune;
import protocol.PetMessage.RuneProperties;
import protocol.PetMessage.RunePropertieyEntity;
import protocol.PetMessage.RunePropertieyEntity.Builder;
import protocol.RetCodeId.RetCodeEnum;
import protocol.TargetSystem.TargetTypeEnum;
import util.EventUtil;
import util.LogUtil;
import util.MapUtil;
import util.RandomUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@annationInit(value = "petruneCache", methodname = "load")
public class petruneCache extends baseCache<petruneCache> implements IbaseCache {
    /******************* MUST HAVE ********************************/

    private static petruneCache instance = null;

    public static petruneCache getInstance() {

        if (instance == null) {
            instance = new petruneCache();
        }
        return instance;

    }

    @Override
    public String getDaoName() {

        return "petruneDAO";
    }

    @Override
    public BaseDAO getDao() {

        return AppContext.getBean("petruneDAO");
    }

    @Override
    public void load(baseCache o) {

        if (instance == null) {
            instance = (petruneCache) o;
        }
        super.loadAllFromDb();

    }

    public static void put(petruneEntity v) {

        getInstance().putBaseEntity(v);

    }

    public static petruneEntity getByIdx(String idx) {
        if (StringHelper.isNull(idx)) {
            return null;
        }


        BaseEntity v = getInstance().getBaseEntityByIdx(idx);
        if (v == null) {
            return null;
        }

        return (petruneEntity) v;

    }

    @Override
    public Map<String, BaseEntity> getAll() {

        return getInstance()._ix_id;

    }

    @Override
    public baseUpdateCache getUpdateCache() {

        return petruneUpdateCache.getInstance();

    }

    public static void remove(String idx) {

        getInstance().BaseRemove(idx);

    }

    @Override
    public void putToMem(BaseEntity v) {
        petruneEntity entity = (petruneEntity) v;
        if (entity.getPlayeridx() != null) {
            runeEntity.put(entity.getPlayeridx(), entity);
        }
    }

    /***************************分割**********************************/

    /**
     * <playerIdx, entity>
     */
    private final Map<String, petruneEntity> runeEntity = new ConcurrentHashMap<>();

    public petruneEntity getEntityByPlayer(String playerId) {
        if (StringHelper.isNull(playerId)) {
            return null;
        }

        petruneEntity entity = runeEntity.get(playerId);
        if (entity == null && PlayerUtil.playerIsExist(playerId)) {
            entity = new petruneEntity(playerId);
            entity.putToCache();
            return entity;
        }

        return entity;
    }

    public Rune getPlayerRune(String playerId, String runeIdx) {
        if (StringHelper.isNull(playerId) || StringHelper.isNull(runeIdx)) {
            return null;
        }
        petruneEntity cache = getEntityByPlayer(playerId);
        if (cache != null) {
            return cache.getRuneById(runeIdx);
        }
        return null;
    }

    /**
     * 判断符文是否可以移除
     *
     * @param rune
     * @return
     */
    public static boolean runeCanRemove(String playerIdx, Rune rune) {
        if (rune == null) {
            return true;
        }

        if (rune.getRuneLockStatus() == 1) {
            return false;
        }

        return petCache.getInstance().getPetById(playerIdx, rune.getRunePet()) == null;
    }

    /**
     * 获取宠物上装备的符文
     *
     * @param playerId
     * @param petId
     * @return
     */
    public List<Rune> getPetRune(String playerId, String petId) {
        petruneEntity entity = getEntityByPlayer(playerId);
        if (entity == null) {
            return null;
        }

        return entity.getRuneListByPet(petId);
    }

    public boolean capacityEnough(String playerId, int occupancy) {
        return getRemainCapacity(playerId) >= occupancy;
    }

    public int getRemainCapacity(String playerId) {
        petruneEntity cache = getEntityByPlayer(playerId);
        if (cache != null) {
            return Math.max(0, cache.getCapacity() - cache.getOccupancy());
        }
        return 0;
    }

    public RetCodeEnum beforeCheck(List<String> materialIdList, petruneEntity cache, String runeId) {
        if (cache == null || runeId == null) {
            return RetCodeEnum.RCE_ErrorParam;
        }
        // 待升级符文
        // 参数校验
        Rune rune = cache.getRuneById(runeId);
        if (rune == null) {
            return RetCodeEnum.RCE_Pet_RuneNotExist;
        }
        // 缺符文是否锁定检查
        // 升级材料
        List<Rune> materialList = cache.getRuneByList(materialIdList);

        // 待升级符文是否包含在材料中
        if (materialIdList.contains(runeId)) {
            return RetCodeEnum.RCE_Pet_RuneMaterailRuneError;
        }
        // 申请材料和查询材料数量不一致
        if (materialList != null && materialList.size() != materialIdList.size()) {
            return RetCodeEnum.RCE_Pet_RuneMaterailRuneError;
        }
        // 符文是否升级到最大
        if (PetRuneExp.queryRuneMaxLv(PetRuneProperties.getRuneRarity(rune.getRuneBookId())) <= rune.getRuneLvl()) {
            return RetCodeEnum.RCE_Pet_RuneLvlMaxError;
        }
        return RetCodeEnum.RCE_Success;
    }

    public List<Rune> makeRuneList(String playerId, PetMessage.CS_PetRuneMake req){
        List<Rune> reNewRuneList = new ArrayList<Rune>();

        petruneEntity cache = getEntityByPlayer(playerId);

        Map<Integer, Long> rarityUpdateMap = new HashMap<>();
        Map<Integer, Long> equipRarityUpdateMap = new HashMap<>();

        List<String> toNotifyCliDelRuneList = new ArrayList<>();
        List<String> srcRuneIdList = req.getIdList();
        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            for (String srcRuneId :  srcRuneIdList){
                Rune rune = this.makeRune(playerId , srcRuneId, cache, rarityUpdateMap, equipRarityUpdateMap, toNotifyCliDelRuneList);
                if(rune != null){
                    reNewRuneList.add(rune);
                }
            }
            statisticsPlayerRune(rarityUpdateMap, equipRarityUpdateMap);
        });
        petruneEntity.sendRuneRemove(playerId, toNotifyCliDelRuneList);

        return reNewRuneList;
    }

    private Rune makeRune(String playerId, String runeId,petruneEntity cache, Map<Integer, Long> rarityUpdateMap,
                          Map<Integer, Long> equipRarityUpdateMap, List<String> toNotifyCliDelRuneList) {
        Rune rune = cache.getRuneById(runeId);
        if (rune == null) {
            return null;
        }
        int cfgId = rune.getRuneBookId();
        //////////可以抽出单独做一个findNextRune的方法
        PetRunePropertiesObject runeBaseCfg = PetRuneProperties.getByRuneid(cfgId);
        if(runeBaseCfg == null){
            LogUtil.error("make rune not found PetRunePropertiesObject,PID={},runCfgId={}",playerId,runeId);
            return null;
        }

        int suitId = runeBaseCfg.getRunesuit();
        PetRuneKindCfgObject runeKindCfg = PetRuneKindCfg.getByRuneSuitId(suitId);
        if(runeKindCfg == null){
            LogUtil.error("make rune not found PetRuneKindCfgObject,PID={},runCfgId={},suitId={}",playerId,runeId,suitId);
            return null;
        }

        int kind = runeKindCfg.getRunekind();
        int rarity = runeBaseCfg.getRunerarity();
        PetRuneRairtyUpCfgObject runeRarityUpCfg = PetRuneRairtyUpCfg.getCfgByKindAndRairty(kind,rarity);
        if(runeRarityUpCfg == null){
            LogUtil.error("make rune not found PetRuneRairtyUpCfgObject,PID={},runCfgId={},suitId={},kind={},rarity={}",playerId,runeId,suitId,kind,rarity);
            return null;
        }
        int nextRuneSuitId = PetRuneKindCfg.getNextRuneSuitId(suitId);
        if (nextRuneSuitId <= 0) {
            LogUtil.error("make rune err NextRuneSuitId not found,PID={},runCfgId={},suitId={},kind={},rarity={}",playerId,runeId,suitId,kind,rarity);
            return null;
        }
        //判断物品是否足够
        int[][] consumes = runeRarityUpCfg.getConsumes();
        List<Consume> consumeList = ConsumeUtil.parseToConsumeList(consumes);
        if (!ConsumeManager.getInstance().materialIsEnoughByList(playerId, consumeList)) {
            LogUtil.error("make rune err material not enough,PID={},runCfgId={},suitId={},kind={},rarity={}",playerId,runeId,suitId,kind,rarity);
            return null;
        }
        //打造
        PetRunePropertiesObject highRarityRuneCfg = PetRuneProperties.getRarityRune(rarity + 1, nextRuneSuitId, runeBaseCfg.getRunetype());
        if (highRarityRuneCfg == null) {
            LogUtil.error("make rune err highRarityRune not found,PID={},runCfgId={},suitId={},kind={},rarity={}",playerId,runeId,suitId,kind,rarity);
            return null;
        }

        Rune.Builder highRarityRuneBuilder = RandomUtil.makeHigherRarityRune(rune, highRarityRuneCfg);
        //扣物品
        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_MakeRune);
        if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, consumeList, reason)) {
            LogUtil.error("make rune err material not enough,PID={},runCfgId={},suitId={},kind={},rarity={}",playerId,runeId,suitId,kind,rarity);
            return null;
        }
        //替换
        Rune highRarityRune = highRarityRuneBuilder.build();
        cache.removeRuneWithoutSendCli(rune);
        toNotifyCliDelRuneList.add(rune.getId());
        cache.putRune(highRarityRune);
        // 埋点日志
        LogService.getInstance().submit(new PetRuneMakeLog(playerId, rune, highRarityRune, consumeList));
        settleRuneUpdate(playerId, highRarityRuneBuilder, RewardSourceEnum.RSE_MakeRune);
        statisticsPlayerRuneUpdate(rarityUpdateMap, equipRarityUpdateMap, highRarityRune, runeBaseCfg, highRarityRuneCfg);

        return highRarityRune;
    }

    private void statisticsPlayerRuneUpdate(Map<Integer, Long> rarityUpdate, Map<Integer, Long> equipRarityUpdate,
                                            Rune rune, PetRunePropertiesObject curCfg, PetRunePropertiesObject nextCfg) {
        if (nextCfg.getRunerarity() != curCfg.getRunerarity()) {
            MapUtil.add2LongMapValue(rarityUpdate, nextCfg.getRunerarity(), 1L);
            MapUtil.add2LongMapValue(rarityUpdate, curCfg.getRunerarity(), -1L);
            if (!StringUtils.isEmpty(rune.getRunePet())) {
                MapUtil.add2LongMapValue(equipRarityUpdate, nextCfg.getRunerarity(), 1L);
                MapUtil.add2LongMapValue(equipRarityUpdate, curCfg.getRunerarity(), -1L);
            }
        }
    }

    private void statisticsPlayerRune(Map<Integer, Long> rarityUpdate, Map<Integer, Long> equipRarityUpdate) {
        if (!CollectionUtils.isEmpty(rarityUpdate)) {
            RuneStatistics.getInstance().updateOwnRarityMap(rarityUpdate);
        }
        if (!CollectionUtils.isEmpty(equipRarityUpdate)) {
            RuneStatistics.getInstance().updateEquipRarityMap(equipRarityUpdate);
        }
    }

    public PetRuneUpResult runeLvlUp(String playerId, PetMessage.CS_PetRuneLvlUp req) {
        PetRuneUpResult result = new PetRuneUpResult();

        List<String> materialIdList = req.getMaterialRuneList();
        petruneEntity cache = getEntityByPlayer(playerId);
        String runeId = req.getUpRuneId();
        RetCodeEnum codeEnum = beforeCheck(materialIdList, cache, runeId);
        if (codeEnum != RetCodeEnum.RCE_Success) {
            result.setCode(codeEnum);
            return result;
        }

        Rune.Builder rune = cache.getRuneById(req.getUpRuneId()).toBuilder();
        List<Rune> materialList = cache.getRuneByList(materialIdList);
        int originLvl = rune.getRuneLvl();

        PetRunePropertiesObject runeConfig = PetRuneProperties.getByRuneid(rune.getRuneBookId());
        if (runeConfig == null) {
            result.setCode(RetCodeEnum.RSE_ConfigNotExist);
            return result;
        }
        PetRuneExpObject runeExpConfig = PetRuneExp.getByRarityAndLvlAndType(runeConfig.getRunerarity(), rune.getRuneLvl(), runeConfig.getRunetype());
        if (runeExpConfig == null) {
            result.setCode(RetCodeEnum.RSE_ConfigNotExist);
            return result;
        }
        boolean basicLvUp = runeExpConfig.getNextlvlexp() <= 0;
        // 低级升级
        if (basicLvUp) {
            if (!basicLvUp(playerId, req, result, cache, rune, originLvl)) {
                return result;
            }
        } else {
            // 高级升级
            if (!advanceLvUp(playerId, req, result, cache, rune, materialList)) {
                return result;
            }
        }
        settleRuneUpdate(playerId, rune);
        // 埋点日志
        LogService.getInstance().submit(new PetRuneLvlLog(playerId, rune.getRuneBookId(), originLvl, rune.getRuneLvl(), materialList, result.getConsume()));
        // 目标：任意符文强化到x级
        EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_RuneIntensifyLv, rune.getRuneLvl(), 0);
        // 目标：强化符文次数
        EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_CumuRuneIntensify, rune.getRuneLvl() - originLvl, 0);
        result.setSuccess(true);
        result.setRune(rune);
        return result;
    }

    public void settleRuneUpdate(String playerId, Rune.Builder rune) {
        settleRuneUpdate(playerId, rune, RewardSourceEnum.RSE_PetRuneLvUp);
    }

    public void settleRuneUpdate(String playerId, Rune.Builder rune, RewardSourceEnum rse) {
        //更新携带符文宠物的属性
        if (StringUtils.isEmpty(rune.getRunePet())) {
            return;
        }
        petCache.getInstance().refreshPetProperty(playerId, rune.getRunePet(), ReasonManager.getInstance().borrowReason(rse), true);
        //通知战戈宠物更新
        EventUtil.triggerWarPetUpdate(playerId, rune.getRunePet(), WarPetUpdate.MODIFY);
    }

    private boolean basicLvUp(String playerId, PetMessage.CS_PetRuneLvlUp req, PetRuneUpResult result, petruneEntity cache, Rune.Builder rune, int originLvl) {
        PetRuneUpConsumeObject consumeCfg = null;
        List<Consume> consumes = null;
        List<Consume> tempConsumes = null;
        int upLvCount = 0;
        int petRuneMaxLvl = GameConfig.getById(GameConst.CONFIG_ID).getPetiintensifyrunemaxllvl();
        int canMaxLvUp = req.getOneClickUpgrade() ? petRuneMaxLvl - rune.getRuneLvl() : 1;
        while (canMaxLvUp > upLvCount && (consumeCfg = PetRuneUpConsume.getByCfgIdAndLvl(rune.getRuneBookId(), originLvl + upLvCount)) != null) {
            tempConsumes = ConsumeUtil.mergeConsumeByTypeAndId(tempConsumes, new ArrayList(Collections.singleton(ConsumeUtil.parseConsume(consumeCfg.getConsume()))));
            if (!ConsumeManager.getInstance().materialIsEnoughByList(playerId, tempConsumes)) {
                EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_PopMission_MaterialNotEnough,1,1);
                break;
            }
            consumes = tempConsumes;
            upLvCount++;
        }
        if (upLvCount == 0) {
            result.setCode(consumeCfg == null ? RetCodeEnum.RSE_ConfigNotExist : RetCodeEnum.RCE_Player_CurrencysNotEnought);
            return false;
        }

        if (ConsumeManager.getInstance().consumeMaterialByList(playerId, consumes, ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetRuneLvUp))) {
            int finalUpLvCount = upLvCount;
            SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
                runLvUpRefreshProperty(rune, finalUpLvCount);
                cache.putRune(rune.build());
            });
        } else {
            // 材料不足
            result.setCode(RetCodeEnum.RCE_Player_CurrencysNotEnought);
            return false;
        }
        //初级升级都是相同的,累加在第一个consume
        result.setConsume(consumes);
        return true;
    }

    private void runLvUpRefreshProperty(Rune.Builder rune, int upLvCont) {
        if (upLvCont < 1) {
            return;
        }
        for (int i = 0; i < upLvCont; i++) {
            rune.setRuneLvl(rune.getRuneLvl() + 1);
            refreshRuneByLvUp(rune);
        }
    }

    private boolean advanceLvUp(String playerId, PetMessage.CS_PetRuneLvlUp req, PetRuneUpResult result, petruneEntity cache, Rune.Builder rune, List<Rune> materialList) {

        List<PetMessage.RuneExp> materialRuneExp = req.getMaterialRuneExpList();
        if (CollectionUtils.isEmpty(materialRuneExp) && CollectionUtils.isEmpty(materialList)) {
            result.setCode(RetCodeEnum.RCE_ErrorParam);
            return false;
        }

        int originalLv = rune.getRuneLvl();

        // 获取材料提供经验
        int materialExp = getMaterialExp(materialList, materialRuneExp);

        //材料中符文石
        int materialRuneStoneReturnNum = material2RuneStone(materialList);

        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            // 升级符文，出错则返回
            refreshRuneByLvUp(rune, rune.getRuneExp() + materialExp, playerId, result);
        });

        List<Consume> consumes = parseUpConsume(rune, originalLv, materialRuneExp);

        //材料中符文石大于0 需要修正消耗中符文石个数,并且多余符文石需要返还
        if (materialRuneStoneReturnNum > 0) {
            Optional<Consume> stoneConsume = consumes.stream().filter(consume -> consume.getRewardType() == RewardTypeEnum.RTE_Item && consume.getId() == GameConst.RuneStoneItemId).findAny();
            if (stoneConsume.isPresent()) {
                Consume consume = stoneConsume.get();
                int realCostCount = consume.getCount() - materialRuneStoneReturnNum;
                consumes.remove(consume);
                if (realCostCount > 0) {
                    consumes.add(consume.toBuilder().setCount(realCostCount).build());
                }
                materialRuneStoneReturnNum = Math.max(0, materialRuneStoneReturnNum - consume.getCount());
            }
        }


        Reason reason = ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetRuneLvUp);
        if (!ConsumeManager.getInstance().consumeMaterialByList(playerId, consumes, reason)) {
            EventUtil.triggerUpdateTargetProgress(playerId, TargetTypeEnum.TTE_PopMission_MaterialNotEnough,1,1);
            result.setCode(RetCodeEnum.RCE_MatieralNotEnough);
            return false;
        }

        doRuneMaterialReturn(playerId,reason,materialRuneStoneReturnNum,materialList);

        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            cache.putRune(rune.build());
            // 扣除符文材料(包含通知客户端删除)
            cache.removeRuneByRuneList(materialList);
        });
        result.setConsume(consumes);
        return true;
    }

    private void doRuneMaterialReturn(String playerId, Reason reason, int materialRuneStoneReturnNum, List<Rune> runeList) {
        List<Reward> rewards = new ArrayList<>();
        //符文石返还
        if (materialRuneStoneReturnNum > 0) {
            rewards.add(Reward.newBuilder().setCount(materialRuneStoneReturnNum).setId(GameConst.RuneStoneItemId).setRewardType(RewardTypeEnum.RTE_Item).build());
        }
        //祝福返还
        if (!CollectionUtils.isEmpty(runeList)) {
            for (Rune rune : runeList) {
                if (rune.getBlessRating().getConsumesCount() > 0) {
                    rewards.addAll(ConsumeUtil.parseConsumeToReward(rune.getBlessRating().getConsumesList()));
                }
            }
        }

        RewardManager.getInstance().doRewardByList(playerId, rewards, reason, true);
    }

    private int material2RuneStone(List<Rune> materialList) {
        if (CollectionUtils.isEmpty(materialList)) {
            return 0;
        }
        int num = 0;
        for (Rune rune : materialList) {
            PetRuneWorthObject sale = PetRuneWorth.getByRarityAndLevel(PetRuneProperties.getByRuneid(rune.getRuneBookId()).getRunerarity(), rune.getRuneLvl());
            if (sale == null) {
                LogUtil.error("material2RuneStone,PetRuneWorth config is null by rarity:{} and lv:{}", PetRuneProperties.getByRuneid(rune.getRuneBookId()).getRunerarity(), rune.getRuneLvl());
                continue;
            }
            int[][] runeSale = sale.getRunesale();
            if (ArrayUtils.isEmpty(runeSale)) {
                continue;
            }
            for (int[] worthItemConfig : runeSale) {
                if (ArrayUtils.isEmpty(worthItemConfig)) {
                    continue;
                }
                if (configContainsRuneStone(worthItemConfig)) {
                    num += worthItemConfig[2];
                }
            }
        }
        return num;
    }

    private boolean configContainsRuneStone(int[] worth) {
        return worth.length >= 3 && worth[0] == RewardTypeEnum.RTE_Item_VALUE && worth[1] == GameConst.RuneStoneItemId;
    }

    private List<Consume> parseUpConsume(Rune.Builder rune, int originalLv, List<PetMessage.RuneExp> materialRuneExp) {

        List<Consume> runeExpConsume = getRuneExpConsume(materialRuneExp);
        List<Consume> result = new ArrayList<>(runeExpConsume);
        for (int lv = originalLv; lv < rune.getRuneLvl(); lv++) {
            PetRuneUpConsumeObject upConsumeConfig = PetRuneUpConsume.getByCfgIdAndLvl(rune.getRuneBookId(), lv);
            if (upConsumeConfig != null && !ArrayUtils.isEmpty(upConsumeConfig.getConsume())) {
                result.add(ConsumeUtil.parseConsume(upConsumeConfig.getConsume()));
            }
        }
        return ConsumeUtil.mergeConsume(result);
    }

    public static List<Reward> runeExpToReward(int totalExp) {
        if (totalExp <= 0) {
            return Collections.emptyList();
        }
        List<Reward> rewardList = new ArrayList<>();
        int[][] expChangeRate = GameConfig.getById(GameConst.CONFIG_ID).getRuneexpexchangerate();
        for (int i = expChangeRate.length - 1; i >= 0; i--) {
            if (expChangeRate[i].length < 2) {
                LogUtil.error("GameConfig.runeExpExchangeRate config error by length not enough");
            }
            int num = totalExp / expChangeRate[i][1];
            if (num <= 0) {
                continue;
            }
            totalExp = totalExp % expChangeRate[i][1];
            rewardList.add(RewardUtil.parseReward(RewardTypeEnum.RTE_Item, expChangeRate[i][0], num));
        }
        return rewardList;
    }


    private List<Consume> getRuneExpConsume(List<PetMessage.RuneExp> materialRuneExp) {
        if (CollectionUtils.isEmpty(materialRuneExp)) {
            return Collections.emptyList();
        }

        List<Consume> consumes = new ArrayList<>();
        materialRuneExp.forEach(runeExp -> {
            Consume runExpConsume = ConsumeUtil.parseConsume(new int[]{RewardTypeEnum.RTE_Item_VALUE, runeExp.getId(), runeExp.getNum()});
            consumes.add(runExpConsume);
        });
        return consumes;
    }

    /**
     * 计算材料符文提供经验值
     *
     * @param materialList    材料符文列表，提供经验向下取整
     * @param materialRuneExp
     * @return 经验值
     */
    private int getMaterialExp(List<Rune> materialList, List<PetMessage.RuneExp> materialRuneExp) {
        int result = 0;
        if (!CollectionUtils.isEmpty(materialList)) {
            for (Rune rune : materialList) {
                result += calculateRuneTotalExp(rune);
            }
        }
        result += runeExpToLUpExp(materialRuneExp);
        return result;
    }

    public int calculateRuneTotalExp(Rune rune) {
        if (rune == null) {
            return 0;
        }
        PetRunePropertiesObject baseConfig = PetRuneProperties.getByRuneid(rune.getRuneBookId());
        if (baseConfig == null) {
            return 0;
        }
        // 计算符文当前经验转换
        PetRuneExpObject runeCfg = PetRuneExp.getByRarityAndLvlAndType(baseConfig.getRunerarity(), rune.getRuneLvl() - 1, baseConfig.getRunetype());
        if (runeCfg == null) {
            return 0;
        }
        // 当前经验 = 上一级累计经验+本级已有经验
        return (int) Math.floor(runeCfg.getAccumexp() + rune.getRuneExp() /
                ((double) GameConfig.getById(GameConst.CONFIG_ID).getRuneexpaddproportion() / GameConst.runeExpAddProportionMagnification));
    }

    /**
     * 符文经验转换为升级经验
     *
     * @param materialRuneExp
     * @return
     */
    private int runeExpToLUpExp(List<PetMessage.RuneExp> materialRuneExp) {
        if (CollectionUtils.isEmpty(materialRuneExp)) {
            return 0;
        }
        int runExps = 0;
        for (PetMessage.RuneExp runeExp : materialRuneExp) {
            int[][] rateConfigs = GameConfig.getById(GameConst.CONFIG_ID).getRuneexpexchangerate();
            for (int[] ints : rateConfigs) {
                if (ints[0] == runeExp.getId()) {
                    runExps += runeExp.getNum() * ints[1];
                }
            }
        }
        return runExps;
    }

    /**
     * @param rune     符文
     * @param totalExp 总经验值
     * @param playerId
     * @param
     */
    private void refreshRuneByLvUp(Rune.Builder rune, int totalExp, String playerId, PetRuneUpResult result) {
        if (rune == null) {
            return;
        }

        PetRunePropertiesObject runeBaseConfig = PetRuneProperties.getByRuneid(rune.getRuneBookId());
        if (runeBaseConfig == null) {
            return;
        }
        int maxLv = PetRuneExp.queryRuneMaxLv(runeBaseConfig.getRunerarity());
        PetRuneExpObject config = PetRuneExp.getByRarityAndLvlAndType(runeBaseConfig.getRunerarity(), rune.getRuneLvl(), runeBaseConfig.getRunetype());
        if (config == null) {
            return;
        }
        // 升到满级
        if (maxLv <= rune.getRuneLvl()) {
            rune.setRuneExp(0).build();
            result.setConvertRuneExp(remainExp2RuneExpAndDoReward(totalExp, playerId));
        } else if (totalExp >= config.getNextlvlexp()) {
            // 升级
            rune.setRuneLvl(rune.getRuneLvl() + 1).build();
            totalExp -= config.getNextlvlexp();
            refreshRuneByLvUp(rune);
            refreshRuneByLvUp(rune, totalExp, playerId, result);
        } else {
            // 经验不足升级
            rune.setRuneExp(totalExp).build();
        }

    }

    private List<PetMessage.RuneExp> remainExp2RuneExpAndDoReward(int totalExp, String playerId) {
        if (totalExp < 0) {
            return Collections.emptyList();
        }
        List<PetMessage.RuneExp> convertRuneExp = expToRuneExp(totalExp);
        List<Common.Reward> rewards = runeExpList2RewardList(convertRuneExp);
        RewardManager.getInstance().doRewardByList(playerId, rewards,
                ReasonManager.getInstance().borrowReason(RewardSourceEnum.RSE_PetRuneLvUp), false);
        return convertRuneExp;
    }

    private List<Common.Reward> runeExpList2RewardList(List<PetMessage.RuneExp> convertRuneExp) {
        List<Common.Reward> rewards = new ArrayList<>();
        for (PetMessage.RuneExp runeExp : convertRuneExp) {
            Common.Reward reward = RewardUtil.parseReward(RewardTypeEnum.RTE_Item, runeExp.getId(), runeExp.getNum());
            rewards.add(reward);
        }
        return rewards;
    }

    /**
     * 经验转符文经验
     *
     * @param totalExp
     * @return
     */
    private List<PetMessage.RuneExp> expToRuneExp(int totalExp) {
        if (totalExp <= 0) {
            return Collections.emptyList();
        }
        List<PetMessage.RuneExp> convertRunExp = new ArrayList<>();
        int[][] configs = GameConfig.getById(GameConst.CONFIG_ID).getRuneexpexchangerate();
        for (int i = configs.length - 1; i >= 0; i--) {

            if (configs[i].length < 2 || configs[i][1] <= 0) {
                LogUtil.error("gameConfig RuneExpExchangeRate 配置错误");
                continue;
            }
            int num = totalExp / configs[i][1];
            if (num > 0) {
                totalExp -= configs[i][1] * num;
                PetMessage.RuneExp.Builder runeExp = PetMessage.RuneExp.newBuilder().setId(configs[i][0]).setNum(num);

                convertRunExp.add(runeExp.build());
            }

        }
        return convertRunExp;
    }

    /**
     * 符文升级属性刷新
     *
     * @param rune 符文（等级+1后）
     * @return
     */
    private void refreshRuneByLvUp(Rune.Builder rune) {
        PetRuneExp.refreshBaseProperty(rune);
        // 读取配置判断升级是否影响符文词条
        PetRunePropertiesObject baseConfig = PetRuneProperties.getByRuneid(rune.getRuneBookId());
        if (baseConfig == null) {
            return;
        }
        PetRuneExpObject config = PetRuneExp.getByRarityAndLvlAndType(baseConfig.getRunerarity(), rune.getRuneLvl() - 1, baseConfig.getRunetype());
        if (config == null) {
            return;
        }
        if (config.getNewproperties() == 1) {
            // 获得随机属性
            setRandomPropertiesByLvlUp(rune);
        } else if (config.getNewproperties() == 2) {
            // 强化属性
            enhancePropertyByLvlUp(rune);
        }
    }

    /**
     * 符文升级获取随机属性
     *
     * @param rune 符文
     * @return 操作结果
     */
    private void setRandomPropertiesByLvlUp(Rune.Builder rune) {
        // 获得随机新属性
        PetRunePropertyRandom newProperty = RandomUtil.randomExProperty(PetRuneProperties.getByRuneid(rune.getRuneBookId()), rune);
        if (newProperty == null) {
            LogUtil.error("setRandomPropertiesByLvlUp error by newProperty is null,runeId:{}", rune.getId());
            return;
        }
        // 写入额外属性2中
        RuneProperties runeExProperty = rune.getRuneExProperty() == null ? RuneProperties.newBuilder().build() : rune.getRuneExProperty();
        RunePropertieyEntity.Builder property = RunePropertieyEntity.newBuilder();
        property.setPropertyType(newProperty.getPropertyType());
        property.setPropertyValue(newProperty.getPropertyValue());
        runeExProperty = runeExProperty.toBuilder().addProperty(property).build();
        rune.setRuneExProperty(runeExProperty);
    }


    /**
     * 符文升级强化符文属性
     *
     * @param rune
     */
    public void enhancePropertyByLvlUp(Rune.Builder rune) {
        PetRunePropertiesObject baseConfig = PetRuneProperties.getByRuneid(rune.getRuneBookId());
        if (baseConfig == null) {
            return;
        }
        PetRuneExpObject config = PetRuneExp.getByRarityAndLvlAndType(baseConfig.getRunerarity(), rune.getRuneLvl(), baseConfig.getRunetype());
        if (config == null) {
            return;
        }
        List<Builder> propertyList = rune.getRuneExPropertyBuilder().getPropertyBuilderList();
        for (Builder lastProperty : propertyList) {
            for (int[] configProperty : config.getExbaseproperties()) {
                if (configProperty.length > 1 && configProperty[0] == lastProperty.getPropertyType()) {
                    int newValue = RandomUtil.getRandomValue(configProperty[2], configProperty[3]);
                    if (newValue > lastProperty.getPropertyValue()) {
                        lastProperty.setPropertyValue(newValue);
                    }
                }
            }
        }
    }

    /**
     * @param rune
     */
    public void refreshPropertyByConfig(Rune.Builder rune) {
        // 读取配置判断升级是否影响符文词条
        PetRunePropertiesObject baseConfig = PetRuneProperties.getByRuneid(rune.getRuneBookId());
        if (baseConfig == null) {
            return;
        }
        PetRuneExpObject config = PetRuneExp.getByRarityAndLvlAndType(baseConfig.getRunerarity(), rune.getRuneLvl(), baseConfig.getRunetype());
        if (config == null) {
            return;
        }
        List<Builder> propertyList = rune.getRuneExPropertyBuilder().getPropertyBuilderList();
        for (Builder lastProperty : propertyList) {
            for (int[] configProperty : config.getExbaseproperties()) {
                if (configProperty.length > 1 && configProperty[0] == lastProperty.getPropertyType()) {
                    int newValue = RandomUtil.getRandomValue(configProperty[2], configProperty[3]);
                    lastProperty.setPropertyValue(newValue);
                }
            }
        }
    }

    public boolean playerObtainRune(petruneEntity cache, Map<Integer, Integer> runeMap, Reason reason) {
        if (cache == null) {
            return false;
        }

        List<Rune> addRuneList = new ArrayList<>();
        Map<Integer, Long> rarityNum = new HashMap<>();
        for (Entry<Integer, Integer> entry : runeMap.entrySet()) {
            if (entry.getKey() == null || PetRuneProperties.getByRuneid(entry.getKey()) == null) {
                LogUtil.warn("PetRuneServiceImpl.playerObtainRune, runeId is not exist, id = " + entry.getKey());
                continue;
            }

            for (int i = 0; i < entry.getValue(); i++) {
                Rune newRune = RandomUtil.getInitRuneById(entry.getKey());
                if (newRune == null) {
                    continue;
                }
                addRuneList.add(newRune);
                MapUtil.add2LongMapValue(rarityNum, PetRuneProperties.getByRuneid(newRune.getRuneBookId()).getRunerarity(), 1L);
            }
        }


        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            //统计目前玩家的拥有量，<符文id，数量>
            Map<Integer, Integer> ownedMap = new HashMap<>();
            for (Rune rune : addRuneList) {
                ownedMap.put(rune.getRuneBookId(), cache.getSameRuneBookIdCountByBookId(rune.getRuneBookId()));
            }

            cache.putAllRune(addRuneList);

            for (Entry<Integer, Integer> entry : ownedMap.entrySet()) {
                int nowOwned = cache.getSameRuneBookIdCountByBookId(entry.getKey());
                LogService.getInstance().submit(new DailyDateLog(cache.getPlayeridx(), false, RewardTypeEnum.RTE_Rune, entry.getKey(),
                        entry.getValue(), nowOwned - entry.getValue(), nowOwned, reason));
            }
        });
        // 消息推送
        petruneEntity.sendRuneGet(cache.getPlayeridx(), addRuneList);
        for (Entry<Integer, Long> entry : rarityNum.entrySet()) {
            //目标：累积获得x个x品质的符文
            EventUtil.triggerUpdateTargetProgress(cache.getPlayeridx(), TargetTypeEnum.TTE_CumuGainRune, entry.getValue().intValue(), entry.getKey());
        }
        RuneStatistics.getInstance().updateOwnRarityMap(rarityNum);
        return true;
    }

    public boolean removeRune(petruneEntity cache, List<String> idList, Reason reason) {
        if (cache == null) {
            return false;
        }

        if (idList == null || idList.isEmpty()) {
            LogUtil.warn("PetRuneServiceImpl.removeRune, remove rune id list is null");
            return true;
        }

        return SyncExecuteFunction.executeFunction(cache, cacheTemp -> {
            if (!cacheTemp.runeListCanRemove(idList)) {
                return false;
            }

            //统计删除之前符文的拥有量
            Map<Integer, Integer> ownedCountMap = new HashMap<>();
            for (String runeIdx : idList) {
                Rune rune = cache.getRuneById(runeIdx);
                if (rune == null) {
                    continue;
                }
                ownedCountMap.put(rune.getRuneBookId(), cache.getSameRuneBookIdCountByBookId(rune.getRuneBookId()));
            }

            //统计日志re
            for (Entry<Integer, Integer> entry : ownedCountMap.entrySet()) {
                LogService.getInstance().submit(new DailyDateLog(cache.getPlayeridx(), true, RewardTypeEnum.RTE_Rune,
                        entry.getKey(), entry.getValue(), entry.getValue(), cacheTemp.getSameRuneBookIdCountByBookId(entry.getKey()), reason));
            }

            petruneEntity.sendRuneRemove(cache.getPlayeridx(), idList);
            return true;
        });
    }

    public boolean bagEnlarge(String playerId, int vipLevel) {
        petruneEntity cache = getEntityByPlayer(playerId);
        if (cache == null) {
            return false;
        }
        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            cacheTemp.setCapacity(cacheTemp.getCapacity() + VIPConfig.getById(vipLevel).getRunebagimprove()
                    - +VIPConfig.getById(vipLevel - 1).getRunebagimprove());
        });
        petruneEntity.sendPetRuneBagRefresh(playerId, cache.getCapacity());
        return true;
    }

    public void resetRuneStatus(String playerId, List<String> petIdList) {
        petruneEntity cache = petruneCache.getInstance().getEntityByPlayer(playerId);
        if (cache == null || petIdList == null || petIdList.isEmpty()) {
            return;
        }
        SyncExecuteFunction.executeConsumer(cache, cacheTemp -> {
            cache.unEquipAllPetListRune(petIdList, true);
        });
    }

    public List<Rune> getRuneListByPets(String playerId, List<String> petIdList) {
        if (StringUtils.isEmpty(petIdList)) {
            return Collections.emptyList();
        }
        petruneEntity entity = getEntityByPlayer(playerId);
        if (entity == null) {
            return Collections.emptyList();
        }
        Collection<Rune> runes = new ArrayList<>(entity.getRuneListBuilder().getRuneMap().values());
        if (CollectionUtils.isEmpty(runes)) {
            return Collections.emptyList();
        }
        List<Rune> result = new ArrayList<>();
        for (String petId : petIdList) {
            for (Rune rune : runes) {
                if (petId.equals(rune.getRunePet())) {
                    result.add(rune);
                }
            }
        }
        return result;
    }
}
