package model.itembag;

import cfg.GameConfig;
import cfg.GameConfigObject;
import cfg.Item;
import cfg.ItemObject;
import common.GameConst;
import javafx.util.Pair;
import lombok.Getter;
import model.reward.RewardUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.CollectionUtils;
import protocol.Bag;
import protocol.Common;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlindBoxManager {
    @Getter
    private static BlindBoxManager instance = new BlindBoxManager();

    private int[] blindBoxRewardNum;
    private int[] blindBoxRewardWeight;

    private int totalRewardRarityWeight;


    /**
     * map<盲盒id,<奖励品质,List<奖励>>
     */
    private static final Map<Integer, Map<Integer, List<Bag.BlindBoxReward>>> blindBoxMap = new HashMap<>();

    public boolean init() {
        GameConfigObject gameConfig = GameConfig.getById(GameConst.CONFIG_ID);
        blindBoxRewardNum = gameConfig.getBlindboxreardnum();
        blindBoxRewardWeight = gameConfig.getBlindboxreardweight();
        if (blindBoxRewardNum.length != blindBoxRewardWeight.length || blindBoxRewardNum.length <= 0) {
            LogUtil.error("GameConfig.blindBoxRewardNum&blindBoxRewardWeight error,length not same or empty");
            return false;
        }
        totalRewardRarityWeight = Arrays.stream(blindBoxRewardWeight).sum();


        for (ItemObject item : Item._ix_id.values()) {
            if (item == null || ItemConst.ItemType.Blind_Box != item.getSpecialtype()) {
                continue;
            }
            int[][] paramStr = item.getParamstr();
            if (ArrayUtils.isEmpty(paramStr)) {
                LogUtil.error("initBlindBoxRewards error,item paramStr is empty,itemId:{}", item.getId());
                return false;
            }
            Map<Integer, List<Bag.BlindBoxReward>> rewardMap = new HashMap<>();
            int itemRarity;
            for (int[] ints : paramStr) {
                if (ints.length < 5) {
                    LogUtil.error("initBlindBoxRewards error,item paramStr item length not enough,need 5 length,itemId:{}", item.getId());
                    return false;
                }
                itemRarity = ints[4];
                Bag.BlindBoxReward.Builder builder = Bag.BlindBoxReward.newBuilder();
                builder.setReward(RewardUtil.parseReward(ints)).setRewardRarityValue(itemRarity).setProperty(ints[3]);
                List<Bag.BlindBoxReward> pairList = rewardMap.computeIfAbsent(itemRarity, v -> new ArrayList<>());

                pairList.add(builder.build());
            }
            blindBoxMap.put(item.getId(), rewardMap);
        }
        return true;
    }

    public int randomRewardRarityByCfg() {
        int random = RandomUtils.nextInt(totalRewardRarityWeight);

        for (int i = 0; i < totalRewardRarityWeight; i++) {
            if (random < blindBoxRewardWeight[i]) {
                return i + 1;
            }
            random -= blindBoxRewardWeight[i];
        }
        return 1;
    }


    /**
     * 随机出每个转盘的奖励
     * @param cfgId 道具id
     * @return
     */
    public List<Bag.BlindBoxReward> randomBlindBoxShowRewards(int cfgId) {
        Map<Integer, List<Bag.BlindBoxReward>> integerListMap = blindBoxMap.get(cfgId);

        List<Bag.BlindBoxReward> result = new ArrayList<>();
        List<Bag.BlindBoxReward> rewardPool;
        for (int index = 0; index < blindBoxRewardNum.length; index++) {
            rewardPool = integerListMap.get(index + 1);
            if (rewardPool != null) {
                randomShowRewards(rewardPool, blindBoxRewardNum[index], result);
            }
        }
        if (CollectionUtils.isEmpty(result)) {
            LogUtil.error("randomBlindBoxShowRewards is empty by itemCfgId:{}", cfgId);
        }
        Collections.shuffle(result);
        return result;
    }

    private static List<Bag.BlindBoxReward> randomShowRewards(List<Bag.BlindBoxReward> pool, int num, List<Bag.BlindBoxReward> result) {
        if (CollectionUtils.isEmpty(pool) || num <= 0) {
            return Collections.emptyList();
        }
        int totalWeight = pool.stream().mapToInt(Bag.BlindBoxReward::getProperty).sum();


        while (num >= pool.size()) {
            result.addAll(pool);
            num -= pool.size();
        }

        List<Bag.BlindBoxReward> noRepeatReward = new ArrayList<>();
        Bag.BlindBoxReward reward;
        while (num > 0) {
            reward = randomOneFromList(pool, totalWeight);
            if (!noRepeatReward.contains(reward) && reward != null) {
                num--;
                noRepeatReward.add(reward);
            }

        }
        result.addAll(noRepeatReward);
        return result;
    }

    private static Bag.BlindBoxReward randomOneFromList(List<Bag.BlindBoxReward> pool, int totalWeight) {
        int random = RandomUtils.nextInt(totalWeight);
        for (Bag.BlindBoxReward reward : pool) {
            if (random < reward.getProperty()) {
                return reward;
            }
            random -= reward.getProperty();
        }
        return null;
    }

    /**
     * 从转盘的展示奖励中随机出奖励
     * @param rewardListList
     * @return
     */
    public Pair<Integer, List<Common.Reward>> randomRewardFromShowReward(List<Bag.BlindBoxReward> rewardListList) {
        int itemRarity = BlindBoxManager.getInstance().randomRewardRarityByCfg();
        List<Bag.BlindBoxReward> rewards = rewardListList.stream().filter(e -> e.getRewardRarityValue() == itemRarity).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rewards)) {
            LogUtil.error("doBlindBoxReward error ,rewards is empty,itemRarity:{},rewardSource:{}",itemRarity,rewardListList);
            return null;
        }
        int randomIndex = RandomUtils.nextInt(rewards.size());
        Bag.BlindBoxReward reward = rewards.get(randomIndex);
        int rewardIndex = rewardListList.indexOf(reward);
        return new Pair<>(rewardIndex, Collections.singletonList(rewards.get(randomIndex).getReward()));
    }

    /**
     * 批量随机奖励
     * @param itemCfgId
     * @param useCount
     * @return
     */
    public List<Common.Reward> batchRandomReward(int itemCfgId, int useCount) {
        List<Common.Reward> result = new ArrayList<>();
        List<Bag.BlindBoxReward> blindBoxRewards;
        Pair<Integer, List<Common.Reward>> randomResult;
        for (int i = 0; i < useCount; i++) {
            blindBoxRewards = randomBlindBoxShowRewards(itemCfgId);
            randomResult = randomRewardFromShowReward(blindBoxRewards);
            if (randomResult != null) {
                result.addAll(randomResult.getValue());
            }
        }
        return result;
    }
}
