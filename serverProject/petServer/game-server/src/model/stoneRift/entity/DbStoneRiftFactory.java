package model.stoneRift.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.stoneRift.stoneriftEntity;
import protocol.Common;

import static protocol.StoneRift.StoneRiftScienceEnum.SRSE_AddDurableWhenFactoryRich;

public class DbStoneRiftFactory implements Serializable {

    private static final long serialVersionUID = -3829546374361482867L;

    private int cfgId;

    private long lastSettleTime;

    private int level;

    private int claimTimes;

    private int petCfgId;

    private int petRarity;

    private String petId;

    private int petType;

    private int petAddition;

    private long nextCanClaimTime;

    private List<Common.Reward> outPut = Collections.synchronizedList(new ArrayList<>());

    private List<Common.Reward> onceStealReduce = new ArrayList<>();


    private int produceTime;

    /**
     * 当前耐久
     */
    private int curDurable;

    /**
     * 最大耐久
     */
    private int maxDurable;

    /**
     * 当前存储(次数)
     */
    private int curStore;

    /**
     * 最大存储(次数)
     */
    private int maxStore;

    /**
     * 是否获取过满仓资源
     */
    private boolean claimFullReward;

    public boolean isClaimFullReward() {
        return claimFullReward;
    }

    public void setClaimFullReward(boolean claimFullReward) {
        this.claimFullReward = claimFullReward;
    }

    public int getMaxStore() {
        return maxStore;
    }

    public void setMaxStore(int maxStore) {
        this.maxStore = maxStore;
    }


    private List<Common.Reward> baseReward = new ArrayList<>();

    private List<Common.Reward> settleReward = new ArrayList<>();


    public List<Common.Reward> getBaseReward() {
        return baseReward;
    }

    public void setBaseReward(List<Common.Reward> baseReward) {
        this.baseReward = baseReward;
    }

    public int getCurStore() {
        return curStore;
    }

    public void setCurStore(int curStore) {
        this.curStore = curStore;
    }

    public int getPetAddition() {
        return petAddition;
    }

    public void setPetAddition(int petAddition) {
        this.petAddition = petAddition;
    }

    public int getPetCfgId() {
        return petCfgId;
    }

    public void setPetCfgId(int petCfgId) {
        this.petCfgId = petCfgId;
    }

    public int getPetRarity() {
        return petRarity;
    }

    public void setPetRarity(int petRarity) {
        this.petRarity = petRarity;
    }

    public String getPetId() {
        return petId;
    }

    public int getPetType() {
        return petType;
    }

    public void setPetType(int petType) {
        this.petType = petType;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }


    public int getClaimTimes() {
        return claimTimes;
    }

    public void setClaimTimes(int claimTimes) {
        this.claimTimes = claimTimes;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCurDurable() {
        return curDurable;
    }

    public int getCurRealDurable(DbStoneRiftFactory factory, stoneriftEntity entity) {
        return factory.getCurDurable() + entity.getScienceEffect(SRSE_AddDurableWhenFactoryRich);
    }

    public void setCurDurable(int curDurable) {
        this.curDurable = curDurable;
    }

    public int getMaxDurable() {
        return maxDurable;
    }

    public void setMaxDurable(int maxDurable) {
        this.maxDurable = maxDurable;
    }


    public int getCfgId() {
        return cfgId;
    }

    public void setCfgId(int cfgId) {
        this.cfgId = cfgId;
    }


    public long getLastSettleTime() {
        return lastSettleTime;
    }

    public void setLastSettleTime(long lastSettleTime) {
        this.lastSettleTime = lastSettleTime;
    }

    public List<Common.Reward> getOutPut() {
        return outPut;
    }

    public void setOutPut(List<Common.Reward> outPut) {
        this.outPut = outPut;
    }

    public long getNextCanClaimTime() {
        return nextCanClaimTime;
    }

    public void setNextCanClaimTime(long nextCanClaimTime) {
        this.nextCanClaimTime = nextCanClaimTime;
    }

    public List<Common.Reward> getSettleReward() {
        return settleReward;
    }

    public void setSettleReward(List<Common.Reward> settleReward) {
        this.settleReward = settleReward;
    }


    public int getProduceTime() {
        return produceTime;
    }

    public void setProduceTime(int produceTime) {
        this.produceTime = produceTime;
    }

    public List<Common.Reward> getOnceStealReduce() {
        return onceStealReduce;
    }

    public void setOnceStealReduce(List<Common.Reward> onceStealReduce) {
        this.onceStealReduce = onceStealReduce;
    }
}
