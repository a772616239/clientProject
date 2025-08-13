package model.activity;

import cfg.PetBaseProperties;
import cfg.ScratchLotteryParams;
import cfg.ScratchLotteryParamsObject;
import cfg.ScratchLotteryReward;
import cfg.ScratchLotteryRewardObject;
import common.GameConst;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import model.activity.entity.Lottery;
import model.drawCard.DrawCardUtil;
import model.drawCard.OddsChangeQuality;
import model.drawCard.QualityWeight;
import model.reward.RewardUtil;
import model.targetsystem.dbCache.targetsystemCache;
import model.targetsystem.entity.targetsystemEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import protocol.Activity.ActivityTypeEnum;
import protocol.Common.Reward;
import protocol.Server.ServerActivity;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/03/24
 */
public class ScratchLotteryManager {
    private static ScratchLotteryManager instance;

    public static ScratchLotteryManager getInstance() {
        if (instance == null) {
            synchronized (ScratchLotteryManager.class) {
                if (instance == null) {
                    instance = new ScratchLotteryManager();
                }
            }
        }
        return instance;
    }

    private ScratchLotteryManager() {
    }

    private ScratchLotteryParamsObject config;

    /**
     * 所有允许的宠物头像
     */
    private final List<Integer> allAllowPetId = new ArrayList<>();

    /**
     * 所有允许的头像,除去最高品质
     */
    private final List<Integer> allAllowPetIdBesideQuality_6 = new ArrayList<>();

    /**
     * 奖励个数权重
     *
     * @return
     */
    private final List<QualityWeight> rewardCountWeight = new ArrayList<>();
    private int rewardCountTotalOdds = 0;

    /**
     * 概率不变化品质
     */
    private final List<QualityWeight> qualityWeights = new ArrayList<>();

    private final List<OddsChangeQuality> oddsChangeQualities = new ArrayList<>();

    private final Map<Integer, LotteryQualityRewardPool> qualityRewards = new HashMap<>();

    public boolean init() {
        this.config = ScratchLotteryParams.getById(GameConst.CONFIG_ID);
        //初始个数权重
        for (int[] countOdds : config.getRewardcountodds()) {
            if (countOdds.length < 2) {
                LogUtil.error("ScratchLotteryParams cfg error, Reward count odds length < 2 ");
                return false;
            }
            this.rewardCountWeight.add(new QualityWeight(countOdds[0], countOdds[1]));
            this.rewardCountTotalOdds += countOdds[1];
        }

        //初始化概率
        for (int[] qualityOdd : this.config.getQualityodds()) {
            if (qualityOdd.length < 2) {
                LogUtil.warn("ScratchLotteryManager.init,lottery quality odds length is error");
                return false;
            }
            this.qualityWeights.add(new QualityWeight(qualityOdd[0], qualityOdd[1]));
        }

        //初始化变化品质
        for (int[] qualityOdd : this.config.getOddschangequality()) {
            if (qualityOdd.length < 4) {
                LogUtil.warn("ScratchLotteryManager.init,lottery odds change quality length is error");
                return false;
            }
            this.oddsChangeQualities.add(new OddsChangeQuality(qualityOdd[0], qualityOdd[1], qualityOdd[2], qualityOdd[3]));
        }

        //初始化奖池
        for (ScratchLotteryRewardObject rewardObject : ScratchLotteryReward._ix_id.values()) {
            if (0 == rewardObject.getOdds()) {
                LogUtil.warn("ScratchLotteryRewardObject odds is 0, skip add this, id =" + rewardObject.getId());
                continue;
            }

            this.qualityRewards.computeIfAbsent(rewardObject.getQuality(), e -> new LotteryQualityRewardPool()).addReward(rewardObject);

            this.allAllowPetId.add(rewardObject.getPetavatar());
            if (rewardObject.getQuality() != 6) {
                allAllowPetIdBesideQuality_6.add(rewardObject.getPetavatar());
            }
        }

        return !qualityRewards.isEmpty() && !qualityWeights.isEmpty()
                && !this.rewardCountWeight.isEmpty() && this.rewardCountTotalOdds > 0;
    }

    public boolean isOpen() {
       ServerActivity activity = ActivityManager.getInstance().getActivityByType(ActivityTypeEnum.ATE_ScratchLottery);
        if (activity == null) {
            return false;
        }
        return ActivityUtil.activityInOpen(activity);
    }

    /**
     * 刮出彩票
     *
     * @return
     */
    public Lottery randomLottery(String playerIdx) {
        if (StringUtils.isBlank(playerIdx)) {
            return null;
        }
        int rewardCount = randomRewardCount();
        List<ScratchLotteryRewardObject> reward = null;
        if (rewardCount > 0) {
            reward = randomReward(playerIdx, rewardCount);
        }
        return fillLottery(reward);
    }

    public int randomQuality(String playerIdx) {
        List<QualityWeight> qualityWeights = initPlayerQualityWeight(playerIdx);
        int quality = DrawCardUtil.randomQuality(qualityWeights);
        if (quality == -1) {
            LogUtil.error("ScratchLotteryManager.randomQuality, random quality failed, total info:"
                    + GameUtil.collectionToString(qualityWeights));
            return quality;
        }

        changePlayerOdds(playerIdx, quality);
        return quality;
    }

    /**
     * 修改玩家的概率,获得的品质
     *
     * @param playerIdx
     * @param quality   获得的品质
     */
    public void changePlayerOdds(String playerIdx, int quality) {
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(entity, p -> {
            for (OddsChangeQuality changeQuality : this.oddsChangeQualities) {
                int newOdds = 0;

                if (Objects.equals(quality, changeQuality.getQuality())) {
                    newOdds = changeQuality.getResetOdds();
                } else {
                    newOdds = Math.min(entity.getLotteryOdds(changeQuality.getQuality()) + changeQuality.getIncreaseOdds(), changeQuality.getMaxOdds());
                }
                entity.setLotteryOdds(changeQuality.getQuality(), newOdds);
            }
        });
    }

    public List<QualityWeight> initPlayerQualityWeight(String playerIdx) {
        List<QualityWeight> result = new ArrayList<>(this.qualityWeights);
        targetsystemEntity entity = targetsystemCache.getInstance().getTargetEntityByPlayerIdx(playerIdx);
        if (entity == null) {
            return result;
        }

        SyncExecuteFunction.executeConsumer(entity, e -> {
            for (OddsChangeQuality changeQuality : oddsChangeQualities) {
                int curOdds = entity.getLotteryOdds(changeQuality.getQuality());
                if (curOdds == 0) {
                    curOdds = changeQuality.getResetOdds();
                    entity.setLotteryOdds(changeQuality.getQuality(), curOdds);
                }
                result.add(new QualityWeight(changeQuality.getQuality(), curOdds));
            }
        });

        return result;
    }

    /**
     * 允许存在无奖励
     *
     * @param needCount
     * @return
     */
    private List<ScratchLotteryRewardObject> randomReward(String playerIdx, int needCount) {
        List<ScratchLotteryRewardObject> result = new ArrayList<>();
        if (needCount <= 0) {
            return result;
        }

        for (int i = 0; i < needCount * 2; i++) {
            int quality = randomQuality(playerIdx);
            LotteryQualityRewardPool rewardPool = this.qualityRewards.get(quality);
            if (rewardPool == null) {
                LogUtil.error("can not find quality rewards pool, quality:" + quality);
                continue;
            }

            ScratchLotteryRewardObject rewards = rewardPool.randomRewards();
            if (rewards != null) {
                result.add(rewards);
            }

            if (result.size() >= needCount) {
                break;
            }
        }

        return result;
    }


    /**
     * 填充彩票
     *
     * @return
     */
    private Lottery fillLottery(List<ScratchLotteryRewardObject> reward) {
        Set<Integer> usedPetId = new HashSet<>();
        Lottery lottery = new Lottery(this.config.getLength(), this.config.getWidth());
        if (!GameUtil.collectionIsEmpty(reward)) {
            //是否包含斜向
            boolean containSlant = false;
            if (reward.size() <= 1) {
                containSlant = true;
            }

            for (ScratchLotteryRewardObject rewardObject : reward) {
                usedPetId.add(rewardObject.getPetavatar());
                lottery.addContext(rewardObject.getPetavatar(), rewardObject.getLinkcount(), containSlant);
            }
        }

        //如果还有剩余的位置需要随机不重复填充宠物id
        int remainTypeCount = this.config.getNeedqualitycount() - (null == reward ? 0 : reward.size());
        if (remainTypeCount > 0) {
            Set<Integer> set = distinctRandomGetPetAvatar(remainTypeCount, usedPetId);
            List<Integer> newOrder = randomOrder(generateOrderList(set));
            for (Integer petId : newOrder) {
                lottery.addContextNotLink(petId);
            }
        }

        return lottery;
    }

    /**
     * 生成petId顺序
     */
    private List<Integer> generateOrderList(Set<Integer> petId) {
        if (CollectionUtils.isEmpty(petId)) {
            return null;
        }
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < getEachQualityNeedCount(); i++) {
            result.addAll(petId);
        }
        return result;
    }

    /**
     * 对指定列表顺序随机
     *
     * @param list
     * @return
     */
    private List<Integer> randomOrder(List<Integer> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        Random random = new Random();
        LinkedList<Integer> linkedList = new LinkedList<>(list);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Integer newInt = linkedList.get(random.nextInt(linkedList.size()));
            result.add(newInt);
            linkedList.remove(newInt);
        }
        return result;
    }

    /**
     * 从所有允许的宠物头像中不重复获取宠物头像
     *
     * @param needCount
     * @param usedAvatar
     * @return
     */
    private Set<Integer> distinctRandomGetPetAvatar(int needCount, Set<Integer> usedAvatar) {
        Set<Integer> result = new HashSet<>();
        if (needCount <= 0) {
            return result;
        }

        //TODO 先替换成不随机到红色宠物
        LinkedList<Integer> allAllow = new LinkedList<>(this.allAllowPetIdBesideQuality_6);
        allAllow.removeAll(usedAvatar);
        Random random = new Random();
        for (int i = 0; i < needCount; i++) {
            int index = random.nextInt(allAllow.size());
            result.add(allAllow.get(random.nextInt(allAllow.size())));
            allAllow.remove(index);
        }
        return result;
    }

    private int getEachQualityNeedCount() {
        return (this.config.getLength() * this.config.getWidth()) / this.config.getNeedqualitycount();
    }

    public int randomRewardCount() {
        Random random = new Random();
        int randomNum = random.nextInt(this.rewardCountTotalOdds);
        int curNum = 0;
        for (QualityWeight qualityWeight : this.rewardCountWeight) {
            if ((curNum += qualityWeight.getWeight()) > randomNum) {
                return qualityWeight.getQuality();
            }
        }
        return this.rewardCountWeight.get(random.nextInt(this.rewardCountWeight.size())).getQuality();
    }

    private int randomQuality(List<QualityWeight> qualityWeights, int totalOdds) {
        if (GameUtil.collectionIsEmpty(qualityWeights)) {
            return -1;
        }

        if (qualityWeights.size() == 1) {
            return qualityWeights.get(0).getQuality();
        }

        Random random = new Random();
        if (totalOdds <= 0) {
            return qualityWeights.get(random.nextInt(qualityWeights.size())).getQuality();
        }

        int randomNum = random.nextInt(totalOdds);
        int curNum = 0;
        for (QualityWeight qualityWeight : qualityWeights) {
            if ((curNum += qualityWeight.getWeight()) > randomNum) {
                return qualityWeight.getQuality();
            }
        }
        return qualityWeights.get(random.nextInt(qualityWeights.size())).getQuality();
    }


    /**
     * =======================结算彩票奖励==============================
     */

    public List<Reward> settleLottery(Lottery lottery) {
        if (lottery == null) {
            LogUtil.info("model.activity.ScratchLotteryManager.settleLottery, lottery is not fill full");
            return null;
        }

        Map<Integer, Integer> idCountMap = lottery.settleLottery();
        if (idCountMap == null) {
            return null;
        }

        List<Reward> result = new ArrayList<>();
        for (Entry<Integer, Integer> entry : idCountMap.entrySet()) {
            //todo [宠物品质修改]
            if (PetBaseProperties.getStartRarityById(entry.getKey()) == 6 && entry.getValue() >= 3) {
                LogUtil.debug("ScratchLotteryManager.settleLottery, get a quality 6 reward, link pet id:" + entry.getKey());
            }

            List<Reward> rewards = ScratchLotteryReward.getRewardsByPetIdAndCount(entry.getKey(), entry.getValue());
            if (rewards != null) {
                result.addAll(rewards);
            }
        }

        //如果奖励为空则发放保底奖励
        if (result.isEmpty()) {
            List<Reward> baseReward = RewardUtil.getRewardsByRewardId(this.config.getBasereward());
            if (!GameUtil.collectionIsEmpty(baseReward)) {
                result.addAll(baseReward);
            }
        }

        return result;
    }

    /**
     * =======================结算彩票奖励==============================
     */
}

@Getter
@Setter
class LotteryQualityRewardPool {
    private int totalOdds;
    private List<ScratchLotteryRewardObject> rewards;

    public void addReward(ScratchLotteryRewardObject obj) {
        if (obj == null) {
            return;
        }

        if (this.rewards == null) {
            this.rewards = new ArrayList<>();
        }
        this.rewards.add(obj);
    }

    public ScratchLotteryRewardObject randomRewards() {
        if (CollectionUtils.isEmpty(rewards)) {
            return null;
        }

        Random random = new Random();
        if (totalOdds <= 0) {
            return rewards.get(random.nextInt(rewards.size()));
        } else {
            int randomNum = random.nextInt(totalOdds);
            int curNum = 0;
            ScratchLotteryRewardObject newRandom = null;
            for (ScratchLotteryRewardObject rewardObject : rewards) {
                if ((curNum += rewardObject.getOdds()) > randomNum) {
                    newRandom = rewardObject;
                    if (PetBaseProperties.getQualityByPetId(rewardObject.getPetavatar()) == 6) {
                        LogUtil.debug("ScratchLotteryManager.randomReward, random num:" + randomNum + ",total odds:" + totalOdds
                                + ",result id:" + rewardObject.getId() + "link pet id:" + rewardObject.getPetavatar());
                        break;
                    }
                }
            }
            return newRandom;
        }
    }
}

