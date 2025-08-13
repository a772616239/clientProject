package model.arena.entity;

import cfg.BuffCondition;
import cfg.BuffConditionObject;
import cfg.BuffConfig;
import cfg.BuffConfigObject;
import cfg.PetBaseProperties;
import cfg.PetBasePropertiesObject;
import common.GameConst;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import protocol.PetMessage.PetProperty;
import util.LogUtil;

import java.util.List;
import java.util.Map;

/**
 * @Description 宠物战力计算
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
    /**
     * 符文套装战力加成
     */
    @Setter
    private int runeSuitAbilityAddition;

    private double evolveAbility;

    public FightPowerCalculate(Map<Integer, Integer> propertyMap) {
        if (MapUtils.isEmpty(propertyMap)) {
            return;
        }
        for (Map.Entry<Integer, Integer> property : propertyMap.entrySet()) {
            setValue(property.getKey(), property.getValue());
        }
    }


    public FightPowerCalculate(int petLv) {
        this.petLv = petLv;
    }

    public static long getAbilityInTeam(int petBookId, long curAbility, List<Integer> allExBuff, int petIndex) {

        long fixAddition1 = 0;

        long fixAddition2 = 0;

        int abilityAdditionRate = 0;

        if (!CollectionUtils.isEmpty(allExBuff)) {
            PetBasePropertiesObject petBaseConfig = PetBaseProperties.getByPetid(petBookId);
            if (petBaseConfig == null) {
                LogUtil.error("getAbilityInTeam petBaseConfig is null by petBookId:" + petBookId);
                return curAbility;
            }

            for (Integer buffId : allExBuff) {
                BuffConfigObject config = BuffConfig.getById(buffId);
                if (config == null) {
                    LogUtil.warn("getAbilityInTeam buffConfig is null by buffId:" + buffId);
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


    public void setValue(int proType, int propertyValue) {
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
     * 攻击+防御*2.5+生命*0.14+（（闪避值+命中值+暴击值+免暴值+爆伤值+抗爆值-24600）*0.125*宠物等级+进化技能固定战力
     *
     * @return 战力
     */
    private double calculateBaseAbility() {
        return Math.floor((attack + defence * 2.5 + heath * 0.14) +
                (missRate + accuracy + criticalDamage + criticalRate + criticalRateResistance + criticalDamageResistance - 24600) * 0.125 * petLv)
                + runeSuitAbilityAddition + evolveAbility;
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

    public long calculateAbility() {

        return new Double(calculateBaseAbility()).longValue();
    }

}
