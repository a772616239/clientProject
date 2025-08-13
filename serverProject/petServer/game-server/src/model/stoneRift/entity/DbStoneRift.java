package model.stoneRift.entity;

import common.entity.DBEntity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import protocol.Common;

public class DbStoneRift extends DBEntity implements Serializable {

    private static final long serialVersionUID = 7621632631435595921L;

    private int level;
    private int exp;

    private Map<Integer, DbStoneRiftFactory> factoryMap = new HashMap<>();

    private Common.Reward currencyAReward;

    private Common.Reward currencyBReward;

    private long overLoadExpire;
    private long overLoadStart;
    private long nextCanOverLoad;
    private DbStoneRiftEvent event = new DbStoneRiftEvent();
    private int mapId;

    private Map<Integer, String> defendPet = new HashMap<>();

    private DbStoneRiftAchievement achievement = new DbStoneRiftAchievement();

    private DbPlayerWorldMap dbPlayerWorldMap = new DbPlayerWorldMap();

    private long nextSettleTime;

    private long nextCanClaimTime;

    /**
     * 储存货币B次数
     */
    private int storeCurrencyBTimes;

    private Map<Integer,Integer> factoryAddWorkRate = new HashMap<>();

    public Map<Integer, Integer> getFactoryAddWorkRate() {
        return factoryAddWorkRate;
    }

    public void setFactoryAddWorkRate(Map<Integer, Integer> factoryAddWorkRate) {
        this.factoryAddWorkRate = factoryAddWorkRate;
    }

    public StoneRiftScience getDbScience() {
        return dbScience;
    }

    public void setDbScience(StoneRiftScience dbScience) {
        this.dbScience = dbScience;
    }

    private StoneRiftScience dbScience = new StoneRiftScience();


    public long getNextSettleTime() {
        return nextSettleTime;
    }

    public Common.Reward getCurrencyAReward() {
        return currencyAReward;
    }

    public void setCurrencyAReward(Common.Reward currencyAReward) {
        this.currencyAReward = currencyAReward;
    }

    public Common.Reward getCurrencyBReward() {
        return currencyBReward;
    }

    public void setCurrencyBReward(Common.Reward currencyBReward) {
        this.currencyBReward = currencyBReward;
    }

    public void setNextSettleTime(long nextSettleTime) {
        this.nextSettleTime = nextSettleTime;
    }

    public DbStoneRift() {
        event = new DbStoneRiftEvent();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public Map<Integer, DbStoneRiftFactory> getFactoryMap() {
        return factoryMap;
    }

    public void setFactoryMap(Map<Integer, DbStoneRiftFactory> factoryMap) {
        this.factoryMap = factoryMap;
    }

    public long getOverLoadExpire() {
        return overLoadExpire;
    }

    public void setOverLoadExpire(long overLoadExpire) {
        this.overLoadExpire = overLoadExpire;
    }

    public long getNextCanOverLoad() {
        return nextCanOverLoad;
    }

    public void setNextCanOverLoad(long nextCanOverLoad) {
        this.nextCanOverLoad = nextCanOverLoad;
    }

    public DbStoneRiftEvent getEvent() {
        return event;
    }

    public void setEvent(DbStoneRiftEvent event) {
        this.event = event;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public long getNextCanClaimTime() {
        return nextCanClaimTime;
    }

    public void setNextCanClaimTime(long nextCanClaimTime) {
        this.nextCanClaimTime = nextCanClaimTime;
    }

    public long getOverLoadStart() {
        return overLoadStart;
    }

    public void setOverLoadStart(long overLoadStart) {
        this.overLoadStart = overLoadStart;
    }

    public Map<Integer, String> getDefendPet() {
        return defendPet;
    }

    public void setDefendPet(Map<Integer, String> defendPet) {
        this.defendPet = defendPet;
    }

    public DbStoneRiftAchievement getAchievement() {
        return achievement;
    }

    public void setAchievement(DbStoneRiftAchievement achievement) {
        this.achievement = achievement;
    }


    public int getStoreCurrencyBTimes() {
        return storeCurrencyBTimes;
    }

    public void setStoreCurrencyBTimes(int storeCurrencyBTimes) {
        this.storeCurrencyBTimes = storeCurrencyBTimes;
    }

    public DbPlayerWorldMap getDbPlayerWorldMap() {
        return dbPlayerWorldMap;
    }

    public void setDbPlayerWorldMap(DbPlayerWorldMap dbPlayerWorldMap) {
        this.dbPlayerWorldMap = dbPlayerWorldMap;
    }
}
