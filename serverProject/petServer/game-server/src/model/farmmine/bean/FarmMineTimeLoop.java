package model.farmmine.bean;

import java.util.Map;
import java.util.TreeMap;

/**
 * 农场矿场一个活动周期时间数据
 */
public class FarmMineTimeLoop {
    private long startTime;// 活动周期开始
    private long endTime;// 活动周期结束
    private long startAuctionTime;// 活动周期竞拍开始
    private long endAuctionTime;// 竞价周期竞拍结束
    private Map<Integer, FarmMineAuctionTime> auctionTime = new TreeMap<>();// 竞价时间段
    private long startGiveTime;// 活动周期收获开始时间
    private long endGiveTime;// 竞价时周期收获结束时间
    private boolean needCheck = true;

    public boolean isNeedCheck() {
        return needCheck;
    }

    public void setNeedCheck(boolean needCheck) {
        this.needCheck = needCheck;
    }

    // 活动开始时间作为标记时间
    public long getJointime() {
        return startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartAuctionTime() {
        return startAuctionTime;
    }

    public void setStartAuctionTime(long startAuctionTime) {
        this.startAuctionTime = startAuctionTime;
    }

    public long getEndAuctionTime() {
        return endAuctionTime;
    }

    public void setEndAuctionTime(long endAuctionTime) {
        this.endAuctionTime = endAuctionTime;
    }

    public Map<Integer, FarmMineAuctionTime> getAuctionTime() {
        return auctionTime;
    }

    public void setAuctionTime(Map<Integer, FarmMineAuctionTime> auctionTime) {
        this.auctionTime = auctionTime;
    }

    public long getStartGiveTime() {
        return startGiveTime;
    }

    public void setStartGiveTime(long startGiveTime) {
        this.startGiveTime = startGiveTime;
    }

    public long getEndGiveTime() {
        return endGiveTime;
    }

    public void setEndGiveTime(long endGiveTime) {
        this.endGiveTime = endGiveTime;
    }

    public boolean isAuction(long currTime) {
        return currTime >= startAuctionTime && currTime < endAuctionTime;
    }

    public boolean isGive(long currTime) {
        return currTime >= startGiveTime && currTime < endTime;
    }

}
