package model.arena.util;

import cfg.BattleSubTypeConfig;
import cfg.BattleSubTypeConfigObject;
import cfg.GameConfig;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetBondConfig;
import cfg.PetBondConfigObject;
import cfg.PetLvlGroupUpConfig;
import cfg.PetLvlGroupUpConfigObject;
import cfg.PetRarityConfig;
import cfg.PetRarityConfigObject;
import common.GameConst;
import common.IdGenerator;
import model.arena.entity.FightPowerCalculate;
import org.springframework.util.CollectionUtils;
import protocol.Battle;
import protocol.Battle.BattlePetData;
import protocol.Battle.PetPropertyDict;
import protocol.PetMessage.Pet;
import protocol.PetMessage.PetProperties;
import protocol.PetMessage.PetProperty;
import protocol.PetMessage.PetPropertyEntity;
import protocol.PetMessage.Rune;
import protocol.RetCodeId.RetCodeEnum;
import util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author huhan
 * @date 2020/05/15
 */
public class ArenaPetUtil {


    public static List<BattlePetData> buildPetBattleData(Battle.BattleSubTypeEnum subTypeEnum, List<Pet> petList, boolean player) {

        List<BattlePetData> result = new ArrayList<>();
        if (petList == null) {
            return result;
        }
        List<Integer> bookIds = petList.stream().map(Pet::getPetBookId).collect(Collectors.toList());
        List<Integer> bonusBuff = getTeamBattleBonusBuff(subTypeEnum, bookIds, player);
        for (Pet pet : petList) {

            //当前宠物属性加成
            BattlePetData.Builder battlePet = BattlePetData.newBuilder();
            battlePet.setPetId(pet.getId());
            battlePet.setPetLevel(pet.getPetLvl());
            battlePet.setPetRarity(pet.getPetRarity());
            battlePet.setPetCfgId(pet.getPetBookId());
            battlePet.setAwake(pet.getPetUpLvl());
            battlePet.setEvolveLv(pet.getEvolveLv());
            PetPropertyDict.Builder property = PetPropertyDict.newBuilder();
            for (PetPropertyEntity propertyEntity : pet.getPetProperty().getPropertyList()) {
                property.addKeys(PetProperty.forNumber(propertyEntity.getPropertyType()));
                property.addValues(propertyEntity.getPropertyValue());
                if (propertyEntity.getPropertyType() == PetProperty.HEALTH_VALUE) {
                    property.addKeys(PetProperty.Current_Health);
                    property.addValues(GameConst.PetMaxHpRate);
                }
            }
            battlePet.setPropDict(property);
            battlePet.setAbility(FightPowerCalculate.getAbilityInTeam(pet.getPetBookId(), pet.getAbility()
                    , bonusBuff, petList.indexOf(pet)));

            result.add(battlePet.build());
        }

        return result;
    }

    /**
     * 玩家需要计算羁绊 ,其他的按照BattleSubTypeConfig来判断
     *
     * @param subType
     * @param petBookIds
     * @param player
     * @return
     */
    private static List<Integer> getTeamBattleBonusBuff(Battle.BattleSubTypeEnum subType, List<Integer> petBookIds, boolean player) {
        if (CollectionUtils.isEmpty(petBookIds) || noBonusBuff(subType, player)) {
            return Collections.emptyList();
        }
        //<种族id,个数>
        Map<Integer, Long> bonusNumMap = petBookIds.stream().collect(Collectors.groupingBy(bookId -> {
            if (bookId == null) {
                return 0;
            }
            PetBasePropertiesObject baseProp = PetBaseProperties.getByPetid(bookId);
            return baseProp != null ? baseProp.getPettype() : 0;
        }, Collectors.counting()));

        return PetBondConfig.getInstance().queryBonusBuffs(bonusNumMap, player);
    }

    private static boolean noBonusBuff(Battle.BattleSubTypeEnum subType, boolean player) {
        if (player) {
            return false;
        }
        if (subType == null) {
            LogUtil.warn("petCache getTeamBattleBonusBuff subType is null ");
            return true;
        }

        BattleSubTypeConfigObject cfg = BattleSubTypeConfig.getById(subType.getNumber());
        if (cfg == null) {
            LogUtil.error("getTeamBattleBonusBuff error BattleSubTypeConfig is null by battleSubType:" + subType);
            return true;
        }
        if (!cfg.getHasmonsterbondbuff()) {
            return true;
        }
        return false;
    }

    /**
     * @param petBookIds 编队宠物BookId
     * @return
     */
    public static List<Integer> getTeamBonusBuff(List<Integer> petBookIds) {
        if (CollectionUtils.isEmpty(petBookIds) || petBookIds.size() <= 1) {
            return Collections.emptyList();
        }
        //<种族id,个数>
        Map<Integer, Long> bonusNumMap = petBookIds.stream().collect(Collectors.groupingBy(bookId -> {
            if (bookId == null) {
                return 0;
            }
            PetBasePropertiesObject baseProp = PetBaseProperties.getByPetid(bookId);
            return baseProp != null ? baseProp.getPettype() : 0;
        }, Collectors.counting()));
        List<Integer> buffs = new ArrayList<>();
        for (Entry<Integer, Long> entry : bonusNumMap.entrySet()) {
            for (Entry<String, PetBondConfigObject> config : PetBondConfig._ix_id.entrySet()) {
                if (triggerCurrentBonus(entry.getKey(), entry.getValue(), config.getValue())) {
                    buffs.add(config.getValue().getBuffid());
                }
            }
        }
        return buffs;
    }


    public static long getAbilityInTeam(Pet.Builder pet, int bonusRate) {
        if (pet == null) {
            return 0;
        }
        return pet.getAbility() + pet.getAbility() * bonusRate / 1000;
    }

    /**
     * 是否触发当前羁绊
     *
     * @param petType
     * @param petNum
     * @param config
     * @return
     */
    private static boolean triggerCurrentBonus(Integer petType, long petNum, PetBondConfigObject config) {
        if (petType == null) {
            return false;
        }
        List<Integer> bondIds = getBondTypesFromConfig(config);
        if (bondIds != null && bondIds.size() > 1 && petType.equals(bondIds.get(0))) {
            int needPetNum = petBondNeedPetNum(bondIds.get(1));
            return petNum >= needPetNum;
        }
        return false;
    }


    /**
     * 当前羁绊等级需要的宠物数
     *
     * @param level
     * @return
     */
    private static int petBondNeedPetNum(int level) {
        int[] bondLevel = GameConfig.getById(GameConst.ConfigId).getBondlevel();
        if (level > bondLevel.length || bondLevel.length == 0) {
            LogUtil.error("level in gameConfig`s bondLevel error,level+[" + level + "],bondLevel"
                    + Arrays.toString(bondLevel));
        }
        return bondLevel[level - 1];
    }

    private static List<Integer> getBondTypesFromConfig(PetBondConfigObject petBondConfig) {
        String[] idArray = petBondConfig.getId().split(",");
        if (idArray.length < 2) {
            LogUtil.error("petBondConfig error with configId+[" + petBondConfig.getId() + "]");
            return Collections.emptyList();
        }
        return Arrays.stream(idArray).map(Integer::parseInt).collect(Collectors.toList());
    }


    /**
     * 刷新宠物属性
     *
     * @param pet 原宠物
     */
    public static Pet.Builder refreshPetDataWithoutRune(Pet.Builder pet) {
        if (pet == null) {
            return null;
        }

        PetBasePropertiesObject petBaseProperties = PetBaseProperties.getByPetid(pet.getPetBookId());
        PetLvlGroupUpConfigObject lvCfg = PetLvlGroupUpConfig.findByPetPropertyModelAndLv(petBaseProperties.getPropertymodel(), pet.getPetLvl());

        FightPowerCalculate fightPowerCalculate = new FightPowerCalculate(pet.getPetLvl());

        List<PetPropertyEntity> proList = new ArrayList<>(pet.getPetProperty().getPropertyList());
        for (PetPropertyEntity propertyEntity : pet.getPetProperty().getPropertyList()) {
            int propertyType = propertyEntity.getPropertyType();
            int value = getBasePetPropertyByConfig(pet, petBaseProperties, lvCfg, propertyType);
            refreshProp(value, proList, propertyType);
            fightPowerCalculate.setValue(propertyType, value);
        }

        pet.getPetPropertyBuilder().clear().addAllProperty(proList);
        pet.setAbility(fightPowerCalculate.calculateAbility());
        return pet;
    }


    private static void refreshProp(int value, List<PetPropertyEntity> proList, int propertyType) {
        if (CollectionUtils.isEmpty(proList)) {
            return;
        }
        Optional<PetPropertyEntity> proInCache = proList.stream().
                filter(pro -> propertyType == pro.getPropertyType() && value != pro.getPropertyValue()).findAny();

        if (proInCache.isPresent()) {
            proList.remove(proInCache.get());
            proList.add(PetPropertyEntity.newBuilder(proInCache.get()).setPropertyValue(value).build());
        }
    }


    /**
     * 当前宠物的属性(包含等级,不计算装备)
     *
     * @param pet
     * @param petBaseProperties
     * @param lvCfg
     * @param propertyType
     * @return
     */
    private static int getBasePetPropertyByConfig(Pet.Builder pet, PetBasePropertiesObject petBaseProperties,
                                           PetLvlGroupUpConfigObject lvCfg, int propertyType) {
        //基础属性
        int petBaseValue = getBasicPropertyValueByCfg(petBaseProperties.getPetproperties(), propertyType);
        int factor;
        //基础属性计算
        if (PetProperty.ATTACK_VALUE == propertyType || PetProperty.DEFENSIVE_VALUE == propertyType
                || PetProperty.HEALTH_VALUE == propertyType) {
            // 卡牌品质加成
            if (pet.getPetRarity() > petBaseProperties.getMaxrarity()) {
                pet.setPetRarity(petBaseProperties.getMaxrarity());
            }
            PetRarityConfigObject rarityConfig = PetRarityConfig.getByRarityAndPropertyModel(pet.getPetRarity(), petBaseProperties.getPropertymodel());
            if (rarityConfig == null) {
                LogUtil.error("cant`t find rarityConfig by key:" + pet.getPetRarity() + ",petBookId:" + pet.getPetBookId());
                return petBaseValue;
            }
            factor = getValueFromKeyValueIntArray(lvCfg.getFactor(), propertyType);
            //攻防血 = 基础值*品质系数*等级系数
            return (int) Math.floor(petBaseValue * ((double) rarityConfig.getPetfactor() / 1000) * ((double) factor / 1000));
        }
        //暴击爆伤,抗暴击抗暴伤....攻防血以外其他属性= 基础值*品质系+等级系数
        factor = getValueFromKeyValueIntArray(lvCfg.getOtherfactors(), propertyType);
        return petBaseValue + factor;
    }

    public static int getValueFromKeyValueIntArray(int[][] data, int key) {
        for (int[] ints : data) {
            if (ints.length < 2) {
                continue;
            }
            if (ints[0] == key) {
                return ints[1];
            }
        }
        return 0;
    }



    public static List<Pet.Builder> getPetBuilder(int petBookId, int count, int sourceValue) {
        List<Pet.Builder> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Pet.Builder newPet = getPetBuilder(petBookId, sourceValue);
            if (null != newPet) {
                result.add(newPet);
            }
        }
        return result;
    }

    /**
     * 按枚举类型取出配置属性值
     *
     * @param source 配置数据，二维数组：
     *               [属性类型][属性值]
     *               [属性类型][属性值]
     * @param type   属性类型
     * @return 属性值
     */
    private static int getBasicPropertyValueByCfg(int[][] source, int type) {
        if (source != null) {
            for (int[] property : source) {
                if (property[0] == type) {
                    return property[1];
                }
            }
        }
        LogUtil.error("error in PetCache,method getPropertyValueByCfg(),source: ");
        if (source != null) {
            for (int[] ints : source) {
                LogUtil.error("" + Arrays.toString(ints) + "");
            }
        } else {
            LogUtil.error("source is null");
        }
        LogUtil.error(",type: " + type + "\n");
        return 0;
    }

    public static Pet.Builder getPetBuilder(int petBookId) {
        return getPetBuilder(petBookId, 0);
    }

    public static Pet.Builder getPetBuilder(int petBookId, int sourceValue) {
        PetBasePropertiesObject petInit = PetBaseProperties.getByPetid(petBookId);
        if (petInit == null) {
            LogUtil.error("model.pet.dbCache.service.PetServiceImpl.getPetEntity, pet bool id is not exist, book id =" + petBookId);
            return null;
        }
        return getPetBuilder(petInit, sourceValue);
    }

    /**
     * 通过配置初始化宠物
     *
     * @return 初始化宠物实体
     */
    public static Pet.Builder getPetBuilder(PetBasePropertiesObject pet, int sourceValue) {
        if (pet == null) {
            LogUtil.error("error in PetServiceImpl,method getPetEntity():pet cfg is null" + "\n");
            return null;
        }
        // 配置表属性部分数据已*1000，注意

        Pet.Builder result = Pet.newBuilder();
        // 基础属性
        result.setId(IdGenerator.getInstance().generateId());
        result.setPetBookId(pet.getPetid());
        result.setPetLvl(1);
        result.setPetRarity(pet.getStartrarity());
        result.setPetUpLvl(0);
        // 状态初始化
        result.setPetLockStatus(0);
        result.setPetMineStatus(0);
        result.setPetMissionStatus(0);
        result.setPetTeamStatus(0);
        result.setPetAliveStatus(1);
        // 属性初始化
        PetProperties.Builder properties = PetProperties.newBuilder();
        for (PetProperty value : PetProperty.values()) {
            if (value != PetProperty.UNRECOGNIZED && PetProperty.ATTACK_VALUE <= value.getNumber() && value.getNumber() <= PetProperty.ATTACK_SPEED_VALUE) {
                PetPropertyEntity.Builder property = PetPropertyEntity.newBuilder();
                property.setPropertyType(value.getNumber());
                property.setPropertyValue(getBasicPropertyValueByCfg(pet.getPetproperties(), value.getNumber()));
                properties.addProperty(property);
            }
        }
        result.setPetProperty(properties);
        result.setSource(sourceValue);
        result.setAbility(getAbilityWithoutRune(result));
        return result;
    }

    /**
     * 计算宠物战斗力
     *
     * @param pet 宠物当前状态
     * @return 新pet实体
     */
    private static long getAbilityWithoutRune(Pet.Builder pet) {
        if (null == pet) {
            return 0;
        }
        FightPowerCalculate fightPowerCalculate = new FightPowerCalculate(pet.getPetLvl());
        for (PetPropertyEntity propertyEntity : pet.getPetProperty().getPropertyList()) {
            fightPowerCalculate.setValue(propertyEntity.getPropertyType(), propertyEntity.getPropertyValue());
        }
        return fightPowerCalculate.calculateAbility();
    }


}
