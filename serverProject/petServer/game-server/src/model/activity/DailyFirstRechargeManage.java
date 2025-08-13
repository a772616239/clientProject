package model.activity;

import cfg.DailyFirstRecharge;
import cfg.DailyFirstRechargeObject;
import common.GameConst;
import common.tick.GlobalTick;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.util.Pair;
import lombok.Getter;
import model.reward.RewardUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.CollectionUtils;
import protocol.Common;
import util.LogUtil;
import util.TimeUtil;

public class DailyFirstRechargeManage {
    private static DailyFirstRechargeManage instance;

    public static DailyFirstRechargeManage getInstance() {
        if (instance == null) {
            synchronized (DailyFirstRechargeManage.class) {
                if (instance == null) {
                    instance = new DailyFirstRechargeManage();
                }
            }
        }
        return instance;
    }

    public DailyFirstRechargeManage() {

    }

    private final Map<Long, List<Common.Reward>> timeSegmentRewards = new HashMap<>();

    private final Map<Integer, Integer> rewardsWeightMap = new HashMap<>();

    private final Map<Integer, List<Common.Reward>> rewardsMap = new HashMap<>();

    private static final int towerFloor = 6;

    private static final int totalRewardNum = (1 + towerFloor) * towerFloor / 2;

    private static int cycleDays;

    private static List<Integer> extraTimesDays;

    private static List<Integer> bigRewards;

    @Getter
    private static int bigRewardMarqueeId;


    public boolean init() {
        long startTime = TimeUtil.getNextDaysResetTime(GlobalTick.getInstance().getCurrentTime(), 0);
        DailyFirstRechargeObject config = DailyFirstRecharge.getById(GameConst.CONFIG_ID);
        if (config == null) {
            LogUtil.error("DailyFirstRecharge config is empty by id:{}", GameConst.CONFIG_ID);
            return false;
        }
        if (!initTimeSegmentRewards(startTime, config)) {
            return false;
        }
        if (!initRewardsWeightMap(config)) {
            return false;
        }
        if (!initRewardsMap(config)) {
            return false;
        }
        cycleDays = config.getRechargecycle();
        extraTimesDays = Arrays.stream(config.getExtratimesday()).boxed().collect(Collectors.toList());
        bigRewards = Arrays.stream(config.getBigrewardindex()).boxed().collect(Collectors.toList());
        bigRewardMarqueeId = config.getBigrewardmarqueeid();
        return true;
    }

    /**
     * 初始化宝塔奖励
     *
     * @param config
     * @return
     */
    private boolean initRewardsMap(DailyFirstRechargeObject config) {
        int[][] towerReward = config.getTowerreward();
        if (ArrayUtils.isEmpty(towerReward)) {
            LogUtil.error("DailyFirstRecharge config towerReward empty by id:{}", GameConst.CONFIG_ID);
            return false;

        }
        for (int[] rewardIds : towerReward) {
            for (int i = 0; i < rewardIds.length; i++) {
                List<Common.Reward> rewards = RewardUtil.getRewardsByRewardId((rewardIds[i]));
                rewardsMap.put(calculateRewardIndex(rewardIds.length, i), rewards);
            }
        }
        if (rewardsMap.size() != totalRewardNum) {
            LogUtil.error("DailyFirstRecharge towerReward  init failed by rewardsNum not enough ");
            return false;
        }
        return true;
    }

    /**
     * 初始化宝塔奖励权重
     *
     * @param config
     * @return
     */
    private boolean initRewardsWeightMap(DailyFirstRechargeObject config) {
        int[][] towerWeight = config.getTowerweight();
        if (ArrayUtils.isEmpty(towerWeight)) {
            LogUtil.error("DailyFirstRecharge config towerReward empty by id:{}", GameConst.CONFIG_ID);
            return false;

        }
        for (int[] weights : towerWeight) {
            for (int i = 0; i < weights.length; i++) {
                rewardsWeightMap.put(calculateRewardIndex(weights.length, i), weights[i]);
            }
        }
        if (rewardsWeightMap.size() != totalRewardNum) {
            LogUtil.error("DailyFirstRecharge towerReward  init failed by weightNum not enough ");
            return false;
        }
        return true;

    }

    private int calculateRewardIndex(int curFloor, int curFloorIndex) {
        return curFloor * (1 + curFloor) / 2 - curFloor + curFloorIndex;
    }

    /**
     * 初始化每个时间段奖励
     *
     * @param startTime
     * @param config
     * @return
     */
    private boolean initTimeSegmentRewards(long startTime, DailyFirstRechargeObject config) {
        int[] segmentDays = config.getSegmentdays();
        if (ArrayUtils.isEmpty(segmentDays)) {
            LogUtil.error("DailyFirstRecharge segmentDays is empty ");
            return false;
        }
        int[] dailyReward = config.getDailyreward();
        if (segmentDays.length != dailyReward.length) {
            LogUtil.error("DailyFirstRecharge segmentDays  length not equal dailyReward length ");
            return false;
        }

        List<Common.Reward> rewards = null;
        for (int i = 0; i < segmentDays.length; i++) {
            startTime += segmentDays[i] * TimeUtil.MS_IN_A_DAY;
            rewards = RewardUtil.getRewardsByRewardId(dailyReward[i]);
            if (CollectionUtils.isEmpty(rewards)) {
                LogUtil.error("DailyFirstRecharge dailyReward is empty by rewardId :{}", dailyReward[i]);
                return false;
            }
            timeSegmentRewards.put(startTime, rewards);
        }
        timeSegmentRewards.put(Long.MAX_VALUE, rewards);
        return true;
    }

    /**
     * 查询每日奖励
     *
     * @return
     */
    public List<Common.Reward> queryDailyReward() {
        long currentTime = GlobalTick.getInstance().getCurrentTime();
        return queryDailyReward(currentTime);
    }

    public List<Common.Reward> queryDailyReward(long time) {
        Optional<Long> min = timeSegmentRewards.keySet().stream().filter(e -> time < e).min(Long::compareTo);
        if (min.isPresent()) {
            return timeSegmentRewards.get(min.get());
        }
        return timeSegmentRewards.get(Long.MAX_VALUE);
    }


    /**
     * @param earnedRewardIndexList 玩家剩余已经获取的奖励索引
     * @return <奖励索引,奖励集合>
     */
    public Pair<Integer, List<Common.Reward>> randomReward(List<Integer> earnedRewardIndexList) {
        Integer rewardId = randomRewardIndex(earnedRewardIndexList);
        if (rewardId == null) {
            LogUtil.error("DailyFirstPayManager randomRewards error, please check logic,earnedRewardIndexList:{}", earnedRewardIndexList);
            return null;
        }
        List<Common.Reward> rewards = rewardsMap.get(rewardId);
        return new Pair<>(rewardId, rewards);
    }


    public Integer randomRewardIndex(List<Integer> earnedRewardIndexList) {
        int totalWeights = calculateTotalWeights(earnedRewardIndexList);
        int random = RandomUtils.nextInt(totalWeights);
        for (Map.Entry<Integer, Integer> entry : rewardsWeightMap.entrySet()) {
            if (!earnedRewardIndexList.contains(entry.getKey())) {
                if (random < entry.getValue()) {
                    return entry.getKey();
                }
                random -= entry.getValue();
            }

        }
        return null;
    }

    private int calculateTotalWeights(List<Integer> earnedRewardIndexList) {
        return rewardsWeightMap.entrySet().stream().filter(entry -> !earnedRewardIndexList.contains(entry.getKey())).mapToInt(Map.Entry::getValue).sum();
    }

    public boolean activityOpen() {
        return !ActivityManager.getInstance().activityIsEnd(ActivityUtil.LocalActivityId.DailyFirstRecharge);
    }

    public int increaseRechargeDays(int rechargeDays) {
        rechargeDays++;
        if (rechargeDays > cycleDays) {
            rechargeDays = 1;
        }
        return rechargeDays;
    }

    public int increaseExploreTimes(int exploreTimes, int rechargeDays) {
        exploreTimes++;
        if (extraTimesDays.contains(rechargeDays)) {
            exploreTimes++;
        }
        return exploreTimes;
    }

    public boolean bigReward(Integer key) {
        return bigRewards.contains(key);
    }

    public boolean earnAllRewards(int earnedRewardCount) {
        return earnedRewardCount >= totalRewardNum;
    }
}
