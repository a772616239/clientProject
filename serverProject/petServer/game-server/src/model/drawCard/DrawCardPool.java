package model.drawCard;

import cfg.DrawCommonCardConfig;
import cfg.DrawCommonCardConfigObject;
import cfg.DrawFriendShipCardConfig;
import cfg.DrawFriendShipCardConfigObject;
import cfg.DrawHighCardConfig;
import cfg.DrawHighCardConfigObject;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import protocol.DrawCard.EnumDrawCardType;
import util.GameUtil;
import util.LogUtil;

/**
 * @author huhan
 * @date 2020/03/18
 */
@Getter
@Setter
public class DrawCardPool {
    private EnumDrawCardType type;
    /**
     * <品质,（品质内奖励总权重, 该品质包含的奖励)>
     **/
    private Map<Integer, PoolQualityOddsReward> pool = new HashMap<>();
    private List<QualityWeight> qualityWeightList = new ArrayList<>();

    /**
     * 概率变化的品质
     */
    private List<OddsChangeQuality> oddsChangeQualityList = new ArrayList<>();
    /**
     * 奖次必得的奖励配置
     */
    private int[] mustGainQuality;

    /**
     * 随机种族概率
     */
    private List<QualityWeight> petTypeWeight = new ArrayList<>();

    /**
    * 核心魔灵池子
    *
    */
    private Map<Integer, OddsRandom> corePetMap = new HashMap<>();

    public DrawCardPool() {
    }

    /**
     * @param type
     * @param qualityWeight
     * @param oddsChangeQuality
     * @param mustGainQuality
     * @param petTypeWeight     目前只有友情抽卡和普通抽卡支持自选宠物
     * @return
     */
    public static DrawCardPool createDrawCardPoolEntity(EnumDrawCardType type, int[][] qualityWeight,
                                                        int[][] oddsChangeQuality, int[] mustGainQuality, int[][] petTypeWeight) {
        if (type == null || qualityWeight == null || oddsChangeQuality == null || petTypeWeight == null) {
            LogUtil.error("DrawCardPool.createDrawCardPoolEntity, error params, type=" + type + ", qualityWeight="
                    + Arrays.toString(qualityWeight) + ", oddsChangeQuality = " + Arrays.toString(oddsChangeQuality));
            return null;
        }

        DrawCardPool drawCardPool = getEntity(type);

        for (int[] ints : qualityWeight) {
            if (ints.length < 2) {
                LogUtil.error("DrawCardPool.createDrawCardPoolEntity,quality weight is cfg error：" + Arrays.toString(ints));
                return null;
            }

            drawCardPool.addQualityWeight(new QualityWeight(ints[0], ints[1]));
        }

        for (int[] ints : oddsChangeQuality) {
            if (ints.length < 4) {
                LogUtil.error("DrawCardPool.createDrawCardPoolEntity,oddsChangeQuality is cfg error：" + Arrays.toString(ints));
                return null;
            }
            drawCardPool.addOddsChangeQuality(new OddsChangeQuality(ints[0], ints[1], ints[2], ints[3]));
        }
        drawCardPool.setMustGainQuality(mustGainQuality);

        for (int[] ints : petTypeWeight) {
            if (ints.length < 2) {
                LogUtil.error("DrawCardPool.createDrawCardPoolEntity,petTypeWeight is cfg error:");
                return null;
            }

            drawCardPool.addPetTypeWeight(new QualityWeight(ints[0], ints[1]));
        }
        return drawCardPool;
    }

    private static DrawCardPool getEntity(EnumDrawCardType type) {
        DrawCardPool result = null;
        if (type == EnumDrawCardType.EDCT_COMMON) {
            result = new CommonDrawCardPool();
        } else if (type == EnumDrawCardType.EDCT_HIGH) {
            result = new HighDrawCardPool();
        } else {
            result = new DrawCardPool();
        }

        result.setType(type);

        return result;
    }

    /**
     * 添加奖励随机品质
     *
     * @param oddsChangeQuality
     */
    private void addOddsChangeQuality(OddsChangeQuality oddsChangeQuality) {
        if (oddsChangeQuality == null) {
            return;
        }

        this.oddsChangeQualityList.add(oddsChangeQuality);
    }

    private void addQualityWeight(QualityWeight qualityWeight) {
        if (qualityWeight == null) {
            return;
        }
        this.qualityWeightList.add(qualityWeight);
    }

    private void addPetTypeWeight(QualityWeight qualityWeight) {
        if (qualityWeight == null) {
            return;
        }
        this.petTypeWeight.add(qualityWeight);
    }

    /**
     * 添加奖励列表
     *
     * @param oddsRandom
     */
    public void addOddsRandom(OddsRandom oddsRandom) {
        if (oddsRandom == null) {
            return;
        }

        if (oddsRandom.getOdds() <= 0) {
            LogUtil.warn("model.drawCard.DrawCardPool.addOddsRandom, odds <= 0, skip add, cfgId = " + oddsRandom.getId());
            return;
        }

        pool.computeIfAbsent(oddsRandom.getQuality(), t -> new PoolQualityOddsReward()).addOddsRandom(oddsRandom);

        if (oddsRandom.getIscorepet()) {
            corePetMap.put(oddsRandom.getId(), oddsRandom);
        }
    }

    public OddsRandom randomWithRandomQuality(String playerIdx) {
        return randomByQuality(playerIdx, randomQuality(playerIdx), false);
    }

    public OddsRandom randomByQuality(String playerIdx, int quality, boolean mustGetFlag) {
        if (!pool.containsKey(quality) || StringUtils.isBlank(playerIdx)) {
            LogUtil.error("model.drawCard.DrawCardPool.randomByQuality, quality is not exist, quality = " + quality);
            return null;
        }
        PoolQualityOddsReward qualityOddsReward = getUnlockPoolByPlayerId(playerIdx, quality);
        if (qualityOddsReward == null || qualityOddsReward.isEmpty()) {
            LogUtil.error("quality PoolQualityOddsReward is null");
            return null;
        }
        return playerChoiceRandom(playerIdx, quality, qualityOddsReward.getOddsRandomList(), qualityOddsReward.getTotalOdds());
    }

    private int randomPetType() {
        if (GameUtil.collectionIsEmpty(petTypeWeight)) {
            return -1;
        }
        int totalOdds = DrawCardUtil.calculateTotalOdds(petTypeWeight);
        int randomNum = new Random().nextInt(totalOdds);
        int curNum = 0;
        for (QualityWeight qualityWeight : petTypeWeight) {
            if ((curNum += qualityWeight.getWeight()) >= randomNum) {
//                LogUtil.debug("DrawCardUtil.randomQuality, random petType:" + qualityWeight.getQuality() + ", curNum:"
//                        + randomNum + ", petType weight:" + GameUtil.collectionToString(petTypeWeight));
                return qualityWeight.getQuality();
            }
        }

        return -1;
    }

    /**
     * 获取玩家自选某个种族内指定品质的宠物
     *
     * @param playerIdx
     * @param quality
     * @return
     */
    private Set<Integer> getPlayerChoicePet(String playerIdx, int quality) {
        return DrawCardUtil.getPlayerChoicePet(playerIdx, quality, randomPetType());
    }

    /**
     * 根据指定的列表和总概率随机
     *
     * @param oddsRandoms
     * @param totalOdds
     * @return
     */
    protected OddsRandom random(List<? extends OddsRandom> oddsRandoms, int totalOdds) {
        if (GameUtil.collectionIsEmpty(oddsRandoms)) {
            return null;
        }
        Random random = new Random();
        if (totalOdds <= 0) {
            return oddsRandoms.get(random.nextInt(oddsRandoms.size()));
        } else {
            int odds = random.nextInt(totalOdds);
            int sumOdds = 0;
            for (OddsRandom value : oddsRandoms) {
                if (odds < (sumOdds += value.getOdds())) {
//                    LogUtil.debug("model.drawCard.DrawCardPool.random, curNum:" + odds + ", totalOdds:" + totalOdds + ",detail:" + GameUtil.collectionToString(oddsRandoms));
                    return value;
                }
            }
        }

        LogUtil.warn("DrawCardPool.random, random failed, oddsRandom:"
                + GameUtil.collectionToString(oddsRandoms) + ", totalOdds:" + totalOdds);
        return null;
    }

    /**
     * 随机品质，
     *
     * @param playerIdx
     * @return -1随机错误
     */
    public int randomQuality(String playerIdx) {
        List<QualityWeight> qualityWeights = initPlayerQuality(playerIdx);
        int quality = DrawCardUtil.randomQuality(qualityWeights);
        if (quality == -1) {
            LogUtil.error("DrawCardPool.randomQuality, random failed, qualityWeights="
                    + GameUtil.collectionToString(qualityWeights));
            return quality;
        }

        changePlayerOdds(playerIdx, quality);
        return quality;
    }

    /**
     * 初始化玩家概率
     *
     * @param playerIdx
     * @return
     */
    public List<QualityWeight> initPlayerQuality(String playerIdx) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return getQualityWeightList();
        }

        List<QualityWeight> result = new ArrayList<>(getQualityWeightList());
        SyncExecuteFunction.executeConsumer(player, p -> {
            //初始化概率
            for (OddsChangeQuality drawCardOddsChangeQuality : getOddsChangeQualityList()) {
                int curOdds = player.getDrawCardCurOdds(getType(), drawCardOddsChangeQuality.getQuality());
                if (curOdds <= 0) {
                    curOdds = drawCardOddsChangeQuality.getResetOdds();
                    player.setDrawCardOdds(getType(), drawCardOddsChangeQuality.getQuality(), curOdds);
                }
                result.add(new QualityWeight(drawCardOddsChangeQuality.getQuality(), curOdds));
            }
        });

        return result;
    }

    /**
     * 修改玩家的概率,获得的品质
     *
     * @param playerIdx
     * @param quality   获得的品质
     */
    public void changePlayerOdds(String playerIdx, int quality) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {
            for (OddsChangeQuality changeQuality : getOddsChangeQualityList()) {
                int newOdds = 0;

                if (Objects.equals(quality, changeQuality.getQuality())) {
                    newOdds = changeQuality.getResetOdds();
                } else {
                    newOdds = Math.min(player.getDrawCardCurOdds(getType(), changeQuality.getQuality()) + changeQuality.getIncreaseOdds(), changeQuality.getMaxOdds());
                }
                player.setDrawCardOdds(getType(), changeQuality.getQuality(), newOdds);
            }
        });
    }

    /**
     * 重置指定玩家的指定品质概率
     *
     * @param playerIdx
     * @param quality
     */
    public void resetPlayerOdds(String playerIdx, int quality) {
        playerEntity player = playerCache.getByIdx(playerIdx);
        OddsChangeQuality changeQualityCfg = getChangeQualityOddsByQuality(quality);
        if (player == null || changeQualityCfg == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(player, p -> {
            player.setDrawCardOdds(getType(), quality, changeQualityCfg.getResetOdds());
        });
    }

    public OddsChangeQuality getChangeQualityOddsByQuality(int quality) {
        for (OddsChangeQuality changeQuality : oddsChangeQualityList) {
            if (Objects.equals(quality, changeQuality.getQuality())) {
                return changeQuality;
            }
        }
        return null;
    }

    public List<OddsRandom> drawCard(String playerIdx, int times, boolean mustDrawCorePet) {
        List<OddsRandom> result = new ArrayList<>();
        boolean tmpDrawCorePet = mustDrawCorePet;
        for (int i = 0; i < times * 2; i++) {
            OddsRandom oddsRandom;
            if (tmpDrawCorePet) {
                oddsRandom = gainRandomCorePet();
            } else {
                oddsRandom = randomWithRandomQuality(playerIdx);
            }
            if (oddsRandom != null) {
                result.add(oddsRandom);
                if (tmpDrawCorePet) {
                    tmpDrawCorePet = false;
                }
            }

            if (result.size() >= times) {
                break;
            }
        }

        LogUtil.info("playerIdx:" + playerIdx + ", random times: " + times + ", quality 5 count:"
                + DrawCardUtil.calculateQualityCount(result, 5));
        return result;
    }

    /**
     * 无重复随机
     *
     * @param quality
     * @param alreadyGet 已经获得的奖励
     * @return
     */
    public OddsRandom uniqueRandomByQuality(String playerIdx, int quality, List<OddsRandom> alreadyGet) {
        if (!pool.containsKey(quality)) {
            LogUtil.error("model.drawCard.DrawCardPool.randomByQuality, quality is not exist, quality = " + quality);
            return null;
        }

        PoolQualityOddsReward qualityOddsReward = getUnlockPoolByPlayerId(playerIdx, quality);
        if (qualityOddsReward == null || qualityOddsReward.isEmpty()) {
            LogUtil.error("quality PoolQualityOddsReward is null");
            return null;
        }

        //剔除
        if (!GameUtil.collectionIsEmpty(alreadyGet)) {
            List<OddsRandom> newRandomList = new ArrayList<>();
            int newTotalOdds = 0;

            for (OddsRandom oddsRandom : qualityOddsReward.getOddsRandomList()) {
                boolean contain = false;
                for (OddsRandom random : alreadyGet) {
                    if (random.getId() == oddsRandom.getId()) {
                        contain = true;
                        break;
                    }
                }

                if (contain) {
                    continue;
                }

                newRandomList.add(oddsRandom);
                newTotalOdds += oddsRandom.getOdds();
            }
            return playerChoiceRandom(playerIdx, quality, newRandomList, newTotalOdds);
        } else {
            return playerChoiceRandom(playerIdx, quality, qualityOddsReward.getOddsRandomList(), qualityOddsReward.getTotalOdds());
        }
    }

    public PoolQualityOddsReward getUnlockPoolByPlayerId(String playerIdx, int quality) {
        PoolQualityOddsReward qualityOddsReward = pool.get(quality);
        if (qualityOddsReward == null || qualityOddsReward.isEmpty()) {
            LogUtil.error("quality PoolQualityOddsReward is null");
            return null;
        }
        // 高级抽卡移除未解锁的
        if (getType() == EnumDrawCardType.EDCT_COMMON || getType() == EnumDrawCardType.EDCT_HIGH) {
            PoolQualityOddsReward qualityOddsReward1 = new PoolQualityOddsReward();
            List<Integer> commList = DrawCardManager.getInstance().getUnlockReward(playerIdx, getType());
            List<Integer> atLimit = new ArrayList<>();
            if (getType() == EnumDrawCardType.EDCT_COMMON) {
                atLimit.addAll(DrawCardManager.getInstance().getTujianCfg().getPetspKeyCommon().keySet());
            } else {
                atLimit.addAll(DrawCardManager.getInstance().getTujianCfg().getPetspKeyhigh().keySet());
            }
            for (OddsRandom or : qualityOddsReward.getOddsRandomList()) {
                if (atLimit.contains(or.getRewards()[1]) && !commList.contains(or.getRewards()[1])) {
                    continue;
                }
                qualityOddsReward1.addOddsRandom(or);
            }
            if (!qualityOddsReward1.isEmpty()) {
                qualityOddsReward = qualityOddsReward1;
            }
        }
        return qualityOddsReward;
    }

    /**
     * 无重复随机奖励
     *
     * @param times
     * @return
     */
    public List<OddsRandom> uniqueDrawCard(String playerIdx, int times, boolean mustDrawCorePet) {
        List<OddsRandom> result = new ArrayList<>();
        boolean tmpDrawCorePet = mustDrawCorePet;
        for (int i = 0; i < times * 2; i++) {
            OddsRandom oddsRandom;
            if (tmpDrawCorePet) {
                oddsRandom = gainRandomCorePet();
            } else {
                oddsRandom = uniqueRandomByQuality(playerIdx, randomQuality(playerIdx), result);
            }
            if (oddsRandom != null) {
                result.add(oddsRandom);
                if (tmpDrawCorePet) {
                    tmpDrawCorePet = false;
                }
            }

            if (result.size() >= times) {
                break;
            }
        }

        LogUtil.info("playerIdx:" + playerIdx + ", random times: " + times + ", quality:" + DrawCardManager.HIGHEST_QUALITY
                + DrawCardUtil.calculateQualityCount(result, DrawCardManager.HIGHEST_QUALITY));
        return result;
    }

    private OddsRandom gainRandomCorePet() {
        if (corePetMap.isEmpty()) {
            return null;
        }
        int rand = RandomUtils.nextInt(corePetMap.size());
        List<OddsRandom> corePetList = corePetMap.values().stream().collect(Collectors.toList());
        return corePetList != null ? corePetList.get(rand) : null;
    }

    /**
     * 玩家自选随机
     *
     * @param randomList
     * @param totalOdds
     * @return
     */
    private OddsRandom playerChoiceRandom(String playerIdx, int quality, List<OddsRandom> randomList, int totalOdds) {
        Set<Integer> playerChoicePet = getPlayerChoicePet(playerIdx, quality);
        if (CollectionUtils.isEmpty(playerChoicePet)) {
            return random(randomList, totalOdds);
        } else {
            List<OddsRandom> playerChoice = new ArrayList<>();

            List<OddsRandom> unChoice = new ArrayList<>();
            int unChoiceTotalOdds = 0;
            for (OddsRandom oddsRandom : randomList) {
                if (playerChoicePet.contains(DrawCardUtil.getRewardsLinkPetId(oddsRandom.getRewards()))) {
                    TempOddsRandom tempOddsRandom = TempOddsRandom.create(getType(), oddsRandom.getId());
                    if (tempOddsRandom != null) {
                        playerChoice.add(tempOddsRandom);
                    }
                } else {
                    unChoice.add(oddsRandom);
                    unChoiceTotalOdds += oddsRandom.getOdds();
                }
            }

            OddsRandom choiceRandom = random(playerChoice, totalOdds);
            LogUtil.debug("DrawCardPool.playerChoiceRandom, player choice random, result:" + (choiceRandom == null ? "" : choiceRandom.toString()));
            if (choiceRandom != null) {
                return choiceRandom;
            }

            return random(unChoice, unChoiceTotalOdds);
        }
    }
}

@Getter
@Setter
@AllArgsConstructor
@ToString
class TempOddsRandom implements OddsRandom {
    private int id;
    private int quality;
    private int odds;
    private int[] rewards;
    private boolean isCorePet;

    public static TempOddsRandom create(EnumDrawCardType drawCardType, int cfgId) {
        if (drawCardType == EnumDrawCardType.EDCT_FRIEND) {
            DrawFriendShipCardConfigObject cfg = DrawFriendShipCardConfig.getById(cfgId);
            if (cfg == null) {
                return null;
            }
            return new TempOddsRandom(cfg.getId(), cfg.getQuality(), cfg.getSelectedodds(), cfg.getRewards(), false);
        } else if (drawCardType == EnumDrawCardType.EDCT_COMMON) {
            DrawCommonCardConfigObject cfg = DrawCommonCardConfig.getById(cfgId);
            if (cfg == null) {
                return null;
            }
            return new TempOddsRandom(cfg.getId(), cfg.getQuality(), cfg.getSelectedodds(), cfg.getRewards(), cfg.getIscorepet());
        } else if (drawCardType == EnumDrawCardType.EDCT_HIGH) {
            DrawHighCardConfigObject cfg = DrawHighCardConfig.getById(cfgId);
            if (cfg == null) {
                return null;
            }
            return new TempOddsRandom(cfg.getId(), cfg.getQuality(), cfg.getSelectedodds(), cfg.getRewards(), cfg.getIscorepet());
        }
        return null;
    }

    @Override
    public boolean getIscorepet() {
        return isCorePet;
    }
}