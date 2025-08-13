package model.farmmine.bean;

public class FarmMineOfferBean {
    private int idx;
    private int baseId;
    private String playerIdx;
    private int price;
    private long time;
    private boolean isFirst = false;

    public int getOfferNum() {
        return offerNum;
    }

    public void setOfferNum(int offerNum) {
        this.offerNum = offerNum;
    }

    private int offerNum;

    public int getBaseId() {
        return baseId;
    }

    public void setBaseId(int baseId) {
        this.baseId = baseId;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getPlayerIdx() {
        return playerIdx;
    }

    public void setPlayerIdx(String playerIdx) {
        this.playerIdx = playerIdx;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setFirst(boolean first) {
        isFirst = first;
    }
}
