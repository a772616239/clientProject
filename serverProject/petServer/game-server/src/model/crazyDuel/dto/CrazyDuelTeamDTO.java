package model.crazyDuel.dto;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

public class CrazyDuelTeamDTO implements Serializable {
    private static final long serialVersionUID = -3880086314217171436L;
    private int teamType = 1;     //0:普通 1热门
    private long ability;      //战力
    private int duelCount;      //挑战次数
    private double successRate;     //成功率
    private List<Integer> cfgId;    //宠物bookId
    private List<Integer> rarity;    //宠物品质与宠物id一一对应
    private List<Integer> teamBuff;    //编队buff
    private boolean completeDuel;        //是否完成挑战
    private int floor;              //编队楼层

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

    public List<Integer> getCfgId() {
        return cfgId;
    }

    public void setCfgId(List<Integer> cfgId) {
        this.cfgId = cfgId;
    }

    public List<Integer> getRarity() {
        return rarity;
    }

    public void setRarity(List<Integer> rarity) {
        this.rarity = rarity;
    }

    public List<Integer> getTeamBuff() {
        return teamBuff;
    }

    public void setTeamBuff(List<Integer> teamBuff) {
        this.teamBuff = teamBuff;
    }

    public boolean isCompleteDuel() {
        return completeDuel;
    }

    public void setCompleteDuel(boolean completeDuel) {
        this.completeDuel = completeDuel;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }
}
