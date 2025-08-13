package model.crazyDuel.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

public class BattleUpdatePackage implements Serializable {
    private static final long serialVersionUID = 6491891925661145899L;
    private String playerIdx;
    private int winIncr;
    private int failIncr;

    public int getWinIncr() {
        return winIncr;
    }

    public void setWinIncr(int winIncr) {
        this.winIncr = winIncr;
    }

    public int getFailIncr() {
        return failIncr;
    }

    public void setFailIncr(int failIncr) {
        this.failIncr = failIncr;
    }

    private Map<Integer, BattleUpdateFloor> updateFloorMap = new HashMap<>();

    public BattleUpdatePackage(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public String getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public Map<Integer, BattleUpdateFloor> getUpdateFloorMap() {
        return updateFloorMap;
    }

    public void setUpdateFloorMap(Map<Integer, BattleUpdateFloor> updateFloorMap) {
        this.updateFloorMap = updateFloorMap;
    }
}
