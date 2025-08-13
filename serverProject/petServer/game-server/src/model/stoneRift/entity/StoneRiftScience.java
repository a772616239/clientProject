package model.stoneRift.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import protocol.StoneRift;

public class StoneRiftScience implements Serializable {

    private static final long serialVersionUID = -2738039574328508775L;

    private Map<Integer, Integer> skillLvMap = new HashMap<>();

    private Map<StoneRift.StoneRiftScienceEnum,Map<Integer,Integer>> scienceEffect2 = new HashMap<>();

    public Map<StoneRift.StoneRiftScienceEnum, Map<Integer, Integer>> getScienceEffect2() {
        return scienceEffect2;
    }


    public void setScienceEffect2(Map<StoneRift.StoneRiftScienceEnum, Map<Integer, Integer>> scienceEffect2) {
        this.scienceEffect2 = scienceEffect2;
    }

    public Map<Integer, Integer> getSkillLvMap() {
        return skillLvMap;
    }

    public void setSkillLvMap(Map<Integer, Integer> skillLvMap) {
        this.skillLvMap = skillLvMap;
    }


    private Map<StoneRift.StoneRiftScienceEnum,Integer> scienceEffect = new HashMap<>();

    public Map<StoneRift.StoneRiftScienceEnum, Integer> getScienceEffect() {
        return scienceEffect;
    }

    public void setScienceEffect(Map<StoneRift.StoneRiftScienceEnum, Integer> scienceEffect) {
        this.scienceEffect = scienceEffect;
    }
    /* *//**
     * 货币A上限增加
     *//*
    private int AddCurrencyALimitRate;

    *//**
     * 偷取资源增加率
     *//*
    private int stealIncrRate;

    *//**
     * 增加偷取次数
     *//*
    private int stealIncrTime;

    *//**
     * 是否可超载
     *//*
    private boolean canOverLoad;

    *//**
     * 超载冷却时间对原值比例
     *//*
    private int overloadIntervalRate;

    *//**
     * 耐久减少慢千分比
     *//*
    private int durableReduceRate;

    *//**
     * 超载效能提升千分比
     *//*
    private int overLoadEffectIncrRate;

    *//**
     * 收取自身矿暴击率
     *//*
    private int claimCriticalRate;


    *//**
     * 收取自身矿暴击倍率
     *//*
    private int claimCriticalMagnification;

    *//**
     * 生产效率增加千分之X
     *//*
    private int productAddRate;

    *//**
     * 富饶状态下增加耐久
     *//*
    private int AddDurableWhenFactoryRich;

    *//**
     * 偷取暴击率
     *//*
    private int stealCriticalProbability;

    *//**
     * 矿场储存上限增加率
     *//*
    private int factoryStoreUpRate;

    *//**
     * 货币A上限增加率
     *//*
    private int currencyAMaxUpRate;

    *//**
     * 货币A产出效率
     *//*
    private int currencyAEfficientRate;

    *//**
     * 最大可驻守宠物品质
     *//*
    private int canDefendMaxRarityPet;

    *//**
     * 获取货币B概率
     *//*
    private int gainCurrencyBRate;

    *//**
     * 偷取稀有道具 概率
     *//*
    private int stealRareItemProbability;

    *//**
     * X级矿场生产效率+千分之X
     *//*
    private Map<Integer, Integer> factoryAddWorkRate;

    *//**
     * x级矿收取到达满仓获得稀有道具
     *//*
    private boolean claimMaxGainRareItem;

    *//**
     * 解锁更多稀有道具
     *//*
    private boolean unlockMoreGoods;

    *//**
     * X级矿场储存上限增加千分之X(矿最低等级,增加比例)
     *//*
    private int factoryStoreMaxAddRate;

    *//**
     * //X级矿场生产效率千分之X(矿最低等级,增加比例)
     *//*
    private int XLevelFactoryWorkIncrRate;


    public Map<Integer, Integer> getSkillLvMap() {
        return skillLvMap;
    }

    public void setSkillLvMap(Map<Integer, Integer> skillLvMap) {
        this.skillLvMap = skillLvMap;
    }

    public int getAddCurrencyALimitRate() {
        return AddCurrencyALimitRate;
    }

    public void setAddCurrencyALimitRate(int addCurrencyALimitRate) {
        AddCurrencyALimitRate = addCurrencyALimitRate;
    }

    public int getStealIncrRate() {
        return stealIncrRate;
    }

    public void setStealIncrRate(int stealIncrRate) {
        this.stealIncrRate = stealIncrRate;
    }

    public int getStealIncrTime() {
        return stealIncrTime;
    }

    public void setStealIncrTime(int stealIncrTime) {
        this.stealIncrTime = stealIncrTime;
    }

    public boolean isCanOverLoad() {
        return canOverLoad;
    }

    public void setCanOverLoad(boolean canOverLoad) {
        this.canOverLoad = canOverLoad;
    }

    public int getOverloadIntervalRate() {
        return overloadIntervalRate;
    }

    public void setOverloadIntervalRate(int overloadIntervalRate) {
        this.overloadIntervalRate = overloadIntervalRate;
    }

    public int getDurableReduceRate() {
        return durableReduceRate;
    }

    public void setDurableReduceRate(int durableReduceRate) {
        this.durableReduceRate = durableReduceRate;
    }

    public int getOverLoadEffectIncrRate() {
        return overLoadEffectIncrRate;
    }

    public void setOverLoadEffectIncrRate(int overLoadEffectIncrRate) {
        this.overLoadEffectIncrRate = overLoadEffectIncrRate;
    }

    public int getClaimCriticalRate() {
        return claimCriticalRate;
    }

    public void setClaimCriticalRate(int claimCriticalRate) {
        this.claimCriticalRate = claimCriticalRate;
    }

    public int getClaimCriticalMagnification() {
        return claimCriticalMagnification;
    }

    public void setClaimCriticalMagnification(int claimCriticalMagnification) {
        this.claimCriticalMagnification = claimCriticalMagnification;
    }

    public int getProductAddRate() {
        return productAddRate;
    }

    public void setProductAddRate(int productAddRate) {
        this.productAddRate = productAddRate;
    }

    public int getAddDurableWhenFactoryRich() {
        return AddDurableWhenFactoryRich;
    }

    public void setAddDurableWhenFactoryRich(int addDurableWhenFactoryRich) {
        AddDurableWhenFactoryRich = addDurableWhenFactoryRich;
    }

    public int getStealCriticalProbability() {
        return stealCriticalProbability;
    }

    public void setStealCriticalProbability(int stealCriticalProbability) {
        this.stealCriticalProbability = stealCriticalProbability;
    }

    public int getFactoryStoreUpRate() {
        return factoryStoreUpRate;
    }

    public void setFactoryStoreUpRate(int factoryStoreUpRate) {
        this.factoryStoreUpRate = factoryStoreUpRate;
    }

    public int getCurrencyAMaxUpRate() {
        return currencyAMaxUpRate;
    }

    public void setCurrencyAMaxUpRate(int currencyAMaxUpRate) {
        this.currencyAMaxUpRate = currencyAMaxUpRate;
    }

    public int getCurrencyAEfficientRate() {
        return currencyAEfficientRate;
    }

    public void setCurrencyAEfficientRate(int currencyAEfficientRate) {
        this.currencyAEfficientRate = currencyAEfficientRate;
    }

    public int getCanDefendMaxRarityPet() {
        return canDefendMaxRarityPet;
    }

    public void setCanDefendMaxRarityPet(int canDefendMaxRarityPet) {
        this.canDefendMaxRarityPet = canDefendMaxRarityPet;
    }

    public int getGainCurrencyBRate() {
        return gainCurrencyBRate;
    }

    public void setGainCurrencyBRate(int gainCurrencyBRate) {
        this.gainCurrencyBRate = gainCurrencyBRate;
    }

    public int getStealRareItemProbability() {
        return stealRareItemProbability;
    }

    public void setStealRareItemProbability(int stealRareItemProbability) {
        this.stealRareItemProbability = stealRareItemProbability;
    }

    public Map<Integer, Integer> getFactoryAddWorkRate() {
        return factoryAddWorkRate;
    }

    public void setFactoryAddWorkRate(Map<Integer, Integer> factoryAddWorkRate) {
        this.factoryAddWorkRate = factoryAddWorkRate;
    }

    public boolean isClaimMaxGainRareItem() {
        return claimMaxGainRareItem;
    }

    public void setClaimMaxGainRareItem(boolean claimMaxGainRareItem) {
        this.claimMaxGainRareItem = claimMaxGainRareItem;
    }

    public boolean isUnlockMoreGoods() {
        return unlockMoreGoods;
    }

    public void setUnlockMoreGoods(boolean unlockMoreGoods) {
        this.unlockMoreGoods = unlockMoreGoods;
    }

    public int getFactoryStoreMaxAddRate() {
        return factoryStoreMaxAddRate;
    }

    public void setFactoryStoreMaxAddRate(int factoryStoreMaxAddRate) {
        this.factoryStoreMaxAddRate = factoryStoreMaxAddRate;
    }

    public int getXLevelFactoryWorkIncrRate() {
        return XLevelFactoryWorkIncrRate;
    }

    public void setXLevelFactoryWorkIncrRate(int XLevelFactoryWorkIncrRate) {
        this.XLevelFactoryWorkIncrRate = XLevelFactoryWorkIncrRate;
    }*/
}
