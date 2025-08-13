package model.pet.entity;

import cfg.BuffCondition;
import cfg.BuffConditionObject;
import cfg.BuffConfig;
import cfg.BuffConfigObject;
import cfg.LinkConfig;
import cfg.LinkConfigObject;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import cfg.PetEvolveCfg;
import cfg.PetEvolveCfgObject;
import cfg.PetRuneProperties;
import cfg.PetRuneSuitProperties;
import common.GameConst;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import protocol.PetMessage;
import protocol.PetMessage.PetProperty;
import util.LogUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @Description 宠物战力计算(Pet 对象中存储的战力是不包含群体加成 ( 比如神器) 而战斗中因为需要把加成属性添加上,所以战力全体加成后的战力  )
 * @Author hanx
 * @Date2020/8/11 0011 15:45
 **/
@Getter
public class FightPowerCalculate {
    private int attack;
    private int defence;
    private int heath;
    private int missRate;
    private int accuracy;
    private int criticalRate;
    private int criticalRateResistance;
    private int criticalDamageResistance;
    private int criticalDamage;
    private int petLv;
    private int evolveAbility;
    /**
     * 符文套装战力加成
     */
    @Setter
    private int runeSuitAbilityAddition;

    private int fixAbility;

    private int fixFactor;

    public FightPowerCalculate(Map<Integer, Integer> propertyMap) {
        if (MapUtils.isEmpty(propertyMap)) {
            return;
        }
        for (Entry<Integer, Integer> property : propertyMap.entrySet()) {
            setPropertyValue(property.getKey(), property.getValue());
        }

    }


    public FightPowerCalculate(PetMessage.Pet.Builder pet, List<PetMessage.Rune> runeList) {
        for (PetMessage.PetPropertyEntity property : pet.getPetProperty().getPropertyList()) {
            setPropertyValue(property.getPropertyType(), property.getPropertyValue());
        }
        setRuneSuitAbilityAddition(getSuitAbilityAddition(runeList));
        this.petLv = pet.getPetLvl();
        if (pet.getEvolveLv() > 0) {
            PetEvolveCfgObject cfg = PetEvolveCfg.getInstance().getByPetIdAndEvolveLv(pet.getPetBookId(), pet.getEvolveLv());
            if (cfg != null) {
                this.evolveAbility = cfg.getAddability();
            }
        }
        setLinkAddition(pet);
    }

    private void setLinkAddition(PetMessage.Pet.Builder pet) {
        if (pet.getActiveLinkCount() <= 0) {
            return;
        }
        for (Integer linkId : pet.getActiveLinkList()) {
            LinkConfigObject cfg = LinkConfig.getById(linkId);
            if (cfg==null){
                continue;
            }
            int petIndex = LinkConfig.findPetIndex(cfg, pet.getPetBookId());
            if (petIndex == -1) {
                continue;
            }
            fixAbility += cfg.getFixfight()[petIndex];
            fixFactor += cfg.getLvlfightfactor()[petIndex];
        }
    }


    public static long getAbilityInTeam(int petBookId, long curAbility, List<Integer> allExBuff, int petIndex) {

        long fixAddition1 = 0;

        long fixAddition2 = 0;

        int abilityAdditionRate = 0;

        if (!CollectionUtils.isEmpty(allExBuff)) {
            PetBasePropertiesObject petBaseConfig = PetBaseProperties.getByPetid(petBookId);
            if (petBaseConfig == null) {
                LogUtil.error("getAbilityInTeam petBaseConfig is null by petBookId:{}", petBookId);
                return curAbility;
            }

            for (Integer buffId : allExBuff) {
                BuffConfigObject config = BuffConfig.getById(buffId);
                if (config == null) {
                    LogUtil.warn("getAbilityInTeam buffConfig is null by buffId:{}", buffId);
                    continue;
                }

                if (!satisfyBuffCondition(buffId, petBaseConfig, petIndex)) {
                    continue;
                }

                fixAddition1 += config.getFixability1();
                fixAddition2 += config.getFixability2();
                abilityAdditionRate += config.getAbilityaddtion();
            }
        }


        return (long) ((curAbility + fixAddition1) * (1 + abilityAdditionRate / GameConst.petAdditionMagnification) + fixAddition2);
    }

    private static boolean satisfyBuffCondition(Integer buffId, PetBasePropertiesObject petBaseConfig, int petIndex) {
        BuffConditionObject condition = BuffCondition.getById(buffId);
        if (condition == null) {
            return true;
        }
        if (condition.getPetclass() > 0 && condition.getPetclass() != petBaseConfig.getPetclass()) {
            return false;
        }
        if (condition.getPettype() > 0 && condition.getPettype() != petBaseConfig.getPettype()) {
            return false;
        }
        if (condition.getOnbattleindex() > 0 && petIndex >= condition.getOnbattleindex()) {
            return false;
        }
        return true;
    }


    public void setPropertyValue(int proType, int propertyValue) {
        if (PetProperty.ATTACK_VALUE == proType) {
            attack = propertyValue;
        } else if (PetProperty.DEFENSIVE_VALUE == proType) {
            defence = propertyValue;
        } else if (PetProperty.HEALTH_VALUE == proType) {
            heath = propertyValue;
        } else if (PetProperty.CRITICAL_RATE_VALUE == proType) {
            criticalRate = propertyValue;
        } else if (PetProperty.CRITICAL_DAMAGE_VALUE == proType) {
            criticalDamage = propertyValue;
        } else if (PetProperty.MISS_VALUE == proType) {
            missRate = propertyValue;
        } else if (PetProperty.ACCURACY_VALUE == proType) {
            accuracy = propertyValue;
        } else if (PetProperty.CRIT_RATE_Resistance_VALUE == proType) {
            criticalRateResistance = propertyValue;
        } else if (PetProperty.CRIT_DAMAGE_Resistance_VALUE == proType) {
            criticalDamageResistance = propertyValue;
        }
    }

    /**
     * 攻击+防御*2.5+生命*0.14）+（闪避值+命中值+暴击值+免暴值+爆伤值+抗爆值-24600）*0.125*宠物等级+进化技能固定战力+符文套装固定战力+通用固定战力+固定系数战力*宠物等级
     *
     * @return 战力
     */
    private double calculateBaseAbilityD() {
        return Math.floor((attack + defence * 2.5 + heath * 0.14) +
                (missRate + accuracy + criticalDamage + criticalRate + criticalRateResistance + criticalDamageResistance - 24600) * 0.125 * petLv)
                + runeSuitAbilityAddition + evolveAbility + fixAbility + fixFactor / 1000.0 * petLv;
    }

    /**
     * 计算加成战力 这里不会减去基础战力计算的补偿(24600)
     * 目前加成类战斗力没有涉及到特殊属性(攻防血以外 ,如果有需要找策划再做调整)
     *
     * @return
     */
    public long calculateAdditionAbility() {
        return new Double(Math.floor((attack + defence * 2.5 + heath * 0.14))).longValue();
    }

    public long calculateAbilityL() {

        return new Double(calculateBaseAbilityD()).longValue();
    }


    private int getSuitAbilityAddition(List<PetMessage.Rune> runeList) {
        if (org.springframework.util.CollectionUtils.isEmpty(runeList)) {
            return 0;
        }

        int addition = 0;
        Map<Integer, List<PetMessage.Rune>> runeMap = runeList.stream().
                collect(Collectors.groupingBy(rune -> PetRuneProperties.getByRuneid(rune.getRuneBookId()).getRunesuit()));
        // 统计计算套装加成，KV:key-propertyType,value-propertyValue
        for (Integer suitId : runeMap.keySet()) {
            int temp = 0;
            for (int[] fightAdd : PetRuneSuitProperties.getBySuitid(suitId).getFightadd()) {
                // 检查到套装属性要求小于已穿戴的套装数，触发套装效果
                if (fightAdd.length > 1 && fightAdd[0] <= runeMap.get(suitId).size()) {
                    temp = Math.max(temp, fightAdd[1]);
                }
            }
            addition += temp;
        }
        return addition;
    }
}
