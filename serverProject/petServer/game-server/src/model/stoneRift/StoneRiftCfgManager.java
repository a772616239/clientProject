package model.stoneRift;

import cfg.*;
import common.GameConst;
import common.tick.GlobalTick;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import model.consume.ConsumeUtil;
import model.reward.RewardUtil;
import model.stoneRift.entity.DbStoneRiftFactory;
import org.apache.commons.collections4.CollectionUtils;
import protocol.Common;
import protocol.StoneRift;
import util.LogUtil;
import util.RandomUtil;
import util.TimeUtil;

@Slf4j
public class StoneRiftCfgManager {

    @Getter
    private static StoneRiftCfgManager instance = new StoneRiftCfgManager();

    private Map<Integer, Integer> unlockMissionMap = new HashMap<>();

    @Getter
    private Map<Integer, int[]> scienceParams = new HashMap<>();


    private List<MissionObject> unlockMissions = new ArrayList<>();

    private List<MissionObject> achievementMissions = new ArrayList<>();

    private Map<Integer, List<Common.Reward>> cfgRewardMap = new HashMap<>();

    private Map<Integer, Integer> outputUpByLevel = new HashMap<>();

    private Map<Integer, Integer> maxStoreMap = new HashMap<>();

    private Map<Integer, Integer> durableConsumeMap = new HashMap<>();

    private StoneRiftConfigObject commonCfg;

    /**
     * 矿区科技id解锁需要的矿区等级
     * 矿区科技id,矿区等级
     */
    private Map<Integer, Integer> riftLvUnlockScience = new HashMap<>();

    /**
     * 经验兑换配置
     */
    private Map<Integer, Integer> expChange = new HashMap<>();

    /**
     * 货币A基础产出
     */
    private Common.Reward currencyABaseOutput;

    /**
     * 货币b基础产出
     */
    private Common.Reward currencyBBaseOutput;

    /**
     * 矿场id价值排序
     */
    private List<Integer> worth;

    /**
     * 特殊矿场id
     */
    private List<Integer> specialFactoryIds = new ArrayList<>();


    public boolean init() {

        initDefaultFactory();

        return true;

    }


    private void initDefaultFactory() {
        initStoneRiftMineCfg();
        initLevelCfg();
        initCommonCfg();
        initAchievementMissions();
        initScience();
    }

    private void initScience() {
        for (StoneRiftScienceObject cfg : StoneRiftScience._ix_id.values()) {
            cfg.getLevelprams();
            scienceParams.put(cfg.getId(), cfg.getLevelprams());
        }
    }

    private void initAchievementMissions() {
        for (StoneRiftAchievementObject cfg : StoneRiftAchievement._ix_id.values()) {
            MissionObject mission = Mission.getById(cfg.getMissionid());
            if (mission != null && mission.getMissiontype() > 0) {
                achievementMissions.add(mission);
            }
        }

    }

    private void initCommonCfg() {
        commonCfg = StoneRiftConfig.getById(GameConst.CONFIG_ID);
        currencyABaseOutput = RewardUtil.parseReward(commonCfg.getCurrencyaoutput());
        currencyBBaseOutput = RewardUtil.parseReward(commonCfg.getCurrencyboutput());
    }

    private void initLevelCfg() {
        for (StoneRiftLevelObject lvCfg : StoneRiftLevel._ix_level.values()) {
            outputUpByLevel.put(lvCfg.getLevel(), lvCfg.getOutputup());
            for (int science : lvCfg.getUnlockscience()) {
                riftLvUnlockScience.put(science, lvCfg.getLevel());
            }
        }
    }

    private void initStoneRiftMineCfg() {
        for (StoneRiftMineObject cfg : StoneRiftMine._ix_id.values()) {
            if (cfg.getUnlockcondition() > 0) {
                MissionObject mission = Mission.getById(cfg.getUnlockcondition());
                if (mission == null) {
                    LogUtil.error("StoneRiftMine unlock mission is error,cfgId:{},missionId", cfg.getId(), cfg.getUnlockcondition());
                }
                unlockMissions.add(mission);
            }
            unlockMissionMap.put(cfg.getId(), cfg.getUnlockcondition());
            cfgRewardMap.put(cfg.getId(), RewardUtil.parseRewardIntArrayToRewardList(cfg.getOutput()));
            durableConsumeMap.put(cfg.getId(), cfg.getDurableconsume());
            maxStoreMap.put(cfg.getId(), cfg.getMaxstore());
            expChange.put(cfg.getExchangeexp()[0], cfg.getExchangeexp()[1]);
            if (cfg.getType() == 2) {
                specialFactoryIds.add(cfg.getId());
            }
        }
        worth = StoneRiftMine._ix_id.values().stream().sorted((o1, o2) -> o2.getWorth() - o1.getWorth()).map(StoneRiftMineObject::getId).collect(Collectors.toList());
    }

    public List<MissionObject> getUnlockMissions() {
        return unlockMissions;
    }

    public List<MissionObject> getAchievementMissions() {
        return achievementMissions;
    }

    public Set<Integer> getCanUnlockFactoryIds(Set<Integer> alreadyUnlockId, List<Integer> completeMissionIds) {
        Set<Integer> unlockIds = new HashSet<>();
        unlockMissionMap.forEach((fId, missionId) -> {
            if (!alreadyUnlockId.contains(fId) && (missionId == 0 || completeMissionIds.contains(missionId))) {
                unlockIds.add(fId);
            }

        });
        return unlockIds;
    }

    public List<DbStoneRiftFactory> getCanUnlockFactory(Set<Integer> alreadyUnlockId, List<Integer> completeMissionIds) {
        Set<Integer> canUnlockFactoryIds = getCanUnlockFactoryIds(alreadyUnlockId, completeMissionIds);
        return createDbStoneRiftFactoryByIds(canUnlockFactoryIds);
    }

    public List<DbStoneRiftFactory> createDbStoneRiftFactoryByIds(Set<Integer> cfgIds) {
        if (CollectionUtils.isEmpty(cfgIds)) {
            return Collections.emptyList();
        }
        List<DbStoneRiftFactory> result = new ArrayList<>();
        for (Integer cfgId : cfgIds) {
            DbStoneRiftFactory factory = buildFactory(cfgId);
            result.add(factory);
        }
        return result;
    }

    public DbStoneRiftFactory buildFactory(int cfgId) {
        DbStoneRiftFactory factory = new DbStoneRiftFactory();
        factory.setCfgId(cfgId);
        factory.setBaseReward(getBaseReward(cfgId));
        factory.setMaxDurable(commonCfg.getInitdurable());
        factory.setCurDurable(commonCfg.getInitdurable());
        factory.setLevel(1);
        factory.setNextCanClaimTime(getNextCanClaimTime());
        return factory;
    }

    public long getNextCanClaimTime() {
        return GlobalTick.getInstance().getCurrentTime() + getClaimInterval();
    }


    public List<Common.Reward> getBaseReward(int factory) {
        return cfgRewardMap.get(factory);

    }

    public Common.Consume findUnlockConsume(int id) {
        StoneRiftMineObject cfg = StoneRiftMine.getById(id);
        if (cfg != null) {
            return ConsumeUtil.parseConsume(cfg.getUnlockconsume());
        }
        return null;
    }

    public Common.Consume getScienceUpConsume(int scienceId) {
        StoneRiftScienceObject cfg = StoneRiftScience.getById(scienceId);
        if (cfg == null) {
            return null;
        }
        return ConsumeUtil.parseConsume(cfg.getStudyconsume());
    }

    public StoneRift.StoneRiftEvent randomEvent() {
        int[] cfg = RandomUtil.getRandomCfgByWeightIndex(commonCfg.getEventweight(), 2);
        return StoneRift.StoneRiftEvent.forNumber(cfg[0]);
    }

    public long getClaimInterval() {
        return commonCfg.getClaiminterval() * TimeUtil.MS_IN_A_S;
    }

    public int randomPlayerMap() {
        int[] mapPool = commonCfg.getPlayermappool();
        return mapPool[ThreadLocalRandom.current().nextInt(mapPool.length)];
    }

    public long getEventNextExpireTime() {
        return GlobalTick.getInstance().getCurrentTime() + commonCfg.getEventduration() * TimeUtil.MS_IN_A_S;

    }

    public List<Common.Reward> getEventReward(int rewardIndex) {
        return RewardUtil.parseRewardIntArrayToRewardList(commonCfg.getEventreward());
    }

    public int getGainExp(int cfgId, List<Common.Reward> settleReward) {
        if (CollectionUtils.isEmpty(settleReward)) {
            return 0;
        }
        Integer base = expChange.get(cfgId);
        if (base == null) {
            return 0;
        }
        return settleReward.get(0).getCount() / base;
    }

    public List<Integer> getUnlockAchievementIdByMissionIds(List<Integer> completeMissions) {
        if (CollectionUtils.isEmpty(completeMissions)) {
            return Collections.emptyList();
        }
        return StoneRiftAchievement._ix_id.values().stream()
                .filter(e -> completeMissions.contains(e.getMissionid())).map(StoneRiftAchievementObject::getId).collect(Collectors.toList());
    }

    public int findEffByDurable(double rate) {
        for (int[] ints : commonCfg.getDurableefficiency()) {
            if (ints[1] <= rate && (ints[2] == -1 || ints[2] > rate)) {
                return ints[3];
            }
        }
        return 0;
    }

    public StoneRiftPetObject getPetCfgByPetType(int petType) {
        return StoneRiftPet.getByPettype(petType);
    }

    public int findEventBuffEffect(int event) {
        for (int[] ints : commonCfg.getBuffeffect()) {
            if (ints[0] == event) {
                return ints[1];
            }
        }
        return 0;
    }

    public int getBaseClaimMultiCritical() {
        return commonCfg.getClaimbasemulticritical();
    }

    public int[] getEventEffectCfg(int event) {
        for (int[] ints : commonCfg.getBuffeffect()) {
            if (ints[0] == event) {
                return ints;
            }
        }
        return new int[0];
    }


    public int getSettleDurableConsume(int cfgId) {
        StoneRiftMineObject cfg = StoneRiftMine.getById(cfgId);
        if (cfg == null) {
            log.error("can`t find StoneRiftMine cfg by mineId:{}", cfg.getId());
            throw new RuntimeException();
        }
        return cfg.getDurableconsume();
    }

    public int getFactoryBaseEfficiency(int cfgId, int level) {
        StoneMineLevelObject cfg = StoneMineLevel.getByLevel(level);
        if (cfg == null) {
            log.error("StoneMainLevel cfg is not exists by level:{} ,factoryId:{}", level, cfgId);
            return 1000;
        }
        for (int[] ints : cfg.getImproveefficiency()) {
            if (ints[0] == cfgId) {
                return ints[1];
            }
        }
        log.error("StoneMainLevel cfg is error by level:{} ,factoryId:{}", level, cfgId);
        return 1000;
    }

    public int getMaxCanProduce(int cfgId, int level) {
        StoneMineLevelObject cfg = StoneMineLevel.getByLevel(level);
        if (cfg == null) {
            return 0;
        }
        for (int[] ints : cfg.getProducetime()) {
            if (ints[0] == cfgId) {
                return ints[1];
            }
        }
        return 0;
    }

    public int getMinDurable() {
        return commonCfg.getMindurable();
    }

    public int getCanMaxDefendPetRarity() {
        return commonCfg.getDefendpetrarity()[1];
    }

    public int getCanMinDefendPetRarity() {
        return commonCfg.getDefendpetrarity()[0];
    }

    /**
     * 获取超载持续时间
     *
     * @return
     */
    public long getOverLoadDurTime() {
        return commonCfg.getOverloadduration() * TimeUtil.MS_IN_A_S;
    }

    public Common.Reward getCurrABaseOutPut() {
        return currencyABaseOutput;
    }

    public Common.Reward getCurrBBaseOutPut() {
        return currencyBBaseOutput;
    }

    public int getcurrBPro() {
        return commonCfg.getCurrencybgainpro();
    }

    public int getCurrencyAItemId() {
        return commonCfg.getCurrencyaitemid();

    }

    public int getCurrencyAMaxStore(int stoneRiftLv) {
        StoneRiftLevelObject cfg = StoneRiftLevel.getByLevel(stoneRiftLv);
        if (cfg == null) {
            return 0;
        }
        return cfg.getCurrencyamax();
    }

    public int getOverloadEfficiency() {
        return commonCfg.getOverloadefficiency();

    }

    public long getOverloadCd() {
        return commonCfg.getOverloadcd() * TimeUtil.MS_IN_A_S;
    }

    public int[] getCanRandomIcon() {
        return commonCfg.getWorldplayericonpool();

    }

    public StoneRiftRobotObject randomOneRobotCfg() {
        return StoneRiftRobot.getById(RandomUtil.randomOneFromList(new ArrayList<>(StoneRiftRobot._ix_id.keySet())));

    }

    public Common.Consume getBuyStealConsume() {
        return ConsumeUtil.parseConsume(commonCfg.getStealbuyconsume());
    }

    public List<Integer> getFactoryWorth() {
        return worth;
    }

    public int getMapPlayerSize() {
        return commonCfg.getPlayersize();
    }

    public int getMapTotalSize() {
        return commonCfg.getMapsize();
    }

    public int getBaseRefreshTime() {
        return commonCfg.getWorldmaprefreshtime();

    }

    public int getBaseStealTime() {
        return commonCfg.getCanstealtime();
    }

    public int getCanStolenTime() {
        return commonCfg.getCanstolentime();
    }

    public int randomEventRewardId() {
        return RandomUtil.getRandomCfgBy2(commonCfg.getEventreward())[0];
    }

    public boolean checkLevelMax(int scienceId, stoneriftEntity stoneRift, StoneRiftScienceObject cfg) {
        int skillLv = stoneRift.getSkillLv(scienceId);
        if (skillLv >= cfg.getMaxlevel()) {
            return true;
        }
        return false;

    }

    public boolean checkLevelMax(int scienceId, stoneriftEntity stoneRift) {
        StoneRiftScienceObject cfg = StoneRiftScience.getById(scienceId);
        if (cfg == null) {
            return false;
        }
        return checkLevelMax(scienceId, stoneRift, cfg);
    }

    public boolean isSpecial(int cfgId) {
        return specialFactoryIds.contains(cfgId);
    }

    public long getSettleInterval() {
        return commonCfg.getTimeitem() * TimeUtil.MS_IN_A_S;
    }

    public boolean checkStoneRiftLvEnough(int stoneRiftLv, int scienceId) {
        Integer needLv = riftLvUnlockScience.get(scienceId);
        return needLv == null || needLv <= stoneRiftLv;
    }

    public int getDefendNeedRiftLv() {
        return commonCfg.getDefendpetneedriftlv();
    }

    public List<Common.Reward> getStealRateItem() {
        return RewardUtil.parseRewardIntArrayToRewardList(commonCfg.getStalrareitem());

    }
}
