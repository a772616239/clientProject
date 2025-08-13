package model.farmmine.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FarmMinePlayerOffer {
    private String playerIdx;
    private int firstIdx = 0;
    private Map<Integer, Integer> offerInfo = new ConcurrentHashMap<>();
    private Map<Integer, Long> offerTime = new ConcurrentHashMap<>();

    public String getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public int getFirstIdx() {
        return firstIdx;
    }

    public void setFirstIdx(int firstIdx) {
        this.firstIdx = firstIdx;
    }

    public Map<Integer, Integer> getOfferInfo() {
        return offerInfo;
    }

    public void setOfferInfo(Map<Integer, Integer> offerInfo) {
        this.offerInfo = offerInfo;
    }

    public Map<Integer, Long> getOfferTime() {
        return offerTime;
    }

    public void setOfferTime(Map<Integer, Long> offerTime) {
        this.offerTime = offerTime;
    }
}
