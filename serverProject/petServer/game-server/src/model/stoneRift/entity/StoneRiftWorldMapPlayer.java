package model.stoneRift.entity;

import model.stoneRift.StoneRiftCfgManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoneRiftWorldMapPlayer implements Serializable {
    private static final long serialVersionUID = 6717812901426537135L;
    private String playerIdx;
    private String playerName;
    private int riftLv;
    private int serverIndex;
    private int icon;
    private int header;
    private int headBroder;
    private int exp;
    private int backGroundId;

    private List<String> stealPlayers = new ArrayList<>();
    public List<String> getStealPlayers() {
        return stealPlayers;
    }

    public void setStealPlayers(List<String> stealPlayers) {
        this.stealPlayers = stealPlayers;
    }


    public int getHeader() {
        return header;
    }

    public int getBackGroundId() {
        return backGroundId;
    }

    public void setBackGroundId(int backGroundId) {
        this.backGroundId = backGroundId;
    }

    public void setHeader(int header) {
        this.header = header;
    }

    private Map<Integer, DbStoneRiftSteal> canStealMap = new HashMap<>();

    public String getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public Map<Integer, DbStoneRiftSteal> getCanStealMap() {
        return canStealMap;
    }

    public void setCanStealMap(Map<Integer, DbStoneRiftSteal> canStealMap) {
        this.canStealMap = canStealMap;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getRiftLv() {
        return riftLv;
    }

    public void setRiftLv(int riftLv) {
        this.riftLv = riftLv;
    }

    public int getServerIndex() {
        return serverIndex;
    }

    public void setServerIndex(int serverIndex) {
        this.serverIndex = serverIndex;
    }

    public boolean isCanSteal(String playerIdx) {
        return isCanSteal() && !stealPlayers.contains(playerIdx);
    }


    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getHeadBroder() {
        return headBroder;
    }

    public void setHeadBroder(int headBroder) {
        this.headBroder = headBroder;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public boolean isCanSteal() {
        return getCanStealMap().values().stream().anyMatch(e -> e.isCanSteal()
                && StoneRiftCfgManager.getInstance().getCanStolenTime() > e.getStealCount());
    }
}
