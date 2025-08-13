package model.shop.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import protocol.Shop;

public class PlayerShopInfo implements Serializable {
    private static final long serialVersionUID = -2378704284455244695L;
    /**
     * 下次自动刷新时间
     */
    private long nextRefreshTime;

    /**
     * 手动刷新次数
     */
    private int manualRefreshTimes;

    /**
     * 购买记录
     */
    Map<Integer, Shop.GoodsInfo> buyRecord = new HashMap<>();

    /**
     * 自动刷新次数
     */
    private int autoRefreshTimes;

    /**
     * 完成任务id
     */
    List<Integer> completeMissionId = new ArrayList<>();

    public void putBuyRecord(int id, Shop.GoodsInfo goodsInfo) {
        this.buyRecord.put(id, goodsInfo);
    }

    public void clearBuyRecord() {
        buyRecord.clear();
    }

    public PlayerShopInfo clearManualRefreshTimes() {
        this.manualRefreshTimes = 0;
        return this;
    }

    public long getNextRefreshTime() {
        return nextRefreshTime;
    }

    public void setNextRefreshTime(long nextRefreshTime) {
        this.nextRefreshTime = nextRefreshTime;
    }

    public int getManualRefreshTimes() {
        return manualRefreshTimes;
    }

    public void setManualRefreshTimes(int manualRefreshTimes) {
        this.manualRefreshTimes = manualRefreshTimes;
    }

    public Map<Integer, Shop.GoodsInfo> getBuyRecord() {
        return buyRecord;
    }

    public void setBuyRecord(Map<Integer, Shop.GoodsInfo> buyRecord) {
        this.buyRecord = buyRecord;
    }

    public int getAutoRefreshTimes() {
        return autoRefreshTimes;
    }

    public void setAutoRefreshTimes(int autoRefreshTimes) {
        this.autoRefreshTimes = autoRefreshTimes;
    }

    public List<Integer> getCompleteMissionId() {
        return completeMissionId;
    }

    public void setCompleteMissionId(List<Integer> completeMissionId) {
        this.completeMissionId = completeMissionId;
    }
}
