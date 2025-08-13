package model.crazyDuel.dto;

import java.io.Serializable;

public class CrazyDuelPlayerPageDB implements Serializable {
    private static final long serialVersionUID = 7976172923764030841L;
    private String playerId;
    private String name;
    private int duelCount;
    /**
     * 这里是其他玩家攻击该玩家的胜率
     */
    private double successRate;
    private long ability;
    private int headId;
    private int headBorderId;
    private int playerLevel;
    private long publishTime;
    private int teamType;
    private int winCount;
    private boolean robot;
    private boolean publish;

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isRobot() {
        return robot;
    }

    public void setRobot(boolean robot) {
        this.robot = robot;
    }

    public int getHonLv() {
        return honLv;
    }

    public void setHonLv(int honLv) {
        this.honLv = honLv;
    }

    private int honLv;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public long getAbility() {
        return ability;
    }

    public void setAbility(long ability) {
        this.ability = ability;
    }


    public int getHeadId() {
        return headId;
    }

    public void setHeadId(int headId) {
        this.headId = headId;
    }

    public int getHeadBorderId() {
        return headBorderId;
    }

    public void setHeadBorderId(int headBorderId) {
        this.headBorderId = headBorderId;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel(int playerLevel) {
        this.playerLevel = playerLevel;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public int getTeamType() {
        return teamType;
    }

    public void setTeamType(int teamType) {
        this.teamType = teamType;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

}
