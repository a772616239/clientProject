package model.reward;

import cfg.FightMake;
import cfg.FightMakeObject;
import cfg.GameConfig;
import cfg.GameConfigObject;
import cfg.Head;
import cfg.Item;
import cfg.ItemObject;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetFragmentConfig;
import cfg.PetFragmentConfigObject;
import cfg.PetRuneProperties;
import cfg.RewardConfig;
import cfg.RewardConfigObject;
import cfg.ServerStringRes;
import cfg.VIPConfig;
import cfg.VIPConfigObject;
import common.GameConst;
import common.GlobalData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import model.crossarena.CrossArenaManager;
import model.itembag.entity.itembagEntity;
import static model.mainLine.entity.mainlineEntity.MAINLINE_ON_HOOK_RANDOM_TOTAL_ODDS;
import model.pet.dbCache.petCache;
import model.petgem.dbCache.petgemCache;
import model.petrune.dbCache.petruneCache;
import model.player.util.PlayerUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import platform.logs.ReasonManager;
import protocol.Bag.BagTypeEnum;
import protocol.Common;
import protocol.Common.RandomReward;
import protocol.Common.Reward;
import protocol.Common.Reward.Builder;
import protocol.Common.RewardTypeEnum;
import protocol.PetMessage;
import protocol.RetCodeId.RetCodeEnum;
import server.http.entity.PlatformReward;
import util.ArrayUtil;
import util.GameUtil;
import util.LogUtil;

public class RewardUtil {

    public static List<Reward> parseRewardIntArrayToRewardList(int[][] rewardIntArray) {
        if (rewardIntArray == null) {
            return null;
        }
        List<Reward> rewardList = new ArrayList<>();
        for (int[] ints : rewardIntArray) {
            if (ints.length == 3 && checkReward(ints[0], ints[1], ints[2])) {
                Reward reward1 = parseReward(ints[0], ints[1], ints[2]);
                if (reward1 != null) {
                    rewardList.add(reward1);
                }
            }
        }
        return rewardList;
    }

    public static Reward parseReward(int[] params) {
        if (params == null || params.length < 3) {
            return null;
        }
        return parseReward(RewardTypeEnum.forNumber(params[0]), params[1], params[2]);
    }

    public static Reward.Builder parseRewardBuilder(int[] params) {
        if (params == null || params.length < 3) {
            return null;
        }
        return parseRewardBuilder(RewardTypeEnum.forNumber(params[0]), params[1], params[2]);
    }

    public static Map<RewardTypeEnum, List<Reward>> classifyByRewardType(List<Reward> rewardList) {
        if (rewardList == null) {
            LogUtil.warn("classifyByRewardType, rewardList is null");
            return null;
        }

        Map<RewardTypeEnum, List<Reward>> classifyMap = new HashMap<>();
        for (Reward reward : rewardList) {
            RewardTypeEnum type = reward.getRewardType();
            if (type == null || type == RewardTypeEnum.RTE_Null) {
                continue;
            }
            List<Reward> rewards = classifyMap.computeIfAbsent(type, k -> new ArrayList<>());
            rewards.add(reward);
        }

        return classifyMap;
    }

    /**
     * 将随机奖励的 二维数组，变为随机奖励类
     *
     * @param randomRewardStr
     * @return
     */
    public static List<RandomReward> parseIntArrayToRandomRewardList(int[][] randomRewardStr) {
        if (randomRewardStr == null) {
            return null;
        }

        List<RandomReward> randomRewards = new ArrayList<>();
        for (int[] ints : randomRewardStr) {
            if (ints.length >= 4) {
                RandomReward.Builder randomReward = RandomReward.newBuilder();
                randomReward.setRewardType(RewardTypeEnum.forNumber(ints[0]));
                randomReward.setId(ints[1]);
                randomReward.setCount(ints[2]);
                randomReward.setRandomOdds(ints[3]);
                randomRewards.add(randomReward.build());
            }
        }
        return randomRewards;
    }

    public static List<Reward> drawMustRandomReward(int[][] randomRewards, int randomCount) {
        if (randomRewards == null || randomCount <= 0) {
            return null;
        }
        return drawMustRandomReward(parseIntArrayToRandomRewardList(randomRewards), randomCount);
    }

    /**
     * 随机，随机奖励列表，并返回奖励列表,不是必出,千分比随机
     *
     * @param randomRewards
     * @return
     */
    public static List<Reward> drawRandomReward(List<RandomReward> randomRewards) {
        if (randomRewards == null) {
            return null;
        }

        List<Reward> rewards = new ArrayList<>();
        Random random = new Random();
        for (RandomReward randomReward : randomRewards) {
            if (randomReward.getRandomOdds() >= random.nextInt(1000)) {
                Reward.Builder rewardBuilder = Reward.newBuilder();
                rewardBuilder.setRewardType(randomReward.getRewardType());
                rewardBuilder.setId(randomReward.getId());
                rewardBuilder.setCount(randomReward.getCount());
                rewards.add(rewardBuilder.build());
            }
        }
        return rewards;
    }

    public static Reward parseRandomRewardToReward(RandomReward random) {
//        if (!checkRandomReward(random)) {
//            return null;
//        }

        Reward.Builder reward = Reward.newBuilder();
        reward.setRewardType(random.getRewardType());
        reward.setId(random.getId());
        reward.setCount(random.getCount());
        return reward.build();
    }

    public static List<Reward> drawMustRandomReward(int[][] randomReward, int totalOdds, int randomCount) {
        return drawMustRandomReward(parseIntArrayToRandomRewardList(randomReward), totalOdds, randomCount);
    }

    public static List<Reward> drawMustRandomReward(List<RandomReward> randomRewards, int totalOdds, int randomCount) {
        if (randomRewards == null || randomRewards.isEmpty() || randomCount <= 0) {
            return null;
        }

        List<Reward> resultReward = new ArrayList<>();
        Random random = new Random();
        if (totalOdds <= 0) {
            for (int i = 0; i < randomCount; i++) {
                Reward reward = parseRandomRewardToReward(randomRewards.get(random.nextInt(randomRewards.size())));
                if (reward != null) {
                    resultReward.add(reward);
                }
            }
        } else {
            for (int i = 0; i < randomCount; i++) {
                int randomNum = random.nextInt(totalOdds);
                int curOdds = 0;
                for (RandomReward randomReward : randomRewards) {
                    curOdds += randomReward.getRandomOdds();
                    if (curOdds > randomNum) {
                        Reward reward = parseRandomRewardToReward(randomReward);
                        if (reward != null) {
                            resultReward.add(reward);
                        }
                        break;
                    }
                }
            }
        }
        return resultReward;
    }

    public static List<IndexReward> drawMustRandomIndexAndRewardByRewardId(int rewardId) {
        RewardConfigObject rewardCfg = RewardConfig.getByRewardid(rewardId);
        if (rewardCfg == null) {
            LogUtil.error("drawMustRandomIndexAndRewardByRewardId, can not find reward cfg, id:" + rewardId);
            return null;
        }
        return drawMustRandomIndexAndReward(rewardCfg.getRandomreward(), rewardCfg.getRandomtimes());
    }

    public static List<IndexReward> drawMustRandomIndexAndReward(int[][] randomRewardsArray, int drawCount) {
        List<RandomReward> randomRewards = parseIntArrayToRandomRewardList(randomRewardsArray);
        if (CollectionUtils.isEmpty(randomRewards) || drawCount <= 0) {
            return null;
        }
        return drawMustRandomIndexAndReward(randomRewards, calculateTotalOdds(randomRewards), drawCount);
    }

    public static int calculateTotalOdds(List<RandomReward> randomRewards) {
        if (CollectionUtils.isEmpty(randomRewards)) {
            return 0;
        }
        int totalOdds = 0;
        for (RandomReward randomReward : randomRewards) {
            totalOdds += randomReward.getRandomOdds();
        }
        return totalOdds;
    }


    public static List<IndexReward> drawMustRandomIndexAndReward(List<RandomReward> randomRewards, int totalOdds, int randomCount) {
        if (randomRewards == null || randomRewards.isEmpty() || randomCount <= 0) {
            return null;
        }

        List<IndexReward> resultReward = new ArrayList<>();
        Random random = new Random();
        if (totalOdds <= 0) {
            for (int i = 0; i < randomCount; i++) {
                int index = random.nextInt(randomRewards.size());
                Reward reward = parseRandomRewardToReward(randomRewards.get(index));
                if (reward != null) {
                    resultReward.add(new IndexReward(index, reward));
                }
            }
        } else {
            for (int i = 0; i < randomCount; i++) {
                int randomNum = random.nextInt(totalOdds);
                int curOdds = 0;

                for (int j = 0; j < randomRewards.size(); j++) {
                    RandomReward randomReward = randomRewards.get(j);
                    curOdds += randomReward.getRandomOdds();
                    if (curOdds > randomNum) {
                        Reward reward = parseRandomRewardToReward(randomReward);
                        if (reward != null) {
                            resultReward.add(new IndexReward(j, reward));
                        }
                        break;
                    }
                }
            }
        }
        return resultReward;
    }

    /**
     * 必出随机
     *
     * @param randomRewards
     * @param randomCount   随机次数
     * @return
     */
    public static List<Reward> drawMustRandomReward(List<RandomReward> randomRewards, int randomCount) {
        if (randomRewards == null || randomRewards.isEmpty() || randomCount <= 0) {
            return null;
        }

        int totalOdds = 0;
        for (RandomReward randomReward : randomRewards) {
            totalOdds += randomReward.getRandomOdds();
        }

        return drawMustRandomReward(randomRewards, totalOdds, randomCount);
    }

    public static Reward drawMustRandomReward(List<RandomReward> randomRewards) {
        List<Reward> rewards = drawMustRandomReward(randomRewards, 1);
        return rewards == null ? null : rewards.isEmpty() ? null : rewards.get(0);
    }

    public static List<Integer> getCfgIdList(RewardTypeEnum type, List<Reward> rewards) {
        if (type == null || rewards == null) {
            return null;
        }
        List<Integer> list = new ArrayList<>();

        for (Reward reward : rewards) {
            if (reward.getRewardType() == type) {
                list.add(reward.getId());
            }
        }
        return list;
    }

    public static Map<Integer, Integer> getCfgIdCountMap(RewardTypeEnum type, List<Reward> rewards) {
        if (type == null || rewards == null) {
            return null;
        }
        Map<Integer, Integer> cfgCountMap = new HashMap<>();
        for (Reward reward : rewards) {
            if (reward.getRewardType() == type) {
                if (cfgCountMap.containsKey(reward.getId())) {
                    cfgCountMap.put(reward.getId(), cfgCountMap.get(reward.getId()) + reward.getCount());
                } else {
                    cfgCountMap.put(reward.getId(), reward.getCount());
                }
            }
        }
        return cfgCountMap;
    }

    public static List<Reward> getRewardsByFightMakeId(int fightMakeId) {
        FightMakeObject cfg = FightMake.getById(fightMakeId);
        if (cfg == null) {
            return null;
        }

        return getRewardsByRewardId(cfg.getRewardid());
    }

    public static List<Reward> getRewardsByRewardId(int rewardId) {
        RewardConfigObject rewardCfg = RewardConfig.getByRewardid(rewardId);
        if (rewardCfg == null) {
            return null;
        }

        List<Reward> rewards = new ArrayList<>();
        List<Reward> mustRewards = RewardUtil.parseRewardIntArrayToRewardList(rewardCfg.getMustreward());
        if (mustRewards != null && !mustRewards.isEmpty()) {
            rewards.addAll(mustRewards);
        }

        List<Reward> randomRewards = RewardUtil.drawMustRandomReward(rewardCfg.getRandomreward(), rewardCfg.getRandomtimes());
        if (randomRewards != null) {
            rewards.addAll(randomRewards);
        }
        return rewards;
    }

    /**
     * 检查指定的背包容量是否足够
     *
     * @param playerIdx
     * @param rewards
     * @param bagType
     * @return
     */
    public static boolean capacityIsEnough(String playerIdx, List<Reward> rewards, BagTypeEnum bagType) {
        if (rewards == null) {
            return true;
        }

        if (playerIdx == null || bagType == null) {
            LogUtil.error("RewardUtil.capacityIsEnough, error params");
            return false;
        }

        int needCapacity = 0;

        //目前宠物和符文有容量限制
        for (Reward reward : rewards) {
            switch (bagType) {
                case BTE_ItemBag:
                    if (reward.getRewardType() == RewardTypeEnum.RTE_Item) {
                        needCapacity += reward.getCount();
                    }
                    break;
                case BTE_PetBag:
                    if (reward.getRewardType() == RewardTypeEnum.RTE_Pet) {
                        needCapacity += (reward.getCount() == 0 ? 1 : reward.getCount());
                    }
                    break;
                case BTE_RuneBag:
                    if (reward.getRewardType() == RewardTypeEnum.RTE_Rune) {
                        needCapacity += (reward.getCount() == 0 ? 1 : reward.getCount());
                    }
                    break;
                default:
                    break;
            }
        }

        if (bagType == BagTypeEnum.BTE_PetBag) {
            return petCache.getInstance().capacityEnough(playerIdx, needCapacity);
        } else if (bagType == BagTypeEnum.BTE_RuneBag) {
            return petruneCache.getInstance().capacityEnough(playerIdx, needCapacity);
        }
        return false;
    }

    /**
     * 判断玩家所有的背包容量是否足够
     * 目前只有宠物和符文有容量限制
     *
     * @param rewards
     * @return
     */
    public static RetCodeEnum capacityIsEnough(String playerIdx, List<Reward> rewards) {
        if (!capacityIsEnough(playerIdx, rewards, BagTypeEnum.BTE_PetBag)) {
            return RetCodeEnum.RCE_Pet_PetBagNotEnough;
        }

        if (!capacityIsEnough(playerIdx, rewards, BagTypeEnum.BTE_RuneBag)) {
            return RetCodeEnum.RCE_Pet_RuneBagNotEnough;
        }

        return RetCodeEnum.RCE_Success;
    }

    public static boolean capacityIsEnough(String playerIdx, Reward reward) {
        if (reward == null) {
            return true;
        }
        if (playerIdx == null) {
            return false;
        }

        if (reward.getRewardType() == RewardTypeEnum.RTE_Pet) {
            return petCache.getInstance().capacityEnough(playerIdx, reward.getCount());
        } else if (reward.getRewardType() == RewardTypeEnum.RTE_Rune) {
            return petruneCache.getInstance().capacityEnough(playerIdx, reward.getCount());
        }

        return true;
    }

    /**
     * 对给定的奖励进行加倍
     *
     * @param rewards
     * @param multiple
     * @return
     */
    public static List<Reward> multiReward(List<Reward> rewards, int multiple) {
        if (rewards == null || rewards.isEmpty()) {
            return null;
        }

        List<Reward> multi = new ArrayList<>();
        for (Reward reward : rewards) {
            Reward reward1 = multiReward(reward, multiple);
            if (reward1 != null) {
                multi.add(reward1);
            }
        }

        return multi;
    }

    /**
     * 对给定奖励进行加倍
     *
     * @param reward
     * @param multiple
     * @return
     */
    public static Reward multiReward(Reward reward, int multiple) {
        if (reward == null) {
            return null;
        }

        if (multiple <= 0) {
            return null;
        }

        Builder builder = reward.toBuilder();
        builder.setCount(GameUtil.multi(builder.getCount(), multiple));
        return builder.build();
    }

    /**
     * 合并相同的奖励
     *
     * @param rewardList
     * @return
     */
    public static List<Reward> mergeReward(List<Reward> rewardList) {
        if (rewardList == null || rewardList.isEmpty()) {
            return rewardList;
        }


        List<Reward.Builder> rewardBuilder = new ArrayList<>();
        boolean contain;
        for (Reward reward : rewardList) {
            if (reward == null) {
                continue;
            }
            contain = false;
            for (Builder builder : rewardBuilder) {
                if (reward.getRewardType() == builder.getRewardType() && reward.getId() == builder.getId()) {
                    builder.setCount(builder.getCount() + reward.getCount());
                    contain = true;
                }
            }
            if (!contain) {
                rewardBuilder.add(reward.toBuilder());
            }
        }

        List<Reward> rewards = new ArrayList<>();
        for (Builder builder : rewardBuilder) {
            rewards.add(builder.build());
        }
        return rewards;
    }


    @SafeVarargs
    public static List<Reward> mergeRewardList(Collection<Reward>... rewardList) {
        List<Reward> totalReward = new ArrayList<>();
        for (Collection<Reward> rewards : rewardList) {
            if (!CollectionUtils.isEmpty(rewards)) {
                totalReward.addAll(rewards);
            }
        }
        return mergeReward(totalReward);
    }

    /**
     * 检查reward的配置是否正确
     *
     * @param rewardIntArr reward的配置数组
     * @return
     */
    public static boolean checkRewardByIntArr(int[][] rewardIntArr) {
        if (rewardIntArr == null) {
            return true;
        }

        for (int[] ints : rewardIntArr) {
            if (ints.length != 3 || !checkReward(ints[0], ints[1], ints[2])) {
                LogUtil.warn("reward is not exist, type:" + ints[0] + ", id:" + ints[1] + ",count:" + ints[2]);
            }
        }

        return true;
    }


    public static boolean checkRandomReward(RandomReward randomReward) {
        if (randomReward == null || randomReward.getRandomOdds() <= 0) {
            return false;
        }

        return checkReward(randomReward.getRewardTypeValue(), randomReward.getId(), randomReward.getCount());
    }

    /**
     * 检查随机奖励的配置是否正确
     *
     * @param randomRewardIntArr
     * @return
     */
    public static boolean checkRandomReward(int[][] randomRewardIntArr) {
        if (randomRewardIntArr == null) {
            return false;
        }

        for (int[] ints : randomRewardIntArr) {
            if (ints.length != 4) {
                continue;
            }
            if (!checkReward(ints[0], ints[1], ints[2])) {
                return false;
            }

            if (ints[3] > MAINLINE_ON_HOOK_RANDOM_TOTAL_ODDS) {
                LogUtil.warn("randomReward rate = " + ints[3]);
            }
        }

        return true;
    }

    /**
     * 检查reward的配置是否正确
     *
     * @param rewardType
     * @param rewardId
     * @param rewardCount
     * @return
     */
    public static boolean checkReward(int rewardType, int rewardId, int rewardCount) {
        if (rewardCount <= 0) {
            return false;
        }

        RewardTypeEnum rewardTypeEnum = RewardTypeEnum.forNumber(rewardType);
        if (rewardTypeEnum == null || rewardTypeEnum == RewardTypeEnum.RTE_Null) {
            return false;
        }

        switch (rewardTypeEnum) {
            case RTE_Item:
                if (Item.getById(rewardId) == null) {
                    LogUtil.error("undefined item id, id = " + rewardId);
                    return false;
                }
                break;
            case RTE_Pet:
                if (PetBaseProperties.getByPetid(rewardId) == null) {
                    LogUtil.error("undefined pet id, id = " + rewardId);
                    return false;
                }
                break;
            case RTE_PetFragment:
                if (PetFragmentConfig.getById(rewardId) == null) {
                    LogUtil.error("undefined petFragment id, id = " + rewardId);
                    return false;
                }
                break;
            case RTE_Rune:
                if (PetRuneProperties.getByRuneid(rewardId) == null) {
                    LogUtil.error("undefined petRune id, id = " + rewardId);
                    return false;
                }
                break;
            case RTE_Avatar:
                if (Head.getById(rewardId) == null) {
                    LogUtil.error("undefined avatar id, id = " + rewardId);
                    return false;
                }
                break;
            default:
                break;
        }
        return true;
    }

    public static Reward parseReward(int type, int id, int count) {
        if (type <= 0 || id < 0 || count <= 0 || !checkReward(type, id, count)) {
            return null;
        }

        Reward.Builder builder = Reward.newBuilder();
        builder.setRewardType(RewardTypeEnum.forNumber(type));
        builder.setId(id);
        builder.setCount(count);
        return builder.build();
    }


    public static Reward parseReward(RewardTypeEnum type, int id, int count) {
        if (type == null || type == RewardTypeEnum.RTE_Null || id < 0 || count <= 0) {
            return null;
        }

        return parseRewardBuilder(type, id, count).build();
    }

    public static Reward.Builder parseRewardBuilder(RewardTypeEnum type, int id, int count) {
        Reward.Builder builder = Reward.newBuilder();
        if (type != null) {
            builder.setRewardType(type);
        }
        builder.setId(id);
        builder.setCount(count);
        return builder;
    }

    public static List<Reward> parseRewardList(RewardTypeEnum type, int id, int count) {
        if (type == null || type == RewardTypeEnum.RTE_Null || id < 0 || count <= 0) {
            return null;
        }

        List<Reward> list = new ArrayList<>();
        list.add(parseReward(type, id, count));
        return list;
    }

    public static RandomReward parseRewardToRandom(Reward reward, int odds) {
        if (reward == null) {
            return null;
        }
        RandomReward.Builder builder = RandomReward.newBuilder();
        builder.setRewardType(reward.getRewardType());
        builder.setCount(reward.getCount());
        builder.setId(reward.getId());
        builder.setRandomOdds(odds);
        return builder.build();
    }

    public static List<Reward> parseAndMulti(int[][] intReward, int multi) {
        return multiReward(parseRewardIntArrayToRewardList(intReward), multi);
    }

    public static Reward parseAndMulti(int[] intReward, int multi) {
        return multiReward(parseReward(intReward), multi);
    }

    /**
     * 移除指定类型的奖励
     *
     * @return
     */
    public static List<Reward> removeRewardType(List<Reward> rewards, RewardTypeEnum removeType) {
        if (rewards == null || rewards.isEmpty()) {
            return rewards;
        }

        List<Reward> removeReward = new ArrayList<>();
        for (Reward reward : rewards) {
            if (reward.getRewardType() == removeType) {
                removeReward.add(reward);
            }
        }

        rewards.removeAll(removeReward);
        return rewards;
    }

    /**
     * 拿到可以发放的奖励,所有字段都不能为空
     *
     * @param rewards      奖励列表
     * @param remainReward 剩余的奖励,不能为空
     * @param reason
     * @return 可以发放的奖励
     */
    public static List<Reward> getCanDoReward(String playerIdx, List<Reward> rewards, List<Reward> remainReward, ReasonManager.Reason reason) {
        if (playerIdx == null || rewards == null || remainReward == null) {
            return null;
        }
        remainReward.clear();

        List<Reward> doRewards = new ArrayList<>();
        int petRemainCapacity = petCache.getInstance().getRemainCapacity(playerIdx);
        int runeRemainCapacity = petruneCache.getInstance().getRemainCapacity(playerIdx);
        int gemRemainCapacity = petgemCache.getInstance().getRemainCapacity(playerIdx);
        int remainCrossScoreCount = CrossArenaManager.getInstance().getRemainScoreItemCount(playerIdx);
        for (Reward reward : rewards) {
            if (reward.getCount() <= 0) {
                continue;
            }

            int canGetCount;
            boolean canRemain = true;
            if (reward.getRewardType() == RewardTypeEnum.RTE_Pet) {
                canGetCount = Math.min(reward.getCount(), petRemainCapacity);
                petRemainCapacity -= canGetCount;
            } else if (reward.getRewardType() == RewardTypeEnum.RTE_Rune) {
                canGetCount = Math.min(reward.getCount(), runeRemainCapacity);
                runeRemainCapacity -= canGetCount;
            } else if (reward.getRewardType() == RewardTypeEnum.RTE_Gem) {
                canGetCount = Math.min(reward.getCount(), gemRemainCapacity);
                gemRemainCapacity -= canGetCount;
            } else if (reward.getRewardType() == RewardTypeEnum.RTE_Item){
                if (itembagEntity.crossArenaLimitScoreItem(reward.getId(), reason)) {
                    canRemain = false;
                    canGetCount = Math.min(reward.getCount(), remainCrossScoreCount);
                    remainCrossScoreCount -= canGetCount;
                } else {
                    canGetCount = reward.getCount();
                }
            } else {
                canGetCount = reward.getCount();
            }

            if (canGetCount == 0 ) {
                if (canRemain) {
                    remainReward.add(reward);
                }
            } else if (canGetCount < reward.getCount() && canRemain) {
                Builder builder = reward.toBuilder();
                builder.setCount(canGetCount);
                doRewards.add(builder.build());

                builder.setCount(reward.getCount() - canGetCount);
                remainReward.add(builder.build());
            } else {
                doRewards.add(reward);
            }
        }
        return doRewards;
    }


    public static int getAllCount(RewardTypeEnum typeEnum, List<Reward> rewards) {
        if (rewards == null || rewards.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Reward reward : rewards) {
            if (reward.getRewardType() == typeEnum) {
                count += reward.getCount();
            }
        }
        return count;
    }

    public static String getBagFullName(String playerIdx, List<Reward> rewards) {
        if (rewards == null) {
            return "";
        }

        int id = -1;
        for (Reward reward : rewards) {
            if (reward.getRewardType() == RewardTypeEnum.RTE_Rune) {
                id = 66;
                break;
            }

            if (reward.getRewardType() == RewardTypeEnum.RTE_Pet) {
                id = 67;
                break;
            }

            if (reward.getRewardType() == RewardTypeEnum.RTE_Item) {
                id = 65;
                break;
            }
        }

        if (id == -1) {
            return "";
        }

        return ServerStringRes.getContentByLanguage(id, PlayerUtil.queryPlayerLanguage(playerIdx));
    }

    /**
     * 千分比加成奖励( = (rate + 1000) / 1000
     */
    public static List<Reward> additionRewardByRate1000(List<Reward> rewards, int rate) {
        if (GameUtil.collectionIsEmpty(rewards) || rate <= 0) {
            return rewards;
        }

        List<Reward> result = new ArrayList<>();
        for (Reward reward : rewards) {
            int newCount = ((rate + 1000) * reward.getCount()) / 1000;
            result.add(reward.toBuilder().setCount(newCount).build());
        }
        return result;
    }

    /**
     * 过滤奖励
     *
     * @param target
     * @return
     */
    public static List<Reward> filterRewards(List<Reward> target, Predicate<Reward> filter) {
        if (target == null || filter == null) {
            return target;
        }

        List<Reward> result = new ArrayList<>();
        for (Reward reward : target) {
            if (reward == null) {
                continue;
            }
            if (filter.test(reward)) {
                result.add(reward);
            }
        }
        return result;
    }

    /**
     * 加成奖励通过千分比
     *
     * @return
     */
    public static List<Reward> multiRewardsByPerThousand(List<Reward> rewards, int addition) {
        if (GameUtil.collectionIsEmpty(rewards) || addition <= 0) {
            return rewards;
        }

        List<Reward> result = new ArrayList<>();
        for (Reward reward : rewards) {
            result.add(multiRewardByPerThousand(reward, addition));
        }
        return result;
    }

    public static Reward multiRewardByPerThousand(Reward reward, int addition) {
        if (reward == null || addition == 0) {
            return reward;
        }
        int newCount = (reward.getCount() * addition) / 1000;
        if (newCount <= 0) {
            return reward;
        }
        return reward.toBuilder().setCount(newCount).build();
    }

    /**
     * 发送玩家获得宠物奖励跑马灯
     *
     * @param playerIdx
     * @param petCfgId
     */
    public static void sendPetGainReward(String playerIdx, int petCfgId, int count) {
        PetBasePropertiesObject petBaseCfg = PetBaseProperties.getByPetid(petCfgId);
        if (petBaseCfg == null) {
            LogUtil.error("model.reward.RewardUtil.sendPetGainReward, can not find petCfg:" + petCfgId);
            return;
        }

        GameConfigObject gameCfg = GameConfig.getById(GameConst.CONFIG_ID);
        if (gameCfg == null) {
            LogUtil.error("model.reward.RewardUtil.sendPetGainReward, gameCfg is not exist");
            return;
        }

        if (!ArrayUtil.intArrayContain(gameCfg.getRunquality(), petBaseCfg.getStartrarity())) {
            return;
        }

        //发送到所有玩家
        Map<Integer, String> name = ServerStringRes.buildLanguageContentMap(petBaseCfg.getPetname());
        String playerName = PlayerUtil.queryPlayerName(playerIdx);
        for (String onlinePlayerIdx : GlobalData.getInstance().getAllOnlinePlayerIdx()) {
            String paramName = name.get(PlayerUtil.queryPlayerLanguage(onlinePlayerIdx).getNumber());
            if (StringUtils.isNotBlank(paramName)) {
                GlobalData.getInstance().sendMarqueeToPlayer(onlinePlayerIdx, gameCfg.getRunmarqueeid(), playerName, paramName, count);
            }
        }
    }

    /**
     * 发送玩家获得宠物碎片奖励跑马灯
     *
     * @param playerIdx
     * @param fragmentId
     */
    public static void sendPetFragmentGainReward(String playerIdx, int fragmentId, int count) {
        if (count <= 0) {
            return;
        }
        PetFragmentConfigObject fragmentCfg = PetFragmentConfig.getById(fragmentId);
        if (fragmentCfg == null) {
            LogUtil.error("model.reward.RewardUtil.sendPetFragmentGainReward, can not find pet fragment cfg by fragmentId:" + fragmentId);
            return;
        }

        //不能合成一个完整的魔灵不播放
        if (count < fragmentCfg.getAmount()) {
            return;
        }

        GameConfigObject gameCfg = GameConfig.getById(GameConst.CONFIG_ID);
        if (gameCfg == null) {
            LogUtil.error("model.reward.RewardUtil.sendPetFragmentGainReward, gameCfg is not exist");
            return;
        }

        if (!ArrayUtil.intArrayContain(gameCfg.getRunquality(), fragmentCfg.getDebrisrarity())) {
            return;
        }

        //发送到所有玩家
        Map<Integer, String> name = ServerStringRes.buildLanguageContentMap(fragmentCfg.getNamelanguage());
        String playerName = PlayerUtil.queryPlayerName(playerIdx);
        for (String onlinePlayerIdx : GlobalData.getInstance().getAllOnlinePlayerIdx()) {
            String paramName = name.get(PlayerUtil.queryPlayerLanguage(onlinePlayerIdx).getNumber());
            if (StringUtils.isNotBlank(paramName)) {
                GlobalData.getInstance().sendMarqueeToPlayer(onlinePlayerIdx, gameCfg.getRunmarqueeid(), playerName, paramName, count);
            }
        }
    }

    public static List<Reward> additionResourceCopyRewardByVip(int vipLv, List<Reward> rewardsByFightMakeId) {
        List<Reward> result = new ArrayList<>();
        VIPConfigObject vipConfig = VIPConfig.getById(vipLv);
        if (vipConfig == null || CollectionUtils.isEmpty(rewardsByFightMakeId)) {
            return rewardsByFightMakeId;
        }
        int addition = vipConfig.getRescopyexaddtion();
        for (Reward reward : rewardsByFightMakeId) {
            int newCount = (int) Math.min(Integer.MAX_VALUE, reward.getCount() * (100F + addition) / 100);
            result.add(reward.toBuilder().setCount(newCount).build());
        }
        return result;
    }


    /**
     * 暂时只支持获取道具,宠物,宠物碎片,符文
     *
     * @param rewardType
     * @param id
     * @return
     */
    public static int getQuality(RewardTypeEnum rewardType, int id) {
        if (rewardType == null || id <= 0) {
            return 0;
        }
        return getQuality(rewardType.getNumber(), id);
    }

    /**
     * 暂时只支持获取道具,宠物,宠物碎片,符文
     *
     * @param rewardTypeValue
     * @param id
     * @return
     */
    public static int getQuality(int rewardTypeValue, int id) {
        if (rewardTypeValue == RewardTypeEnum.RTE_Item_VALUE) {
            return Item.getQuality(id);
        } else if (rewardTypeValue == RewardTypeEnum.RTE_Pet_VALUE) {
            return PetBaseProperties.getQualityByPetId(id);
        } else if (rewardTypeValue == RewardTypeEnum.RTE_PetFragment_VALUE) {
            return PetFragmentConfig.getQualityByCfgId(id);
        } else if (rewardTypeValue == RewardTypeEnum.RTE_Rune_VALUE) {
            return PetRuneProperties.getQualityByCfgId(id);
        }
        return 0;
    }

    public static List<Reward> excludeAutoUseItem(List<Reward> rewards) {
        if (CollectionUtils.isEmpty(rewards)) {
            return rewards;
        }
        List<Reward> result = new ArrayList<>();
        for (Reward reward : rewards) {
            if (isAutoUseItem(reward)) {
                continue;
            }
            result.add(reward);
        }
        return result;
    }

    public static boolean isAutoUseItem(Reward reward) {
        if (reward == null || reward.getRewardType() != RewardTypeEnum.RTE_Item) {
            return false;
        }

        ItemObject itemCfg = Item.getById(reward.getId());
        if (itemCfg == null) {
            return false;
        }
        return itemCfg.getAutouse();
    }

    public static String toJsonStr(Collection<Reward> rewards) {
        if (CollectionUtils.isEmpty(rewards)) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (Reward reward : rewards) {
            if (reward == null) {
                continue;
            }
            builder.append("{");
            builder.append("rewardType:");
            builder.append(reward.getRewardType());
            builder.append(",id:");
            builder.append(reward.getId());
            builder.append(",count:");
            builder.append(reward.getCount());
            builder.append("}");
        }
        builder.append("]");
        return builder.toString();
    }


    public static List<Reward> gemToReward(List<PetMessage.Gem> Gems) {
        if (CollectionUtils.isEmpty(Gems)) {
            return Collections.emptyList();
        }
        List<Reward> rewards = new ArrayList<>();
        for (PetMessage.Gem Gem : Gems) {
            rewards.add(Reward.newBuilder().setId(Gem.getGemConfigId()).setRewardType(RewardTypeEnum.RTE_Gem).setCount(1).build());
        }
        return rewards;
    }

    public static List<Reward> runeToReward(List<PetMessage.Rune> runes) {
        if (CollectionUtils.isEmpty(runes)) {
            return Collections.emptyList();
        }
        List<Reward> rewards = new ArrayList<>();
        for (PetMessage.Rune rune : runes) {
            rewards.add(Reward.newBuilder().setId(rune.getRuneBookId()).setRewardType(RewardTypeEnum.RTE_Rune).setCount(1).build());
        }
        return rewards;
    }

    public static List<Common.RuneReward> runeToRuneReward(List<PetMessage.Rune> runes) {
        if (CollectionUtils.isEmpty(runes)) {
            return Collections.emptyList();
        }
        List<Common.RuneReward> rewards = new ArrayList<>();
        for (PetMessage.Rune rune : runes) {
            rewards.add(Common.RuneReward.newBuilder().setCfgId(rune.getRuneBookId()).setLevel(rune.getRuneLvl()).build());
        }
        return rewards;
    }

    public static List<Reward> platformRewards2Rewards(List<PlatformReward> rewards) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(rewards)) {
            return Collections.emptyList();
        }
        List<Reward> result = new ArrayList<>();
        for (PlatformReward reward : rewards) {
            Reward tempReward = RewardUtil.parseReward(reward.getRewardType(), reward.getId(), reward.getCount());
            if (tempReward != null) {
                result.add(tempReward);
            }
        }
        return result;
    }

    public static Common.ListReward.Builder rewards2RewardList(List<Reward> rewards) {
        Common.ListReward.Builder result = Common.ListReward.newBuilder();
        if (!CollectionUtils.isEmpty(rewards)) {
            result.addAllReward(rewards);
        }
        return result;
    }


}


