package model.team.entity;

import common.entity.DBEntity;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

public class TeamsDB extends DBEntity implements Serializable {
    private static final long serialVersionUID = -3289186804973079016L;

    public TeamsDB() {
        unlockTeams = 0;
        unlockPosition = 0;
        buyTeamCount = 0;
        teamsMap = new ConcurrentHashMap<>();
        nowUsedTeamMap = new ConcurrentHashMap<>();
        coupTeamLv = 0;
    }

    /**
     * 解锁小队数量
     */
    private int unlockTeams;
    /**
     * 解锁的宠物位置数量
     */
    private int unlockPosition;
    /**
     *
     */
    private int buyTeamCount;
    /**
     * 所有队伍信息
     */
    private Map<Integer, Team> teamsMap;
    /**
     * 当前使用小队，<TeamTypeNum,TeamNum>
     */
    private Map<Integer, Integer> nowUsedTeamMap;
    /**
     * 魔晶队伍等级
     */
    private int coupTeamLv;

    public void putTeams(int teamType, Team team) {
        this.teamsMap.put(teamType, team);
    }

    public void putNowUsedTeam(int teamType, int team) {
        this.nowUsedTeamMap.put(teamType, team);
    }

    public int getUnlockTeams() {
        return unlockTeams;
    }

    public void setUnlockTeams(int unlockTeams) {
        this.unlockTeams = unlockTeams;
    }

    public int getUnlockPosition() {
        return unlockPosition;
    }

    public void setUnlockPosition(int unlockPosition) {
        this.unlockPosition = unlockPosition;
    }

    public int getBuyTeamCount() {
        return buyTeamCount;
    }

    public void setBuyTeamCount(int buyTeamCount) {
        this.buyTeamCount = buyTeamCount;
    }

    public Map<Integer, Team> getTeamsMap() {
        return teamsMap;
    }

    public void setTeamsMap(Map<Integer, Team> teamsMap) {
        this.teamsMap = teamsMap;
    }

    public Map<Integer, Integer> getNowUsedTeamMap() {
        return nowUsedTeamMap;
    }

    public void setNowUsedTeamMap(Map<Integer, Integer> nowUsedTeamMap) {
        this.nowUsedTeamMap = nowUsedTeamMap;
    }

    public int getCoupTeamLv() {
        return coupTeamLv;
    }

    public void setCoupTeamLv(int coupTeamLv) {
        this.coupTeamLv = coupTeamLv;
    }
}
