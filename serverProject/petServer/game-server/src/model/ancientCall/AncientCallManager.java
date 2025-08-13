package model.ancientCall;

import cfg.AltarConfig;
import cfg.AltarConfigObject;
import cfg.AncientCall;
import cfg.AncientCallObject;
import common.SyncExecuteFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import model.drawCard.DrawCardManager;
import model.drawCard.DrawCardUtil;
import model.drawCard.OddsRandom;
import model.drawCard.PoolQualityOddsReward;
import model.player.dbCache.playerCache;
import model.player.entity.playerEntity;
import model.reward.RewardUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import protocol.Common.Reward;
import protocol.DrawCard;
import util.ArrayUtil;
import util.GameUtil;
import util.LogUtil;

/**
 * 远古召唤
 */
public class AncientCallManager {
    private static AncientCallManager instance = new AncientCallManager();

    public static AncientCallManager getInstance() {
        if (instance == null) {
            synchronized (AncientCallManager.class) {
                if (instance == null) {
                    instance = new AncientCallManager();
                }
            }
        }
        return instance;
    }

    private AncientCallManager() {
    }

    /**
     * <<祭坛类型,<奖励品质, 配置>
     */
    private final Map<Integer, Map<Integer, List<AncientCallObject>>> typeQualityMap = new HashMap<>();
    /**
     * <<祭坛类型,<奖励品质, 总概率>,
     */
    private final Map<Integer, Map<Integer, Integer>> typeQualityOddsMap = new HashMap<>();

    /**
     * 祭坛类型总品质概率和
     */
    private final Map<Integer, Integer> altarQualityTotalOddsMap = new ConcurrentHashMap<>();

    public boolean init() {
        Map<Integer, AncientCallObject> ancientCallMap = AncientCall._ix_id;
        if (ancientCallMap == null || ancientCallMap.isEmpty()) {
            LogUtil.error("AncientCall cfg is null");
            return false;
        }

        for (AncientCallObject value : ancientCallMap.values()) {
            if (value.getId() <= 0) {
                continue;
            }
            Map<Integer, List<AncientCallObject>> qualityMap = typeQualityMap.computeIfAbsent(value.getType(), k -> new HashMap<>());
            List<AncientCallObject> ancientCallObjects = qualityMap.computeIfAbsent(value.getQuality(), k -> new ArrayList<>());
            ancientCallObjects.add(value);

            Map<Integer, Integer> qualityOddsMap = typeQualityOddsMap.computeIfAbsent(value.getType(), t -> new HashMap<>());
            if (qualityOddsMap.containsKey(value.getQuality())) {
                qualityOddsMap.put(value.getQuality(), qualityOddsMap.get(value.getQuality()) + value.getRate());
            } else {
                qualityOddsMap.put(value.getQuality(), value.getRate());
            }

            if (AltarConfig.getById(value.getType()) == null) {
                LogUtil.error("AncientCall has type not exist in AltarConfig, type = " + value.getType());
                return false;
            }
        }

        Map<Integer, AltarConfigObject> altarMap = AltarConfig._ix_id;
        for (AltarConfigObject value : altarMap.values()) {
            if (value == null) {
                return false;
            }
            int[][] qualityWeight = value.getQualityweight();
            if (qualityWeight.length <= 0) {
                LogUtil.error("altar qualityWeight is null, id = " + value.getId());

                return false;
            }

            for (int[] ints : qualityWeight) {
                if (ints.length < 2) {
                    return false;
                }
            }
        }

        return true;
    }

    public List<Reward> callAncient(String playerIdx, int type, int count) {
        List<AncientCallObject> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AncientCallObject callObjects = callPlayerChoiceAncient(playerIdx, randomQuality(playerIdx, type), type);
            if (callObjects != null) {
                result.add(callObjects);
            }
        }
        return parseToReward(result);
    }

    private List<AncientCallObject> getAncientCallObjects(int type, int quality) {
        Map<Integer, List<AncientCallObject>> integerListMap = typeQualityMap.get(type);
        if (integerListMap == null) {
            return null;
        }
        return integerListMap.get(quality);
    }

    private List<AncientCallObject> getAncientCallObjectsUnlock(String playerIdx, int type, int quality) {
        Map<Integer, List<AncientCallObject>> integerListMap = typeQualityMap.get(type);
        if (integerListMap == null || null == integerListMap.get(quality)) {
            return null;
        }
        List<AncientCallObject> temp = new ArrayList<>();
        List<Integer> commList = DrawCardManager.getInstance().getUnlockReward(playerIdx, DrawCard.EnumDrawCardType.EDCT_NULL);
        List<Integer> atLimit = new ArrayList<>();
        atLimit.addAll(DrawCardManager.getInstance().getTujianCfg().getPetspKeyAncient().keySet());
        for (AncientCallObject aco : integerListMap.get(quality)) {
            if (atLimit.contains(aco.getContant()[1]) && !commList.contains(aco.getContant()[1])) {
                continue;
            }
            temp.add(aco);
        }
        return temp;
    }

    /**
     * 随机品质
     *
     * @return
     */
    private int randomQuality(String playerIdx, int altarType) {
        AltarConfigObject cfg = AltarConfig.getById(altarType);
        if (cfg == null || StringUtils.isBlank(playerIdx)) {
            return -1;
        }

        if (canGetMustQuality(playerIdx, cfg)) {
            LogUtil.debug("model.ancientCall.AncientCallManager.randomQuality, playerIdx:" + playerIdx + ", reach must get times");
            clearPlayerDrawTimes(playerIdx, altarType);
            return cfg.getMustgetquality();
        }

        int[][] qualityWeight = cfg.getQualityweight();
        if (qualityWeight == null || qualityWeight.length <= 0) {
            LogUtil.error("altar type quality cfg is null, type = " + altarType);
            return -1;
        }

        int totalOdds = altarQualityTotalOddsMap.computeIfAbsent(altarType, k -> calculateQualityOdds(qualityWeight));

        int result = -1;
        int curNum = 0;
        int curOdds = new Random().nextInt(totalOdds);
        for (int[] ints : qualityWeight) {
            if ((curNum += ints[1]) >= curOdds) {
                result = ints[0];
                break;
            }
        }

        LogUtil.debug("model.ancientCall.AncientCallManager.randomQuality, playerIdx:" + playerIdx
                + ", random odds:" + curOdds + ", odds detail:" + ArrayUtil.toString(qualityWeight));
        addPlayerDrawTimes(playerIdx, altarType);
        return result;
    }

    /**
     * 是否可以获得保底
     *
     * @param playerIdx
     * @param altarCfg
     * @return
     */
    private boolean canGetMustQuality(String playerIdx, AltarConfigObject altarCfg) {
        if (StringUtils.isBlank(playerIdx) || altarCfg == null) {
            return false;
        }

        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return false;
        }

        return SyncExecuteFunction.executePredicate(player, e -> {
            Integer nowTimes = player.getDb_data().getAncientAltar().getMustGetDrawTimesMap().get(altarCfg.getId());
            //第xx抽必出
            return nowTimes != null && nowTimes >= (altarCfg.getMustgetdrawtimes() - 1);
        });
    }

    /**
     * 增加玩家抽卡次数
     *
     * @param playerIdx
     */
    private void addPlayerDrawTimes(String playerIdx, int type) {
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(player, e -> {
            Integer oldTimes = player.getDb_data().getAncientAltar().getMustGetDrawTimesMap().get(type);
            int newTimes = oldTimes == null ? 1 : oldTimes + 1;
            player.getDb_data().getAncientAltarBuilder().putMustGetDrawTimes(type, newTimes);
        });
    }

    /**
     * 清空抽奖次数
     *
     * @param playerIdx
     * @param type
     */
    private void clearPlayerDrawTimes(String playerIdx, int type) {
        if (StringUtils.isBlank(playerIdx)) {
            return;
        }
        playerEntity player = playerCache.getByIdx(playerIdx);
        if (player == null) {
            return;
        }

        SyncExecuteFunction.executeConsumer(player, e ->
                player.getDb_data().getAncientAltarBuilder().putMustGetDrawTimes(type, 0)
        );
    }

    private int calculateQualityOdds(int[][] qualityWeight) {
        if (qualityWeight == null) {
            return 0;
        }
        int totalOdds = 0;
        for (int[] ints : qualityWeight) {
            totalOdds += ints[1];
        }
        return totalOdds;
    }

//    private List<AncientCallObject> callAncients(List<AncientCallObject> cfgList, int totalRate, int count) {
//        if (cfgList == null || cfgList.isEmpty()) {
//            LogUtil.error("AncientCallManager.callAncient, param error");
//            return null;
//        }
//
//        List<AncientCallObject> callResult = new ArrayList<>();
//        for (int i = 0; i < count; i++) {
//            AncientCallObject object = callAncient(cfgList, totalRate);
//            if (object != null) {
//                callResult.add(object);
//            }
//        }
//        return callResult;
//    }

    private AncientCallObject callAncient(List<AncientCallObject> cfgList, int totalRate) {
        if (cfgList == null || cfgList.isEmpty()) {
            LogUtil.error("AncientCallManager.callAncient, param error");
            return null;
        }

        Random random = new Random();
        if (totalRate <= 0) {
            return cfgList.get(random.nextInt(cfgList.size()));
        } else {
            int targetRate = random.nextInt(totalRate);
            int curRate = 0;

            for (AncientCallObject obj : cfgList) {
                if (obj.getRate() > 0 && targetRate <= (curRate += obj.getRate())) {
                    return obj;
                }
            }
        }

        LogUtil.error("AncientCallManager.callAncient, random reward failed, list:"
                + GameUtil.collectionToString(cfgList) + ", totalRate:" + totalRate);
        return null;
    }

    private List<Reward> parseToReward(List<AncientCallObject> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<Reward> result = new ArrayList<>();
        for (AncientCallObject object : list) {
            Reward reward = RewardUtil.parseReward(object.getContant());
            if (reward != null) {
                result.add(reward);
            }
        }
        return result;
    }

    /**
     * 根据类型和品质获取总权重
     */
    private int getTotalRateByTypeAndQuality(int petType, int quality) {
        Map<Integer, Integer> qualityOddsMap = this.typeQualityOddsMap.get(petType);
        if (qualityOddsMap == null) {
            return 0;
        }
        Integer result = qualityOddsMap.get(quality);
        return result == null ? 0 : result;
    }

    /**
     * 玩家自选随机
     *
     * @param playerIdx
     * @param quality
     * @param petType
     * @return
     */
    private AncientCallObject callPlayerChoiceAncient(String playerIdx, int quality, int petType) {
        List<AncientCallObject> callObjectList = getAncientCallObjectsUnlock(playerIdx, petType, quality);
        if (callObjectList == null) {
            return null;
        }
        int totalRate = 0;
        for (AncientCallObject aco : callObjectList) {
            totalRate += aco.getRate();
        }

        Set<Integer> playerChoicePet = DrawCardUtil.getPlayerChoicePet(playerIdx, quality, petType);

        if (CollectionUtils.isEmpty(playerChoicePet)) {
            return callAncient(callObjectList, totalRate);
        } else {
            List<AncientCallObject> choice = new ArrayList<>();

            List<AncientCallObject> unChoice = new ArrayList<>();
            int unChoiceTotalRate = 0;

            for (AncientCallObject callObject : callObjectList) {
                if (playerChoicePet.contains(DrawCardUtil.getRewardsLinkPetId(callObject.getContant()))) {
                    choice.add(callObject);
                } else {
                    unChoice.add(callObject);
                    unChoiceTotalRate += callObject.getRate();
                }
            }

            AncientCallObject object = callSelectedAncient(choice, totalRate);
            LogUtil.debug("model.ancientCall.AncientCallManager.callPlayerChoiceAncient, player choice random result:" + (object == null ? "" : object.toString()));
            if (object != null) {
                return object;
            }

            return callAncient(unChoice, unChoiceTotalRate);
        }
    }

    /**
     * 随机玩家选中
     *
     * @param cfgList
     * @param totalRate
     * @return
     */
    private AncientCallObject callSelectedAncient(List<AncientCallObject> cfgList, int totalRate) {
        if (cfgList == null || cfgList.isEmpty()) {
            LogUtil.error("AncientCallManager.callAncient, param error");
            return null;
        }

        Random random = new Random();
        if (totalRate <= 0) {
            return cfgList.get(random.nextInt());
        } else {
            int targetRate = random.nextInt(totalRate);
            int curRate = 0;

            for (AncientCallObject obj : cfgList) {
                if (targetRate < (curRate += obj.getSelectedodds())) {
//                    LogUtil.debug("model.ancientCall.AncientCallManager.callSelectedAncient, curNum:" + targetRate
//                            + ", totalOdds:" + totalRate + ",detail:" + GameUtil.collectionToString(cfgList));
                    return obj;
                }
            }
        }

        LogUtil.info("AncientCallManager.callSelectedAncient, random reward failed, list:"
                + GameUtil.collectionToString(cfgList) + ", totalRate:" + totalRate);
        return null;
    }

}


