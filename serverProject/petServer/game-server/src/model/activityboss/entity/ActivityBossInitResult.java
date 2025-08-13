package model.activityboss.entity;

import entity.CommonResult;

public class ActivityBossInitResult extends CommonResult {
    /**
     * 玩家已经挑战次数
     */
    private int times;

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }
}
