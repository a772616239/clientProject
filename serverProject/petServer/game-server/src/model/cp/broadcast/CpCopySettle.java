package model.cp.broadcast;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import model.cp.entity.CpCopyMap;
import model.cp.entity.CpTeamCopyPlayerProgress;
import server.handler.cp.CpFunctionUtil;

public class CpCopySettle implements Serializable {
    private static final long serialVersionUID = -5273063307730529135L;
    private String mapId;
    private List<String> alreadySettlePlayerIdx = new LinkedList<>();
    private String teamName;
    private int teamScore;
    private List<String> needSettlePlayers;

    public List<String> getAlreadySettlePlayerIdx() {
        return alreadySettlePlayerIdx;
    }

    public void setAlreadySettlePlayerIdx(List<String> alreadySettlePlayerIdx) {
        this.alreadySettlePlayerIdx = alreadySettlePlayerIdx;
    }

    public List<String> getNeedSettlePlayers() {
        return needSettlePlayers;
    }

    public void setNeedSettlePlayers(List<String> needSettlePlayers) {
        this.needSettlePlayers = needSettlePlayers;
    }

    public int getTeamScore() {
        return teamScore;
    }

    public void setTeamScore(int teamScore) {
        this.teamScore = teamScore;
    }

    private Map<String, List<Integer>> alreadyClaimRewardId;

    public CpCopySettle(CpCopyMap mapData) {
        this.mapId = mapData.getMapId();
        this.teamName = mapData.getTeamName();
        this.alreadyClaimRewardId = mapData.getAlreadyClaimRewardId();
        this.teamScore = mapData.getTeamScore();
        needSettlePlayers = mapData.getInitRealPlayerIds();
    }

    public Map<String, List<Integer>> getAlreadyClaimRewardId() {
        if (alreadyClaimRewardId == null) {
            return Collections.emptyMap();
        }
        return alreadyClaimRewardId;
    }

    public void addAllSettlePlayerIdx(Set<String> localPlayerIdx) {
        alreadySettlePlayerIdx.addAll(localPlayerIdx);

    }

    public boolean allSettle() {
        return alreadySettlePlayerIdx.containsAll(needSettlePlayers);

    }

    public String getMapId() {
        return mapId;
    }

    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
