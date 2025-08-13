package model.crazyDuel.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrazyDuelPlayerDB implements Serializable {
    private static final long serialVersionUID = -3032045391996280138L;
    private String playerIdx;
    private List<String> defeatPlayer = Collections.synchronizedList(new LinkedList<>());
    //<玩家id,当前楼层>
    private Map<String, Integer> battingData = new ConcurrentHashMap<>();

    private Map<String,Integer> scoreAddition = new HashMap<>();

    private int winNum;
    private int totalNum;

    public int getWinNum() {
        return winNum;
    }

    public void setWinNum(int winNum) {
        this.winNum = winNum;
    }

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    private List<String> lastOpponentIds = new ArrayList<>();

    private List<String> alreadySeePlayers = new ArrayList<>();

    public List<String> getAlreadySeePlayers() {
        return alreadySeePlayers;
    }

    public void setAlreadySeePlayers(List<String> alreadySeePlayers) {
        this.alreadySeePlayers = alreadySeePlayers;
    }

    public CrazyDuelPlayerDB(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public CrazyDuelPlayerDB() {
    }

    public List<String> getLastOpponentIds() {
        return lastOpponentIds;
    }

    public void setLastOpponentIds(List<String> lastOpponentIds) {
        this.lastOpponentIds = lastOpponentIds;
    }

    public String getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public List<String> getDefeatPlayer() {
        return defeatPlayer;
    }

    public void setDefeatPlayer(List<String> defeatPlayer) {
        this.defeatPlayer = defeatPlayer;
    }

    public Map<String, Integer> getBattingData() {
        return battingData;
    }

    public void setBattingData(Map<String, Integer> battingData) {
        this.battingData = battingData;
    }

    public void replaceOpponent(String opponentId, String newOpponentId) {
       this.lastOpponentIds.remove(opponentId);
       this.lastOpponentIds.add(newOpponentId);
    }

    public Map<String, Integer> getScoreAddition() {
        return scoreAddition;
    }

    public void setScoreAddition(Map<String, Integer> scoreAddition) {
        this.scoreAddition = scoreAddition;
    }

    public Collection<String> getChoosePlayers() {
        HashSet<String> strings = new HashSet<>(battingData.keySet());
        strings.addAll(defeatPlayer);
        return strings;
    }
}
