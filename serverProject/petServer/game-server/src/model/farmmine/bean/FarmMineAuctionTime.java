package model.farmmine.bean;

import model.farmmine.util.FarmMineUtil;

import java.util.ArrayList;
import java.util.List;

public class FarmMineAuctionTime implements Cloneable {
    private int loop;
    private long startTime;
    private long endTimeoffer;
    private long endTime;
    private long startTimeView;
    private long endTimeView;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    private int state = 0;//默认为开启
    private int tempState = 0;

    private List<Integer> mineIds = new ArrayList<>();

    public int getLoop() {
        return loop;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTimeoffer() {
        return endTimeoffer;
    }

    public void setEndTimeoffer(long endTimeoffer) {
        this.endTimeoffer = endTimeoffer;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTimeAndOfferTime(long endTime, long offerEndTime) {
        this.endTime = endTime;
        this.endTimeView = endTime;
        this.startTimeView = offerEndTime;
        this.endTimeoffer = startTimeView;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getStartTimeView() {
        return startTimeView;
    }

    public void setStartTimeView(long startTimeView) {
        this.startTimeView = startTimeView;
    }

    public long getEndTimeView() {
        return endTimeView;
    }

    public void setEndTimeView(long endTimeView) {
        this.endTimeView = endTimeView;
    }

    public void backState(long now) {
        tempState = FarmMineUtil.STATE_NOT_OPEN;
        if (now > startTime && now < endTime) {
            if (now < endTimeoffer) {
                tempState = FarmMineUtil.STATE_OFFERPRICE;
            } else {
                tempState = FarmMineUtil.STATE_VIEW;
            }
        } else {
            tempState = FarmMineUtil.STATE_NOT_OPEN;
        }
    }

    public void stateGive() {
        state = tempState;
    }

    public boolean isNeedOffer() {
        if (state == FarmMineUtil.STATE_OFFERPRICE) {// 已经在出价阶段
            return false;
        }
        return tempState == FarmMineUtil.STATE_OFFERPRICE;
    }

    public boolean isNeedView() {
        if (state == FarmMineUtil.STATE_VIEW) {// 已经在出价阶段
            return false;
        }
        return tempState == FarmMineUtil.STATE_VIEW;
    }

    public boolean isNeedClose() {
        if (state == FarmMineUtil.STATE_NOT_OPEN) {// 已经在出价阶段
            return false;
        }
        return tempState == FarmMineUtil.STATE_NOT_OPEN;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public List<Integer> getMineIds() {
        return mineIds;
    }

    public void setMineIds(List<Integer> mineIds) {
        this.mineIds = mineIds;
    }
}
