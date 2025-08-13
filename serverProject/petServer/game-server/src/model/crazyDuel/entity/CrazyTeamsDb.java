package model.crazyDuel.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import protocol.Battle;

public class CrazyTeamsDb implements Serializable {

    private static final long serialVersionUID = 8663780171535970032L;
    private int teamType;     //0:普通 1热门
    private long ability;      //战力
    private int duelCount;      //挑战次数
    private double successRate;     //成功率
    private List<Integer> petCfgIdList = new ArrayList<>();    //宠物bookId
    private List<Integer> rarityList = new ArrayList<>();    //宠物品质与宠物id一一对应
    private List<Integer> petLevel = new ArrayList<>();
    private int floor;              //编队楼层
    private int playerLevel;

    private int winCount;

    private int failCount;

    private List<Battle.BattlePetData> battleData = new ArrayList<>();

    public void addCfgId(int petBookId) {
        petCfgIdList.add(petBookId);
    }

    public void addRarity(int petRarity) {
        rarityList.add(petRarity);
    }

    public int getTeamType() {
        return teamType;
    }

    public void setTeamType(int teamType) {
        this.teamType = teamType;
    }

    public long getAbility() {
        return ability;
    }

    public void setAbility(long ability) {
        this.ability = ability;
    }

    public int getDuelCount() {
        return duelCount;
    }

    public void setDuelCount(int duelCount) {
        this.duelCount = duelCount;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public List<Integer> getPetCfgIdList() {
        return petCfgIdList;
    }

    public void setPetCfgIdList(List<Integer> petCfgIdList) {
        this.petCfgIdList = petCfgIdList;
    }

    public List<Integer> getRarityList() {
        return rarityList;
    }

    public void setRarityList(List<Integer> rarityList) {
        this.rarityList = rarityList;
    }


    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public List<Battle.BattlePetData> getBattleData() {
        return battleData;
    }

    public void setBattleData(List<Battle.BattlePetData> battleData) {
        this.battleData = battleData;
    }


    public void addLevel(int petLvl) {
        this.petLevel.add(petLvl);
    }

    public List<Integer> getPetLevel() {
        return petLevel == null ? Collections.emptyList() : petLevel;
    }

    public void setPetLevel(List<Integer> petLevel) {
        this.petLevel = petLevel;
    }
}
