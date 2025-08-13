/**
 * created by tool DAOGenerate
 */
package model.stoneRift;

import cfg.PetBaseProperties;
import cfg.StoneRiftLevel;
import cfg.StoneRiftLevelObject;
import cfg.StoneRiftPetObject;
import cfg.StoneRiftScienceObject;
import common.GlobalData;
import common.tick.GlobalTick;
import entity.UpdateDailyData;
import helper.ArrayUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import model.itembag.dbCache.itembagCache;
import model.itembag.entity.itembagEntity;
import model.obj.BaseObj;
import model.reward.RewardManager;
import model.reward.RewardUtil;
import model.stoneRift.dbCache.stoneriftCache;
import model.stoneRift.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import platform.logs.ReasonManager;
import protocol.Common;
import protocol.PetMessage;
import protocol.StoneRift;
import protocol.TargetSystem;
import util.*;

import static protocol.MessageId.MsgIdEnum.*;
import static protocol.StoneRift.StoneRiftPetEffect.SRPE_IncreaseClaimCriticalMulti;
import static protocol.StoneRift.StoneRiftPetEffect.SRPE_IncreaseClaimCriticalRate;
import static protocol.StoneRift.StoneRiftPetEffect.SRPE_IncreaseProduct;
import static protocol.StoneRift.StoneRiftPetEffect.SRPE_ReduceDurableConsume;
import static protocol.StoneRift.StoneRiftScienceEnum.*;
import static protocol.TargetSystem.TargetTypeEnum.*;

/**
 * created by tool
 */
@SuppressWarnings("serial")
@Slf4j
public class stoneriftEntity extends BaseObj implements UpdateDailyData {

    public String getClassType() {
        return "teamEntity";
    }

    /**
     *
     */
    private String idx;


    private byte[] data;

    private static final int cfgBaseNum = 1000;
    private static final double cfgBaseDouble = 1000.0;

    private AtomicBoolean factoryStatusChange = new AtomicBoolean(false);

    /**
     * 获得
     */
    public String getIdx() {
        return idx;
    }

    /**
     * 设置
     */
    public void setIdx(String idx) {
        this.idx = idx;
    }


    public String getBaseIdx() {
        return idx;
    }

    private stoneriftEntity() {
    }

    private List<Common.Reward> playerReward = new ArrayList<>();

    private static final long settleInterval = TimeUtil.MS_IN_A_MIN * 30;

    private ReadWriteLock rewardLock = new ReentrantReadWriteLock();

    /**
     * =========================================================================
     */
    @Override
    public void putToCache() {
        stoneriftCache.put(this);
    }

    private DbStoneRift db_data;

    private DbStoneRift getDbEntity() {
        try {
            if (this.data != null) {
                return (DbStoneRift) DbStoneRift.parseFrom(data);
            } else {
                return null;
            }
        } catch (Exception e) {
            LogUtil.printStackTrace(e);
            return null;
        }
    }

    public void init() {
        initDbStoneRift();

    }

    @Override
    public void updateDailyData() {
        resetEvent();
    }

    private void initDbStoneRift() {
        DbStoneRift dbStoneRift = new DbStoneRift();
        this.db_data = dbStoneRift;
        unlockFactoryByCompleteMission(Collections.emptyList());
        updateEntityNextSettleTime(dbStoneRift);
        resetNextCanClaimTime();
        dbStoneRift.setMapId(StoneRiftCfgManager.getInstance().randomPlayerMap());
        resetEvent();
        StoneRiftManager.getInstance().addStoneEntity(getIdx(), dbStoneRift);
    }


    public void resetNextCanClaimTime() {
        getDB_Builder().setNextCanClaimTime(getDB_Builder().getFactoryMap().values().stream()
                .mapToLong(DbStoneRiftFactory::getNextCanClaimTime).min().orElse(0));

    }


    public DbStoneRift getDB_Builder() {
        if (this.db_data == null) {
            this.db_data = getDbEntity();
        }
        return db_data;
    }

    @Override
    public void transformDBData() {
        this.data = getDB_Builder().toByteArray();
    }

    public stoneriftEntity(String playerIdx) {
        this.idx = playerIdx;
    }


    public void unlockFactoryByCompleteMission(List<Integer> completeMissionIds) {
        DbStoneRift dbStoneRift = getDB_Builder();
        Map<Integer, DbStoneRiftFactory> factoryMap = dbStoneRift.getFactoryMap();
        List<DbStoneRiftFactory> unlockFactory = StoneRiftCfgManager.getInstance().getCanUnlockFactory(factoryMap.keySet(), completeMissionIds);
        if (CollectionUtils.isEmpty(unlockFactory)) {
            return;
        }
        unlockFactory.forEach(factory -> {
            factoryMap.put(factory.getCfgId(), factory);
            sendFactoryUpdate(factory.getCfgId());
        });
        EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_UnlockFactory, unlockFactory.size(), 0);
    }


    public void settleReward() {

        DbStoneRift dbStoneRift = getDB_Builder();
        for (DbStoneRiftFactory factory : dbStoneRift.getFactoryMap().values()) {
            settleFactory(factory);
        }
        settleCurrencyAB();

        updateEntityNextSettleTime(dbStoneRift);

        resetStauts();

        updateWorldMapPlayer();

    }

    private void updateWorldMapPlayer() {
        if (!factoryStatusChange.get()) {
            return;
        }

        StoneRiftWorldMapManager.getInstance().broadcastWoldPlayerUpdateToAll(getIdx(), true);


        factoryStatusChange.set(false);
    }

    private void settleCurrencyAB() {
        if (storeCurrencyAMax()) {
            return;
        }
        List<Common.Reward> rewards = new ArrayList<>();
        rewards.add(getCurrAOutPut());
        Common.Reward bOutPut = getCurrencyBOutPut();
        if (bOutPut != null) {
            rewards.add(bOutPut);
        }
        RewardManager.getInstance().doRewardByList(getIdx(), rewards, ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift), false);
    }

    private Common.Reward getCurrencyBOutPut() {
        int pro = StoneRiftCfgManager.getInstance().getcurrBPro() +
                getScienceEffect(SRSE_AddGainCurrencyBRate);
        if (RandomUtil.getRandom1000() < pro) {
            return StoneRiftCfgManager.getInstance().getCurrBBaseOutPut();
        }
        return null;
    }

    private Common.Reward mergeSameReward(Common.Reward a, Common.Reward b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a.getRewardType() != b.getRewardType() || a.getId() != b.getId()) {
            throw new RuntimeException("mergeSameReward type not same");
        }
        return a.toBuilder().setCount(a.getCount() + b.getCount()).build();
    }


    private Common.Reward getCurrAOutPut() {
        int scienceEffect = cfgBaseNum + getScienceEffect(SRSE_AddCurrencyAEfficient);
        return multiReward(StoneRiftCfgManager.getInstance().getCurrABaseOutPut(), scienceEffect);
    }

    /**
     * @param reward
     * @param multi  千分比
     * @return
     */
    public Common.Reward multiReward(Common.Reward reward, int multi) {
        if (reward == null) {
            return null;
        }
        return reward.toBuilder().setCount((int) (reward.getCount() * (multi / cfgBaseDouble))).build();
    }

    private boolean storeCurrencyAMax() {
        itembagEntity item = itembagCache.getInstance().getItemBagByPlayerIdx(getIdx());
        if (item == null) {
            return true;
        }

        return item.getItemCount(StoneRiftCfgManager.getInstance().getCurrencyAItemId()) >= getCurrencyAMaxStore();
    }

    /**
     * 查询货币A最大储存上限
     *
     * @return
     */
    public int getCurrencyAMaxStore() {
        return StoneRiftCfgManager.getInstance().getCurrencyAMaxStore(getStoneRiftLv()) + getScienceEffect(SRSE_AddCurrencyAStoreLimit);
    }

    /**
     * 查询矿区等级
     *
     * @return
     */
    public int getStoneRiftLv() {
        return getDB_Builder().getLevel();
    }


    private void resetStauts() {
        checkePetRemove();

    }

    private void checkePetRemove() {


    }

    private void updateEntityNextSettleTime(DbStoneRift dbStoneRift) {
        long nextSettleTime = GlobalTick.getInstance().getCurrentTime() + StoneRiftCfgManager.getInstance().getSettleInterval();
        dbStoneRift.setNextSettleTime(nextSettleTime);
        StoneRiftManager.getInstance().updateNextSettleTime(getIdx(), nextSettleTime);
    }

    private void settleFactory(DbStoneRiftFactory factory) {
        if (storeMax(factory)) {
            return;
        }
        List<Common.Reward> baseReward = StoneRiftCfgManager.getInstance().getBaseReward(factory.getCfgId());
        int outPutUp = getFactoryEfficiency(factory);
        int multi = getCritical(factory);

        for (Common.Reward reward : baseReward) {
            factory.getSettleReward().add(reward.toBuilder().setCount((int) (reward.getCount() * ((outPutUp * 1.0) / cfgBaseDouble) * (multi / cfgBaseDouble))).build());
        }
        if (storeMax(factory)) {
            factory.getSettleReward().addAll(triggerExReward(factory));
            factoryStatusChange.set(true);
        }
        factory.setCurStore(factory.getCurStore() + 1);
        settleFactoryDurable(factory);
    }


    private List<Common.Reward> triggerExReward(DbStoneRiftFactory factory) {
        Map<Integer, Integer> cfg = getDB_Builder().getDbScience().getScienceEffect2().get(SRSE_GainRareItemWhenClaimResourceLevelFactory);
        if (cfg == null) {
            return Collections.emptyList();
        }
        List<Common.Reward> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : cfg.entrySet()) {
            if (entry.getKey() <= factory.getLevel()) {
                List<Common.Reward> rewardById = RewardUtil.getRewardsByRewardId(entry.getKey());
                if (rewardById != null) {
                    result.addAll(rewardById);
                }
            }
        }
        return result;
    }

    public boolean storeMax(DbStoneRiftFactory factory) {
        return factory.getCurStore() >= getMaxCanStore(factory);
    }

    public int getMaxCanStore(DbStoneRiftFactory factory) {
        return getScienceEffect(SRSE_FactoryStoreUp)
                + StoneRiftCfgManager.getInstance().getMaxCanProduce(factory.getCfgId(), factory.getLevel())
                + getScienceEffect2(SRSE_XLevelFactoryStoreMaxUp, factory.getLevel());
    }

    private void settleFactoryDurable(DbStoneRiftFactory factory) {
        if (StoneRiftCfgManager.getInstance().isSpecial(factory.getCfgId())) {
            return;
        }
        int minDurable = StoneRiftCfgManager.getInstance().getMinDurable();
        if (factory.getCurDurable() <= minDurable) {
            factory.setCurDurable(minDurable);
            return;
        }
        int reduceRate = getDefendPetUpByFunction(factory, SRPE_ReduceDurableConsume)
                + getEventEffect(SRSE_MoreDurable)
                + getScienceEffect(SRSE_MoreDurable);
        int baseConsume = StoneRiftCfgManager.getInstance().getSettleDurableConsume(factory.getCfgId());
        int durable = (int) (factory.getCurRealDurable(factory,this) - baseConsume * ((cfgBaseNum - reduceRate) / cfgBaseDouble));
        durable = Math.max(durable, minDurable);
        factory.setCurDurable(durable);
    }

    private int getCritical(DbStoneRiftFactory factory) {
        int scienceCriticalRate = getScienceCriticalRate();
        if (scienceCriticalRate <= 0) {
            return cfgBaseNum;
        }
        //暴击率
        int criticalRate = scienceCriticalRate + getDefendPetUpByFunction(factory, SRPE_IncreaseClaimCriticalRate);
        if (RandomUtil.getRandom1000() < criticalRate) {
            return getMultiCritical(factory);
        }
        return cfgBaseNum;
    }

    private int getMultiCritical(DbStoneRiftFactory factory) {
        int base = StoneRiftCfgManager.getInstance().getBaseClaimMultiCritical();
        return base + getDefendPetUpByFunction(factory, SRPE_IncreaseClaimCriticalMulti);
    }

    private int getDefendPetUpByFunction(DbStoneRiftFactory factory, StoneRift.StoneRiftPetEffect effect) {
        if (effect == null) {
            return 0;
        }

        StoneRiftPetObject cfg = StoneRiftCfgManager.getInstance().getPetCfgByPetType(factory.getPetType());
        if (cfg == null) {
            return 0;
        }
        if (effect.getNumber() != cfg.getFunction()) {
            return 0;
        }
        for (int[] ints : cfg.getRitygain()) {
            if (ints[0] == factory.getPetRarity()) {
                return ints[1];
            }
        }
        return 0;
    }

    private int getScienceCriticalRate() {
        return getScienceEffect(SRSE_ClaimCanCritical);
    }

    /**
     * 基础值(单个矿等级影响)* (1+超载效率+科技树+耐久度(折算,计算后更新耐久)+驻守魔灵(持续收取奖励)+随机事件)
     *
     * @param factory
     * @return
     */
    public int getFactoryEfficiency(DbStoneRiftFactory factory) {
        return getFactoryBaseEfficiency(factory) + getOverLoadEfficiency() + getScienceEfficiencyUp(factory.getLevel()) + computeDurableEfficiency(factory)
                + getDefendPetUpByFunction(factory, SRPE_IncreaseProduct) + getEventEffect(SRSE_FactoryWorkBetter);
    }

    private int getOverLoadEfficiency() {
        DbStoneRift rift = getDB_Builder();
        if (!GameUtil.inScope(rift.getOverLoadStart(), rift.getOverLoadExpire(), GlobalTick.getInstance().getCurrentTime())) {
            return 0;
        }
        return StoneRiftCfgManager.getInstance().getOverloadEfficiency() + getScienceEffect(SRSE_OverLoadPerformBetter);
    }

    /**
     * 基础效率(只跟矿等级有关)
     *
     * @param factory
     * @return
     */
    public int getFactoryBaseEfficiency(DbStoneRiftFactory factory) {
        return StoneRiftCfgManager.getInstance().getFactoryBaseEfficiency(factory.getCfgId(), factory.getLevel());
    }


    public int getScienceEffect(StoneRift.StoneRiftScienceEnum science) {
        Integer value = getDB_Builder().getDbScience().getScienceEffect().get(science);
        return value == null ? 0 : value;
    }

    public int getScienceEffect2(StoneRift.StoneRiftScienceEnum science, int parm) {
        Map<Integer, Integer> map = getDB_Builder().getDbScience().getScienceEffect2().get(science);
        if (map == null) {
            return 0;
        }
        int temp = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getKey() <= parm) {
                temp += entry.getValue();
            }
        }
        return temp;
    }


    private int getScienceEfficiencyUp(int level) {
        return getScienceEffect(SRSE_FactoryWorkBetter) + getScienceEffect2(SRSE_AddLevelFactoryEfficient, level);
    }

    public List<Common.Reward> queryCanClaimReward() {
        return playerReward;
    }

    public void claimAllStone() {
        Map<Integer, DbStoneRiftFactory> factoryMap = getDB_Builder().getFactoryMap();
        if (MapUtils.isEmpty(factoryMap)) {
            return;
        }
        boolean needUpdateWorldMap = factoryMap.values().stream().anyMatch(this::storeMax);
        List<Common.Reward> rewards = findAllCanClaimReward();
        factoryMap.values().forEach(this::settleFactoryClaim);
        RewardManager.getInstance().doRewardByList(getIdx(), rewards,
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift), true);
        resetNextCanClaimTime();
        sendAllFactoryUpdate();
        if (needUpdateWorldMap) {
            StoneRiftWorldMapManager.getInstance().broadcastWoldPlayerUpdateToAll(getIdx(), false);
        }

    }

    private void sendLvExpUpdate() {
        StoneRift.SC_UpdateStoneRiftLv.Builder msg = StoneRift.SC_UpdateStoneRiftLv.newBuilder();
        msg.setExp(getDB_Builder().getExp());
        msg.setLv(getDB_Builder().getLevel());
        GlobalData.getInstance().sendMsg(getIdx(), SC_UpdateStoneRiftLv_VALUE, msg);


    }

    /**
     * 查询所有矿可领取奖励
     * @return
     */
    public List<Common.Reward> findAllCanClaimReward() {
        List<Common.Reward> rewards = new ArrayList<>();
        for (DbStoneRiftFactory factory : getDB_Builder().getFactoryMap().values()) {
            rewards.addAll(factory.getSettleReward());
        }
        return RewardUtil.mergeReward(rewards);
    }


    public void useOverLoad() {
        DbStoneRift db_builder = getDB_Builder();
        long nextSettleTime = db_builder.getNextSettleTime();
        db_builder.setOverLoadStart(nextSettleTime);
        db_builder.setOverLoadExpire(nextSettleTime + StoneRiftCfgManager.getInstance().getOverLoadDurTime());
        db_builder.setNextCanOverLoad(getNextCanOverLoadTime());
        EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_UseOverLoad, 1, 0);
    }

    private long getNextCanOverLoadTime() {
        long now = GlobalTick.getInstance().getCurrentTime();
        long overloadCd = StoneRiftCfgManager.getInstance().getOverloadCd();
        return (long) (now + overloadCd * (1 - getScienceEffect(SRSE_ReduceOverloadCD) / cfgBaseDouble));
    }

    public void deFendPet(PetMessage.Pet pet, DbStoneRiftFactory factory) {
        factory.setPetCfgId(pet.getPetBookId());
        factory.setPetRarity(pet.getPetRarity());
        factory.setPetId(pet.getId());
        factory.setPetType(PetBaseProperties.getTypeById(pet.getPetBookId()));
        getDB_Builder().getDefendPet().put(factory.getPetCfgId(), pet.getId());
    }

    public void unlockFactory(int id) {
        DbStoneRiftFactory dbStoneRiftFactory = StoneRiftCfgManager.getInstance().buildFactory(id);
        getDB_Builder().getFactoryMap().put(id, dbStoneRiftFactory);
        sendFactoryUpdate(id);
        EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_UnlockFactory, 1, 0);
    }

    public int getEventEffect(StoneRift.StoneRiftScienceEnum function) {
        Integer integer = getDB_Builder().getEvent().getEventEffect().get(function.getNumber());
        return integer == null ? 0 : integer;
    }


    public void resetEvent() {
        DbStoneRiftEvent dbEvent = getDB_Builder().getEvent();
        if (dbEvent.getEvent() == 0 || !dbEvent.isAlreadyTrigger()
                || dbEvent.getExpireTime() > GlobalTick.getInstance().getCurrentTime()) {
            return;
        }
        StoneRift.StoneRiftEvent event = StoneRiftCfgManager.getInstance().randomEvent();
        if (StoneRift.StoneRiftEvent.SRE_Reward == event) {
            dbEvent.setRewardId(StoneRiftCfgManager.getInstance().randomEventRewardId());
        } else {
            dbEvent.setRewardId(0);
        }

        dbEvent.setEvent(event.getNumber());
        int[] eventEffectCfg = StoneRiftCfgManager.getInstance().getEventEffectCfg(dbEvent.getEvent());
        if (eventEffectCfg != null) {
            dbEvent.getEventEffect().put(eventEffectCfg[1], eventEffectCfg[2]);
        }

        dbEvent.setAlreadyTrigger(false);
    }

    public void randomEvent() {
        DbStoneRiftEvent dbEvent = getDB_Builder().getEvent();
        dbEvent.setAlreadyTrigger(true);
        switch (dbEvent.getEvent()) {
            case StoneRift.StoneRiftEvent
                    .SRE_NULL_VALUE:
                break;
            case StoneRift.StoneRiftEvent
                    .SRE_Reward_VALUE:
                doEventReward(dbEvent.getRewardId());
            default:
                dbEvent.setStartTime(getDB_Builder().getNextSettleTime());
                dbEvent.setExpireTime(StoneRiftCfgManager.getInstance().getEventNextExpireTime());
        }

        sendEventUpdate();

    }

    private void doEventReward(int rewardId) {
        RewardManager.getInstance().doRewardByList(getIdx(),
                RewardUtil.getRewardsByRewardId(rewardId), ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift), true);
    }

    private void sendEventUpdate() {
        StoneRift.SC_UpdateStoneRiftEvent.Builder msg = StoneRift.SC_UpdateStoneRiftEvent.newBuilder();
        DbStoneRiftEvent event = getDB_Builder().getEvent();
        msg.setUpdate(dbEvent2Vo(event));
        GlobalData.getInstance().sendMsg(getIdx(), SC_UpdateStoneRiftEvent_VALUE, msg);

    }

    private StoneRift.StoneRiftEventVo.Builder dbEvent2Vo(DbStoneRiftEvent event) {
        StoneRift.StoneRiftEventVo.Builder vo = StoneRift.StoneRiftEventVo.newBuilder();
        vo.setEventValue(event.getEvent());
        vo.setCanTrigger(!event.isAlreadyTrigger());
        vo.setExpireTime(event.getExpireTime());
        vo.setRewardId(event.getRewardId());
        return vo;
    }

    public void doStudyScience(StoneRiftScienceObject cfg) {
        if (cfg == null) {
            return;
        }

        Map<Integer, int[]> scienceParams = StoneRiftCfgManager.getInstance().getScienceParams();
        int[] params = scienceParams.get(cfg.getId());

        StoneRiftScience dbScience = getDB_Builder().getDbScience();

        StoneRift.StoneRiftScienceEnum science = StoneRift.StoneRiftScienceEnum.forNumber(cfg.getFunction());
        if (params == null || params.length < 2) {

            int add = ArrayUtil.isEmpty(params) ? 0 : params[0];

            Map<StoneRift.StoneRiftScienceEnum, Integer> effect = dbScience.getScienceEffect();

            Integer before = effect.get(science);
            int now = before == null ? add : before + add;
            effect.put(science, now);
        } else {
            Map<Integer, Integer> map = dbScience.getScienceEffect2().computeIfAbsent(science, a -> new HashMap<>());
            MapUtil.add2IntMapValue(map, params[0], params[1]);
        }
        switch (science) {
            case SRSE_UnlockMoreGoods:
                EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_UnlockMoreGoods, 1, 0);
                break;
            case SRSE_UnlockOverLoad:
                doOverloadUnlock();
                break;
            default:
        }
    }

    private void doOverloadUnlock() {
        getDB_Builder().setNextCanOverLoad(GlobalTick.getInstance().getCurrentTime());
        sendOverLoadUpdate();
    }


    public int getSkillLv(int scienceId) {
        Integer integer = getDB_Builder().getDbScience().getSkillLvMap().get(scienceId);
        return integer == null ? 0 : integer;

    }

    public void studyScience(StoneRiftScienceObject scienceObject) {
        if (scienceObject == null) {
            return;
        }
        incrScienceLv(scienceObject.getId());
        doStudyScience(scienceObject);
        sendScienceUpdate(scienceObject.getId());
        addAchivment(scienceObject);
    }

    private void addAchivment(StoneRiftScienceObject scienceObject) {
        if (getSkillLv(scienceObject.getId()) == 1) {
            EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_UnlockScience, 1, 0);
        } else {
            EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_UpScienceLv, 1, 0);
        }
        if (getDB_Builder().getDbScience().getSkillLvMap().size() == cfg.StoneRiftScience._ix_id.size()) {
            EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_UnlockAllScience, 1, 0);
        }
        if (StoneRiftCfgManager.getInstance().checkLevelMax(scienceObject.getId(), this, scienceObject)) {
            EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_ScienceLvMax, 1, 0);
        }
    }

    private void sendScienceUpdate(int scienceId) {
        StoneRift.SC_StoneRiftScienceUpdate.Builder msg = StoneRift.SC_StoneRiftScienceUpdate.newBuilder();
        msg.setScienceId(scienceId);
        msg.setScienceLevel(getSkillLv(scienceId));
        GlobalData.getInstance().sendMsg(getIdx(), SC_StoneRiftScienceUpdate_VALUE, msg);
    }

    private void incrScienceLv(int id) {
        Map<Integer, Integer> skillLvMap = getDB_Builder().getDbScience().getSkillLvMap();
        MapUtil.add2IntMapValue(skillLvMap, id, 1);
    }


    public void claimOneStone(int cfgId) {
        DbStoneRiftFactory factory = getDB_Builder().getFactoryMap().get(cfgId);
        if (factory == null) {
            return;
        }
        List<Common.Reward> settleReward = factory.getSettleReward();
        if (CollectionUtils.isEmpty(settleReward)) {
            return;
        }
        boolean needUpdateWorldMap = storeMax(factory) && getDB_Builder().getFactoryMap()
                .values().stream().noneMatch(e -> e.getCfgId() != cfgId && storeMax(factory));

        resetNextCanClaimTime();
        RewardManager.getInstance().doRewardByList(getIdx(), settleReward,
                ReasonManager.getInstance().borrowReason(Common.RewardSourceEnum.RSE_StoneRift), true);
        settleFactoryClaim(factory);
        sendFactoryUpdate(factory.getCfgId());
        if (needUpdateWorldMap) {
            StoneRiftWorldMapManager.getInstance().broadcastWoldPlayerUpdateToAll(getIdx(), false);
        }
    }

    public void sendFactoryUpdate(int factoryId) {
        StoneRift.SC_StoneRiftFactoryUpdate.Builder msg = StoneRift.SC_StoneRiftFactoryUpdate.newBuilder();
        StoneRift.StoneFactoryVo vo = StoneRiftUtil.toFactoryVo(this, factoryId);
        if (vo == null) {
            return;
        }
        msg.addStoneVo(vo);
        GlobalData.getInstance().sendMsg(getIdx(), SC_StoneRiftFactoryUpdate_VALUE, msg);
    }

    public void sendAllFactoryUpdate() {
        StoneRift.SC_StoneRiftFactoryUpdate.Builder msg = StoneRift.SC_StoneRiftFactoryUpdate.newBuilder();

        for (Integer factoryId : getDB_Builder().getFactoryMap().keySet()) {
            StoneRift.StoneFactoryVo vo = StoneRiftUtil.toFactoryVo(this, factoryId);
            if (vo != null) {
                msg.addStoneVo(vo);
            }
        }
        GlobalData.getInstance().sendMsg(getIdx(), SC_StoneRiftFactoryUpdate_VALUE, msg);

    }

    private void settleFactoryClaim(DbStoneRiftFactory factory) {
        if (factory == null) {
            return;
        }
        factory.setClaimTimes(factory.getClaimTimes() + 1);
        factory.setNextCanClaimTime(StoneRiftCfgManager.getInstance().getNextCanClaimTime());
        addStoneRiftExp(StoneRiftCfgManager.getInstance().getGainExp(factory.getCfgId(), factory.getSettleReward()));
        factory.getSettleReward().clear();
        factory.setCurStore(0);
        EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_ClaimRes, 1, 0);

    }

    public void addStoneRiftExp(int exp) {
        int totalExp = getDB_Builder().getExp() + exp;
        int beforeLv = getDB_Builder().getLevel();
        int nowLv = beforeLv;
        StoneRiftLevelObject cfg;
        while (true) {
            cfg = StoneRiftLevel.getByLevel(nowLv);
            if (cfg == null) {
                break;
            }
            if (cfg.getUpexp() <= 0 || cfg.getUpexp() > totalExp) {
                break;
            }
            totalExp -= cfg.getUpexp();
            nowLv++;
        }
        getDB_Builder().setExp(totalExp);
        if (nowLv != beforeLv) {
            getDB_Builder().setLevel(nowLv);
            EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_MineLv, nowLv - beforeLv, 0);
        }
        sendLvExpUpdate();
    }

    public void sendOverLoadUpdate() {
        StoneRift.SC_UpdateOverLoad.Builder msg = StoneRift.SC_UpdateOverLoad.newBuilder();

        msg.setOverload(StoneRiftUtil.toOverLoadInfo(this.getDB_Builder()));

        GlobalData.getInstance().sendMsg(getIdx(), SC_UpdateOverLoad_VALUE, msg);
    }

    public void sendStoneRiftAchievementUpdate(Collection<TargetSystem.TargetMission> updateMissions) {
        StoneRift.SC_StoneRiftAchievementUpdate.Builder msg = StoneRift.SC_StoneRiftAchievementUpdate.newBuilder();

        stoneriftEntity stoneRift = stoneriftCache.getByIdx(getIdx());
        if (stoneRift == null) {
            return;
        }
        if (!CollectionUtils.isEmpty(updateMissions)) {
            msg.addAllUpdateMissions(updateMissions);
        }
        DbStoneRiftAchievement achievement = stoneRift.getDB_Builder().getAchievement();
        msg.addAllClaimedIds(achievement.getClaimedIds());
        msg.addAllCompleteAchievementIds(achievement.getCompleteAchievementIds());
        GlobalData.getInstance().sendMsg(getIdx(), SC_StoneRiftAchievementUpdate_VALUE, msg);
    }

    public void updateStoneRiftAchievement(List<TargetSystem.TargetMission> updateMissions) {
        if (CollectionUtils.isEmpty(updateMissions)) {
            return;
        }
        List<Integer> completeMissions = updateMissions.stream().filter(e -> Common.MissionStatusEnum.MSE_Finished == e.getStatus()).map(TargetSystem.TargetMission::getCfgId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(completeMissions)) {
            List<Integer> achievementIds = StoneRiftCfgManager.getInstance().getUnlockAchievementIdByMissionIds(completeMissions);
            getDB_Builder().getAchievement().getCompleteAchievementIds().addAll(achievementIds);
        }
        sendStoneRiftAchievementUpdate(updateMissions);
    }

    public int computeDurableEfficiency(int cfgId) {
        DbStoneRiftFactory factory = getDB_Builder().getFactoryMap().get(cfgId);
        if (factory == null) {
            return 0;
        }
        return computeDurableEfficiency(factory);
    }

    private int computeDurableEfficiency(DbStoneRiftFactory factory) {
        return StoneRiftCfgManager.getInstance().findEffByDurable(factory.getCurRealDurable(factory,this));
    }

    public int getCanMaxDefendPetRarity() {
        int scienceEffect = getScienceEffect(SRSE_CanDefendPetRarity);
        if (scienceEffect != 0) {
            return scienceEffect;
        }
        return StoneRiftCfgManager.getInstance().getCanMaxDefendPetRarity();
    }

    public int getCanMinDefendPetRarity() {
        return StoneRiftCfgManager.getInstance().getCanMinDefendPetRarity();
    }

    public boolean isOverloadUnlock() {
        return existScience(SRSE_UnlockOverLoad);
    }

    public boolean existScience(StoneRift.StoneRiftScienceEnum function) {
        return getDB_Builder().getEvent().getEventEffect().get(function.getNumber()) != null;
    }

    public boolean canSteal() {
        return getDB_Builder().getFactoryMap().values().stream().anyMatch(this::storeMax);
    }

    public void randomNextWorldMapId() {
        DbStoneRift dbStoneRift = getDB_Builder();
        StoneRiftWorldMapManager.getInstance().randomNextWorldMap(dbStoneRift.getDbPlayerWorldMap());
    }

    public void sendPlayerWorldMapInfoUpdate() {
        StoneRift.SC_RefreshWorldMapPlayerInfo.Builder msg = StoneRift.SC_RefreshWorldMapPlayerInfo.newBuilder();
        DbPlayerWorldMap worldMap = getDB_Builder().getDbPlayerWorldMap();

        msg.setBuyStealTime(worldMap.getBuyStealTime());
        msg.setUseStealTime(worldMap.getUseStealTime());
        msg.setUseRefreshTime(worldMap.getUserFreeRefreshTime());
        msg.setBuyRefreshTime(worldMap.getBuyRefreshTime());
        GlobalData.getInstance().sendMsg(getIdx(), SC_RefreshWorldMapPlayerInfo_VALUE, msg);
    }

    public void upStoneRiftLv(int factoryId) {
        DbStoneRiftFactory factory = getDB_Builder().getFactoryMap().get(factoryId);
        if (factory == null) {
            LogUtil.error("upStoneRiftLv fail,can`t find factory,playerIdx:{},factoryId:{}", getIdx(), factoryId);
            return;
        }
        factory.setLevel(factory.getLevel() + 1);
        sendFactoryUpdate(factoryId);
        EventUtil.triggerUpdateTargetProgress(getIdx(), TTE_StoneRift_FactoryLevelReach, 1, factory.getLevel());
        LogUtil.info("player success upStoneRiftLv,playerIdx:{},factoryId:{},now factoryLv", getIdx(), factoryId, factory.getLevel());
    }

    public void recoveryStoneFactory(int factoryId) {
        DbStoneRiftFactory factory = getDB_Builder().getFactoryMap().get(factoryId);
        if (factory == null) {
            log.error("recoveryStoneFactory fail,can`t find factory,playerIdx:{},factoryId:{}", getIdx(), factoryId);
            return;
        }
        factory.setCurStore(0);
        factory.setCurDurable(factory.getMaxDurable());
        sendFactoryUpdate(factoryId);
    }

    /**
     * 查询单个矿可领取奖励
     * @param factoryId
     * @return
     */
    public List<Common.Reward> findCanClaimReward(int factoryId) {
        DbStoneRiftFactory factory = getDB_Builder().getFactoryMap().get(factoryId);
        if (factory == null) {
            return Collections.emptyList();
        }
        return RewardUtil.mergeReward(factory.getSettleReward());
    }

    public int getFactoryCurDurable(DbStoneRiftFactory factory) {
        if (factory == null) {
            return 0;
        }
        return factory.getCurDurable() + getScienceEffect(SRSE_AddDurableWhenFactoryRich);

    }
}