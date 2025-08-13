package util;

import cfg.GameConfig;
import cfg.MonsterDifficulty;
import cfg.MonsterDifficultyObject;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetFragmentConfig;
import cfg.PetFragmentConfigObject;
import cfg.PetMissionDescription;
import cfg.PetMissionLevel;
import cfg.PetMissionLevelObject;
import cfg.PetMissionObject;
import cfg.PetRuneExp;
import cfg.PetRuneExpObject;
import cfg.PetRuneProperties;
import cfg.PetRunePropertiesObject;
import cfg.SaleManGoodsCfg;
import cfg.SaleManGoodsCfgObject;
import common.GameConst;
import common.IdGenerator;

import java.util.*;

import model.patrol.entity.PatrolTree;
import model.petrune.entity.PetRunePropertyRandom;
import model.reward.RewardUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.util.CollectionUtils;
import protocol.Common.Reward;
import protocol.Common.RewardTypeEnum;
import protocol.Patrol.SaleManGoods;
import protocol.PetMessage;
import protocol.PetMessage.PetMission;
import protocol.PetMessage.Rune;
import protocol.PetMessage.Rune.Builder;
import protocol.PetMessage.RuneProperties;
import protocol.PetMessage.RunePropertieyEntity;

import java.util.stream.Collectors;

/**
 * 工具方法，主要是根据配置随机获取内容等方法
 *
 * @author xiao_FL
 * @date 2019/6/3
 */
public class RandomUtil {
    /**
     * 千分比，概率计算基础值
     */
    private static final int BASE_RANGE = 1000;
    private static final Random RANDOM = new Random();
    private static final int MISSION_REQUIRE_RARITY = 1;
    private static final int MISSION_REQUIRE_TYPE = 2;

    private static final List<Integer> type = new ArrayList<>();

    // 随机需求具体内容
    // 随机具体需求类型：1宠物品质，2宠物职业
    private static final List<Integer> requireListRange = Arrays.asList(MISSION_REQUIRE_RARITY, MISSION_REQUIRE_TYPE);

    static {
        type.add(PatrolTree.EVENT_EXPLORE);
        type.add(PatrolTree.EVENT_CHAMBER);
        type.add(PatrolTree.EVENT_BASTARD);
    }


    /**
     * 获取一个[0,999]的随机数
     *
     * @return 随机数
     */
    public static int getRandom1000() {
        return RANDOM.nextInt(BASE_RANGE);
    }

    /**
     * 按配置几率获取某稀有度宠物
     *
     * @param rarity  稀有度
     * @param type    碎片类型，对应宠物职业，0则是通用碎片
     * @param corePet 核心宠物
     * @return 宠物图鉴id
     */
    public static int getRandomPet(int rarity, int type, boolean corePet) {
        // 读取配置
        List<PetFragmentConfigObject> random = PetFragmentConfig.getProbByRarity(rarity);
        int prob = RANDOM.nextInt(PetFragmentConfig.getTotalWeightByRarityAndType(rarity, type, corePet));
        if (type != 0) {
            // 不是通用碎片，从稀有度+职业限制中随机
            for (PetFragmentConfigObject randomRange : random) {
                if (randomRange.getDebristype() == type && randomRange.getPetcore() == corePet) {
                    if (prob < randomRange.getProbabilitybyclass()) {
                        return randomRange.getPetid();
                    } else {
                        prob -= randomRange.getProbabilitybyclass();
                    }
                }
            }
        } else {
            for (PetFragmentConfigObject randomRange : random) {
                if (randomRange.getPetcore() != corePet) {
                    continue;
                }
                if (prob < randomRange.getProbability()) {
                    return randomRange.getPetid();
                } else {
                    prob -= randomRange.getProbabilitybyclass();
                }
            }
        }
        LogUtil.error("getRandomPet error by rarity:{} and type :{} ", rarity, type);
        return random.get(0).getId();
    }

    public static int getRandomAvailablePet() {
        PetBasePropertiesObject randomPet = PetBaseProperties.basePropertiesList.get(getRandomValue(1, PetBaseProperties._ix_petid.size()));
        if (randomPet.getPetfinished() == 1) {
            return randomPet.getPetid();
        } else {
            return getRandomAvailablePet();
        }
    }

    public static int getRandomHelpPet(int rarity) {
        List<PetBasePropertiesObject> helpPetByRarity = PetBaseProperties.getHelpPetByRarity(rarity);
        if (CollectionUtils.isEmpty(helpPetByRarity)) {
            LogUtil.error("getRandomHelpPet by rarity:{} is empty ", rarity);
            throw new RuntimeException("getRandomHelpPet by rarity is empty");
        }
        return helpPetByRarity.get(new Random().nextInt(helpPetByRarity.size())).getPetid();
    }

    public static List<SaleManGoods> randomSaleGoods() {
        List<SaleManGoods> result = new ArrayList<>();
        int bound = SaleManGoodsCfg._ix_id.values().stream().mapToInt(item ->
                SaleManGoodsCfg.getAppearRateById(item.getId())).sum();

        int goodsNum = GameConfig.getById(GameConst.CONFIG_ID).getPatrolgoogsnum();
        while (bound > 0 && result.size() < goodsNum) {
            SaleManGoods saleManGoods = randomOneSaleGoods(bound);
            if (saleManGoods != null && result.stream().noneMatch(e -> e.getGoodsId() == saleManGoods.getGoodsId())) {
                result.add(saleManGoods);
            }
        }
        return result;
    }

    public static SaleManGoods randomOneSaleGoods(int bound) {
        int condition = RANDOM.nextInt(bound);
        for (SaleManGoodsCfgObject config : SaleManGoodsCfg._ix_id.values()) {
            if (condition < SaleManGoodsCfg.getAppearRateById(config.getId())) {
                int discount = getRandomCfgBy2(config.getDiscount())[0];
                return SaleManGoods.newBuilder().setGoodsId(config.getId()).setDiscount(discount).build();
            }
            condition -= SaleManGoodsCfg.getAppearRateById(config.getId());
        }
        return null;
    }

    /**
     * 根据列表创建符文,默认一个bookId创建一个
     *
     * @param bookList
     * @return
     */
    public static List<Rune> createRuneList(List<Integer> bookList) {
        if (bookList == null || bookList.isEmpty()) {
            return null;
        }

        List<Rune> runeList = new ArrayList<>();
        for (Integer bookId : bookList) {
            Rune rune = getInitRuneById(bookId);
            if (rune != null) {
                runeList.add(getInitRuneById(bookId));
            }
        }
        return runeList;
    }

    public static List<Rune> createRune(int bookId, int count) {
        if (count <= 0) {
            return null;
        }

        List<Rune> runeList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Rune rune = getInitRuneById(bookId);
            if (rune != null) {
                runeList.add(getInitRuneById(bookId));
            }
        }
        return runeList;
    }

    /**
     * 根据runeId随机初始化符文
     *
     * @param runeCfgId 符文id
     * @return 符文实体
     */
    public static Rune getInitRuneById(int runeCfgId) {
        // 读取基础配置，不处理额外属性2
        PetRunePropertiesObject baseProperties = PetRuneProperties.getByRuneid(runeCfgId);
        if (baseProperties == null) {
            LogUtil.error("util.RandomUtil.getInitRuneById, rune bookId =" + runeCfgId + ", is not exist");
            return null;
        }
        Rune.Builder rune = Rune.newBuilder();
        rune.setId(IdGenerator.getInstance().generateId());
        rune.setRuneBookId(runeCfgId);
        // 初始符文等级1
        rune.setRuneLvl(1);
        // 初始符文经验0
        rune.setRuneExp(0);
        // 穿戴宠物
        rune.setRunePet("");
        // 随机基本属性
        PetRuneExp.refreshBaseProperty(rune);
        // 生成符文获得随机属性1
        // 读取套装配置，获得随机属性数量
        int[] rangeList = baseProperties.getExpropertiesrange();
        // 套装配置最大额外属性 - 最小额外属性
        int range = rangeList[1] - rangeList[0];
        if (range >= 0) {
            int amount = RANDOM.nextInt(range + 1) + rangeList[0];
            // 用于序列化
            RuneProperties.Builder exPropertyBuilder = RuneProperties.newBuilder();
            for (int i = 0; i < amount; i++) {
                PetRunePropertyRandom exProperty = randomExProperty(PetRuneProperties.getByRuneid(runeCfgId), rune);
                if (exProperty == null) {
                    LogUtil.error("getInitRuneById randomExProperty error by exProperty is null");
                    continue;
                }
                RunePropertieyEntity.Builder exProperties = RunePropertieyEntity.newBuilder();
                exProperties.setPropertyType(exProperty.getPropertyType());
                exProperties.setPropertyValue(exProperty.getPropertyValue());
                exPropertyBuilder.addProperty(exProperties);
                rune.setRuneExProperty(exPropertyBuilder);
            }
        }
        return rune.build();
    }

    public static Rune.Builder makeHigherRarityRune(Rune preRune, PetRunePropertiesObject highRarityRuneCfg) {
        Rune.Builder highRarityRune = Rune.newBuilder();
        highRarityRune.setId(IdGenerator.getInstance().generateId());
        highRarityRune.setRuneBookId(highRarityRuneCfg.getRuneid());
        // 初始符文等级1
        highRarityRune.setRuneLvl(preRune.getRuneLvl());
        // 初始符文经验0
        highRarityRune.setRuneExp(preRune.getRuneExp());
        // 穿戴宠物
        highRarityRune.setRunePet(preRune.getRunePet());
        // 随机基本属性
        PetRuneExp.refreshBaseProperty(highRarityRune);
        // 生成符文获得随机属性1
        // 读取套装配置，获得随机属性数量
        int[] rangeList = highRarityRuneCfg.getExpropertiesrange();

        // 套装配置最大额外属性 - 最小额外属性
        int range = rangeList[1] - rangeList[0];
        if (range >= 0) {
            int amount = RANDOM.nextInt(range + 1) + rangeList[0];
            // 用于序列化
            RuneProperties.Builder exPropertyBuilder = RuneProperties.newBuilder();
            for (int i = 0; i < amount; i++) {
                PetRunePropertyRandom exProperty = randomExProperty(highRarityRuneCfg, highRarityRune);
                if (exProperty == null) {
                    LogUtil.error("makeHigherRarityRune randomExProperty error by exProperty is null");
                    continue;
                }
                RunePropertieyEntity.Builder exProperties = RunePropertieyEntity.newBuilder();
                exProperties.setPropertyType(exProperty.getPropertyType());
                exProperties.setPropertyValue(exProperty.getPropertyValue());
                exPropertyBuilder.addProperty(exProperties);
                highRarityRune.setRuneExProperty(exPropertyBuilder);
            }
        }
        // 已有属性类型不变
        RuneProperties runeExProperty = preRune.getRuneExProperty();
        List<RunePropertieyEntity> propertyList = runeExProperty.getPropertyList();
        RuneProperties.Builder exPropertyBuilder = RuneProperties.newBuilder();
        for (RunePropertieyEntity runePropertieyEntity : propertyList) {
            int propertyType = runePropertieyEntity.getPropertyType();
            PetRunePropertyRandom exProperty = randomAppearedExProperty(highRarityRuneCfg, highRarityRune, propertyType);
            if (exProperty == null) {
                LogUtil.error("makeHigherRarityRune randomExProperty error1 by exProperty is null");
                continue;
            }
            RunePropertieyEntity.Builder exProperties = RunePropertieyEntity.newBuilder();
            exProperties.setPropertyType(exProperty.getPropertyType());
            exProperties.setPropertyValue(exProperty.getPropertyValue());
            exPropertyBuilder.addProperty(exProperties);
            highRarityRune.setRuneExProperty(exPropertyBuilder);
        }
        return highRarityRune;
    }

    private static PetRunePropertyRandom randomAppearedExProperty(PetRunePropertiesObject config, Builder rune, int propertyType) {
        if (config == null) {
            LogUtil.error("randomAppearedExProperty config is null");
            return null;
        }
        PetRuneExpObject runeExpConfig = PetRuneExp.getByRarityAndLvlAndType(config.getRunerarity(), rune.getRuneLvl(), config.getRunetype());
        if (runeExpConfig == null) {
            LogUtil.error("randomAppearedExProperty config is null by rarity:{},runeLv:{},runeType:{}", config.getRunerarity(), rune.getRuneLvl(), config.getRunetype());
            return null;
        }
        PetRunePropertyRandom result = new PetRunePropertyRandom();
        int[][] exBaseProperties = runeExpConfig.getExbaseproperties();
        if (ArrayUtils.isEmpty(exBaseProperties)) {
            LogUtil.error("randomAppearedExProperty exBaseProperties is empty by PetRuneExpObjectKey:{}", runeExpConfig.getKey());
            return null;
        }
        int[] exProperty = null;
        for (int[] exBaseProperty : exBaseProperties) {
            if (exBaseProperty[0] == propertyType) {
                exProperty = exBaseProperty;
                break;
            }
        }
        if (exProperty == null) {
            LogUtil.error("randomAppearedExProperty the same property type is not found by PetRuneExpObjectKey:{}, propertyType:{}", runeExpConfig.getKey(), propertyType);
            return null;
        }

        result.setPropertyType(exProperty[0]);
        result.setPropertyValue(getRandomValue(exProperty[2], exProperty[3]));

        return result;
    }

    private static int randomLimitMissionCount(int total, int missionLv, int maxLimitMissionCount) {
        if (maxLimitMissionCount <= 0) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < total; i++) {
            if (PetMessage.PetMissionType.PMT_Limit == randomPetMissionType(missionLv)) {
                if (result >= maxLimitMissionCount) {
                    return maxLimitMissionCount;
                }
                result++;
            }
        }
        return Math.min(maxLimitMissionCount, result);
    }

    /**
     * 随机宠物委托任务
     *
     * @param totalCount           任务个数
     * @param mainlinePoint        主线关卡节点
     * @param totalCount           总任务数
     * @param maxLimitMissionCount 最大限制任务数
     * @return
     */
    public static List<PetMission> randomPetMission(int mainlinePoint, int missionLv,
                                                    int totalCount, int maxLimitMissionCount) {
        int limitMissionCount = randomLimitMissionCount(totalCount, missionLv, maxLimitMissionCount);

        if (totalCount <= 0) {
            return Collections.emptyList();
        }

        List<PetMission> result = new ArrayList<>();
        PetMission petMission;
        for (int i = 0; i < totalCount; i++) {
            if (i < limitMissionCount) {
                petMission = randomPetMissionWithType(mainlinePoint, missionLv, PetMessage.PetMissionType.PMT_Limit);
            } else {
                petMission = randomPetMissionWithType(mainlinePoint, missionLv, PetMessage.PetMissionType.PMT_Normal);
            }
            if (petMission != null) {
                result.add(petMission);
            }
        }

        return result;
    }

    private static PetMessage.PetMissionType randomPetMissionType(int playerMissionLv) {
        PetMissionLevelObject cfg = PetMissionLevel.getByMissionlv(playerMissionLv);
        if (cfg == null) {
            return PetMessage.PetMissionType.PMT_Null;
        }
        int[] randomCfgBy2 = getRandomCfgBy2(cfg.getMissiontypeweight());
        return PetMessage.PetMissionType.forNumber(randomCfgBy2[0]);
    }


    /**
     * 获得一个随机宠物委托
     *
     * @return 宠物委托
     */
    public static PetMission randomPetMissionWithType(int mainlinePoint, int playerMissionLv, PetMessage.PetMissionType missionType) {
        if (missionType == null || PetMessage.PetMissionType.PMT_Null == missionType) {
            LogUtil.error("randomPetMissionWithType error params missionType:{}", missionType);
            return null;
        }
        PetMissionLevelObject missionLevelObject = PetMissionLevel.getByMissionlv(playerMissionLv);
        if (missionLevelObject == null) {
            return null;
        }
        int[] randomMissionStar = getRandomCfgBy2(missionLevelObject.getMissinratio());
        if (ArrayUtils.isEmpty(randomMissionStar)) {
            return null;
        }
        int missionStar = randomMissionStar[0];


        MonsterDifficultyObject config = MonsterDifficulty.getById(mainlinePoint);
        if (config == null || config.getPetmissionrewardconfig().length < 1) {
            LogUtil.error("MonsterDifficultyObject cfg is empty by id:[{}]", mainlinePoint);
            return null;

        }

        List<int[]> collect = Arrays.stream(config.getPetmissionrewardconfig()).filter(item -> item.length > 4 && item[4] == missionStar).collect(Collectors.toList());
        int[][] ints = new int[collect.size()][];

        int[] missData = getRandomCfgBy4(collect.toArray(ints));
        if (missData == null || missData.length < 5) {
            LogUtil.error("MonsterDifficulty PetMissionRewardConfig error by length <5,error id:[{}]", mainlinePoint);
            return null;
        }
        PetMission.Builder result = PetMission.newBuilder();
        // 随机任务星级和需求
        PetMissionObject petMissionCfg = cfg.PetMission.getByMissionlvl(missionStar);
        if (petMissionCfg == null) {
            LogUtil.error("PetMission cfg is not exist, mission lv = " + missionStar);
            return null;
        }

        result.setMissionId(IdGenerator.getInstance().generateId());
        // 获得星级
        result.setMissionLvl(missionStar);
        // 随机获得任务描述
        result.setMissionDescription(PetMissionDescription.randomDescriptionId());
        // 随机任务完成时间
        result.setTime(petMissionCfg.getTime());
        setPetMissionCondition(result, petMissionCfg, missionType);
        // 随机具体奖励类型
        Reward reward = parsePetMissionShowReward(missionType, missionStar, missData);
        if (reward != null) {
            result.setReward(reward);
        }
        result.setMissionType(missionType);
        return result.build();

    }

    private static void setPetMissionCondition(PetMission.Builder mission, PetMissionObject petMissionCfg, PetMessage.PetMissionType missionType) {
        if (PetMessage.PetMissionType.PMT_Limit == missionType) {
            mission.setRequiredPetRarity(petMissionCfg.getLimitmissionrarity());
            return;
        }
        //其他任务
        for (int i = 0; i < petMissionCfg.getRequire(); i++) {
            // 随机到的条件
            int condition = RANDOM.nextInt(requireListRange.size());
            addMissionRequire(petMissionCfg.getMissionlvl(), requireListRange.get(condition), mission);
        }
    }


    private static Reward parsePetMissionShowReward(PetMessage.PetMissionType missionType, int missionStar, int[] missData) {
        if (PetMessage.PetMissionType.PMT_Limit == missionType) {
            return Reward.newBuilder().setCount(cfg.PetMission.queryLimitMissionRewardCount(missionStar)).build();
        }
        return RewardUtil.drawMustRandomReward(RewardUtil.parseIntArrayToRandomRewardList(new int[][]{missData}));

    }


    /**
     * 获取一个[min,max)随机值
     *
     * @param min 最小值
     * @param max 最大值
     * @return 随机结果
     */
    public static int getRandomValue(int min, int max) {
        if (min > max) {
            return -1;
        } else if (min == max) {
            return min;
        } else {
            int random = RANDOM.nextInt(max - min);
            return random + min;
        }
    }


    /**
     * 按需求类型给宠物委托添加内容
     *
     * @param missionLvl 委托星级
     * @param i          具体需求类型：1宠物品质，2宠物职业
     * @param mission    委托参数
     */
    private static void addMissionRequire(int missionLvl, int i, PetMission.Builder mission) {
        PetMissionObject require = cfg.PetMission.getByMissionlvl(missionLvl);
        switch (i) {
            case MISSION_REQUIRE_RARITY: {
                int[] rarityRange = require.getPetrarity();
                mission.setRequiredPetRarity(rarityRange[RANDOM.nextInt(rarityRange.length)]);
                break;
            }
            case MISSION_REQUIRE_TYPE: {
                int[] typeRange = require.getPettype();
                mission.setRequiredPetType(typeRange[RANDOM.nextInt(typeRange.length)]);
                break;
            }
            default: {
                break;
            }
        }
    }

    /**
     * 解析一个随机二维数组
     *
     * @param randomCfg [][][][随机概率]
     *                  [][][][随机概率]
     *                  [][][][随机概率]
     *                  [][][][随机概率]
     * @return 随机到的某一行数据
     */
    public static int[] getRandomCfgBy4(int[][] randomCfg) {
        return getRandomCfgByWeightIndex(randomCfg, 4);
    }

    /**
     * 解析一个随机二维数组
     *
     * @param randomCfg [][随机概率][][]
     *                  [][随机概率][][]
     *                  [][随机概率][][]
     *                  [][随机概率][][]
     * @return 随机到的某一行数据
     */
    public static int[] getRandomCfgBy2(int[][] randomCfg) {
        return getRandomCfgByWeightIndex(randomCfg, 2);
    }

    public static int[] getRandomCfgByWeightIndex(int[][] randomCfg, int index) {
        index = index - 1;
        int range = 0;
        for (int[] i : randomCfg) {
            range += i[index];
        }
        int randomRange = RANDOM.nextInt(range);
        for (int[] i : randomCfg) {
            if (randomRange < i[index]) {
                return i;
            } else {
                randomRange -= i[index];
            }
        }
        LogUtil.error("error in RandomUtil,method getRandomCfgBy2(),random out range:randomRange = " + randomRange + ",randomCfg = " + Arrays.deepToString(randomCfg) + "\n");
        throw new OutOfRangeException();
    }

    public static List<Reward> mergeReward(List<Reward> oldRewardList, List<Reward> newRewardList) {
        if (newRewardList == null) {
            return oldRewardList;
        }
        if (oldRewardList.size() == 0) {
            return newRewardList;
        } else {
            oldRewardList = new ArrayList<>(oldRewardList);
            for (Reward reward : newRewardList) {
                for (int i = 0; i < oldRewardList.size(); i++) {
                    if (reward.getRewardType() == RewardTypeEnum.RTE_Pet) {
                        break;
                    }
                    if (reward.getRewardType() == oldRewardList.get(i).getRewardType()
                            && reward.getId() == oldRewardList.get(i).getId()) {
                        Reward oldReward = oldRewardList.get(i);
                        oldRewardList.remove(oldReward);
                        oldRewardList.add(reward.toBuilder().setCount(reward.getCount() + oldReward.getCount()).build());
                        break;
                    }
                    if (i == oldRewardList.size() - 1) {
                        oldRewardList.add(reward);
                        break;
                    }
                }
            }
            return oldRewardList;
        }
    }

    public static List<Reward> mergeReward(List<Reward> rewardList, Reward reward) {
        if (rewardList == null || reward == null) {
            if (rewardList != null) {
                return rewardList;
            } else if (reward != null) {
                List<Reward> result = new ArrayList<>();
                result.add(reward);
                return result;
            } else {
                return new ArrayList<>();
            }
        }
        if (rewardList.size() == 0 || reward.getRewardType() == RewardTypeEnum.RTE_Pet) {
            rewardList.add(reward);
            return rewardList;
        } else {
            Iterator<Reward> iterator = rewardList.iterator();
            while (iterator.hasNext()) {
                Reward rewardTemp = iterator.next();
                if (rewardTemp.getRewardType().equals(reward.getRewardType()) && rewardTemp.getId() == reward.getId()) {
                    Reward newReward = rewardTemp.toBuilder()
                            .setCount(rewardTemp.getCount() + reward.getCount())
                            .build();
                    iterator.remove();
                    rewardList.add(newReward);
                    return rewardList;
                }
            }
            rewardList.add(reward);
            return rewardList;
        }
    }

    /**
     * 包含上界
     *
     * @param border_1
     * @param border_2
     * @return
     */
    public static int randomInScope(int border_1, int border_2) {
        int max = Math.max(border_1, border_2);
        int min = Math.min(border_1, border_2);
        if (max <= min) {
            return max;
        }

        return min + new Random().nextInt(max - min + 1);
    }


    public static int randomTreasureGreedConfig(List<Integer> data) {
        if (CollectionUtils.isEmpty(data)) {
            int length = GameConfig.getById(GameConst.CONFIG_ID).getTreasuregreedconfig().length;
            for (int i = 1; i < length; i++) {
                data.add(i);
            }
        }
        int index = RandomUtil.randomInScope(0, data.size() - 1);
        int result = data.get(index);
        data.remove(index);
        return result;
    }

    public static PetRunePropertyRandom randomExProperty(PetRunePropertiesObject config, Builder rune) {
        if (config == null) {
            LogUtil.error("randomExProperty config is null");
            return null;
        }
        PetRuneExpObject runeExpConfig = PetRuneExp.getByRarityAndLvlAndType(config.getRunerarity(), rune.getRuneLvl(), config.getRunetype());
        if (runeExpConfig == null) {
            LogUtil.error("runeExpConfig config is null by rarity:{},runeLv:{},runeType:{}", config.getRunerarity(), rune.getRuneLvl(), config.getRunetype());
            return null;
        }
        PetRunePropertyRandom result = new PetRunePropertyRandom();
        int[][] exBaseProperties = runeExpConfig.getExbaseproperties();
        if (ArrayUtils.isEmpty(exBaseProperties)) {
            LogUtil.error("runeExpConfig exBaseProperties is empty by PetRuneExpObjectKey:{}", runeExpConfig.getKey());
            return null;
        }
        //获取可以相同的额外属性
        int[] exProperty = getRandomCfgBy2(exBaseProperties);
        //获取不同类型的额外属性(暂时注释,可能改回来)
        // int[] exProperty = getRandomCfgBy2(getExPropertyConfigWithOutObtain(exBaseProperties, rune));
        result.setPropertyType(exProperty[0]);
        result.setPropertyValue(getRandomValue(exProperty[2], exProperty[3]));
        return result;
    }


   /* public static int[][] getExPropertyConfigWithOutObtain(int[][] exProperties, Builder rune) {
        if (rune.getRuneExProperty().getPropertyCount() <= 0 || ArrayUtils.isEmpty(exProperties)) {
            return exProperties;
        }
        //已拥有的属性类型
        List<Integer> collect = rune.getRuneExProperty().getPropertyList().stream().map(RunePropertieyEntity::getPropertyType).distinct().collect(Collectors.toList());
        int[][] ints = new int[exProperties.length - collect.size()][exProperties[0].length];
        //去除已拥有的属性类型
        int index = 0;
        for (int[] exProperty : exProperties) {
            if (!collect.contains(exProperty[0])) {
                ints[index++] = exProperty;
            }
        }
        return ints;
    }*/


    public static List<Integer> batchRandomFromList(List<Integer> numPool, int needNum, boolean canRepeated) {
        if (CollectionUtils.isEmpty(numPool)) {
            return Collections.emptyList();
        }
        List<Integer> nums = new ArrayList<>();
        if (canRepeated) {
            for (int i = 0; i < needNum; i++) {
                nums.add(randomOneFromList(numPool));
            }
            return nums;

        }
        if (numPool.size() < needNum) {
            LogUtil.error("batchRandomFromList,numPool size less than needNum");
            return numPool;
        }
        int random;
        while (nums.size() < needNum) {
            random = randomOneFromList(numPool);
            if (!nums.contains(random)) {
                nums.add(random);
            }

        }
        return nums;
    }

    public static Integer randomOneFromList(List<Integer> numPool) {
        return numPool.get(RandomUtils.nextInt(numPool.size()));

    }

    public static boolean canRandomHit(int rate) {
        return getRandom1000() < rate;
    }
}


/**
 * 超出范围的异常
 */
class OutOfRangeException extends RuntimeException {
    OutOfRangeException() {
        super();
    }
}