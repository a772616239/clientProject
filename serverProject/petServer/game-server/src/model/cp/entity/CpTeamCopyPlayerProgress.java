package model.cp.entity;

import java.io.Serializable;
import java.util.*;

import model.cp.CpCopyManger;
import model.cp.CpTeamManger;
import server.handler.cp.CpFunctionUtil;
import util.MapUtil;

/**
 * 组队玩法玩家数据
 */
public class CpTeamCopyPlayerProgress implements Serializable {
    private static final long serialVersionUID = 3638312900235495021L;
    private String playerIdx;
    private int header;
    /**
     * <buffId,层数></>
     */
    private Map<Integer,Integer> battleBuff = new HashMap<>();
    private boolean doubleStarReward;
    private int borderId;
    int floor;
    int curPoint;
    private boolean finish;
    private boolean success;
    private List<Integer> passPointIds = new ArrayList<>();
    private long ability;
    private int starScore;
    private String playerName;
    private boolean canRevive;
    private boolean survive;
    private boolean offline;
    private boolean leave ;

    public boolean isLeave() {
        return leave;
    }

    public void setLeave(boolean leave) {
        this.leave = leave;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public boolean isSurvive() {
        return survive;
    }

    public void setSurvive(boolean survive) {
        this.survive = survive;
    }

    public boolean isCanRevive() {
        return canRevive;
    }

    public void setCanRevive(boolean canRevive) {
        this.canRevive = canRevive;
    }

    public Map<Integer, Integer> getBattleBuff() {
        return battleBuff;
    }

    public void setBattleBuff(Map<Integer, Integer> battleBuff) {
        this.battleBuff = battleBuff;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public CpTeamCopyPlayerProgress(String playerIdx, String leaderId) {
        this.playerIdx = playerIdx;
        CpTeamMember playerInfo = CpTeamManger.getInstance().findPlayerInfo(playerIdx);
        if (playerInfo != null) {
            this.header = playerInfo.getHeader();
            this.borderId = playerInfo.getAvatarBorder();
            this.ability = playerInfo.getAbility();
            if (CpFunctionUtil.isRobot(playerIdx)) {
                this.ability = CpFunctionUtil.queryRobotAbility(leaderId);
            }
            this.playerName = playerInfo.getPlayerName();
            this.canRevive = CpCopyManger.getInstance().canRevive(playerIdx);
        }


    }


    public void addBuffId(int buffId) {
        MapUtil.add2IntMapValue(battleBuff, buffId, 1);
    }

    public void addPassPoint(int pointId) {
        passPointIds.add(pointId);
    }

    public void addStarScore(int addScore) {
        starScore += addScore;

    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public boolean isFinish() {
        if (CpFunctionUtil.isRobot(playerIdx)) {
            return true;
        }
        return finish;
    }

    public String getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public int getHeader() {
        return header;
    }

    public void setHeader(int header) {
        this.header = header;
    }


    public boolean isDoubleStarReward() {
        return doubleStarReward;
    }

    public void setDoubleStarReward(boolean doubleStarReward) {
        this.doubleStarReward = doubleStarReward;
    }

    public int getBorderId() {
        return borderId;
    }

    public void setBorderId(int borderId) {
        this.borderId = borderId;
    }

    public int getFloor() {
        return floor;
    }

    public int getCurPoint() {
        return curPoint;
    }

    public void setCurPoint(int curPoint) {
        this.curPoint = curPoint;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Integer> getPassPointIds() {
        return passPointIds;
    }

    public void setPassPointIds(List<Integer> passPointIds) {
        this.passPointIds = passPointIds;
    }

    public long getAbility() {
        return ability;
    }

    public void setAbility(long ability) {
        this.ability = ability;
    }

    public int getStarScore() {
        return starScore;
    }

    public void setStarScore(int starScore) {
        this.starScore = starScore;
    }


}
